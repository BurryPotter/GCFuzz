package reduce;

import dtjvms.DTConfiguration;
import dtjvms.DTGlobal;
import dtjvms.DTPlatform;
import dtjvms.analyzer.DiffCore;
import dtjvms.executor.GC.tool.GCDiffCore;
import dtjvms.executor.JvmOutput;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ReduceExecutorHelper {


    public static JvmOutput getJvmOutput(Process process) throws IOException {

        final String[] stdoutBuffer = {""};
        final String[] stderrBuffer = {""};
        Thread stdThread = null;
        Thread errThread = null;
        boolean isTimeout = true;
        final int[] lindCount = {0, 0};
        try {
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            stdThread = new Thread(() -> {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String line = null;
                    while (!Thread.interrupted() && process.isAlive()) {
                        if (inputReader.ready() && (line = inputReader.readLine()) != null) {
                            stdoutBuffer[0] = stdoutBuffer[0] + line + "\n";
                            lindCount[0]++;
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
                            stderrBuffer[0] = stderrBuffer[0] + line + "\n";
                            lindCount[1]++;

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
            return new JvmOutput(stdoutBuffer[0], stderrBuffer[0], process.exitValue());
        }
    }

    public static JvmOutput getJvmOutput(String cmd, String runDir) throws IOException {
        File workingDirectory = new File(runDir); // run dir
        File stdOutput = new File(runDir + DTPlatform.FILE_SEPARATOR + "stdOutput.out");
        File errOutput = new File(runDir + DTPlatform.FILE_SEPARATOR + "errOutput.out");
        String[] cmds = cmd.split("\\s+");
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectOutput(stdOutput);
        processBuilder.redirectError(errOutput);
        processBuilder.directory(workingDirectory);
        boolean isTimeout = false;
        int exitValue = -1;
        Process process = null;
        try {
            process = processBuilder.start();
            isTimeout = !process.waitFor(DTConfiguration.CLASS_MAX_RUNTIME, TimeUnit.SECONDS);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        String stdout = readFile(stdOutput);
        String errout = readFile(errOutput);
        if (process != null) {
            if (process.isAlive()) {
                try {
                    process.destroyForcibly().waitFor();
                    exitValue = process.exitValue();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                exitValue = process.exitValue();
            }
        }
        if (isTimeout) {
            return null;
        } else {
            return new JvmOutput(stdout, errout, exitValue);
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
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
            DTGlobal.getDiffLogger().info(diff.getDetailedMessage());
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
