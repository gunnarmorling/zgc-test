package org.acme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;

public class HistogramCreator {

	private static final Histogram HDR_HISTOGRAM =
		    new Histogram(TimeUnit.MINUTES.toNanos(1), 3);

	public static void main(String[] args) throws Exception {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
//		try (BufferedReader br = new BufferedReader(new FileReader("results.csv"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(",");
		        HDR_HISTOGRAM.recordValue(Long.valueOf(values[2]));
		    }
		}

//		HDR_HISTOGRAM.outputPercentileDistribution(new PrintStream(new File("hdrhist.hdr")), 1000_000.0);
		HDR_HISTOGRAM.outputPercentileDistribution(new PrintStream(System.out), 1000_000.0);
HDR_HISTOGRAM.reset();

	}


}
