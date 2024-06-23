package dtjvms.executor.GC;


public class DiffResult {
    private boolean diffFound;
    private boolean perfDiffFound;

    private boolean disCard;
    private boolean rerun;

    private int timeoutCount = 0;

    public DiffResult(GCExecutor gcExecutor) {
        this(gcExecutor.isDiffFound(), gcExecutor.isPerfDiffFound(), gcExecutor.isDisCard(), gcExecutor.isRerun(), gcExecutor.getTimeoutCount());
    }

    public DiffResult(boolean diffFound, boolean perfDiffFound, boolean disCard, boolean rerun, int timeoutCount) {
        this.diffFound = diffFound;
        this.perfDiffFound = perfDiffFound;
        this.disCard = disCard;
        this.rerun = rerun;
        this.timeoutCount = timeoutCount;
    }

    public boolean isDiffFound() {
        return diffFound;
    }

    public void setDiffFound(boolean diffFound) {
        this.diffFound = diffFound;
    }

    public boolean isPerfDiffFound() {
        return perfDiffFound;
    }

    public void setPerfDiffFound(boolean perfDiffFound) {
        this.perfDiffFound = perfDiffFound;
    }

    public boolean isDisCard() {
        return disCard;
    }

    public void setDisCard(boolean disCard) {
        this.disCard = disCard;
    }

    public boolean isRerun() {
        return rerun;
    }

    public void setRerun(boolean rerun) {
        this.rerun = rerun;
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(int timeoutCount) {
        this.timeoutCount = timeoutCount;
    }
}
