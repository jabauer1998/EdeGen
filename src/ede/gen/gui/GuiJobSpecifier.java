package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ede.gen.utils.JavaSyntaxHighlighter;
import javax.swing.border.Border;

public class GuiJobSpecifier extends JPanel {
    private static final String[] JOB_TYPES = {"Verilog Job", "Java Job", "Exe Job"};
    private JTextPane importsPane;
    private JTextPane textPane;
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
    private DefaultListModel<String> jarListModel;
    private JList<String> jarList;
    private int currentHeight = 300;

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

        JPanel importsPanel = new JPanel(new BorderLayout());
        importsPanel.setBorder(BorderFactory.createTitledBorder("Imports"));
        importsPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        importsPane.setPreferredSize(new Dimension(400, 50));
        importsPane.setMinimumSize(new Dimension(100, 30));
        JavaSyntaxHighlighter importsHighlighter = new JavaSyntaxHighlighter(importsPane);
        importsPane.getDocument().addDocumentListener(importsHighlighter);
        JScrollPane importsScrollPane = new JScrollPane(importsPane);
        importsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        importsScrollPane.setMinimumSize(new Dimension(100, 30));
        importsPanel.add(importsScrollPane, BorderLayout.CENTER);

        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setBorder(BorderFactory.createTitledBorder("Code"));
        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        textPane.setPreferredSize(new Dimension(400, 100));
        textPane.setMinimumSize(new Dimension(100, 30));
        JavaSyntaxHighlighter highlighter = new JavaSyntaxHighlighter(textPane);
        textPane.getDocument().addDocumentListener(highlighter);
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(100, 30));
        codePanel.add(scrollPane, BorderLayout.CENTER);

        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, importsPanel, codePanel);
        editorSplitPane.setResizeWeight(0.3);
        editorSplitPane.setDividerLocation(80);
        editorSplitPane.setContinuousLayout(true);
        textAreaPanel.add(editorSplitPane, BorderLayout.CENTER);

        JPanel jarPanel = new JPanel(new BorderLayout(4, 4));
        jarPanel.setBorder(BorderFactory.createTitledBorder("Classpath JARs"));
        jarListModel = new DefaultListModel<>();
        jarList = new JList<>(jarListModel);
        jarList.setVisibleRowCount(3);
        JScrollPane jarScrollPane = new JScrollPane(jarList);
        jarScrollPane.setPreferredSize(new Dimension(400, 60));
        JPanel jarButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton addJarBtn = new JButton("Add JAR...");
        addJarBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select JAR file(s)");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(true);
                javax.swing.filechooser.FileNameExtensionFilter filter =
                    new javax.swing.filechooser.FileNameExtensionFilter("JAR files", "jar");
                chooser.setFileFilter(filter);
                int result = chooser.showOpenDialog(GuiJobSpecifier.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    for (File f : chooser.getSelectedFiles()) {
                        String path = f.getAbsolutePath();
                        if (!jarListModel.contains(path)) {
                            jarListModel.addElement(path);
                        }
                    }
                }
            }
        });
        JButton removeJarBtn = new JButton("Remove");
        removeJarBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = jarList.getSelectedIndex();
                if (idx >= 0) {
                    jarListModel.remove(idx);
                }
            }
        });
        jarButtonPanel.add(addJarBtn);
        jarButtonPanel.add(removeJarBtn);
        jarPanel.add(jarScrollPane, BorderLayout.CENTER);
        jarPanel.add(jarButtonPanel, BorderLayout.SOUTH);
        textAreaPanel.add(jarPanel, BorderLayout.SOUTH);

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

        JPanel resizeHandle = new JPanel();
        resizeHandle.setPreferredSize(new Dimension(0, 6));
        resizeHandle.setBackground(new Color(180, 180, 180));
        resizeHandle.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        resizeHandle.setToolTipText("Drag to resize");
        final GuiJobSpecifier self = this;
        resizeHandle.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                resizeHandle.setBackground(new Color(130, 130, 130));
            }
            public void mouseExited(MouseEvent e) {
                resizeHandle.setBackground(new Color(180, 180, 180));
            }
        });
        resizeHandle.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (collapsed) return;
                Point p = SwingUtilities.convertPoint(resizeHandle, e.getPoint(), self.getParent());
                int newHeight = p.y - self.getY();
                if (newHeight < 80) newHeight = 80;
                currentHeight = newHeight;
                setPreferredSize(new Dimension(getWidth(), currentHeight));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
                revalidate();
                if (getParent() != null) {
                    getParent().revalidate();
                    getParent().repaint();
                }
            }
        });

        add(headerBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(resizeHandle, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(Integer.MAX_VALUE, currentHeight));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
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
        Component south = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (south != null) south.setVisible(!collapsed);
        if (collapsed) {
            border.setTitle(jobTitle + " [+]");
            setPreferredSize(new Dimension(getWidth(), 50));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        } else {
            border.setTitle(jobTitle + " [-]");
            setPreferredSize(new Dimension(getWidth(), currentHeight));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
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

    public void setJobTitle(String title) {
        this.jobTitle = title;
        if (collapsed) {
            border.setTitle(title + " [+]");
        } else {
            border.setTitle(title + " [-]");
        }
        repaint();
    }

    public String getText() {
        return textPane.getText();
    }

    public void setText(String text) {
        textPane.setText(text);
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public String getSelectedJobType() {
        return (String) jobTypeDropdown.getSelectedItem();
    }

    public String getVerilogPath() {
        return pathField.getText();
    }

    public String getExePath() {
        return exePathField.getText();
    }

    public String getImportsText() {
        return importsPane.getText();
    }

    public List<String> getJarPaths() {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < jarListModel.size(); i++) {
            paths.add(jarListModel.getElementAt(i));
        }
        return paths;
    }
}
