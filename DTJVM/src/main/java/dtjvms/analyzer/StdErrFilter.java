package dtjvms.analyzer;

import dtjvms.DTConfiguration;
import dtjvms.executor.JvmOutput;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StdErrFilter extends Filter {

    private static final String jvmwarningmsg = ".* VM warning:.*";
    private static final String COMMON_EXCEPTION_KEYWORD_PATTERN = "[\\s\\S]*?[^A-Za-z]([A-Za-z]+?Exception)[^A-Za-z][\\s\\S]*";
    private static final String COMMON_ERROR_KEYWORD_PATTERN = "[\\s\\S]*?[^A-Za-z]([A-Za-z]+?Error)[^A-Za-z][\\s\\S]*";
    private static final String COMMON_FAILURE_KEYWORD_PATTERN = "[\\s\\S]*?[^A-Za-z]([A-Za-z]+?Failure)[^A-Za-z][\\s\\S]*";

    private boolean containFixedBug(String key, String message) {
        if (DTConfiguration.FIND_UNIQUE_BUG) {
            return false;
        }
        if (message.contains("-Xgc:hierarchicalScanOrdering") || message.contains("-XX:G1RSetSparseRegionEntries=")
                || (message.contains("FileNotFoundException") && message.contains("NoSuchFileException") && message.contains("UnixException"))
                || (message.contains("NullPointerException") && message.contains("java.util.ArrayDeque"))
                || message.contains("g1StringDedupTable.cpp:566") || message.contains("g1StringDedupQueue.cpp:164")
                || message.contains("guarantee(!obj->is_forwarded()) failed") || message.contains("guarantee(!value->is_forwarded()) failed")
                || ((message.contains("Segmentation error") || message.contains("fatal error")) &&
                (message.contains("-XX:+ZeroTLAB") || message.contains("-XX:G1SATBBufferSize")))
        ) {
            return true;
        }
        if (key.toLowerCase().contains("jdk8") && key.toLowerCase().contains("g1") && message.contains("Segmentation error") && message.contains("-XX:G1")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilter(HashMap<String, JvmOutput> results) {
        System.out.println("start StdErrFilter " + System.currentTimeMillis());

        Pattern errorPattern = Pattern.compile(COMMON_ERROR_KEYWORD_PATTERN, Pattern.MULTILINE);
        Pattern exceptionPattern = Pattern.compile(COMMON_EXCEPTION_KEYWORD_PATTERN, Pattern.MULTILINE);
        Pattern failurePattern = Pattern.compile(COMMON_FAILURE_KEYWORD_PATTERN, Pattern.MULTILINE);

        for (String key : results.keySet()) {

            System.out.println("key = " + key);
            JvmOutput jvmOut = results.get(key);

            if (!jvmOut.getOutput().isEmpty()) {

                List<String> outputs = jvmOut.asLines();
                String errOutput = jvmOut.getStderr();
                String allOutput = String.join("\n", outputs);
                System.out.println("allOutput size = " + outputs.size());
//                System.out.println(allOutput);
                if (containFixedBug(key, allOutput)) {
                    jvmOut.getErrors().add("FixedBug");
                    continue;
                }
//                else if (allOutput.contains("Segmentation error") || allOutput.contains("core dump") ||
//                        allOutput.contains("javacore.") || allOutput.contains("ASSERTION FAILED") ||
//                        allOutput.contains("fatal error")) {
//                    jvmOut.getErrors().add("Segmentation error");
//                    continue;
//                }

                for (String output : outputs) {
                    if (output.contains("Segmentation error") || output.contains("core dump") ||
                            output.contains("javacore.") || output.contains("ASSERTION FAILED") ||
                            output.contains("fatal error")) {
                        jvmOut.getErrors().add("Segmentation error");
                        break;
                    }
                    if (output.contains("JvmOutput-TIMEOUT")) {
                        jvmOut.getErrors().add("JvmOutput-TIMEOUT");
                        break;
                    }
                    output = output + "\n";
                    Matcher errorMatcher = errorPattern.matcher(output);
                    if (errorMatcher.find()) {
                        if (!jvmOut.getErrors().contains(errorMatcher.group(1))) {
                            jvmOut.getErrors().add(errorMatcher.group(1));
                        }
                    }
                    Matcher exceptionMatcher = exceptionPattern.matcher(output);
                    if (exceptionMatcher.find()) {
                        if (!jvmOut.getExceptions().contains(exceptionMatcher.group(1))) {
                            jvmOut.getExceptions().add(exceptionMatcher.group(1));
                        }
                    }
                    if (output.contains("Could not find or load main class")) {
                        String cnfe = "ClassNotFoundException";
                        if (!jvmOut.getExceptions().contains(cnfe)) {
                            jvmOut.getExceptions().add(cnfe);
                        }
                    } else if (output.contains("Error: Could not create the Java Virtual Machine.") || output.contains("Error occurred during initialization of VM")) {
                        String cnfe = "VMInitError-S " + allOutput.replaceAll("\n", " ") + " VMInitError-E";
                        if (!jvmOut.getExceptions().contains(cnfe)) {
                            jvmOut.getExceptions().add(cnfe);
                        }
                    }
                    Matcher failureMatcher = failurePattern.matcher(output);
                    if (failureMatcher.find()) {
                        if (!jvmOut.getFailures().contains(failureMatcher.group(1))) {
                            jvmOut.getFailures().add(failureMatcher.group(1));
                        }
                    }
                }
            }
        }
    }
}
