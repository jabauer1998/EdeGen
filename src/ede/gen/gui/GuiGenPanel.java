package ede.gen.gui;

import javax.swing.*;
import java.awt.*;

public class GuiGenPanel extends JPanel{
    private GuiJobSpecifierList jobs;
    private GuiMachineSpecifier machine;
    private GuiEdeLog log;
    
    public GuiGenPanel(double width, double height){
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension((int)width, (int)height));
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        
        this.jobs = new GuiJobSpecifierList(width/2, 5 * height / 6);
        this.machine = new GuiMachineSpecifier(width/2, 5 * height / 6);
        this.log = new GuiEdeLog(width, height / 6);
        
        panel.add(this.jobs);
        panel.add(this.machine);
        this.add(panel, BorderLayout.CENTER);
        this.add(this.log, BorderLayout.SOUTH);
    }
}
