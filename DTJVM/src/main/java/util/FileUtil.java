package util;

import dtjvms.DTPlatform;

import java.io.*;

public class FileUtil {
    public static FileReader readFile(String path, String file) throws FileNotFoundException {
        String fullName = path + DTPlatform.FILE_SEPARATOR + file;
        return readFile(fullName);
    }

    public static FileReader readFile(String fullPath) throws FileNotFoundException {
        File rcFile = new File(fullPath);
        if (rcFile.exists()) {
            try {
                return new FileReader(rcFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        throw new FileNotFoundException(fullPath + " not exist!");
    }

    public static FileWriter writeFile(String path, String file) {
        String fullName = path + DTPlatform.FILE_SEPARATOR + file;
        return writeFile(fullName);
    }

    public static FileWriter writeFile(String fullPath) {
        File file = new File(fullPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileWriter fw = new FileWriter(file);
            return fw;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
