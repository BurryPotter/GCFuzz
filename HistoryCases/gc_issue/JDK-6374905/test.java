public class test
{
public static void main( String [] args )
{
StringBuffer s = null;
long l = 0;
while( true )
{ s = new StringBuffer( 1024 );
l++;
if( l % 10000 == 0 )
System.out.println( l );
}
}
}