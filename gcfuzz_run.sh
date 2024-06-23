
benchmarks=("eclipse-dacapo")
count=0
ngram=5
ujdk=21
seed=4
for benchmark in "${benchmarks[@]}"; do
#       covjava="/home/zhengkai/covjdk/${covjdk[$count]}/build/linux-x86_64-normal-server-release/images/jdk/bin/java"
        ((count++))
        echo "./scripts/GCFuzzing.sh main.GCMain -m default -u $ujdk -p $benchmark -n 3 -g $ngram -s $seed"
        ./scripts/GCFuzzing.sh main.GCMain -m default -u $ujdk -p $benchmark -n 3 -g $ngram -s $seed
        sleep 2
        echo "./scripts/GCFuzzing.sh main.GCMain -m random -u $ujdk -p $benchmark-2 -n 3 -g $ngram -s $seed"
        ./scripts/GCFuzzing.sh main.GCMain -m random -u $ujdk -p $benchmark-2 -n 3 -g $ngram -s $seed
        sleep 2
        echo "./scripts/GCFuzzing.sh main.GCMain -m edge -u $ujdk -p $benchmark-3  -n 3 -g $ngram -s $seed -a /home/zhengkai/shareDir/AFLplusplus/afl-showmap"
        ./scripts/GCFuzzing.sh main.GCMain -m edge -u $ujdk -p $benchmark-3  -n 3 -g $ngram -s $seed -a /home/zhengkai/shareDir/AFLplusplus/afl-showmap
        sleep 2
done
