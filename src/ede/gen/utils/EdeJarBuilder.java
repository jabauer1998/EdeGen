package ede.gen.utils;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import ede.stl.ast.*;
import ede.stl.common.*;
import ede.stl.parser.*;
import ede.gen.compiler.VerilogToEdeGen;

public class EdeJarBuilder {

    private static final Set<String> EXCLUDED_STL_PREFIXES = new HashSet<>(Arrays.asList(
        "ede/stl/ast/",
        "ede/stl/parser/",
        "ede/stl/interpreter/",
        "ede/stl/passes/"
    ));

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

    public static class RegisterData {
        public String name;
        public int size;

        public RegisterData(String name, int size) {
            this.name = name;
            this.size = size;
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
        public boolean syntaxHighlighting;
        public List<RegisterData> verilogRegisters;
        public List<String> verilogFlags;
        public int verilogMemorySize;

        public JobData(String jobType, String jobName) {
            this.jobType = jobType;
            this.jobName = jobName;
            this.jarPaths = new ArrayList<>();
            this.verilogRegisters = new ArrayList<>();
            this.verilogFlags = new ArrayList<>();
            this.verilogMemorySize = -1;
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
        boolean hasVerilogJobs = false;

        File verilogInstanceDir = new File(System.getProperty("user.dir"), "ede/instance");
        File verilogRootDir = new File(System.getProperty("user.dir"));

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
            } else if ("Verilog Job".equals(job.jobType)) {
                javaJobClassNames.add(null);
                if (job.verilogPath != null && !job.verilogPath.trim().isEmpty()) {
                    File vFile = new File(job.verilogPath);
                    if (vFile.exists()) {
                        hasVerilogJobs = true;
                        VerilogFile ast = parseVerilogFile(job.verilogPath);
                        extractVerilogMetadata(ast, job);
                        VerilogToEdeGen gen = new VerilogToEdeGen(69, null, "StandardOutput", "StandardInput");
                        gen.codeGenVerilogFile(ast);
                    }
                }
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
                    if (hasVerilogJobs && isExcludedStlEntry(name)) {
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

            if (hasVerilogJobs) {
                if (!verilogInstanceDir.exists()) {
                    throw new RuntimeException("Compiled Verilog classes not found at: " + verilogInstanceDir.getAbsolutePath());
                }
                addClassFiles(jos, verilogInstanceDir, verilogRootDir, addedEntries);
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

    private static boolean isExcludedStlEntry(String name) {
        for (String prefix : EXCLUDED_STL_PREFIXES) {
            if (name.startsWith(prefix)) return true;
        }
        if (name.startsWith("ede/stl/compiler/VerilogToJavaGen") || name.startsWith("ede/stl/compiler/VerilogToEdeGen")) return true;
        return false;
    }

    private static VerilogFile parseVerilogFile(String path) throws Exception {
        FileReader reader = new FileReader(path);
        Source source = new Source(reader);
        ErrorLog errorLog = new ErrorLog();
        Lexer lexer = new Lexer(source, errorLog);
        List<Token> tokens = lexer.tokenize();
        Preprocessor preProc = new Preprocessor(errorLog, tokens);
        tokens = preProc.executePass();
        Parser parser = new Parser(tokens, errorLog);
        VerilogFile verilogFile = parser.parseVerilogFile();
        reader.close();
        if (errorLog.size() > 0) {
            errorLog.printLog();
            throw new RuntimeException("Verilog parse errors in: " + path);
        }
        return verilogFile;
    }

    private static void extractVerilogMetadata(VerilogFile ast, JobData job) {
        for (ModuleDeclaration module : ast.modules) {
            for (ModuleItem item : module.moduleItemList) {
                processModuleItem(item, job);
            }
        }
    }

    private static void processModuleItem(ModuleItem item, JobData job) {
        if (item instanceof Reg.Scalar.Ident) {
            Reg.Scalar.Ident ident = (Reg.Scalar.Ident) item;
            handleAnnotation(ident.annotationLexeme, ident.declarationIdentifier, 1, -1, job);
        } else if (item instanceof Reg.Vector.Ident) {
            Reg.Vector.Ident ident = (Reg.Vector.Ident) item;
            int size = computeVectorSize(ident.GetIndex1(), ident.GetIndex2());
            handleAnnotation(ident.annotationLexeme, ident.declarationIdentifier, size, -1, job);
        } else if (item instanceof Reg.Scalar.Array) {
            Reg.Scalar.Array arr = (Reg.Scalar.Array) item;
            int memSize = computeArraySize(arr.arrayIndex1, arr.arrayIndex2);
            handleAnnotation(arr.annotationLexeme, arr.declarationIdentifier, 1, memSize, job);
        } else if (item instanceof Reg.Vector.Array) {
            Reg.Vector.Array arr = (Reg.Vector.Array) item;
            int vecSize = computeVectorSize(arr.GetIndex1(), arr.GetIndex2());
            int memSize = computeArraySize(arr.arrayIndex1, arr.arrayIndex2);
            handleAnnotation(arr.annotationLexeme, arr.declarationIdentifier, vecSize, memSize, job);
        } else if (item instanceof Output.Reg.Scalar.Ident) {
            Output.Reg.Scalar.Ident ident = (Output.Reg.Scalar.Ident) item;
            handleAnnotation(ident.annotationLexeme, ident.declarationIdentifier, 1, -1, job);
        } else if (item instanceof Output.Reg.Vector.Ident) {
            Output.Reg.Vector.Ident ident = (Output.Reg.Vector.Ident) item;
            int size = computeVectorSize(ident.GetIndex1(), ident.GetIndex2());
            handleAnnotation(ident.annotationLexeme, ident.declarationIdentifier, size, -1, job);
        } else if (item instanceof Output.Reg.Scalar.Array) {
            Output.Reg.Scalar.Array arr = (Output.Reg.Scalar.Array) item;
            int memSize = computeArraySize(arr.arrayIndex1, arr.arrayIndex2);
            handleAnnotation(arr.annotationLexeme, arr.declarationIdentifier, 1, memSize, job);
        } else if (item instanceof Output.Reg.Vector.Array) {
            Output.Reg.Vector.Array arr = (Output.Reg.Vector.Array) item;
            int vecSize = computeVectorSize(arr.GetIndex1(), arr.GetIndex2());
            int memSize = computeArraySize(arr.arrayIndex1, arr.arrayIndex2);
            handleAnnotation(arr.annotationLexeme, arr.declarationIdentifier, vecSize, memSize, job);
        }
    }

    private static void handleAnnotation(String annotation, String name, int regSize, int memSize, JobData job) {
        if (annotation == null || annotation.isEmpty()) return;
        String ann = annotation.trim().toLowerCase();
        if (ann.equals("register")) {
            job.verilogRegisters.add(new RegisterData(name, regSize));
        } else if (ann.equals("status")) {
            job.verilogFlags.add(name);
        } else if (ann.equals("memory")) {
            if (memSize > 0) {
                job.verilogMemorySize = memSize;
            }
        }
    }

    private static int computeVectorSize(Expression idx1, Expression idx2) {
        int i1 = evaluateExpression(idx1);
        int i2 = evaluateExpression(idx2);
        return Math.abs(i1 - i2) + 1;
    }

    private static int computeArraySize(Expression idx1, Expression idx2) {
        int i1 = evaluateExpression(idx1);
        int i2 = evaluateExpression(idx2);
        return Math.abs(i1 - i2) + 1;
    }

    private static int evaluateExpression(Expression expr) {
        if (expr instanceof DecimalNode) {
            try {
                return Integer.parseInt(((DecimalNode) expr).lexeme.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (expr instanceof HexadecimalNode) {
            try {
                String hex = ((HexadecimalNode) expr).lexeme.trim().replaceAll("_", "");
                return Integer.parseInt(hex, 16);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (expr instanceof OctalNode) {
            try {
                String oct = ((OctalNode) expr).lexeme.trim().replaceAll("_", "");
                return Integer.parseInt(oct, 8);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (expr instanceof BinaryNode) {
            try {
                String bin = ((BinaryNode) expr).lexeme.trim().replaceAll("_", "");
                return Integer.parseInt(bin, 2);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (expr instanceof ConstantExpression) {
            return evaluateExpression(((ConstantExpression) expr).expression);
        } else if (expr instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) expr;
            int left = evaluateExpression(binOp.left);
            int right = evaluateExpression(binOp.right);
            if (binOp.Op == BinaryOperation.Operator.PLUS) return left + right;
            if (binOp.Op == BinaryOperation.Operator.MINUS) return left - right;
            if (binOp.Op == BinaryOperation.Operator.TIMES) return left * right;
            return 0;
        }
        return 0;
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

        for (int i = 0; i < jobs.size(); i++) {
            JobData job = jobs.get(i);
            if ("Verilog Job".equals(job.jobType)) {
                if (job.verilogMemorySize > 0) {
                    sb.append("            guiEde.setUpMemory(").append(job.verilogMemorySize).append(");\n");
                }
                for (RegisterData reg : job.verilogRegisters) {
                    sb.append("            guiEde.AddRegister(\"").append(escapeJava(reg.name)).append("\", ").append(reg.size).append(", regFmt);\n");
                }
                for (String flag : job.verilogFlags) {
                    sb.append("            guiEde.AddFlag(\"").append(escapeJava(flag)).append("\");\n");
                }
                sb.append("\n");
            }
        }

        int javaIdx = 0;
        for (int i = 0; i < jobs.size(); i++) {
            JobData job = jobs.get(i);
            if ("Java Job".equals(job.jobType)) {
                String className = javaJobClassNames.get(i);
                sb.append("            try {\n");
                sb.append("                EdeCallable callable_").append(javaIdx).append(" = new ").append(className).append("(guiEde);\n");
                String javaTextAreaType = job.syntaxHighlighting ? "GuiJob.TextAreaType.KEYWORD" : "GuiJob.TextAreaType.DEFAULT";
                if (job.keywords != null && job.keywords.length > 0) {
                    sb.append("                guiEde.AddJavaJob(\"").append(escapeJava(job.jobName)).append("\", ").append(javaTextAreaType).append(", callable_").append(javaIdx);
                    for (String kw : job.keywords) {
                        sb.append(", \"").append(escapeJava(kw)).append("\"");
                    }
                    sb.append(");\n");
                } else {
                    sb.append("                guiEde.AddJavaJob(\"").append(escapeJava(job.jobName)).append("\", ").append(javaTextAreaType).append(", callable_").append(javaIdx).append(");\n");
                }
                sb.append("            } catch (Exception e) {\n");
                sb.append("                System.err.println(\"Failed to create Java Job: \" + e.getMessage());\n");
                sb.append("            }\n");
                javaIdx++;
            } else if ("Verilog Job".equals(job.jobType)) {
                String verilogInput = job.verilogInputFile != null ? job.verilogInputFile : "";
                sb.append("            guiEde.AddVerilogJob(\"").append(escapeJava(job.jobName))
                  .append("\", \"\", \"").append(escapeJava(verilogInput))
                  .append("\", \"StandardInput\", \"StandardOutput\", \"StandardError\", false);\n");
            } else if ("Exe Job".equals(job.jobType)) {
                String exeTextAreaType = job.syntaxHighlighting ? "GuiJob.TextAreaType.KEYWORD" : "GuiJob.TextAreaType.DEFAULT";
                if (job.keywords != null && job.keywords.length > 0) {
                    sb.append("            guiEde.AddExeJob(\"").append(escapeJava(job.jobName)).append("\", ").append(exeTextAreaType).append(", \"")
                      .append(escapeJava(job.exePath)).append("\"");
                    for (String kw : job.keywords) {
                        sb.append(", \"").append(escapeJava(kw)).append("\"");
                    }
                    sb.append(");\n");
                } else {
                    sb.append("            guiEde.AddExeJob(\"").append(escapeJava(job.jobName)).append("\", ").append(exeTextAreaType).append(", \"")
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
