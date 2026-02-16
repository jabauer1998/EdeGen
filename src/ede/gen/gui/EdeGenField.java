package ede.gen.gui;

import javax.swing.*;
import java.awt.Dimension;

public class EdeGenField extends JPanel{
    private JTextField field;
    
    public EdeGenField(String title, double length, double height){
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setPreferredSize(new Dimension((int)length, (int)height));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)height));
        this.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(title);
        label.setPreferredSize(new Dimension((int)(length/2), (int)height));
        field = new JTextField();
        field.setPreferredSize(new Dimension((int)(length/2), (int)height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)height));

        this.add(label);
        this.add(field);
    }
    
    public String getInputText(){
        return field.getText();
    }
}
