package dtjvms.fjvms.myloader;

import com.google.gson.Gson;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.ProjectInfo;
import dtjvms.loader.LoaderHelper;
import dtjvms.fjvms.config.ConfigBean;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;


public class ProjFileLoaderHelper extends LoaderHelper {

    /**
     * analysis target project elements
     *
     * @param projectFile project file
     * @return ProjectInfo object
     */
    public static ProjectInfo analysisProject(File projectFile) {
        // projectRootPath : JDK_xx
        String projectName = projectFile.getName();
        String projectRootPath = projectFile.getAbsolutePath();
        ProjectInfo currentProject = new ProjectInfo(projectName, projectRootPath);
        currentProject.setSrcClassPath(projectFile.getAbsolutePath());
        currentProject.setSrcJavaPath(projectFile.getAbsolutePath());

        String runtimeConfigPath = projectRootPath + DTPlatform.FILE_SEPARATOR + DTConfiguration.PROJECT_RUNTIME_CONFIG;
        File rcFile = new File(runtimeConfigPath);
        if (rcFile.exists()) {
            try {
                FileReader reader = new FileReader(rcFile);
                Gson gson = new Gson();
                ConfigBean config = gson.fromJson(reader, ConfigBean.class);
                ArrayList<String> arr = new ArrayList<>();
                Collections.addAll(arr, StringUtils.trimToEmpty(config.getJavac_options()).split("[ ]"));
                currentProject.setJavacOptions(arr);
                arr = new ArrayList<>();
                Collections.addAll(arr, StringUtils.trimToEmpty(config.getOptions()).split("[ ]"));
                currentProject.setVmoptions(arr);
                arr = new ArrayList<>();
                Collections.addAll(arr, StringUtils.trimToEmpty(config.getParameters()).split("[ ]"));
                currentProject.setProjoptions(arr);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            currentProject.setProjoptions(new ArrayList<>());
        }


        File[] contentFiles = projectFile.listFiles();
        ArrayList<String> classes = new ArrayList<>();
        if (contentFiles != null) {
            for (File contentFile : contentFiles) {
                if (contentFile.getName().endsWith(".java")) {
//                    boolean compiled = compileJavaFile(contentFile, currentProject.getJavacOptions());
//                    if (compiled) {
                    classes.add(contentFile.getName().split("[.]")[0]);
//                    }
                }
            }
        }
        currentProject.setApplicationClasses(classes);
        currentProject.setJunitClasses(new ArrayList<>());
        if (currentProject.getSrcClassPath() == null && currentProject.getTestClassPath() == null) {

            long classNumber = countFileSizeWithSuffix(projectFile, ".class");
            if (classNumber > 0) {
                currentProject.setSrcClassPath(projectRootPath);
                currentProject.setTotalSrcClassSize(classNumber);
                currentProject.setTotalTestClassSize(classNumber);
            } else {
                System.err.println("Project " + projectName + " seems to be an empty project! Please check the project root path: " + projectRootPath);
            }
        }
        return currentProject;
    }

    public static boolean shouldTestProject(String projectName) {

        if (projectName == null) {
            return false;
        }
        if (DTConfiguration.TARGET_PROJECTS.contains(projectName)) {
            return true;
        }
        for (String key : DTConfiguration.TARGET_PROJECTS_KEYWORDS) {
            if (projectName.contains(key)) {
                return true;
            }
        }
        for (String key : DTConfiguration.FILTER_PROJECTS_KEYWORDS) {
            if (projectName.contains(key)) {
                return false;
            }
        }
        if (DTConfiguration.FILTER_PROJECTS.contains(projectName)) {
            return false;
        }
        return true;
    }
}
