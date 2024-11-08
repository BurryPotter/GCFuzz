import java.util.ArrayList;

public class AllocTest {
   public static ArrayList list = new ArrayList(0);

   public static void main(String[] args) {
      for (int i = 0; i < 10 * 1024 * 1024; i++) {
         list.add(new byte[30]);
      }
   }
}