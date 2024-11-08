public class Test {
	public static void main(String args[]) {
		Object garbage[] = new Object[1000000];
		for (int i = 0; i < garbage.length; i++) {
			garbage[i] = new byte[0];
		}
		for (int i = 0; i < 16; i++) {
			System.gc();
		}
	}
}
