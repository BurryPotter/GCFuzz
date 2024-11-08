import java.util.*;

public class CoreGenerator implements Runnable
{
   static Random generator = new Random();
   private String genString( int length )
   {
      final byte space = 32;
      final int range = 128 - 32;
         char newMessage[] = new char[ length ];
         for ( int j = 0; j < length; j++ )
         {
            newMessage[ j ] = (char)( space + (byte) generator.nextInt( range ) );
         }
         return new String( newMessage );
   }
   public void run()
   {
      HashMap hm = new HashMap( 1024 );
      final int maxLength = 4 * 1024 * 1024;
      for ( int i = 1; i < 2 * 1024 * 1024; i++ )
      {
         hm.put( new Integer( i ), genString( generator.nextInt( maxLength ) ) );
         if (0 == ( i % 20 ) )
         {
            hm.put( new Integer( generator.nextInt( i ) ), genString( generator.nextInt( maxLength ) ) );
         }
      }
   }
   public static void main( String args[] )
   {
      for ( int i = 0; i < 20; i++ )
      {
         new Thread( new CoreGenerator() ).start();
      }
   }
}