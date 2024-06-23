package dtjvms.executor.GC.tool;

import com.google.gson.Gson;
import dtjvms.DTConfiguration;
import dtjvms.DTGlobal;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.executor.ExecutorHelper;
import dtjvms.executor.JvmOutput;
import org.junit.Test;

import java.io.File;

public class GCLogAnalyzer {
    public static final String LIB_DIR = System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "analyzer";

    public static GCLogStats analyze(JvmInfo jvm, String filePath) {
//        ProcessBuilder processBuilder = new ProcessBuilder(jvm.getJavaCmd(), "-p", LIB_DIR, "-m", "GCLogTool/org.example.Main ", filePath);

        try {
            StringBuilder cmd = new StringBuilder(jvm.getJavaCmd() + " -p " + LIB_DIR + " -m GCLogTool/org.example.Main -f " + filePath + " -n " + DTConfiguration.GC_EVENT_NGRAM);
            if (!DTGlobal.GC_DIMENSION) {
                cmd.append(" -d1 false");
            }
            if (!DTGlobal.EVENT_DIMENSION) {
                cmd.append(" -d2 false");
            }
            if (!DTGlobal.HEAP_DIMENSION) {
                cmd.append(" -d3 false");
            }
            Process process = Runtime.getRuntime().exec(cmd.toString(), null, new File(LIB_DIR));
            System.out.println("GCLogParser:");
            System.out.println(cmd);
            JvmOutput jvmOutput = ExecutorHelper.getJvmOutput(process);
            if (jvmOutput == null) {
                jvmOutput = new JvmOutput("");
            }
            System.out.println("result:" + jvmOutput.getStdout());
            System.err.println("error:" + jvmOutput.getStderr());
            Gson gson = new Gson();
            return gson.fromJson(jvmOutput.getStdout(), GCLogStats.class);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }

    @Test
    public void test() {
        String s = "{\"pauseTime\":2.7165711999999993,\"runTime\":3.5757958,\"freeHeap\":10439053,\"gcEventCov\":21.0,\"avgBeforeHeap\":1040821.2954545454,\"avgAfterHeap\":822100.2272727273}\n";
        Gson gson = new Gson();
        GCLogStats gcLogStats = gson.fromJson(s, GCLogStats.class);
        System.out.println();
    }
}
