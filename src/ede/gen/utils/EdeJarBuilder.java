package ede.gen.utils;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

public class EdeJarBuilder {

    public static class IoSectionData {
        public String tabName;
        public String sectionTitle;
        public boolean readOnly;

        public IoSectionData(String tabName, String sectionTitle, boolean readOnly) {
            this.tabName = tabName;
            this.sectionTitle = sectionTitle;
            this.readOnly = readOnly;
        }
    }

    public static class JobData {
        public String jobType;
        public String jobName;
        public String code;
        public String imports;
        public List<String> jarPaths;
        public String verilogPath;
        public String verilogInputFile;
        public String exePath;
        public String bundledVerilogPath;
        public String[] keywords;

        public JobData(String jobType, String jobName) {
            this.jobType = jobType;
            this.jobName = jobName;
            this.jarPaths = new ArrayList<>();
        }
    }

    public static File buildJar(
            String edeName,
            int ramBytesPerRow,
            String addressFormat,
            String memoryFormat,
            String registerFormat,
            List<IoSectionData> ioSections,
            List<JobData> jobs,
            String edeStlJarPath,
            File outputDir
    ) throws Exception {

        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "edegen_jar_build_" + System.currentTimeMillis());
        tmpDir.mkdirs();

        List<String> javaJobClassNames = new ArrayList<>();
        List<File> javaJobSources = new ArrayList<>();

        int javaJobIndex = 0;
        for (JobData job : jobs) {
            if ("Java Job".equals(job.jobType)) {
                javaJobIndex++;
                String className = "EdeJob_" + javaJobIndex;
                javaJobClassNames.add(className);
                String source = generateJobSource(className, job.code, job.imports);
                File sourceFile = new File(tmpDir, className + ".java");
                writeFile(sourceFile, source);
                javaJobSources.add(sourceFile);
            } else {
                javaJobClassNames.add(null);
            }
        }

        String mainSource = generateMainClass(edeName, ramBytesPerRow, addressFormat, memoryFormat, registerFormat, ioSections, jobs, javaJobClassNames);
        File mainSourceFile = new File(tmpDir, "EdeMain.java");
        writeFile(mainSourceFile, mainSource);

        List<File> allSources = new ArrayList<>();
        allSources.add(mainSourceFile);
        allSources.addAll(javaJobSources);

        StringBuilder classPath = new StringBuilder(edeStlJarPath);
        for (JobData job : jobs) {
            if (job.jarPaths != null) {
                for (String jarPath : job.jarPaths) {
                    classPath.append(File.pathSeparator).append(jarPath);
                }
            }
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Java compiler not available.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(classPath.toString());
        options.add("-d");
        options.add(tmpDir.getAbsolutePath());

        Iterable<? extends JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjectsFromFiles(allSources);

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

        String jarFileName = edeName.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".jar";
        File outputJar = new File(outputDir, jarFileName);

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "EdeMain");

