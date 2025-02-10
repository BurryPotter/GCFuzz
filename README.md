# GCFuzz

This is the implementation repository for out paper submission: **Exploring JVM Garbage Collector Testing with Event-Coverage**

## Directory Description

```
├── 01JVMS         :  Test jvms
├── 02Benchmarks   :  Origin benchmarks
├── 03results      :  Difference results
├── DTJVM          :  Execution Phase Module
├── GCMain         :  Generation Phase Module
├── Main           :  Control Module
├── analyzer       :  GC log parser
├── scripts        :  Execute scripts
├── vmoptions      :  Collected GC options
└── sootOutput     :  Benchmarks for generation
```

## Reported Bugs

| Bug ID      | Link                                                  |
| ----------- | ----------------------------------------------------- |
| JDK-8303961 | https://bugs.openjdk.org/browse/JDK-8303961           |
| JDK-8307243 | https://bugs.openjdk.org/browse/JDK-8307243           |
| JDK-8308169 | https://bugs.openjdk.org/browse/JDK-8308169           |
| JDK-8305890 | https://bugs.openjdk.org/browse/JDK-8305890           |
| JDK-8308157 | https://bugs.openjdk.org/browse/JDK-8308157           |
| JDK-8317009 | https://bugs.openjdk.org/browse/JDK-8317009           |
| JDK-8322702 | https://bugs.openjdk.org/browse/JDK-8322702           |
| JDK-8303059 | https://bugs.openjdk.org/browse/JDK-8303059           |
| JDK-8309081 | https://bugs.openjdk.org/browse/JDK-8309081           |
| JDK-8305832 | https://bugs.openjdk.org/browse/JDK-8305832           |
| #17458      | https://github.com/eclipse-openj9/openj9/issues/17458 |
| #16829      | https://github.com/eclipse-openj9/openj9/issues/16829 |
| #17016      | https://github.com/eclipse-openj9/openj9/issues/17016 |
| #17389      | https://github.com/eclipse-openj9/openj9/issues/17389 |
| #17534      | https://github.com/eclipse-openj9/openj9/issues/17534 |
| #18156      | https://github.com/eclipse-openj9/openj9/issues/18156 |
| #18644      | https://github.com/eclipse-openj9/openj9/issues/18644 |
| #17793      | https://github.com/eclipse-openj9/openj9/issues/17793 |
| #17445      | https://github.com/eclipse-openj9/openj9/issues/17445 |
| #20687      | https://github.com/eclipse-openj9/openj9/issues/20687 |
| #20477      | https://github.com/eclipse-openj9/openj9/issues/20477 |

## Start

### step1. Environment Setup

GCFuzz tests the debug build version of the JVMs, so users need to download the source code and enable the debug option during compilation.

Here we take the OpenJDK11 as an example.

```bash
# git clone https://github.com/openjdk/jdk11u.git
# cd jdk11u
# bash configure --enable-debug --disable-warnings-as-errors --with-target-bits=64
# make images
```

Since ZGC is an experimental version in JDK11, if you want to test ZGC in JDK11, you need to add the parameter `--with-jvm-features=zgc` to enable it.

```bash
# git clone https://github.com/openjdk/jdk11u.git
# cd jdk11u
# bash configure --enable-debug --disable-warnings-as-errors --with-target-bits=64 --with-jvm-features=zgc
# make images
```

If you want to collect the JVM coverage information, you need to add the parameter `--enable-native-coverage ` .

```bash
# git clone https://github.com/openjdk/jdk11u.git
# cd jdk11u
# bash configure --enable-debug --disable-warnings-as-errors --with-target-bits=64 --with-jvm-features=zgc --enable-native-coverage
# make images
```

If you want to collect the Edge coverage information, you need to build AFLplusplus, and then compiler JDK with it.

```bash
# CC=PATH/TO/AFLPLUSPLUS/afl-gcc-fast
# CXX=PATH/TO/AFLPLUSPLUS/afl-g++-fast 
# bash configure --enable-debug --disable-warnings-as-errors
# AFL_GCC_ALLOWLIST=PATH/TO/AFL_GCC_ALLOWLIST 
# make images

# Please use the full path of AFL_GCC_ALLOWLIST.
```

### Step2. Build and Run GCFuzz

* You can import it into IntelliJ IDEA workspace

* Extract the Benchmark archive to the 02Benchmark directory.

  ```bash
  # cd GCFuzz
  # tar -zxvf VECT_Benchmarks.tar.gz -C .
  ```

* Build and run `GCFuzz` with ` JDK8 / JDK11`. (Execute the `main.GCMain.java`)

  ```
  # you can set the arguments of GCFuzz
  -t			time stamp for saving results
  -s 			initial random seed
  -n 			generation times for each run
  -p  		test project, seed programs
  -f			set predefined class path
  -vm			set jvm root path
  -cjdk		set cov JDK path
  -afl		set afl-showmap path
  -mode		set gcfuzz mode [default/random/edge]
  -ujdk		set unique JDK version to test
  -gram		set event n-gram hyperparameters N
  ```

  ```bash
  # After setup the environment and build the GCFuzz, you can alse run it using scripts.
  # ./gcfuzz_run.sh
  ```

* Run the reduction program to reduce the inconsistencies. (Execute the `reduce.OptionReduce.java`)

  ```bash
  # After setup the environment and build the GCFuzz, you can alse run it using scripts.
  # ./option_reduce_run.sh
  ```

* Run the result statistics program to calculate the number of inconsistencies after deduplication. (Execute the `reduce.TotalBug.java`)

  This program depends on the difference information form `03results` and the core dump files form `sootOutput`. And the output is like this:

  ```
  ==========crash==========
  crash size = 1
  ===Exceptions===
  total Unique size= 7
  ```

  
