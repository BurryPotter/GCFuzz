import javax.xml.bind.DatatypeConverter; 
import java.nio.charset.StandardCharsets; 
import java.util.Arrays; 
import java.util.Random; 
import java.util.stream.IntStream; 

public class NewlyAllocatedArrayFilledByOtherInstanceFinalizer 
{ 
    void demonstrate() throws Exception 
    { 
        final int iterations = 1000000; 
        IntStream.range(0, iterations) 
                 .parallel() 
                 .forEach(i -> 
                 { 
                     String expectedValue = randomAlphaNumeric(10); 
                     byte[] expectedBytes = expectedValue.getBytes(StandardCharsets.UTF_8); 
                     ArrayHolder holder = new ArrayHolder(expectedBytes); 
                     byte[] actualBytes = holder.getBytes(); 
                     if (!Arrays.equals(expectedBytes, actualBytes)) 
                     { 
                         String actualValue = new String(actualBytes, StandardCharsets.UTF_8); 
                         System.err.printf("Assertion failed: expected='%s' actual='%s' (bytes: %s)%n", 
                             expectedValue, actualValue, DatatypeConverter.printHexBinary(actualBytes)); 
                     } 
                 }); 
    } 

    static class ArrayHolder 
    { 
        private byte[] _bytes; 

        ArrayHolder(final byte[] bytes) { _bytes = bytes.clone(); } 

        byte[] getBytes() { return _bytes.clone(); } 

        @Override 
        protected void finalize() throws Throwable 
        { 
            if (_bytes != null) 
            { 
                Arrays.fill(_bytes, (byte) 'z'); 
                _bytes = null; 
            } 
            super.finalize(); 
        } 
    } 

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; 
    private static final Random RND = new Random(); 

    static String randomAlphaNumeric(int count) 
    { 
        final StringBuilder sb = new StringBuilder(); 
        while (count-- != 0) 
        { 
            int character = RND.nextInt(ALPHA_NUMERIC_STRING.length()); 
            sb.append(ALPHA_NUMERIC_STRING.charAt(character)); 
        } 
        return sb.toString(); 
    } 

    public static void main(String[] args) 
        throws Exception 
    { 
        new NewlyAllocatedArrayFilledByOtherInstanceFinalizer().demonstrate(); 
    } 
} 