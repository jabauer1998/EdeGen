package ede.gen.gui;

import javax.swing.*;

public class GuiEdeLog extends JTextArea{
    public GuiEdeLog(){
        this.setEditable(false);
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
    }

    public void log(String message) {
        this.append(message + "\n");
        this.setCaretPosition(this.getDocument().getLength());
    }
}
