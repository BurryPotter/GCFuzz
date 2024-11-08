for i in 1 2 3 4 5 6 7 8 9 10; do
    (while test ! -f stop; do /home/iklam/jdk/bld/neo-fastdebug/images/jdk/bin/java -cp ~/tmp StressMe; done) &
done

trap 'kill $(jobs -p)' EXIT

wait
