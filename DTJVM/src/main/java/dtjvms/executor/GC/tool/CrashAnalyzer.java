package dtjvms.executor.GC.tool;

import dtjvms.DTPlatform;
import dtjvms.executor.JvmOutput;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashAnalyzer {

    public static final String HOTSPOT_PREFIX = "hs_err_";
    public static final String HOTSPOT_SUFFIX = ".log";

    public static final String OPENJ9_PREFIX = "javacore";
    public static final String OPENJ9_SUFFIX = ".log";

    public static DedupInfo analyzeCrash(JvmOutput output, String rootDir) {
        String message = output.getOutput();
        Pattern hotspotPattern = Pattern.compile("hs_err_pid(\\d+)\\.log");
        Matcher hotspotMatcher = hotspotPattern.matcher(message);

        Pattern openj9Pattern = Pattern.compile("javacore\\.(\\d+\\.)+txt");
        Matcher openj9Matcher = openj9Pattern.matcher(message);
        // 如果找到匹配项，则提取文件名和数字后缀
        String fileName = "";
        if (hotspotMatcher.find()) {
            fileName = hotspotMatcher.group(0);
        } else if (openj9Matcher.find()) {
            fileName = openj9Matcher.group(0);
        } else {
            return null;
        }
        return analyzeCrash(rootDir + DTPlatform.FILE_SEPARATOR + fileName);
    }

    public static DedupInfo analyzeCrash(String filePath) {
        return analyzeCrash(new File(filePath));
    }

    public static DedupInfo analyzeCrash(File filePath) {
        String fileName = filePath.getName();
        if (fileName.startsWith(HOTSPOT_PREFIX) && fileName.endsWith(HOTSPOT_SUFFIX)) {
            return dedupHotSpotCrashLogs(filePath);
        } else if (fileName.startsWith(OPENJ9_PREFIX) && fileName.endsWith(OPENJ9_SUFFIX)) {
            return dedupOpenJ9CrashLogs(filePath);
        }
        return null;
    }

    private static DedupInfo dedupHotSpotCrashLogs(File file) {

        String ErrorRegex = "Internal Error \\((.*?)\\)";
        String FrameRegex = "^V\\s+\\[.+\\]\\s+.*$";
        Pattern ErrorPattern = Pattern.compile(ErrorRegex, Pattern.MULTILINE);
        Pattern FramePattern = Pattern.compile(FrameRegex, Pattern.MULTILINE);

        String fileTxt = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileTxt = fileTxt + line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matcher ematcher = ErrorPattern.matcher(fileTxt);
        Matcher fmatcher = FramePattern.matcher(fileTxt);
        HotspotCrashInfo hotspotCrashInfo = new HotspotCrashInfo(file.getAbsolutePath());
        if (ematcher.find()) {

            StringBuilder errorLine = new StringBuilder();
            ematcher.reset();
            while (ematcher.find()) {
                String match = ematcher.group();
                errorLine.append(match).append("\n");
            }
            if (!errorLine.toString().equals("")) {
//                        results.add(errorLine.toString());
                hotspotCrashInfo.setErrorMessage(errorLine.toString());
            }

        }
        if (fmatcher.find()) {
            StringBuilder nativeFrames = new StringBuilder();
            fmatcher.reset();
            while (fmatcher.find()) {
                String match = fmatcher.group();
                nativeFrames.append(match).append("\n");
            }
            if (!nativeFrames.toString().equals("")) {
//                        results.add(nativeFrames.toString());
                hotspotCrashInfo.setStackFrames(nativeFrames.toString());
            }
        }
        String s = "";
        if (hotspotCrashInfo.getErrorMessage() != null) {
            s = hotspotCrashInfo.getErrorMessage();
        } else if (hotspotCrashInfo.getStackFrames() != null) {
            s = hotspotCrashInfo.getStackFrames();
        } else {
            return null;
        }
        return new DedupInfo(hotspotCrashInfo.fileName, s);
    }

    private static DedupInfo dedupOpenJ9CrashLogs(File file) {

        String assertionRegex = ".*ASSERTION FAILED.*";
        String VMFlagRegex = "^1XHFLAGS\\s+VM flags:\\d+"; // 正则表达式模式

        String OOMRegex = "Dump Event \"systhrow\" (00040000) Detail \"java/lang/OutOfMemoryError\" \"Java heap space\" received";
        String exceptModuleRegex = "1XHEXCPMODULE  Module: ";
        String currentThreadRegex = "1XMCURTHDINFO  Current thread";
        String nativeStackRegex = "3XMTHREADINFO3           Native callstack:";
        String stackFrameRegex = "4XENATIVESTACK";

        Pattern aseertionPattern = Pattern.compile(assertionRegex, Pattern.MULTILINE);
        Pattern flagPattern = Pattern.compile(VMFlagRegex, Pattern.MULTILINE);


        StringBuilder fileTxt = new StringBuilder();
        J9CrashInfo j9CrashInfo = new J9CrashInfo(file.getAbsolutePath());
        boolean startCurrentThread = false;
        boolean startStack = false;
        StringBuilder nativeStackLines = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(exceptModuleRegex)) {
                    j9CrashInfo.setExceptModule(line);
                }
                if (line.contains(currentThreadRegex)) {
                    startCurrentThread = true;
                }
                if (line.contains(nativeStackRegex)) {
                    startStack = true;
                    continue;
                }
                if (startStack && startCurrentThread) {
                    if (line.startsWith(stackFrameRegex)) {
                        String formatedLine = line.replaceAll("0x\\w+", "").replaceFirst("4XENATIVESTACK\\s+", "").trim();
                        if (!formatedLine.equals("")) {
                            nativeStackLines.append(formatedLine).append("\n");
                        }
                    } else {
                        startCurrentThread = false;
                        startStack = false;
                    }
                }
                fileTxt.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!nativeStackLines.toString().equals("")) {
            j9CrashInfo.setCurrentStackFrames(nativeStackLines.toString());
        }
        Matcher pmatcher = aseertionPattern.matcher(fileTxt.toString());
        Matcher fmatcher = flagPattern.matcher(fileTxt.toString());

        if (pmatcher.find()) {
            StringBuilder assertInfo = new StringBuilder();
            pmatcher.reset();
            while (pmatcher.find()) {
                String match = pmatcher.group();
                match = match.substring(match.indexOf("** ASSERTION FAILED **"));
                assertInfo.append(match).append("\n");
            }
            if (!assertInfo.toString().equals("")) {
                j9CrashInfo.setAssertion(assertInfo.toString());
//                        results.add(assertInfo);
            }
        }
        if (fmatcher.find()) {
            StringBuilder flagLine = new StringBuilder();
            fmatcher.reset();
            while (fmatcher.find()) {
                String match = fmatcher.group();
                flagLine.append(match).append("\n");
            }
            if (!flagLine.toString().equals("")) {
                j9CrashInfo.setVmFlag(flagLine.toString());
//                        results.add(flagLine);
            }
        }
        if (!fileTxt.toString().contains(OOMRegex)) {
            StringBuilder sb = new StringBuilder();
//                    sb.append(file.getName()).append("\t");
//                    sb.append(j9CrashInfo.getFileName()).append("\n");
            if (j9CrashInfo.getAssertion() != null) {
                sb.append(j9CrashInfo.getAssertion());
            } else if (j9CrashInfo.getCurrentStackFrames() != null) {
                sb.append(j9CrashInfo.getCurrentStackFrames());
            } else if (j9CrashInfo.getVmFlag() != null) {
                sb.append(j9CrashInfo.getVmFlag());
            } else if (j9CrashInfo.getExceptModule() != null) {
                sb.append(j9CrashInfo.getExceptModule()).append("\t");
            }
            return new DedupInfo(file.getAbsolutePath(), sb.toString());
        }

        return null;
    }

    public static class HotspotCrashInfo {
        private String errorMessage;
        private String stackFrames;
        private String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getStackFrames() {
            return stackFrames;
        }

        public void setStackFrames(String stackFrames) {
            this.stackFrames = stackFrames;
        }

        public HotspotCrashInfo(String fileName) {
            this.fileName = fileName;
        }

        public HotspotCrashInfo(String errorMessage, String stackFrames) {
            this.errorMessage = errorMessage;
            this.stackFrames = stackFrames;
        }
    }

    public static class J9CrashInfo {
        private String vmFlag;
        private String assertion;
        private String exceptModule;
        private String currentStackFrames;

        private String fileName;

        public String getVmFlag() {
            return vmFlag;
        }

        public void setVmFlag(String vmFlag) {
            this.vmFlag = vmFlag;
        }

        public String getAssertion() {
            return assertion;
        }

        public void setAssertion(String assertion) {
            this.assertion = assertion;
        }

        public String getExceptModule() {
            return exceptModule;
        }

        public void setExceptModule(String exceptModule) {
            this.exceptModule = exceptModule;
        }

        public String getCurrentStackFrames() {
            return currentStackFrames;
        }

        public void setCurrentStackFrames(String currentStackFrames) {
            this.currentStackFrames = currentStackFrames;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public J9CrashInfo(String fileName) {
            this.fileName = fileName;
        }

        public J9CrashInfo(String vmFlag, String assertion, String exceptModule, String currentStackFrames) {
            this.vmFlag = vmFlag;
            this.assertion = assertion;
            this.exceptModule = exceptModule;
            this.currentStackFrames = currentStackFrames;
        }
    }

    public static class DedupInfo {
        private String filePath;
        private String crashInfo;

        public DedupInfo(String filePath, String crashInfo) {
            this.filePath = filePath;
            this.crashInfo = crashInfo;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getCrashInfo() {
            return crashInfo;
        }

        public void setCrashInfo(String crashInfo) {
            this.crashInfo = crashInfo;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof DedupInfo) {
                return this.crashInfo.equals(((DedupInfo) obj).crashInfo);
            }
            return false;
        }

        @Override
        public String toString() {
            return filePath +
                    "\n" +
                    crashInfo +
                    "\n";
        }

        @Override
        public int hashCode() {
            return crashInfo.hashCode();
        }
    }
}
