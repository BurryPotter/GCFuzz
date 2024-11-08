public class InternedStringBenchmark {
    public static int numStrings = 1000000;
    public static String[] strings = new String[numStrings];
    public static Object dummy;

    public static void main(String [] args) {
        for (int i = 0; i < numStrings; i++) {
            strings[i] = String.valueOf(i).intern();
        }
        System.out.println("Interned done");
        // provoke young GCs
        while (true) {
            dummy = new byte[2 * 1024];
        }
    }
}