package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import ede.stl.common.EdeCallable;
import ede.stl.gui.GuiEde;
import ede.stl.gui.GuiRam;
import ede.stl.gui.GuiJob;
import ede.stl.gui.GuiRegister;
import ede.stl.gui.GuiIO;
import ede.gen.utils.JavaJobCompiler;
import ede.gen.utils.JavaSyntaxHighlighter;
import ede.gen.utils.EdeJarBuilder;

public class GuiMachineSpecifier extends JPanel{
    private GuiEdeGenField title;
    private GuiEdeGenField ramBytesPerRow;
    private JComboBox<String> registerFormatDropdown;
    private JComboBox<String> ramAddressFormatDropdown;
    private JComboBox<String> ramFormatDropdown;
    private GuiJobSpecifierList jobList;
    private GuiEdeLog log;
    private JPanel ioListPanel;
    private ArrayList<IoSectionEntry> ioSections;

    private static class IoSectionEntry {
        JTextField tabNameField;
        JTextField sectionTitleField;
        JComboBox<String> editableDropdown;
        JPanel panel;

        IoSectionEntry(JTextField tab, JTextField section, JComboBox<String> editable, JPanel panel) {
            this.tabNameField = tab;
            this.sectionTitleField = section;
            this.editableDropdown = editable;
            this.panel = panel;
        }
    }
    
