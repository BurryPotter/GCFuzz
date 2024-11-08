import java.util.Random;
import java.util.Vector;

public class G1StringTableLeak {
    private static final Random random = new Random();
    private static final char[] characters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    private static final int charArrayLength = characters.length;
    static int i = 0;
   
    static Vector<Object> objects = new Vector<Object>();
    
    public static void main(String[] args) {
        while (true) {
            objects.add(new Object());
            String s = generateRandomString(20).intern();
            objects.add(s);
            if (++i % 10000 == 0) {
                System.out.println("i: " + i);
                if (i % 500000 == 0) {
                        System.out.println("now making most strings unreachable");
                            final Vector<Object> newObjects = new Vector<Object>();
                            for (int j = 0; j < 100; j++) {
                                    newObjects.add(objects.get(j));
                            }
                            objects = newObjects;
                }
            }     
        }
    }
    
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters[random.nextInt(charArrayLength)]);
        }
       return sb.toString();
    }
 
}


