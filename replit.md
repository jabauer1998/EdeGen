# EdeGen - Emulator Development Environment Generator Tool

## Overview
EdeGen is a Java Swing desktop application designed to generate and configure emulator development environments. It provides a visual interface for users to define machine and job specifications, ultimately generating a `GuiEde` instance from the `EdeStl` library. The tool aims to streamline the setup of complex emulator environments, offering a user-friendly way to define hardware and software interactions within an emulated system. Its key capabilities include runtime compilation of user-defined Java jobs, testing of `GuiEde` instances, and generating portable FAT JARs of the configured emulator.

## User Preferences
I prefer clear and concise information. When making changes, please explain the reasoning and impact. For new features, I appreciate a brief discussion of design choices before implementation. I prefer an iterative development approach, focusing on core functionality first and then refining it.

## System Architecture
The application is a Java Swing desktop application targeting OpenJDK 25.0.2. The core source code is located in `src/ede/gen/`, with `EdeGenerator.java` as the main entry point. GUI components are organized under `gui/`.

### UI/UX Decisions
The application utilizes a standard Java Swing look and feel. It features dynamic addition/removal of list elements for IO sections in the Machine Specifier, and a `JTabbedPane` for organizing job configurations. Integrated Java code editors include syntax highlighting. Progress indicators are used for long-running operations.

### Technical Implementations
- **Runtime Java Job Compilation**: User-provided Java code is compiled at runtime into `EdeCallable` objects using `javax.tools.JavaCompiler`.
- **Ede Environment Testing**: The tool can launch a `GuiEde` instance from `EdeStl.jar` in a new JFrame for immediate testing of configured environments.
- **Environment Saving**: The application generates portable FAT JARs that include compiled Verilog (via ASM bytecode), compiled Java jobs, and necessary `EdeStl` components. For compiled Verilog mode, the `EdeStl` interpreter, parser, AST, and passes are excluded.
- **Configuration Management**: The entire application state (machine settings, IO sections, job specifiers) can be saved and loaded as an XML file using `javax.xml` serialization via `EdeConfigManager`.
- **Feature Specifications**:
    - **IO Sections**: Dynamic configuration of `GuiEde` IO sections (tab name, title, editability).
    - **Job Types**: Supports Verilog (file selection), Java (code editor), and Exe (file selection) job types.
    - **Syntax Highlighting**: Provides color-coded syntax highlighting for Java code editors.
    - **Executable JAR Generation**: Creates self-contained JARs for the defined emulator environment.

## External Dependencies
- **EdeStl**: A git submodule (`lib/EdeStl/`) providing core emulator framework functionalities. It is compiled into `lib/EdeStl/bin/EdeStl.jar`.
- **ASM 9.6**: Bytecode manipulation framework (`asm-9.6.jar`, `asm-util-9.6.jar`, `asm-tree-9.6.jar`, `asm-analysis-9.6.jar`) used by EdeStl for Verilog compilation.
- **OpenJDK 25.0.2**: The required Java Development Kit version.