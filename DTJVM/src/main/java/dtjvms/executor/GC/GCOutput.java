package dtjvms.executor.GC;

import dtjvms.executor.GC.tool.CrashAnalyzer;
import dtjvms.executor.GC.tool.GCLogStats;
import dtjvms.executor.JvmOutput;

public class GCOutput {
    private JvmOutput jvmOutput;
    private GCLogStats gcLogStats;

    private CrashAnalyzer.DedupInfo dedupInfo;

    public GCOutput(JvmOutput jvmOutput, GCLogStats gcLogStats) {
        this.jvmOutput = jvmOutput;
        this.gcLogStats = gcLogStats;
    }

    public GCOutput(JvmOutput jvmOutput, GCLogStats gcLogStats, CrashAnalyzer.DedupInfo dedupInfo) {
        this.jvmOutput = jvmOutput;
        this.gcLogStats = gcLogStats;
        this.dedupInfo = dedupInfo;
    }

    public CrashAnalyzer.DedupInfo getDedupInfo() {
        return dedupInfo;
    }

    public void setDedupInfo(CrashAnalyzer.DedupInfo dedupInfo) {
        this.dedupInfo = dedupInfo;
    }

    public JvmOutput getJvmOutput() {
        return jvmOutput;
    }

    public void setJvmOutput(JvmOutput jvmOutput) {
        this.jvmOutput = jvmOutput;
    }

    public GCLogStats getGcLogStats() {
        return gcLogStats;
    }

    public void setGcLogStats(GCLogStats gcLogStats) {
        this.gcLogStats = gcLogStats;
    }
}
