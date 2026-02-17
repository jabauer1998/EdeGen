package ede.gen.gui;

import javax.swing.*;
import java.awt.*;

public class GuiEdeLog extends JTextArea{
    public GuiEdeLog(double width, double height){
        this.setEditable(false);
        this.setPreferredSize(new Dimension((int)width, (int)height));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)height));
        this.setMinimumSize(new Dimension((int)width, (int)height));
    }

    public void log(String message) {
        this.append(message + "\n");
        this.setCaretPosition(this.getDocument().getLength());
    }
}