// To stress large-array dirty-card scanning
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2
class card_scan {
  static Object[] bigobj = new Object[1024 * 1024 * 1024 / 4 - 20];

  static final int stride = 32;

  static int[] garbage;

  static final int loop_count = 20;

  public static void main(String[] args) {
    for (int i = 0; i < loop_count; i++) {
      Object tmp = new Object();
      for (int j = 0; j < bigobj.length; j += stride) {
        bigobj[j] = tmp;
      }
      int garbage_loop = 8_000 * 32;
      for (int j = 0; j < garbage_loop; ++j) {
          garbage = new int[100];
      }
    }
  }
}
