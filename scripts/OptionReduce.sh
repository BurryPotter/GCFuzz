#!/usr/bin/env bash

if [ -n "$1" ] ;then
    export mainClass="$1"
    shift
else
    export mainClass="DTPreview"
fi

while getopts "n:f:p:v:t:b:r:u:" opt ; do
    case $opt in
        n)
            export fuzzingRound="$OPTARG"
            ;;
        f)
            export predefinedClassPath="$OPTARG"
            ;;
        p)
            export projectName="$OPTARG"
            ;;
        v)
            export JVMPATH="$OPTARG"
            ;;
        t)
            export TIMESTAMP="$OPTARG"
            ;;
        b)
            export ROOT="$OPTARG"
            ;;
        r)
            export RESULTSPATH="$OPTARG"
            ;;
	u)
            export UNIQUE_VERSION="$OPTARG"
            ;;
        \?)
          echo "Invalid option: -$OPTARG"
          exit 1
          ;;
        :)
          echo "Option -$OPTARG requires an argument."
          exit 1
          ;;
    esac
done


export ProjectPath="."
export ClassPath="${ProjectPath}/GCFuzzing/OptionReduce.jar"
export LibPath="${ProjectPath}/lib/*"
export JavaLib="${JAVA_HOME}/lib/*:${JAVA_HOME}/jre/lib/*"
export timeout=$[3600*5]

if [ "${mainClass}" == "reduce.OptionReduce" ]; then
    export timeStamp=$(date +%s)
    export ResultRootPath="03results/$timeStamp"
    mkdir -p "$ResultRootPath"
fi

timeout ${timeout} ./scripts/Executor.sh
#echo "Timeout Reached..."
#for mac test
#./scripts/Executor.sh
