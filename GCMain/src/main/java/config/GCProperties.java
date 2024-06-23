package config;

import java.io.InputStream;
import java.util.Properties;

public class GCProperties extends Properties {

    public static final String FORBIDDEN_MODIFY_CLASS_KEY = "forbidden.modify.class";
    public static final String OPTION_PRIORITY_KEY = "option.priority";
    public static final String MAX_OPTION_NUMBER_KEY = "max.option.number";
    public static final String MAX_MUTATE_TIME_KEY = "max.mutate.time";
    public static final String MAX_MUTATE_ORDER_KEY = "max.mutate.order";
    public static final String REWARD_WEIGHT_KEY = "reward.weight";
    public static final String PENALTY_WEIGHT_KEY = "penalty.weight";
    public static final String DECAY_WEIGHT_KEY = "decay.weight";
    public static final String TEMPERATURE_INIT_KEY = "temperature.init";
    public static final String TEMPERATURE_DECAY_SPEED_KEY = "temperature.decay.speed";
    public static final String GC_COV_DECAY_KEY = "gc.cov.decay";
    public static final String OPTION_THRESHOLD_KEY = "option.threshold";
    public static final String ACC_DECAY_WEIGHT_KEY = "acc.decay.weight";

    public static final String DEFAULT_PROPERTIES = "/gc.properties";

    private static GCProperties instance;
    public GCProperties() {

        try {
            InputStream inputStream = this.getClass().getResourceAsStream(DEFAULT_PROPERTIES);
            if (inputStream == null){
                System.err.println("No " + DEFAULT_PROPERTIES);
                throw new IllegalAccessException();
            }
            load(inputStream);
            inputStream.close();
        }catch (Exception e){

            e.printStackTrace();
            System.err.println("Unable to load properties file from " + DEFAULT_PROPERTIES);
        }

    }

    public static GCProperties getInstance(){
        if (instance == null){
            instance = new GCProperties();
        }
        return instance;
    }

    @Override
    public String getProperty(String key) {
        String prop = System.getProperty(key);
        if (prop == null) {
            prop = super.getProperty(key);
        }
        return prop;
    }
}
