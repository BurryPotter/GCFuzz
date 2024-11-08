
// java -Xlog:gc*=info StringTable

public class Test
{
  static
  {
    System.loadLibrary("test");
  }

  static native void initJWeaks(int count);
  static native void addJWeak(int index, Object object);
  static native void removeJWeak(int index);

  public static void main(String[] args)
  {
    int objects_count = 1000000;
    int gc_loops = 10;

    initJWeaks(objects_count);
    Object[] objects = new Object[objects_count];

    System.err.println("\n");
    System.err.println("#1 allocating memory, creating objects, jweaks ...\n");
    {
      for (int i=0; i<objects_count; i++)
      {
        objects[i] = new Object();
        addJWeak(i, objects[i]);
      }

      long start = System.currentTimeMillis();
      for (int i=0; i<gc_loops; i++)
      {
        long start2 = System.currentTimeMillis();
        System.gc();
        System.err.println("live objects gc time: "+(System.currentTimeMillis() - start2));
      }
      System.err.println("total time: "+(System.currentTimeMillis() - start)+"\n");
    }

    System.err.println("#2 freeing java objects ...\n");
    {
      for (int i=0; i<objects_count; i++)
      {
        objects[i] = null;
      }

      long start = System.currentTimeMillis();
      for (int i=0; i<gc_loops; i++)
      {
        long start2 = System.currentTimeMillis();
        System.gc();
        System.err.println("dead objects gc time: "+(System.currentTimeMillis() - start2));
      }
      System.err.println("total time: "+(System.currentTimeMillis() - start)+"\n");
    }

    System.err.println("#3 freeing jweak references ...\n");
    {
      for (int i=0; i<objects_count; i++)
      {
        removeJWeak(i);
      }

      long start = System.currentTimeMillis();
      for (int i=0; i<gc_loops; i++)
      {
        long start2 = System.currentTimeMillis();
        System.gc();
        System.err.println("destroyed jweaks gc time: "+(System.currentTimeMillis() - start2));
      }
      System.err.println("total time: "+(System.currentTimeMillis() - start)+"\n");
    }
  }
}
  {
        removeJWeak(i);
      }

      long start = System.currentTimeMillis();
      for (int i=0; i<gc_loops; i++)
      {
        long start2 = System.currentTimeMillis();
        System.gc();
        System.err.println("destroyed jweaks gc time: "+(System.currentTimeMillis() - start2));
      }
      System.err.println("total time: "+(System.currentTimeMillis() - start)+"\n");
    }
  }
}
