import java.util.*;

public class Test8000311 {
  public static void main(String args[]) {
    for(int i = 0; i<100; i++) {
      byte[] garbage = new byte[1000];
      System.gc();
    }
  }
}
