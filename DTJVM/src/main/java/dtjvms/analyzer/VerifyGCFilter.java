package dtjvms.analyzer;

import dtjvms.executor.JvmOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VerifyGCFilter extends Filter {

    private static final List<String> FILTER_STRINGS = new ArrayList<>();
    private static final List<String> VERIFY_GC_ERROR = new ArrayList<>();

    static {
        FILTER_STRINGS.add("VerifyBeforeGC");
        FILTER_STRINGS.add("VerifyDuringGC");
        FILTER_STRINGS.add("VerifyAfterGC");
        FILTER_STRINGS.add("[Verifying ");

        VERIFY_GC_ERROR.add("Fail");
        VERIFY_GC_ERROR.add("fail");
        VERIFY_GC_ERROR.add("assert");
        VERIFY_GC_ERROR.add("Assert");
        VERIFY_GC_ERROR.add("Error");
        VERIFY_GC_ERROR.add("error");
    }

    @Override
    protected void doFilter(HashMap<String, JvmOutput> results) {
        System.out.println("start VerifyGCFilter "+ System.currentTimeMillis());

//        String filterOut = "";
        for (String key : results.keySet()) {

            JvmOutput jvmOut = results.get(key);
            String filterOut = "";
            if (!jvmOut.getStdout().isEmpty()) {

                List<String> outputs = jvmOut.stdoutAsLines();

                for (String output : outputs) {

                    if (output.length() != 0) {

                        boolean flag = false;
                        for (String filterString : FILTER_STRINGS) {

                            if (output.contains(filterString)) {
                                for (String errorString : VERIFY_GC_ERROR){
                                    if (output.contains(errorString)){
                                        break;
                                    }
                                }
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            filterOut = filterOut + output + "\n";
                        }
                    }
                }
                jvmOut.setStdout(filterOut);
            }
        }
    }
}