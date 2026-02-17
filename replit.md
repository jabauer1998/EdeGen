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
  - `gui/JavaJobCompiler.java` - Runtime Java code compilation into Callable objects
  - `gui/JavaSyntaxHighlighter.java` - Syntax highlighting for Java code editor
- **Libraries**: `lib/EdeStl.jar` - standard library dependency (compiled with Java 25)
- **Output**: Compiled classes go to `bin/`

## Key Features
- **Test Ede Environment**: Collects form parameters and opens a new JFrame with GuiEde from EdeStl.jar
- **Java Job Compilation**: Typed Java code is compiled at runtime into Callable<Void> using javax.tools.JavaCompiler
- **Syntax Highlighting**: Java code editor with color-coded keywords, types, strings, comments, numbers, annotations
- **Job Types**: Verilog (file selector), Java (code editor), Exe (file selector)

## Running
The workflow compiles all Java sources with JDK 25 and runs `ede.gen.driver.EdeGenerator` via VNC display.

## Recent Changes
- 2026-02-17: Upgraded to JDK 25 to match EdeStl.jar class version requirement
- 2026-02-17: Wired Test Ede Environment button to create GuiEde from EdeStl.jar with form parameters
- 2026-02-17: Created JavaJobCompiler for runtime compilation of Java code into Callable objects
- 2026-02-17: Added getter methods to GuiJobSpecifier (job type, paths) and updated GuiMachineSpecifier
- 2026-02-17: Fixed syntax highlighting order (comments/strings override keywords/numbers)
- 2026-02-17: Added line wrapping to JTextPane editor
- 2026-02-17: Fixed class name mismatch (EdeGenField -> GuiEdeGenField)
- 2026-02-16: Initial Replit setup - installed Java, configured VNC workflow, added .gitignore
