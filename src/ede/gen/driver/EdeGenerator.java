package ede.gen.driver;

import javax.swing.*;
import ede.gen.gui.*;
import java.awt.Dimension;
import java.awt.Toolkit;

public class EdeGenerator{
    private static void createAndShowGUI() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        GuiGenFrame frame = new GuiGenFrame(screenWidth, screenHeight);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
