package com.yurpha.outgoingengine.build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.jar.Manifest;
import java.util.jar.JarOutputStream;
import java.util.jar.Attributes;
import java.io.FileOutputStream;

public class BuildManager {

    private static String oldPath;

    public static String runBuild(String projectName, String mainclass, Path iconpath){
        Random rr = new Random();
        int ri = rr.nextInt(0, 9);
        String uniqueID = String.valueOf(System.currentTimeMillis()).substring(8) + "_" + String.valueOf(ri);
        String uniqueName = projectName + "_" + uniqueID;

        StringBuilder log = new StringBuilder();

        Path engineHome = getEngineHomeDirectories();
        Path source = engineHome.resolve("Projects").resolve(projectName);
        Path target = engineHome.resolve("Builds").resolve(uniqueName + "_BUILD");

        try {
            log.append("[OutGoing Engine Build]\n");
            log.append("Project: ").append(projectName).append("\n\n");

            if(oldPath != null){
                Path old = Paths.get(oldPath);
                if(Files.exists(old)){
                    log.append("Cleaning previous session build...\n");
                    deleteFolder(old);
                }
            }

            ensureBuildStructure(target, log);

            Path compileOutput = target.resolve("compiled");
            Path jarFile = target.resolve("app").resolve(projectName + ".jar");

            log.append("\nCompiling project...\n");
            compileJavaFiles(
                    source.resolve("src"),
                    compileOutput,
                    log
            );

            // CASE-INSENSITIVE FALLBACK CHECK FOR RESOURCES
            Path gameResources = source.resolve("resources");
            if (!Files.exists(gameResources)) {
                gameResources = source.resolve("Resources"); // Check for capital R
            }

            createJar(compileOutput, gameResources, jarFile, log);

            if(iconpath == null){
                iconpath = engineHome.resolve("lib").resolve("templates").resolve("OG.ico");
            }
            packageWithJPackage(projectName, jarFile, target, mainclass, iconpath, log);

            log.append("\nBUILD SUCCESS\n");

        } catch (IOException e){
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
                                Path temp = Files.createTempFile("junk", ".exe");
                                Files.move(p, temp, StandardCopyOption.REPLACE_EXISTING);
                                temp.toFile().deleteOnExit();
                            } catch (IOException ignored) {
                                System.err.println("Completely blocked: " + p);
                            }
                        }
                    });
        }
    }

    private static void compileJavaFiles(Path sourceDir, Path outputDir, StringBuilder log){
        try {
            javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                log.append("ERROR: Internal Java compiler module not found in runtime.\n");
                return;
            }

            Files.createDirectories(outputDir);

            Path engineHome = getEngineHomeDirectories();
            Path fxSdkLib = engineHome.resolve("lib").resolve("javafx-sdk").resolve("lib").toAbsolutePath();

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

            List<String> arguments = new ArrayList<>();
            arguments.add("--module-path");
            arguments.add(fxSdkLib.toFile().getAbsolutePath());
            arguments.add("--add-modules");
            arguments.add("javafx.controls,javafx.fxml,javafx.graphics,javafx.media");
            arguments.add("-d");
            arguments.add(outputDir.toFile().getAbsolutePath());
            arguments.addAll(javaFiles);

            log.append("Compiling ").append(javaFiles.size()).append(" files programmatically...\n");

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


    private static void createJar(Path classDir, Path resourceDir, Path outputJarFile, StringBuilder log) {
        try {
            Files.createDirectories(outputJarFile.getParent());

            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "Main");

            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJarFile.toFile()), manifest)) {

                // 1. Pack compiled .class files
                if (Files.exists(classDir)) {
                    try (Stream<Path> walk = Files.walk(classDir)) {
                        List<Path> files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                        for (Path file : files) {
                            String name = classDir.relativize(file).toString().replace("\\", "/");
                            jos.putNextEntry(new JarEntry(name));
                            Files.copy(file, jos);
                            jos.closeEntry();
                        }
                    }
                }

                // 2. Pack resource assets
                if (Files.exists(resourceDir)) {
                    try (Stream<Path> walk = Files.walk(resourceDir)) {
                        List<Path> files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                        for (Path file : files) {
                            String name = resourceDir.relativize(file).toString().replace("\\", "/");
                            jos.putNextEntry(new JarEntry(name));
                            Files.copy(file, jos);
                            jos.closeEntry();
                        }
                    }
                    log.append("Successfully bundled resource files into JAR.\n");
                } else {
                    log.append("Warning: Resource directory not found at: ").append(resourceDir.toAbsolutePath()).append("\n");
                }

            }
            log.append("JAR created successfully at: ").append(outputJarFile).append("\n");
        } catch (Exception e) {
            log.append("Error creating JAR: ").append(e.getMessage()).append("\n");
        }
    }

    private static void packageWithJPackage(String projectName, Path jarFile, Path buildRoot, String mainClass, Path iconPath, StringBuilder log){
        try {
            Path outputDir = buildRoot.resolve("output");
            Files.createDirectories(outputDir);

            log.append("\nRunning jpackage with JavaFX modules...\n");

            Path engineHome = getEngineHomeDirectories();
            Path fxModulesPath = engineHome.resolve("lib").resolve("javafx-jmods").toAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    "jpackage",
                    "--type", "app-image",
                    "--name", projectName,
                    "--input", jarFile.getParent().toString(),
                    "--main-jar", jarFile.getFileName().toString(),
                    "--main-class", mainClass,
                    "--dest", outputDir.toString(),
                    "--icon", iconPath.toAbsolutePath().toString(),
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
            log.append("jpackage failed: ").append(e.getMessage()).append("\n");
        }
    }

    private static void ensureBuildStructure(Path buildRoot, StringBuilder log){
        try{
            Path compiled = buildRoot.resolve("compiled");
            Path runtime = buildRoot.resolve("runtime");
            Path app = buildRoot.resolve("app");

            Files.createDirectories(compiled);
            Files.createDirectories(runtime);
            Files.createDirectories(app);

            log.append("Build folders initialized. \n");
        } catch (Exception e) {
            log.append("Failed to create build structure: ").append(e.getMessage()).append("\n");
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
        InputStream in = BuildManager.class.getResourceAsStream(resourcePath);
        if(in == null) return null;

        try{
            Path tempIcon = Files.createTempFile("outgoing_engine_icon", ".ico");
            Files.copy(in, tempIcon, StandardCopyOption.REPLACE_EXISTING);
            return tempIcon;
        }catch (Exception e){
            System.out.println("Error extracting icon");
        }
        return null;
    }

    public static Path getEngineHomeDirectories() {
        try {
            String codePath = BuildManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(codePath);
            File resultDir = jarFile.getParentFile();

            if (resultDir != null && resultDir.getName().equals("app")) {
                return resultDir.getParentFile().toPath().toAbsolutePath();
            }
            return Paths.get("").toAbsolutePath();
        } catch (URISyntaxException e) {
            return Paths.get("").toAbsolutePath();
        }
    }

    public static void initializeEngineWorkspace(){
        try {
            Path engineHome = getEngineHomeDirectories();
            Path projectDir = engineHome.resolve("Projects").toAbsolutePath();
            Path buildsDir = engineHome.resolve("Builds").toAbsolutePath();
            Path libDir = engineHome.resolve("lib").toAbsolutePath();

            Files.createDirectories(projectDir);
            Files.createDirectories(buildsDir);
            Files.createDirectories(libDir);

            Path debugFile = engineHome.resolve("engine_path_debug.txt");
            String debugText = "Engine Home: " + engineHome.toString() + "\n" +
                    "Projects Target: " + projectDir.toString();
            Files.writeString(debugFile, debugText);

        } catch (Exception e) {
            System.err.println("Failed to initialize engine workspace: " + e.getMessage());
        }
    }
}