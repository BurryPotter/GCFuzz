package main;

import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * 这个类主要应用于覆盖率的收集，需要使用 lcov 产生的 info 文件
 */
public class CoverageCollector {
    private static String covInfoPath = "";
    private static final Map<String, CovRecord> covRecordList = new TreeMap<>(); // key 为源文件路径

    private static long lineCount; // 总行数
    private static long covLineCount; // 覆盖的行数
    private static double lineCoverage; // 行覆盖率

    private static long functionCount;
    private static long covFunctionCount;
    private static double functionCoverage;

    private static long branchCount;
    private static long covBranchCount;
    private static double branchCoverage;

    private static String[] patterns = {""}; // 只收集源文件路径(SF)中包含至少一个 pattern 的覆盖率



    @Test
    public void test() throws IOException {
//        initEnv("D:\\Project\\VECT++\\test\\InvokeSpecialSuccessTest@1310.info");
//
//        long startTime = System.currentTimeMillis();
//        analysisCovInfo();
//        long endTime = System.currentTimeMillis();
//        System.out.println("执行时间："+(endTime - startTime));
//
//        System.out.println(showCoverage());

//        System.out.println(initAndAnalysis("D:\\jdk\\cov_new_gc.info"));

        File file = new File("D:\\projects\\GCGenerator\\cov\\GCFuzz-ECov-9_22\\jdk11u-jdk-11.0.20-8_v8");
        File[] listFiles = file.listFiles();
        for (int i = 0 ; i < listFiles.length ; i ++){
            File f = listFiles[i];
            System.out.println(i);
            String fileName = f.getName();
            if ( fileName.endsWith(".info")){
                String p = f.getAbsolutePath();
                System.out.println(p);
                try{
                    System.out.println(initAndAnalysis(p));

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
//        System.out.println(initAndAnalysis("D:\\Project\\VECT++\\实验结果\\covInfo\\UnexpectedNodeInOuterLoopWhenCloning.info"));
//        System.out.println(initAndAnalysis("D:\\Project\\VECT++\\实验结果\\covInfo\\XCheckJSig.info"));

    }

    /**
     * 初始化环境，分析 info 文件，并且返回覆盖率信息
     * @param covInfoPath
     * @return
     * @throws IOException
     */
    public static String initAndAnalysis(String covInfoPath) throws IOException {
        initEnv(covInfoPath);
        analysisCovInfo();
        return showCoverage();
    }

    /**
     * 设置新的 info 文件路径，并且重置全部信息
     * @param covInfoPath
     */
    public static void initEnv(String covInfoPath) {
        reset();
        CoverageCollector.covInfoPath = covInfoPath;
    }

    /**
     * 根据 covInfoPath 设置的路径，分析 lcov 产生的 info 文件，建模 info 内全部信息
     * @throws IOException
     */
    public static void analysisCovInfo() throws IOException {
        boolean skipFlag = false;
        String name = "";

        File inputFile = new File(covInfoPath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line = bufferedReader.readLine();
        while(line!=null){

            if(line.contains("TN:") || line.contains("end_of_record")){ // 不需要分析
                line=bufferedReader.readLine();
                continue;
            }
            if(line.contains("SF:")){ // 新纪录开始
                skipFlag = patterns.length != 0;
                for (String pattern : patterns) {
                    if (line.contains(pattern)) {
                        skipFlag = false;
                        break;
                    }
                }
                if(!skipFlag){
                    name = line.replace("SF:","");
                    if(!covRecordList.containsKey(name)){
                        covRecordList.put(name, new CovRecord());
                    }
                }

            }
            if(skipFlag){
                line=bufferedReader.readLine();
                continue;
            }
            covRecordList.get(name).analysisLine(line);
            line=bufferedReader.readLine();
        }

        for (String s : covRecordList.keySet()) { // 总结覆盖内容
            CovRecord record = covRecordList.get(s);
            record.summarize();
            CoverageCollector.functionCount += record.FNF;
            CoverageCollector.covFunctionCount += record.FNH;
            CoverageCollector.branchCount += record.BRF;
            CoverageCollector.covBranchCount += record.BRH;
            CoverageCollector.lineCount += record.LF;
            CoverageCollector.covLineCount += record.LH;
        }

        functionCoverage = (double) covFunctionCount / functionCount * 100;
        branchCoverage = (double) covBranchCount / branchCount * 100;
        lineCoverage = (double) covLineCount / lineCount * 100;
    }



    /**
     * 获得覆盖率信息
     * @return
     */
    public static String showCoverage(){
        return "Overall coverage rate:\n" +
                "    " + "lines......: "
                + String.format("%.2f", lineCoverage) + "% (" + covLineCount + " of " + lineCount + " lines)\n" +
                "    " + "functions..: "
                + String.format("%.2f", functionCoverage) + "% (" + covFunctionCount + " of " + functionCount + " functions)\n" +
                "    " + "branches...: "
                + String.format("%.2f", branchCoverage) + "% (" + covBranchCount + " of " + branchCount + " branches)";
    }

    private static void reset(){
        covInfoPath = "";
        covRecordList.clear();
        functionCount = 0;
        covFunctionCount = 0;
        functionCoverage = 0.0;
        branchCount = 0;
        covBranchCount = 0;
        branchCoverage = 0.0;
        lineCount = 0;
        covLineCount = 0;
        lineCoverage = 0.0;
    }

    public static long getFunctionCount() {
        return functionCount;
    }

    public static long getCovFunctionCount() {
        return covFunctionCount;
    }

    public static double getFunctionCoverage() {
        return functionCoverage;
    }

    public static long getBranchCount() {
        return branchCount;
    }

    public static long getCovBranchCount() {
        return covBranchCount;
    }

    public static double getBranchCoverage() {
        return branchCoverage;
    }

    public static long getLineCount() {
        return lineCount;
    }

    public static long getCovLineCount() {
        return covLineCount;
    }

    public static double getLineCoverage() {
        return lineCoverage;
    }

    public static String getCovInfoPath() {
        return covInfoPath;
    }

    public static void setPatterns(String[] patterns) {
        CoverageCollector.patterns = patterns;
    }

    public static Map<String, CovRecord> getCovRecordList(){
        return covRecordList;
    }
}