    public GuiMachineSpecifier(double width, double height, GuiJobSpecifierList jobList, GuiEdeLog log){
        this.jobList = jobList;
        this.log = log;
        this.setLayout(new BorderLayout());

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        toolBar.setAlignmentX(LEFT_ALIGNMENT);
        toolBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton testEde = new JButton("Test Ede Environment");
        testEde.setPreferredSize(new Dimension((int)(width/3), 40));
        testEde.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event){
                launchEdeEnvironment();
            }
        });
        toolBar.add(testEde);

        JButton saveEde = new JButton("Save Ede Environment");
        saveEde.setPreferredSize(new Dimension((int)(width/3), 40));
        saveEde.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event){
                saveEdeEnvironment();
            }
        });
        toolBar.add(saveEde);

        JButton clearLog = new JButton("Clear Log");
        clearLog.setPreferredSize(new Dimension((int)(width/6), 40));
        clearLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event){
                log.setText("");
            }
        });
        toolBar.add(clearLog);

        this.title = new GuiEdeGenField("Title of Ede Environment: ", width, 30);
        this.title.setAlignmentX(LEFT_ALIGNMENT);
        this.title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.ramBytesPerRow = new GuiEdeGenField("Number of Bytes per Row in Ram: ", width, 30);
        this.ramBytesPerRow.setAlignmentX(LEFT_ALIGNMENT);
        this.ramBytesPerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel registerFormatPanel = new JPanel();
        registerFormatPanel.setLayout(new BoxLayout(registerFormatPanel, BoxLayout.X_AXIS));
        registerFormatPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel registerFormatLabel = new JLabel("Register Format: ");
        String[] formats = {"Binary", "Hexadecimal", "Decimal", "Octal"};
        this.registerFormatDropdown = new JComboBox<>(formats);
        this.registerFormatDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        registerFormatPanel.add(registerFormatLabel);
        registerFormatPanel.add(this.registerFormatDropdown);
        registerFormatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel ramAddressFormatPanel = new JPanel();
        ramAddressFormatPanel.setLayout(new BoxLayout(ramAddressFormatPanel, BoxLayout.X_AXIS));
        ramAddressFormatPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ramAddressFormatLabel = new JLabel("Ram Address Format: ");
        this.ramAddressFormatDropdown = new JComboBox<>(formats);
        this.ramAddressFormatDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        ramAddressFormatPanel.add(ramAddressFormatLabel);
        ramAddressFormatPanel.add(this.ramAddressFormatDropdown);
        ramAddressFormatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel ramFormatPanel = new JPanel();
        ramFormatPanel.setLayout(new BoxLayout(ramFormatPanel, BoxLayout.X_AXIS));
        ramFormatPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ramFormatLabel = new JLabel("Ram Format: ");
        String[] ramFormats = {"Binary", "Hexadecimal"};
        this.ramFormatDropdown = new JComboBox<>(ramFormats);
        this.ramFormatDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        ramFormatPanel.add(ramFormatLabel);
        ramFormatPanel.add(this.ramFormatDropdown);
        ramFormatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        topSection.add(toolBar);
        topSection.add(title);
        topSection.add(ramBytesPerRow);
        topSection.add(registerFormatPanel);
        topSection.add(ramAddressFormatPanel);
        topSection.add(ramFormatPanel);

        this.ioSections = new ArrayList<>();

        JPanel ioHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        ioHeaderPanel.setAlignmentX(LEFT_ALIGNMENT);
        ioHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel ioLabel = new JLabel("IO Sections:");
        ioLabel.setFont(ioLabel.getFont().deriveFont(Font.BOLD));
        JButton addIoBtn = new JButton("+ Add IO Section");
        addIoBtn.addActionListener(e -> addIoSectionRow(width));
        ioHeaderPanel.add(ioLabel);
        ioHeaderPanel.add(addIoBtn);

        this.ioListPanel = new JPanel();
        this.ioListPanel.setLayout(new BoxLayout(this.ioListPanel, BoxLayout.Y_AXIS));
        this.ioListPanel.setAlignmentX(LEFT_ALIGNMENT);

        JScrollPane ioScroll = new JScrollPane(this.ioListPanel);
        ioScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel ioContainer = new JPanel(new BorderLayout());
        ioContainer.add(ioHeaderPanel, BorderLayout.NORTH);
        ioContainer.add(ioScroll, BorderLayout.CENTER);

        this.add(topSection, BorderLayout.NORTH);
        this.add(ioContainer, BorderLayout.CENTER);
    }

    private void addIoSectionRow(double width) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel tabLabel = new JLabel("Tab: ");
        JTextField tabField = new JTextField(10);
        tabField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel sectionLabel = new JLabel(" Section Title: ");
        JTextField sectionField = new JTextField(10);
        sectionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel editLabel = new JLabel(" ");
        String[] editOptions = {"Editable", "Read Only"};
        JComboBox<String> editDropdown = new JComboBox<>(editOptions);
        editDropdown.setMaximumSize(new Dimension(120, 25));

        JButton removeBtn = new JButton("X");
        removeBtn.setMargin(new Insets(0, 4, 0, 4));

        IoSectionEntry entry = new IoSectionEntry(tabField, sectionField, editDropdown, row);

        removeBtn.addActionListener(e -> {
            ioSections.remove(entry);
            ioListPanel.remove(row);
            ioListPanel.revalidate();
            ioListPanel.repaint();
        });

        row.add(tabLabel);
        row.add(tabField);
        row.add(sectionLabel);
        row.add(sectionField);
        row.add(editLabel);
        row.add(editDropdown);
        row.add(Box.createHorizontalStrut(4));
        row.add(removeBtn);

        ioSections.add(entry);
        ioListPanel.add(row);
        ioListPanel.revalidate();
        ioListPanel.repaint();
    }

    private GuiRam.AddressFormat getSelectedAddressFormat() {
        String selected = (String) ramAddressFormatDropdown.getSelectedItem();
        switch (selected) {
            case "Hexadecimal": return GuiRam.AddressFormat.HEXIDECIMAL;
            case "Decimal": return GuiRam.AddressFormat.DECIMAL;
            case "Octal": return GuiRam.AddressFormat.OCTAL;
            default: return GuiRam.AddressFormat.BINARY;
        }
    }

    private GuiRam.MemoryFormat getSelectedMemoryFormat() {
        String selected = (String)ramFormatDropdown.getSelectedItem();
        if ("Hexadecimal".equals(selected)) {
            return GuiRam.MemoryFormat.HEXADECIMAL;
        }
        return GuiRam.MemoryFormat.BINARY;
    }

    private GuiRegister.Format getSelectedRegisterFormat() {
        String selected = (String)registerFormatDropdown.getSelectedItem();
        if(selected.equals("Hexidecimal")){
            return GuiRegister.Format.HEXIDECIMAL;
        } else {
            return GuiRegister.Format.BINARY;
        }
    }

    private String getAddressFormatEnumName() {
        String selected = (String) ramAddressFormatDropdown.getSelectedItem();
        switch (selected) {
            case "Hexadecimal": return "HEXIDECIMAL";
            case "Decimal": return "DECIMAL";
            case "Octal": return "OCTAL";
            default: return "BINARY";
        }
    }

    private String getMemoryFormatEnumName() {
        String selected = (String) ramFormatDropdown.getSelectedItem();
        if ("Hexadecimal".equals(selected)) {
            return "HEXADECIMAL";
        }
        return "BINARY";
    }

    private String getRegisterFormatEnumName() {
        String selected = (String) registerFormatDropdown.getSelectedItem();
        if ("Hexidecimal".equals(selected)) {
            return "HEXIDECIMAL";
        }
        return "BINARY";
    }

    private boolean validateJobs() {
        ArrayList<GuiJobSpecifier> specs = jobList.getJobSpecifiers();

        if (specs.isEmpty()) {
            log.log("[ERROR] No jobs defined. At least one job is required.");
            return false;
        }

        log.log("[CHECK] Validating job list (" + specs.size() + " jobs)...");

        int lastIndex = specs.size() - 1;
        GuiJobSpecifier lastJob = specs.get(lastIndex);
        String lastJobType = lastJob.getSelectedJobType();
        String lastJobName = lastJob.getJobTitle();

        log.log("[CHECK] Checking last job: " + lastJobName + " (type: " + lastJobType + ")");
        if (!"Verilog Job".equals(lastJobType)) {
            log.log("[ERROR] Last job \"" + lastJobName + "\" must be a Verilog Job, but is " + lastJobType + ".");
            return false;
        }
        log.log("[PASS] Last job \"" + lastJobName + "\" is a Verilog Job.");

        for (int i = 0; i < lastIndex; i++) {
            GuiJobSpecifier spec = specs.get(i);
            String jobType = spec.getSelectedJobType();
            String jobName = spec.getJobTitle();

            log.log("[CHECK] Checking " + jobName + " (type: " + jobType + ")");
            if ("Verilog Job".equals(jobType)) {
                log.log("[ERROR] " + jobName + " is a Verilog Job. Only Java or Exe Jobs are allowed before the last job.");
                return false;
            }
            log.log("[PASS] " + jobName + " is a valid " + jobType + ".");
        }

        log.log("[PASS] All job validations passed.");
        return true;
    }

    private void saveEdeEnvironment() {
        log.log("--- Save Ede Environment ---");

        String edeTitle = title.getInputText();
        if (edeTitle == null || edeTitle.trim().isEmpty()) {
            edeTitle = "Ede Environment";
        }

        int ramBytesPerRowVal = 16;
        try {
            ramBytesPerRowVal = Integer.parseInt(ramBytesPerRow.getInputText().trim());
        } catch (NumberFormatException e) {
        }

        String addrFmtName = getAddressFormatEnumName();
        String memFmtName = getMemoryFormatEnumName();
        String regFmtName = getRegisterFormatEnumName();

        java.util.List<EdeJarBuilder.IoSectionData> ioData = new ArrayList<>();
        for (IoSectionEntry entry : ioSections) {
            String tabName = entry.tabNameField.getText().trim();
            String sectionTitle = entry.sectionTitleField.getText().trim();
            if (tabName.isEmpty() || sectionTitle.isEmpty()) continue;
            String editChoice = (String) entry.editableDropdown.getSelectedItem();
            boolean readOnly = "Read Only".equals(editChoice);
            ioData.add(new EdeJarBuilder.IoSectionData(tabName, sectionTitle, readOnly));
        }

        ArrayList<GuiJobSpecifier> specs = jobList.getJobSpecifiers();
        java.util.List<EdeJarBuilder.JobData> jobData = new ArrayList<>();
        for (GuiJobSpecifier spec : specs) {
            String jobType = spec.getSelectedJobType();
            String jobName = spec.getJobName();
            EdeJarBuilder.JobData jd = new EdeJarBuilder.JobData(jobType, jobName);
            if ("Java Job".equals(jobType)) {
                jd.code = spec.getText();
                jd.imports = spec.getImportsText();
                jd.jarPaths = spec.getJarPaths();
            } else if ("Verilog Job".equals(jobType)) {
                jd.verilogPath = spec.getVerilogPath();
                jd.verilogInputFile = spec.getVerilogInputFile();
            } else if ("Exe Job".equals(jobType)) {
                jd.exePath = spec.getExePath();
            }
            jobData.add(jd);
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Ede JAR - Select Output Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            log.log("[INFO] Save cancelled.");
            return;
        }
        java.io.File outputDir = chooser.getSelectedFile();

        String edeStlJarPath = System.getProperty("user.dir") + java.io.File.separator + "lib" + java.io.File.separator + "EdeStl.jar";

        final String finalEdeTitle = edeTitle;
        final int finalRamBytesPerRow = ramBytesPerRowVal;
        log.log("[INFO] Building JAR: " + edeTitle + ".jar ...");

        try {
            java.io.File jarFile = EdeJarBuilder.buildJar(
                finalEdeTitle, finalRamBytesPerRow,
                addrFmtName, memFmtName, regFmtName,
                ioData, jobData, edeStlJarPath, outputDir
            );
            log.log("[PASS] JAR saved successfully: " + jarFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                "Ede JAR saved:\n" + jarFile.getAbsolutePath(),
                "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            log.log("[ERROR] Failed to build JAR: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Failed to build JAR:\n" + e.getMessage(),
                "Build Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void launchEdeEnvironment() {
        log.log("--- Test Ede Environment ---");

        if (!validateJobs()) {
            log.log("[ABORTED] Ede Environment generation aborted due to validation errors.");
            return;
        }

        String edeTitle = title.getInputText();
        if (edeTitle == null || edeTitle.trim().isEmpty()) {
            edeTitle = "Ede Environment";
        }

        int ramBytesVal = 256;

        int ramBytesPerRowVal = 16;
        try {
            ramBytesPerRowVal = Integer.parseInt(ramBytesPerRow.getInputText().trim());
        } catch (NumberFormatException e) {
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        double edeWidth = screenSize.width;
        double edeHeight = screenSize.height;

        GuiRam.AddressFormat addrFmt = getSelectedAddressFormat();
        GuiRam.MemoryFormat memFmt = getSelectedMemoryFormat();
        GuiRegister.Format regFmt = getSelectedRegisterFormat();

        log.log("[INFO] Creating GuiEde: title=\"" + edeTitle + "\", bytesPerRow=" + ramBytesPerRowVal);

        GuiEde guiEde = new GuiEde(edeWidth, edeHeight, ramBytesPerRowVal, addrFmt, memFmt);

        guiEde.AddIoSection("IO", "StandardOutput", GuiIO.Editable.READ_ONLY);
        guiEde.AddIoSection("IO", "StandardInput", GuiIO.Editable.EDITABLE);
        guiEde.AddIoSection("Errors", "StandardError", GuiIO.Editable.READ_ONLY);

        for (IoSectionEntry entry : ioSections) {
            String tabName = entry.tabNameField.getText().trim();
            String sectionTitle = entry.sectionTitleField.getText().trim();
            if (tabName.isEmpty() || sectionTitle.isEmpty()) {
                log.log("[WARN] Skipping IO section with empty tab name or section title.");
                continue;
            }
            String editChoice = (String) entry.editableDropdown.getSelectedItem();
            GuiIO.Editable editable = "Read Only".equals(editChoice)
                ? GuiIO.Editable.READ_ONLY
                : GuiIO.Editable.EDITABLE;
            guiEde.AddIoSection(tabName, sectionTitle, editable);
            log.log("[INFO] Added IO section: tab=\"" + tabName + "\", title=\"" + sectionTitle + "\", " + editChoice);
        }

        ArrayList<GuiJobSpecifier> specs = jobList.getJobSpecifiers();
        
        for (int i = 0; i < specs.size(); i++) {
            GuiJobSpecifier spec = specs.get(i);
            String jobType = spec.getSelectedJobType();
            String jobName = spec.getJobName();

            if ("Java Job".equals(jobType)) {
                String code = spec.getText();
                log.log("[INFO] Compiling Java Job: " + jobName);
                try {
                    String imports = spec.getImportsText();
                    java.util.List<String> jarPaths = spec.getJarPaths();
                    EdeCallable callable = JavaJobCompiler.compile(code, imports, jarPaths, guiEde);
                    guiEde.AddJavaJob(jobName, GuiJob.TextAreaType.DEFAULT, callable);
                    log.log("[PASS] " + jobName + " compiled and added successfully.");
                } catch (Exception e) {
                    log.log("[ERROR] Failed to compile " + jobName + ": " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Failed to compile " + jobName + ":\n" + e.getMessage(),
                        "Compilation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if ("Verilog Job".equals(jobType)) {
                String path = spec.getVerilogPath();
                if (path == null || path.trim().isEmpty()) {
                    log.log("[ERROR] Verilog Job \"" + jobName + "\" has no file path specified.");
                    JOptionPane.showMessageDialog(this,
                        "Verilog Job \"" + jobName + "\" requires a file path.",
                        "Missing Verilog Path", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                java.io.File verilogFile = new java.io.File(path);
                if (!verilogFile.exists()) {
                    log.log("[ERROR] Verilog file not found: " + path);
                    JOptionPane.showMessageDialog(this,
                        "Verilog file not found:\n" + path,
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                guiEde.gatherMetaDataFromVerilogFile(path, regFmt);
                log.log("[INFO] Adding Verilog Job: " + jobName + " (path: " + path + ")");
                String verilogInputFile = spec.getVerilogInputFile();
                guiEde.AddVerilogJob(jobName, path, verilogInputFile, "StandardInput", "StandardOutput", "StandardError");
            } else if ("Exe Job".equals(jobType)) {
                String path = spec.getExePath();
                if (path == null || path.trim().isEmpty()) {
                    log.log("[ERROR] Exe Job \"" + jobName + "\" has no file path specified.");
                    JOptionPane.showMessageDialog(this,
                        "Exe Job \"" + jobName + "\" requires a file path.",
                        "Missing Exe Path", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                log.log("[INFO] Adding Exe Job: " + jobName + " (path: " + path + ")");
                guiEde.AddExeJob(jobName, GuiJob.TextAreaType.DEFAULT, path);
            }
        }

        guiEde.linkJobs();
        
        log.log("[INFO] Opening Ede Environment window: " + edeTitle);

        final String frameTitle = edeTitle;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame(frameTitle);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.add(guiEde);
                frame.setPreferredSize(screenSize);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
