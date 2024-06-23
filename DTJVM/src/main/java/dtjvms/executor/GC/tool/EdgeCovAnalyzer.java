package dtjvms.executor.GC.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class EdgeCovAnalyzer {
    public static GCLogStats analyze(String filePath, GCLogStats gcLogStats) {
//        ProcessBuilder processBuilder = new ProcessBuilder(jvm.getJavaCmd(), "-p", LIB_DIR, "-m", "GCLogTool/org.example.Main ", filePath);
        try {
            if (gcLogStats == null) {
                gcLogStats = new GCLogStats();
            }
            File showmapFile = new File(filePath);
            if (showmapFile.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(showmapFile));
                Set<String> edgeSet = new HashSet<>();
                bufferedReader.lines().forEach(line -> {
                    edgeSet.add(line.split(":")[0]);
                });
                gcLogStats.setEdgeCov(edgeSet);
            } else {
                throw new RuntimeException("File " + filePath + " not exists");
            }
            return gcLogStats;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }
}
