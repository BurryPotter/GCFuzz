package main;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class StatusStatistics {
    public static void main(String[] args) {
        String folderPath = "D:\\projects\\GCGenerator\\uniqueBug\\JDK8_7_25_GCFuzz\\03results"; // 替换为你要读取的文件夹路径
        String filterName = "status.log"; // 替换为你要过滤的文件名

        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            System.out.println("Reading files from: " + folder.getAbsolutePath());
            filterFilesInFolder(folder, filterName);
        } else {
            System.out.println("Invalid folder path or folder does not exist.");
        }
        System.out.println();
        System.out.println("diffCount=" + diffCount);
        System.out.println("invalidCount=" + invalidCount);
        System.out.println("totalCount=" + totalCount);

    }

    private static int invalidCount = 0;
    private static int diffCount = 0;
    private static int totalCount = 0;

    private static void filterFilesInFolder(File folder, String filterName) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        Set<String> internalError = new HashSet<>();
        Set<String> currentThread = new HashSet<>();
        for (File file : files) {
            if (file.isDirectory()) {
                filterFilesInFolder(file, filterName); // 递归处理子目录
            } else {
                if (file.getName().contains(filterName)) {
                    int invalid = 0;
                    int diff = 0;
                    int total = 0;
                    System.out.println(file.getAbsolutePath()); // 输出符合条件的文件路径
                    System.out.println("文件内容:");
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("TIMEOUT&Consistent") || line.contains("Rerun&Consistent")) {
                                invalid++;
                            } else if (line.contains("DifferenceFound")) {
                                diff++;
                            }
                            if (line.contains("<CR:")) {
                                total++;
                            }
                        }
                        System.out.println();
                    } catch (IOException e) {
                        System.out.println("读取文件时发生错误: " + file.getName());
                        e.printStackTrace();
                    }
                    diffCount += diff;
                    invalidCount += invalid;
                    totalCount += total;
                    System.out.println("diff=" + diff);
                    System.out.println("invalid=" + invalid);
                    System.out.println("total=" + total);
                }
            }
        }

    }
}
