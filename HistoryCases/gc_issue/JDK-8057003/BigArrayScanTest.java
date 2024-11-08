public class BigArrayScanTest {
  static Object[] large;

  public static void main(String[] args) {
    large =  new Object[2 * 1024 * 1024 * 1024 - 10];
    System.out.println(large.length);
    for (int i = 0; i < large.length / 8; i++) {
      large[i] = new Object();
    }
    long i = 0;
    while (i < 40000000) {
      int[] temp = new int[1024];
      i++;
      large[100] = temp;
    }
    System.out.println(large);
  }
}

