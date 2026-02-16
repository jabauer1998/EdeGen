package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.LinkedList;
import java.awt.event.*;

public class GuiCollapsableFrame<ElemType extends Component> extends JPanel {
    private TitledBorder border;
    private boolean collapsed;
    private JPanel contentPanel;
    private LinkedList<ElemType> elems;
    private Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

    private String storedTitle;

    public GuiCollapsableFrame(String title) {
        elems = new LinkedList<ElemType>();
        collapsed = false;
        storedTitle = title;

        setLayout(new BorderLayout());
        setName(title);
        border = new TitledBorder(title);
        border.setTitleColor(Color.BLACK);
        setBorder(border);

        JPanel headerBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        headerBar.setOpaque(false);
        headerBar.setCursor(handCursor);
        headerBar.setToolTipText("Click to collapse or expand");

        headerBar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                toggleCollapsed();
            }
        });

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        add(headerBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void toggleCollapsed() {
        collapsed = !collapsed;
        contentPanel.setVisible(!collapsed);
        if (collapsed) {
            border.setTitle(storedTitle + " [+]");
        } else {
            border.setTitle(storedTitle + " [-]");
        }
        revalidate();
        repaint();
    }

    public void setTitle(String title) {
        storedTitle = title;
        setName(title);
        if (collapsed) {
            border.setTitle(title + " [+]");
        } else {
            border.setTitle(title + " [-]");
        }
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void addElement(ElemType elem) {
        elems.add(elem);
        contentPanel.add(elem);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        revalidate();
        repaint();
    }

    public void removeElement(ElemType elem) {
        elems.remove(elem);
        contentPanel.remove(elem);
        revalidate();
        repaint();
    }

    public LinkedList<ElemType> getElements() {
        return elems;
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
