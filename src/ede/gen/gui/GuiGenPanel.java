package ede.gen.gui;

import javax.swing.*;
import java.awt.*;

public class GuiGenPanel extends JPanel{
    private GuiJobSpecifierList jobs;
    private GuiMachineSpecifier machine;
    
    public GuiGenPanel(double width, double height){
	this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	this.setPreferredSize(new Dimension((int)width, (int)height));
	
	this.jobs = new GuiJobSpecifierList(width/2, height);
	this.machine = new GuiMachineSpecifier(width/2, height);

	this.add(this.jobs);
	this.add(this.machine);
    }
}
