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
    static WeakReference<Obj>[][] weak_refs_matrix;
    static final int concurrent_threads = 4;

    static Object[] garbage;

    static int iterations = 1_00_000_000;
    static int garbage_length = 20_000_000;

    static int list_length = 1_000_000;
    // static int list_length = 10_000_000;
    // static int arr_length = 3_000_000;
    static int arr_length = 1_000_000;

    static void shuffle(Object[] arr, Random rand) {
        for (int i = 0; i < arr.length; i++) {
            var index = rand.nextInt(arr_length);
            var tmp = arr[index];
            arr[index] = arr[i];
            arr[i] = tmp;
        }
    }

    static void setup() {
        start = new Node();
        var cur = start;
        for (int i = 0; i < list_length; i++) {
            cur.next = new Node();
            cur = cur.next;
        }
        cur.arr = new Obj[arr_length];
        for (int i = 0; i < arr_length; i++) {
            cur.arr[i] = new Obj(i);
        }

        Random rand = new Random(17);
        weak_refs_matrix = new WeakReference[concurrent_threads][];
        for (int i = 0; i < concurrent_threads; i++) {
            weak_refs_matrix[i] = new WeakReference[arr_length];
            for (int j = 0; j < arr_length; j++) {
                weak_refs_matrix[i][j] = new WeakReference<>(cur.arr[j]);
            }

            shuffle(weak_refs_matrix[i], rand);
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

    static void config_jvm(String[] jvm_args) throws Exception {
        String custom_jvm_args = "custom_jvm_args";

        if (System.getProperty(custom_jvm_args) == null) {
            var flags = new ArrayList<>(Arrays.asList(jvm_args));
            flags.add(0, "-D" + custom_jvm_args);
            var ret = exec(Main.class, flags);
            System.exit(ret);
        } else {
            // System.out.println("defined");
        }
    }

    public static void main(String[] args) throws Exception {
        config_jvm(new String[]{
                "-Xms1800m",
                "-Xmx1800m",
                "-XX:+UseG1GC",
                "-XX:ConcGCThreads=1",
                "-XX:ParallelGCThreads=1",
                "-Xlog:gc",
        });

        setup();
        trigger_gc();
    }
}
