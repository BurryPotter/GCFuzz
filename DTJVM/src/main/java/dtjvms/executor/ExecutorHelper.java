package dtjvms.executor;

import dtjvms.DTConfiguration;
import dtjvms.DTGlobal;
import dtjvms.DTPlatform;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.tool.GCDiffCore;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ExecutorHelper {

    /**
     * Pumps stdout and stderr the running process into a String.
     *
     * @param process Process to pump.
     * @return Output from process.
     * @throws IOException If an I/O error occurs.
     */
//    public static JvmOutput getJvmOutput(Process process) throws IOException {
//
//        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
//        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
//        StreamPumper outPumper = new StreamPumper(process.getInputStream(), stdoutBuffer);
//        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), stderrBuffer);
//        Thread outPumperThread = new Thread(outPumper);
//        Thread errPumperThread = new Thread(errPumper);
//
//        outPumperThread.setDaemon(true);
//        errPumperThread.setDaemon(true);
//
//        outPumperThread.start();
//        errPumperThread.start();
//
//        try {
//            process.waitFor(15, TimeUnit.SECONDS);
//            outPumperThread.join();
//            errPumperThread.join();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return null;
//        }
//
//        return new JvmOutput(stdoutBuffer.toString(), stderrBuffer.toString(), process.exitValue());
//    }
//    public static JvmOutput getJvmOutput(Process process) throws IOException {
//
//        StringBuilder stdoutBuffer = new StringBuilder();
//        StringBuilder stderrBuffer = new StringBuilder();
//        boolean isTimeout = false;
//        int stdLineCount = 0, errLineCount = 0;
//        try {
//            InputStream inputStream = process.getInputStream();
//            InputStream errorStream = process.getErrorStream();
//
//            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
//            try {
//                String line = null;
//                while (process.isAlive()) {
//                    if (inputReader.ready() && (line = inputReader.readLine()) != null) {
//                        if (stdLineCount < DTConfiguration.OUTPUT_MAX_LINES) {
//                            stdoutBuffer.append(line).append("\n");
//                            stdLineCount++;
//                        }
//                    }
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//
//                try {
//                    inputStream.close();
//                    inputReader.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
//            try {
//                String line = null;
//                while (process.isAlive()) {
//                    if (errorReader.ready() && (line = errorReader.readLine()) != null) {
//                        if (errLineCount < DTConfiguration.OUTPUT_MAX_LINES) {
//                            stderrBuffer.append(line).append("\n");
//                            errLineCount++;
//                        }
//                    }
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    errorStream.close();
//                    errorReader.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            isTimeout = !process.waitFor(DTConfiguration.CLASS_MAX_RUNTIME, TimeUnit.SECONDS);
//            process.destroy();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (isTimeout) {
//            return new JvmOutput("JvmOutput-TIMEOUT");
//        } else {
//            return new JvmOutput(stdoutBuffer.toString(), stderrBuffer.toString(), process.exitValue());
//
//        }
//    }
    public static JvmOutput getJvmOutput(Process process) throws IOException {

        StringBuilder stdBuilder = new StringBuilder();
        StringBuilder errBuilder = new StringBuilder();
        Thread stdThread = null;
        Thread errThread = null;
        boolean isTimeout = true;
        final int[] lineCount = {0,0};
        try {
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            stdThread = new Thread(() -> {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String line = null;
                    while (!Thread.interrupted() && process.isAlive()) {
                        if (inputReader.ready() && (line = inputReader.readLine()) != null) {
                            if (lineCount[0] < DTConfiguration.OUTPUT_MAX_LINES) {
                                stdBuilder.append(line).append("\n");
                                lineCount[0]++;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });

            errThread = new Thread(() -> {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                try {
                    String line = null;
                    while (!Thread.interrupted() && process.isAlive()) {
                        if (errorReader.ready() && (line = errorReader.readLine()) != null) {
                            if (lineCount[1] < DTConfiguration.OUTPUT_MAX_LINES) {
                                errBuilder.append(line).append("\n");
                                lineCount[1]++;
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        errorStream.close();
                        errorReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            stdThread.start();
            errThread.start();
            isTimeout = !process.waitFor(DTConfiguration.CLASS_MAX_RUNTIME, TimeUnit.SECONDS);
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stdThread != null && stdThread.isAlive()) {
                stdThread.interrupt();
            }
            if (errThread != null && errThread.isAlive()) {
                errThread.interrupt();
            }
        }
        if (isTimeout) {
            return null;
        } else {
            return new JvmOutput(stdBuilder.toString(), errBuilder.toString(), process.exitValue());
        }
    }

    /**
     * assemble the given param to a runnable java cmd
     *
     * @param javaCmd   path to java
     * @param vmoptions jvm vm options
     * @param classpath classpath (jar + jvm classpath + project path)
     * @param classname class name
     * @param isJunit   if current class is a junit class, use junit cmd
     * @param args      ars
     * @return
     */
    public static String assembleJavaCmd(String javaCmd, ArrayList<String> vmoptions, String classpath, String classname, boolean isJunit, String... args) {

        String argString = "";
        String optString = "";
        if (args.length > 0) {
            argString = StringUtils.join(Arrays.asList(args), " ");
        }
        if (vmoptions != null && vmoptions.size() > 0) {
            optString = StringUtils.join(vmoptions, " ");
        }
        String timeout = "";
        //timeout command
        if (DTPlatform.isMac()) {
            timeout = "gtimeout " + DTConfiguration.CLASS_MAX_RUNTIME + " ";
        } else if (DTPlatform.isLinux()) {
            timeout = "timeout " + DTConfiguration.CLASS_MAX_RUNTIME + " ";
        } else {
            //windows
        }

        if (isJunit) {
            return timeout + DTPlatform.JUNIT_CMD.replace("JAVACMD", javaCmd)
                    .replace("VMOPTIONS", optString)
                    .replace("CLASSPATH", classpath)
                    .replace("CLASSNAME", classname)
                    .replace("ARGS", argString);
        } else {
            return timeout + DTPlatform.APPLICATION_CMD.replace("JAVACMD", javaCmd)
                    .replace("VMOPTIONS", optString)
                    .replace("CLASSPATH", classpath)
                    .replace("CLASSNAME", classname)
                    .replace("ARGS", argString);
        }
    }

    public static void logJvmOutput(Logger logger, String projName, String className, DiffCore diff, HashMap<String, JvmOutput> results) {

        if (logger != null) {

            logger.info("Difference found: project-" + projName + "-class-" + className);
            logger.info(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                logger.info(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                logger.info(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                logger.info(String.valueOf(results.get(s)));

            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            System.err.println(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                System.err.println(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                System.err.println(results.get(s));
                System.err.println("execute seconds: " + results.get(s).getExecuteSecond() + "s");
            }
        }
    }

    public static void logData(Logger logger, String projName, HashMap<String, Integer> data) {

        if (logger != null) {

            logger.info("Difference found: project-" + projName);
            for (String s : data.keySet()) {

                Integer time = data.get(s);
                logger.info(s + " " + time);
            }
        } else {

            System.err.println("Difference found: project-" + projName);
            for (String s : data.keySet()) {
                Integer time = data.get(s);
                System.out.println(s + " " + time);
            }
        }
    }


    public static void logJvmOutput(String projName, String className, DiffCore diff, HashMap<String, JvmOutput> results) {

        if (DTGlobal.getDiffLogger() != null) {

            DTGlobal.getDiffLogger().info("Difference found: project-" + projName + "-class-" + className);
            DTGlobal.getDiffLogger().info(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                DTGlobal.getDiffLogger().info(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                DTGlobal.getDiffLogger().info(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                DTGlobal.getDiffLogger().info(String.valueOf(results.get(s)));
            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            System.err.println(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                System.err.println(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                System.err.println(results.get(s));
            }
        }
    }

    public static void logJvmOutput(String projName, String className, DiffCore diff, HashMap<String, JvmOutput> results, String vmoption, int classIterate, int optionIterate) {

        if (DTGlobal.getDiffLogger() != null) {

            DTGlobal.getDiffLogger().info("Difference found: project-" + projName + "-class-" + className + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate));
            DTGlobal.getDiffLogger().info("VM options: " + vmoption);
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : results.keySet()) {
                stringBuilder.append(s).append(":").append(results.get(s).getFEEInfo()).append(";");
            }
            DTGlobal.getDiffLogger().info(stringBuilder.toString());
//            DTGlobal.getDiffLogger().info(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                DTGlobal.getDiffLogger().info(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                DTGlobal.getDiffLogger().info(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                DTGlobal.getDiffLogger().info(String.valueOf(results.get(s)));
                DTGlobal.getDiffLogger().info("execute seconds: " + results.get(s).getExecuteSecond() + "s");

            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate));
            System.err.println("VM options: " + vmoption);
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : results.keySet()) {
                stringBuilder.append(s).append(":").append(results.get(s).getFEEInfo()).append(";");
            }
            System.err.println(stringBuilder);
//            System.err.println(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                System.err.println(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                System.err.println(results.get(s));
                System.err.println("execute seconds: " + results.get(s).getExecuteSecond() + "s");

            }
        }
    }

    public static void logJvmOutput(String projName, String className, GCDiffCore diff, String vmoption, int classIterate, int optionIterate) {

        if (DTGlobal.getPerformLogger() != null) {

            DTGlobal.getPerformLogger().info("Difference found: project-" + projName + "-class-" + className + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate));
            DTGlobal.getPerformLogger().info("VM options: " + vmoption);
            DTGlobal.getPerformLogger().info(diff.getDiffMessage());

            DTGlobal.getPerformLogger().info(diff.getDetailedMessage());

        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className + String.format(" <CR:% 5d,OR:% 5d> ", classIterate, optionIterate));
            System.err.println("VM options: " + vmoption);
            System.err.println(diff.getDiffMessage());
            System.err.println(diff.getDetailedMessage());

        }
    }

    public static void logJvmOutput(String projName, String className, HashMap<String, JvmOutput> results) {

        if (DTGlobal.getDiffLogger() != null) {

            DTGlobal.getDiffLogger().info("Difference found: project-" + projName + "-class-" + className);
            for (String s : results.keySet()) {

                DTGlobal.getDiffLogger().info(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                DTGlobal.getDiffLogger().info(String.valueOf(results.get(s)));
            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                System.err.println(results.get(s));
            }
        }
    }

    /**
     * log different jvm output to local file
     *
     * @param projName  current project name
     * @param className current class name
     * @param results   all jvm output
     */
    public static void logOutput(String projName, String className, HashMap<String, ArrayList<String>> results) {

        if (DTGlobal.getDiffLogger() != null) {

            DTGlobal.getDiffLogger().info("Difference found: project-" + projName + "-class-" + className);
            for (String s : results.keySet()) {

                DTGlobal.getDiffLogger().info(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                DTGlobal.getDiffLogger().info(String.valueOf(results.get(s)));
            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                System.err.println(results.get(s));
            }
        }
    }

    /**
     * log different jvm output and the options used in this run to local file
     *
     * @param projName  current project name
     * @param className current class name
     * @param options   vm options used
     * @param commands  full java command
     * @param results   all jvm output
     */
    public static void logWithOptions(String projName,
                                      String className,
                                      HashMap<String, ArrayList<String>> options,
                                      HashMap<String, String> commands,
                                      HashMap<String, ArrayList<String>> results) {

        if (DTGlobal.getDiffLogger() != null) {

            DTGlobal.getDiffLogger().info("Difference found: project-" + projName + "-class-" + className);
            for (String s : results.keySet()) {

                DTGlobal.getDiffLogger().info(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));

                //options
                List<String> opts = options.get(s);
                DTGlobal.getDiffLogger().info(String.valueOf(opts));
                //commands
                String cmd = commands.get(s);
                DTGlobal.getDiffLogger().info(cmd);
                //results
                List<String> less = results.get(s);
                DTGlobal.getDiffLogger().info(String.valueOf(less));
            }
            DTGlobal.getDiffLogger().info("\n");
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50, "=")) +
                        s + String.join("", Collections.nCopies(50, "=")));
                System.err.println(results.get(s));
            }
        }
    }
}
