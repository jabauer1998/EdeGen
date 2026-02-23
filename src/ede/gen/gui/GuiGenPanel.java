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
        
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.jobs, this.machine);
        horizontalSplit.setResizeWeight(0.5);
        horizontalSplit.setDividerLocation((int)(width / 2));
        horizontalSplit.setContinuousLayout(true);

        JPanel logPanel = new JPanel(new BorderLayout());
        JLabel logTitle = new JLabel("Ede Generator Tool Log", SwingConstants.CENTER);
        JScrollPane logScroll = new JScrollPane(this.log);
        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, logPanel);
        verticalSplit.setResizeWeight(0.85);
        verticalSplit.setDividerLocation((int)(5 * height / 6));
        verticalSplit.setContinuousLayout(true);

        this.add(verticalSplit, BorderLayout.CENTER);
    }
}
