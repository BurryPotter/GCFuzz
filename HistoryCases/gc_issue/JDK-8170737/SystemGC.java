public class SystemGC {
    public static void main(String args[]) {
        Runtime r = Runtime.getRuntime();
        System.out.println("1: " + r.freeMemory());
        System.gc();
        System.out.println("2: " + r.freeMemory());
    }
}