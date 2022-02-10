package io.github.aerospike_examples.aero_time_series.client;

import java.util.Date;

public class DataPoint {
    private final long timestamp;
    private final double value;

    public DataPoint(long timestamp,double value){
        this.timestamp = timestamp;
        this.value = value;
    }

    public DataPoint(Date date, double value){
        this(date.getTime(),value);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString(){
        return String.format("(%s,%f)",timestamp,value);
    }

    public boolean equals(DataPoint d){
        return timestamp == d.timestamp && value == d.value;
    }
}