        Set<String> addedEntries = new HashSet<>();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar), manifest)) {

            addClassFiles(jos, tmpDir, tmpDir, addedEntries);

            try (JarFile stlJar = new JarFile(edeStlJarPath)) {
                Enumeration<JarEntry> entries = stlJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.equals("META-INF/MANIFEST.MF") || name.equals("META-INF/")) {
                        continue;
                    }
                    if (addedEntries.contains(name)) {
                        continue;
                    }
                    addedEntries.add(name);
                    jos.putNextEntry(new JarEntry(name));
                    try (InputStream is = stlJar.getInputStream(entry)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            jos.write(buffer, 0, len);
                        }
                    }
                    jos.closeEntry();
                }
            }

            int verilogFileIdx = 0;
            for (JobData job : jobs) {
                if ("Verilog Job".equals(job.jobType)) {
                    if (job.verilogPath != null && !job.verilogPath.trim().isEmpty()) {
                        File vFile = new File(job.verilogPath);
                        if (vFile.exists()) {
                            String entryName = "verilog/schematic_" + verilogFileIdx + "_" + vFile.getName();
                            if (!addedEntries.contains(entryName)) {
                                addedEntries.add(entryName);
                                jos.putNextEntry(new JarEntry(entryName));
                                try (FileInputStream fis = new FileInputStream(vFile)) {
                                    byte[] buffer = new byte[4096];
                                    int len;
                                    while ((len = fis.read(buffer)) > 0) {
                                        jos.write(buffer, 0, len);
                                    }
                                }
                                jos.closeEntry();
                            }
                            job.bundledVerilogPath = entryName;
                        }
                    }
                    verilogFileIdx++;
                }
            }

            for (JobData job : jobs) {
                if (job.jarPaths != null) {
                    for (String jarPath : job.jarPaths) {
                        try (JarFile extraJar = new JarFile(jarPath)) {
                            Enumeration<JarEntry> entries = extraJar.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.startsWith("META-INF/")) continue;
                                if (addedEntries.contains(name)) continue;
                                addedEntries.add(name);
                                jos.putNextEntry(new JarEntry(name));
                                try (InputStream is = extraJar.getInputStream(entry)) {
                                    byte[] buffer = new byte[4096];
                                    int len;
                                    while ((len = is.read(buffer)) > 0) {
                                        jos.write(buffer, 0, len);
                                    }
                                }
                                jos.closeEntry();
                            }
                        }
                    }
                }
            }
        }

        deleteDir(tmpDir);

        return outputJar;
    }

    private static String generateJobSource(String className, String code, String imports) {
        StringBuilder sb = new StringBuilder();
        sb.append("import ede.stl.common.EdeCallable;\n");
        sb.append("import ede.stl.gui.GuiEde;\n");
        if (imports != null && !imports.trim().isEmpty()) {
            for (String line : imports.split("\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    if (!trimmed.startsWith("import ")) {
                        trimmed = "import " + trimmed;
                    }
                    if (!trimmed.endsWith(";")) {
                        trimmed = trimmed + ";";
                    }
                    sb.append(trimmed).append("\n");
                }
            }
        }
        sb.append("public class ").append(className).append(" implements EdeCallable {\n");
        sb.append("    private GuiEde EdeInstance;\n");
        sb.append("    public ").append(className).append("(GuiEde edeInstance) {\n");
        sb.append("        this.EdeInstance = edeInstance;\n");
        sb.append("    }\n");
        sb.append("    public String call(String input) throws Exception {\n");
        sb.append("        ").append(code).append("\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String generateMainClass(
            String edeName,
            int ramBytesPerRow,
            String addressFormat,
            String memoryFormat,
            String registerFormat,
            List<IoSectionData> ioSections,
            List<JobData> jobs,
            List<String> javaJobClassNames
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("import javax.swing.*;\n");
        sb.append("import java.awt.*;\n");
        sb.append("import java.io.*;\n");
        sb.append("import ede.stl.gui.GuiEde;\n");
        sb.append("import ede.stl.gui.GuiRam;\n");
        sb.append("import ede.stl.gui.GuiJob;\n");
        sb.append("import ede.stl.gui.GuiRegister;\n");
        sb.append("import ede.stl.gui.GuiIO;\n");
        sb.append("import ede.stl.common.EdeCallable;\n");
        sb.append("\n");
        sb.append("public class EdeMain {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        SwingUtilities.invokeLater(() -> {\n");
        sb.append("            Toolkit toolkit = Toolkit.getDefaultToolkit();\n");
        sb.append("            Dimension screenSize = toolkit.getScreenSize();\n");
        sb.append("            double edeWidth = screenSize.width;\n");
        sb.append("            double edeHeight = screenSize.height;\n");
        sb.append("\n");

        sb.append("            GuiRam.AddressFormat addrFmt = GuiRam.AddressFormat.").append(addressFormat).append(";\n");
        sb.append("            GuiRam.MemoryFormat memFmt = GuiRam.MemoryFormat.").append(memoryFormat).append(";\n");
        sb.append("            GuiRegister.Format regFmt = GuiRegister.Format.").append(registerFormat).append(";\n");
        sb.append("\n");
        sb.append("            GuiEde guiEde = new GuiEde(edeWidth, edeHeight, ").append(ramBytesPerRow).append(", addrFmt, memFmt);\n");
        sb.append("\n");

        sb.append("            guiEde.AddIoSection(\"IO\", \"StandardOutput\", GuiIO.Editable.READ_ONLY);\n");
        sb.append("            guiEde.AddIoSection(\"IO\", \"StandardInput\", GuiIO.Editable.EDITABLE);\n");
        sb.append("            guiEde.AddIoSection(\"Errors\", \"StandardError\", GuiIO.Editable.READ_ONLY);\n");

        for (IoSectionData io : ioSections) {
            String editable = io.readOnly ? "GuiIO.Editable.READ_ONLY" : "GuiIO.Editable.EDITABLE";
            sb.append("            guiEde.AddIoSection(\"").append(escapeJava(io.tabName)).append("\", \"")
              .append(escapeJava(io.sectionTitle)).append("\", ").append(editable).append(");\n");
        }
        sb.append("\n");

        int javaIdx = 0;
        for (int i = 0; i < jobs.size(); i++) {
            JobData job = jobs.get(i);
            if ("Java Job".equals(job.jobType)) {
                String className = javaJobClassNames.get(i);
                sb.append("            try {\n");
                sb.append("                EdeCallable callable_").append(javaIdx).append(" = new ").append(className).append("(guiEde);\n");
                if (job.keywords != null && job.keywords.length > 0) {
                    sb.append("                guiEde.AddJavaJob(\"").append(escapeJava(job.jobName)).append("\", GuiJob.TextAreaType.DEFAULT, callable_").append(javaIdx);
                    for (String kw : job.keywords) {
                        sb.append(", \"").append(escapeJava(kw)).append("\"");
                    }
                    sb.append(");\n");
                } else {
                    sb.append("                guiEde.AddJavaJob(\"").append(escapeJava(job.jobName)).append("\", GuiJob.TextAreaType.DEFAULT, callable_").append(javaIdx).append(");\n");
                }
                sb.append("            } catch (Exception e) {\n");
                sb.append("                System.err.println(\"Failed to create Java Job: \" + e.getMessage());\n");
                sb.append("            }\n");
                javaIdx++;
            } else if ("Verilog Job".equals(job.jobType)) {
                if (job.bundledVerilogPath != null) {
                    sb.append("            String verilogPath_").append(i).append(" = extractResource(\"").append(escapeJava(job.bundledVerilogPath)).append("\");\n");
                } else {
                    sb.append("            String verilogPath_").append(i).append(" = \"").append(escapeJava(job.verilogPath)).append("\";\n");
                }
                sb.append("            String verilogInput_").append(i).append(" = \"").append(escapeJava(job.verilogInputFile != null ? job.verilogInputFile : "")).append("\";\n");
                sb.append("            guiEde.gatherMetaDataFromVerilogFile(verilogPath_").append(i).append(", regFmt);\n");
                sb.append("            guiEde.AddVerilogJob(\"").append(escapeJava(job.jobName)).append("\", verilogPath_").append(i).append(", verilogInput_").append(i).append(", \"StandardInput\", \"StandardOutput\", \"StandardError\");\n");
            } else if ("Exe Job".equals(job.jobType)) {
                if (job.keywords != null && job.keywords.length > 0) {
                    sb.append("            guiEde.AddExeJob(\"").append(escapeJava(job.jobName)).append("\", GuiJob.TextAreaType.DEFAULT, \"")
                      .append(escapeJava(job.exePath)).append("\"");
                    for (String kw : job.keywords) {
                        sb.append(", \"").append(escapeJava(kw)).append("\"");
                    }
                    sb.append(");\n");
                } else {
                    sb.append("            guiEde.AddExeJob(\"").append(escapeJava(job.jobName)).append("\", GuiJob.TextAreaType.DEFAULT, \"")
                      .append(escapeJava(job.exePath)).append("\");\n");
                }
            }
        }

        sb.append("\n");
        sb.append("            guiEde.linkJobs();\n");
        sb.append("\n");
        sb.append("            JFrame frame = new JFrame(\"").append(escapeJava(edeName)).append("\");\n");
        sb.append("            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n");
        sb.append("            frame.add(guiEde);\n");
        sb.append("            frame.setPreferredSize(screenSize);\n");
        sb.append("            frame.pack();\n");
        sb.append("            frame.setVisible(true);\n");
        sb.append("        });\n");
        sb.append("    }\n");
        sb.append("\n");
        sb.append("    private static String extractResource(String resourcePath) {\n");
        sb.append("        try {\n");
        sb.append("            InputStream is = EdeMain.class.getClassLoader().getResourceAsStream(resourcePath);\n");
        sb.append("            if (is == null) throw new RuntimeException(\"Resource not found: \" + resourcePath);\n");
        sb.append("            String fileName = resourcePath.contains(\"/\") ? resourcePath.substring(resourcePath.lastIndexOf('/') + 1) : resourcePath;\n");
        sb.append("            File tmpFile = File.createTempFile(\"ede_\", \"_\" + fileName);\n");
        sb.append("            tmpFile.deleteOnExit();\n");
        sb.append("            try (FileOutputStream fos = new FileOutputStream(tmpFile)) {\n");
        sb.append("                byte[] buffer = new byte[4096];\n");
        sb.append("                int len;\n");
        sb.append("                while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);\n");
        sb.append("            }\n");
        sb.append("            is.close();\n");
        sb.append("            return tmpFile.getAbsolutePath();\n");
        sb.append("        } catch (Exception e) {\n");
        sb.append("            throw new RuntimeException(\"Failed to extract resource: \" + resourcePath, e);\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String escapeJava(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static void addClassFiles(JarOutputStream jos, File dir, File root, Set<String> addedEntries) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                addClassFiles(jos, file, root, addedEntries);
            } else if (file.getName().endsWith(".class")) {
                String entryName = root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
                if (addedEntries.contains(entryName)) continue;
                addedEntries.add(entryName);
                jos.putNextEntry(new JarEntry(entryName));
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        jos.write(buffer, 0, len);
                    }
                }
                jos.closeEntry();
            }
        }
    }

    private static void writeFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private static void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
