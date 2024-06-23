package optiongen.generator;

import config.FuzzingConfig;

import java.util.Map;
import java.util.Random;

public class CommonValueUtil extends ValueUtil {

    private Map<String, Object> VALUE_POOL;

    public CommonValueUtil(Map<String, Object> valuePool){
        super();
        this.VALUE_POOL = valuePool;
    }

    @Override
    public String getValue(String prefix, String name, String type, String scope) {
        String result = null;
        switch (type) {
            case "size":
                result = getSizeVal(name, scope);
                break;
            default:
                break;
        }
        return result;
    }

    public String getSizeVal(String name, String scope){
        Random random = FuzzingConfig.getDefaultRandom();
        String maxValStr = scope.split(",")[1].substring(0, scope.split(",")[1].length()-1);
        Long val = 0L;
        if(VALUE_POOL.get(maxValStr) != null){
            val = (random.nextInt(10) + 1) * (Long.parseLong(VALUE_POOL.get(maxValStr).toString()) / 10);
        }else {
            val = (random.nextInt(10) + 1) * (Long.parseLong(maxValStr) / 10);
        }

        return name + COMMON_PLACEHOLDER + val;
    }
}
