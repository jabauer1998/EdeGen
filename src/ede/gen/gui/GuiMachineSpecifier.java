package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GuiMachineSpecifier extends JPanel{
    private EdeGenField title;
    
    public GuiMachineSpecifier(double width, double height){
	//First Set this Panel to be a vertical Panel
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	this.title = new EdeGenField("Title of Ede Environment: ", width, 10);
	JPanel toolBar = new JPanel();
	toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));

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
    }
}
