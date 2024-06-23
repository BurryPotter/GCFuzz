package main;

import codegen.operators.LoopOperator;
import codegen.operators.Operator;
import codegen.operators.control.CheckOperator;
import codegen.operators.control.GCTrapOperator;
import dtjvms.*;
import optiongen.generator.Generator;

import java.io.IOException;
import java.util.*;

public class TestClass {
    public static void main(String[] args) throws IOException {
         TreeMap<Integer, Operator> controlOperators = new TreeMap<>();
        controlOperators.put(20, LoopOperator.getInstance());
        controlOperators.put(40, CheckOperator.getInstance());
        controlOperators.put(100, GCTrapOperator.getInstance());
        Iterator<Integer> iterator = controlOperators.keySet().iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
//        Generator generator = new Generator();
//        generator.testOption();
        //        List<String> list = generator.generateAll("hotspot");
//        String dir = "D:\\projects\\GCGenerator\\JITFuzzing\\sootOutput\\GCTestCases\\target\\classes";
//        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();
//        for (int i = 0; i < 10000; i++) {
//            String option = generateOption();
//            for (JvmInfo info : jvmCmds) {
//                String cmd = info.getJavaCmd() + " " + option + " " + "-version";
//                JvmOutput output = GCExecutor.getInstance().execute(cmd, dir);
//                String err = output.getStderr();
//                if (err.contains("Unrecognized VM option")){
//                    String o = err.split("'")[1];
//                    if (o.contains("=")){
//                        o = o.split("=")[0];
//                    }
//                    System.out.println(o);
//                }
//            }
//        }
    }
    private static String generateOption(){
        String option = "";
        Generator vmOptionGenerator = new Generator();
//        Map<String, String> vmoption = vmOptionGenerator.generateOption();
//
//        if (DTConfiguration.TARGET_JVMS.size() == 1) {
//            if (DTConfiguration.TARGET_JVMS.contains("hotspot")) {
//                option = vmoption.get("hotspot");
//            } else if (DTConfiguration.TARGET_JVMS.contains("openj9")) {
//                option = vmoption.get("openj9");
//            }
//        } else {
//            String warn = "Warning! gc fuzzer does not suggest test 2 or more jvms at a time.";
//            System.err.println(warn);
//        }
        return option;
    }
    static class OptionInfo{
        String name;
        String unrecognizedVersion;

        public OptionInfo(String name, String unrecognizedVersion) {
            this.name = name;
            this.unrecognizedVersion = unrecognizedVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OptionInfo that = (OptionInfo) o;
            return Objects.equals(name, that.name) && Objects.equals(unrecognizedVersion, that.unrecognizedVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, unrecognizedVersion);
        }
    }
}
