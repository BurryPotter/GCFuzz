public class Repro {

    public static Object o;

    public static void main(String args[]) throws Exception {
        Thread t = new Thread(()->{
                while(true) {
                    o = new byte[8*1024*1024];
                }
            });
        t.setDaemon(true);
        t.start();

        Thread t2 = new Thread(()->{
                System.out.println("Exiting");
                System.exit(1);
            });

        t2.setDaemon(true);
        t2.start();
    }
}

