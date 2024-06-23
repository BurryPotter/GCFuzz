package dtjvms.analyzer;

import dtjvms.executor.JvmOutput;

import java.util.*;
import java.util.stream.Collectors;

public class JDKAnalyzer extends Analyzer {

    public static JDKAnalyzer jdkAnalyzer;

    public static int RESULT_LEVEL1 = -1;
    public static int RESULT_LEVEL2 = 0;
    public static int RESULT_LEVEL3 = 1;

    public boolean discardFlag = false;
    public boolean needRerun = false;
    public int resultState = 0;

    public static JDKAnalyzer getInstance() {

        if (jdkAnalyzer == null) {
            jdkAnalyzer = new JDKAnalyzer();
        }
        return jdkAnalyzer;
    }

    /**
     * Return value state:
     * -1  Exception and consistent
     * 0  Normal execute without exception
     * 1  Difference found (both exception and normal execute)
     *
     * @param results
     * @return
     */
    @Override
    public DiffCore analysis(String className, HashMap<String, JvmOutput> results) {
        System.out.println("analysis results");
        for (String s : results.keySet()) {
            System.out.println("key=" + s);
            System.out.println("output:");
            System.out.println(results.get(s));
            System.out.println("error: ");
            results.get(s).getErrors().forEach(System.out::println);
            System.out.println("exception: ");
            results.get(s).getExceptions().forEach(System.out::println);
            System.out.println("failures: ");
            results.get(s).getFailures().forEach(System.out::println);

        }
        FilterChain filterChain = new FilterChain();

        filterChain.addFilter(new WarningFilter());
        filterChain.addFilter(new VerifyGCFilter());
        filterChain.addFilter(new JunitFilter());
        filterChain.addFilter(new StdErrFilter());
        filterChain.addFilter(new DefErrFilter());
//        filterChain.addFilter(new GCRelateErrFilter());

        filterChain.startFilter(results);

        System.out.println("filtered results");
        for (String s : results.keySet()) {
            System.out.println("key=" + s);
            System.out.println("output:");
            System.out.println(results.get(s));
            System.out.println("error: ");
            results.get(s).getErrors().forEach(System.out::println);
            System.out.println("exception: ");
            results.get(s).getExceptions().forEach(System.out::println);
            System.out.println("failures: ");
            results.get(s).getFailures().forEach(System.out::println);
        }
        DiffCore diffCore = checkIfOutputConsistentWithDiffCore(results);
        if (diffCore != null) {
            diffCore.setResults(results);
        }
        return diffCore;
    }

    public boolean getDiscardFlag() {
        return discardFlag;
    }

    public boolean getNeedRerun() {
        return needRerun;
    }

    public int getResultState() {
        return resultState;
    }

    public static boolean exitValueNormal(HashMap<String, JvmOutput> results) {

        int normalExecCount = 0;
//        int exitValue = 0;
//        for (JvmOutput value : results.values()) {
//            exitValue = value.getExitValue();
//            break;
//        }
//        for (JvmOutput value : results.values()) {
//            if (value.getExitValue() == exitValue) {
//                normalExecCount++;
//            }
//        }
        for (Map.Entry<String, JvmOutput> result : results.entrySet()) {
            if (result.getValue().getExitValue() == 0) {
                normalExecCount++;
            }
        }
        if (normalExecCount < results.keySet().size()) {
            return false;
        } else if (normalExecCount == results.keySet().size()) {
            return true;
        } else {
            throw new RuntimeException("This should not happen: normalExecCount (" +
                    normalExecCount +
                    ") is greater than results size (" +
                    results.keySet().size() +
                    ")");
        }
    }

