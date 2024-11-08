
import java.util.ArrayList;
import java.util.List;

/**
 * GC benchmark producing a mix of lifetime=0 and lifetime>0 objects which are kept in randomly updated lists. 
 * 
 * @author Andreas Mueller
 */
public class MixedRandomList {
    private static final int DEFAULT_NUMBEROFTHREADS=1;
    // object size in bytes
    private static final int DEFAULT_OBJECTSIZE=100;
    
    private static int numberOfThreads=DEFAULT_NUMBEROFTHREADS;
    private static int objectSize=DEFAULT_OBJECTSIZE;
    // number of objects to fill half of the available memory with (permanent) live objects
    private static long numLive = (Runtime.getRuntime().maxMemory()/objectSize/5);
        
    /**
     * @param args the command line arguments
     */ 
    public static void main(String[] args) {
        if( args.length>0 ) {
            // first, optional argument is the size of the objects
            objectSize = Integer.parseInt(args[0]);
            // second, optional argument is the number of live objects
            if( args.length>1 ) {
                numberOfThreads = Integer.parseInt(args[1]);
                // third, optional argument is the number of live objects
                if( args.length>2 ) {
                    numLive = Long.parseLong(args[2]);
                }
            }
        }
        for( int i=0; i<numberOfThreads; i++ ) {
            // run several GarbageProducer threads, each with its own mix of lifetime=0 and higher lifetime objects
            new Thread(new GarbageProducer((int)Math.pow(50.0,(double)(i+1)), numLive/numberOfThreads)).start();
        }
        try {
            Thread.sleep(1200000);
        } catch( InterruptedException iexc) {
            iexc.printStackTrace();
        }
        System.exit(0);
    }
    
    private static char[] getCharArray(int length) {
        char[] retVal = new char[length];
        for(int i=0; i<length; i++ ) {
            retVal[i] = 'a';
        }
        return retVal;
    }
    
    public static class GarbageProducer implements Runnable {
        
        // the fraction of newly created objects that do not become garbage immediately but are stored in the liveList
        int fractionLive;
        // the size of the liveList
        long myNumLive;
        
        /**
         * Each GarbageProducer creates objects that become garbage immediately (lifetime=0) and 
         * objects that become garbage only after a lifetime>0 which is distributed about an average lifetime. 
         * This average lifetime is a function of fractionLive and numLive 
         * 
         * @param fractionLive
         * @param numLive 
         */
        public GarbageProducer(int fractionLive, long numLive) {
            this.fractionLive = fractionLive;
            this.myNumLive = numLive;
        }

        @Override
        public void run() {
            int osize = objectSize; 
            char[] chars = getCharArray(objectSize);
            List<String> liveList = new ArrayList<String>((int)myNumLive);
            // initially, the lifeList is filled
            for(int i=0; i<myNumLive; i++) {
                liveList.add(new String(chars));
            }
            while(true) {
                // create the majority of objects as garbage
                for(int i=0; i<fractionLive; i++) {
                    String garbageObject = new String(chars);
                }
                // keep the fraction of objects live by placing them in the list (at a random index)
                int index = (int)(Math.random()*myNumLive);
                liveList.set(index, new String(chars));
            }
        }
    }
    
    
    
}
