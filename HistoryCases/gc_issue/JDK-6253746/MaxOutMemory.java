import java.util.*;

public class MaxOutMemory {
  public MaxOutMemory() {
    System.out.println("Starting array allocation");
    Collection list = new ArrayList();
    try {
      for (int i = 0; i < 10000000; i++) {
        list.add(new String[100000]);
        if (i % 400 == 0) {
          System.out.println("Executing GC i=" + i);
          System.gc();
        }
      }
     }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  public static void main(String args[]) {
      MaxOutMemory m = new MaxOutMemory();

      System.exit(95);
  }
}