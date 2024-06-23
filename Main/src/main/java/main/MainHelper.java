package main;

import codegen.blocks.ClassInfo;
import com.google.common.collect.Sets;
import core.SeedInfo;
import dtjvms.DTGlobal;
import dtjvms.DTPlatform;
import dtjvms.ProjectInfo;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.ExecutorHelper;
import dtjvms.executor.GC.DiffResult;
import dtjvms.executor.GC.GCExecutor;
import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.tool.GCLogStats;
import optiongen.generator.Generator;
import optiongen.generator.VMType;
import org.apache.commons.cli.*;
import soot.JastAddJ.ForStmt;
import soot.JastAddJ.WhileStmt;
import soot.Printer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.baf.BafASMBackend;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MainHelper {

    public static Options options = null;

    public static void loadOptions() {

        options = new Options();
        options.addOption(new Option("t", "timestamp", true, "time stamp for saving results"));
        options.addOption(new Option("s", "initialSeed", true, "initial random seed"));
        options.addOption(new Option("n", "fuzzing times", true, "set fuzzing times for each run"));
        options.addOption(new Option("p", "project", true, "test project, seed programs"));
        options.addOption(new Option("f", "predefined classes", true, "set predefined class path"));
        options.addOption(new Option("vm", "jvm root path", true, "set jvm root path"));
        options.addOption(new Option("cjdk", "cov-jdk", true, "set cov JDK path"));
        options.addOption(new Option("afl", "afl-showmap", true, "set afl-showmap path"));
        options.addOption(new Option("mode", "gcfuzz-mode", true, "set gcfuzz mode [default/random/edge]"));
        options.addOption(new Option("ujdk", "unique-jdk", true, "set unique JDK version"));
        options.addOption(new Option("gram", "ngram", true, "set event n-gram"));
        options.addOption(new Option("h", "help", false, "print all system settings"));
    }

    public static void restoreBadClasses(List<String> badClasses, ProjectInfo originProject, ProjectInfo targetProject) {

        String originAppOutPath = originProject.getSrcClassPath();
        String targetAppOutPath = targetProject.getSrcClassPath();
        String originTestOutPath = originProject.getTestClassPath();
        String targetTestOutPath = targetProject.getTestClassPath();

        for (String badClass : badClasses) {

            String cpath = badClass.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";

            if (originProject.getApplicationClasses().contains(badClass)) {

                String sourceFilePath = originAppOutPath + DTPlatform.FILE_SEPARATOR + cpath;
                String targetFilePath = targetAppOutPath + DTPlatform.FILE_SEPARATOR + cpath;
                copyToFile(sourceFilePath, targetFilePath);
            }
            if (originProject.getJunitClasses().contains(badClass)) {

                String sourceFilePath = originTestOutPath + DTPlatform.FILE_SEPARATOR + cpath;
                String targetFilePath = targetTestOutPath + DTPlatform.FILE_SEPARATOR + cpath;
                copyToFile(sourceFilePath, targetFilePath);
            }
        }
    }

    public static CommandLine parseArgs(String[] args) {

        if (options == null) {
            loadOptions();
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cmd == null) {

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("JITFuzzing Commands", options);
            System.out.println();
            return null;
        }
        return cmd;
    }

    public static void createFolderIfNotExist(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void saveSootClassToTargetPath(SootClass seedClass, String path) {

        try {
            OutputStream jimpleStreamOut = new FileOutputStream(path.replace(".class", ".jimple"));
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            Printer.v().printTo(seedClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(path);
            BafASMBackend backend = new BafASMBackend(seedClass, soot.options.Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<SeedInfo> initialSeeds(List<String> classes, String srcClassPath) {

        List<SeedInfo> classInfos = new ArrayList<>();
        classes.forEach(clazz -> {
            String cpath = srcClassPath + DTPlatform.FILE_SEPARATOR +
                    clazz.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";
            String tpath = cpath.replace("sootOutput", "02Benchmarks");
            classInfos.add(new SeedInfo(clazz, cpath, clazz, tpath, 0, 0));
        });
        return classInfos;
    }

    public static List<SeedInfo> initialSeedsWithType(List<String> classes, String srcClassPath, boolean isJunit, String bakPath) {

        List<SeedInfo> classInfos = new ArrayList<>();
        classes.forEach(clazz -> {

            String cpath = srcClassPath + DTPlatform.FILE_SEPARATOR +
                    clazz.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";
            if (!bakPath.equals("")) {

                String classFileFolder = bakPath + DTPlatform.FILE_SEPARATOR + clazz;
                MainHelper.createFolderIfNotExist(classFileFolder);
                String originClassBakPath = classFileFolder + DTPlatform.FILE_SEPARATOR + clazz + "-origin.class";
                MainHelper.copyToFile(cpath, originClassBakPath);
                classInfos.add(new SeedInfo(clazz, cpath, clazz, originClassBakPath, isJunit, 0, 0));
            } else {
                classInfos.add(new SeedInfo(clazz, cpath, clazz, cpath, isJunit, 0, 0));
            }
        });
        return classInfos;
    }

    public static void copyToFile(String sourceFilePath, String targetFilePath) {

        try {
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.exists()) {
                File targetFile = new File(targetFilePath);
                targetFile.getParentFile().mkdirs();
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int calCC(SootClass sootClass) {
        List<SootMethod> sootMethods = sootClass.getMethods();
        int complexity = 0;
        for (SootMethod method : sootMethods) {
            complexity += calCC(method);
        }
        return complexity;
    }

    public static int calCC(SootMethod method) {
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        UnitGraph cfg = new ExceptionalUnitGraph(body);

        // Compute the Cyclomatic Complexity for the method
        int complexity = 1; // Start with a complexity of 1 for the single entry point
        for (Unit u : cfg) {
            Stmt s = (Stmt) u;
//                if (s.containsInvokeExpr()) {
//                    complexity++;
//                }
            if (s instanceof IfStmt || s instanceof SwitchStmt || s instanceof WhileStmt || s instanceof ForStmt) {
                complexity++;
            }
//            complexity += body.getTraps().size();
        }
        return complexity;
    }

    public static void logSeedInfo(String tag, double oldPrio, SeedInfo seedInfo) {
        DTGlobal.getClassPriorityLogger().info(String.format("%s%5.2f->%-5.2f scr:%5.2f tm:%2d df:%2d mt:%2d cov:%5.1f C:%s",
                tag, oldPrio, seedInfo.getPriority(), seedInfo.calcScore(), seedInfo.getBrokenTimes(), seedInfo.getDiffTimes(), seedInfo.getMutationTimes(), seedInfo.getCov(), seedInfo.getClassName()));

    }

    public static void optionFeedBack(boolean random,Generator vmOptionGenerator, VMType vmType, DiffResult diffResult, HashMap<String, GCLogStats> seedGCLogStats, int optionIterate) {
        HashMap<String, GCLogStats> gcLogStatsHashMap = GCExecutor.getInstance().getGcLogStatsHashMap();
        DiffCore diffCore = GCExecutor.getInstance().getDiffCore();
        if (diffResult == null) {
            diffResult = new DiffResult(false, false, false, false, 0);
        }
        if (!diffResult.isDiffFound() && GCExecutor.getInstance().isDiffFound()) {
            vmOptionGenerator.feedback(random,vmType, gcLogStatsHashMap, optionIterate, false, true, diffCore, seedGCLogStats);
        } else if (GCExecutor.getInstance().getTimeoutCount() > diffResult.getTimeoutCount()) {
            vmOptionGenerator.feedback(random,vmType, gcLogStatsHashMap, optionIterate, true, false, diffCore, seedGCLogStats);
        } else if (!diffResult.isRerun() && GCExecutor.getInstance().isRerun()) {
            vmOptionGenerator.feedback(random,vmType, gcLogStatsHashMap, optionIterate, true, false, diffCore, seedGCLogStats);
        } else {
//            if (GCExecutor.getInstance().isDisCard()) {
//                vmOptionGenerator.feedback(vmType, gcLogStatsHashMap, optionIterate, false, false, diffCore, noOptionCov);
//            } else {
            vmOptionGenerator.feedback(random,vmType, gcLogStatsHashMap, optionIterate, false, false, diffCore, seedGCLogStats);
//            }
        }
//        if (GCExecutor.getInstance().isPerfDiffFound()) {
//            vmOptionGenerator.feedback(vmType, gcLogStatsHashMap, optionIterate, false, false, diffCore, noOptionCov);
//        }
    }

    public static void classFeedBack(SeedInfo seed) {
        if (GCExecutor.getInstance().isDiffFound()) {
            seed.diffTimesIncrease(GCExecutor.gcExecutor.getDiffCore());
        } else if (GCExecutor.getInstance().getTimeoutCount() > 0 || GCExecutor.getInstance().isRerun()) {
            seed.brokenTimesIncrease();
        }
    }

    public static void logResult(SeedInfo newSeed, SeedInfo seed, ClassInfo clazz, String option, GCType gcType, int globalGCCov, int classIterate, int optionIterate) {
        double newCov = newSeed.getCov(gcType);
        double oldCov = seed.getCov(gcType);
        double seedCov = newSeed.getCov();
        if (GCExecutor.getInstance().isDiffFound()) {
            String diffClassFolder = Status.diffClassPath + DTPlatform.FILE_SEPARATOR + newSeed.getOriginClassName();
            MainHelper.createFolderIfNotExist(diffClassFolder);
            //save to diffClasses
            MainHelper.saveSootClassToTargetPath(clazz.getSootClass(), diffClassFolder + DTPlatform.FILE_SEPARATOR + newSeed.getClassName());
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "DifferenceFound", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);

        } else if (GCExecutor.getInstance().getTimeoutCount() > 0) {
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "TIMEOUT&Consistent", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);
        } else if (GCExecutor.getInstance().isRerun()) {
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "Rerun&Consistent", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);
        } else {
            if (GCExecutor.getInstance().isDisCard()) {
                Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "DisCard", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);
            } else if (GCExecutor.getInstance().isNormalDiff()) {
                Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "NormalDiff", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);
            } else {
                Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "Normal&Consistent", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);
            }
        }
        if (GCExecutor.getInstance().isPerfDiffFound()) {
            String diffClassFolder = Status.diffClassPath + DTPlatform.FILE_SEPARATOR + newSeed.getOriginClassName();
            MainHelper.createFolderIfNotExist(diffClassFolder);
            //save to diffClasses
            MainHelper.saveSootClassToTargetPath(clazz.getSootClass(), diffClassFolder + DTPlatform.FILE_SEPARATOR + newSeed.getClassName());
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "PerformanceDiff", option, gcType, oldCov, newCov, seedCov, globalGCCov, classIterate, optionIterate);
        }
    }
    public static void logResult(SeedInfo newSeed, SeedInfo seed, ClassInfo clazz, String option, GCType gcType, int eventCov,int edgeCov, int classIterate, int optionIterate) {
        double newCov = newSeed.getCov(gcType);
        double oldCov = seed.getCov(gcType);
        double seedCov = newSeed.getCov();
        if (GCExecutor.getInstance().isDiffFound()) {
            String diffClassFolder = Status.diffClassPath + DTPlatform.FILE_SEPARATOR + newSeed.getOriginClassName();
            MainHelper.createFolderIfNotExist(diffClassFolder);
            //save to diffClasses
            MainHelper.saveSootClassToTargetPath(clazz.getSootClass(), diffClassFolder + DTPlatform.FILE_SEPARATOR + newSeed.getClassName());
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "DifferenceFound", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);

        } else if (GCExecutor.getInstance().getTimeoutCount() > 0) {
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "TIMEOUT&Consistent", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);
        } else if (GCExecutor.getInstance().isRerun()) {
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "Rerun&Consistent", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);
        } else {
            if (GCExecutor.getInstance().isDisCard()) {
                Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "DisCard", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);
            } else if (GCExecutor.getInstance().isNormalDiff()) {
                Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "NormalDiff", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);
            } else {
                Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "Normal&Consistent", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);
            }
        }
        if (GCExecutor.getInstance().isPerfDiffFound()) {
            String diffClassFolder = Status.diffClassPath + DTPlatform.FILE_SEPARATOR + newSeed.getOriginClassName();
            MainHelper.createFolderIfNotExist(diffClassFolder);
            //save to diffClasses
            MainHelper.saveSootClassToTargetPath(clazz.getSootClass(), diffClassFolder + DTPlatform.FILE_SEPARATOR + newSeed.getClassName());
            Status.updateStatus(seed.getClassName(), newSeed.getClassName(), "PerformanceDiff", option, gcType, oldCov, newCov, seedCov, eventCov,edgeCov, classIterate, optionIterate);
        }
    }

    public static double gcCovCalculate(HashMap<String, GCLogStats> gcLogStatsHashMap, Set<String> gcCovTotal) {
        double newCov = 0f;
        Set<String> patterns = new HashSet<>();
        for (GCLogStats value : gcLogStatsHashMap.values()) {
            if (value != null && value.getGcEventNGram() != null) {
                patterns.addAll(value.getGcEventNGram());
            }
        }
        if (patterns.isEmpty()) {
            // the number of event < N
            double cov = 0;
            for (GCLogStats value : gcLogStatsHashMap.values()) {
                if (value != null) {
                    cov += value.getGcEventCov();
                }
            }
            newCov = cov / Math.max(gcLogStatsHashMap.size(), 1);
        } else {
            Sets.SetView<String> difference = Sets.difference(patterns, gcCovTotal);
            newCov = difference.size();
        }
        return newCov;
    }
}
