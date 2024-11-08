import java.lang.ref.SoftReference;

public class TestEvacFailureThreaded {

    static {
        System.loadLibrary("TestEvacFailureThreaded");
    }

    static int wu_threads;
    static int main_threads;
    static long wu_size;
    static int wu_iter;

    static long main_size;
    static int main_iter;
    
    static double alive_fraq;

    public static volatile Object tmp;

    public static volatile boolean cont = true;

    private static native int TestCriticalArray0(int[] x);

    public static class Node {
        public SoftReference<Node> next;
        long payload1;
        long payload2;
        long payload3;
        long payload4;

        public Node(int load) {
            payload1 = payload2 = payload3 = payload4 = load;
        }
    }

    public static void warmUp(long pre, int size) {
        // First let the GC assume most of our objects will die.
        Node[] roots = new Node[size];

        for (long i = 0; i < pre; ++i) {
            int index = (int) (Math.random() * roots.length);
            roots[index] = new Node(1);
        }

        // Make sure the young generation is empty.
        for (int i = 0; i < roots.length; ++i) {
            roots[i] = null;
        }
    }

    public static void runTest(long post, int size, double alive) {
        final int length = 10000;
        int[] array1 = new int[length];
        for (int x = 1; x < length; x++) {
            array1[x] = x;
        }

        Node[] roots = new Node[size];
        try {
            int index = 0;
            roots[0] = new Node(0);

            int i = 0;
            while (cont && (i < post)) {
                i++;
                int test_val1 = TestCriticalArray0(array1);

                if (Math.random() > alive) {
                    tmp = new Node(test_val1);
                } else {
                    index = (int) (Math.random() * roots.length);

                    if (roots[index] != null) {
                        Node node = new Node(test_val1);
                        node.next = new SoftReference<Node>(roots[index]);
                        roots[index] = node;
                    } else {
                        roots[index] = new Node(test_val1);
                    }
                }

            }
        } catch (OutOfMemoryError e) {
            cont = false;
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 7){
            System.out.println("Usage: java TestEvacFailureThreaded <numThreads> <wu size> <wu iter> <size> <iter> <aliveFraq>");
            System.exit(0);
        }
        
        wu_threads = Integer.parseInt(args[0]);
        System.out.println("wu_threads = " + wu_threads);
        wu_size = Long.parseLong(args[1]);
        System.out.println("wu_size = " + wu_size);
        wu_iter = Integer.parseInt(args[2]);
        System.out.println("wu_iter = "+ wu_iter);
        main_size = Long.parseLong(args[3]);
        System.out.println("main_size = " + main_size);
        main_threads = Integer.parseInt(args[4]);
        System.out.println("main_threads = " + main_threads);
        
        
        main_iter = Integer.parseInt(args[5]);
        System.out.println("main_iter = " + main_iter);
        alive_fraq = Double.parseDouble(args[6]);
        System.out.println(" alive_fraq = " + alive_fraq);
        
        int max_threads = Math.max(wu_threads, main_threads);
        Thread threads[] = new Thread[max_threads];
        System.out.println("Start warm-up threads!");
        for (int i = 0; i < wu_threads; i++) {
            threads[i] = new Thread() {
                public void run() {
                    warmUp(wu_size, wu_iter);
                };
            };
            threads[i].start();
        }
        
        for (int i = 0; i < wu_threads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.gc();
        System.out.println("Keep alive a lot");
        for (int i = 0; i < main_threads; i++) {
            threads[i] = new Thread() {
                public void run() {
                    runTest(main_size, main_iter, alive_fraq);
                };
            };
            threads[i].start();
        }
    }
}
