#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Usage: dumpCoverage.sh <outdir>"
    exit
fi

#for i in `seq 1 12`;
i=100
while true
do
  i=$((i+1))
  sleep 300 #sleep for 5 minutes
  echo $1/$i
  adb -s $2 shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE
  adb -s $2 pull /mnt/sdcard/coverage.ec $1/coverage$i.ec
done
