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
        
        this.jobs = new GuiJobSpecifierList(width/2, 5 * height / 6);
        this.log = new GuiEdeLog(width, height / 6);
        this.machine = new GuiMachineSpecifier(width/2, 5 * height / 6, this.jobs, this.log);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.jobs, this.machine);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation((int)(width / 2));
        splitPane.setContinuousLayout(true);
        JPanel logPanel = new JPanel(new BorderLayout());
        JLabel logTitle = new JLabel("Ede Generator Tool Log", SwingConstants.CENTER);
        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(this.log, BorderLayout.CENTER);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(logPanel, BorderLayout.SOUTH);
    }
}
