package gc.g1.humongousObjects;

import jdk.test.lib.Asserts;
import sun.hotspot.WhiteBox;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @test gc.g1.humongousObjects.TestClassLoader
 * @summary Checks that unreachable classloader is unloaded
 * @requires vm.gc=="G1" | vm.gc=="null"
 * @library /testlibrary /../../test/lib
 * @modules java.management
 * @build sun.hotspot.WhiteBox
 *        gc.g1.humongousObjects.TestClassLoader
 * @run driver ClassFileInstaller sun.hotspot.WhiteBox
 *                                sun.hotspot.WhiteBox$WhiteBoxPermission
 *
 * @run main/othervm -Xms128M -XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:.
 *                   -Xloggc:TestClassLoader_fullGC.gc.log -XX:G1HeapRegionSize=1M -XX:+TraceClassUnloading
 *                   gc.g1.humongousObjects.TestClassLoader fullGC
 *
 * @run main/othervm -Xms128M -XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:.
 *                   -Xloggc:TestClassLoader_concMark.gc.log -XX:G1HeapRegionSize=1M -XX:+TraceClassUnloading
 *                   gc.g1.humongousObjects.TestClassLoader

 *
 */


public class TestClassLoader {
    private static WhiteBox WB = WhiteBox.getWhiteBox();
    private static final String CLASS_NAME_PREFIX = TestClassLoader.class.getSimpleName() + "_";
    private static final String CHILD_CLASSLOADER_NAME = CLASS_NAME_PREFIX + "ChildClassLoader";

    private static class ClassInfo {
        public final String className;
        public final String baseClass;

        public ClassInfo(String className, String baseClass) {
            this.className = className;
            this.baseClass = baseClass;
        }
    }

    public static void compileClass(String className, String root, String source) throws IOException {
        File sourceFile = new File(root, className + ".java");
        Files.write(sourceFile.toPath(), source.getBytes());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, "-cp", System.getProperty("java.class.path") + ":" + root, sourceFile.getPath());
    }

    public static String generateClass(String className, String extendsClass, String constructor) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("public class %s%s {\n", className, extendsClass == null ? ""
                : " extends " + extendsClass));

        if (constructor != null) {
            buffer.append(constructor);
        }
        buffer.append("}\n");

        return new String(buffer);
    }

    static Class<?> compileAndGetClass(ClassLoader classLoader, String source, String root, String className)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        compileClass(className, root, source);
        return Class.forName(className, true, classLoader);
    }


    public static Class<?> getGeneratedAndCompiledClass(ClassLoader classLoader, String className, String extendsClass,
                                                        String constructor, String root)
            throws ClassNotFoundException, IOException {
        compileClass(className, root, generateClass(className, extendsClass, constructor));
        return Class.forName(className, true, classLoader);
    }

    public static void generateAndCompile(String className, String extendsClass, String root)
            throws ClassNotFoundException, IOException {
        compileClass(className, root, generateClass(className, extendsClass, null));

    }


    public static final String CLASSLOADER_PROTOTYPE = "import java.io.File;\n"
            + "import java.io.IOException;\n"
            + "import java.nio.file.Files;\n"
            + "public class SimpleClassLoader extends ClassLoader{\n"
            + "    public SimpleClassLoader(ClassLoader parent) {\n"
            + "        super(parent);\n"
            + "    }\n"
            + "    @Override\n"
            + "    public Class loadClass(String fileName) throws ClassNotFoundException {\n"
            + "        if (fileName.startsWith(\"" + CLASS_NAME_PREFIX + "\")) {\n"
            + "            byte[] b = null;\n"
            + "            try {\n"
            + "                b = Files.readAllBytes(new File(fileName + \".class\").toPath());\n"
            + "            } catch (IOException e) {\n"
            + "                e.printStackTrace();\n"
            + "            }\n"
            + "            Class c = defineClass(fileName, b, 0, b.length);\n"
            + "            resolveClass(c);\n"
            + "            return c;\n"
            + "        } else {\n"
            + "            return super.loadClass(fileName);\n"
            + "        }\n"
            + "\n"
            + "\n"
            + "    }\n"
            + "\n"
            + "}\n";


    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IOException, NoSuchMethodException, InvocationTargetException {
        boolean runFullGC = false;

        if (args.length !=0 && "fullgc".equals(args[0].toLowerCase())) {
            runFullGC = true;
        }

        String workDir = Paths.get("").toAbsolutePath().toString();

        Class<?> simpleClassLoaderClass = compileAndGetClass(TestClassLoader.class.getClassLoader(),
                CLASSLOADER_PROTOTYPE, workDir, "SimpleClassLoader");

        ClassLoader simpleClassLoader = (ClassLoader) simpleClassLoaderClass.
                getConstructor(java.lang.ClassLoader.class).newInstance(TestClassLoader.class.getClassLoader());

        Class<?> childClassLoaderClass = getGeneratedAndCompiledClass(simpleClassLoader,
                CHILD_CLASSLOADER_NAME, "SimpleClassLoader",
                "    public " + CHILD_CLASSLOADER_NAME + "(ClassLoader parent) { super(parent);}\n",
                workDir);


        ClassLoader childClassLoader = (ClassLoader) childClassLoaderClass.
                getConstructor(java.lang.ClassLoader.class).newInstance(simpleClassLoader);

        String baseClassName = CLASS_NAME_PREFIX + "BaseClass";
        generateAndCompile(baseClassName, null, workDir);


        ClassInfo testClassInfo = new ClassInfo(CLASS_NAME_PREFIX + "TestClass", baseClassName);

        Asserts.assertEquals(WB.isClassAlive(testClassInfo.className), false,
                String.format("Class %s is loaded before we start testing", testClassInfo.className));

        generateAndCompile(testClassInfo.className, testClassInfo.baseClass, workDir);

        Class<?> loadedClass = childClassLoader.loadClass(testClassInfo.className);

        Asserts.assertEquals(WB.isClassAlive(testClassInfo.className), true,
                String.format("Class %s is not loaded after we load it", testClassInfo.className));


        // forgetting references to loaded classes
        loadedClass = null;
        // forgetting referencies to classloaders
        childClassLoader = null;
        childClassLoaderClass = null;

        simpleClassLoader = null;
        simpleClassLoaderClass = null;

        if (runFullGC) {
            WB.fullGC();
        } else {
            while (WB.g1InConcurrentMark()) {}
            WB.g1StartConcMarkCycle();
            while (WB.g1InConcurrentMark()) {}
        }

        Asserts.assertEquals(WB.isClassAlive(testClassInfo.className), false,
                String.format("Class %s is loaded after we forget all references to it",
                        testClassInfo.className));

        Asserts.assertEquals(WB.isClassAlive(CHILD_CLASSLOADER_NAME), false,
                String.format("Classloader class %s is loaded after we forget all references to it",
                        CHILD_CLASSLOADER_NAME));
    }
}
