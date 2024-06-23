package dtjvms.executor.GC;


import dtjvms.executor.GC.tool.GCDiffCore;
import dtjvms.executor.GC.tool.GCLogStats;

import java.util.*;
import java.util.stream.Collectors;

public class GCLogDT {
    //TODO performance threshold
    private static final double EPS = 1e-5;
    private static final double THROUGHPUT_THRESHOLD = 1.2 - EPS;
    private static final double PAUSE_THRESHOLD = 1.2 - EPS;
    private static final double FREE_HEAP_THRESHOLD = 1.2 - EPS;
    private static final double BEFORE_HEAP_THRESHOLD = 1.2 - EPS;
    private static final double AFTER_HEAP_THRESHOLD = 1.2 - EPS;
    private static final double DIFF_THRESHOLD = 2 - EPS;

    private static final String[] COL_HEADERS = {"throughput", "pause", "total", "free heap", "avg heap bf", "avg heap af"};

    private static final class AopHolder {
        static final GCLogDT aop = new GCLogDT();
    }

    private GCLogDT() {
    }

    public static GCLogDT getInstance() {
        return GCLogDT.AopHolder.aop;
    }

    public GCDiffCore analyze(HashMap<String, GCLogStats> gcLogDiff) {
        HashMap<String, GCLogStats> filteredGCLogDiff = new HashMap<>();
        for (Map.Entry<String, GCLogStats> stringGCLogStatsEntry : gcLogDiff.entrySet()) {
            GCLogStats gcLogStats = stringGCLogStatsEntry.getValue();
            if (gcLogStats.getThroughput() > EPS && gcLogStats.getRunTime() > EPS && gcLogStats.getPauseTime() > EPS) {
                filteredGCLogDiff.put(stringGCLogStatsEntry.getKey(), stringGCLogStatsEntry.getValue());
            }
        }
        gcLogDiff = filteredGCLogDiff;

        List<String> keys = new ArrayList<>(gcLogDiff.keySet());
        GCDiffCore diffCore = null;
        if (keys.size() <= 1) {
            return null;
        }
        boolean throughputDiffFound = false, pauseDiffFound = false, afHeapDiffFound = false, bfHeapDiffFound = false;
        keys = keys.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (int i = 0; i < keys.size(); i++) {
            GCLogStats logStats1 = gcLogDiff.get(keys.get(i));
            for (int j = 0; j < keys.size(); j++) {
                if (i == j) {
                    continue;
                }
                GCLogStats logStats2 = gcLogDiff.get(keys.get(j));
                double throughputFrac = logStats2.getThroughput() / logStats1.getThroughput();
                double pauseFrac = logStats1.getPauseTime() / logStats2.getPauseTime();
                double afHeapFrac = logStats1.getAvgAfterHeap() / logStats2.getAvgAfterHeap();
                double bfHeapFrac = logStats1.getAvgBeforeHeap() / logStats2.getAvgBeforeHeap();

                double throughputDiff = logStats2.getThroughput() - logStats1.getThroughput();
                double pauseDiff = logStats1.getPauseTime() - logStats2.getPauseTime();
                double afHeapDiff = logStats1.getAvgAfterHeap() - logStats2.getAvgAfterHeap();
                double bfHeapDiff = logStats1.getAvgBeforeHeap() - logStats2.getAvgBeforeHeap();
//                double heapDiff = logStats1.getFreeHeap() * 1.0 / logStats2.getFreeHeap();
                throughputDiffFound |= throughputFrac > THROUGHPUT_THRESHOLD && throughputDiff > DIFF_THRESHOLD;
                pauseDiffFound |= pauseFrac > PAUSE_THRESHOLD && pauseDiff > DIFF_THRESHOLD;
//                heapDiffFound |= heapDiff > FREE_HEAP_THRESHOLD;
                afHeapDiffFound |= afHeapFrac > AFTER_HEAP_THRESHOLD && afHeapDiff > DIFF_THRESHOLD;
                bfHeapDiffFound |= bfHeapFrac > BEFORE_HEAP_THRESHOLD && bfHeapDiff > DIFF_THRESHOLD;


            }
        }
        if (throughputDiffFound || pauseDiffFound || afHeapDiffFound || bfHeapDiffFound) {
            StringBuilder stringBuilder = new StringBuilder();
            if (throughputDiffFound) {
                stringBuilder.append("throughput diff found\n");
            }
            if (pauseDiffFound) {
                stringBuilder.append("pause diff found\n");
            }
            if (bfHeapDiffFound) {
                stringBuilder.append("heap before diff found\n");
            }
            if (afHeapDiffFound) {
                stringBuilder.append("heap after diff found\n");
            }

//            if (heapDiffFound) {
//                stringBuilder.append("free heap diff found\n");
//            }
            diffCore = new GCDiffCore(stringBuilder.toString());
        }
        if (diffCore != null) {
            String[] rowHeaders = keys.toArray(new String[0]);
            String[][] tableData = new String[rowHeaders.length][COL_HEADERS.length];
            for (int i = 0; i < rowHeaders.length; i++) {
                tableData[i][0] = String.format("%.2f", gcLogDiff.get(rowHeaders[i]).getThroughput() * 100) + " %";
                tableData[i][1] = String.format("%.2f", gcLogDiff.get(rowHeaders[i]).getPauseTime()) + " s";
                tableData[i][2] = String.format("%.2f", gcLogDiff.get(rowHeaders[i]).getRunTime()) + " s";

                long heap = gcLogDiff.get(rowHeaders[i]).getFreeHeap();
                String unit = "KB";
                if (heap % 1024 == 0) {
                    heap /= 1024;
                    unit = "MB";
                }
                if (heap % 1024 == 0) {
                    heap /= 1024;
                    unit = "GB";
                }

                tableData[i][3] = String.format("%d", heap) + " " + unit;
                double heapBF = gcLogDiff.get(rowHeaders[i]).getAvgBeforeHeap();
                unit = "KB";
                if (heapBF / 1024 > 1) {
                    heapBF /= 1024;
                    unit = "MB";
                }
                if (heapBF / 1024 > 1) {
                    heapBF /= 1024;
                    unit = "GB";
                }
                tableData[i][4] = String.format("%.2f", heapBF) + " " + unit;

                double heapAF = gcLogDiff.get(rowHeaders[i]).getAvgAfterHeap();
                unit = "KB";
                if (heapAF / 1024 > 1) {
                    heapAF /= 1024;
                    unit = "MB";
                }
                if (heapAF / 1024 > 1) {
                    heapAF /= 1024;
                    unit = "GB";
                }
                tableData[i][5] = String.format("%.2f", heapAF) + " " + unit;
            }
            diffCore.setDetailedMessage(getTableAsString(rowHeaders, COL_HEADERS, tableData));
        }
        return diffCore;
    }

    private static String getTableAsString(String[] rowHeaders, String[] colHeaders, String[][] tableData) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%45s", ""));
        for (String colHeader : colHeaders) {
            sb.append(String.format("%13s", colHeader));
        }
        sb.append("\n");

        for (int i = 0; i < rowHeaders.length; i++) {
            sb.append(String.format("%45s", rowHeaders[i]));
            for (int j = 0; j < colHeaders.length; j++) {
                sb.append(String.format("%13s", tableData[i][j]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
