package optiongen.bean;

import com.google.common.collect.Sets;
import config.GCConfiguration;
import dtjvms.DTConfiguration;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.tool.CrashAnalyzer;
import dtjvms.executor.GC.tool.GCLogStats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class StatisticData {
    private int lastSelect = 1;
    private int inconsistentTime = 0;
    private int brokenTime = 0;
    private int selectTime = 0;
    private HashSet<DiffCore> diffCores = new HashSet<>();
    private HashSet<CrashAnalyzer.DedupInfo> crashDedup = new HashSet<>();

    private HashMap<String, GCLogStats> gcLogStats = null;

    //    private double covDiff = 0;
    private Set<String> covSet = new HashSet<>();
    private double basePriority;

    public void select(int time) {
        this.lastSelect = time;
        this.selectTime++;
    }

    public void addBrokenTime() {
        brokenTime++;
    }

    public void addInconsistent(DiffCore diffCore) {
        if (!diffCores.contains(diffCore)) {
            diffCores.add(diffCore);
            inconsistentTime++;
        }
    }

    public void addCrashDedup(CrashAnalyzer.DedupInfo dedupInfo) {
        if (!crashDedup.contains(dedupInfo)) {
            crashDedup.add(dedupInfo);
            inconsistentTime++;
        }
    }

    public double getBasePriority() {
        return basePriority;
    }

    public void setBasePriority(double basePriority) {
        this.basePriority = basePriority;
    }

    public int getLastSelect() {
        return lastSelect;
    }

    public int getInconsistentTime() {
        return inconsistentTime;
    }

    public int getBrokenTime() {
        return brokenTime;
    }

    public int getSelectTime() {
        return selectTime;
    }

    public HashMap<String, GCLogStats> getGcLogStats() {
        return gcLogStats;
    }

    public double getCovDiff() {
//        return covDiff;
        return covSet.size();
    }

//    public void setCovDiff(double covDiff) {
//        this.covDiff = covDiff;
//    }

    public void setGcLogStats(HashMap<String, GCLogStats> gcLogStats) {
        this.gcLogStats = gcLogStats;
    }

    public void updateData(HashMap<String, GCLogStats> gcLogStats, int iterate, boolean isBroken, boolean isInconsistent, DiffCore diffCore, HashMap<String, GCLogStats> seedGCLogStats) {
        setGcLogStats(gcLogStats);
        select(iterate);
        if (isBroken) {
            addBrokenTime();
        }
        if (isInconsistent) {
            if (diffCore.getDetailedMessage().contains("Segmentation")) {
                for (CrashAnalyzer.DedupInfo dedupInfo : diffCore.getCrashDedup()) {
                    addCrashDedup(dedupInfo);
                }
            } else {
                addInconsistent(diffCore);
            }
        }
        Set<String> optionPatterns = new HashSet<>();
        if (DTConfiguration.EDGE_COV) {
            for (GCLogStats value : gcLogStats.values()) {
                if (value != null && value.getEdgeCov() != null) {
                    optionPatterns.addAll(value.getEdgeCov());
                }
            }
        } else {
            for (GCLogStats value : gcLogStats.values()) {
                if (value != null && value.getGcEventNGram() != null) {
                    optionPatterns.addAll(value.getGcEventNGram());
                }
            }
        }
        this.covSet.addAll(optionPatterns);
//        Set<String> seedPatterns = new HashSet<>();
//
////        for (GCLogStats value : gcLogStats.values()) {
////            if (value != null && value.getGcEventNGram() != null) {
////                seedPatterns.addAll(value.getGcEventNGram());
////            }
////        }
//        double optionCov = 0;
//        double seedCov = 0;
//        if (optionPatterns.isEmpty() && seedPatterns.isEmpty()) {
//            double cov = 0;
//            if (DTConfiguration.EDGE_COV) {
//                HashSet<String> edgeCov = new HashSet<>();
//                for (GCLogStats value : gcLogStats.values()) {
//                    if (value != null && value.getEdgeCov() != null) {
//                        edgeCov.addAll(value.getEdgeCov());
//                    }
//                }
//                optionCov = edgeCov.size();
//            } else {
//                for (GCLogStats value : gcLogStats.values()) {
//                    if (value != null) {
//                        cov += value.getGcEventCov();
//                    }
//                }
//                optionCov = cov / Math.max(gcLogStats.size(), 1);
//            }
//
//
////            cov = 0;
////            for (GCLogStats value : seedGCLogStats.values()) {
////                if (value != null) {
////                    cov += value.getGcEventCov();
////                }
////            }
////            seedCov = cov / Math.max(seedGCLogStats.size(), 1);
//        } else {
//            Sets.SetView<String> difference = Sets.difference(optionPatterns, seedPatterns);
//            optionCov = difference.size();
//        }
////        this.covDiff += optionCov - seedCov;
//        this.covDiff += optionCov;

    }

    public double calcPriority(int iterate) {
//        double benefit;
//        if (selectTime == 0) {
//            benefit = 0;
//        } else {
////            benefit = (inconsistentTime * 10 - brokenTime * 3) * 1.0 / (inconsistentTime + brokenTime) / (1 + Math.exp(-selectTime));
////            benefit = (inconsistentTime * GCConfiguration.REWARD_WEIGHT - brokenTime * GCConfiguration.PENALTY_WEIGHT) * 1.0 / selectTime;
////            benefit = (inconsistentTime * GCConfiguration.REWARD_WEIGHT - brokenTime * GCConfiguration.PENALTY_WEIGHT) * 1.0 / (inconsistentTime + brokenTime + 1);
//            benefit = (inconsistentTime * GCConfiguration.REWARD_WEIGHT - brokenTime * GCConfiguration.PENALTY_WEIGHT);
//
//        }
////        double timeCompensation = GCConfiguration.DECAY_WEIGHT * Math.log((iterate - lastSelect + 1));
////        double temperature = GCConfiguration.TEMPERATURE_INIT * Math.exp(-iterate * GCConfiguration.TEMPERATURE_DECAY_SPEED);
////        return Math.exp((benefit + timeCompensation) / temperature);
////        return Math.exp(benefit - Math.log(selectTime + 1) * GCConfiguration.DECAY_WEIGHT);
//        int coeffi = covDiff > 0 ? 1 : -1;

//        return 1 / (1 + Math.exp(-(benefit - Math.log(selectTime + 1) * GCConfiguration.DECAY_WEIGHT + coeffi * Math.log(Math.abs(covDiff / selectTime) + 1) * GCConfiguration.GC_COV_DECAY)));
//        priority = Math.log(1 + Math.exp(-mutationTimes * GCConfiguration.DECAY_WEIGHT + getCov() * GCConfiguration.GC_COV_DECAY) - brokenTimes *GCConfiguration.PENALTY_WEIGHT);



//        double sum = this.basePriority
//                - Math.min(selectTime, GCConfiguration.OPTION_THRESHOLD) * GCConfiguration.DECAY_WEIGHT
//                - Math.max(0, selectTime - GCConfiguration.OPTION_THRESHOLD) * GCConfiguration.ACC_DECAY_WEIGHT
//                + this.getCovDiff() * GCConfiguration.GC_COV_DECAY
//                - brokenTime * GCConfiguration.PENALTY_WEIGHT;
//        double exp = Math.exp(sum);
//        if (Double.isInfinite(exp)) {
//            exp = Double.MAX_VALUE;
//        }
//        double priority = Math.log(1 + exp);
//        return priority;
        return (covSet.size() + 1) * 1.0 / (selectTime + 1);
//        return Math.exp((inconsistentTime - brokenTime) * 1.0 / (selectTime + 1));
    }

    public double calcPriorityGC(int iterate) {
        return (covSet.size() + 1) * 1.0 / (selectTime + 1);
    }
}
