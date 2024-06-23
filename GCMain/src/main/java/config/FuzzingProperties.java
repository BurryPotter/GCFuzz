package config;


import java.io.InputStream;
import java.util.Properties;

public class FuzzingProperties extends Properties {


    private static FuzzingProperties instance;

    // max value configurations
    public static final String MAX_FIELD_NUM_KEY = "max.field.num";  //done

    public static final String MAX_FUNS_NUM_KEY = "max.funs.num";  //done
    public static final String MAX_FUNS_PARAMS_KEY = "max.fun.params"; //done
    public static final String MAX_METHOD_INVOCATION_KEY = "max.fun.invocation"; //done

    public static final String MAX_ARRAY_DIM_KEY = "max.array.dim"; //done
    public static final String MAX_ARRAY_SIZE_PERDIM_KEY = "max.array.size.perdim"; //done
    public static final String MAX_ARRAY_LENGTH_KEY = "max.array.length"; //done

    public static final String MAX_FUZZ_STEP_KEY = "max.fuzz.step"; //done
    public static final String MAX_NESTED_SIZE_KEY = "max.nested.size"; //done
    public static final String MAX_BLOCK_INST_KEY = "max.block.inst"; //done
    public static final String MAX_LOOP_SIZE_KEY = "max.loop.size"; //done
    public static final String MAX_LOOP_STEP_KEY = "max.loop.step"; //done
    public static final String MAX_SWITCH_CASES_KEY = "max.switch.cases"; //done

    //probability configurations
    public static final String PROB_ADD_VALUE_KEY = "prob.add.value"; //done
    public static final String PROB_SUB_VALUE_KEY = "prob.sub.value"; //done
    public static final String PROB_MUL_VALUE_KEY = "prob.mul.value"; //done
    public static final String PROB_DIV_VALUE_KEY = "prob.div.value"; //done
    public static final String PROB_MOD_VALUE_KEY = "prob.mod.value"; //done
    public static final String PROB_CAST_VALUE_KEY = "prob.cast.value"; //done
    public static final String PROB_OR_VALUE_KEY = "prob.or.value"; //done
    public static final String PROB_AND_VALUE_KEY = "prob.and.value"; //done
    public static final String PROB_XOR_VALUE_KEY = "prob.xor.value"; //done
    public static final String PROB_NEG_VALUE_KEY = "prob.neg.value"; //done
    public static final String PROB_SHL_VALUE_KEY = "prob.shl.value"; //done
    public static final String PROB_SHR_VALUE_KEY = "prob.shr.value"; //done
    public static final String PROB_USHR_VALUE_KEY = "prob.ushr.value"; //done
    public static final String PROB_EQ_VALUE_KEY = "prob.eq.value"; //done
    public static final String PROB_GT_VALUE_KEY = "prob.gt.value"; //done
    public static final String PROB_GE_VALUE_KEY = "prob.ge.value"; //done
    public static final String PROB_LT_VALUE_KEY = "prob.lt.value"; //done
    public static final String PROB_LE_VALUE_KEY = "prob.le.value"; //done

    public static final String PROB_CHAR_VALUE_KEY = "prob.char.value"; //done
    public static final String PROB_INT_VALUE_KEY = "prob.int.value"; //done
    public static final String PROB_BOOL_VALUE_KEY = "prob.bool.value"; //done
    public static final String PROB_FLOAT_VALUE_KEY = "prob.float.value"; //done
    public static final String PROB_DOUBLE_VALUE_KEY = "prob.double.value"; //done
    public static final String PROB_LONG_VALUE_KEY = "prob.long.value"; //done
    public static final String PROB_SHORT_VALUE_KEY = "prob.short.value"; //done
    public static final String PROB_NULL_VALUE_KEY = "prob.null.value"; //done
    public static final String PROB_OBJECT_VALUE_KEY = "prob.object.value"; //done

    public static final String PROB_GLOBAL_FIELD_KEY = "prob.global.field.value"; //done
    public static final String PROB_NEW_ARRAY_KEY = "prob.new.array.value"; //done
    public static final String PROB_REUSE_VAR_KEY = "prob.reuse.var.value"; //done
    public static final String PROB_REUSE_REF_VAR_KEY = "prob.reuse.ref.var.value"; //done

    public static final String PROB_VOID_METHOD_KEY = "prob.void.function.value"; //done
    public static final String PROB_STATIC_FIELD_KEY = "prob.static.field.value"; //done
    public static final String PROB_VOLATILE_FIELD_KEY = "prob.volatile.field.value"; //done

    //group probability
    public static final String PROB_API_VALUE_KEY = "prob.api.value"; //done
    public static final String PROB_FUNC_VALUE_KEY = "prob.func.value"; //done
    public static final String PROB_ARITH_VALUE_KEY = "prob.arith.value"; //done
    public static final String PROB_ARRAY_VALUE_KEY = "prob.array.value"; //done
    public static final String PROB_IF_VALUE_KEY = "prob.if.value"; //done
    public static final String PROB_LOOP_VALUE_KEY = "prob.loop.value"; //done
    public static final String PROB_SWITCH_VALUE_KEY = "prob.switch.value"; //done
    public static final String PROB_TRAP_VALUE_KEY = "prob.trap.value"; //done

    //internal
    public static final String PROB_API_INTERNAL_VALUE_KEY = "prob.api.internal.value"; //done
    public static final String PROB_ARITH_INTERNAL_VALUE_KEY = "prob.arith.internal.value"; //done
    public static final String PROB_ARRAY_INTERNAL_VALUE_KEY = "prob.array.internal.value"; //done

    public static final String PROB_RETURN_VALUE_KEY = "prob.return.value"; //TODO
    public static final String PROB_BREAK_VALUE_KEY = "prob.break.value"; //TODO
    public static final String PROB_GOTO_VALUE_KEY = "prob.goto.value"; //TODO
    public static final String PROB_REUSE_INST_KEY = "prob.reuse.inst"; //partial done

    public static final String PROB_STATIC_METHOD_INVOCATION_KEY = "prob.static.method.invocation"; //done

    public static final String PROB_SELF_METHOD_INVOCATION_KEY = "prob.self.method.invocation"; //done

    public static final String PROB_INITIALIZE_METHOD_VALUE_KEY = "prob.initialize.method.value"; //TODO

    public static final String INVALID_REFERENCE_TYPE_KEY = "invalid.reference.type"; //done
    public static final String INVALID_EXCEPTION_TYPE_KEY = "invalid.exception.type"; //done

    public static final String DEFAULT_PROPERTIES = "/fuzzing.properties";

    public FuzzingProperties() {

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

    public static FuzzingProperties getInstance(){
        if (instance == null){
            instance = new FuzzingProperties();
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
