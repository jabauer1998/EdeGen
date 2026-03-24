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
- 2026-03-18: Fixed EdeJarBuilder.handleAnnotation: comparisons were missing '@' prefix (checked "register" but lexer stores "@register"); now accepts both "@register"/"@status"/"@memory" and bare forms; this was preventing flags/registers/memory from appearing in the saved JAR
- 2026-03-18: Fixed VerilogToJavaGen.codeGenModule constructor: added super() call (ALOAD_0 + INVOKESPECIAL VerilogAsJavaBase.<init>()V), RETURN instruction, and visitMaxs(0,0) before visitEnd(); fixed superclass typo "VerilogAsJaaBase" → "VerilogAsJavaBase"; these omissions caused "Execution can fall off end of code" bytecode verifier error
- 2026-03-18: Fixed RepeatLoop counter: visitIincInsn was incrementing slot 0 (this) instead of localArg1; fixed to visitIincInsn(localArg1, 1)
- 2026-03-18: Fixed GuiEdeLog: removed setPreferredSize/setMaximumSize/setMinimumSize that pinned height to initial value, preventing vertical scroll from working; now uses no-arg constructor and lets JScrollPane manage sizing
- 2026-03-18: Fixed VerilogToJavaGen calculateUsedShallowFunctions/DeepFunctions field name errors: Concatenation.expressionList→circuitElementExpressionList, TernaryOperation.trueExpression/falseExpression→ifTrue/ifFalse, UnaryOperation.expression→rightHandSideExpression; added throws Exception to Deep expression overloads; added 4-arg codeGenFunction overload; removed duplicate Shallow Statement overload
- 2026-03-24: Fixed GuiVerilogJob.RunJob: getDeclaredMethod("loadProcesses",...) only finds methods declared directly on the class, not inherited ones; changed to getMethod which searches the full class hierarchy including VerilogAsJavaBase
- 2026-03-24: Fixed VerilogToEdeGen.codeGenShallowBlockingAssign: was pushing RHS value first then computing LHS target, giving wrong stack order for PUTFIELD ([Value, objectref] instead of [objectref, Value]) and wrong arg order for shallowAssignElemEde/shallowAssignSliceEde ([rhs, index, arr] instead of [arr, index, rhs]); restructured to push LHS target (arr/objectref) first, then index(es), then RHS value last; added ARETURN + RHS codegen for the function-return case; fallthrough to super for unknown LValue types
- 2026-03-24: Fixed VerilogToJavaGen.codeGenShallowNonBlockingAssign Identifier/field PUTFIELD: value is pre-computed and already on the stack before ALOAD_0 is pushed, giving [Value, objectref] order; added SWAP between ALOAD_0 and PUTFIELD to correct to [objectref, Value]
- 2026-03-23: Fixed VerilogToJavaGen.codeGenVerilogFile: switched ClassWriter from COMPUTE_MAXS to COMPUTE_FRAMES (anonymous subclass overriding getCommonSuperClass to return "java/lang/Object") — JDK 25 requires stackmap frames even for V1_6 class files; COMPUTE_MAXS does not generate them; the getCommonSuperClass override prevents the Frame.merge class-loading crash that originally forced the switch to COMPUTE_MAXS
- 2026-03-23: Added progress bar for Save Ede Environment: EdeJarBuilder now accepts ProgressListener interface; pre-counts STL/extra-JAR/Verilog entries before writing; reports per-entry progress; GuiMachineSpecifier uses SwingWorker + modeless JDialog with JProgressBar and status label; Save button disabled during build
- 2026-03-15: VerilogToJavaGen/VerilogToEdeGen: changed all ClassWriter method parameters to ClassVisitor for polymorphism; codeGenVerilogFile now wraps ClassWriter in CheckClassAdapter (from asm-util-9.6.jar) before passing through the codegen chain — verification fires inline as each module class is generated; added asm-util-9.6.jar, asm-tree-9.6.jar, asm-analysis-9.6.jar to lib/EdeStl/lib/
- 2026-03-15: Fixed array field codegen in VerilogToJavaGen: all three array init methods (codeGenFieldRegScalarArray, codeGenFieldRegVectorArray, codeGenFieldIntArray) now emit ALOAD 0 + NEW + DUP before constructor args, use correct PUTFIELD owner (modName), and use correct descriptors; ArrayVectorVal descriptor corrected to 4-arg (Value,Value,Value,Value)V; ArrayIntVal corrected to 2-arg (Value,Value)V (was wrongly using intValue()+Math.abs+(int) single-arg form); codeGenShallowBlockingAssign PUTFIELD owner fixed from "ede/stl/values/Value" to modName
- 2026-03-15: Fixed Source.java in EdeStl: added missing Source(Reader) convenience constructor; changed Source(InputStream) to delegate to this(new InputStreamReader(inputStream), 1, 1) instead of non-existent this(Reader) overload; fixes 8 build errors across GuiEde, VerilogInterpreter, Preprocessor
- 2026-03-13: VerilogToJavaGen bytecode fixes: switched ClassWriter from COMPUTE_FRAMES to COMPUTE_MAXS (class version V1_6/50) to fix Frame.merge crash when loop bodies allocate local variables; fixed boolValue descriptor from (Value)B to ()Z and intValue from (Value)I to ()I for correct INVOKEVIRTUAL stack analysis; fixed Input.Reg.Vector.Ident typeOf missing semicolon; removed extra GuiEde from task/function descriptors; added Deep type storage for tasks/functions; fixed task method double-Shallow naming; fixed task call-site lookup and INVOKEVIRTUAL to use +Shallow suffix; fixed missing semicolon in codeGenShallowNonBlockingAssign ALOAD
- 2026-03-13: GuiGenPanel reference wired through GuiMachineSpecifier → EdeJarBuilder → VerilogToEdeGen constructor (no longer null)
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
