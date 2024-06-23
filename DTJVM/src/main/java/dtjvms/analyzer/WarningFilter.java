package dtjvms.analyzer;

import dtjvms.executor.JvmOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WarningFilter extends Filter {

    private static final List<String> FILTER_STRINGS = new ArrayList<>();

    static {
        FILTER_STRINGS.add("Warning");
        FILTER_STRINGS.add("warning");

    }

    @Override
    protected void doFilter(HashMap<String, JvmOutput> results) {
        System.out.println("start WarningFilter "+ System.currentTimeMillis());
//        String filterOut = "";
        for (String key : results.keySet()) {

            JvmOutput jvmOut = results.get(key);
            String filterOut = "";
            String errOut = "";

            if (!jvmOut.getStdout().isEmpty()) {

                List<String> outputs = jvmOut.stdoutAsLines();

                boolean junitOutput = false;
                for (String output : outputs) {

                    if (output.length() != 0){

                        boolean flag = false;
                        for (String filterString : FILTER_STRINGS) {

                            if (output.contains(filterString)){

                                flag = true;
                                break;
                            }
                        }
                        if (!flag){
                            filterOut = filterOut + output + "\n";
                        }
                    }
                }
                jvmOut.setStdout(filterOut);
            }
            if (!jvmOut.getStderr().isEmpty()) {

                List<String> outputs = jvmOut.stderrAsLines();

                for (String output : outputs) {

                    if (output.length() != 0){

                        boolean flag = false;
                        for (String filterString : FILTER_STRINGS) {

                            if (output.contains(filterString)){

                                flag = true;
                                break;
                            }
                        }
                        if (!flag){
                            errOut = errOut + output + "\n";
                        }
                    }
                }
                jvmOut.setStderr(errOut);
            }
        }
    }
}
