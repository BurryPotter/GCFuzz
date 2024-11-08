import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    static void create_new_element(int index, Random rand) {
        arr[index] = new int[1_000_0 + rand.nextInt(10)];
    }

    static void create_large_element(int index, Random rand) {
        large_arr = new int[1_000_1000 / 4 + rand.nextInt(10)];
    }

    static int[][] arr;
    static int arr_length = 4_000_0;
    static int iterations = 2_000_00;
    static int[] large_arr;

    static void setup() {
        Random rand = new Random(17);
        arr = new int[arr_length][];
        for (int i = 0; i < arr.length; i++) {
            create_new_element(i, rand);
        }
        System.gc();
    }

    static void trigger_gc() {
        Random rand = new Random(13);

        for (int i = 0; i < iterations; i++) {
            var index = rand.nextInt(arr_length);
            create_new_element(index, rand);
            if (i % 1000 == 0) {
                create_large_element(index, rand);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        setup();
        trigger_gc();
    }
}
