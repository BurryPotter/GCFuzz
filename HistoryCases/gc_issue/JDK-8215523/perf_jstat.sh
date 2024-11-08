#! /bin/sh

#JAVA_HOME=/shared/java/linux/jdk1.8.0_121_x64
#OPTS="-Xms512m -Xmx512m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -verbosegc"
OPTS="-Xms512m -Xmx512m -Xlog:gc=trace:file=gc.log -XX:+UseConcMarkSweepGC"
GC_LOG=gc_jstat.log
JSTAT_LOG=perf_jstat.log



${JAVA_HOME}/bin/java ${OPTS} test > ${GC_LOG} &
java_pid=$!

#echo "" > perf_jstat.log
${JAVA_HOME}/bin/jstat -gc -t ${java_pid} 1 > ${JSTAT_LOG} &
jstat_pid=$!

function finish {
  kill -9 ${java_pid}
  kill -9 ${jstat_pid}
}

trap 'echo "trapped."; finish; exit 1' 1 2 3 15

while true
do
  test $? -gt 128 && break
done
