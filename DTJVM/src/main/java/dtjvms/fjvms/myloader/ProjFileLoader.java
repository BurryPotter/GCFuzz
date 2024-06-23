package dtjvms.fjvms.myloader;

import dtjvms.DTConfiguration;
import dtjvms.ProjectInfo;
import dtjvms.loader.Loader;

import java.io.File;
import java.util.ArrayList;

/**
 * simple loader.
 * unlike DTLoader, the config file is reproduce.cfg.json, instead of RuntimeConfig.xml
 * without lib,target..., only has .java file and reproduce.cfg.json in one project.
 */
public class ProjFileLoader extends Loader {
    private static ProjFileLoader myloader;
    public static ProjFileLoader getInstance() {

        if (myloader == null) {
            myloader = new ProjFileLoader();
        }
        return myloader;
    }

    public ArrayList<ProjectInfo> loadProjects() {

        if (Projects == null) {
            Projects = new ArrayList<>();
        }
        File pDpends = new File(DTConfiguration.PROJECT_DEPENS_ROOT);
        if (pDpends.exists()) {

            for (File projectFile : pDpends.listFiles()) {
                //lib, class
                if (projectFile.isDirectory()) {

                    String projectName = projectFile.getName();
                    if (ProjFileLoaderHelper.shouldTestProject(projectName)) {

                        ProjectInfo currentProject = ProjFileLoaderHelper.analysisProject(projectFile);
                        Projects.add(setupRunnableClasses(currentProject, null));
//                        Projects.add(currentProject);
                    }
                }
            }
        } else {
            throw new RuntimeException("Project-depens directory: " + DTConfiguration.PROJECT_DEPENS_ROOT + " not exists!");
        }
        return Projects;
    }

}
