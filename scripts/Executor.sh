#!/usr/bin/env bash

ClassPaths="${ClassPath}:${LibPath}:${JavaLib}"
ClassPaths=${ClassPaths// }

logFile="${ResultRootPath}/output"
mainPidFile="/home/zhengkai/GC/GCFuzz/main.pid"

options=""
if [ -n "${projectName}" ]; then
  options="${options} -p ${projectName}"
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
if [ -n "${ROOT}" ]; then
  options="${options} -b ${ROOT}"
fi
if [ -n "${RESULTSPATH}" ]; then
  options="${options} -r ${RESULTSPATH}"
fi
if [ -n "${SEED}" ]; then
  options="${options} -s ${SEED}"
fi
if [ -n "${VECTRANDOM}" ]; then
  options="${options} -r ${VECTRANDOM}"
fi
if [ -n "${SEEDPROJECT}" ]; then
  options="${options} -seed ${SEEDPROJECT}"
fi
if [ -n "${UNIQUE_VERSION}" ]; then
  options="${options} -ujdk ${UNIQUE_VERSION}"
fi
if [ -n "${MODE}" ]; then
  options="${options} -mode ${MODE}"
fi
if [ -n "${AFL}" ]; then
  options="${options} -afl ${AFL}"
fi
if [ -n "${COVJDK}" ]; then
  options="${options} -cjdk ${COVJDK}"
fi
if [ -n "${NGRAM}" ]; then
  options="${options} -gram ${NGRAM}"
fi
echo "Executing command: java -Xms1024m -Xmx10240m  -ea -cp ${ClassPaths} ${mainClass} -t ${timeStamp} ${options} 2>&1 | tee ${logFile}.out ..."
nohup java -Xms1024m -Xmx10240m  -ea -cp ${ClassPaths} ${mainClass} -t ${timeStamp} ${options} 2>&1 > ${logFile}.out & (echo $! >> ${mainPidFile})

