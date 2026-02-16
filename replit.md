# EdeGen - Emulator Development Environment Generator Tool

## Overview
A Java Swing desktop GUI application for generating emulator development environments. It provides a visual interface for configuring machine specifications and job specifications.

## Project Architecture
- **Language**: Java (Swing GUI)
- **Build**: javac compilation from source files listed in `build/BuildList.txt`
- **Source**: `src/ede/gen/` - main source tree
  - `driver/EdeGenerator.java` - Main entry point
  - `gui/` - GUI components (panels, fields, collapsable frames, specifiers)
- **Libraries**: `lib/EdeStl.jar` - standard library dependency
- **Output**: Compiled classes go to `bin/`

## Running
The workflow compiles all Java sources and runs `ede.gen.driver.EdeGenerator` via VNC display.

## Recent Changes
- 2026-02-16: Initial Replit setup - installed Java, configured VNC workflow, added .gitignore
