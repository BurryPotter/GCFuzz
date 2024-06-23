package cases;

import java.util.ArrayList;
import java.util.Random;

public class MMR_Case1 {
    public static void main(String[] args) {
        ArrayList list = new ArrayList();
        Random random = new Random(-1L);
        int length = args.length;
        if (length > 0) {
            args[random.nextInt(length)] = new String();
        }
        Object param = new Object();
        list.set(0, param);
    }
}
