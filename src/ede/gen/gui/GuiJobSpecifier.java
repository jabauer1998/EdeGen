package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class GuiJobSpecifier extends JPanel {
    private static final String[] JOB_TYPES = {"VerilogJob", "JavaJob", "ExeJob"};
    private JTextArea textArea;
    private JPanel contentPanel;
    private boolean collapsed;
    private TitledBorder border;
    private String jobTitle;
    private JComboBox<String> jobTypeDropdown;

    public GuiJobSpecifier(String title, Runnable onRemove) {
        this.jobTitle = title;
        this.collapsed = false;

        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);

        border = new TitledBorder(title + " [-]");
        border.setTitleColor(Color.DARK_GRAY);
        setBorder(border);

        JPanel headerBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerBar.setOpaque(false);

        jobTypeDropdown = new JComboBox<String>(JOB_TYPES);
        jobTypeDropdown.setToolTipText("Select job type");

        JButton collapseBtn = new JButton("-");
        collapseBtn.setMargin(new Insets(0, 4, 0, 4));
        collapseBtn.setToolTipText("Collapse/Expand");
        collapseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleCollapsed();
                collapseBtn.setText(collapsed ? "+" : "-");
            }
        });

        JButton removeBtn = new JButton("X");
        removeBtn.setMargin(new Insets(0, 4, 0, 4));
        removeBtn.setForeground(Color.RED);
        removeBtn.setToolTipText("Remove this job");
        removeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (onRemove != null) {
                    onRemove.run();
                }
            }
        });

        headerBar.add(jobTypeDropdown);
        headerBar.add(collapseBtn);
        headerBar.add(removeBtn);

        contentPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea(6, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(headerBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
    }

    public void toggleCollapsed() {
        collapsed = !collapsed;
        contentPanel.setVisible(!collapsed);
        if (collapsed) {
            border.setTitle(jobTitle + " [+]");
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        } else {
            border.setTitle(jobTitle + " [-]");
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        }
        revalidate();
        repaint();
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
