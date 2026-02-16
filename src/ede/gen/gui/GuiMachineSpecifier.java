package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GuiMachineSpecifier extends JPanel{
    private EdeGenField title;
    private EdeGenField ramBytes;
    private EdeGenField ramBytesPerRow;
    
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

        
        
        this.add(toolBar);
        this.add(title);
        this.add(ramBytes);
        this.add(ramBytesPerRow);
    }
}
