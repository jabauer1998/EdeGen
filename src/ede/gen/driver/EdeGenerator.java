package ede.gen.driver;

import javax.swing.*;

public class EdeGenerator{
    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window (JFrame).
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set the default close operation.

        // Add the "Hello World" label (JLabel).
        JLabel label = new JLabel("Hello World", SwingConstants.CENTER);
        frame.getContentPane().add(label);

        // Display the window.
        frame.pack(); // Size the frame.
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
