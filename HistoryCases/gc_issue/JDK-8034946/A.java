import java.lang.ref.*;


public class A {

	public static void main(String[] args) throws Exception {
		main1();
		main2();
	}

	private static void main1() throws Exception {
		ReferenceQueue rq = new ReferenceQueue();
		
		Object o = new Object();
		
		PhantomReference r = new MyPhantomReference(o, rq); // Only difference between main1 and main2 is in this row
		
		o = null;
		
		int cnt = 0;
		while (rq.poll() == null) {
			System.out.println("Attempt " + cnt++);
			eatMemory();
			System.gc();
			System.runFinalization();
			Thread.sleep(300);
		}
		
		System.out.println("main1 successfully finished");
	}
	
	private static void main2() throws Exception {
		ReferenceQueue rq = new ReferenceQueue();
		
		Object o = new Object();
		
		new MyPhantomReference(o, rq); // Only difference between main1 and main2 is in this row
		
		o = null;
		
		int cnt = 0;
		while (rq.poll() == null) {
			System.out.println("Attempt " + cnt++);
			eatMemory();
			System.gc();
			System.runFinalization();
			Thread.sleep(300);
		}
		
		System.out.println("main2 successfully finished");
	}

	private static void eatMemory() {
		byte[] b;
		for (int i = 0; i < 3000; i++) {
			b = new byte[1024];
		}
	}
	
	private static class MyPhantomReference extends PhantomReference {
		public MyPhantomReference(Object referent, ReferenceQueue q) {
			super(referent, q);
			System.out.println("in constructor");
		}
	}

}

/*
 
  private static void main1() throws java.lang.Exception;
    Code:
       0: new           #4                  // class java/lang/ref/ReferenceQueue
       3: dup           
       4: invokespecial #5                  // Method java/lang/ref/ReferenceQueue."<init>":()V
       7: astore_0      
       8: new           #6                  // class java/lang/Object
      11: dup           
      12: invokespecial #1                  // Method java/lang/Object."<init>":()V
      15: astore_1      
      16: new           #7                  // class A$MyPhantomReference
      19: dup           
      20: aload_1       
      21: aload_0       
      22: invokespecial #8                  // Method A$MyPhantomReference."<init>":(Ljava/lang/Object;Ljava/lang/ref/ReferenceQueue;)V
!---> 25: astore_2      
      26: aconst_null   
      27: astore_1      
      28: iconst_0      
      29: istore_3      
      30: aload_0       
      31: invokevirtual #9                  // Method java/lang/ref/ReferenceQueue.poll:()Ljava/lang/ref/Reference;
      34: ifnonnull     83
      37: getstatic     #10                 // Field java/lang/System.out:Ljava/io/PrintStream;
      40: new           #11                 // class java/lang/StringBuilder
      43: dup           
      44: invokespecial #12                 // Method java/lang/StringBuilder."<init>":()V
      47: ldc           #13                 // String Attempt 
      49: invokevirtual #14                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      52: iload_3       
      53: iinc          3, 1
      56: invokevirtual #15                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      59: invokevirtual #16                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      62: invokevirtual #17                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      65: invokestatic  #18                 // Method eatMemory:()V
      68: invokestatic  #19                 // Method java/lang/System.gc:()V
      71: invokestatic  #20                 // Method java/lang/System.runFinalization:()V
      74: ldc2_w        #21                 // long 300l
      77: invokestatic  #23                 // Method java/lang/Thread.sleep:(J)V
      80: goto          30
      83: getstatic     #10                 // Field java/lang/System.out:Ljava/io/PrintStream;
      86: ldc           #24                 // String main1 successfully finished
      88: invokevirtual #17                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      91: return        

   private static void main2() throws java.lang.Exception;
    Code:
       0: new           #4                  // class java/lang/ref/ReferenceQueue
       3: dup           
       4: invokespecial #5                  // Method java/lang/ref/ReferenceQueue."<init>":()V
       7: astore_0      
       8: new           #6                  // class java/lang/Object
      11: dup           
      12: invokespecial #1                  // Method java/lang/Object."<init>":()V
      15: astore_1      
      16: new           #7                  // class A$MyPhantomReference
      19: dup           
      20: aload_1       
      21: aload_0       
      22: invokespecial #8                  // Method A$MyPhantomReference."<init>":(Ljava/lang/Object;Ljava/lang/ref/ReferenceQueue;)V
!---> 25: pop           
      26: aconst_null   
      27: astore_1      
      28: iconst_0      
      29: istore_2      
      30: aload_0       
      31: invokevirtual #9                  // Method java/lang/ref/ReferenceQueue.poll:()Ljava/lang/ref/Reference;
      34: ifnonnull     83
      37: getstatic     #10                 // Field java/lang/System.out:Ljava/io/PrintStream;
      40: new           #11                 // class java/lang/StringBuilder
      43: dup           
      44: invokespecial #12                 // Method java/lang/StringBuilder."<init>":()V
      47: ldc           #13                 // String Attempt 
      49: invokevirtual #14                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      52: iload_2       
      53: iinc          2, 1
      56: invokevirtual #15                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      59: invokevirtual #16                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      62: invokevirtual #17                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      65: invokestatic  #18                 // Method eatMemory:()V
      68: invokestatic  #19                 // Method java/lang/System.gc:()V
      71: invokestatic  #20                 // Method java/lang/System.runFinalization:()V
      74: ldc2_w        #21                 // long 300l
      77: invokestatic  #23                 // Method java/lang/Thread.sleep:(J)V
      80: goto          30
      83: getstatic     #10                 // Field java/lang/System.out:Ljava/io/PrintStream;
      86: ldc           #25                 // String main2 successfully finished
      88: invokevirtual #17                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      91: return        
  
  */
