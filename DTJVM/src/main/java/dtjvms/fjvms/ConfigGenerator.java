package dtjvms.fjvms;

import dtjvms.DTPlatform;
import dtjvms.ProjectInfo;
import dtjvms.fjvms.config.ConfigBean;
import dtjvms.fjvms.config.ConfigJTRegBean;
import org.json.JSONObject;
import util.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ConfigGenerator {
    public static final String CONFIG_FILE = "reproduce.cfg.json";

    public static boolean writeConfig(HashMap<String, ArrayList<ConfigBean>> bean, ProjectInfo info, String relativePath) {
        String srcPath = info.getSrcJavaPath() + DTPlatform.FILE_SEPARATOR + relativePath;
        FileWriter fw = FileUtil.writeFile(srcPath, CONFIG_FILE);
        JSONObject jsonObject = new JSONObject(bean);
        Writer writer = jsonObject.write(fw);
        try {
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static HashMap<String, ArrayList<ConfigBean>> generate(ProjectInfo info, String vmOption, String parameter) {
        HashMap<String, ArrayList<ConfigBean>> map = new HashMap<>();
        ArrayList<String> classes = info.getApplicationClasses();
        for (String clazz : classes) {
            String className = "";
            if (clazz.contains(".")) {
                className = clazz.substring(clazz.lastIndexOf(".") + 1);
            } else {
                className = clazz;
            }
            ConfigJTRegBean configBean = new ConfigJTRegBean(clazz, vmOption, className, parameter);
            ArrayList<ConfigBean> initValue = new ArrayList<ConfigBean>();
            initValue.add(configBean.getBean());
            map.merge(clazz, initValue, (o, n) -> {
                o.addAll(n);
                return o;
            });
        }
        return map;
    }

    public static HashMap<String, ArrayList<ConfigBean>> generate(ProjectInfo info, String relativePath) {
        HashMap<String, ArrayList<ConfigBean>> map = new HashMap<>();
        String srcPath = info.getSrcJavaPath() + DTPlatform.FILE_SEPARATOR + relativePath;
        ArrayList<String> classes = info.getApplicationClasses();
        for (String clazz : classes) {
            try {
                String classPath = clazz.replace(".", DTPlatform.FILE_SEPARATOR);
                ArrayList<ConfigBean> initValue = generate(srcPath + DTPlatform.FILE_SEPARATOR + classPath + ".java");
                map.merge(clazz, initValue, (o, n) -> {
                    o.addAll(n);
                    return o;
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static ArrayList<ConfigBean> generate(String javaPath) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(FileUtil.readFile(javaPath));
        ArrayList<String> comments = (ArrayList<String>) reader.lines()
                .filter(line -> line.startsWith(" *") | line.startsWith("/*"))
                .collect(Collectors.toList());
        ArrayList<String> runTags = JTRegParser.getInstance().parseRun(comments);
        ArrayList<ConfigBean> initValue = new ArrayList<ConfigBean>();
        runTags
                .forEach(line -> {
//                            System.out.println(line);
                    String[] s = line.split("@run\\s+((driver)|(main)|(junit))(([/=])?\\w*)*\\s*");
                    if (s.length <= 1) {
                        return;
                    }
                    s = s[1].replace("*/", "").split("\\s+");
                    StringBuilder options = new StringBuilder();
                    StringBuilder parameters = new StringBuilder();
                    String className = null;
                    for (String str : s) {

                        if (str.startsWith("-")) {
                            options.append(str);
                            options.append(' ');
                        } else {
                            if (className == null) {
                                className = str;
                            } else {
                                parameters.append(str);
                                parameters.append(' ');
                            }
                        }
                    }
                    if (className == null){
                        className = "";
                    }
                    ConfigJTRegBean bean = new ConfigJTRegBean(className, options.toString().trim(), className.trim(), parameters.toString().trim());
//                            ConfigBean bean = new ConfigBean();
                    initValue.add(bean.getBean());

                });
        return initValue;
    }
//    public static ArrayList<ConfigJTRegBean> generate(ProjectInfo info, String relativePath) {
//        ArrayList<ConfigJTRegBean> list = new ArrayList<>();
//        String srcPath = info.getSrcJavaPath() + DTPlatform.FILE_SEPARATOR + relativePath;
//        ArrayList<String> classes = info.getApplicationClasses();
//        for (String clazz : classes) {
//            try {
//                clazz = clazz.replace(".", DTPlatform.FILE_SEPARATOR);
//                BufferedReader reader = new BufferedReader(FileUtil.readFile(srcPath, clazz + ".java"));
//                ArrayList<String> comments = (ArrayList<String>) reader.lines()
//                        .filter(line -> line.startsWith(" *") | line.startsWith("/*"))
//                        .collect(Collectors.toList());
//                ArrayList<String> runTags = JTRegParser.getInstance().parseRun(comments);
//                list.addAll(runTags.stream()
//                        .map(line -> {
//                            System.out.println(line);
//                            String[] s = line.split("((driver)|(main))(([/=])?\\w*)*\\s*");
//                            if (s.length == 0) {
//                                return null;
//                            }
//
//                            s = s[1].replace("*/", "").split(" ");
//                            StringBuilder options = new StringBuilder();
//                            StringBuilder parameters = new StringBuilder();
//                            String className = null;
//                            for (String str : s) {
//
//                                if (str.startsWith("-")) {
//                                    options.append(str);
//                                    options.append(' ');
//                                } else {
//                                    if (className == null) {
//                                        className = str;
//                                    } else {
//                                        parameters.append(str);
//                                        parameters.append(' ');
//                                    }
//                                }
//                            }
//                            ConfigJTRegBean bean = new ConfigJTRegBean(className, options.toString(), className, parameters.toString());
//                            return bean;
//                        }).collect(Collectors.toList()));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//        return list;
//    }
}
