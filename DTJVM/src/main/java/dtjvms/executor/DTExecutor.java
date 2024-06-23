package dtjvms.executor;

import dtjvms.*;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.fjvms.config.ConfigBean;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DTExecutor extends Executor {

    private JvmOutput currentOutput;
    private Process currentProcess;
    private boolean debug = false;

    public static DTExecutor dtExecutor;

    public static DTExecutor getInstance() {
        if (dtExecutor == null) {
            dtExecutor = new DTExecutor();
        }
        return dtExecutor;
    }

    @Override
    public JvmOutput execute(String cmd) {

        JvmOutput output = null;
        currentOutput = null;
        try {
            currentProcess = Runtime.getRuntime().exec(cmd);
            if (currentProcess.waitFor(DTConfiguration.CLASS_MAX_RUNTIME, TimeUnit.SECONDS)) {
                output = ExecutorHelper.getJvmOutput(currentProcess);
            } else {
                shutDown();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        currentOutput = output;
        return output;
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
            if (runDir != null) {
                currentProcess = Runtime.getRuntime().exec(cmd, null, new File(runDir));
            } else {
                currentProcess = Runtime.getRuntime().exec(cmd);
            }
            if (currentProcess.waitFor(DTConfiguration.CLASS_MAX_RUNTIME, TimeUnit.SECONDS)) {
                output = ExecutorHelper.getJvmOutput(currentProcess);
            } else {
                shutDown();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        currentOutput = output;
        return output;
    }

    public void dtSingleClassInProj(ArrayList<JvmInfo> jvmCmds, ProjectInfo currentProject, String classname) {

        ArrayList<String> vmOptions = currentProject.getVmoptions();
        String classPath = currentProject.getpClassPath();
        /**
         * 01 differential testing java application class
         */
        if (currentProject.getApplicationClasses().contains(classname)) {

            System.out.println("Project-application: "
                    + currentProject.getProjectName()
                    + classname
                    + "...");

            dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName(), classname, false, new String[]{});
        }
        /**
         * 02 differential testing junit test case
         */
        if (currentProject.getJunitClasses().contains(classname)) {

            System.out.println("Project-junit: "
                    + currentProject.getProjectName()
                    + classname
                    + "...");

            dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName(), classname, true, new String[]{});

        }
    }

    public void dtSingleClassInProj(ArrayList<JvmInfo> jvmCmds, ProjectInfo currentProject, String classname, HashMap<String, ArrayList<ConfigBean>> jtregConfig) {

        ArrayList<String> vmOptions = new ArrayList<>();
        String classPath = currentProject.getpClassPath();
        /**
         * 01 differential testing java application class
         */
//        if (classname.contains("ClassFileInstaller")) {
//            return;
//        }
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
                dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName(), classname, currentProject.getSrcClassPath(), false, bean.getParameters().split("\\s+"));

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


            dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName(), classname, null, true, new String[]{});


        }
    }

    public void dtSingleClass(ArrayList<JvmInfo> jvms,
                              ArrayList<String> pOptions,
                              String classpath,
                              String projName,
                              String className,
                              String runDir,
                              boolean isJunit,
                              String... args
    ) {

        HashMap<String, JvmOutput> results = new HashMap<>();

        for (JvmInfo jvm : jvms) {

            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getFolderName();
            ArrayList<String> options = new ArrayList<>(pOptions);
            options.add("-Dtest.jdk=" + new File(jvm.getRootPath() + DTPlatform.FILE_SEPARATOR + jvm.getFolderName()).getAbsolutePath());
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd(), options, classpath, className, isJunit, args);
            System.out.println("runDir:" + runDir);
            System.out.println(cmdExecute);

            getInstance().execute(cmdExecute, runDir);

//            Thread ctester = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                }
//            });
//            ctester.start();

            String err = currentOutput.getStderr();
            if (err != null && err.length() > 0) {
                System.err.println(err);
            }
            if (currentOutput != null) {

                results.put(jvmId, currentOutput);
            } else {
                results.put(jvmId, new JvmOutput("JvmOutput-TIMEOUT"));
            }
        }

        DiffCore diff = JDKAnalyzer.getInstance().analysis(className, results);

        if (diff != null) {

            ExecutorHelper.logJvmOutput(projName, className, diff, results);
        }
    }

    public void dtSingleClass(ArrayList<JvmInfo> jvms,
                              ArrayList<String> pOptions,
                              String classpath,
                              String projName,
                              String className,
                              boolean isJunit,
                              String... args) {

        HashMap<String, JvmOutput> results = new HashMap<>();

        for (JvmInfo jvm : jvms) {

            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getFolderName();
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd(), pOptions, classpath, className, isJunit, args);

            getInstance().execute(cmdExecute);

            if (currentOutput != null) {
                results.put(jvmId, currentOutput);
            } else {
                results.put(jvmId, new JvmOutput("JvmOutput-TIMEOUT"));
            }
            if (debug) {
                System.out.println(String.join("", Collections.nCopies(50, "=")) +
                        jvm.getJvmName() + "-" + jvm.getVersion() + String.join("", Collections.nCopies(50, "=")));
                System.out.println(currentOutput.getOutput());
            }
        }
        DiffCore diff = JDKAnalyzer.getInstance().analysis(className, results);
        if (diff != null) {
            ExecutorHelper.logJvmOutput(projName, className, diff, results);
        }
    }

    public void enableDebugMode() {
        debug = true;
    }

    public void disableDebugMode() {
        debug = false;
    }

}
