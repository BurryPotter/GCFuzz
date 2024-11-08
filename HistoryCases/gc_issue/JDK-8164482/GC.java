public class GC {

    private static native long maxObjectInspectionAge();

    public static void main(String[] args) {
	try {
	    System.load("GC.so");
	    System.out.println("GC.so loaded successfully.");
	} catch (Exception e) {
	    System.out.println("Unable to load library! " + e.getMessage());
	}

	while (true) {
	    long age = maxObjectInspectionAge();

	    System.out.println(Long.toHexString(age) + " " + age);

	    System.gc();

	    try {
		Thread.sleep(2000);
	    } catch (InterruptedException e) {
	    }
	}
    }

}
