package io.github.aerospike_examples.timeseries.benchmarker;

import com.aerospike.client.AerospikeClient;
import io.github.aerospike_examples.timeseries.TimeSeriesClient;
import io.github.aerospike_examples.timeseries.benchmarker.util.ClientUtils;
import io.github.aerospike_examples.timeseries.util.Constants;
import io.github.aerospike_examples.timeseries.util.Utilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

import io.github.aerospike_examples.timeseries.DataPoint;

/**
 * Time Series Writer class to write out a time series to the command line
 */
@SuppressWarnings("WeakerAccess") // Want to expose class
public class TimeSeriesWriter {

    // Specify Aerospike cluster, namespace, set
    private final AerospikeClient asClient;
    private final String asNamespace;
    private final String asSet;
	public Date timestamp_ritesh;

    private TimeSeriesWriter(AerospikeClient asClient, String asNamespace, String asSet, String timestamp_ritesh) {
        this.asClient = asClient;
        this.asNamespace = asNamespace;
        this.asSet = asSet;

		try {
			this.timestamp_ritesh = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(timestamp_ritesh);
		} catch (Exception e) {
		}
    }

    /**
     * Entry point for command line use of the time series reader
     *
     * @param args command line arguments as String[]
     */
    public static void main(String[] args) {
        try {
            TimeSeriesWriter timeSeriesWriter = initBenchmarkerFromStringArgs(args);
            timeSeriesWriter.run(args);
        } catch (Utilities.ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TimeSeriesWriter", OptionsHelper.cmdLineOptionsForReader());
        }
    }

    /**
     * Helper method allowing a TimeSeriesBenchmarker to be initialised from an array of Strings - as per main method
     * Protected visibility to allow testing use
     */
    private static TimeSeriesWriter initBenchmarkerFromStringArgs(String[] args) throws Utilities.ParseException {
        TimeSeriesWriter timeSeriesWriter;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(OptionsHelper.cmdLineOptionsForReader(), args);

            timeSeriesWriter = new TimeSeriesWriter(
                    new AerospikeClient(
                            OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.HOST_FLAG),
                            3100),
                    OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.NAMESPACE_FLAG),
                    OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.TIME_SERIES_SET_FLAG),
					OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.RITESH_START_TIME)
            );
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TimeSeriesWriter", OptionsHelper.cmdLineOptionsForReader());
            throw (new Utilities.ParseException(e.getMessage()));
        }
        return timeSeriesWriter;
    }

    private void run(String[] args) {
        TimeSeriesClient timeSeriesClient = new TimeSeriesClient(asClient, asNamespace, asSet,
																 Constants.DEFAULT_MAX_ENTRIES_PER_TIME_SERIES_BLOCK);
		Scanner sc = new Scanner(System.in);	//System.in is a standard input stream
		while(sc.hasNext()) {
			String timeSeriesName = sc.nextLine();
			//assert(scanner.hasNext());
			double value = sc.nextDouble();
			String dummy = sc.nextLine();

			DataPoint dataPoint = new DataPoint(this.timestamp_ritesh,
												value);
			timeSeriesClient.put(timeSeriesName,
								 dataPoint);
		}
		sc.close();
	}
}
