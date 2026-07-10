package com.yurpha.outgoingengine.data;

import com.yurpha.outgoingengine.build.BuildManager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataGetter {

    public static List<Path> getProjectSourceFiles(String projectName) {
        List<Path> filePaths = new ArrayList<>();

        Path engineHome = BuildManager.getEngineHomeDirectories();

        Path projectSrcDir = engineHome.resolve("Projects").resolve(projectName).resolve("src");


        File srcFolder = projectSrcDir.toFile();

        System.out.println("[ENGINE] Scanning Paths Inside: " + projectSrcDir.toAbsolutePath());


        if( srcFolder.exists() && srcFolder.isDirectory()){
            File[] files = srcFolder.listFiles();
            if(files != null){
                for (File file : files){
                    if(file.isFile()){
                        filePaths.add(file.toPath());

                        System.out.println("Found File: " + file.getName() + " at path: " + file.getAbsolutePath());

                    }
                }
            }
        }else{
            System.err.println("[ENGINE] Source folder does not exist: " + projectSrcDir.toAbsolutePath());
        }

        return filePaths;
    }
}
