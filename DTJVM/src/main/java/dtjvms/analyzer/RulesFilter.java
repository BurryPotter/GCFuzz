package dtjvms.analyzer;

import dtjvms.executor.JvmOutput;

import java.util.HashMap;

public class RulesFilter extends Filter{

    @Override
    protected void doFilter(HashMap<String, JvmOutput> results) {
        System.out.println("This is RulesFilter");
    }
}
