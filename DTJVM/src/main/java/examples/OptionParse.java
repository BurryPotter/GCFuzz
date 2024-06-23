package examples;

import dtjvms.ProjectInfo;
import dtjvms.fjvms.ConfigGenerator;
import dtjvms.fjvms.config.ConfigBean;
import dtjvms.loader.DTLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionParse {
    public static void main(String[] args) throws IOException {
        String srcPath = "D:\\projects\\GCGenerator\\GCFuzzing\\02Benchmarks\\HotspotTests-Java\\src";
        File root = new File(srcPath);
        HashMap<String, Integer> option2Count = new HashMap<>();

        filterFilesInFolder(root, ".java", option2Count);
        for (Map.Entry<String, Integer> stringIntegerEntry : option2Count.entrySet()) {
            System.out.println(String.format("%s\t%d", stringIntegerEntry.getKey(), stringIntegerEntry.getValue()));
        }
        System.out.println(option2Count);
    }

    public static void filterFilesInFolder(File folder, String suffix, HashMap<String, Integer> option2Count) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                filterFilesInFolder(file, suffix, option2Count); // 递归处理子目录
            } else {
                if (file.getName().endsWith(suffix)) {
                    System.out.println(file.getAbsolutePath());
                    ArrayList<ConfigBean> configBeans = ConfigGenerator.generate(file.getAbsolutePath());
                    List<List<String>> optionList = configBeans.stream()
                            .map(configBean -> Arrays.stream(configBean.getOptions().split("\\s+"))
                                    .map(option -> {
                                                String newOption = option.trim()
                                                        .replace("-X", "")
                                                        .replace("-", "")
                                                        .replace("+", "")
                                                        .replace("X:", "");
                                                int index = newOption.indexOf("=");
                                                if (index > 0) {
                                                    newOption = newOption.substring(0, index);
                                                }
//                                                index = newOption.indexOf(":");
//                                                if (index > 0) {
//                                                    newOption = newOption.substring(0, index);
//                                                }
                                                return newOption;
                                            }
                                    ).collect(Collectors.toList()))
                            .collect(Collectors.toList());
                    for (List<String> ss : optionList) {
                        for (String s : ss) {
                            if (s.isEmpty()) {
                                continue;
                            }
                            option2Count.merge(s, 1, Integer::sum);
                        }
                    }
//                    System.out.println(optionList);
                }
            }
        }

    }
}
