import java.util.LinkedList;

public class Repro2 {

    public static LinkedList<byte[]> dummyStore = new LinkedList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Started");
        for (int i = 0; i < 1024*16; i++) {
            dummyStore.add(new byte[1024]);
        }
        System.out.println("Triggered one YC");

        Thread thread = new Thread(()->System.exit(0));
        thread.start();
        Thread.sleep(100);

        for (int i = 0; i < 1024*16; i++) {
            dummyStore.add(new byte[1024]);
        }
        System.out.println("Triggered Initial mark");

        System.gc(); // Full GG

        System.out.println("Done.");
    }
}
