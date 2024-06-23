rootPath='.'
projectName='Openj9Test-Test'

benchmarks=("avrora-cvs-20091224" "avrora-cvs-20091224-2" "avrora-cvs-20091224-3")  # "HotspotTests-Java"  "Openj9Test-Test" )  # "jython-dacapo" "fop-dacapo"   "sunflow-0.07.2") # "HotspotTests-Java-2"  "Openj9Test-Test")
results=("1709700142"  "1709700144"  "1709700146" )
count=0
ujdk=11
for benchmark in "${benchmarks[@]}"; do
#       covjava="/home/zhengkai/covjdk/${covjdk[$count]}/build/linux-x86_64-normal-server-release/images/jdk/bin/java"
        echo "./scripts/OptionReduce.sh reduce.OptionReduce -u $ujdk -p $benchmark -t ${results[$count]} -b $rootPath -r $rootPath/03results"
        ./scripts/OptionReduce.sh reduce.OptionReduce -u $ujdk -p $benchmark -t ${results[$count]} -b $rootPath -r $rootPath/03results
	((count++))
        sleep 2
done

