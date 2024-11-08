public class b8065402 {
  public static void main(String [] args)
  {
    if (args.length != 1) {
      System.err.println("Usage: b8065402 <number of roots");
      return;
    }

    int num = Integer.valueOf(args[0]);
  
    long start = System.currentTimeMillis();

    b8065402 [] arr = new b8065402[num];
    for(int i = 0, j = 0; i < 1000; i++, j++)
    {
      if (j == num)
        j = 0;
      String nm = "root_" + i + "_";
      System.out.println("******** " +nm);
      arr[j] = create(nm, 1000000);
    } 
    double duration = (System.currentTimeMillis() - start) / 1000.0;
    System.out.println("******** duration " + duration + " sec.");
  }

  public String name;

  public b8065402 next;

  public b8065402(String nm) {
    name = nm;
  }

  public static b8065402 create (String nm, int len)
  {
    b8065402 [] tmp = new b8065402[len];
    b8065402 prev = null;
    for(int i = 0; i < len; i++)
    {
      b8065402 cur = new b8065402(nm + i);
      tmp[i] = cur;
      if (prev != null)
        prev.next = cur;
      
      prev = cur;
    }
    return tmp[0];
  }
}
