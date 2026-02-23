package ede.gen.utils;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
import ede.stl.gui.GuiEde;

public class JavaJobCompiler {
    private static int classCounter = 0;

    public static Callable<Void> compile(String userCode, GuiEde edeInstance) throws Exception {
        return compile(userCode, "", Collections.emptyList(), edeInstance);
    }

    public static Callable<Void> compile(String userCode, String userImports, List<String> jarPaths, GuiEde edeInstance) throws Exception {
        classCounter++;
        String className = "DynamicJob_" + classCounter;

        StringBuilder importBlock = new StringBuilder();
        importBlock.append("import java.util.concurrent.Callable;\n");
        importBlock.append("import ede.stl.gui.GuiEde;\n");
        if (userImports != null && !userImports.trim().isEmpty()) {
            for (String line : userImports.split("\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    if (!trimmed.startsWith("import ")) {
                        trimmed = "import " + trimmed;
                    }
                    if (!trimmed.endsWith(";")) {
                        trimmed = trimmed + ";";
                    }
                    importBlock.append(trimmed).append("\n");
                }
            }
        }

        String fullSource =
            importBlock.toString() +
            "public class " + className + " implements Callable<Void> {\n" +
            "    private GuiEde EdeInstance;\n" +
            "    public " + className + "(GuiEde edeInstance){\n"+
            "        this.EdeInstance = edeInstance;\n" +
            "    }\n" +
            "    public Void call() throws Exception {\n" +
            "        " + userCode + "\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n";

        System.out.println("[JavaJobCompiler] Generated source for " + className + ":");
        System.out.println(fullSource);

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
        classPath.append(File.pathSeparator).append(tmpDir.getAbsolutePath());
        for (String jarPath : jarPaths) {
            classPath.append(File.pathSeparator).append(jarPath);
        }

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
        return clazz.getDeclaredConstructor(GuiEde.class).newInstance(edeInstance);
    }
}
