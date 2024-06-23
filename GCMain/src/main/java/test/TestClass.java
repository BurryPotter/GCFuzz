package test;//import sun.hotspot.WhiteBox;


import org.junit.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestClass {

    public static void main(String[] args) {
        String message = "openjdk11-Hotspot-jdk11u-28_hotspot:[VMInitError-Error occurred during initialization of VM " +
                "GC triggered before VM initialization completed. Try increasing NewSize, current value K.];openjdk11-Hotspot-jdk11.0.20_hotspot:[];";
        HashMap<String, String> map = parseDetailOutput(message);
        System.out.println(map);
    }
    private static HashMap<String, String> parseDetailOutput(String detailMessage) {

        // 使用正则表达式匹配JDK信息和括号中的内容
        Pattern pattern = Pattern.compile("((\\w+[-\\._]?)*):\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(detailMessage);

        HashMap<String, String> results = new HashMap<>();
        // 循环匹配并输出结果
        while (matcher.find()) {
            String jdkVersion = matcher.group(1);
            String output = matcher.group(3);
            results.put(jdkVersion, output);
            System.out.println("JVM: " + jdkVersion);
            System.out.println("Error: " + output);
        }
        return results;
    }

}
