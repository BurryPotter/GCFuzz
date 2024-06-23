package optiongen.generator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.FuzzingConfig;
import dtjvms.DTConfiguration;
import dtjvms.DTGlobal;
import dtjvms.DTPlatform;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.HotspotGCType;
import dtjvms.executor.GC.tool.GCLogStats;
import optiongen.bean.GCOption;
import optiongen.bean.OptionBean;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Generator {

    //    private List<Map<String, String>> HOTSPOT_OPTIONS;
//    private List<Map<String, String>> OPENJ9_OPTIONS;
    private List<OptionBean> HOTSPOT_OPTIONS;
    private List<OptionBean> OPENJ9_OPTIONS;
    private List<OptionBean> HOTSPOT_WITHOUT_GC_OPTIONS;
    private List<OptionBean> OPENJ9_WITHOUT_GC_OPTIONS;
    private List<OptionBean> COMMON_OPTIONS;

    private List<Integer> hotspotLastIndexes;
    private List<Integer> openj9LastIndexes;

    private final String WHITE_BOX_OPTION = "-Xbootclasspath/a:" + System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "wb.jar -XX:+WhiteBoxAPI";
    private final String EXPERIMENT_OPTION = "-XX:+UnlockExperimentalVMOptions";
    private final String IGNORE_UNRECOGNIZED_OPTION = "-XX:+IgnoreUnrecognizedVMOptions";
    //    private final String STRING_DEDUPLICATION = "-XX:+UseStringDeduplication";
    private final String NOT_DUMP_OOM_OPTION = "-XX:-HeapDumpOnOutOfMemoryError";
    private final String MODEL_ACCESS_OPTION = "--illegal-access=warn " +
            //hotspot export
            "--add-opens=java.base/java.lang=ALL-UNNAMED " +
            "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " +
            "--add-opens=java.base/jdk.internal.platform=ALL-UNNAMED " +
            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED " +
            "--add-opens=jdk.jartool/sun.tools.jar=ALL-UNNAMED " +
            "--add-opens=java.base/javax.crypto.spec=ALL-UNNAMED " +
            "--add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED " +
            "--add-opens=java.desktop/java.awt=ALL-UNNAMED " +
            //j9 export
            "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED " +
            "--add-opens=java.base/sun.util.locale.provider=ALL-UNNAMED " +
            "--add-opens=jdk.jartool/sun.tools.jar=ALL-UNNAMED " +
            "--add-opens=java.base/sun.security.provider=ALL-UNNAMED " +
            "--add-opens=java.base/sun.security.util=ALL-UNNAMED " +
            "--add-opens=java.base/sun.security.action=ALL-UNNAMED " +
            "--add-opens=java.base/sun.security.x509=ALL-UNNAMED " +
            "--add-opens=java.base/sun.net.www=ALL-UNNAMED " +
            "--add-opens=java.base/java.util=ALL-UNNAMED " +
            "--add-opens=java.base/java.net=ALL-UNNAMED " +
            "--add-opens=java.base/java.lang.ref=ALL-UNNAMED " +
            "--add-opens=java.base/com.sun.crypto.provider=ALL-UNNAMED " +
            "--add-opens=java.desktop/java.awt=ALL-UNNAMED " +
            "--add-opens=java.desktop/com.sun.beans=ALL-UNNAMED " +
            "--add-opens=java.xml.crypto/com.sun.org.apache.xml.internal.security.utils=ALL-UNNAMED " +
            "--add-opens=java.rmi/sun.rmi.registry=ALL-UNNAMED " +
            "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED " +
            "--add-opens=java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED " +
            "--add-opens=java.rmi/sun.rmi.server=ALL-UNNAMED ";
    private final String UNLOCK_DIAGNOSTIC = "-XX:+UnlockDiagnosticVMOptions";
    private final String VERIFY_GC_OPTION = "-XX:+VerifyAfterGC -XX:+VerifyBeforeGC -XX:+VerifyDuringGC";
    private final String BASE_OPTION = EXPERIMENT_OPTION + " " +
            IGNORE_UNRECOGNIZED_OPTION + " " + MODEL_ACCESS_OPTION + " " + NOT_DUMP_OOM_OPTION;
    //    "  -XX:-UseCompressedOops "
    private int RANDOM_MAX = 10;
    private final GCGenerator hotspotGenerator = new GCGenerator(VMType.HOTSPOT);
    private final GCGenerator j9Generator = new GCGenerator(VMType.J9);

    private GCOption<GCType> hotspotGCTypeGCOption;
    private GCOption<GCType> openJ9GCTypeGCOption;

    public List<OptionBean> getHOTSPOT_OPTIONS() {
        return HOTSPOT_OPTIONS;
    }

    public void setHOTSPOT_OPTIONS(List<OptionBean> HOTSPOT_OPTIONS) {
        this.HOTSPOT_OPTIONS = HOTSPOT_OPTIONS;
    }

    public List<OptionBean> getOPENJ9_OPTIONS() {
        return OPENJ9_OPTIONS;
    }

    public void setOPENJ9_OPTIONS(List<OptionBean> OPENJ9_OPTIONS) {
        this.OPENJ9_OPTIONS = OPENJ9_OPTIONS;
    }

//    public List<OptionBean> getHOTSPOT_WITH_GC_OPTIONS() {
//        return HOTSPOT_WITH_GC_OPTIONS;
//    }
//
//    public void setHOTSPOT_WITH_GC_OPTIONS(List<OptionBean> HOTSPOT_WITH_GC_OPTIONS) {
//        this.HOTSPOT_WITH_GC_OPTIONS = HOTSPOT_WITH_GC_OPTIONS;
//    }
//
//    public List<OptionBean> getOPENJ9_WITH_GC_OPTIONS() {
//        return OPENJ9_WITH_GC_OPTIONS;
//    }
//
//    public void setOPENJ9_WITH_GC_OPTIONS(List<OptionBean> OPENJ9_WITH_GC_OPTIONS) {
//        this.OPENJ9_WITH_GC_OPTIONS = OPENJ9_WITH_GC_OPTIONS;
//    }

    public String getWHITE_BOX_OPTION() {
        return WHITE_BOX_OPTION;
    }

    public String getMODEL_ACCESS_OPTION() {
        return MODEL_ACCESS_OPTION;
    }

    public String getEXPERIMENT_OPTION() {
        return EXPERIMENT_OPTION;
    }

    public String getIGNORE_UNRECOGNIZED_OPTION() {
        return IGNORE_UNRECOGNIZED_OPTION;
    }

    public String getNOT_DUMP_OOM_OPTION() {
        return NOT_DUMP_OOM_OPTION;
    }

    public String getVERIFY_GC_OPTION() {
        return VERIFY_GC_OPTION;
    }

    public String getBASE_OPTION() {
        return BASE_OPTION;
    }

    public Map<String, Object> VALUE_POOL;

    public Generator() {
        loadData(null, null, null);
    }

    public Generator(int randomMax) {
        loadData(null, null, null);
        RANDOM_MAX = randomMax;
    }

    public Generator(String hotspotPath, String openj9Path, String commonPath) {
        loadData(hotspotPath, openj9Path, commonPath);
    }

    public Generator(String hotspotPath, String openj9Path, String commonPath, int randomMax) {
        loadData(hotspotPath, openj9Path, commonPath);
        RANDOM_MAX = randomMax;
    }

    private void loadData(String hotspotPath, String openj9Path, String commonPath) {
//        String resource = this.getClass().getClassLoader().getResource("").getPath();
        String resource = System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "vmoptions" + DTPlatform.FILE_SEPARATOR;
        HOTSPOT_OPTIONS = hotspotPath == null ? getJsonString(resource + "hotspot_option.json") : getJsonString(hotspotPath);
        OPENJ9_OPTIONS = openj9Path == null ? getJsonString(resource + "openj9_option.json") : getJsonString(openj9Path);
        COMMON_OPTIONS = commonPath == null ? getJsonString(resource + "common_option.json") : getJsonString(commonPath);

        for (OptionBean hotspotOption : HOTSPOT_OPTIONS) {
            hotspotOption.setBasePriority(hotspotOption.getPriority());
        }
        for (OptionBean openj9Option : OPENJ9_OPTIONS) {
            openj9Option.setBasePriority(openj9Option.getPriority());
        }
        if (DTConfiguration.FIND_UNIQUE_BUG) {
            // old jvm does not recognize option
            OPENJ9_OPTIONS = OPENJ9_OPTIONS.stream()
                    .filter(optionBean -> !Objects.equals(optionBean.getName(), "nocompressedrefs") &&
                            !Objects.equals(optionBean.getName(), "dynamicBreadthFirstScanOrdering"))
                    .collect(Collectors.toList());
        }
        OPENJ9_WITHOUT_GC_OPTIONS = OPENJ9_OPTIONS.stream()
                .filter(optionBean -> !Objects.equals(optionBean.getName(), "gcpolicy"))
                .collect(Collectors.toList());
        HOTSPOT_WITHOUT_GC_OPTIONS = HOTSPOT_OPTIONS.stream()
                .filter(optionBean -> !Objects.equals(optionBean.getName(), "UseSerialGC/UseParallelGC/UseG1GC/UseConcMarkSweepGC/UseZGC"))
                .collect(Collectors.toList());
        // 加载数值池子
        VALUE_POOL = new HashMap<>();
        VALUE_POOL.put("max_heap", 1024 * 1024 * 1024);
        VALUE_POOL.put("max_thread_stack", 1024 * 1024 * 1024);
        VALUE_POOL.put("max_memory", 1024 * 1024 * 1024);

        // 加载大小映射表
        ValueUtil.SIZE_MAP.put("KB", 1024L);
        ValueUtil.SIZE_MAP.put("K", 1024L);
        ValueUtil.SIZE_MAP.put("MB", 1024L * 1024L);
        ValueUtil.SIZE_MAP.put("M", 1024L * 1024L);
        ValueUtil.SIZE_MAP.put("GB", 1024L * 1024L * 1024L);
        ValueUtil.SIZE_MAP.put("G", 1024L * 1024L * 1024L);

        // 加载（openj9）绑定的参数
        ValueUtil.BINDING_OPTION.add("loaminimum/loainitial/loamaximum");
        ValueUtil.BINDING_OPTION.add("mine/maxe");
        ValueUtil.BINDING_OPTION.add("minf/maxf");
        ValueUtil.BINDING_OPTION.add("mint/maxt");
        ValueUtil.BINDING_OPTION.add("mns/mnx");
        ValueUtil.BINDING_OPTION.add("mos/mox");
        ValueUtil.BINDING_OPTION.add("mr/mrx");
        ValueUtil.BINDING_OPTION.add("ms/mx/softmx");


    }

    public void testOption() {
//        OptionFilter filter = new OptionFilter();
//        if (DTConfiguration.TARGET_JVMS.contains("hotspot")) {
//            HOTSPOT_OPTIONS = filter.filter(HOTSPOT_OPTIONS, VMType.HOTSPOT);
//
//        }
////        OPENJ9_OPTIONS = OPENJ9_OPTIONS.subList(26,OPENJ9_OPTIONS.size());
//        if (DTConfiguration.TARGET_JVMS.contains("openj9")) {
//            OPENJ9_OPTIONS = filter.filter(OPENJ9_OPTIONS, VMType.J9);
//
//        }
    }

    /**
     * 读取json文件
     *
     * @param filepath json文件路径
     * @return 返回json字符串
     */
//    private List<Map<String, String>> getJsonString(String filepath) {
//        StringBuffer sb = new StringBuffer();
//        try {
//            Reader reader = new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8);
//            int ch;
//            while ((ch = reader.read()) != -1) {
//                sb.append((char) ch);
//            }
//            reader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return JSONObject.parseObject(sb.toString().replaceAll("\r\n", ""), List.class);
//    }
    private List<OptionBean> getJsonString(String filepath) {
        StringBuffer sb = new StringBuffer();
        try {
            Reader reader = new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8);
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<OptionBean>>() {
        }.getType();

        return gson.fromJson(sb.toString().replaceAll("\r\n", ""), type);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 被判断字符串
     * @return 返回是与否
     */
    public static boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判单jdk版本是否适用
     *
     * @param option
     * @return
     */
    private static boolean isAvailable(OptionBean option) {
        if (isStringEmpty(option.getSince())) {
            return true;
        }
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        int sinceVersion = Integer.parseInt(p.matcher(option.getSince()).replaceAll("").trim());
        int obsoletedVersion = Integer.parseInt(isStringEmpty(option.getObsoleted()) ? "100" : p.matcher(option.getObsoleted()).replaceAll("").trim());
        int expiredVersion = Integer.parseInt(isStringEmpty(option.getExpired()) ? "100" : p.matcher(option.getExpired()).replaceAll("").trim());
        int deprecatedVersion = Integer.parseInt(isStringEmpty(option.getDeprecated()) ? "100" : p.matcher(option.getDeprecated()).replaceAll("").trim());
        for (String version : DTConfiguration.OPENJDK_VERSIONS) {
            int jdkVersion = Integer.parseInt(p.matcher(version).replaceAll("").trim());
            if (jdkVersion < sinceVersion || jdkVersion >= Math.min(obsoletedVersion, Math.min(expiredVersion, deprecatedVersion))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 生产vm options
     *
     * @return 返回一个hashmap，包含hotspot和openj9的options
     */
    public String generateOption(VMType type, int optionNum, boolean random) {
        StringBuilder result = new StringBuilder();
        String vmoption;
        String commonOption = getRandomOption(VMType.COMMON, optionNum, random);
        commonOption = commonOption.replaceAll(ValueUtil.COMMON_PLACEHOLDER, "");
        String gcType;
        switch (type) {
            case HOTSPOT:
                vmoption = getRandomOption(VMType.HOTSPOT, optionNum, random);
                gcType = hotspotGCTypeGCOption.getGcOption();
                break;
            case J9:
                vmoption = getRandomOption(VMType.J9, optionNum, random);
                gcType = openJ9GCTypeGCOption.getGcOption();
                break;
            default:
                throw new RuntimeException("unsupport vmtype:" + type);
        }
        result.append(UNLOCK_DIAGNOSTIC)
                .append(" ")
                .append(EXPERIMENT_OPTION);
        if (!random) {
            result.append(" ")
                    .append(gcType);
        }
        result.append(" ")
                .append(vmoption)
                .append(" ")
                .append(commonOption)
                .append(" ")
                .append(BASE_OPTION);
        return result.toString();
    }

    public GCOption<GCType> generateGCOption(VMType type, boolean random) {
        switch (type) {
            case HOTSPOT:
                this.hotspotGCTypeGCOption = hotspotGenerator.randomGCOption(random);
                return this.hotspotGCTypeGCOption;

            case J9:
                this.openJ9GCTypeGCOption = j9Generator.randomGCOption(random);
                return this.openJ9GCTypeGCOption;
            default:
                throw new RuntimeException("unsupport vmtype:" + type);
        }
    }

    public GCOption<GCType> getGCOption(VMType type) {
        switch (type) {
            case HOTSPOT:
                return this.hotspotGCTypeGCOption;
            case J9:
                return this.openJ9GCTypeGCOption;
            default:
                throw new RuntimeException("unsupport vmtype:" + type);
        }
    }

    public List<GCOption<GCType>> getSupportGCTypes(VMType type) {
        switch (type) {
            case HOTSPOT:
                return hotspotGenerator.getGcOptions();
            case J9:
                return j9Generator.getGcOptions();
            default:
                throw new RuntimeException("unsupport vmtype:" + type);
        }
    }

    /**
     * 获取随机option
     *
     * @param vmType 虚拟机类型
     * @return 返回option命令行
     */
//    private String getRandomOption(VMType vmType, int optionNum) {
//        return getRandomOption(vmType, optionNum, false);
//    }

    /**
     * 获取随机option
     *
     * @param vmType 虚拟机类型
     * @return 返回option命令行
     */
    private String getRandomOption(VMType vmType, int optionNum, boolean random) {
        ValueUtil valueUtil;
//        List<Map<String, String>> list;
        List<OptionBean> list;
        if (random) {
            // random set GC option
            if (vmType == VMType.J9) {
                list = OPENJ9_OPTIONS;
                valueUtil = new Openj9ValueUtil();
            } else if (vmType == VMType.HOTSPOT) {
                list = HOTSPOT_OPTIONS;
                valueUtil = new HotspotValueUtil();
            } else {
                return MemorySizeUtil.generate();
            }
        } else {
            // GC option has been set before
            if (vmType == VMType.J9) {
                list = OPENJ9_WITHOUT_GC_OPTIONS;
                valueUtil = new Openj9ValueUtil();
            } else if (vmType == VMType.HOTSPOT) {
                list = HOTSPOT_WITHOUT_GC_OPTIONS;
                valueUtil = new HotspotValueUtil();
            } else {
                return MemorySizeUtil.generate();
            }
        }
//        int pickNum = FuzzingConfig.nextChoice(Math.min(list.size(), optionNum), FuzzingConfig.RandomType.OPTION_POOL);
//        List<Integer> indexes = null;
//        List<Double> weights = new ArrayList<>();
//        for (OptionBean optionBean : list) {
//            weights.add(optionBean.getPriority());
//        }
//        indexes = WeightedRandom.randomPick(list.size(), pickNum);
        List<Integer> indexes = WeightedRandom.randomPick(list.size(), optionNum);
//        if (!random && !indexes.contains(0)) {
//            indexes.add(0);
//        }

//        List<Integer> indexes = null;
//        if (random) {
//            indexes = WeightedRandom.randomPick(list.size(), optionNum);
//        } else {
//            List<Double> weights = new ArrayList<>();
//            for (OptionBean optionBean : list) {
//                weights.add(optionBean.getPriority());
//            }
//            indexes = WeightedRandom.pick(weights, optionNum);
//        }

        if (vmType == VMType.J9) {
            openj9LastIndexes = indexes;
        } else {
            hotspotLastIndexes = indexes;
        }
        Set<String> mutexSet = new HashSet<>();
        StringBuilder optionCmd = new StringBuilder();
        for (Integer index : indexes) {

//            Map<String, String> option = list.get(idx);
            OptionBean option = list.get(index);
            String name = option.getName();

            if (mutexSet.contains(name) || !isAvailable(option)) {
                continue;
            }
            mutexSet.addAll(Arrays.asList(isStringEmpty(option.getMutex()) ? new String[0] : option.getMutex().split(",")));
            String type = option.getType();
            String scope = option.getScope();
            String prefix = option.getPrefix();
            String value = valueUtil.getValue(prefix, name, type, scope);
            if (value != null) {
                optionCmd.append(value).append(" ");

            }
        }

        return optionCmd.toString();
    }

    //    public void feedback(VMType vmType, HashMap<String, GCLogStats> gcLogStats, int iterate, boolean isBroken, boolean isInconsistent, DiffCore diffCore, HashMap<String, GCLogStats> seedGCLogStats) {
//        List<OptionBean> list;
//        List<Integer> lastIndexes;
//        GCGenerator gcGenerator;
//        if (vmType == VMType.J9) {
//            list = OPENJ9_OPTIONS;
//            lastIndexes = openj9LastIndexes;
//            gcGenerator = j9Generator;
//        } else if (vmType == VMType.HOTSPOT) {
//            list = HOTSPOT_OPTIONS;
//            lastIndexes = hotspotLastIndexes;
//            gcGenerator = hotspotGenerator;
//
//        } else {
//            throw new RuntimeException("could not reach here");
//        }
////        if (vmType == VMType.J9) {
////            noOptionCov = 0;
////        }
//        gcGenerator.feedback(gcLogStats, iterate, isBroken, isInconsistent, diffCore, seedGCLogStats);
//
//        DTGlobal.getOptionPriorityLogger().info(String.format("%12s %s", "Priority", "OptionName"));
//
//        for (Integer lastIndex : lastIndexes) {
//            OptionBean optionBean = list.get(lastIndex);
//            optionBean.updateData(gcLogStats, iterate, isBroken, isInconsistent, diffCore, seedGCLogStats);
//            double newPriority = optionBean.calcPriority(iterate);
//            DTGlobal.getOptionPriorityLogger().info(String.format("%5.2f->%-5.2f diff:%d broke:%d sel:%d cov:%.2f %s",
//                    optionBean.getPriority(), newPriority, optionBean.getInconsistentTime(), optionBean.getBrokenTime(), optionBean.getSelectTime(), optionBean.getCovDiff(), optionBean.getName()));
//            optionBean.setPriority(newPriority);
//        }
//
////        for (OptionBean optionBean : list) {
////            double newPriority = optionBean.calcPriority(iterate);
////
////            DTGlobal.getOptionPriorityLogger().info(String.format("%5.2f->%-5.2f %s", optionBean.getPriority(), newPriority, optionBean.getName()));
////
////            optionBean.setPriority(newPriority);
////        }
//    }
    public void feedback(boolean random, VMType vmType, HashMap<String, GCLogStats> gcLogStats, int iterate, boolean isBroken, boolean isInconsistent, DiffCore diffCore, HashMap<String, GCLogStats> seedGCLogStats) {
        List<OptionBean> list;
        List<Integer> lastIndexes;
        if (vmType == VMType.J9) {
            if (random) {
                list = OPENJ9_OPTIONS;
            } else {
                list = OPENJ9_WITHOUT_GC_OPTIONS;
            }
            lastIndexes = openj9LastIndexes;
        } else if (vmType == VMType.HOTSPOT) {
            if (random) {
                list = HOTSPOT_OPTIONS;
            } else {
                list = HOTSPOT_WITHOUT_GC_OPTIONS;
            }
            lastIndexes = hotspotLastIndexes;

        } else {
            throw new RuntimeException("could not reach here");
        }

//        if (vmType == VMType.J9) {
//            noOptionCov = 0;
//        }
        if (vmType == VMType.J9) {
            j9Generator.feedback(gcLogStats, iterate, isBroken, isInconsistent, diffCore, seedGCLogStats);
        } else {
            hotspotGenerator.feedback(gcLogStats, iterate, isBroken, isInconsistent, diffCore, seedGCLogStats);
        }

        DTGlobal.getOptionPriorityLogger().info(String.format("%12s %s", "Priority", "OptionName"));

        for (Integer lastIndex : lastIndexes) {
            OptionBean optionBean = list.get(lastIndex);
            optionBean.updateData(gcLogStats, iterate, isBroken, isInconsistent, diffCore, seedGCLogStats);
            double newPriority;
            if (lastIndex == 0) {
                //NOTE option.json : GC option must be stored at index 0
                newPriority = optionBean.calcPriorityGC(iterate);
            } else {
                newPriority = optionBean.calcPriority(iterate);
            }
            DTGlobal.getOptionPriorityLogger().info(String.format("%5.2f->%-5.2f diff:%d broke:%d sel:%d cov:%.2f %s",
                    optionBean.getPriority(), newPriority, optionBean.getInconsistentTime(), optionBean.getBrokenTime(), optionBean.getSelectTime(), optionBean.getCovDiff(), optionBean.getName()));
            optionBean.setPriority(newPriority);
        }
    }
}
