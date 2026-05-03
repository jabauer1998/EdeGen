package ede.gen.gui;

import javax.swing.*;
import java.awt.Dimension;

public class GuiEdeGenSpinnerField extends JPanel {
    private JSpinner spinner;

    public GuiEdeGenSpinnerField(String title, double length, double height, int initial, int min, int max, int step) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setPreferredSize(new Dimension((int)length, (int)height));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)height));
        this.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(title);
        label.setPreferredSize(new Dimension((int)(length/2), (int)height));

        SpinnerNumberModel model = new SpinnerNumberModel(initial, min, max, step);
        spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension((int)(length/2), (int)height));
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)height));

        this.add(label);
        this.add(spinner);
    }

    public String getInputText() {
        return String.valueOf(((Number) spinner.getValue()).intValue());
    }

    public void setInputText(String text) {
        try {
            int v = Integer.parseInt(text.trim());
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            Comparable<?> minC = model.getMinimum();
            Comparable<?> maxC = model.getMaximum();
            if (minC instanceof Number && v < ((Number) minC).intValue()) v = ((Number) minC).intValue();
            if (maxC instanceof Number && v > ((Number) maxC).intValue()) v = ((Number) maxC).intValue();
            spinner.setValue(v);
        } catch (NumberFormatException ignored) {
        }
    }

    public int getValueInt() {
        return ((Number) spinner.getValue()).intValue();
    }
}
