package codegen.providers;

import soot.Modifier;
import config.FuzzingConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *  提供一些生成时用的的修饰符
 */
public class ModifierProvider {

    public static List<Integer> accessModifiers = new ArrayList<>();
    static {
        accessModifiers.add(Modifier.PUBLIC);
        accessModifiers.add(Modifier.PRIVATE);
        accessModifiers.add(Modifier.PROTECTED);
    }

    public static int nextFieldModifier() {
        int modifier = accessModifiers.get(FuzzingConfig.nextChoice(accessModifiers.size()));
        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_STATIC_FIELD)) modifier |= Modifier.STATIC;
        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_VOLATILE_FIELD)) modifier |= Modifier.VOLATILE;
        return modifier;
    }

    public static int nextMethodModifier() {
        int modifier = accessModifiers.get(FuzzingConfig.nextChoice(accessModifiers.size()));
        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_STATIC_FIELD)) modifier |= Modifier.STATIC;
        return modifier;
    }
}
