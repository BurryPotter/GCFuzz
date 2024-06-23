package core;

import config.GCConfiguration;
import dtjvms.DTPlatform;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.HotspotGCType;
import dtjvms.executor.GC.tool.GCLogStats;
import main.MainHelper;
import soot.Printer;
import soot.SootClass;
import soot.baf.BafASMBackend;
import soot.options.Options;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SeedInfo {

    private String originClassName;
    private String originClassPath;
    private String className;
    private String pathToClass;

    private boolean isJunit;

    private int mutationOrder;
    private int mutationTimes;

    private int brokenTimes = 0;
    private int diffTimes = 0;

    private double score = 0f;
    private double priority = 0f;
    private HashMap<GCType, Double> gcEventCov = new HashMap<>();

    private int lastSelect = 0;

    private HashSet<DiffCore> diffCores = new HashSet<>();

    public SeedInfo() {
    }

    public int getBrokenTimes() {
        return brokenTimes;
    }

    public int getDiffTimes() {
        return diffTimes;
    }

    public double calcScore() {
//        return score + (diffTimes - brokenTimes) * 1.0 / (mutationTimes + 1);

//        return score + diffTimes * GCConfiguration.REWARD_WEIGHT - brokenTimes * GCConfiguration.PENALTY_WEIGHT;

        return 0;
    }

    public double getPriority() {
        return priority;
    }

    public double calcPriority() {
//        priority = Math.exp(calcScore());

////        priority = Math.exp(calslcScore() - Math.log(mutationTimes + 1) * GCConfiguration.DECAY_WEIGHT );
//        double sum = -mutationTimes * GCConfiguration.DECAY_WEIGHT + getCov() * GCConfiguration.GC_COV_DECAY - brokenTimes * GCConfiguration.PENALTY_WEIGHT;
//        double exp = Math.exp(sum);
//        if (Double.isInfinite(exp)) {
//            exp = sum;
//        }
//        priority = Math.log(1 + exp);
////        priority = 1 / (1 + Math.exp(-(calcScore() - Math.log(mutationTimes + 1) * GCConfiguration.DECAY_WEIGHT + Math.log(getCov() + 1) * GCConfiguration.GC_COV_DECAY)));
//        return priority;
        priority = (getCov() + 1) / (getMutationTimes() + 1);
        return priority;
    }

    public void setLastSelect(int lastSelect) {
        this.lastSelect = lastSelect;
    }

    public void setGcEventCov(HashMap<GCType, Double> gcEventCov) {
        this.gcEventCov = gcEventCov;
    }

    public void updateGCEventCov(GCType gcType, HashMap<String, GCLogStats> gcLogStatsHashMap, Set<String> gcCovTotal) {
        if (!(gcType instanceof HotspotGCType)) {
            // cannot coverage not-hotspot gc.
            return;
        }
        double newCov = MainHelper.gcCovCalculate(gcLogStatsHashMap, gcCovTotal);
        this.gcEventCov.put(gcType, newCov);
    }

    public double getCov(GCType gcType) {
        return gcEventCov.getOrDefault(gcType, 0d);
//        return gcEventCov.getOrDefault(gcType,0d);

    }

    public double getCov() {
        double res = 0;
        for (Map.Entry<GCType, Double> gcTypeDoubleEntry : gcEventCov.entrySet()) {
            res += gcTypeDoubleEntry.getValue();
        }
        return res;
//        return gcEventCov.getOrDefault(gcType,0d);

    }

    public SeedInfo(String originClassName, String className, int mutationOrder, int mutationTimes) {
        this.originClassName = originClassName;
        this.className = className;
        this.isJunit = false;
        this.mutationOrder = mutationOrder;
        this.mutationTimes = mutationTimes;
    }

    public SeedInfo(String originClassName, String className, String classFileFolder, int mutationOrder, int mutationTimes) {
        this.originClassName = originClassName;
        this.className = className;
        if (classFileFolder.endsWith(".class")) {
            this.pathToClass = classFileFolder;
        } else {
            this.pathToClass = classFileFolder + DTPlatform.FILE_SEPARATOR + className;
        }
        this.isJunit = false;
        this.mutationOrder = mutationOrder;
        this.mutationTimes = mutationTimes;
    }

    public SeedInfo(String originClassName, String originClassPath, String className, String classFileFolder, int mutationOrder, int mutationTimes) {
        this(originClassName, originClassPath, className, classFileFolder, false, mutationOrder, mutationTimes);
    }

    public SeedInfo(String originClassName, String originClassPath, String className, String classFileFolder, Boolean isJunit, int mutationOrder, int mutationTimes) {
        this(originClassName, originClassPath, className, classFileFolder, false, mutationOrder, mutationTimes, 0, 0, new HashMap<>());
    }

    public SeedInfo(String originClassName, String originClassPath, String className, String classFileFolder, Boolean isJunit, int mutationOrder, int mutationTimes, int timeOutTimes, double score, HashMap<GCType, Double> gcEventCov) {
        this.originClassName = originClassName;
        this.originClassPath = originClassPath;
        this.className = className;
        if (classFileFolder.endsWith(".class")) {
            this.pathToClass = classFileFolder;
        } else {
            this.pathToClass = classFileFolder + DTPlatform.FILE_SEPARATOR + className;
        }
        this.isJunit = isJunit;
        this.mutationOrder = mutationOrder;
        this.mutationTimes = mutationTimes;
        this.brokenTimes = timeOutTimes;
        this.score = score;
        this.gcEventCov = new HashMap<>(gcEventCov);
        calcPriority();
    }

    public boolean hasCovered() {
        return mutationTimes != 0;
    }

    public boolean isOriginClass() {
        return originClassName.equals(className);
    }

    public void storeToCoverOriginClass() {

        try {
            File sourceFile = new File(pathToClass);
            File targetFile = new File(originClassPath);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOriginClassPath() {
        return originClassPath;
    }

    public void setOriginClassPath(String originClassPath) {
        this.originClassPath = originClassPath;
    }

    public String getOriginClassName() {
        return originClassName;
    }

    public String generateMutateClassFilename() {

        return originClassName + "_" +
                String.format("%02d", mutationOrder + 1) +
                String.format("%02d", mutationTimes) +
                "@" +
                System.currentTimeMillis() +
                ".class";
    }

    public void saveSootClassToFile(SootClass seedClass) {

        try {
            OutputStream jimpleStreamOut = new FileOutputStream(pathToClass.replace(".class", ".jimple"));
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            Printer.v().printTo(seedClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(pathToClass);
            BafASMBackend backend = new BafASMBackend(seedClass, soot.options.Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveSootClassToTargetPath(SootClass seedClass, String path) {

        try {
            OutputStream jimpleStreamOut = new FileOutputStream(path.replace(".class", ".jimple"));
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            Printer.v().printTo(seedClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(path);
            BafASMBackend backend = new BafASMBackend(seedClass, Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mutationTimesIncrease() {
        this.mutationTimes++;
    }

    public void brokenTimesIncrease() {
        this.brokenTimes++;
    }

    public void diffTimesIncrease(DiffCore diffCore) {
        if (!diffCores.contains(diffCore)) {
            diffCores.add(diffCore);
            this.diffTimes++;
        }

    }

    public boolean isJunit() {
        return isJunit;
    }

    public void setJunit(boolean junit) {
        isJunit = junit;
    }

    public void setOriginClassName(String originClassName) {
        this.originClassName = originClassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPathToClass() {
        return pathToClass;
    }

    public void setPathToClass(String pathToClass) {
        this.pathToClass = pathToClass;
    }

    public int getMutationOrder() {
        return mutationOrder;
    }

    public void setMutationOrder(int mutationOrder) {
        this.mutationOrder = mutationOrder;
    }

    public int getMutationTimes() {
        return mutationTimes;
    }

    public void setMutationTimes(int mutationTimes) {
        this.mutationTimes = mutationTimes;
    }

    public HashMap<GCType, Double> getGcEventCov() {
        return gcEventCov;
    }

    @Override
    public String toString() {
        return "SeedInfo{" +
                "originClassName='" + originClassName + '\'' +
                ", originClassPath='" + originClassPath + '\'' +
                ", className='" + className + '\'' +
                ", pathToClass='" + pathToClass + '\'' +
                ", isJunit=" + isJunit +
                ", mutationOrder=" + mutationOrder +
                ", mutationTimes=" + mutationTimes +
                '}';
    }
}
