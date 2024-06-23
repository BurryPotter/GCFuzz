package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameJavaFile {
    public static void main(String[] args) throws IOException {
        String path = "D:\\projects\\GCGenerator\\historyCases";
        File rootDir = new File(path);
        FileFinder.loadFiles(rootDir, ".java");
        for (File javaFile : FileFinder.files) {
            String className = extractClassName(javaFile.getAbsolutePath());
            if (className == null) {
                System.out.println("contain JNI. Delete " + javaFile.getParentFile().getAbsolutePath());
                javaFile.delete();
                continue;
            }
            System.out.println(javaFile.getAbsolutePath() + " -> " + className);

            javaFile.renameTo(new File(javaFile.getParentFile(), className + ".java"));
        }
    }

    private static String extractClassName(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String className = "Default";
        while ((line = reader.readLine()) != null) {
            String name = extractClassNames(line);
            if (name != null && className.equals("Default")) {
                className = name;
            }
            if (line.contains("#include") || line.contains("JNIEXPORT") || line.contains("JNICALL") || line.contains("<!DOCTYPE html PUBLIC")) {
                className = null;
                break;
            }
        }

        reader.close();
        return className;
    }

    private static String extractClassNames(String fileContent) {
        Pattern pattern = Pattern.compile("public\\s+(strictfp|final)?\\s*class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(fileContent);

        if (matcher.find()) {
            String className = matcher.group(2);
            return className;
        }
        return null;
    }
}
