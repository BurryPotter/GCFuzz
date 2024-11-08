public class TestThrowableOom {

    static class ThrowOom {
        ThrowOom next;
    };
  
    static ThrowOom memoryEater() {
        ThrowOom list = new ThrowOom();
        for (int i = 0; i< Integer.MAX_VALUE; i++) {
            list.next = new ThrowOom();
            list = list.next;
        }
        return list;
    }
  
    public static void main(String... unused) throws Exception {
      ThrowOom t = memoryEater();
    }
  }