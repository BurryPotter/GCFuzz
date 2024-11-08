// -Xms3g -Xmx3g -XX:+UseParallelGC -XX:-ScavengeBeforeFullGC -XX:MarkSweepDeadRatio=0 -XX:NewSize=1g -XX:ParallelGCThreads=2 -Xlog:gc fullgc.java
class fullgc {
	static final int arr_len = 10_000_000;
	static final int num_gc = 4;
	static Object[] objs = new Object[arr_len];

	public static void main(String... args) {
		for (int t = 0; t < num_gc; t++) {
			for (int i = 0; i < arr_len; i++) {
				if ((i + t) % 2 == 0) {
					objs[i] = new Object();
				}
			}
			System.gc();
		}
	}
}
