#!/bin/bash

source "$HOME/.sdkman/bin/sdkman-init.sh"

set -e
set -o pipefail
set -x

# Default values
DEFAULT_DURATION=10
DEFAULT_BENCH="allocationheavy"
DEFAULT_MEM=4096
DEFAULT_QPS=5000

# Function to show usage
usage() {
    echo "Usage: $0 [--duration DURATION] [--bench BENCH] [--mem MEM]"
    echo "  --duration DURATION  Duration in seconds (default: $DEFAULT_DURATION)"
    echo "  --bench BENCH        Benchmark name (default: $DEFAULT_BENCH)"
    echo "  --mem MEM            Memory in MB (default: $DEFAULT_MEM)"
    echo "  --qps QPS            Queries/sec (default: $DEFAULT_QPS)"
    echo "  -h, --help          Show this help message"
    exit 1
}

# Parse command line arguments
DURATION=$DEFAULT_DURATION
BENCH=$DEFAULT_BENCH
MEM=$DEFAULT_MEM
QPS=$DEFAULT_QPS

while [[ $# -gt 0 ]]; do
    case $1 in
        --duration)
            DURATION="$2"
            shift 2
            ;;
        --bench)
            BENCH="$2"
            shift 2
            ;;
        --mem)
            MEM="$2"
            shift 2
            ;;
        --qps)
            QPS="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown parameter: $1"
            usage
            ;;
    esac
done

# Validate duration is a number
if ! [[ "$DURATION" =~ ^[0-9]+$ ]]; then
    echo "Error: Duration must be a positive integer"
    exit 1
fi

# Validate memory is a number
if ! [[ "$MEM" =~ ^[0-9]+$ ]]; then
    echo "Error: Memory must be a positive integer"
    exit 1
fi

# Validate qps is a number
if ! [[ "$QPS" =~ ^[0-9]+$ ]]; then
    echo "Error: QPS must be a positive integer"
    exit 1
fi

echo "Running with:"
echo "  Duration: $DURATION"
echo "  Bench: $BENCH"
echo "  Memory: $MEMm"
echo "  Queries/sec: $QPS"

GCS=("G1" "Z" "Parallel" "Z")
JAVA_VERSIONS=("24.0.2-open" "24.0.2-open" "24.0.2-open" "21-tem")

GCS=("G1" "Z")
JAVA_VERSIONS=("25.ea.34-open" "25.ea.34-open")

# GCS=("G1" "Z")
# JAVA_VERSIONS=("24.0.2-open" "24.0.2-open")

for i in "${!GCS[@]}"; do
    gc="${GCS[i]}"
    java_version="${JAVA_VERSIONS[i]}"
    echo "Processing GC: $gc, Java Version: $java_version"

    sdk use java $java_version
    export RANDOM_COUNT=200
    numactl --physcpubind=0-3 java -XX:+AlwaysPreTouch -XX:ActiveProcessorCount=4 -Xmx$(echo $MEM)m -Xms$(echo $MEM)m -XX:+Use$(echo $gc)GC -XX:StartFlightRecording:filename=$(echo $BENCH)_$(echo $gc)_$(echo $java_version)_$(echo $QPS).jfr,dumponexit=true,maxsize=500MB,settings=default.jfc -jar target/quarkus-app/quarkus-run.jar &
    sleep 10

    # ../../oha -z $(echo $DURATION)s -c 200 --http2 -q $(echo $QPS) --latency-correction --output $(echo $BENCH)_$(echo $gc)_$(echo $java_version)_$(echo $QPS).csv --output-format csv http://localhost:8080/purchase_orders/$(echo $BENCH)

    date
    echo "GET http://localhost:8080/purchase_orders/$BENCH" | ./vegeta attack -duration=$(echo $DURATION)s -rate $(echo $QPS) -max-connections 10 -workers 200 | tee $(echo $BENCH)_$(echo $gc)_$(echo $java_version)_$(echo $QPS).bin | ./vegeta report
    date

    jcmd $(jps | grep quarkus-run.jar | cut -d' ' -f1) JFR.dump name=1
    kill -9 $(jps | grep quarkus-run.jar | cut -d' ' -f1)
done
