package optiongen.generator;

import config.FuzzingConfig;

import java.text.DecimalFormat;
import java.util.*;

public class Openj9ValueUtil extends ValueUtil {

    @Override
    public String getValue(String prefix, String name, String type, String scope) {
        String result = null;
        switch (type) {
            case "num":
                result = getIntValue(prefix, name, scope);
                break;
            case "int":
                result = getIntValue(prefix, name, scope);
                break;
            case "<bytes>":
                result = getIntValue(prefix, name, scope);
                break;
            case "enum":
                result = getEnumValue(prefix, name, scope);
                break;
            case "bool":
                result = getBoolValue(prefix, name, scope);
                break;
            case "size":
                result = getSizeVal(prefix, name, scope);
                break;
            case "double":
                result = getDoubleVal(prefix, name, scope);
                break;
            default:
                break;
        }
        return result;
    }


    public String getIntValue(String prefix, String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        int maxVal = Integer.parseInt(scope.split(",")[1].substring(0, scope.split(",")[1].length() - 1));
        int minVal = Integer.parseInt(scope.split(",")[0].substring(1));

        int val = random.nextInt(maxVal - minVal) + minVal;
        if (prefix.equals("-X")) {
            return prefix + name + String.valueOf(val);
        }
        return prefix + name + "=" + String.valueOf(val);
    }

    public String getBoolValue(String prefix, String name, String scope) {
        if (prefix.equals("-Xgc:")) {
            return prefix + name;
        } else if (prefix.equals("-XX:")) {
            Random random = FuzzingConfig.getDefaultRandom();
            if (random.nextInt(2) > 0) {
                return prefix + "+" + name;
            } else {
                return prefix + "-" + name;
            }
        } else {
            if (name.contains("/")) {
                Random random = FuzzingConfig.getDefaultRandom();
                if (random.nextInt(2) > 0) {
                    return prefix + name.split("/")[0];
                } else {
                    return prefix + name.split("/")[1];
                }
            } else {
                return prefix + name;
            }
        }
    }

    public String getEnumValue(String prefix, String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        String[] pool = scope.substring(1, scope.length() - 1).split(",");
        String val = pool[random.nextInt(pool.length)];
        if (prefix.equals("-Xgc:")) {
            return prefix + name + "=" + val;
        } else {
            if (name.endsWith(",")) {
                return prefix + name + val;
            }
            return prefix + name + ":" + val;
        }
    }

    public String getSizeVal(String prefix, String name, String scope) {
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

        List<Long> vals = new ArrayList<>();
        while (vals.size() < name.split("/").length) {
            long val = (random.nextInt(10) + 1) * (maxVal / 10);
            if (val < minVal || val > maxVal) {
                continue;
            }
            vals.add(val);
        }
        Collections.sort(vals);
        StringBuilder result = new StringBuilder();
        int idx = 0;
        for (String optionName : name.split("/")) {
            if (prefix.equals("-X")) {
                result.append(prefix).append(optionName).append(vals.get(idx)).append(" ");
            } else {
                result.append(prefix).append(optionName).append("=").append(vals.get(idx)).append(" ");
            }
            idx += 1;
        }
        return result.substring(0, result.length() - 1);
    }

    public String getDoubleVal(String prefix, String name, String scope) {
        Random random = FuzzingConfig.getDefaultRandom();
        String minValStr = scope.split(",")[0].substring(1);
        String maxValStr = scope.split(",")[1];
        maxValStr = maxValStr.substring(0, maxValStr.length() - 1);
        double minVal = Double.parseDouble(minValStr);
        double maxVal = Double.parseDouble(maxValStr);
        int minFloor = Integer.parseInt(String.valueOf((int) Math.floor(minVal)));
        List<Double> vals = new ArrayList<>();
        while (vals.size() < name.split("/").length) {
            double val = random.nextInt(minFloor + 1) + random.nextDouble();
            if (val < minVal || val > maxVal) {
                continue;
            }
            if (vals.size() > 0 && name.equals("minf/maxf")) {
                //  -Xminf 的值 0.69 必须至少比 -Xmaxf 的值小 0.05
                if (Math.abs(val - vals.get(0)) < 0.05) {
                    continue;
                }
            }
            DecimalFormat format = new DecimalFormat("#.00");
            val = Double.parseDouble(format.format(val));
            vals.add(val);
        }
        Collections.sort(vals);

        StringBuilder result = new StringBuilder();
        int idx = 0;
        for (String optionName : name.split("/")) {
            if (prefix.equals("-X")) {
                result.append(prefix).append(optionName).append(vals.get(idx)).append(" ");
            } else {
                result.append(prefix).append(optionName).append("=").append(vals.get(idx)).append(" ");
            }
            idx += 1;
        }
        return result.substring(0, result.length() - 1);
    }
}