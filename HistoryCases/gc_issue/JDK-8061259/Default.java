import java.util.LinkedList;
class PromoFail {

  static class Container {

    Container p;
    byte[] a;

    public Container(int size) {
      if (size > 0) {
        p = new Container(size / 2);
      } else {
        p = null;
      }
      a = new byte[size];
    }
  }

  public static void main(String args[]) {
    if (args.length < 1) {
      System.err.println("@ 1st argument must be size in MB.");
      System.exit(1);
    }
    int size = 0;
    try {
      size = Integer.parseInt(args[0]) * 1024 * 1024;
    } catch (NumberFormatException e) {
      System.err.println("@ Cannot parse the size(=" + args[0] + ")");
      System.exit(1);
    }

    // LinkedList will have more unbalanced workload.
    LinkedList<Container> list = new LinkedList<Container>();

    // 1st iteration adds element without removal.
    // These are all live objects.
    for (int i = 0; i < size / 4; i++) {
      list.add(new Container(1));
    }
    // Promote to the old gen.
    System.gc();

    for (int container_size = 2; container_size < 512; container_size *= 3) {
      for (int i = 0; i < size / 4; i++) {
        // Most likely removing an old object due to System.gc() from previous iteration.
        // This will cause fragmentation.
        list.remove();
        list.add(new Container(container_size));
      }

      {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        System.out.println("@ Current Used: "
            + (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
      }
    }
  }
}