package optiongen.generator;

import config.FuzzingConfig;

import java.util.*;

public abstract class ValueUtil {

    public abstract String getValue(String prefix, String name, String type, String scope);

    public static String COMMON_PLACEHOLDER = "#";

    public static Map<String, Long> SIZE_MAP = new HashMap<>();

    public static Set<String> BINDING_OPTION = new HashSet<>();

    public String getSize(Long size){
        String valWithUnit = "";
        if(size < SIZE_MAP.get("KB")){
            valWithUnit = size + "";
        }else if(size < SIZE_MAP.get("MB")){
            valWithUnit = size/SIZE_MAP.get("KB") + "K";
        }else if(size < SIZE_MAP.get("GB")){
            valWithUnit = size / SIZE_MAP.get("MB") + "M";
        }else {
            valWithUnit = size / SIZE_MAP.get("GB") + "G";
        }
        return valWithUnit;
    }

    public Long getBytes(String size){
        String unit = size.split("\\*")[1];
        long val = Long.parseLong(size.split("\\*")[0]);
        return  SIZE_MAP.get(unit) * val;
    }
}
