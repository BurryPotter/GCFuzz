package optiongen.generator;

import config.FuzzingConfig;
import dtjvms.executor.GC.GCType;
import optiongen.bean.GCOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class HotspotValueUtil extends ValueUtil {
    private final GCGenerator hotspotGenerator = new GCGenerator(VMType.HOTSPOT);

    @Override
    public String getValue(String prefix, String name, String type, String scope) {
        String result = null;
        switch (type) {
            case "gc":
                result = getGCVal(name, scope);
                break;
            case "uint":
                result = getInt64Val(name, scope);
                break;
            case "ccstr":
                result = getStrVal(name, scope);
                break;
            case "int":
                result = getInt64Val(name, scope);
                break;
            case "intx":
                result = getInt64Val(name, scope);
                break;
            case "uintx":
                result = getInt64Val(name, scope);
                break;
            case "double":
                result = getDoubleVal(name, scope);
                break;
            case "bool":
                result = getBoolVal(name, scope);
                break;
            case "size_t":
                ArrayList<String> options = getSizeVal(name, scope);
                StringBuilder stringBuilder = new StringBuilder();
                for (String option : options) {
                    stringBuilder.append(prefix).append(option).append(" ");
                }
                result = stringBuilder.toString();
                return result;
            default:
                break;
        }
        return prefix + result;
    }

    public String getGCVal(String name, String scope) {
        GCOption<GCType> gcOption = hotspotGenerator.randomGCOption(true);
        return gcOption.getGcOption();
    }

    public String getInt64Val(String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        String result = "";
        Long minVal = Long.parseLong(scope.split(",")[0].substring(1));
        Long maxVal = Long.parseLong(scope.split(",")[1].substring(0, scope.split(",")[1].length() - 1));
        Long val;
        if (maxVal - minVal < 100L) {
            val = minVal + random.nextInt(Integer.parseInt(maxVal.toString()) - Integer.parseInt(minVal.toString()));
        } else {
            val = minVal + (random.nextInt(100) + 1) * ((maxVal - minVal) / 100);
            while (val < minVal) {
                val = minVal + (random.nextInt(100) + 1) * ((maxVal - minVal) / 100);
            }
        }

        result = result + name + "=" + val;

        return result;
    }

    public String getDoubleVal(String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        String result = "";
        double minVal = Double.parseDouble(scope.split(",")[0].substring(1));
//        double maxVal = Double.MAX_VALUE;
        double maxVal = Double.parseDouble(scope.split(",")[1].replace(')', ' '));
        double val = random.nextDouble();
        while (val < minVal || val > maxVal) {
            val = random.nextInt() + random.nextDouble();
        }
        result = result + name + "=" + val;
        return result;
    }

    public String getBoolVal(String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        String result = "";
        String flag = random.nextInt(2) == 1 ? "+" : "-";
        return result + flag + name;
    }

    public String getStrVal(String name, String scope) {
        if (scope == null) {
            scope = "";
        }

        return name + "=" + scope;
    }

    public ArrayList<String> getSizeVal(String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        String minValStr = scope.split(",")[0].substring(1);
        String maxValStr = scope.split(",")[1];
        maxValStr = maxValStr.substring(0, maxValStr.length() - 1);
        long minVal = 0;
        long maxVal = 0;
        if (minValStr.contains("*")) {
            minVal = getBytes(minValStr);
        } else {
            minVal = Long.parseLong(minValStr);
        }
        if (maxValStr.contains("*")) {
            maxVal = getBytes(maxValStr);
        } else {
            maxVal = Long.parseLong(maxValStr);
        }

        ArrayList<Long> vals = new ArrayList<>();
        while (vals.size() < name.split("/").length) {
            if (maxVal < 100) {
                long val = random.nextInt((int) maxVal + 1);
                while (val < minVal) {
                    val = random.nextInt((int) maxVal + 1);
                }
                vals.add(val);
            } else {
                long val = (random.nextInt(100) + 1) * (maxVal / 100);
                while (val < minVal) {
                    val = (random.nextInt(100) + 1) * (maxVal / 100);
                }
                vals.add(val);
            }
        }
        Collections.sort(vals);

        ArrayList<String> result = new ArrayList<>();
        int idx = 0;
        for (String optionName : name.split("/")) {
            result.add(optionName + "=" + vals.get(idx));
            idx += 1;
        }
        return result;
    }


}
