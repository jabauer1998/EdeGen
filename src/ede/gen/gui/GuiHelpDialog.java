package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GuiHelpDialog extends JDialog {

    public GuiHelpDialog(Frame owner) {
        super(owner, "Help With EdeGen", true);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Help inputting Ede Machine Specifications", buildScroll(MACHINE_HELP));
        tabs.addTab("Help inputting Ede Job Specifications", buildScroll(JOB_HELP));

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("EdeGen \u2014 Emulator Development Environment Generator");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.add(close);

        content.add(title, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        content.add(buttonRow, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(820, 640);
        setLocationRelativeTo(owner);
    }

    private JScrollPane buildScroll(String text) {
        JTextPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setText(text);
        pane.setEditable(false);
        pane.setCaretPosition(0);
        pane.setBorder(new EmptyBorder(8, 12, 8, 12));
        JScrollPane scroll = new JScrollPane(pane);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static final String STYLE =
        "<style>" +
        "body { font-family: sans-serif; font-size: 12px; color: #222; }" +
        "h2 { color: #0066cc; margin-top: 14px; margin-bottom: 4px; font-size: 14px; }" +
        "h3 { color: #333; margin-top: 10px; margin-bottom: 2px; font-size: 13px; }" +
        "p { margin: 4px 0 8px 0; }" +
        "ul { margin: 4px 0 8px 18px; }" +
        "li { margin-bottom: 3px; }" +
        "code { background: #f0f0f0; padding: 1px 4px; font-family: monospace; }" +
        "</style>";

    private static final String MACHINE_HELP = "<html>" + STYLE + "<body>" +
        "<p>The <b>Machine Specifier</b> (right-hand panel of the Home tab) describes the emulated " +
        "hardware that the generated <code>GuiEde</code> environment will present at runtime. " +
        "Everything you set here becomes part of the launched window's title bar, layout, and " +
        "default formats.</p>" +

        "<h2>Title and Version</h2>" +
        "<p>Three fields across the top.</p>" +
        "<ul>" +
        "<li><b>Title</b> \u2014 Free-form name of your machine. Used in the launched <code>GuiEde</code> " +
        "window title and as the base name of any saved JAR.</li>" +
        "<li><b>Major</b> / <b>Minor</b> \u2014 Two integer spinners (0\u20139999). Combined with the title " +
        "as <code>Title-Major.Minor</code> at runtime, and sanitized to <code>Title-Major_Minor.jar</code> " +
        "when exported.</li>" +
        "</ul>" +

        "<h2>Number of Bytes per Row in Ram</h2>" +
        "<p>Spinner (1\u20131024, default 16). Controls how many bytes the RAM viewer in the launched " +
        "<code>GuiEde</code> instance shows per row. Pick a power of two for clean address alignment.</p>" +

        "<h2>Display Formats</h2>" +
        "<p>Three dropdowns choosing the default numeric base for the <code>GuiEde</code> viewers:</p>" +
        "<ul>" +
        "<li><b>RAM Address Format</b> \u2014 base used for the address column.</li>" +
        "<li><b>RAM Format</b> \u2014 base used for memory cell values.</li>" +
        "<li><b>Register Format</b> \u2014 base used for register values.</li>" +
        "</ul>" +
        "<p>Common choices are Binary, Hex, or Decimal. The end user can still flip these at runtime.</p>" +

        "<h2>IO Sections</h2>" +
        "<p>Defines the tabs that appear in the IO panel of the launched <code>GuiEde</code>. Three " +
        "sections are pre-installed and cannot be removed (they appear with a lock icon and disabled " +
        "fields):</p>" +
        "<ul>" +
        "<li><b>StandardError</b> \u2192 Errors tab</li>" +
        "<li><b>StandardOutput</b> \u2192 IO tab</li>" +
        "<li><b>StandardInput</b> \u2192 IO tab (editable for the user)</li>" +
        "</ul>" +
        "<p>Use <i>Add IO Section</i> to add your own. Each row has:</p>" +
        "<ul>" +
        "<li><b>Tab Name</b> \u2014 grouping label; sections that share a Tab Name are placed in the " +
        "same tab in <code>GuiEde</code>.</li>" +
        "<li><b>Section Title</b> \u2014 label shown above the section's text area.</li>" +
        "<li><b>Read Only</b> \u2014 if checked, the user cannot type into this section at runtime " +
        "(use it for output-only streams).</li>" +
        "<li><b>X</b> button \u2014 removes a custom row. The three defaults have no X button.</li>" +
        "</ul>" +

        "<h2>Toolbar Actions (Top of Window)</h2>" +
        "<ul>" +
        "<li><b>Test \u2192 Test Ede Environment</b> \u2014 Compiles every Java job in-memory and " +
        "launches a live <code>GuiEde</code> window using your current spec. The fastest way to " +
        "verify behavior without producing a JAR.</li>" +
        "<li><b>Save \u2192 Save Ede As</b> \u2014 Compiles all jobs, bundles <code>EdeStl</code> " +
        "and any Bundle-strategy classpath JARs, and writes a self-contained executable FAT JAR " +
        "named <code>Title-Major_Minor.jar</code>.</li>" +
        "<li><b>Save \u2192 Save And Run Ede</b> \u2014 Same as above, then immediately launches " +
        "the produced JAR with <code>java -jar</code>.</li>" +
        "<li><b>Clean \u2192 Clear Ede Log</b> \u2014 Empties the bottom log panel.</li>" +
        "<li><b>Config \u2192 Save Config / Load Config</b> \u2014 Serializes (or restores) the " +
        "entire EdeGen state \u2014 machine settings, IO sections, and every job specifier \u2014 " +
        "as an XML file. Useful for sharing or versioning a machine spec.</li>" +
        "</ul>" +

        "<h2>The Log Panel</h2>" +
        "<p>The bottom third of the Home tab streams build progress, compilation errors, generated " +
        "Verilog output, and runtime messages from the launched <code>GuiEde</code>. If a build " +
        "fails, this is the first place to look.</p>" +
        "</body></html>";

    private static final String JOB_HELP = "<html>" + STYLE + "<body>" +
        "<p>The <b>Job Specifier List</b> (left-hand panel of the Home tab) defines the executable " +
        "units the emulated machine will run. Each entry is a job; you can add as many as you need, " +
        "drag them by the <b>\u2195</b> handle to reorder, or remove them with the X button.</p>" +

        "<h2>Common Job Header</h2>" +
        "<ul>" +
        "<li><b>Job Title</b> \u2014 The collapsible header label. Click the title bar to expand or " +
        "collapse the job.</li>" +
        "<li><b>Job Name</b> \u2014 Identifier exposed to <code>GuiEde</code> (used for tab labels " +
        "and references between jobs).</li>" +
        "<li><b>Job Type</b> dropdown \u2014 Switches the body between <i>Verilog Job</i>, " +
        "<i>Java Job</i>, and <i>Exe Job</i>.</li>" +
        "</ul>" +

        "<h2>Verilog Job</h2>" +
        "<p>Runs a Verilog source file. Configure with:</p>" +
        "<ul>" +
        "<li><b>Verilog Path</b> \u2014 The <code>.v</code> source file. Browse to select.</li>" +
        "<li><b>Verilog Main Module</b> \u2014 Dropdown populated from the modules parsed out of the " +
        "selected file. Pick the top-level module that should be instantiated.</li>" +
        "<li><b>Verilog Input File</b> \u2014 Optional file whose contents are streamed into the " +
        "module's input port at runtime.</li>" +
        "</ul>" +
        "<p>At save time, <code>EdeGen</code> compiles the Verilog through the <code>EdeStl</code> " +
        "Verilog\u2192bytecode pipeline (using ASM) so the resulting JAR ships the compiled module " +
        "directly \u2014 the interpreter is not bundled when you use this mode.</p>" +

        "<h2>Java Job</h2>" +
        "<p>Runs an arbitrary <code>EdeCallable</code> written in Java. Compiled at runtime via " +
        "<code>javax.tools.JavaCompiler</code> (when testing) and at build time when saving a JAR.</p>" +
        "<ul>" +
        "<li><b>Imports</b> editor (top) \u2014 The <code>import</code> statements that prepend " +
        "your code. Keep this small; treat the rest as your method body.</li>" +
        "<li><b>Code</b> editor (bottom) \u2014 The body of <code>EdeCallable.call(GuiEde ede)</code>. " +
        "You have access to the <code>ede</code> instance for I/O, RAM, and register manipulation.</li>" +
        "<li><b>Syntax Highlighting</b> checkbox \u2014 Enables Java color highlighting in both " +
        "editors and unlocks the keyword file selector.</li>" +
        "<li><b>Choose Keyword File</b> \u2014 Optional plain-text file of additional identifiers " +
        "to color as keywords (useful for DSL extensions).</li>" +
        "<li><b>Classpath JARs</b> table \u2014 Extra JARs needed by your Java code. Each row has " +
        "a <i>Link Strategy</i>:" +
        "<ul>" +
        "<li><b>Bundle</b> \u2014 The JAR's class entries are extracted into the output FAT JAR. " +
        "Best for small dependencies you want self-contained.</li>" +
        "<li><b>Classpath</b> \u2014 Only the JAR's path is recorded in the output JAR's manifest " +
        "<code>Class-Path</code> header. The JAR is loaded from its original location at runtime, " +
        "so it must remain available there.</li>" +
        "</ul>" +
        "Either way, every JAR is on the compile classpath so your code resolves against them.</li>" +
        "</ul>" +
        "<p>When this job type is added, a dedicated editor tab also opens at the top of the window " +
        "with a larger view of the same Java code.</p>" +

        "<h2>Exe Job</h2>" +
        "<p>Runs an external native executable as a job.</p>" +
        "<ul>" +
        "<li><b>Exe Path</b> \u2014 Path to the executable to launch.</li>" +
        "<li><b>Choose Keyword File</b> \u2014 Optional keyword file used for any associated " +
        "syntax highlighting context.</li>" +
        "</ul>" +
        "<p>At runtime <code>GuiEde</code> spawns the process and wires its stdio into the standard " +
        "IO sections.</p>" +

        "<h2>Reordering and Removing Jobs</h2>" +
        "<ul>" +
        "<li>Use <b>Add Job</b> at the bottom of the list to append a new job.</li>" +
        "<li>Drag the <b>\u2195</b> handle on the left edge of any job header to reorder the list. " +
        "Execution order in the launched <code>GuiEde</code> follows the list order.</li>" +
        "<li>Click the <b>X</b> on a job header to remove it. If it was a Java job, its dedicated " +
        "editor tab is closed automatically.</li>" +
        "</ul>" +

        "<h2>Tips</h2>" +
        "<ul>" +
        "<li>Use <b>Test \u2192 Test Ede Environment</b> early and often \u2014 it surfaces " +
        "compilation errors in the log panel before you commit to building a JAR.</li>" +
        "<li>If a Java job depends on classes from a Verilog job (or vice versa), put the " +
        "producer above the consumer in the list.</li>" +
        "<li>Save a config file once you have something working; reloading it restores the entire " +
        "spec including IO sections and per-jar link strategies.</li>" +
        "</ul>" +
        "</body></html>";
}
