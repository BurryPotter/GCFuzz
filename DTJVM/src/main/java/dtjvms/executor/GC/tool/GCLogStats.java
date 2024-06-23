package dtjvms.executor.GC.tool;


import java.util.Set;
public class GCLogStats {
    public double pauseTime;
    public double runTime;
    public Double throughput = null;
    public long freeHeap; //unit KB
    public double gcEventCov;
    public Set<String> gcEventNGram;

    public double avgBeforeHeap;
    public double avgAfterHeap;

    public Set<String> edgeCov;

    public Set<String> getEdgeCov() {
        return edgeCov;
    }

    public void setEdgeCov(Set<String> edgeCov) {
        this.edgeCov = edgeCov;
    }

    public Set<String> getGcEventNGram() {
        return gcEventNGram;
    }

    public void setGcEventNGram(Set<String> gcEventNGram) {
        this.gcEventNGram = gcEventNGram;
    }

    public double getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(double pauseTime) {
        this.pauseTime = pauseTime;
    }

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

    public double getThroughput() {
        if (throughput == null) {
            throughput = (runTime - pauseTime) / runTime;
        }
        return throughput;
    }

    public long getFreeHeap() {
        return freeHeap;
    }

    public void setFreeHeap(long freeHeap) {
        this.freeHeap = freeHeap;
    }

    public double getGcEventCov() {
        return gcEventCov;
    }

    public void setGcEventCov(double gcEventCov) {
        this.gcEventCov = gcEventCov;
    }

    public double getAvgBeforeHeap() {
        return avgBeforeHeap;
    }

    public void setAvgBeforeHeap(double avgBeforeHeap) {
        this.avgBeforeHeap = avgBeforeHeap;
    }

    public double getAvgAfterHeap() {
        return avgAfterHeap;
    }

    public void setAvgAfterHeap(double avgAfterHeap) {
        this.avgAfterHeap = avgAfterHeap;
    }
}
