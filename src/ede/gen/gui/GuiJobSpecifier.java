package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class GuiJobSpecifier extends JPanel {
    private static final String[] JOB_TYPES = {"Verilog Job", "Java Job", "Exe Job"};
    private JTextArea textArea;
    private JPanel contentPanel;
    private boolean collapsed;
    private TitledBorder border;
    private String jobTitle;
    private JComboBox<String> jobTypeDropdown;
    private JPanel verilogPanel;
    private JPanel exePanel;
    private JPanel textAreaPanel;
    private JTextField pathField;
    private JTextField exePathField;

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
        jobTypeDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateContentForJobType();
            }
        });

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

        contentPanel = new JPanel(new CardLayout());

        textAreaPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea(6, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textAreaPanel.add(scrollPane, BorderLayout.CENTER);

        verilogPanel = new JPanel(new BorderLayout());
        JPanel pathRow = new JPanel(new BorderLayout(4, 0));
        pathRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel pathLabel = new JLabel("Schematic Root Path: ");
        pathField = new JTextField();
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Verilog Root Schematic Path");
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int result = chooser.showOpenDialog(GuiJobSpecifier.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    pathField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        pathRow.add(pathLabel, BorderLayout.WEST);
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.add(browseBtn, BorderLayout.EAST);
        verilogPanel.add(pathRow, BorderLayout.NORTH);

        exePanel = new JPanel(new BorderLayout());
        JPanel exePathRow = new JPanel(new BorderLayout(4, 0));
        exePathRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel exePathLabel = new JLabel("Exe Path: ");
        exePathField = new JTextField();
        JButton exeBrowseBtn = new JButton("Browse...");
        exeBrowseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Exe Path");
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int result = chooser.showOpenDialog(GuiJobSpecifier.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    exePathField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        exePathRow.add(exePathLabel, BorderLayout.WEST);
        exePathRow.add(exePathField, BorderLayout.CENTER);
        exePathRow.add(exeBrowseBtn, BorderLayout.EAST);
        exePanel.add(exePathRow, BorderLayout.NORTH);

        contentPanel.add(verilogPanel, "Verilog Job");
        contentPanel.add(exePanel, "Exe Job");
        contentPanel.add(textAreaPanel, "TextArea");

        updateContentForJobType();

        add(headerBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
    }

    private void updateContentForJobType() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        String selected = (String) jobTypeDropdown.getSelectedItem();
        if ("Verilog Job".equals(selected)) {
            cl.show(contentPanel, "Verilog Job");
        } else if ("Exe Job".equals(selected)) {
            cl.show(contentPanel, "Exe Job");
        } else {
            cl.show(contentPanel, "TextArea");
        }
        revalidate();
        repaint();
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
