public class Test {
  public static void main(String... args) {
     var bean = ManagementFactory.getMemoryMXBean();
     System.out.println("MXBean says: " + bean.getHeapMemoryUsage());
  }
}