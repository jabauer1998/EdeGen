package ede.gen.gui;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.*;

public class JavaJobCompiler {
    private static int classCounter = 0;

    public static Callable<Void> compile(String userCode) throws Exception {
        return compile(userCode, Collections.emptyList());
    }

    public static Callable<Void> compile(String userCode, List<String> jarPaths) throws Exception {
        classCounter++;
        String className = "DynamicJob_" + classCounter;

        StringBuilder imports = new StringBuilder();
        imports.append("import java.util.concurrent.Callable;\n");
        for (String jarPath : jarPaths) {
            Set<String> packages = getPackagesFromJar(jarPath);
            for (String pkg : packages) {
                imports.append("import ").append(pkg).append(".*;\n");
            }
        }

        String fullSource =
            imports.toString() +
            "public class " + className + " implements Callable<Void> {\n" +
            "    public Void call() throws Exception {\n" +
            "        " + userCode + "\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n";

        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "edegen_compile");
        tmpDir.mkdirs();
        File sourceFile = new File(tmpDir, className + ".java");
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(fullSource);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Java compiler not available. Ensure JDK is installed.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        StringBuilder classPath = new StringBuilder(System.getProperty("java.class.path"));
        for (String jarPath : jarPaths) {
            classPath.append(File.pathSeparator).append(jarPath);
        }
        classPath.append(File.pathSeparator).append(tmpDir.getAbsolutePath());

        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(classPath.toString());
        options.add("-d");
        options.add(tmpDir.getAbsolutePath());

        Iterable<? extends JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));

        JavaCompiler.CompilationTask task = compiler.getTask(
            null, fileManager, diagnostics, options, null, compilationUnits);

        boolean success = task.call();
        fileManager.close();

        if (!success) {
            StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                errorMsg.append("Line ").append(d.getLineNumber())
                        .append(": ").append(d.getMessage(null)).append("\n");
            }
            throw new RuntimeException(errorMsg.toString());
        }

        List<URL> urls = new ArrayList<>();
        urls.add(tmpDir.toURI().toURL());
        for (String jarPath : jarPaths) {
            urls.add(new File(jarPath).toURI().toURL());
        }

        URLClassLoader classLoader = new URLClassLoader(
            urls.toArray(new URL[0]),
            JavaJobCompiler.class.getClassLoader()
        );

        @SuppressWarnings("unchecked")
        Class<Callable<Void>> clazz =
            (Class<Callable<Void>>) classLoader.loadClass(className);
        return clazz.getDeclaredConstructor().newInstance();
    }

    private static Set<String> getPackagesFromJar(String jarPath) {
        Set<String> packages = new TreeSet<>();
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && !name.contains("module-info") && !name.contains("$")) {
                    int lastSlash = name.lastIndexOf('/');
                    if (lastSlash > 0) {
                        String pkg = name.substring(0, lastSlash).replace('/', '.');
                        packages.add(pkg);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[WARNING] Could not read JAR: " + jarPath + " - " + e.getMessage());
        }
        return packages;
    }
}
