package dtjvms.executor.GC.tool;

public class GCDiffCore {
    private String diffMessage;
    private String detailedMessage;

    public GCDiffCore(String diffMessage) {
        this.diffMessage = diffMessage;
    }

    public String getDiffMessage() {
        return diffMessage;
    }

    public void setDiffMessage(String diffMessage) {
        this.diffMessage = diffMessage;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }
}
