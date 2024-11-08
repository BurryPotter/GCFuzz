public class Crasher {
	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < 60000) {
			byte a[][] = new byte[100000][];
			try {
				for(int i = 0; i<a.length && System.currentTimeMillis() - startTime < 60000; i++) {
					a[i] = new byte[100000];
				}
			} catch (OutOfMemoryError oome) {
				a = null;
				continue;
			}
		}
	}
}
