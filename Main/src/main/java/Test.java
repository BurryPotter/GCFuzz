//import main.ReplayInfo;
//
//import java.io.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class Test {
//    public static String statusPath = "D:\\projects\\GCGenerator\\ecoop\\rq1\\avrora_137_11_GCFuzz_GCFuzz-Random_12_21\\03results\\1702995386\\avrora-cvs-20091224\\status.log";
//
//    public static void main(String[] args) throws IOException {
//        File f = new File(statusPath);
//        BufferedReader reader = new BufferedReader(new FileReader(f));
//        File fo = new File("./output_status.log");
//        BufferedWriter writer = new BufferedWriter(new FileWriter(fo));
//        reader.lines().forEach(line -> {
//            if (line.contains("CR:") && line.contains("DifferenceFound")) {
//                ReplayInfo replayInfo = parseStatus(line);
//                try {
//                    writer.write(replayInfo.getTestClass() + "\n");
//                    writer.write(replayInfo.getVmoption() + "\n");
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//        writer.flush();
//        writer.close();
//    }
//    public static ReplayInfo parseStatus(String lineStr) {
//        String tag = lineStr.substring(lineStr.indexOf("<CR"), lineStr.indexOf(">") + 1);
//        String afterL = lineStr.split("\\(")[1];
//        long seed = Long.parseLong(afterL.split("\\)")[0]);
//        String afterR = afterL.split("\\)")[1];
//        String vmoption = afterR.split("vmoption:")[1];
//        String afterRBeforeOption = afterR.split("vmoption:")[0];
//        Matcher gcTypeMatcher = Pattern.compile("<(\\w+) event").matcher(afterRBeforeOption);
//        String gcType = null;
//        if (gcTypeMatcher.find()) {
//            gcType = gcTypeMatcher.group(1);
//        }
//        Matcher mathcer = Pattern.compile("(\\w|@|\\$|\\.)+\\.class").matcher(afterRBeforeOption);
//        String testClass = null;
//        while (mathcer.find()) {
//            testClass = mathcer.group();
//        }
//        String originClass = testClass.substring(0, testClass.lastIndexOf("_"));
//        return new ReplayInfo(seed, testClass, vmoption, originClass, tag, gcType);
//    }
//}
