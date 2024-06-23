package analyzer;


import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.CancellationException;

public class FileFinder {
    public static ArrayList<File> files = new ArrayList<>();

    public static void loadFiles(File rootDir, String suffix) {
        if (rootDir == null) {
            return;
        }
        if (rootDir.isFile()) {
            if (rootDir.getName().endsWith(suffix)) {
                files.add(rootDir);
            }
        } else if (rootDir.isDirectory()) {
            File[] list = rootDir.listFiles();
            if (list == null) {
                return;
            }
            for (File file : list) {
                loadFiles(file, suffix);
            }
        }
    }
}
