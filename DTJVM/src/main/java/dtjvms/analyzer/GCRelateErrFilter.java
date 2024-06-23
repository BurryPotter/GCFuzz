package dtjvms.analyzer;


import dtjvms.executor.JvmOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GCRelateErrFilter extends Filter {
    public static final String[] GC_ERROR = new String[]{"JvmOutput-TIMEOUT", "VMInitError", "OutOfMemoryError", "Segmentation error"};

    @Override
    protected void doFilter(HashMap<String, JvmOutput> results) {
        for (String key : results.keySet()) {
            JvmOutput jvmOut = results.get(key);
            System.out.println(key);
            System.out.println("origin jvm output");
            jvmOut.getErrors().forEach(System.out::println);
            jvmOut.getExceptions().forEach(System.out::println);
            jvmOut.getFailures().forEach(System.out::println);
            List<String> errors = new ArrayList<>(jvmOut.getErrors());
            List<String> exceptions = new ArrayList<>(jvmOut.getExceptions());
            List<String> failures = new ArrayList<>(jvmOut.getFailures());
            jvmOut.getErrors().clear();
            jvmOut.getExceptions().clear();
            jvmOut.getFailures().clear();
            for (String gcError : GC_ERROR) {
                for (String error : errors) {
                    if (error.contains(gcError)){
                        jvmOut.getErrors().add(error);
                    }
                }
                for (String exception : exceptions) {
                    if (exception.contains(gcError)){
                        jvmOut.getExceptions().add(exception);
                    }
                }
                for (String failure : failures) {
                    if (failure.contains(gcError)){
                        jvmOut.getFailures().add(failure);
                    }
                }
            }
            System.out.println("result jvm output");
            jvmOut.getErrors().forEach(System.out::println);
            jvmOut.getExceptions().forEach(System.out::println);
            jvmOut.getFailures().forEach(System.out::println);
        }
    }
}
