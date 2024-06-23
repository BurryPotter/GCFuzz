package reduce;

import dtjvms.*;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.Executor;
import dtjvms.executor.ExecutorHelper;
import dtjvms.executor.GC.GCHelper;
import dtjvms.executor.GC.GCLogDT;
import dtjvms.executor.GC.GCOutput;
import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.tool.EdgeCovAnalyzer;
import dtjvms.executor.GC.tool.GCDiffCore;
import dtjvms.executor.GC.tool.GCLogAnalyzer;
import dtjvms.executor.GC.tool.GCLogStats;
import dtjvms.executor.JvmOutput;
import dtjvms.fjvms.config.ConfigBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ReduceExecutor extends Executor {
    private JvmOutput currentOutput;
    private Process currentProcess;
    private boolean debug = false;
    private boolean diffFound;
    private boolean perfDiffFound;

    private boolean normalDiff;
    private boolean rerun;
    public static ReduceExecutor reduceExecutor;

    private DiffCore diffCore;
    private HashMap<String, GCLogStats> gcLogStatsHashMap = null;
    private HashMap<String, JvmOutput> outputDiff = null;

    private int timeoutCount = 0;

    private boolean disCard = false;

    public static ReduceExecutor getInstance() {
        if (reduceExecutor == null) {
            reduceExecutor = new ReduceExecutor();
        }
        return reduceExecutor;
    }

    public HashMap<String, GCLogStats> getGcLogStatsHashMap() {
        return gcLogStatsHashMap;
    }

    public HashMap<String, JvmOutput> getOutputDiff() {
        return outputDiff;
    }

    public boolean isDisCard() {
        return disCard;
    }

    public boolean isPerfDiffFound() {
        return perfDiffFound;
    }

    public boolean isDiffFound() {
        return diffFound;
    }

    public boolean isNormalDiff() {
        return normalDiff;
    }

    public boolean isRerun() {
        return rerun;
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public DiffCore getDiffCore() {
        return diffCore;
    }

    private void init() {
        diffFound = false;
        normalDiff = false;
        rerun = false;
        perfDiffFound = false;
        timeoutCount = 0;
        disCard = false;
        Path directory = Paths.get(DTGlobal.getGcLogPath());
        remakeDir(directory);

    }

    private static void remakeDir(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                        .filter(path -> path.toFile().isFile())
//                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // handle the exception here
                                e.printStackTrace();
                            }
                        });
            } else {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            // handle the exception here
            e.printStackTrace();
        }
    }

    public void shutDown() {
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess.destroyForcibly();
            while (currentProcess.isAlive()) {
                Thread.yield();
            }
        }
    }

    public JvmOutput execute(String cmd, String runDir) {

        JvmOutput output = null;
        currentOutput = null;
        try {
            long start = System.currentTimeMillis();
            String[] cmds = cmd.split("\\s+");
            ProcessBuilder processBuilder = new ProcessBuilder(cmds);
//            if (DTPlatform.isLinux()) {
//                File nullFile = new File("/dev/null"); // for Linux/Unix
//                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(nullFile));
//            } else if (DTPlatform.isWin()) {
//                File nullFile = new File("NUL"); // for Windows
//                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(nullFile));
//            }
            if (runDir != null) {
                processBuilder.directory(new File(runDir));
            }
            currentProcess = processBuilder.start();
            output = ExecutorHelper.getJvmOutput(currentProcess);
            long end = System.currentTimeMillis();
            if (output == null) {
                output = new JvmOutput("JvmOutput-TIMEOUT");
                timeoutCount++;
            }
            output.setExecuteSecond((double) (end - start) / 1000);

        } catch (IOException e) {
            e.printStackTrace();
        }
        currentOutput = output;
        return output;
    }

    public boolean dtSingleClassInProj(ArrayList<JvmInfo> jvmCmds, ProjectInfo currentProject, String classname, HashMap<String, ArrayList<ConfigBean>> jtregConfig, GCType gcType, int classIterate, int optionIterate) {

        init();
        ArrayList<String> vmOptions = new ArrayList<>();
        String classPath = currentProject.getpClassPath();
        /**
         * 01 differential testing java application class
         */
//        if (classname.contains("ClassFileInstaller")) {
//            return;
//        }
//        ArrayList<HashMap<String, JvmOutput>> diffTest = null;
        HashMap<String, GCOutput> diffTest = null;
        if (currentProject.getApplicationClasses().contains(classname)) {

            System.out.println("Project-application: "
                    + currentProject.getProjectName()
                    + classname
                    + "...");
            ArrayList<ConfigBean> beans = jtregConfig.get(classname);
            if (beans == null) {
                beans = new ArrayList<>();
                ConfigBean bean = new ConfigBean();
                bean.setOptions("");
                bean.setParameters("");
                bean.setFile(classname);
                beans.add(bean);
            }
            for (ConfigBean bean : beans) {
                if (!classname.contains(bean.getFile())) {
                    continue;
                }
                vmOptions.clear();
                if (currentProject.getVmoptions() != null) {
                    vmOptions.addAll(currentProject.getVmoptions());
                }
                if (bean.getOptions() != null && bean.getOptions().length() > 0) {
                    Collections.addAll(vmOptions, bean.getOptions().split("\\s+"));
                }
                diffTest = dtSingleClass(jvmCmds, vmOptions, classPath, classname, currentProject.getSrcClassPath(), false, gcType, bean.getParameters().split("\\s+"));

//                diffTest = dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName(), classname, currentProject.getSrcClassPath(), false, bean.getParameters().split("\\s+"));
            }

        }
        /**
         * 02 differential testing junit test case
         */
        if (currentProject.getJunitClasses().contains(classname)) {

            System.out.println("Project-junit: "
                    + currentProject.getProjectName()
                    + classname
                    + "...");

//            diffTest = dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName(), classname, null, true, new String[]{});
            diffTest = dtSingleClass(jvmCmds, vmOptions, classPath, classname, null, true, gcType, new String[]{});

        }
        if (diffTest == null) {
            return false;
        }
        HashMap<String, JvmOutput> outputDiff = new HashMap<>();
        HashMap<String, GCLogStats> gcLogDiff = new HashMap<>();
        this.outputDiff = outputDiff;
        this.gcLogStatsHashMap = gcLogDiff;
        diffTest.forEach((jvmID, gcOutput) -> {
            outputDiff.put(jvmID, gcOutput.getJvmOutput());
            if (gcOutput.getGcLogStats() != null && gcOutput.getGcLogStats().getGcEventNGram() != null) {
                // to compare gc perf diff. if GCEventNGram != null.
                gcLogDiff.put(jvmID, gcOutput.getGcLogStats());
            }
        });
        //TODO coverage:remove diff testing
        DiffCore diff = JDKAnalyzer.getInstance().analysis(classname, outputDiff);

        disCard = disCard || JDKAnalyzer.getInstance().getDiscardFlag();
        rerun = rerun || JDKAnalyzer.getInstance().getNeedRerun();
        this.diffCore = diff;
        if (diff != null) {
            if (diff.getSTATE_ID() != 3) {
                diffFound = true;
                ExecutorHelper.logJvmOutput(currentProject.getProjectName(), classname, diff, outputDiff, String.join(" ", vmOptions), classIterate, optionIterate);
            } else {
                normalDiff = true;
            }

        }
        if (diff == null || diff.getSTATE_ID() == 3) {
            // output consistent
            // diff test gc performance
            GCDiffCore gcDiffCore = GCLogDT.getInstance().analyze(gcLogDiff);
            if (gcDiffCore != null) {
                perfDiffFound = true;
//                ExecutorHelper.logJvmOutput(currentProject.getProjectName(), classname, gcDiffCore, String.join(" ", vmOptions), classIterate, optionIterate);
            }
        }

        return normalDiff;
    }

    public HashMap<String, GCOutput> dtSingleClass(ArrayList<JvmInfo> jvms,
                                                   ArrayList<String> pOptions,
                                                   String classpath,
                                                   String className,
                                                   String runDir,
                                                   boolean isJunit,
                                                   GCType gcType,
                                                   String... args
    ) {
//        if (DTPlatform.isLinux()) {
//            className = className.replace("$", "\\$");
//        }
//        HashMap<String, JvmOutput> results = new HashMap<>();
        HashMap<String, GCOutput> results = new HashMap<>();
        JvmInfo jdk11_up = null;
        ArrayList<Integer> jvmVersion = new ArrayList<>();
        for (JvmInfo jvm : jvms) {
            int version = 0;
            int index = jvm.getVersion().length() - 1;
            for (; index >= 0; index--) {
                char c = jvm.getVersion().charAt(index);
                if (!Character.isDigit(c)) {
                    break;
                }
            }
            version = Integer.parseInt(jvm.getVersion().substring(index + 1));
            jvmVersion.add(version);

        }
        for (int i = 0; i < jvmVersion.size(); i++) {
            if (jvms.get(i).getJvmId().contains("11-28")) {
                // magic hotspot old version. which can not run GCLogParser
                continue;
            }
            if (jvmVersion.get(i) >= 11) {
                //TODO latest jdk11

                if (jvms.get(i).getJvmId().contains("images")) {
                    continue;
                }
                jdk11_up = jvms.get(i);
                break;
            }

        }

        for (int i = 0; i < jvms.size(); i++) {
            JvmInfo jvm = jvms.get(i);
            Integer version = jvmVersion.get(i);
            if (DTConfiguration.FIND_UNIQUE_BUG && version != DTConfiguration.UNIQUE_JDK_VERSION) {
                continue;
            }
            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getFolderName();
            if (!GCHelper.supportGC(jvm).contains(gcType)) {
                // skip jvms that not support this GC
                continue;
            }
//            List<GCType> gcTypeList = GCHelper.supportGC(jvm);
//            TODO coverage: remove gcType
//            gcTypeList = gcTypeList.subList(0,1);
//            for (GCType gcType : gcTypeList) {
//                String gcOption = GCHelper.gc2option(gcType);
//            TODO coverage: remove gcType
//                String gcOption = "";
            ArrayList<String> options = new ArrayList<>(pOptions);
//                options.add("-Dtest.jdk=" + new File(jvm.getRootPath() + DTPlatform.FILE_SEPARATOR + jvm.getFolderName()).getAbsolutePath());
//                options.add(gcOption);
            String fileName = jvm.getJvmName() + "_" + jvm.getVersion() + "_" + gcType.toString() + ".log";
            String filePath = DTGlobal.getGcLogPath() + DTPlatform.FILE_SEPARATOR + fileName;
            if (!jvm.getJvmName().toLowerCase().contains("j9")) {
                if (version < 11) {
                    options.add("-XX:+PrintGCDetails");
                    options.add("-Xloggc:" + filePath);

                } else {
                    options.add("-Xlog:gc*:file=" + filePath);
                }
            }
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd(), options, classpath, className, isJunit, args);
            System.out.println("runDir:" + runDir);
            System.out.println(cmdExecute);
            getInstance().execute(cmdExecute, runDir);
            GCLogStats logStats = null;
            JvmOutput output;
            if (currentOutput != null) {
                output = currentOutput;
            } else {
                output = new JvmOutput("JvmOutput-TIMEOUT");
            }
            String err = output.getStderr();
            if (err != null && err.length() > 0) {
                System.err.println(err);
            }
            System.out.println("exitValue=" + output.getExitValue());
            if (jdk11_up != null) {
                logStats = GCLogAnalyzer.analyze(jdk11_up, filePath);
            }
            if (DTConfiguration.EDGE_COV) {
                logStats = EdgeCovAnalyzer.analyze(DTGlobal.getShowmapTmpPath(), logStats);
            }
            results.put(jvmId, new GCOutput(output, logStats));
//            }
        }
        return results;
    }
}
