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

/**
 * Time Series Reader class to write out a time series to the command line
 */
@SuppressWarnings("WeakerAccess") // Want to expose class
public class TimeSeriesReader {

    // Specify Aerospike cluster, namespace, set
    private final AerospikeClient asClient;
    private final String asNamespace;
    private final String asSet;
    private String timeSeriesName;
	public Date start;
	public Date end;
	private String timeSeriesName_alias;

    private TimeSeriesReader(AerospikeClient asClient, String asNamespace, String asSet, String timeSeriesName, String start, String end,String timeSeriesName_alias) {
        this.asClient = asClient;
        this.asNamespace = asNamespace;
        this.asSet = asSet;
        this.timeSeriesName = timeSeriesName;

		this.timeSeriesName_alias = timeSeriesName;
		if(null != timeSeriesName_alias && "" != timeSeriesName_alias) {
			this.timeSeriesName_alias = timeSeriesName_alias;
		}
		try {
			this.start = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(start);
			this.end = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(end);
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
            TimeSeriesReader timeSeriesReader = initBenchmarkerFromStringArgs(args);
            timeSeriesReader.run(args);
        } catch (Utilities.ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TimeSeriesReader", OptionsHelper.cmdLineOptionsForReader());
        }
    }

    /**
     * Helper method allowing a TimeSeriesBenchmarker to be initialised from an array of Strings - as per main method
     * Protected visibility to allow testing use
     */
    private static TimeSeriesReader initBenchmarkerFromStringArgs(String[] args) throws Utilities.ParseException {
        TimeSeriesReader timeSeriesReader;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(OptionsHelper.cmdLineOptionsForReader(), args);

            timeSeriesReader = new TimeSeriesReader(
                    new AerospikeClient(
                            OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.HOST_FLAG),
                            3100),
                    OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.NAMESPACE_FLAG),
                    OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.TIME_SERIES_SET_FLAG),
                    OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.TIME_SERIES_NAME_FLAG),
					OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.RITESH_START_TIME),
					OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.RITESH_END_TIME),
					OptionsHelper.getOptionUsingDefaults(cmd, OptionsHelper.BenchmarkerFlags.RITESH_ALIAS)
            );
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TimeSeriesReader", OptionsHelper.cmdLineOptionsForReader());
            throw (new Utilities.ParseException(e.getMessage()));
        }
        return timeSeriesReader;
    }

    private void run(String[] args) {
        //System.out.println("Running TimeSeriesReader\n");
        TimeSeriesClient timeSeriesClient = new TimeSeriesClient(asClient, asNamespace, asSet,
                Constants.DEFAULT_MAX_ENTRIES_PER_TIME_SERIES_BLOCK);

        if (timeSeriesName != null) {
            //System.out.printf("Running time series reader for %s%n", timeSeriesName);
        } else {
            Vector<String> timeSeriesNames = Utilities.getTimeSeriesNames(timeSeriesClient);
            if (timeSeriesNames.size() > 0) {
                timeSeriesName = timeSeriesNames.get(0);
                System.out.printf("No time series specified - selecting series %s%n%n", timeSeriesName);
            } else {
                System.out.printf("No time series data found in %s.%s%n%n", asNamespace, asSet);
            }
        }

		/*
		if (timeSeriesName != null) {
			ClientUtils.printTimeSeries(timeSeriesClient, timeSeriesName);
		}
		*/
		if (timeSeriesName != null) {
			//System.out.println("ritesh"+this.start);
			//System.out.println("ritesh"+this.end);
			int i;
			System.out.println("timestamp " + this.timeSeriesName_alias);
			for(i=0;i<timeSeriesClient.getPoints(timeSeriesName ,this.start, this.end).length	;i++) {
				System.out.println("" + timeSeriesClient.getPoints(timeSeriesName ,this.start, this.end)[i].getTimestamp() + " " +
								   timeSeriesClient.getPoints(timeSeriesName ,this.start, this.end)[i].getValue());
			}
		}
    }

}
