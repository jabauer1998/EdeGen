# EdeGen - Emulator Development Environment Generator Tool

## Overview
EdeGen is a Java Swing desktop application designed to generate emulator development environments. Its primary purpose is to provide a visual interface for users to configure machine specifications and job specifications, ultimately generating a GuiEde instance from the EdeStl library. This tool aims to streamline the process of setting up complex emulator environments, offering a user-friendly way to define hardware and software interactions within an emulated system.

## User Preferences
I prefer clear and concise information. When making changes, please explain the reasoning and impact. For new features, I appreciate a brief discussion of design choices before implementation. I prefer an iterative development approach, focusing on core functionality first and then refining it.

## System Architecture
The application is built in Java using the Swing GUI framework, targeting OpenJDK 25.0.2. It compiles Java source files with `EdeStl.jar` on the classpath. The core source code resides in `src/ede/gen/`, with `EdeGenerator.java` as the main entry point. GUI components are organized under `gui/`. Key utilities include `JavaJobCompiler` for runtime compilation of Java code into `EdeCallable` objects, `JavaSyntaxHighlighter` for the integrated code editor, and `EdeJarBuilder` for creating executable JARs.

The project incorporates `EdeStl` as a git submodule, located at `lib/EdeStl/`, which is compiled into `lib/EdeStl/bin/EdeStl.jar`. Output compiled classes are placed in the `bin/` directory.

### UI/UX Decisions
The application uses a standard Java Swing look and feel. The GUI features dynamically add/remove list elements for IO sections in the Machine Specifier and a JTabbedPane for organizing job configurations. Java code editors include syntax highlighting for improved readability. The application also includes progress indicators for long-running operations like JAR generation.

### Technical Implementations
- **Runtime Java Job Compilation**: Java code entered by the user is compiled at runtime into `EdeCallable` objects using `javax.tools.JavaCompiler`.
- **Ede Environment Testing**: The tool can collect form parameters and launch a `GuiEde` instance from `EdeStl.jar` in a new JFrame for immediate testing.
- **Environment Saving**: The application can generate a portable FAT JAR. This JAR includes compiled Verilog (generated via ASM bytecode), compiled Java jobs, and the necessary `EdeStl` components. For compiled Verilog mode, the interpreter, parser, AST, and passes are excluded from `EdeStl`.
- **Configuration Management**: The full form state (machine settings, IO sections, job specifiers) can be saved and loaded as an XML file using `javax.xml` serialization via `EdeConfigManager`.

### Feature Specifications
- **IO Sections**: Dynamic configuration of `GuiEde` IO sections, including tab name, section title, and editability.
- **Job Types**: Supports Verilog (file selection), Java (code editor), and Exe (file selection) job types.
- **Syntax Highlighting**: Provides color-coded syntax highlighting for Java code editors.
- **Save/Load Config**: Functionality to persist and retrieve the entire application configuration via XML.
- **Executable JAR Generation**: Ability to create self-contained JARs including all necessary components for the defined emulator environment.

