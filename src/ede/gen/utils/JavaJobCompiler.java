package ede.gen.utils;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;

public class JavaJobCompiler {
    private static int classCounter = 0;

    public static Callable<Void> compile(String userCode) throws Exception {
        classCounter++;
        String className = "DynamicJob_" + classCounter;

        String fullSource =
            "import java.util.concurrent.Callable;\n" +
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

        String classPath = System.getProperty("java.class.path");
        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(classPath + File.pathSeparator + tmpDir.getAbsolutePath());
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

        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{tmpDir.toURI().toURL()},
            JavaJobCompiler.class.getClassLoader()
        );

        @SuppressWarnings("unchecked")
        Class<Callable<Void>> clazz =
            (Class<Callable<Void>>) classLoader.loadClass(className);
        return clazz.getDeclaredConstructor().newInstance();
    }
}
