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

## External Dependencies
- **EdeStl**: Git submodule (`lib/EdeStl/`) providing core emulator framework functionalities.
- **ASM 9.6**: Bytecode manipulation framework, specifically `asm-9.6.jar`, `asm-util-9.6.jar`, `asm-tree-9.6.jar`, `asm-analysis-9.6.jar` (used by EdeStl for Verilog compilation).
- **OpenJDK 25.0.2**: The required Java Development Kit version for compilation and execution.