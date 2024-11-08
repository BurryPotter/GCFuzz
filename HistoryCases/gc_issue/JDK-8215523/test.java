public class test {
  public static void main(String argv[]) throws Exception {
    java.util.ArrayList list;
    //for (int i=0; i<10; i++) {
    for (int i=0;;i++) {
      list = new java.util.ArrayList();
      for (int j=0; j<1000; j++) {
        for (int k=0; k<10; k++) {
          int[] alive = new int[500];
          //String[] alive = new String[500];
          list.add(alive);
          //System.err.print("o");
        }
        for (int k = 0; k<90; k++) {
          int[] dummy = new int[500];
          //System.err.print("-");
        }
        //System.err.print("\n");
     }
     //System.err.printf("===== %4d ==================================================\n", i);
     System.err.printf("%4d ", i);
    }
  }
}





