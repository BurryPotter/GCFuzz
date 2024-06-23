package main;

import config.FuzzingConfig;
import config.GCConfiguration;
import dtjvms.DTConfiguration;
import dtjvms.DTGlobal;
import dtjvms.DTPlatform;
import dtjvms.executor.GC.GCType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import java.io.*;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Status {

    public static int initialSeed = 1;
    public static long currentSeed = 0;
    public static int fuzzingRound;
    public static boolean useVMOptions = true;
    public static String timeStamp;
    public static String projectName = "Templates";
    public static String defineClassesPath = "testcases.txt";
    // 是否在benchmark中人为选择一些项目
    public static boolean usePredefinedClass = true;
    public static String jvmRootPath = "." + DTPlatform.FILE_SEPARATOR + "01JVMS";
    public static String projRootPath = "." + DTPlatform.FILE_SEPARATOR + "02Benchmarks";
    public static String mutationHistoryPath;
    public static String diffClassPath;
    public static String covJDK = "/home/zhengkai/jdk/jdk11u/bin/java";
    public static String methodMode = "default";

    public static void globalStatus() {

        if (DTGlobal.getDataLogger() == null) {
            return;
        }
        String title = "GCFuzz (2.0). " + new Date() + "(" + System.currentTimeMillis() + ")";
        DTGlobal.getDataLogger().info(String.join("", Collections.nCopies(20, "#")) +
                title + String.join("", Collections.nCopies(20, "#")));
        DTGlobal.getDataLogger().info("Test Project: " + projectName);
        DTGlobal.getDataLogger().info("Random Seed: " + initialSeed);
        DTGlobal.getDataLogger().info("Fuzzing Round: " + fuzzingRound);
        DTGlobal.getDataLogger().info("Max Running Time: " + DTConfiguration.CLASS_MAX_RUNTIME);
        DTGlobal.getDataLogger().info("Generation History: " + mutationHistoryPath);
        DTGlobal.getDataLogger().info("reward weight: " + GCConfiguration.REWARD_WEIGHT);
        DTGlobal.getDataLogger().info("penalty weight: " + GCConfiguration.PENALTY_WEIGHT);
        DTGlobal.getDataLogger().info("decay weight: " + GCConfiguration.DECAY_WEIGHT);
        DTGlobal.getDataLogger().info("gc cov weight: " + GCConfiguration.GC_COV_DECAY);
        DTGlobal.getDataLogger().info("Event N-Gram: " + DTConfiguration.GC_EVENT_NGRAM);

        DTGlobal.getDataLogger().info(String.join("", Collections.nCopies(40 + title.length(), "#")));
    }

    public static void updateStatus(String superClass, String className, String status) {
        DTGlobal.getDataLogger().info(new Date() + "(" + currentSeed + ")" + " "
                + String.format("%-18s", status) + " " + superClass + " " + className);
    }

    public static void updateStatus(String superClass, String className, String status, String vmoption) {
        DTGlobal.getDataLogger().info(new Date() + "(" + currentSeed + ")" + " "
                + String.format("%-18s", status) + " " + superClass + " " + className + " vmoption: " + vmoption);
    }

//    public static void updateStatus(String superClass, String className, String status, String vmoption, int oldCC, int newCC) {
//        DTGlobal.getDataLogger().info(new Date() + "(" + currentSeed + ")" + " CC:" + String.format("% 5d", oldCC) + "->" + String.format("%-5d", newCC) + " "
//                + String.format("%-18s", status) + " " + superClass + " " + className + " vmoption: " + vmoption);
//    }

    public static void updateStatus(String superClass, String className, String status, String vmoption, GCType gcType, double gcOldCov, double gcNewCov, double newSeedCov, int globalCov, int classIterate, int optionIterate) {
        DTGlobal.getDataLogger().info(new Date() + "(" + currentSeed + ")" + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate)
                + String.format(" <%s event: %5.2f->%5.2f > ", gcType, gcOldCov, gcNewCov)
                + String.format(" SeedGCCov: %5.2f TotalGCCov: %5d ", newSeedCov, globalCov)
                + String.format("%-18s", status) + " " + superClass + " " + className + " vmoption: " + vmoption);
    }

    public static void updateStatus(String superClass, String className, String status, String vmoption, GCType gcType, double gcOldCov, double gcNewCov, double newSeedCov, int eventCov, int edgeCov, int classIterate, int optionIterate) {
        DTGlobal.getRecordLogger().info(System.currentTimeMillis() + "(" + currentSeed + ")" + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate)
                + String.format(" <%s event: %5.2f->%5.2f > ", gcType, gcOldCov, gcNewCov)
                + String.format(" SeedGCCov: %5.2f EventCov: %5d EdgeCov: %5d ", newSeedCov, eventCov, edgeCov)
                + String.format("%-18s", status) + " " + superClass + " " + className + " vmoption: " + vmoption);
    }

    public static void updateStatus(String superClass, String className, String status, String vmoption, int classIterate, int optionIterate) {
        DTGlobal.getDataLogger().info(new Date() + "(" + currentSeed + ")" + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate)
                + String.format("%-18s", status) + " " + superClass + " " + className + " vmoption: " + vmoption);
    }

    public static ReplayInfo parseStatus(String logFile, int line) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        String lineStr = reader.lines().collect(Collectors.toList()).get(line - 1);
        return parseStatus(lineStr);
    }

    public static ReplayInfo parseStatus(String lineStr) {
        String tag = lineStr.substring(lineStr.indexOf("<CR"), lineStr.indexOf(">") + 1);
        String afterL = lineStr.split("\\(")[1];
        long seed = Long.parseLong(afterL.split("\\)")[0]);
        String afterR = afterL.split("\\)")[1];
        String vmoption = afterR.split("vmoption:")[1];
        String afterRBeforeOption = afterR.split("vmoption:")[0];
        Matcher gcTypeMatcher = Pattern.compile("<(\\w+) event").matcher(afterRBeforeOption);
        String gcType = null;
        if (gcTypeMatcher.find()) {
            gcType = gcTypeMatcher.group(1);
        }
        Matcher mathcer = Pattern.compile("(\\w|@|\\$|\\.)+\\.class").matcher(afterRBeforeOption);
        String testClass = null;
        while (mathcer.find()) {
            testClass = mathcer.group();
        }
        String originClass = testClass.substring(0, testClass.lastIndexOf("_"));
        return new ReplayInfo(seed, testClass, vmoption, originClass, tag, gcType);
    }

    public static void argsParser(String[] args) {

        CommandLine options = MainHelper.parseArgs(args);
        HelpFormatter formatter = new HelpFormatter();
        if (options != null) {

            if (options.hasOption("n")) {
                Status.fuzzingRound = Integer.parseInt(options.getOptionValue("n"));
            } else {
                Status.fuzzingRound = FuzzingConfig.MAX_FUZZ_STEP;
            }
            if (options.hasOption("t")) {
                Status.timeStamp = options.getOptionValue("t");
            } else {
                Status.timeStamp = String.valueOf(new Date().getTime());
            }
            if (options.hasOption("s")) {
                Status.initialSeed = Integer.parseInt(options.getOptionValue("s"));
            }
            if (options.hasOption("p")) {
                Status.projectName = options.getOptionValue("p");
            }
            if (options.hasOption("f")) {
                Status.defineClassesPath = options.getOptionValue("f");
                Status.usePredefinedClass = true;
            }
            if (options.hasOption("vm")) {
                Status.jvmRootPath = options.getOptionValue("vm");
            }
            if (options.hasOption("h")) {
                formatter.printHelp("JITFuzzing Commands", MainHelper.options);
                System.out.println();
            }
            if (options.hasOption("cjdk")) {
                Status.covJDK = options.getOptionValue("cjdk");
            } else {
                Status.covJDK = null;
            }
            if (options.hasOption("ujdk")) {
                DTConfiguration.FIND_UNIQUE_BUG = true;
                DTConfiguration.UNIQUE_JDK_VERSION = Integer.parseInt(options.getOptionValue("ujdk"));
            }else {
                DTConfiguration.FIND_UNIQUE_BUG = false;
            }
            if (options.hasOption("gram")) {
                DTConfiguration.GC_EVENT_NGRAM = Integer.parseInt(options.getOptionValue("gram"));
            }
            if (options.hasOption("afl")) {
                DTConfiguration.AFL_PATH = options.getOptionValue("afl");
                DTConfiguration.EDGE_COV = true;
            }
            if (options.hasOption("mode")) {
                methodMode = options.getOptionValue("mode");
            }
        }
    }
}
