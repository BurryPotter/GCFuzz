package test;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            byte[][] bytes = new byte[1024 * 1024][];
            try {
                for (int j = 0; j < bytes.length; j++) {
                    bytes[j] = new byte[1024 * 1024];
                }
            } catch (OutOfMemoryError error) {
                bytes = null;
                error.printStackTrace();
            }
        }
    }
}
