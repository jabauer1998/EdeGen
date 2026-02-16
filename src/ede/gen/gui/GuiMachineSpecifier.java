package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GuiMachineSpecifier extends JPanel{
    private EdeGenField title;
    private EdeGenField ramBytes;
    private EdeGenField ramBytesPerRow;
    private JComboBox<String> registerFormatDropdown;
    private JComboBox<String> ramAddressFormatDropdown;
    
    public GuiMachineSpecifier(double width, double height){
        //First Set this Panel to be a vertical Panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.title = new EdeGenField("Title of Ede Environment: ", width, 30);
        this.title.setAlignmentX(LEFT_ALIGNMENT);
        this.ramBytes = new EdeGenField("Number of Bytes in Ram: ", width, 30);
        this.ramBytes.setAlignmentX(LEFT_ALIGNMENT);
        this.ramBytesPerRow = new EdeGenField("Number of Bytes per Row in Ram: ", width, 30);
        this.ramBytesPerRow.setAlignmentX(LEFT_ALIGNMENT);
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        toolBar.setAlignmentX(LEFT_ALIGNMENT);

        JButton testEde = new JButton("Test Ede Environment");
        testEde.setPreferredSize(new Dimension((int)(width/3), (int)(height/12)));
        testEde.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event){
                String myTitle = title.getInputText();
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JFrame frame = new JFrame(myTitle);
                            frame.pack();
                            frame.setVisible(true);
                        }
                });
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

        this.add(toolBar);
        this.add(title);
        this.add(ramBytes);
        this.add(ramBytesPerRow);
        this.add(registerFormatPanel);
        this.add(ramAddressFormatPanel);
    }
}
