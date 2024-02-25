#!/bin/bash

export LANG=C


# Check if an argument is provided
if [ -z "$1" ]; then
    # If no argument is provided, get the current date in YYMMDD format
    DATE=$(date +%y%m%d)
else
    # If an argument is provided, use that argument
    DATE=$1
fi

# Execute the command
nohup java -jar ~/local/jars/Utility-sc-2.0.0-fat.jar \
      WebMonitor \
      --url  https://ddbj.nig.ac.jp/public/ddbj_database/README.TXT > "webmonitor.${DATE}.txt" 2>"webmonitor.${DATE}.log" &


