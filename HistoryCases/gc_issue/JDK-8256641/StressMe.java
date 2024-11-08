public class StressMe {
    volatile static int count = 200 * 10;
    static boolean DEBUG;
    public static void main(String args[]) {
        DEBUG = args.length > 0;
        if (DEBUG) {
            System.out.println("start");
        }
        for (int i = 0; i < 20; i++) {
            spawn();
        }
        while (count > 180 * 10) {}
        System.out.println("Loading jdk/internal/math/FDBigInteger: " + Float.toString(1.0f));
        System.out.println("Remaining threads " + count);
    }
    public static void spawn() {
        if (DEBUG) {
            System.out.println(count);
        }
        if (count > 0) {
            Object o = new int[10];
            Thread t = new Thread() {
                    public void run() {
                        count --;
                        spawn();
                    }
                };
            t.setDaemon(true);
            t.start();
        }
    }
}