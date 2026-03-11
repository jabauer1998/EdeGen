# EdeGen - Emulator Development Environment Generator Tool

## Overview
A Java Swing desktop GUI application for generating emulator development environments. It provides a visual interface for configuring machine specifications and job specifications. The form generates a GuiEde instance from the EdeStl library.

## Project Architecture
- **Language**: Java (Swing GUI)
- **JDK**: OpenJDK 25.0.2 (installed at /tmp/jdk-25.0.2, required by EdeStl.jar)
- **Build**: javac compilation from source files listed in `build/BuildList.txt` with EdeStl.jar on classpath
- **Source**: `src/ede/gen/` - main source tree
  - `driver/EdeGenerator.java` - Main entry point
  - `gui/` - GUI components (panels, fields, collapsable frames, specifiers)
  - `utils/JavaJobCompiler.java` - Runtime Java code compilation into EdeCallable objects
  - `utils/JavaSyntaxHighlighter.java` - Syntax highlighting for Java code editor
  - `utils/EdeJarBuilder.java` - Generates executable JARs from form data with compiled jobs and EdeStl
- **Libraries**: `lib/EdeStl/` - EdeStl git submodule (compiled to `lib/EdeStl/bin/EdeStl.jar`); depends on ASM 9.6 (`lib/EdeStl/lib/asm-9.6.jar`, gitignored, auto-downloaded)
- **Output**: Compiled classes go to `bin/`

## Key Features
- **Test Ede Environment**: Collects form parameters and opens a new JFrame with GuiEde from EdeStl.jar
- **IO Sections**: Dynamic add/remove list in Machine Specifier to configure GuiEde IO sections (tab name, section title, editable/read-only)
- **Java Job Compilation**: Typed Java code is compiled at runtime into EdeCallable using javax.tools.JavaCompiler; compiled classes receive GuiEde instance
- **Save Ede Environment**: Generates a portable FAT JAR with compiled Verilog (via VerilogToJavaGen ASM bytecode generation), compiled Java jobs, and EdeStl (excluding interpreter/parser/AST/passes for compiled Verilog mode)
- **Syntax Highlighting**: Java code editor with color-coded keywords, types, strings, comments, numbers, annotations
- **Job Types**: Verilog (file selector), Java (code editor), Exe (file selector)

## Running
The workflow compiles all Java sources with JDK 25 and runs `ede.gen.driver.EdeGenerator` via VNC display.

## Recent Changes
- 2026-03-11: VerilogToEdeGen: new subclass of VerilogToJavaGen for EDE-specific codegen; handles @status/@register/@memory annotations for EdeStatVal/EdeRegVal/EdeMemVal fields; @breakpoint annotation for debugger support; overrides $display to route output to GuiEde.appendIoText; overrides blocking assignment for EDE value types (shallowAssignElemEde/shallowAssignSliceEde)
- 2026-03-11: Utils.java: added shallowAssignElemEde (EdeMemVal/EdeRegVal element assignment with fallback), shallowAssignSliceEde (EdeRegVal slice assignment with fallback); added EdeMemVal/EdeRegVal imports
- 2026-03-11: VerilogToJavaGen: changed 6 private methods to protected for subclass override (codeGenFieldRegScalarIdent, codeGenFieldRegVectorIdent, codeGenFieldRegVectorArray, codeGenShallowBlockingAssign, codeGenShallowSystemTaskCall, codeGenShallowExpression)
- 2026-03-11: Fixed formatString signature (Value first arg instead of String), fixed fScanf return type (Value instead of StrVal), fixed fOpen/fScanf throws Exception, fixed $fclose codegen (method name casing, environment ALOAD), fixed $fopen/$feof/$fscanf codegen (environment ALOAD 2)
- 2026-03-09: VerilogToJavaGen: process methods now generated as instance methods (non-static) on module class instead of separate Processes class; module extends VerilogAsJavaBase; loadProcesses override generated to call each processN; processes can access module fields; VerilogAsJavaBase fixed with proper imports and CompiledEnvironment type; added missing SystemFunctionCall import
- 2026-03-06: EdeStl is now a git submodule at lib/EdeStl; updated build scripts (LinuxBuild.sh for both EdeGen and EdeStl) to match Windows counterparts; workflow builds submodule first then EdeGen; EdeStl.jar path updated to lib/EdeStl/bin/EdeStl.jar
- 2026-03-03: Compiled Verilog mode for Save: VerilogToJavaGen generates bytecode (.class files) at build time; AST annotations (@register→AddRegister, @status→AddFlag, @memory→setUpMemory) extracted and emitted as explicit calls in generated EdeMain; FAT JAR excludes ast/parser/interpreter/passes packages; AddVerilogJob uses compiled mode (false)
- 2026-03-01: Added JTabbedPane to GuiGenPanel - Home tab has original layout, each Java Job gets its own full-page tab for imports+code editing
- 2026-03-01: Enable Syntax Highlighting checkbox moved to header bar, controls TextAreaType (DEFAULT vs KEYWORD)
- 2026-03-01: Added keyword file loading for Java and Exe jobs, passed as varargs to AddJavaJob/AddExeJob
- 2026-02-24: Implemented Save Ede Environment - generates executable JAR with EdeStl, compiled jobs, and generated main class
- 2026-02-24: Updated to EdeCallable interface (String call(String)) replacing Callable<Void>
- 2026-02-24: Added Job Name field to job specifiers, passed to job constructors
- 2026-02-24: Added Verilog Input File field to Verilog job specifier
- 2026-02-24: Updated AddExeJob/AddJavaJob to use new varargs constructors
- 2026-02-17: Upgraded to JDK 25 to match EdeStl.jar class version requirement
- 2026-02-17: Wired Test Ede Environment button to create GuiEde from EdeStl.jar with form parameters
- 2026-02-17: Created JavaJobCompiler for runtime compilation of Java code into EdeCallable objects
- 2026-02-17: Added getter methods to GuiJobSpecifier (job type, paths) and updated GuiMachineSpecifier
- 2026-02-17: Fixed syntax highlighting order (comments/strings override keywords/numbers)
- 2026-02-17: Added line wrapping to JTextPane editor
- 2026-02-17: Fixed class name mismatch (EdeGenField -> GuiEdeGenField)
- 2026-02-16: Initial Replit setup - installed Java, configured VNC workflow, added .gitignore
