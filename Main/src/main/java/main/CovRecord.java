package main;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class CovRecord {
    public String SF; // 文件路径

    public Map<String, Long> FNDAs; // 具体函数覆盖,key为函数名,value是覆盖次数
    public long FNF; // 函数总数
    public long FNH; // 函数总数中被执行到的个数

    public Map<String, Long> BRDAs; // 具体分支覆盖, key是 行号，块编号，分支编号 , value是覆盖次数
    public long BRF; // 分支总数
    public long BRH; // 执行到的分支数

    public Map<String, Long> DAs; // 具体语句覆盖, key是行号，value是覆盖次数
    public long LF; // 有效代码行数
    public long LH; // 执行到的代码行数

    public CovRecord() {
        SF = "";

        FNDAs = new TreeMap<>();
        FNF = 0;
        FNH = 0;

        BRDAs = new TreeMap<>();
        BRF = 0;
        BRH = 0;

        DAs = new TreeMap<String, Long>(
                new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        //利用Comparator来实现降序；
                        return (int) (Long.parseLong(o1) - Long.parseLong(o2));
                    }
                });
        LF = 0;
        LH = 0;
    }

    public void analysisLine(String line) {
        if (line.contains("SF:")) {
            SF = line.replace("SF:", "");
        }

        String key;
        if (line.contains("FNDA:")) {
            String[] temp = line.replace("FNDA:", "").split(",");
            key = temp[1];
            long covTime;
            if (FNDAs.containsKey(key)) {
                covTime = FNDAs.get(key);
                covTime += Long.parseLong(temp[0]);
            } else {
                covTime = Long.parseLong(temp[0]);
            }
            FNDAs.put(key, covTime);
        } else if (line.contains("BRDA:")) {
            String[] temp = line.replace("BRDA:", "").split(",");
            if (Objects.equals(temp[3], "-")) {
                temp[3] = "0";
            }
            key = temp[0] + "," + temp[1] + "," + temp[2];
            long covTime;
            if (BRDAs.containsKey(key)) {
                covTime = BRDAs.get(key);
                covTime += Long.parseLong(temp[3]);
            } else {
                covTime = Long.parseLong(temp[3]);
            }
            BRDAs.put(key, covTime);


        } else if (line.contains("DA:")) {
            String[] temp = line.replace("DA:", "").split(",");
            key = temp[0];
            long covTime;
            if (DAs.containsKey(key)) {
                covTime = DAs.get(key);
                covTime += Long.parseLong(temp[1]);
            } else {
                covTime = Long.parseLong(temp[1]);
            }
            DAs.put(key, covTime);
        }
    }

    public void summarize() {
        FNF = FNDAs.keySet().size();
        for (String key : FNDAs.keySet()) {
            if (FNDAs.get(key) != 0) {
                FNH++;
            }
        }

        BRF = BRDAs.keySet().size();
        for (String key : BRDAs.keySet()) {
            if (BRDAs.get(key) != 0) {
                BRH++;
            }
        }

        LF = DAs.keySet().size();
        for (String key : DAs.keySet()) {
            if (DAs.get(key) != 0) {
                LH++;
            }
        }
    }

    @Override
    public String toString() {
        return "covRecord:" + "\n" +
                "SF='" + SF + '\'' + "\n" +
                "FNDAs=" + FNDAs.toString() + "\n" +
                "FNF=" + FNF + "\n" +
                "FNH=" + FNH + "\n" +
                "BRDAs=" + BRDAs.toString() + "\n" +
                "BRF=" + BRF + "\n" +
                "BRH=" + BRH + "\n" +
                "DAs=" + DAs.toString() + "\n" +
                "LF=" + LF + "\n" +
                "LH=" + LH + "\n";
    }


}
