#!/bin/bash

java_pids_times=$(ps -C java -o pid,etimes --no-headers)
main_pids="$(cat /home/zhengkai/GC/GCFuzz/main.pid)"

while read -r java_pid java_time; do

	if ! [[ "$main_pids" =~ $java_pid  ]]  && [ $java_time -ge 30 ]; then
		kill -9 $java_pid
		echo "kill java process $java_pid with time $java_time"
	fi
done <<< "$java_pids_times"

