package com.yurpha.outgoingengine.build;



import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import javax.tools.JavaCompiler;

import javax.tools.ToolProvider;



public class BuildManager {

    private static String oldPath;
    public static String runBuild(String projectName, String mainclass, Path iconpath){

        Random rr = new Random();
        int ri = rr.nextInt(0, 9);
        String uniqueID = String.valueOf(System.currentTimeMillis()).substring(8) + "_" +String.valueOf(ri);
        String uniqueName = projectName + "_" + uniqueID;

        StringBuilder log = new StringBuilder();

        Path source = Paths.get("Projects", projectName);
        Path target = Paths.get("Builds", uniqueName + "_BUILD");



        try {

            log.append("[OutGoing Engine Build]\n");

            log.append("Project: ").append(projectName).append("\n\n");

            // Delete old build if it exists

            if(oldPath != null){
                Path old = Paths.get(oldPath);
                if(Files.exists(old)){

                    log.append("Cleaning previous build...\n");

                    deleteFolder(old);

                }
            }




            ensureBuildStructure(target, log);

            // Compile FIRST
            Path compileOutput = target.resolve("compiled");

            Path jarFile = target.resolve("app").resolve(projectName + ".jar");

            log.append("\nCompiling project...\n");

            compileJavaFiles(
                    source.resolve("src"),
                    compileOutput,
                    log
            );

            // THEN JAR
            createJar(compileOutput, jarFile, log);
            // THEN PACKAGE

            if(iconpath == null){
                iconpath = extractIconFromResources("/applicationassets/OG.ico");
            }
            packageWithJPackage(projectName, jarFile, target, mainclass, iconpath, log);

            log.append("\nBUILD SUCCESS\n");

        }catch (IOException e){

            log.append("BUILD FAILED:\n");

            log.append(e.getMessage());

        }



        writeLog(target, log.toString());



        oldPath = target.toString();

        return log.toString();

    }



