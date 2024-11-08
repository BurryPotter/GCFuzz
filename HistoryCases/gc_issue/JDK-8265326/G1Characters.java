public class G1Characters {
  private static Object leak;
  public static void main(String... args) {
    while(true) {
      leak = new byte[10_000_000];
    }
  }
}
