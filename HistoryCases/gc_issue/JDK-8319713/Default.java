class pgc_full_gc {
  static class MyObject {
    int x;
    Object obj;
  }

  static int num_obj = 80_000_000;

  static MyObject[] obj_arr;

  public static void main(String[] arg) {
    obj_arr = new MyObject[num_obj];

    for (int i = 0; i < num_obj; i++) {
      obj_arr[i] = new MyObject();
    }
  }
}
