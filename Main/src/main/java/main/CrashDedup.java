package main;


import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashDedup {

    public static String hotspotCrashPrefix = "hs_err_";
    public static String openj9CrashPrefix = "javacore";
    public static final String[] NEW_JDK = new String[]{"11.0.20", "jdk17.0.8","21.0.2"};

    public static void main(String[] args) throws IOException {
        String folderPath = ""; // replace with the path contain crash dump files.

        File folder = new File(folderPath);
        File[] files = Objects.requireNonNull(folder.listFiles(pathname -> pathname.getName().contains("_5_21") && pathname.getName().contains("GCFuzz")));
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            File sootOutputDir = new File(file.getAbsolutePath(), "sootOutput");
            System.out.println("Reading files from: " + sootOutputDir.getAbsolutePath());
            if (!sootOutputDir.exists()) {
                System.out.println("not exists");
                continue;
            }
            ArrayList<ArrayList<DedupInfo>> arrayList = new ArrayList<>();
            filterFilesInFolder(arrayList, sootOutputDir);
            arrayList.forEach(System.out::println);
        }

    }

    public static ArrayList<ArrayList<DedupInfo>> filterFilesInFolder(ArrayList<ArrayList<DedupInfo>> arrayList, File folder) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                ArrayList<DedupInfo> arrayList1 = filterInFolder(file);
                if (!arrayList1.isEmpty()) {
                    arrayList.add(arrayList1);
                }
                filterFilesInFolder(arrayList, file);
            }
        }
        return arrayList;
    }

    private static ArrayList<DedupInfo> filterInFolder(File folder) {
//        System.out.println(folder.getAbsolutePath());
        int total = 0;
        Set<DedupInfo> hc = dedupHotSpotCrashLogs(folder);
        total += hc.size();

        Set<DedupInfo> j9 = dedupOpenJ9CrashLogs(folder);
        total += j9.size();
        if (total == 0) {
//            System.out.println("Total empty");
        } else {
//            System.out.println("Dedup hotspot crashes: ");
            if (hc.isEmpty()) {
//                System.out.println("Empty");
            } else {
//                System.out.println("Hotspot crashes statistics: ");
//                System.out.println("Size: " + hc.size());
//                hc.forEach(System.out::println);
            }
            if (j9.isEmpty()) {
                System.out.println("Empty");
            } else {
//                System.out.println("Openj9 crashes statistics: ");
//                System.out.println("Size: " + j9.size());
//                j9.forEach(System.out::println);
            }

//            System.out.println("Total Unique Crashes: " + total);
        }
        ArrayList<DedupInfo> set = new ArrayList<>();
        set.addAll(hc);
        set.addAll(j9);
        return set;
    }

    private static Set<DedupInfo> dedupHotSpotCrashLogs(File directory) {

        String ErrorRegex = "(Internal|Out of Memory) Error \\((.*?)\\)";
        Pattern ErrorPattern = Pattern.compile(ErrorRegex, Pattern.MULTILINE);

        Set<DedupInfo> results = new HashSet<>();
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".log"));

        for (File file : files) {
            StringBuilder fileTxt = new StringBuilder();
            if (file.getName().startsWith(hotspotCrashPrefix)) {
                StringBuilder stackBuilder = new StringBuilder();
                boolean stackFlag = false;
                int stackLine = 0;
                String javaHome = "";
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Java VM: ")) {
                            javaHome = line;
                        }
                        fileTxt.append(line).append("\n");
                        if (stackFlag) {
                            if (stackLine < 5) {
                                stackBuilder.append(line.replaceAll("\\+0x\\w+", "")).append("\n");
                                stackLine++;
                            }
                        }
                        if (line.startsWith("Native frames:")) {
                            stackFlag = true;
                        }
                        if (line.equals("")) {
                            stackFlag = false;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                boolean unknownBug = false;
                for (String newJDK : NEW_JDK) {
                    if (javaHome.contains(newJDK)) {
                        unknownBug = true;
                        break;
                    }
                }
//                if (!unknownBug) {//todo
//                    continue;
//                }
                Matcher ematcher = ErrorPattern.matcher(fileTxt.toString());
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
                hotspotCrashInfo.setStackFrames(stackBuilder.toString());
                String s = "";
                if (hotspotCrashInfo.getErrorMessage() != null) {
                    s = hotspotCrashInfo.getErrorMessage();
                } else if (hotspotCrashInfo.getStackFrames() != null) {
                    s = hotspotCrashInfo.getStackFrames();
                }
                if (!s.contains("Out of Memory Error") && !unknownBug) {
                    results.add(new DedupInfo(hotspotCrashInfo.fileName, s, unknownBug));
                }
            }
        }
        File summary = new File(directory, directory.getName() + "_hs_err_summary.log");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(summary))) {
            bufferedWriter.write("Unique Crashes: " + results.size() + "\n\n");
            for (DedupInfo info : results) {
                bufferedWriter.write(info.getFilePath() + "\n");
                bufferedWriter.write(info.getCrashInfo() + "\n");
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return results;
    }

    private static Set<DedupInfo> dedupOpenJ9CrashLogs(File directory) {

        String assertionRegex = ".*ASSERTION FAILED.*";
        String VMFlagRegex = "^1XHFLAGS\\s+VM flags:\\d+"; // 正则表达式模式

        String OOMRegex = "Dump Event \"systhrow\" (00040000) Detail \"java/lang/OutOfMemoryError\" \"Java heap space\" received";
        String exceptModuleRegex = "1XHEXCPMODULE  Module: ";
        String currentThreadRegex = "1XMCURTHDINFO  Current thread";
        String nativeStackRegex = "3XMTHREADINFO3           Native callstack:";
        String stackFrameRegex = "4XENATIVESTACK";

        Pattern aseertionPattern = Pattern.compile(assertionRegex, Pattern.MULTILINE);
        Pattern flagPattern = Pattern.compile(VMFlagRegex, Pattern.MULTILINE);

        Set<DedupInfo> results = new HashSet<>();
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        for (File file : files) {
            StringBuilder fileTxt = new StringBuilder();
            if (file.getName().startsWith(openj9CrashPrefix)) {
                J9CrashInfo j9CrashInfo = new J9CrashInfo(file.getAbsolutePath());
                boolean startCurrentThread = false;
                boolean startStack = false;
                int stackLine = 0;
                StringBuilder nativeStackLines = new StringBuilder();
                String javaHome = "";
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Java Home Dir:")) {
                            javaHome = line;
                        }
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
                                    if (stackLine < 5) {
                                        nativeStackLines.append(formatedLine).append("\n");
                                        stackLine++;
                                    }
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
                boolean unknownBug = false;
                for (String newJDK : NEW_JDK) {
                    if (javaHome.contains(newJDK)) {
                        unknownBug = true;
                        break;
                    }
                }
//                if (!unknownBug) { //todo
//                    continue;
//                }
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
                if (!fileTxt.toString().contains(OOMRegex) && !unknownBug) {
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
                    results.add(new DedupInfo(file.getAbsolutePath(), sb.toString(), unknownBug));
                }
            }
        }
        File summary = new File(directory, directory.getName() + "_javacore_summary.log");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(summary))) {
            bufferedWriter.write("Unique Crashes: " + results.size() + "\n\n");
            for (DedupInfo info : results) {
                bufferedWriter.write(info.getFilePath() + "\n");
                bufferedWriter.write(info.getCrashInfo() + "\n");
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return results;
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

    static class DedupInfo {
        private String filePath;
        private String crashInfo;

        private boolean newJDK;

        public DedupInfo(String filePath, String crashInfo, boolean newJDK) {
            this.filePath = filePath;
            this.crashInfo = crashInfo;
            this.newJDK = newJDK;
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
            String s = "";
            if (newJDK) {
                s = "[Unknown Bug]Is New JDK crash";
            } else {
                s = "[Known Bug]Is Old JDK crash";

            }
            return filePath +
                    "\n" +
                    s +
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
