package config;

import codegen.operators.*;
import codegen.providers.OperatorProvider;
import codegen.providers.TypeProvider;
import dtjvms.DTGlobal;
import org.junit.Test;
import soot.Type;
import soot.jimple.Expr;

import java.util.*;

public class FuzzingConfig {
    public enum RandomType {
        DEFAULT, CONTROL, SEED_POOL, OPERATOR_POOL, OPTION_POOL
    }

    private static int probability;

    private static long randomSeed = -1;
    private static Random defaultRandom;
    private static Random controlRandom;
    private static Random seedPoolRandom;
    private static Random operatorPoolRandom;
    private static Random optionPoolRandom;

    public static final String SEMICOLON = ";";
    // max value configurations
    public static int MAX_FIELD_NUM = 10;
    public static int MAX_FUNS_NUM = 10;
    public static int MAX_FUNS_PARAMS = 5;
    public static int MAX_METHOD_INVOCATION = 15;

    public static int MAX_ARRAY_DIM = 5;
    public static int MAX_ARRAY_SIZE_PERDIM = 20;
    public static int MAX_ARRAY_LENGTH = 256;

    public static int MAX_FUZZ_STEP = 10;
    public static int MAX_NESTED_SIZE = 5;
    public static int MAX_BLOCK_INST = 500;
    public static int MAX_LOOP_SIZE = 100;
    public static int MAX_LOOP_STEP = 5;
    public static int MAX_SWITCH_CASES = 10;

    //probability configurations
    /**
     * group probability for Arith
     */
//    public static
    public static HashMap<Expr, Integer> PROB_ARITH_GROUP = new HashMap<>();
    public static int PROB_ADD_VALUE = 50;
    public static int PROB_SUB_VALUE = 50;
    public static int PROB_MUL_VALUE = 50;
    public static int PROB_DIV_VALUE = 50;
    public static int PROB_MOD_VALUE = 50;

    public static int PROB_CAST_VALUE = 50;

    /**
     * group probability for Logic
     */
    public static HashMap<Expr, Integer> PROB_LOGIC_GROUP = new HashMap<>();
    public static int PROB_OR_VALUE = 50;
    public static int PROB_AND_VALUE = 50;
    public static int PROB_XOR_VALUE = 50;
    public static int PROB_NEG_VALUE = 50;
    public static int PROB_SHL_VALUE = 50;
    public static int PROB_SHR_VALUE = 50;
    public static int PROB_USHR_VALUE = 50;

    /**
     * group probability for Relation
     */
    public static HashMap<Expr, Integer> PROB_RELATION_GROUP = new HashMap<>();
    public static int PROB_EQ_VALUE = 50;
    public static int PROB_GT_VALUE = 50;
    public static int PROB_GE_VALUE = 50;
    public static int PROB_LT_VALUE = 50;
    public static int PROB_LE_VALUE = 50;

    /**
     * group probability for Type
     */
    public static HashMap<Type, Integer> PROB_TYPE_GROUP = new HashMap<>();
    public static int PROB_CHAR_VALUE = 50;
    public static int PROB_INT_VALUE = 50;
    public static int PROB_BOOL_VALUE = 50;
    public static int PROB_FLOAT_VALUE = 50;
    public static int PROB_DOUBLE_VALUE = 50;
    public static int PROB_LONG_VALUE = 50;
    public static int PROB_SHORT_VALUE = 50;
    public static int PROB_OBJECT_VALUE = 50;

    public static int PROB_NULL_VALUE = 50;

    public static int PROB_GLOBAL_FIELD = 50;
    public static int PROB_NEW_ARRAY = 50;
    public static int PROB_REUSE_VAR = 50;
    public static int PROB_REUSE_REF_VAR = 50;

    public static int PROB_VOID_METHOD = 50;
    public static int PROB_STATIC_FIELD = 50;
    public static int PROB_VOLATILE_FIELD = 50;

    /**
     * group probability for Operations
     */
    public static int PROB_API_VALUE = 50;
    public static int PROB_FUNC_VALUE = 50;
    public static int PROB_ARITH_VALUE = 50;
    public static int PROB_ARRAY_VALUE = 50;
    public static int PROB_IF_VALUE = 50;
    public static int PROB_LOOP_VALUE = 50;
    public static int PROB_SWITCH_VALUE = 50;
    public static int PROB_TRAP_VALUE = 50;

