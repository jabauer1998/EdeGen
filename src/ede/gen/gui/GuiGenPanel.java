package ede.gen.gui;

import javax.swing.*;
import java.awt.*;

public class GuiGenPanel extends JPanel{
    private GuiJobSpecifierList jobs;
    private GuiMachineSpecifier machine;
    private GuiEdeLog log;
    
    public GuiGenPanel(double width, double height){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension((int)width, (int)height));
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.setPreferredSize(new Dimension((int)width, (int)((5 * height) / 6)));
        
        this.jobs = new GuiJobSpecifierList(width/2, 5 * height / 6);
        this.machine = new GuiMachineSpecifier(width/2, 5 * height / 6);
        this.log = new GuiEdeLog(width, height/6);
        
        panel.add(this.jobs);
        panel.add(this.machine);
        this.add(panel);
        this.add(this.log);
    }
}
