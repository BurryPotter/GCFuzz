package reduce;

import dtjvms.*;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.HotspotGCType;
import dtjvms.executor.GC.OpenJ9GCType;
import dtjvms.executor.JvmOutput;
import dtjvms.fjvms.config.ConfigBean;
import dtjvms.fjvms.config.ConfigJTRegBean;
import dtjvms.loader.DTLoader;
import main.MainHelper;
import main.ReplayInfo;
import main.Status;
import optiongen.generator.VMType;
import org.apache.commons.cli.*;
import org.junit.Test;
import util.LoggerUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OptionReduce {

    public static final String TEMPLATE_FOLDER = "./template";

    public static final String GCOBJ_FILE_PATH = "./GCObj.class";
    public static String ROOT = "D:\\projects\\GCGenerator\\GCFuzzing";
    //    public static final String ROOT = "/home/zhengkai/JITFuzzing-main";
    public static String jvmpath = ROOT + DTPlatform.FILE_SEPARATOR + "01JVMS";
    static ArrayList<JvmInfo> jvmCmds;

    public static ProjectInfo targetProject;

    //    public static final String projectPath = ROOT + DTPlatform.FILE_SEPARATOR + "sootOutput/Openj9Test-Test\\out\\production\\Openj9Test-Test";
    static String projectName = "avrora-cvs-20091224-3";

    public static String timeStamp = "1704151625";
    public static String resultsPath = "D:\\projects\\GCGenerator\\ecoop\\rq1\\avrora_139_17_GCFuzz_Random_Edge_1_3\\03results";
    public static String diffClassPath = resultsPath + DTPlatform.FILE_SEPARATOR + timeStamp + DTPlatform.FILE_SEPARATOR + "classHistory";
    //    public static final String diffClassPath = ROOT + DTPlatform.FILE_SEPARATOR + "03results/" + timeStamp + "/classHistory";
    public static String statusPath = resultsPath + DTPlatform.FILE_SEPARATOR + timeStamp + DTPlatform.FILE_SEPARATOR + projectName + DTPlatform.FILE_SEPARATOR + "status.log";
//    public static final String statusPath = ROOT + DTPlatform.FILE_SEPARATOR + "03results/" + timeStamp + "/HotspotTests-Java/status.log";

    public static String className = "avrora.Main_0801@1704233777785.class";
    public static String option = "-Xconmeter:dynamic -Xminf0.41 -Xmaxf0.5 -Xgc:stdGlobalCompactToSatisfyAllocate -Xlp:objectheap:pagesize=4K,warn -Xgc:overrideHiresTimerCheck -Xenableexcessivegc -Xgc:targetPausetime=73 -XX:-HeapManagementMXBeanCompatibility -Xgc:verbosegcCycleTime=1174972522   -Xms1024m -Xmx1024m";

    public static final String[] GC_PERF_EXCEPTION = new String[]{"JvmOutput-TIMEOUT", "OutOfMemoryError"};

    /**
     * step 1
     * copy origin jimple to template dir
     */
    @Test
    public void copy() throws IOException {
        String target = className.substring(0, className.lastIndexOf("_"));

        String originPath = diffClassPath + DTPlatform.FILE_SEPARATOR + target;
        String originClass = className + ".class";
        String originJimple = className + ".jimple";
        String targetClass = target + ".class";
        String targetJimple = target + ".jimple";
        Files.copy(Paths.get(originPath + DTPlatform.FILE_SEPARATOR + originJimple), Paths.get(TEMPLATE_FOLDER + DTPlatform.FILE_SEPARATOR + targetJimple), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Paths.get(originPath + DTPlatform.FILE_SEPARATOR + originClass), Paths.get(TEMPLATE_FOLDER + DTPlatform.FILE_SEPARATOR + targetClass), StandardCopyOption.REPLACE_EXISTING);
    }

    private static HashMap<String, String> parseDetailOutput(String detailMessage) {

        // 使用正则表达式匹配JDK信息和括号中的内容
        Pattern pattern = Pattern.compile("((\\w+[-\\._]?)*):\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(detailMessage);

        HashMap<String, String> results = new HashMap<>();
        // 循环匹配并输出结果
        while (matcher.find()) {
            String jdkVersion = matcher.group(1);
            String output = matcher.group(3);
            results.put(jdkVersion, output);
            System.out.println("JVM: " + jdkVersion);
            System.out.println("Error: " + output);
        }
        return results;
    }

    private static String lastReduceTag() throws FileNotFoundException {
        String logPath = LoggerUtils.getLoggerPath(timeStamp + DTPlatform.FILE_SEPARATOR + projectName, "optionReduce", ".log");
        File file = new File(logPath);
        if (!file.exists()) {
            return null;
        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        List<String> reduceLines = bufferedReader.lines().collect(Collectors.toList());
        if (reduceLines.size() == 0) {
            return null;
        }
        String lastLine = reduceLines.get(reduceLines.size() - 1);
        String tag = lastLine.substring(lastLine.indexOf("<CR"), lastLine.indexOf(">") + 1); // note: equal to ReplayInfo's tag
        return tag;
    }

    public static void main(String[] args) throws IOException {
        argsParser(args);
        System.out.println("status file: " + statusPath);
        String lastReduceTag = lastReduceTag();
        DTGlobal.setClassPriorityLogger(timeStamp + DTPlatform.FILE_SEPARATOR + projectName, "optionReduce");
        DTGlobal.setDiffLogger(timeStamp + DTPlatform.FILE_SEPARATOR + projectName, "reduce_diff");
        System.out.println("last reduce tag: " + lastReduceTag);
        File f = new File(statusPath);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        ArrayList<ReduceInfo> infoArrayList = new ArrayList<>();
        AtomicBoolean reachLast = new AtomicBoolean(lastReduceTag == null);
        reader.lines().forEach(line -> {
            if (line.contains("CR:") && line.contains("DifferenceFound")) {
                ArrayList<String> options = new ArrayList<>();
                ReplayInfo replayInfo = Status.parseStatus(line);
                if (replayInfo.getTag().equals(lastReduceTag)) {
                    reachLast.set(true);
                }
                if (!reachLast.get()) {
                    return;
                }
                System.out.println(replayInfo.getTag());
                String vmoption = replayInfo.getVmoption();
                String gcOptions = vmoption.substring(0, vmoption.indexOf("--illegal-access=warn"))
                        .replace("-XX:+UnlockExperimentalVMOptions", "")
                        .replace("-XX:+UnlockDiagnosticVMOptions", "")
                        .replace("-XX:+IgnoreUnrecognizedVMOptions", "");
                String gcOption = "";
                GCType gcType = HotspotGCType.G1;
                if (replayInfo.getVmType() == VMType.J9) {
                    gcType = OpenJ9GCType.GENCON;
                }
//                boolean setGC = false;
                for (String option : gcOptions.trim().split("\\s+")) {
//                    if (option.contains("-XX:+Use") && option.contains("GC")) {
//                        gcType = HotspotGCType.G1;
//                        gcOption = option;
//                        setGC = true;
//                        continue;
//                    } else if (option.contains("-Xgcpolicy:")) {
//                        gcType = OpenJ9GCType.GENCON;
//                        gcOption = option;
//                        setGC = true;
//                        continue;
//                    }
                    options.add(option);
                }
//                if (setGC){
//                    gcOption = "";
//                }
                System.out.println(options);
                infoArrayList.add(new ReduceInfo(replayInfo, options, gcOption, gcType));
            }
        });
        Status.timeStamp = timeStamp;
        Status.projectName = projectName;
        DTGlobal.setGcLogPath(System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "03results" +
                DTPlatform.FILE_SEPARATOR + Status.timeStamp +
                DTPlatform.FILE_SEPARATOR + "gcLogs");
        jvmCmds = DTLoader.getInstance().loadJvms();
        DTConfiguration.setJvmDepensRoot(jvmpath);
        targetProject = DTLoader.getInstance().loadTargetProjectWithGivenPath(
                ROOT + DTPlatform.FILE_SEPARATOR + "sootOutput", projectName, null, false);
        MainHelper.copyToFile(GCOBJ_FILE_PATH, targetProject.getSrcClassPath() + DTPlatform.FILE_SEPARATOR + GCOBJ_FILE_PATH);

//        targetProject = DTLoader.getInstance().loadTargetProjectWithGivenPath(projectPath, targetProjectName, null, false);
        System.out.println(targetProject);
        String parameters = "";
        if (Status.projectName.contains("sunflow-0.07.2")) {
            parameters = "-bench 2 256";
        } else if (Status.projectName.contains("pmd-4.2.5")) {
            parameters = "Hello.java text unusedcode";
        } else if (Status.projectName.contains("jython-dacapo")) {
            parameters = "-S hello.py";
        } else if (Status.projectName.contains("fop-dacapo")) {
            parameters = "-xml name.xml -xsl name2fo.xsl -pdf name.pdf";
        } else if (Status.projectName.contains("eclipse-dacapo")) {
            parameters = "-debug";
        } else if (Status.projectName.contains("avrora-cvs-20091224")) {
            parameters = "-action=cfg example.asm";
        }
        Set<String> notGCClassNameSet = new HashSet<>();
        System.out.println(DTPlatform.getInstance());

        for (ReduceInfo reduceInfo : infoArrayList) {
            className = reduceInfo.getReplayInfo().getTestClass().replace(".class", "");
            option = String.join(" ", reduceInfo.getGcOptions());
            if (notGCClassNameSet.contains(className)) {
                continue;
            }
//            OptionReduce optionReduce = new OptionReduce();
//            optionReduce.copy();
//            String target = className.substring(0, className.lastIndexOf("_"));

//            String originClass = className + ".class";
//            String originJimple = className + ".jimple";
//            String targetClass = target + ".class";
//            String targetJimple = target + ".jimple";
//            Files.copy(Paths.get(originPath + DTPlatform.FILE_SEPARATOR + originJimple), Paths.get(TEMPLATE_FOLDER + DTPlatform.FILE_SEPARATOR + targetJimple), StandardCopyOption.REPLACE_EXISTING);
//            Files.copy(Paths.get(originPath + DTPlatform.FILE_SEPARATOR + originClass), Paths.get(TEMPLATE_FOLDER + DTPlatform.FILE_SEPARATOR + targetClass), StandardCopyOption.REPLACE_EXISTING);

            String originClassName = className.substring(0, className.lastIndexOf("_"));
            String originPath = diffClassPath + DTPlatform.FILE_SEPARATOR + originClassName;

            String simpleClassName = "";
            if (originClassName.contains(".")) {
                simpleClassName = originClassName.substring(originClassName.lastIndexOf(".") + 1);
            } else {
                simpleClassName = originClassName;
            }
            String packagePath = ".";

            if (originClassName.contains(".")) {
                packagePath = "." + DTPlatform.FILE_SEPARATOR + originClassName.substring(0, originClassName.lastIndexOf('.')).replace(".", DTPlatform.FILE_SEPARATOR);
            }

            Files.copy(Paths.get(originPath + DTPlatform.FILE_SEPARATOR + className + ".class"),
                    Paths.get(targetProject.getSrcClassPath() + DTPlatform.FILE_SEPARATOR + packagePath + DTPlatform.FILE_SEPARATOR + simpleClassName + ".class"),
                    StandardCopyOption.REPLACE_EXISTING);
            DiffCore replayDiffCore = exec(originClassName, option, reduceInfo, simpleClassName, parameters);
            if (replayDiffCore == null) {
//                notGCClassNameSet.add(className);
                String infoMessage = reduceInfo.getReplayInfo().getTag() + " <reduce info> not replay " + reduceInfo.replayInfo.getTestClass();
                System.out.println(infoMessage);
                DTGlobal.getClassPriorityLogger().info(infoMessage);

                continue;
            }
//            boolean needReduce =  needReduce(replayDiffCore);
//            if (!needReduce){
//                String infoMessage = reduceInfo.getReplayInfo().getTag() + " <reduce info> not need reduce " + reduceInfo.replayInfo.getTestClass();
//                System.out.println(infoMessage);
//                DTGlobal.getClassPriorityLogger().info(infoMessage);
//                continue;
//            }
            DiffCore noOptionDiffCore = exec(originClassName, "", reduceInfo, simpleClassName, parameters);
            if (diffCoreEqual(replayDiffCore, noOptionDiffCore)) {
                notGCClassNameSet.add(className);
                String infoMessage = reduceInfo.getReplayInfo().getTag() + " <reduce info> not gc relate " + reduceInfo.replayInfo.getTestClass();
                System.out.println(infoMessage);
                DTGlobal.getClassPriorityLogger().info(infoMessage);

                continue;
            }
            ArrayList<String> reducedOptions = reduceOption(reduceInfo.gcOptions, reduceInfo, replayDiffCore, originClassName, simpleClassName, parameters);
            String infoMessage = reduceInfo.getReplayInfo().getTag() + " <reduce info> gc relate " + reduceInfo.replayInfo.getTestClass() + " reducedOption: " + reduceInfo.getGcType() + " " + String.join(" ", reducedOptions);
            System.out.println(infoMessage);
            DTGlobal.getClassPriorityLogger().info(infoMessage);
        }
    }

    public static boolean needReduce(DiffCore replayDiffCore) {
        String detailMessage = replayDiffCore.getDetailedMessage();
        boolean perfRelate = false;
        for (String gcPerfException : GC_PERF_EXCEPTION) {
            if (detailMessage.contains(gcPerfException)) {
                perfRelate = true;
                break;
            }
        }
        if (!perfRelate) {
            return true;
        }
        boolean needReduce = true;
        HashMap<String, JvmOutput> results = replayDiffCore.getResults();
        for (String s : results.keySet()) {
            JvmOutput output = results.get(s);
            for (String error : output.getFEEInfo()) {
                boolean isPerf = false;
                for (String gcPerfException : GC_PERF_EXCEPTION) {
                    isPerf |= error.contains(gcPerfException);
                }
                if (!isPerf){ // if not all gc-perf-related errors, don't need to reduce it. Because other errors effect the inspection of perf-related error.
                    needReduce = false;
                    break;
                }
            }
        }
        return needReduce;
    }

    public static ArrayList<String> reduceOption(ArrayList<String> options, ReduceInfo reduceInfo, DiffCore stdDiffCore, String originClassName, String className, String parameters) {
        // Base case: 如果输入的元素列表为空，说明没有找到目标组合
        ArrayList<String> remainOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            System.out.println("reduce option index=" + i);
            ArrayList<String> newOptions = new ArrayList<>(options);
            newOptions.remove(i);
            boolean remainDiff = diffCoreEqual(stdDiffCore, exec(originClassName, String.join(" ", newOptions), reduceInfo, className, parameters));
            if (!remainDiff) {
                remainOptions.add(options.get(i));
            }
        }
        return remainOptions;
    }

    public static boolean diffCoreEqual(DiffCore diffCore1, DiffCore diffCore2) {
        if (diffCore1 == diffCore2) {
            return true;
        }
        if (diffCore1 != null && diffCore2 != null) {
            String detail1 = diffCore1.getDetailedMessage();
            String detail2 = diffCore2.getDetailedMessage();

            if (detail1 == null && detail2 == null) {
                return true;
            }
            if (detail1 == null || detail2 == null) {
                return false;
            }
            HashMap<String, String> jdk2err = parseDetailOutput(detail1);
            HashMap<String, String> jdk2err2 = parseDetailOutput(detail2);
            boolean equal = true;
            for (String key : jdk2err.keySet()) {
                equal = equal && jdk2err.get(key).equals(jdk2err2.get(key));
            }
            return equal;
        }
        return false;
    }

    public static DiffCore exec(String originClassName, String option, ReduceInfo reduceInfo, String className, String parameters) {
        ConfigJTRegBean configBean = new ConfigJTRegBean(originClassName, baseOptions + " " + reduceInfo.getGcType() + " " + option, className, parameters);
        ArrayList<ConfigBean> arrayList = new ArrayList<>();
        HashMap<String, ArrayList<ConfigBean>> configBeans = new HashMap<>();

        arrayList.add(configBean.getBean());
        configBeans.put(originClassName, arrayList);
//        ConfigGenerator.generate(targetProject, vmOption, "");
//        configBeans.put(targetClassName, configBeans.get(originClass));
        targetProject.setApplicationClasses(new ArrayList<>());
        targetProject.setJunitClasses(new ArrayList<>());
        targetProject.getApplicationClasses().add(originClassName);
        ReduceExecutor.getInstance().dtSingleClassInProj(jvmCmds, targetProject, originClassName, configBeans, reduceInfo.getType(), 0, 0);
        DiffCore diffCore = ReduceExecutor.getInstance().getDiffCore();
        if (diffCore == null) {
            return null;
        }
        String detailMessage = diffCore.getDetailedMessage();
        boolean inconsistent = false;
        if (detailMessage == null) {
            return null;
        }
        if (detailMessage.contains("TIMEOUT") || detailMessage.contains("OutOfMemoryError")) {
            // rerun.
            for (int i = 0; i < 3; i++) {
                System.out.println("=====rerun round " + (i + 1) + "=====");
                ReduceExecutor.getInstance().dtSingleClassInProj(jvmCmds, targetProject, originClassName, configBeans, reduceInfo.getType(), 0, 0);
                DiffCore newDiffCore = ReduceExecutor.getInstance().getDiffCore();
                String newDetailMessage = "";
                if (newDiffCore != null) {
                    newDetailMessage = newDiffCore.getDetailedMessage();
                }
                if (!detailMessage.equals(newDetailMessage)) {
                    inconsistent = true;
                    break;
                }
            }
        }
        if (inconsistent) {
            return null;
        }
//        System.out.println(diffCore);
        return diffCore;
    }

    public static class ReduceInfo {
        ReplayInfo replayInfo;
        ArrayList<String> gcOptions;
        String gcType;
        GCType type;

        public ReduceInfo(ReplayInfo replayInfo, ArrayList<String> gcOptions, String gcType, GCType type) {
            this.replayInfo = replayInfo;
            this.gcOptions = gcOptions;
            this.gcType = gcType;
            this.type = type;
        }

        public GCType getType() {
            return type;
        }

        public void setType(GCType type) {
            this.type = type;
        }

        public String getGcType() {
            return gcType;
        }

        public void setGcType(String gcType) {
            this.gcType = gcType;
        }

        public ReplayInfo getReplayInfo() {
            return replayInfo;
        }

        public void setReplayInfo(ReplayInfo replayInfo) {
            this.replayInfo = replayInfo;
        }

        public ArrayList<String> getGcOptions() {
            return gcOptions;
        }

        public void setGcOptions(ArrayList<String> gcOptions) {
            this.gcOptions = gcOptions;
        }
    }

    public static org.apache.commons.cli.Options options = null;

    public static void loadOptions() {

        options = new org.apache.commons.cli.Options();
        options.addOption(new Option("t", "timestamp", true, "time stamp for saving results"));
        options.addOption(new Option("p", "project", true, "test project, seed programs"));
        options.addOption(new Option("b", "base root path", true, "set base root path"));
        options.addOption(new Option("r", "results root path", true, "set results root path"));
        options.addOption(new Option("ujdk", "unique-jdk", true, "set unique JDK version"));
        options.addOption(new Option("h", "help", false, "print all system settings"));
    }

    public static void argsParser(String[] args) {
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
            return;
        }
        CommandLine options = cmd;
        HelpFormatter formatter = new HelpFormatter();

        if (options.hasOption("t")) {
            timeStamp = options.getOptionValue("t");
        }
        if (options.hasOption("p")) {
            projectName = options.getOptionValue("p");
        }

        if (options.hasOption("b")) {
            ROOT = options.getOptionValue("b");
        }
        if (options.hasOption("r")) {
            resultsPath = options.getOptionValue("r");
        }
        if (options.hasOption("h")) {
            formatter.printHelp("Option Reduce Commands", MainHelper.options);
            System.out.println();
        }
        if (options.hasOption("ujdk")) {
            DTConfiguration.UNIQUE_JDK_VERSION = Integer.parseInt(options.getOptionValue("ujdk"));
        }
        jvmpath = ROOT + DTPlatform.FILE_SEPARATOR + "01JVMS";
        diffClassPath = resultsPath + DTPlatform.FILE_SEPARATOR + timeStamp + DTPlatform.FILE_SEPARATOR + "classHistory";
        statusPath = resultsPath + DTPlatform.FILE_SEPARATOR + timeStamp + DTPlatform.FILE_SEPARATOR + projectName + DTPlatform.FILE_SEPARATOR + "status.log";
    }

    public static final String baseOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+IgnoreUnrecognizedVMOptions -XX:-HeapDumpOnOutOfMemoryError " +
            "--illegal-access=warn " +
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
}