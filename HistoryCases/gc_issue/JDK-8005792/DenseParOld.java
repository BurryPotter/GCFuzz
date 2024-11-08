public class DenseParOld {
  public static void main(String [] args) {
    int size = 10000000;
    Integer x[] = new Integer [size];
    Integer y[] = new Integer [size];

    for (int j = 0; j < 10; j++) {
      // Allocate enough objects to cause a minor collection.
      // These allocations suffice for a 4m young geneneration.
      for (int i = 0; i < size; i++) {
        x[i] = new Integer(i);
        y[i] = new Integer(1);
      }
      System.gc();
      System.gc();
      for (int i = 0; i < size; i++) {
        x[i] = y[i];
      }
      System.gc();
      System.gc();
      for (int i = 0; i < size; i++) {
        y[i] = 1;
      }
      System.gc();
      System.gc();
    }
  }
}
