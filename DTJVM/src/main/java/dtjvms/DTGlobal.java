package dtjvms;

import util.LoggerUtils;

import java.util.logging.Logger;


public class DTGlobal {

    public static boolean useVMOptions = true;
    private static long startTime;
    private static Logger logger;
    private static Logger diffLogger;
    private static Logger diffUniqueLogger;
    private static Logger rewardLogger;
    private static Logger dataLogger;
    private static Logger recordLogger;

    private static Logger timeLogger;
    private static Logger performLogger;
    private static Logger classPriorityLogger;
    private static Logger optionPriorityLogger;
    private static Logger randomLogger;

    private static String gcLogPath;
    private static String showmapTmpPath;

    public static boolean GC_DIMENSION = true;
    public static boolean EVENT_DIMENSION = true;
    public static boolean HEAP_DIMENSION = true;
    public static void setLogger(String timeStamp) {

        if (logger == null) {
            logger = LoggerUtils.getInstance(timeStamp);
        }
    }

    public static void setDiffLogger(String timeStamp, String filename) {

        if (diffLogger == null) {
            diffLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static void setDiffUniqueLogger(String timeStamp, String filename) {

        if (diffUniqueLogger == null) {
            diffUniqueLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static void setDataLogger(String timeStamp, String filename) {

        if (dataLogger == null) {
            dataLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static void setTimeLogger(String timeStamp, String filename) {

        if (timeLogger == null) {
            timeLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static void setRewardLogger(String timeStamp, String filename) {
        if (rewardLogger == null) {
            rewardLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static void setPerformLogger(String timeStamp, String filename) {

        if (performLogger == null) {
            performLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }
    public static void setClassPriorityLogger(String timeStamp, String filename) {

        if (classPriorityLogger == null) {
            classPriorityLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }
    public static void setOptionPriorityLogger(String timeStamp, String filename) {

        if (optionPriorityLogger == null) {
            optionPriorityLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }
    public static void setRandomLogger(String timeStamp, String filename) {

        if (randomLogger == null) {
            randomLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }
    public static void setRecordLogger(String timeStamp, String filename) {

        if (recordLogger == null) {
            recordLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }
    public static void setGcLogPath(String gcLogPath) {
        DTGlobal.gcLogPath = gcLogPath;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Logger getDiffLogger() {
        return diffLogger;
    }

    public static Logger getDiffUniqueLogger() {
        return dataLogger;
    }

    public static Logger getDataLogger() {
        return dataLogger;
    }
    public static Logger getRecordLogger() {
        return recordLogger;
    }

    public static Logger getTimeLogger() {
        return timeLogger;
    }

    public static Logger getRewardLogger() {
        return rewardLogger;
    }

    public static Logger getPerformLogger() {
        return performLogger;
    }
    public static Logger getClassPriorityLogger() {
        return classPriorityLogger;
    }

    public static Logger getOptionPriorityLogger() {
        return optionPriorityLogger;
    }
    public static Logger getRandomLogger() {
        return randomLogger;
    }
    public static String getGcLogPath() {
        return gcLogPath;
    }

    public static String getShowmapTmpPath() {
        return showmapTmpPath;
    }

    public static void setShowmapTmpPath(String showmapTmpPath) {
        DTGlobal.showmapTmpPath = showmapTmpPath;
    }
}
