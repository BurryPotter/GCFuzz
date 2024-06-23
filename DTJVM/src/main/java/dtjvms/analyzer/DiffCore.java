package dtjvms.analyzer;

import dtjvms.executor.GC.tool.CrashAnalyzer;
import dtjvms.executor.JvmOutput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DiffCore {

    private int STATE_ID;
    private boolean discard;
    private String diffMessage;
    private String detailedMessage;

    private Set<CrashAnalyzer.DedupInfo> crashDedup;

    private HashMap<String, JvmOutput> results;
    public DiffCore(String diffMessage) {
        this.diffMessage = diffMessage;
    }

    public DiffCore(int STATE_ID, boolean discard, String diffMessage) {
        this.STATE_ID = STATE_ID;
        this.discard = discard;
        this.diffMessage = diffMessage;
    }

    public HashMap<String, JvmOutput> getResults() {
        return results;
    }

    public void setResults(HashMap<String, JvmOutput> results) {
        this.results = results;
    }

    public boolean addCrashDedup(CrashAnalyzer.DedupInfo dedupInfo) {
        if (crashDedup == null) {
            crashDedup = new HashSet<>();
        }
        boolean success = false;
        if (dedupInfo != null) {
            success = crashDedup.add(dedupInfo);
        }
        return success;
    }

    public Set<CrashAnalyzer.DedupInfo> getCrashDedup() {
        return crashDedup;
    }

    public void setCrashDedup(Set<CrashAnalyzer.DedupInfo> crashDedup) {
        this.crashDedup = crashDedup;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }

    public int getSTATE_ID() {
        return STATE_ID;
    }

    public void setSTATE_ID(int STATE_ID) {
        this.STATE_ID = STATE_ID;
    }

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    public String getDiffMessage() {
        return diffMessage;
    }

    public void setDiffMessage(String diffMessage) {
        this.diffMessage = diffMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffCore myObject = (DiffCore) o;
        return STATE_ID == myObject.STATE_ID && Objects.equals(detailedMessage, myObject.detailedMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(STATE_ID, detailedMessage);
    }
}
