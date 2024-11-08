import java.io.File;
    import java.net.URL;
    import java.net.URLClassLoader;

    /**
     *
     * @author nijiaben
     * @version v1.0
     * @createTime 2018年09月22日 12:32:32 PM
     *
     */
    public class ClassLoaderTest {
        private static URL[] urls = new URL[1];

        static {
            try {
                File jarFile = new File("/home/admin/.m2/repository/org/slf4j/slf4j-api/1.7.21/slf4j-api-1.7.21.jar");
                urls[0] = jarFile.toURI().toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void main(String args[]) {
            for(int i=0;i<1000;i++) {
                loadClass();
            }
            System.gc();
        }

        public static void loadClass() {
            try {
                URLClassLoader ucl = new URLClassLoader(urls);
                Class.forName("org.slf4j.Logger", false, ucl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }