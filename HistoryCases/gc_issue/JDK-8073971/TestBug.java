import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

public class TestBug {
    private static long test() throws Exception {
        long count = 0;
        ReferenceQueue<Object> q = new ReferenceQueue<Object>();

        {
            Object obj = new Object();
            WeakReference<Object> ref = new WeakReference<Object>(obj, q);
            obj = null;
        }

        while (q.poll() == null) {
            count++;
        }

        return count;
    }

    public static void main(String[] args) throws Exception {
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }

                    System.gc();
                }
            }
        }.start();

        long count = 0;

        for (int i = 0; i < 400; i++) {
            count += test();
            System.out.println("----> Iteration #" + i + ", count = " + count);
        }

        System.out.println(count);
    }
}