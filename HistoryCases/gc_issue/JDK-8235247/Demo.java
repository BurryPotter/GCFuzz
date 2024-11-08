
import java.lang.*;


public class Demo {
static class Worker implements Runnable {
    static volatile Object o;
    public void run() {
        while (true) {
            o = new Object();
            System.gc();
        }
    }
}

  public static void main(String[] args) throws Exception {
      for (int i = 0; i < 10; i++) {
         Thread thread = new Thread(new Worker());
         thread.start();
      }
      while (true) {
        Thread.sleep(6000);
      }
  }
}