import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

public class Main {
    static int exec(Class<?> clazz, List<String> jvmArgs) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getName();

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(className);

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.inheritIO().start();
        return process.waitFor();
    }


    static class Obj {
        public int x;

        Obj(int x) {
            this.x = x;
        }
    }

    static class Node {
        public Node next;
        public Obj[] arr;
    }

    static Node start = null;
    static WeakReference<Obj>[] weak_refs;

    static Object[] garbage;

    static int iterations = 1_00_000_000;
    static int garbage_length = 20_000_000;

    static int list_length = 1_0_000;
    // static int list_length = 10_000_000;
    static int arr_length = 3_000_000;

    static void setup() {
        start = new Node();
        var cur = start;
        for (int i = 0; i < list_length; i++) {
            cur.next = new Node();
            cur = cur.next;
        }
        cur.arr = new Obj[arr_length];
        weak_refs = new WeakReference[arr_length];
        for (int i = 0; i < arr_length; i++) {
            cur.arr[i] = new Obj(i);
            weak_refs[i] = new WeakReference<>(cur.arr[i]);
        }
        // shuffle the weak_refs array
        Random rand = new Random(17);
        for (int i = 0; i < arr_length; i++) {
            var index = rand.nextInt(arr_length);
            var tmp = weak_refs[index];
            weak_refs[index] = weak_refs[i];
            weak_refs[i] = tmp;
        }
    }

    static void trigger_gc() {
        Random rand = new Random(13);

        garbage = new Object[garbage_length];
        for (int i = 0; i < garbage_length; i++) {
            garbage[i] = new int[10];
            // garbage[i] = new Obj(i);
        }
        System.gc();

        for (int i = 0; i < iterations; i++) {
            var index = rand.nextInt(garbage_length);
            // garbage[index] = new int[rand.nextInt(10)+1];
            garbage[index] = new int[10];
            // garbage[index] = new Obj(i);
        }
    }

    public static void main(String[] args) throws Exception {
        setup();
        trigger_gc();
    }
}
