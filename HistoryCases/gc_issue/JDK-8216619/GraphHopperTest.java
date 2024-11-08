import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.io.File;
import java.io.RandomAccessFile;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import java.util.StringTokenizer;

public class GraphHopperTest
{
    private static String someFile = "test.txt";

// from graphhopper
    public static boolean jreIsMinimumJava9() {
        final StringTokenizer st = new StringTokenizer(System.getProperty("java.specification.version"), ".");
        int JVM_MAJOR_VERSION = Integer.parseInt(st.nextToken());
        int JVM_MINOR_VERSION;
        if (st.hasMoreTokens()) {
            JVM_MINOR_VERSION = Integer.parseInt(st.nextToken());
        } else {
            JVM_MINOR_VERSION = 0;
        }
        return JVM_MAJOR_VERSION > 1 || (JVM_MAJOR_VERSION == 1 && JVM_MINOR_VERSION >= 9);
    }

// from graphhopper
    public static void cleanMappedByteBuffer(final ByteBuffer buffer) {
        // TODO avoid reflection on every call
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    if (jreIsMinimumJava9()) {
                        // >=JDK9 class sun.misc.Unsafe { void invokeCleaner(ByteBuffer buf) }
                        final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                        // fetch the unsafe instance and bind it to the virtual MethodHandle
                        final Field f = unsafeClass.getDeclaredField("theUnsafe");
                        f.setAccessible(true);
                        final Object theUnsafe = f.get(null);
                        final Method method = unsafeClass.getDeclaredMethod("invokeCleaner", ByteBuffer.class);
                        try {
                            method.invoke(theUnsafe, buffer);
                            return null;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    } else {
                        // <=JDK8 class DirectByteBuffer { sun.misc.Cleaner cleaner(Buffer buf) }
                        //        then call sun.misc.Cleaner.clean
                        final Class<?> directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                        try {
                            final Method dbbCleanerMethod = directByteBufferClass.getMethod("cleaner");
                            dbbCleanerMethod.setAccessible(true);
                            // call: cleaner = ((DirectByteBuffer)buffer).cleaner()
                            final Object cleaner = dbbCleanerMethod.invoke(buffer);
                            if (cleaner != null) {
                                final Class<?> cleanerMethodReturnType = dbbCleanerMethod.getReturnType();
                                final Method cleanMethod = cleanerMethodReturnType.getDeclaredMethod("clean");
                                cleanMethod.setAccessible(true);
                                // call: ((sun.misc.Cleaner)cleaner).clean()
                                cleanMethod.invoke(cleaner);
                            }
                        } catch (NoSuchMethodException ex2) {
                                // ignore if method cleaner or clean is not available
                                System.out.println("NoSuchMethodException " + ex2);
                                System.out.println(System.getProperty("java.version"));
                        }
                    }

                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException("Unable to unmap the mapped buffer", e);
        }
}
 
    public static void main(String[] args) throws Exception {
        System.gc(); // Initialize thread stacks/memory memory mappings etc.

        boolean cleanout = args.length > 0;
        boolean useSystemGC = cleanout && args[0].startsWith("s");

        System.out.println("cleanout " + cleanout);
        System.out.println("use system.gc " + useSystemGC);

        for (int j = 0; j < 7000; j++) {
            try (RandomAccessFile file = new RandomAccessFile(new File(someFile), "r")) {
                FileChannel fileChannel = file.getChannel();           
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                System.out.print((char) buffer.get()); //Print the content of file
                if (cleanout) {
                    if (useSystemGC && j % 1000 == 0) {
                        System.gc();
                    }
                    if (!useSystemGC) {
                        cleanMappedByteBuffer(buffer);
                    }
                }
            }
        }
    }
}

