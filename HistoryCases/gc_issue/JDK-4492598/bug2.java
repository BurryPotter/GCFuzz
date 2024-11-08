public class bug2 {

    bug2 next;

    public static void main(String[] args) throws Exception {

        bug2 p, q;
        int count = 0;
        int reportGranularity = 1000;

        if (args.length > 0)
          reportGranularity = Integer.parseInt(args[0]);
        p = new bug2 ();
        try {
            while (p != null) {
                q = new bug2 ();
                q.next = p;
                p = q;
                if (((count % 100000) == 0) ||
                     (count > 4000000 && (count % reportGranularity) == 0))
                  System.out.println(" " + count);
                count++;
            }
            System.out.println("OutOfMemoryError not thrown as expected.");
            System.exit(97/*STATUS_FAILED*/);
        } catch (OutOfMemoryError e) {
            p = null;
            q = null;
            System.exit(95/*STATUS_PASSED*/);
        }
    }
}