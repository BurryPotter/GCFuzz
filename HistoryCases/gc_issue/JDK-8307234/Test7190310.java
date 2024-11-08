import java.lang.ref.*;

public class Test7190310 {

    private static Object str = new Object() {

        public String toString() {
            return "The Object";
        }

        protected void finalize() throws Throwable {
            System.out.println("The Object is being finalized");
            super.finalize();
        }
    };

    private final static ReferenceQueue<Object> rq = new ReferenceQueue<Object>();

    private final static WeakReference<Object> wr = new WeakReference<Object>(str, rq);

    public static void main(String[] args) throws InterruptedException {
        Thread reader = new Thread() {

            public void run() {
//方法已经for语句变异
for (int newIndex0 = 0; newIndex0 < 20; ++newIndex0)
                while (wr.get() != null) {
                }
                System.out.println("wr.get() returned null");
            }
        };
        Thread queueReader = new Thread() {

            public void run() {
                try {
                    Reference<? extends Object> ref = rq.remove();
                    System.out.println(ref);
                    System.out.println("queueReader returned, ref==wr is " + (ref == wr));
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted - exiting");
                }
            }
        };
        reader.start();
        queueReader.start();
        Thread.sleep(1000);
        str = null;
        System.gc();
    }
} 
