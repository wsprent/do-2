#!/usr/bin/env bash

# flag to choose which strategy to use: cplex, rounding, rounding-rand, primal-dual
method=$1

# instance problem data set
instance=$2

# number of times to repeat this
repeat=$3

rm -f $1.csv

if [ ! -f ./$1.csv ]; then
    echo 'instance,lpcost,appcost,time(ms)' > ./$1.csv
fi

echo -e "\n=== Running tests marked as method: $1, data set $2, $3 times: ===\n"
for ((i=1; i <= $3; i++)); do
    # run the program with the given method
    (make run-$1 ILPDATA=$2 2>/dev/null | \
    # last 3 lines: relaxation cost, approximation cost, time
    # in the ILP case: dummy value, ILP cost, time
    tail -n -3 | \
    # find the numeric values in each line + the method name
    grep -o -P '([0-9]+(\.[0-9]+)?)'; echo $2) | \
    # print it to file in a formatted way
    xargs | awk '{OFS=","}{print $4, $1, $2, $3}' >> $1.csv
    echo -ne "$i/$3\r"
done;

# output the result to stdout
printf "              Relaxed cost: "; (cat $1.csv | tail -n -1 | awk -F "," '{print $2}')
printf "Average approximation cost: "; (cat $1.csv | tail -n +2 | awk -F "," '{ sum += $3; n++ } END { if (n > 0) print sum / n; }')
printf "         Average time (ms): "; (cat $1.csv | tail -n +2 | awk -F "," '{ sum += $4; n++ } END { if (n > 0) print sum / n; }')