    public static HashMap<Operator, Integer> PROB_INTERNAL_OPERATOR_GROUP = new HashMap<>();
    public static int PROB_API_INTERNAL_VALUE = 50;
    public static int PROB_ARITH_INTERNAL_VALUE = 50;
    public static int PROB_ARRAY_INTERNAL_VALUE = 50;

    public static int PROB_RETURN_VALUE = 50;
    public static int PROB_BREAK_VALUE = 50;
    public static int PROB_GOTO_VALUE = 50;
    public static int PROB_REUSE_INST = 50;

    public static int PROB_STATIC_METHOD_INVOCATION = 50;

    public static int PROB_SELF_METHOD_INVOCATION = 60;

    public static int PROB_INITIALIZE_METHOD = 50;

    public static Set<String> INVALID_REFERENCE_TYPE = new HashSet<>();
    public static Set<String> INVALID_EXCEPTION_TYPE = new HashSet<>();

    static {

        FuzzingProperties fuzzingProperties = FuzzingProperties.getInstance();
        // max value configurations
        MAX_FIELD_NUM = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_FIELD_NUM_KEY));
        MAX_FUNS_NUM = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_FUNS_NUM_KEY));
        MAX_FUNS_PARAMS = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_FUNS_PARAMS_KEY));
        MAX_METHOD_INVOCATION = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_METHOD_INVOCATION_KEY));

        MAX_ARRAY_DIM = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_ARRAY_DIM_KEY));
        MAX_ARRAY_SIZE_PERDIM = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_ARRAY_SIZE_PERDIM_KEY));
        MAX_ARRAY_LENGTH = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_ARRAY_LENGTH_KEY));

        MAX_FUZZ_STEP = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_FUZZ_STEP_KEY));
        MAX_NESTED_SIZE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_NESTED_SIZE_KEY));
        MAX_BLOCK_INST = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_BLOCK_INST_KEY));
        MAX_LOOP_SIZE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_LOOP_SIZE_KEY));
        MAX_LOOP_STEP = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_LOOP_STEP_KEY));
        MAX_SWITCH_CASES = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.MAX_SWITCH_CASES_KEY));

        PROB_ADD_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_ADD_VALUE_KEY));
        PROB_SUB_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_SUB_VALUE_KEY));
        PROB_MUL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_MUL_VALUE_KEY));
        PROB_DIV_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_DIV_VALUE_KEY));
        PROB_MOD_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_MOD_VALUE_KEY));
        PROB_ARITH_GROUP.put(OperatorProvider.addExpr, PROB_ADD_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.subExpr, PROB_SUB_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.mulExpr, PROB_MUL_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.divExpr, PROB_DIV_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.remExpr, PROB_MOD_VALUE);

        PROB_CAST_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_CAST_VALUE_KEY));

        PROB_OR_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_OR_VALUE_KEY));
        PROB_AND_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_AND_VALUE_KEY));
        PROB_XOR_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_XOR_VALUE_KEY));
        PROB_NEG_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_NEG_VALUE_KEY));
        PROB_SHL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_SHL_VALUE_KEY));
        PROB_SHR_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_SHR_VALUE_KEY));
        PROB_USHR_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_USHR_VALUE_KEY));
        PROB_LOGIC_GROUP.put(OperatorProvider.orExpr, PROB_OR_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.andExpr, PROB_AND_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.xorExpr, PROB_XOR_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.negExpr, PROB_NEG_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.shlExpr, PROB_SHL_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.shrExpr, PROB_SHR_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.ushrExpr, PROB_USHR_VALUE);

        PROB_EQ_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_EQ_VALUE_KEY));
        PROB_GT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_GT_VALUE_KEY));
        PROB_GE_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_GE_VALUE_KEY));
        PROB_LT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_LT_VALUE_KEY));
        PROB_LE_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_LE_VALUE_KEY));
        PROB_RELATION_GROUP.put(OperatorProvider.eqExpr, PROB_EQ_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.gtExpr, PROB_GT_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.geExpr, PROB_GE_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.ltExpr, PROB_LT_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.leExpr, PROB_LE_VALUE);

        PROB_CHAR_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_CHAR_VALUE_KEY));
        PROB_INT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_INT_VALUE_KEY));
        PROB_BOOL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_BOOL_VALUE_KEY));
        PROB_FLOAT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_FLOAT_VALUE_KEY));
        PROB_DOUBLE_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_DOUBLE_VALUE_KEY));
        PROB_LONG_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_LONG_VALUE_KEY));
        PROB_SHORT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_SHORT_VALUE_KEY));
        PROB_OBJECT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_OBJECT_VALUE_KEY));
        PROB_TYPE_GROUP.put(TypeProvider.charType, PROB_CHAR_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.intType, PROB_INT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.booleanType, PROB_BOOL_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.floatType, PROB_FLOAT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.doubleType, PROB_DOUBLE_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.longType, PROB_LONG_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.shortType, PROB_SHORT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.refType, PROB_OBJECT_VALUE);

        PROB_NULL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_NULL_VALUE_KEY));

        PROB_GLOBAL_FIELD = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_GLOBAL_FIELD_KEY));
        PROB_NEW_ARRAY = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_NEW_ARRAY_KEY));
        PROB_REUSE_VAR = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_REUSE_VAR_KEY));
        PROB_REUSE_REF_VAR = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_REUSE_REF_VAR_KEY));

        PROB_VOID_METHOD = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_VOID_METHOD_KEY));
        PROB_STATIC_FIELD = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_STATIC_FIELD_KEY));
        PROB_VOLATILE_FIELD = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_VOLATILE_FIELD_KEY));

        PROB_API_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_API_VALUE_KEY));
        PROB_FUNC_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_FUNC_VALUE_KEY));
        PROB_ARITH_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_ARITH_VALUE_KEY));
        PROB_ARRAY_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_ARRAY_VALUE_KEY));
        PROB_IF_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_IF_VALUE_KEY));
        PROB_LOOP_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_LOOP_VALUE_KEY));
        PROB_SWITCH_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_SWITCH_VALUE_KEY));
        PROB_TRAP_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_TRAP_VALUE_KEY));

        PROB_API_INTERNAL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_API_INTERNAL_VALUE_KEY));
        PROB_ARITH_INTERNAL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_ARITH_INTERNAL_VALUE_KEY));
        PROB_ARRAY_INTERNAL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_ARRAY_INTERNAL_VALUE_KEY));

        PROB_RETURN_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_RETURN_VALUE_KEY));
        PROB_BREAK_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_BREAK_VALUE_KEY));
        PROB_GOTO_VALUE = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_GOTO_VALUE_KEY));
        PROB_REUSE_INST = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_REUSE_INST_KEY));

        PROB_STATIC_METHOD_INVOCATION = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_STATIC_METHOD_INVOCATION_KEY));
        PROB_SELF_METHOD_INVOCATION = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_SELF_METHOD_INVOCATION_KEY));

        PROB_INITIALIZE_METHOD = Integer.parseInt(fuzzingProperties.getProperty(FuzzingProperties.PROB_INITIALIZE_METHOD_VALUE_KEY));

        storePropertyValues(fuzzingProperties.getProperty(FuzzingProperties.INVALID_REFERENCE_TYPE_KEY), INVALID_REFERENCE_TYPE);
        storePropertyValues(fuzzingProperties.getProperty(FuzzingProperties.INVALID_EXCEPTION_TYPE_KEY), INVALID_EXCEPTION_TYPE);

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

    public static long getRandomSeed() {
        return randomSeed;
    }

    public static void setRandomSeed(long seed) {
        randomSeed = seed;
    }

    public static void updateSeed(long seed) {
        if (controlRandom != null) {
            controlRandom.setSeed(seed);
            getRandom(RandomType.DEFAULT).setSeed(controlRandom.nextLong());
            getRandom(RandomType.SEED_POOL).setSeed(controlRandom.nextLong());
            getRandom(RandomType.OPERATOR_POOL).setSeed(controlRandom.nextLong());
            getRandom(RandomType.OPTION_POOL).setSeed(controlRandom.nextLong());
        }
    }

    public static Random getRandom(RandomType randomType) {
        switch (randomType) {
            case CONTROL:
                return getControlRandom();
            case SEED_POOL:
                if (seedPoolRandom == null) {
                    long seed = nextControlRandom();
                    seedPoolRandom = new Random(seed);
                }
                return seedPoolRandom;
            case OPERATOR_POOL:
                if (operatorPoolRandom == null) {
                    long seed = nextControlRandom();
                    operatorPoolRandom = new Random(seed);
                }
                return operatorPoolRandom;
            case OPTION_POOL:
                if (optionPoolRandom == null) {
                    long seed = nextControlRandom();
                    optionPoolRandom = new Random(seed);
                }
                return optionPoolRandom;
            default:
                if (defaultRandom == null) {
                    long seed = nextControlRandom();
                    defaultRandom = new Random(seed);
                }
                return defaultRandom;
        }
    }

    public static Random getDefaultRandom() {

        if (defaultRandom != null) {
            return defaultRandom;
        } else {
            if (randomSeed == -1) {
                long seed = System.currentTimeMillis();
                defaultRandom = new Random(seed);
            } else {
                defaultRandom = new Random(randomSeed);
            }
        }
        return defaultRandom;
    }

    public static Random getControlRandom() {
        if (controlRandom != null) {
            return controlRandom;
        } else {
            if (randomSeed == -1) {
                long seed = System.currentTimeMillis();
                controlRandom = new Random(seed);
            } else {
                controlRandom = new Random(randomSeed);
            }
        }
        return controlRandom;
    }

    public static long nextControlRandom() {
        long randomSeed = getControlRandom().nextLong();
        if (DTGlobal.getRandomLogger() != null) {
            DTGlobal.getRandomLogger().info("random seed: " + randomSeed + " randomType: control");
        }
        return randomSeed;
    }

    @Test
    public void test() {
        randomUpTo(FuzzingConfig.PROB_ARITH_GROUP);
    }

    /**
     * roulette wheel selection
     *
     * @param group
     * @return
     */
    public static <K> K randomUpTo(HashMap<K, Integer> group) {

        List<Map.Entry<K, Integer>> values = new ArrayList<>(group.entrySet());
        Collections.sort(values, new Comparator<Map.Entry<K, Integer>>() {
            @Override
            public int compare(Map.Entry<K, Integer> o1, Map.Entry<K, Integer> o2) {
                return (o1.getValue() - o2.getValue());
            }
        });
        int choice = nextChoice(100);
        for (Map.Entry<K, Integer> value : values) {
            if (choice < value.getValue()) {
                return value.getKey();
            }
        }
        //this should not happen
        return (K) group.keySet().toArray()[nextChoice(group.keySet().size())];
    }

    public static <K> K randomUpTo(HashMap<K, Integer> group, RandomType randomType) {

        List<Map.Entry<K, Integer>> values = new ArrayList<>(group.entrySet());
        Collections.sort(values, new Comparator<Map.Entry<K, Integer>>() {
            @Override
            public int compare(Map.Entry<K, Integer> o1, Map.Entry<K, Integer> o2) {
                return (o1.getValue() - o2.getValue());
            }
        });
        int choice = nextChoice(100, randomType);
        for (Map.Entry<K, Integer> value : values) {
            if (choice < value.getValue()) {
                return value.getKey();
            }
        }
        //this should not happen
        return (K) group.keySet().toArray()[nextChoice(group.keySet().size(), randomType)];
    }

    public static boolean randomUpTo(int prob) {
        return nextChoice(100) < prob;
    }

    public static int nextChoice(int bound) {

        if (bound == 0) {
            return 0;
        }
        if (defaultRandom == null) {
            getDefaultRandom();
        }
        return defaultRandom.nextInt(bound);
    }

    public static boolean flipCoin(int prob) {
        if (defaultRandom == null) {
            getDefaultRandom();
        }
        return defaultRandom.nextInt(100) < prob;
    }

    public static boolean flipCoin() {
        if (defaultRandom == null) {
            getDefaultRandom();
        }
        if (probability <= 0) {
            probability = 50;
        }
        int prob = defaultRandom.nextInt(100);
        return prob < probability;
    }

    public static boolean randomUpTo(int prob, RandomType randomType) {
        return nextChoice(100, randomType) < prob;
    }

    public static int nextChoice(int bound, RandomType randomType) {

        if (bound == 0) {
            return 0;
        }
        Random random = getRandom(randomType);
        int position = random.nextInt(bound);
        if (DTGlobal.getRandomLogger() != null) {
            DTGlobal.getRandomLogger().info("bound size: " + bound + " . position: " + position + " randomType: " + randomType);
        }
        return position;
    }

    public static boolean flipCoin(int prob, RandomType randomType) {
        Random random = getRandom(randomType);
        return random.nextInt(100) < prob;
    }

    public static boolean flipCoin(RandomType randomType) {
        Random random = getRandom(randomType);
        if (probability <= 0) {
            probability = 50;
        }
        int prob = random.nextInt(100);
        return prob < probability;
    }

    public static int getProbability() {
        return probability;
    }

    public static void setProbability(int probability) {
        FuzzingConfig.probability = probability;
    }
}
