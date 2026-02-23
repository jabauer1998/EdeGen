package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import ede.stl.gui.GuiEde;
import ede.stl.gui.GuiRam;
import ede.stl.gui.GuiJob;
import ede.stl.gui.GuiRegister;
import ede.stl.gui.GuiIO;
import ede.gen.utils.JavaJobCompiler;
import ede.gen.utils.JavaSyntaxHighlighter;

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
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.title = new GuiEdeGenField("Title of Ede Environment: ", width, 30);
        this.title.setAlignmentX(LEFT_ALIGNMENT);
        this.ramBytesPerRow = new GuiEdeGenField("Number of Bytes per Row in Ram: ", width, 30);
        this.ramBytesPerRow.setAlignmentX(LEFT_ALIGNMENT);
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        toolBar.setAlignmentX(LEFT_ALIGNMENT);

        JButton testEde = new JButton("Test Ede Environment");
        testEde.setPreferredSize(new Dimension((int)(width/3), (int)(height/12)));
        testEde.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event){
                launchEdeEnvironment();
            }
        });
        toolBar.add(testEde);
        
        JButton saveEde = new JButton("Save Ede Environment");
        saveEde.setPreferredSize(new Dimension((int)(width/3), (int)(height/12)));
        saveEde.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event){
                
            }
        });
        toolBar.add(saveEde);

        
        
        JPanel registerFormatPanel = new JPanel();
        registerFormatPanel.setLayout(new BoxLayout(registerFormatPanel, BoxLayout.X_AXIS));
        registerFormatPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel registerFormatLabel = new JLabel("Register Format: ");
        String[] formats = {"Binary", "Hexadecimal", "Decimal", "Octal"};
        this.registerFormatDropdown = new JComboBox<>(formats);
        this.registerFormatDropdown.setMaximumSize(new Dimension((int)width, 30));
        registerFormatPanel.add(registerFormatLabel);
        registerFormatPanel.add(this.registerFormatDropdown);
        registerFormatPanel.setMaximumSize(new Dimension((int)width, 30));

        JPanel ramAddressFormatPanel = new JPanel();
        ramAddressFormatPanel.setLayout(new BoxLayout(ramAddressFormatPanel, BoxLayout.X_AXIS));
        ramAddressFormatPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ramAddressFormatLabel = new JLabel("Ram Address Format: ");
        this.ramAddressFormatDropdown = new JComboBox<>(formats);
        this.ramAddressFormatDropdown.setMaximumSize(new Dimension((int)width, 30));
        ramAddressFormatPanel.add(ramAddressFormatLabel);
        ramAddressFormatPanel.add(this.ramAddressFormatDropdown);
        ramAddressFormatPanel.setMaximumSize(new Dimension((int)width, 30));

        JPanel ramFormatPanel = new JPanel();
        ramFormatPanel.setLayout(new BoxLayout(ramFormatPanel, BoxLayout.X_AXIS));
        ramFormatPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ramFormatLabel = new JLabel("Ram Format: ");
        String[] ramFormats = {"Binary", "Hexadecimal"};
        this.ramFormatDropdown = new JComboBox<>(ramFormats);
        this.ramFormatDropdown.setMaximumSize(new Dimension((int)width, 30));
        ramFormatPanel.add(ramFormatLabel);
        ramFormatPanel.add(this.ramFormatDropdown);
        ramFormatPanel.setMaximumSize(new Dimension((int)width, 30));

        this.ioSections = new ArrayList<>();

        JPanel ioHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        ioHeaderPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ioLabel = new JLabel("IO Sections:");
        ioLabel.setFont(ioLabel.getFont().deriveFont(Font.BOLD));
        JButton addIoBtn = new JButton("+ Add IO Section");
        addIoBtn.addActionListener(e -> addIoSectionRow(width));
        ioHeaderPanel.add(ioLabel);
        ioHeaderPanel.add(addIoBtn);
        ioHeaderPanel.setMaximumSize(new Dimension((int)width, 35));

        this.ioListPanel = new JPanel();
        this.ioListPanel.setLayout(new BoxLayout(this.ioListPanel, BoxLayout.Y_AXIS));
        this.ioListPanel.setAlignmentX(LEFT_ALIGNMENT);

        JScrollPane ioScroll = new JScrollPane(this.ioListPanel);
        ioScroll.setAlignmentX(LEFT_ALIGNMENT);
        ioScroll.setMaximumSize(new Dimension((int)width, 150));
        ioScroll.setPreferredSize(new Dimension((int)width, 100));
        ioScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.add(toolBar);
        this.add(ioHeaderPanel);
        this.add(ioScroll);
        this.add(title);
        this.add(ramBytesPerRow);
        this.add(registerFormatPanel);
        this.add(ramAddressFormatPanel);
        this.add(ramFormatPanel);
    }

    private void addIoSectionRow(double width) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension((int)width, 30));
        row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel tabLabel = new JLabel("Tab: ");
        JTextField tabField = new JTextField(10);
        tabField.setMaximumSize(new Dimension(150, 25));

        JLabel sectionLabel = new JLabel(" Section Title: ");
        JTextField sectionField = new JTextField(10);
        sectionField.setMaximumSize(new Dimension(150, 25));

        JLabel editLabel = new JLabel(" ");
        String[] editOptions = {"Editable", "Read Only"};
        JComboBox<String> editDropdown = new JComboBox<>(editOptions);
        editDropdown.setMaximumSize(new Dimension(100, 25));

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

        log.log("[INFO] Creating GuiEde: title=\"" + edeTitle + "\", ramBytes=" + ramBytesVal + ", bytesPerRow=" + ramBytesPerRowVal);

        GuiEde guiEde = new GuiEde(edeWidth, edeHeight, ramBytesVal, addrFmt, memFmt);
        guiEde.setUpMemory(ramBytesPerRowVal);

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
            String jobName = spec.getJobTitle();

            if ("Java Job".equals(jobType)) {
                String code = spec.getText();
                log.log("[INFO] Compiling Java Job: " + jobName);
                try {
                    String imports = spec.getImportsText();
                    java.util.List<String> jarPaths = spec.getJarPaths();
                    Callable<Void> callable = JavaJobCompiler.compile(code, imports, jarPaths, guiEde);
                    guiEde.AddJavaJob(jobName, GuiJob.TextAreaType.DEFAULT, callable, "", "", "");
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
                guiEde.gatherMetaDataFromVerilogFile(path, regFmt);
                log.log("[INFO] Adding Verilog Job: " + jobName + " (path: " + path + ")");
                guiEde.AddVerilogJob(jobName, path, "", "", "", "");
            } else if ("Exe Job".equals(jobType)) {
                String path = spec.getExePath();
                log.log("[INFO] Adding Exe Job: " + jobName + " (path: " + path + ")");
                guiEde.AddExeJob(jobName, GuiJob.TextAreaType.DEFAULT, path, "", "", "", "");
            }
        }

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
