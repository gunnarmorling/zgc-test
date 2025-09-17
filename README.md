# zgc-test

A demo application for testing garbage collectors.

Start Postgres:

```shell script
docker compose up
```

Build the application:

```shell script
./mvnw clean verify -Dquick
```

Run the benchmark:

```shell script
./bench.sh --duration 120 --bench random --mem 4096 --qps 1000
```
Create a histogram for https://hdrhistogram.github.io/HdrHistogram/plotFiles.html (discarding the first 30 sec for example):

```shell script
cat random_Z_25.ea.34-open_1000.bin | vegeta encode -to csv | tail -n +30001 | java -cp $M2_HOME/repository/org/hdrhistogram/HdrHistogram/2.2.2/HdrHistogram-2.2.2.jar:./target/test-classes org.acme.HistogramCreator > random_Z_25.ea.34-open_1000.hdr
```

## License

This code base is available under the Apache License, version 2.
