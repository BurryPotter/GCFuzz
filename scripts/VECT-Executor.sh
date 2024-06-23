#!/usr/bin/env bash

ClassPaths="${ClassPath}:${LibPath}:${JavaLib}"
ClassPaths=${ClassPaths// }

logFile="${ResultRootPath}/output"
mainPidFile="/home/zhengkai/GC/GCFuzz/main.pid"

options=""
if [ -n "${projectName}" ]; then
  options="${options} -p ${projectName} -s ${projectName}"
fi
if [ -n "${predefinedClassPath}" ]; then
  options="${options} -f ${predefinedClassPath}"
fi
if [ -n "${fuzzingRound}" ]; then
  options="${options} -n ${fuzzingRound}"
fi
if [ -n "${JVMPATH}" ]; then
  options="${options} -vm ${JVMPATH}"
fi
if [ -n "${TIMESTAMP}" ]; then
  timeStamp=${TIMESTAMP}
fi
if [ -n "${SEED}" ]; then
  options="${options} -r ${SEED}"
fi
echo "Executing command: java -Xms1024m -Xmx20480m  -ea -cp ${ClassPaths} ${mainClass} -t ${timeStamp} ${options} 2>&1 | tee ${logFile}.out ..."
nohup java -Xms1024m -Xmx20480m  -ea -cp ${ClassPaths} ${mainClass} -t ${timeStamp} ${options} 2>&1 > ${logFile}.out & (echo $! >> ${mainPidFile})