    private static void copyFolder(Path source, Path target) throws IOException{

        Files.walk(source).forEach(path -> {

            try{

                Path relative = source.relativize(path);

                Path dest = target.resolve(relative);



                if(Files.isDirectory(path)){

                    Files.createDirectories(dest);

                }else{

                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);

                }

            } catch (Exception e) {

                throw new RuntimeException(e);

            }



        });

    }



    private static void deleteFolder(Path path) throws IOException {
        if (!Files.exists(path)) return;

        try (java.util.stream.Stream<Path> walk = Files.walk(path)) {
            walk.sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            try {
                                // Hard Windows bypass: If we can't delete it, rename it to a random name
                                // in the temp directory. Windows always allows renaming locked files!
                                Path temp = Files.createTempFile("junk", ".exe");
                                Files.move(p, temp, StandardCopyOption.REPLACE_EXISTING);
                                temp.toFile().deleteOnExit(); // Tells the OS to clear it when the IDE closes
                            } catch (IOException ignored) {
                                System.err.println("Completely blocked: " + p);
                            }
                        }
                    });
        }
    }

    private static void compileJavaFiles(Path sourceDir, Path outputDir, StringBuilder log){
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                log.append("ERROR: No Java compiler found. Are you using a JDK?\n");
                return;
            }

            Files.createDirectories(outputDir);

            // 1. Point directly to the folder containing the SDK .jar files
            Path fxSdkLib = Paths.get("lib", "javafx-sdk", "lib").toAbsolutePath();

            // 2. Collect all your project's .java files
            List<String> javaFiles;
            try (Stream<Path> walk = Files.walk(sourceDir)) {
                javaFiles = walk.filter(p -> p.toString().endsWith(".java"))
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }

            if (javaFiles.isEmpty()) {
                log.append("Warning: No .java source files found to compile.\n");
                return;
            }

            // 3. Build the compilation arguments using modules instead of classpath
            List<String> arguments = new ArrayList<>();

            // Tell the compiler where the JavaFX modules are located
            arguments.add("--module-path");
            arguments.add(fxSdkLib.toString());

            // Tell the compiler which specific JavaFX features your game needs
            arguments.add("--add-modules");
            arguments.add("javafx.controls,javafx.fxml,javafx.graphics,javafx.media");

            // Set the destination output directory
            arguments.add("-d");
            arguments.add(outputDir.toString());

            // Append all code files to compile
            arguments.addAll(javaFiles);

            log.append("Compiling ").append(javaFiles.size()).append(" files with module tracking...\n");

            // 4. Run the compilation step
            int result = compiler.run(
                    null,
                    null,
                    null,
                    arguments.toArray(new String[0])
            );

            if (result == 0) {
                log.append("Compilation finished successfully.\n");
            } else {
                log.append("Compilation failed with error code: ").append(result).append("\n");
            }

        } catch (Exception e){
            log.append("Compilation error: ").append(e.getMessage()).append("\n");
        }
    }



    private static void createJar(Path compiledDir, Path jarFile, StringBuilder log){
        try{
            log.append("Creating JAR...\n");

            ProcessBuilder pb = new ProcessBuilder(
                    "jar",
                    "--create",
                    "--file", jarFile.toString(),
                    "-C", compiledDir.toString(),
                    "."
            );



            pb.inheritIO();

            Process process = pb.start();

            process.waitFor();



            log.append("JAR created at: ").append(jarFile).append("\n");

        }catch (Exception e){

            log.append("JAR creation failed: ")

                    .append(e.getMessage())

                    .append("\n");

        }

    }



    private static void packageWithJPackage(String projectName, Path jarFile, Path buildRoot, String mainClass, Path iconPath, StringBuilder log){

        try {

            Path outputDir = buildRoot.resolve("output");

            Path appImageDir = outputDir.resolve(projectName);

            //deleteFolder(outputDir);

            Thread.sleep(200);

            Files.createDirectories(outputDir);

            log.append("\nRunning jpackage with JavaFX modules...\n");

            Path fxModulesPath = Paths.get("lib", "javafx-jmods");

            ProcessBuilder pb = new ProcessBuilder(

                    "jpackage",
                    "--type", "app-image",
                    "--name", projectName,
                    "--input", jarFile.getParent().toString(),
                    "--main-jar", jarFile.getFileName().toString(),
                    "--main-class", mainClass,
                    "--dest", outputDir.toString(),
                    "--icon", iconPath.toAbsolutePath().toString(),

                    // Critical for javafx
                    "--module-path", fxModulesPath.toAbsolutePath().toString(),
                    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics"
            );



            pb.redirectErrorStream(true);

            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());

            process.waitFor();

            log.append("jpackage completed.\n");

            log.append("\n--- jpackage output ---\n");

            log.append(output);

            log.append("\n-----------------------------\n");

        }catch (Exception e){

            log.append("jpackage failed: ")

                    .append(e.getMessage())

                    .append("\n");

        }

    }



    private static void ensureBuildStructure(Path buildRoot, StringBuilder log){

        try{

            Path compiled = buildRoot.resolve("compiled");
            //Path output = buildRoot.resolve("output");
            Path runtime = buildRoot.resolve("runtime");
            Path app = buildRoot.resolve("app");



            Files.createDirectories(compiled);
            //Files.createDirectories(output);
            Files.createDirectories(runtime);
            Files.createDirectories(app);





            log.append("Build folders initialized. \n");

        } catch (Exception e) {

            log.append("Failed to create build structure: ")
                    .append(e.getMessage())
                    .append("\n");
        }

    }



    private static void writeLog(Path buildRoot, String log){

        try{

            Path logFile = buildRoot.resolve("build.log");

            Files.writeString(logFile, log);

        }catch (Exception e){

            System.out.println("Failed to write build log: " + e.getMessage());

        }

    }


    public static Path extractIconFromResources(String resourcePath){
        System.out.println(BuildManager.class.getResource("/"));
        System.out.println(BuildManager.class.getResource("/applicationassets/OG.ico"));
        InputStream in = BuildManager.class.getResourceAsStream(resourcePath);

        if(in == null){
            System.out.println("Check 1");
            return null;
        }

        try{
            Path tempIcon = Files.createTempFile("outgoing_engine_icon", ".ico");
            Files.copy(in, tempIcon, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Check 2");
            return tempIcon;
        }catch (Exception e){
            System.out.println("Error");
        }

        System.out.println("Check 3");
        return null;
    }



}