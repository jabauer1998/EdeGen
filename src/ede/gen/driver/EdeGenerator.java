package ede.gen.driver;

import javax.swing.*;
import ede.gen.gui.*;
import java.awt.Dimension;
import java.awt.Toolkit;

public class EdeGenerator{
    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window (JFrame).
	// Get the default toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        // Extract the width and height
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

	
	JFrame frame = new JFrame("Emulator Development Environment Generator Tool");
	GuiGenPanel panel = new GuiGenPanel(screenWidth, screenHeight);
	frame.setPreferredSize(screenSize);
	
        
	frame.add(panel);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule GUI creation on the event-dispatching thread.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
