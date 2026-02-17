package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import ede.stl.gui.GuiEde;
import ede.stl.gui.GuiRam;
import ede.stl.gui.GuiJob;

public class GuiMachineSpecifier extends JPanel{
    private GuiEdeGenField title;
    private GuiEdeGenField ramBytes;
    private GuiEdeGenField ramBytesPerRow;
    private JComboBox<String> registerFormatDropdown;
    private JComboBox<String> ramAddressFormatDropdown;
    private JComboBox<String> ramFormatDropdown;
    private GuiJobSpecifierList jobList;
    
    public GuiMachineSpecifier(double width, double height, GuiJobSpecifierList jobList){
        this.jobList = jobList;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.title = new GuiEdeGenField("Title of Ede Environment: ", width, 30);
        this.title.setAlignmentX(LEFT_ALIGNMENT);
        this.ramBytes = new GuiEdeGenField("Number of Bytes in Ram: ", width, 30);
        this.ramBytes.setAlignmentX(LEFT_ALIGNMENT);
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

        this.add(toolBar);
        this.add(title);
        this.add(ramBytes);
        this.add(ramBytesPerRow);
        this.add(registerFormatPanel);
        this.add(ramAddressFormatPanel);
        this.add(ramFormatPanel);
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
        String selected = (String) ramFormatDropdown.getSelectedItem();
        if ("Hexadecimal".equals(selected)) {
            return GuiRam.MemoryFormat.HEXADECIMAL;
        }
        return GuiRam.MemoryFormat.BINARY;
    }

    private void launchEdeEnvironment() {
        String edeTitle = title.getInputText();
        if (edeTitle == null || edeTitle.trim().isEmpty()) {
            edeTitle = "Ede Environment";
        }

        int ramBytesVal = 256;
        try {
            ramBytesVal = Integer.parseInt(ramBytes.getInputText().trim());
        } catch (NumberFormatException e) {
        }

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

        GuiEde guiEde = new GuiEde(edeWidth, edeHeight, ramBytesVal, addrFmt, memFmt);
        guiEde.setUpMemory(ramBytesPerRowVal);

        ArrayList<GuiJobSpecifier> specs = jobList.getJobSpecifiers();
        for (int i = 0; i < specs.size(); i++) {
            GuiJobSpecifier spec = specs.get(i);
            String jobType = spec.getSelectedJobType();
            String jobName = spec.getJobTitle();

            if ("Java Job".equals(jobType)) {
                String code = spec.getText();
                try {
                    Callable<Void> callable = JavaJobCompiler.compile(code);
                    guiEde.AddJavaJob(jobName, GuiJob.TextAreaType.DEFAULT, callable, "", "", "");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Failed to compile " + jobName + ":\n" + e.getMessage(),
                        "Compilation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if ("Verilog Job".equals(jobType)) {
                String path = spec.getVerilogPath();
                guiEde.AddVerilogJob(jobName, path, "", "", "", "");
            } else if ("Exe Job".equals(jobType)) {
                String path = spec.getExePath();
                guiEde.AddExeJob(jobName, GuiJob.TextAreaType.DEFAULT, path, "", "", "", "");
            }
        }

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
