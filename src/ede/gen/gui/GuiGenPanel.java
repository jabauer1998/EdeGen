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
        JPanel logPanel = new JPanel(new BorderLayout());
        JLabel logTitle = new JLabel("Ede Generator Tool Log", SwingConstants.CENTER);
        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(this.log, BorderLayout.CENTER);

        this.add(panel, BorderLayout.CENTER);
        this.add(logPanel, BorderLayout.SOUTH);
    }
}
