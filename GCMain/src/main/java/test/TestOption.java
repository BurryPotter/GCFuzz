package test;

import codegen.blocks.ClassInfo;
import dtjvms.DTConfiguration;
import dtjvms.JvmInfo;
import dtjvms.ProjectInfo;
import dtjvms.executor.GC.GCExecutor;
import dtjvms.fjvms.config.ConfigBean;
import dtjvms.fjvms.config.ConfigJTRegBean;
import dtjvms.loader.DTLoader;
import optiongen.generator.Generator;
import soot.SootClass;
import utils.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestOption {
    public static void main(String[] args) {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);

        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();

        ProjectInfo targetProject = new ProjectInfo("test.TestOption", "D:\\projects\\GCGenerator\\GCFuzzing\\GCMain");
        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayList1.add(className);
        targetProject.setApplicationClasses(arrayList1);
        targetProject.setSrcClassPath("D:\\projects\\GCGenerator\\GCFuzzing\\GCMain\\target\\classes");
        targetProject.setSrcJavaPath("D:\\projects\\GCGenerator\\GCFuzzing\\GCMain\\src\\main\\java");
        targetProject.setJunitClasses(new ArrayList<>());

        HashMap<String, ArrayList<ConfigBean>> configBeans = new HashMap<>();
        ArrayList<ConfigBean> arrayList = new ArrayList<>();
        Generator vmOptionGenerator = new Generator();
//        while (true) {
//            Map<String, String> vmoption = vmOptionGenerator.generateOption();
//            String option = "";
//            if (DTConfiguration.TARGET_JVMS.size() == 1) {
//                if (DTConfiguration.TARGET_JVMS.contains("hotspot")) {
//                    option = vmoption.get("hotspot");
//                } else if (DTConfiguration.TARGET_JVMS.contains("openj9")) {
//                    option = vmoption.get("openj9");
//                }
//            } else {
//                String warn = "Warning! gc fuzzer does not suggest test 2 or more jvms at a time.";
//                System.err.println(warn);
//
//            }
//            System.out.println(option);
////            ConfigJTRegBean configBean = new ConfigJTRegBean(className, option, className, "");
////            arrayList.add(configBean.getBean());
////            configBeans.put(className, arrayList);
//
////            GCExecutor.getInstance().dtSingleClassInProj(jvmCmds, targetProject, info.getClassName(), configBeans);
//        }

    }
}