## Recent Changes
- 2026-04-26: Changed `GuiEde.getRegisterValue(String)` and `getRegisterValue(int)` (and the delegating chain through `GuiMachine` and `GuiRegisterFile`) to return `ede.stl.values.Value` instead of `long`. The conversion logic now lives in `GuiRegisterFile.toOptimalValue(long, int)`: it constructs a `VectorVal(width-1, 0)` (width comes from the new `GuiRegister.getRegisterLength()` getter exposing `RegisterDecimalLength`), fills each bit via `vec.setValue(i, new RegVal(((raw >> i) & 1L) != 0))`, then returns `Utils.getOptimalUnsignedForm(vec.longValue())`. Width is clamped to `[1..64]`. `GuiEde` keeps EDT thread-safety using `SwingUtilities.invokeAndWait` with a `Value[1]` capture array. Updated the only consumer, `EdeRegVal`, so all bit-manipulation/value-accessor sites (`setBitAtIndex`, `getBitAtIndex`, `setBitsAtIndex`, `realValue`, `longValue`, `intValue`, `shortValue`, `byteValue`, `toString`, `boolValue`) call the appropriate accessor on the returned `Value`. Note: `getOptimalUnsignedForm` shrinks the result type to the smallest unsigned form that fits the value (e.g., a 32-bit register holding `5` returns `UnsignedByteVal`); this was explicitly requested.
- 2026-04-26: Added single-arg `$display` branch to `VerilogToEdeGen.codeGenShallowSystemTaskCall`: routes calls like `$display("Program executed succesfully!!!")` and `$display("Error: ...")` to `GuiEde.appendIoText("StandardOutput", val.toString())` instead of falling through to the parent's `Utils.display → System.out.println`. Required adding `return;` after the new branch to prevent double codegen (the new bytecode plus the super call's bytecode would otherwise both emit, duplicating output and breaking the verifier).
- 2026-04-26: Fixed `getShallowSliceFromFromIndices` in Utils to handle `LongVal`: when a function parameter typed as `input reg [31:0]` arrives as a `LongVal` at runtime, bit-slice expressions like `instruction[24:21]` were throwing "Unknown slice type". Fixed by converting `LongVal → asVector()` first, then applying `getShallowSlice` as normal.
- 2026-04-26: Fixed two `casez`/`case`/`caseX` bugs in compiled Verilog (VerilogToJavaGen + Utils):
  1. **Fall-through bug**: all three case generators (regular, caseX, caseZ) were using a shared `endStatLabel` placed immediately after each arm's body, so after any matched arm executed, control fell through to the next arm and eventually always hit the `default` clause. Fixed by adding one `overallEndLabel` per case statement and emitting `GOTO overallEndLabel` after every arm body (matched and default).
  2. **caseBoolean target/Val swap**: `Utils.caseBoolean(target, Val)` was checking `Val instanceof Pattern` instead of `target instanceof Pattern`. In `casez`, the case item (target) is the Pattern; the instruction (Val) is a plain value. Fixing the check to try `target instanceof Pattern` first makes `casez` arms with `z` bits (e.g. `DATAPROC = bzzzz00z...`) match correctly. Both bugs together caused every `casez` with don't-care bits to silently fall through to the default arm, producing "Unidentified instruction when decoding instruction" errors in the compiled/JAR execution path.


- 2026-04-26: Fixed VectorVal.setValue(Value) index traversal bug: the previous position-based walk (from index1 downward) was mapping MSB-of-this to MSB-of-vec; when assigning a scalar (e.g. R15 = R15 + 4 → LongVal(4).asVector() = [63:0]) to a [31:0] register, bits 63-32 (all zero) were copied instead of bits 31-0, making register updates silently produce zero. Fixed by walking from getStart() (minimum/LSB index) upward for each vector independently, matching Utils.shallowAssign(VectorVal, VectorVal) semantics. This was the root cause of the INSTR-never-updating / infinite-loop bug.
- 2026-04-26: VectorVal.setValue(Value) rewritten to use asVector() + per-bit signal copy via getStateSignal()/setSignal() instead of Utils.shallowAssign(this, exp.longValue()); preserves circuit signal topology; removed unused Utils import.
- 2026-04-26: Fixed caseBoolean descriptor B→Z in VerilogToJavaGen codeGenShallowCaseStatement and codeGenCaseZStatement (NoSuchMethodError crash fix); changed blocking/non-blocking identifier assignment in VerilogToJavaGen from ASTORE/shallowAssignValue to INVOKEINTERFACE Value.setValue; fixed non-blocking field case SWAP+GETFIELD stack ordering bug; VerilogToEdeGen EDE-typed field assignment changed from setValueOfIdent to setValue.
- 2026-04-25: Removed Pointer<Value> from all interpreter symbol tables; lookupVariable() returns Value directly; all Ptr.deRefrence()/Ptr.assign() calls replaced with value.setValue(exp); fixed setValue implementations on VectorVal, Node, EdeStatVal, EdeRegVal.

## External Dependencies
- **EdeStl**: Git submodule (`lib/EdeStl/`) providing core emulator framework functionalities.
- **ASM 9.6**: Bytecode manipulation framework, specifically `asm-9.6.jar`, `asm-util-9.6.jar`, `asm-tree-9.6.jar`, `asm-analysis-9.6.jar` (used by EdeStl for Verilog compilation).
- **OpenJDK 25.0.2**: The required Java Development Kit version for compilation and execution.