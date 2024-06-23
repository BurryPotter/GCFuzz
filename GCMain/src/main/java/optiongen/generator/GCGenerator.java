package optiongen.generator;


import config.FuzzingConfig;
import dtjvms.DTConfiguration;
import dtjvms.DTGlobal;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.GCHelper;
import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.HotspotGCType;
import dtjvms.executor.GC.OpenJ9GCType;
import dtjvms.executor.GC.tool.GCLogStats;
import optiongen.bean.GCOption;

import java.util.*;

public class GCGenerator {
    private VMType vmType;
    private String jvmName;
    private List<GCOption<GCType>> gcOptions;

    private int lastPosition;

    public GCGenerator(VMType vmType) {
        this.vmType = vmType;
        gcOptions = new ArrayList<GCOption<GCType>>();
        List<GCType> allGCTypes;
        switch (vmType) {
            case HOTSPOT:
                this.jvmName = "hotspot";
                allGCTypes = Arrays.asList(HotspotGCType.values());
                break;
            case J9:
                this.jvmName = "openj9";
                allGCTypes = Arrays.asList(OpenJ9GCType.values());
                break;
            default:
                throw new RuntimeException("not support VMType: " + vmType);
        }

        Set<String> jdkVersions = DTConfiguration.OPENJDK_VERSIONS;
        HashMap<GCType, Integer> gcTypeCounter = new HashMap<>();
        for (GCType type : allGCTypes) {
            gcTypeCounter.put(type, 0);
        }
        if (DTConfiguration.FIND_UNIQUE_BUG) {
            List<GCType> gcTypes = GCHelper.supportGC(DTConfiguration.UNIQUE_JDK_VERSION, jvmName);
            for (GCType type : gcTypes) {
                gcTypeCounter.put(type, gcTypeCounter.get(type) + 1);
            }
        } else {
            for (String version : jdkVersions) {
                List<GCType> gcTypes = GCHelper.supportGC(version, jvmName);
                for (GCType type : gcTypes) {
                    gcTypeCounter.put(type, gcTypeCounter.get(type) + 1);
                }
            }
        }

        gcTypeCounter.forEach((key, value) -> {
            if (value >= 2 || (DTConfiguration.FIND_UNIQUE_BUG && value >= 1)) {
                System.out.println("support gc type: " + key);
                gcOptions.add(new GCOption<>(key));
            }
        });
    }

    public GCOption<GCType> randomGCOption(boolean random) {
        int position = 0;
        if (random) {
            position = FuzzingConfig.getRandom(FuzzingConfig.RandomType.OPTION_POOL).nextInt(gcOptions.size());
        } else {
            List<Double> list = new ArrayList<>();
            gcOptions.forEach(gcTypeGCOption -> {
                list.add(gcTypeGCOption.getPriority());
            });
            position = WeightedRandom.pick(list);
        }
        lastPosition = position;
        return this.gcOptions.get(position);
    }

    public void feedback(HashMap<String, GCLogStats> gcLogStats, int iterate, boolean isBroken, boolean isInconsistent, DiffCore diffCore, HashMap<String, GCLogStats> seedGCLogStats) {
        GCOption<GCType> gcOption = gcOptions.get(lastPosition);
        gcOption.updateData(gcLogStats, iterate, isBroken, isInconsistent, diffCore, seedGCLogStats);
        DTGlobal.getOptionPriorityLogger().info(String.format("%12s %s", "GC Priority", "OptionName"));

        double newPriority = gcOption.calcPriorityGC(iterate);
//        DTGlobal.getOptionPriorityLogger().info(String.format("%5.2f->%-5.2f %s", gcOption.getPriority(), newPriority, gcOption.getGcType().toString()));
        DTGlobal.getOptionPriorityLogger().info(String.format("%5.2f->%-5.2f diff:%d broke:%d sel:%d cov:%.2f %s",
                gcOption.getPriority(), newPriority, gcOption.getInconsistentTime(), gcOption.getBrokenTime(), gcOption.getSelectTime(), gcOption.getCovDiff(), gcOption.getGcType().toString()));

        gcOption.setPriority(newPriority);
    }

    public List<GCOption<GCType>> getGcOptions() {
        return gcOptions;
    }
}
