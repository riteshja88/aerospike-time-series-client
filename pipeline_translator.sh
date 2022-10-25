#!/bin/bash
USAGE="e.g Usage1: $0 10.100.159.18:80 today\ne.g Usage2: $0 10.100.159.18:80 yesterday"
if (( $# != 2 )); then
	echo -e $USAGE
    exit 1
fi

STAT_SERVER=$1 # e.g 10.100.159.18:80
WHICH_DAY=$2 # e.g today or yesterday(for-day-change)

if [[ "today" != "$WHICH_DAY" && "yesterday" != "$WHICH_DAY" ]]; then
	echo -e $USAGE
    exit 2
fi

function submit_stats {
	join -a2 a_$1 b_$1|awk '{if(3==NF) {if($2 != $3) {print $1,$3-$2}} else {print $0}}'| tr ':' ' '|awk '{value=$(NF);dc=$(NF-1);server=$(NF-2);pubid=$(NF-3);stat="";for(i=1;i<=NF-4;i++) {stat=stat""$i":"};if((pubid+0) == pubid){print stat""dc,value;print stat""server":"dc,value;print stat""pubid":"dc,value;print stat""pubid":"server":"dc,value} else {stat=stat""pubid;print stat":"dc,value;print stat":"server":"dc,value}}'|awk '{stats[$1]+=$2} END {for (key in stats) {print key,stats[key]}}'|tr ' ' '\n'|java -cp ./benchmarker-*-SNAPSHOT-jar-with-dependencies.jar io.github.aerospike_examples.timeseries.benchmarker.TimeSeriesWriter -h aerospike.riteshja88.freemyip.com -n test  -w "`TZ='America/Los_Angeles' date  +"%Y-%m-%d %H:%M:%S"`" -s TimeSeriesTemp
}

function create_empty_a_file_if_not_present {
	if [ ! -f a_$1 ]
	then
		#echo "File does not exist in Bash"
		echo "" > a_$1
	fi
}

create_empty_a_file_if_not_present $STAT_SERVER
curl "http://$STAT_SERVER/stat?date=`TZ='America/Los_Angeles' date --date="$WHICH_DAY" +"%Y-%m-%d"`"|jq .|grep "\"TR:.*"|tr -d ',"'|grep -o TR.*|awk '{print substr($1, 1, length($1)-1),$2}'|sort -k1 > b_$STAT_SERVER
CURL_STATUS=${PIPESTATUS[0]} JQ_STATUS=${PIPESTATUS[1]} #Note: all the pipestatus's should be collected in ONE LINE
if (( $CURL_STATUS != 0 )); then
	echo -e "Error: Could NOT get a RESPONSE from $STAT_SERVER using curl."
    exit 3
fi
if (( $JQ_STATUS != 0 )); then
	echo -e "Error: Received a response with INVALID JSON from $STAT_SERVER."
    exit 4
fi
submit_stats $STAT_SERVER

if [ "yesterday" == "$WHICH_DAY" ]; then
	rm -f a_$STAT_SERVER b_$STAT_SERVER
else #today
	mv b_$STAT_SERVER a_$STAT_SERVER
fi
#echo "done"
