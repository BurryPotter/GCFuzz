package optiongen.generator;

import dtjvms.JvmInfo;
import dtjvms.executor.GC.GCExecutor;
import dtjvms.executor.JvmOutput;
import dtjvms.loader.DTLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionFilter {

    public List<Map<String, String>> filter(List<Map<String, String>> optionList, VMType vmType) {
        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();
        ValueUtil valueUtil;
        if (vmType == VMType.J9) {
            valueUtil = new Openj9ValueUtil();
        } else if (vmType == VMType.HOTSPOT) {
            valueUtil = new HotspotValueUtil();
        } else {
            throw new RuntimeException();
        }
        return optionList.stream()
                .filter(optionInfo -> {
                    for (JvmInfo info : jvmCmds) {
                        String type = optionInfo.get("type");
                        String scope = optionInfo.get("scope");
                        String prefix = optionInfo.get("prefix");
                        String name = optionInfo.get("name");
                        String vmOption = valueUtil.getValue(prefix, name, type, scope);
                        String cmd = info.getJavaCmd() + " " + vmOption + " -XX:-IgnoreUnrecognizedVMOptions -version";
                        JvmOutput output = GCExecutor.getInstance().execute(cmd);
                        String err = output.getStderr();
                        System.out.println(cmd);
                        System.out.println(output);
                        if (err.contains("allowed range") || err.contains("Error") || err.contains("error")) {
                            System.err.println("contain");
//                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

    }
}
