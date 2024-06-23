package config;

import java.util.HashSet;
import java.util.Set;

public class GCConfiguration {

    public static final String SLASH = "/";
    public static final String DOT = ".";
    public static final String SEMICOLON = ";";


    public static Set<String> FORBIDDEN_MODIFY_CLASS = new HashSet<>();
    public static double OPTION_PRIORITY = 0.5;
    public static int OPTION_NUMBER = 9;
    public static int MAX_MUTATE_TIME = 10;
    public static int MAX_MUTATE_ORDER = 10;
    public static float REWARD_WEIGHT = 10;
    public static float PENALTY_WEIGHT = 3;
    public static float DECAY_WEIGHT = 1;
    public static int TEMPERATURE_INIT = 3;
    public static float TEMPERATURE_DECAY_SPEED = 0.1f;
    public static float GC_COV_DECAY = 3;
    public static int OPTION_THRESHOLD = 30;
    public static float ACC_DECAY_WEIGHT = 5;

    public static float EPS = 1e-10f;

    static {

        GCProperties gcProps = GCProperties.getInstance();
        storePropertyValues(gcProps.getProperty(GCProperties.FORBIDDEN_MODIFY_CLASS_KEY), FORBIDDEN_MODIFY_CLASS);
        OPTION_PRIORITY = Double.parseDouble(gcProps.getProperty(GCProperties.OPTION_PRIORITY_KEY));
        OPTION_NUMBER = Integer.parseInt(gcProps.getProperty(GCProperties.MAX_OPTION_NUMBER_KEY));
        MAX_MUTATE_TIME = Integer.parseInt(gcProps.getProperty(GCProperties.MAX_MUTATE_TIME_KEY));
        MAX_MUTATE_ORDER = Integer.parseInt(gcProps.getProperty(GCProperties.MAX_MUTATE_ORDER_KEY));
        REWARD_WEIGHT = Float.parseFloat(gcProps.getProperty(GCProperties.REWARD_WEIGHT_KEY));
        PENALTY_WEIGHT = Float.parseFloat(gcProps.getProperty(GCProperties.PENALTY_WEIGHT_KEY));
        DECAY_WEIGHT = Float.parseFloat(gcProps.getProperty(GCProperties.DECAY_WEIGHT_KEY));
        TEMPERATURE_INIT = Integer.parseInt(gcProps.getProperty(GCProperties.TEMPERATURE_INIT_KEY));
        TEMPERATURE_DECAY_SPEED = Float.parseFloat(gcProps.getProperty(GCProperties.TEMPERATURE_DECAY_SPEED_KEY));
        GC_COV_DECAY = Float.parseFloat(gcProps.getProperty(GCProperties.GC_COV_DECAY_KEY));
        OPTION_THRESHOLD = Integer.parseInt(gcProps.getProperty(GCProperties.OPTION_THRESHOLD_KEY));
        ACC_DECAY_WEIGHT = Integer.parseInt(gcProps.getProperty(GCProperties.ACC_DECAY_WEIGHT_KEY));


    }

    private static void storePropertyValues(String values, Set<String> toSet) {

        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
//                val = val.replace(DOT, SLASH).trim();
                val = val.trim();
                if (!val.isEmpty()) {
                    toSet.add(val);
                }
            }
        }
    }


}