    public DiffCore checkIfOutputConsistentWithDiffCore(HashMap<String, JvmOutput> results) {

        discardFlag = false;
        DiffCore diffCore = null;
        needRerun = false;
        if (!exitValueNormal(results)) {
            discardFlag = true;
        }
        //Error, Exception, Failure
        List<String> keys = new ArrayList<>(results.keySet());
        // filter VMInitError
        keys = keys.stream()
                .filter(key -> {
//                    boolean timeout = results.get(key).getStdout().contains("JvmOutput-TIMEOUT");
                    boolean illegalAccess = results.get(key).getErrors().contains("IllegalAccessError");
                    needRerun |= results.get(key).getExceptions().contains("VMInitError");
                    needRerun |= results.get(key).getErrors().contains("VerifyError");
                    return !results.get(key).getErrors().contains("FixedBug") && !illegalAccess;

                })
                .collect(Collectors.toList());
        for (String key : keys) {
            if (results.get(key).getErrors().contains("Segmentation error")) {
                diffCore = new DiffCore(4, discardFlag, "Segmentation Error");
                StringBuilder str = new StringBuilder();
                for (int k = 0; k < keys.size(); k++) {
                    str.append(keys.get(k)).append(":");
                    str.append(Arrays.toString(results.get(keys.get(k)).getErrors().toArray()));
                }
                diffCore.setDetailedMessage(str.toString());
                return diffCore;
            }
        }
        for (int i = 0; i < keys.size(); i++) {

            JvmOutput object1 = results.get(keys.get(i));
            for (int j = i + 1; j < keys.size(); j++) {

                JvmOutput object2 = results.get(keys.get(j));

                //01 Errors
                if (object1.getErrors().size() != object2.getErrors().size()) {

                    resultState = RESULT_LEVEL3;
                    diffCore = new DiffCore(0, discardFlag, "Error Size Inconsistent");
                    StringBuilder str = new StringBuilder();
                    for (int k = 0; k < keys.size(); k++) {
                        str.append(keys.get(k)).append(":");
                        str.append(Arrays.toString(results.get(keys.get(k)).getErrors().toArray()));
                    }
                    diffCore.setDetailedMessage(str.toString());
                    return diffCore;
                } else {

                    Set<String> set1 = new HashSet<>(object1.getErrors());
                    Set<String> set2 = new HashSet<>(object2.getErrors());
                    if (!isSetEqual(set1, set2)) {
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(0, discardFlag, "Error Inconsistent");
                        StringBuilder str = new StringBuilder();
                        for (int k = 0; k < keys.size(); k++) {
                            str.append(keys.get(k)).append(":");
                            str.append(Arrays.toString(results.get(keys.get(k)).getErrors().toArray()));

                        }
                        diffCore.setDetailedMessage(str.toString());
                        return diffCore;
                    }
                }

                //02 Exceptions
                if (object1.getExceptions().size() != object2.getExceptions().size()) {
                    resultState = RESULT_LEVEL3;
                    diffCore = new DiffCore(1, discardFlag, "Exception Inconsistent");
                    StringBuilder str = new StringBuilder();
                    for (int k = 0; k < keys.size(); k++) {
                        str.append(keys.get(k)).append(":");
                        str.append(Arrays.toString(results.get(keys.get(k)).getExceptions().toArray()));

                    }
                    diffCore.setDetailedMessage(str.toString());
                    return diffCore;
                } else {

                    Set<String> set1 = new HashSet<>(object1.getErrors());
                    Set<String> set2 = new HashSet<>(object2.getErrors());
                    if (!isSetEqual(set1, set2)) {
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(1, discardFlag, "Exception Inconsistent");
                        StringBuilder str = new StringBuilder();
                        for (int k = 0; k < keys.size(); k++) {
                            str.append(keys.get(k)).append(":");
                            str.append(Arrays.toString(results.get(keys.get(k)).getExceptions().toArray()));

                        }
                        diffCore.setDetailedMessage(str.toString());
                        return diffCore;
                    }
                }

                //03 Failures
                if (object1.getFailures().size() != object2.getFailures().size()) {
                    resultState = RESULT_LEVEL3;
                    diffCore = new DiffCore(2, discardFlag, "Failure Inconsistent");
                    StringBuilder str = new StringBuilder();
                    for (int k = 0; k < keys.size(); k++) {
                        str.append(keys.get(k)).append(":");
                        str.append(Arrays.toString(results.get(keys.get(k)).getFailures().toArray()));

                    }
                    diffCore.setDetailedMessage(str.toString());
                    return diffCore;
                } else {

                    Set<String> set1 = new HashSet<>(object1.getErrors());
                    Set<String> set2 = new HashSet<>(object2.getErrors());
                    if (!isSetEqual(set1, set2)) {
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(2, discardFlag, "Failure Inconsistent");
                        StringBuilder str = new StringBuilder();
                        for (int k = 0; k < keys.size(); k++) {
                            str.append(keys.get(k)).append(":");
                            str.append(Arrays.toString(results.get(keys.get(k)).getFailures().toArray()));

                        }
                        diffCore.setDetailedMessage(str.toString());
                        return diffCore;
                    }
                }
            }
        }
        resultState = RESULT_LEVEL1;


        //Normal
        for (int i = 0; i < keys.size(); i++) {

            JvmOutput object1 = results.get(keys.get(i));
            for (int j = i; j < keys.size(); j++) {

                JvmOutput object2 = results.get(keys.get(j));

                if (!object1.getStdout().equals(object2.getStdout())) {

                    resultState = RESULT_LEVEL3;
                    diffCore = new DiffCore(3, discardFlag, "Normal Output Inconsistent");
                    return diffCore;
                }
            }
        }
        resultState = RESULT_LEVEL2;
        return null;
    }

    private boolean isSetEqual(Set<String> set1, Set<String> set2) {
        if (set1.size() != set2.size()) {
            return false;
        }
        for (String item : set1) {
            if (!set2.contains(item)) {
                return false;
            }
        }
        return true;
    }
}
