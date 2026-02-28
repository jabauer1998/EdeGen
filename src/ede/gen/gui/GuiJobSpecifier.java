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
import ede.stl.gui.GuiLineNumberGutter;
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
    private JTextField jobNameField;
    private JPanel verilogPanel;
    private JPanel exePanel;
    private JPanel textAreaPanel;
    private JTextField pathField;
    private JTextField exePathField;
    private JTextField verilogInputField;
    private DefaultListModel<String> jarListModel;
    private JList<String> jarList;
    private JCheckBox javaSyntaxCheckbox;
    private JTextField javaKeywordFileField;
    private JPanel javaKeywordPanel;
    private JCheckBox exeSyntaxCheckbox;
    private JTextField exeKeywordFileField;
    private JPanel exeKeywordPanel;
    private int currentHeight = 300;

    public GuiJobSpecifier(String title, Runnable onRemove) {
        this.jobTitle = title;
        this.collapsed = false;

        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);

        border = new TitledBorder(title + " [-]");
        border.setTitleColor(Color.DARK_GRAY);
        setBorder(border);

        JPanel headerBar = new JPanel(new BorderLayout(4, 0));
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

        JLabel jobNameLabel = new JLabel("Job Name: ");
        jobNameField = new JTextField();
        jobNameField.setToolTipText("Name passed to the job constructor");

        JPanel leftPanel = new JPanel(new BorderLayout(4, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(jobNameLabel, BorderLayout.WEST);
        leftPanel.add(jobNameField, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(jobTypeDropdown);
        rightPanel.add(collapseBtn);
        rightPanel.add(removeBtn);

        headerBar.add(leftPanel, BorderLayout.CENTER);
        headerBar.add(rightPanel, BorderLayout.EAST);

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
        importsScrollPane.setRowHeaderView(new GuiLineNumberGutter(importsPane));
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
        scrollPane.setRowHeaderView(new GuiLineNumberGutter(textPane));
        codePanel.add(scrollPane, BorderLayout.CENTER);

        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, importsPanel, codePanel);
        editorSplitPane.setResizeWeight(0.3);
        editorSplitPane.setDividerLocation(80);
        editorSplitPane.setContinuousLayout(true);

        JPanel jarPanel = new JPanel(new BorderLayout(4, 4));
        jarPanel.setBorder(BorderFactory.createTitledBorder("Classpath JARs"));
        jarListModel = new DefaultListModel<>();
        jarList = new JList<>(jarListModel);
        jarList.setVisibleRowCount(3);
        JScrollPane jarScrollPane = new JScrollPane(jarList);
        jarScrollPane.setPreferredSize(new Dimension(400, 60));
        jarScrollPane.setMinimumSize(new Dimension(100, 30));
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
        jarPanel.setMinimumSize(new Dimension(100, 60));

        javaKeywordPanel = new JPanel(new BorderLayout(4, 0));
        javaKeywordPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel javaKeywordLabel = new JLabel("Choose Keyword File: ");
        javaKeywordFileField = new JTextField();
        JButton javaKeywordBrowseBtn = new JButton("Browse...");
        javaKeywordBrowseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Keyword File");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = chooser.showOpenDialog(GuiJobSpecifier.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    javaKeywordFileField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        javaKeywordPanel.add(javaKeywordLabel, BorderLayout.WEST);
        javaKeywordPanel.add(javaKeywordFileField, BorderLayout.CENTER);
        javaKeywordPanel.add(javaKeywordBrowseBtn, BorderLayout.EAST);
        javaKeywordPanel.setVisible(false);

        javaSyntaxCheckbox = new JCheckBox("Enable Syntax Highlighting");
        javaSyntaxCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                javaKeywordPanel.setVisible(javaSyntaxCheckbox.isSelected());
                revalidate();
                repaint();
            }
        });

        JPanel javaSyntaxSection = new JPanel();
        javaSyntaxSection.setLayout(new BoxLayout(javaSyntaxSection, BoxLayout.Y_AXIS));
        javaSyntaxCheckbox.setAlignmentX(LEFT_ALIGNMENT);
        javaKeywordPanel.setAlignmentX(LEFT_ALIGNMENT);
        javaSyntaxSection.add(javaSyntaxCheckbox);
        javaSyntaxSection.add(javaKeywordPanel);

        JPanel javaBottomPanel = new JPanel(new BorderLayout());
        javaBottomPanel.add(jarPanel, BorderLayout.CENTER);
        javaBottomPanel.add(javaSyntaxSection, BorderLayout.SOUTH);

        JSplitPane codeJarSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorSplitPane, javaBottomPanel);
        codeJarSplitPane.setResizeWeight(0.75);
        codeJarSplitPane.setContinuousLayout(true);
        textAreaPanel.add(codeJarSplitPane, BorderLayout.CENTER);

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

        JPanel inputFileRow = new JPanel(new BorderLayout(4, 0));
        inputFileRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel inputFileLabel = new JLabel("Verilog Input File: ");
        verilogInputField = new JTextField();
        JButton inputBrowseBtn = new JButton("Browse...");
        inputBrowseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Verilog Input File");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = chooser.showOpenDialog(GuiJobSpecifier.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    verilogInputField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        inputFileRow.add(inputFileLabel, BorderLayout.WEST);
        inputFileRow.add(verilogInputField, BorderLayout.CENTER);
        inputFileRow.add(inputBrowseBtn, BorderLayout.EAST);

        JPanel verilogFieldsPanel = new JPanel();
        verilogFieldsPanel.setLayout(new BoxLayout(verilogFieldsPanel, BoxLayout.Y_AXIS));
        verilogFieldsPanel.add(pathRow);
        verilogFieldsPanel.add(inputFileRow);
        verilogPanel.add(verilogFieldsPanel, BorderLayout.NORTH);

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

        exeKeywordPanel = new JPanel(new BorderLayout(4, 0));
        exeKeywordPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel exeKeywordLabel = new JLabel("Choose Keyword File: ");
        exeKeywordFileField = new JTextField();
        JButton exeKeywordBrowseBtn = new JButton("Browse...");
        exeKeywordBrowseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Keyword File");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = chooser.showOpenDialog(GuiJobSpecifier.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    exeKeywordFileField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        exeKeywordPanel.add(exeKeywordLabel, BorderLayout.WEST);
        exeKeywordPanel.add(exeKeywordFileField, BorderLayout.CENTER);
        exeKeywordPanel.add(exeKeywordBrowseBtn, BorderLayout.EAST);
        exeKeywordPanel.setVisible(false);

        exeSyntaxCheckbox = new JCheckBox("Enable Syntax Highlighting");
        exeSyntaxCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exeKeywordPanel.setVisible(exeSyntaxCheckbox.isSelected());
                revalidate();
                repaint();
            }
        });

        JPanel exeFieldsPanel = new JPanel();
        exeFieldsPanel.setLayout(new BoxLayout(exeFieldsPanel, BoxLayout.Y_AXIS));
        exePathRow.setAlignmentX(LEFT_ALIGNMENT);
        exeSyntaxCheckbox.setAlignmentX(LEFT_ALIGNMENT);
        exeKeywordPanel.setAlignmentX(LEFT_ALIGNMENT);
        exeFieldsPanel.add(exePathRow);
        exeFieldsPanel.add(exeSyntaxCheckbox);
        exeFieldsPanel.add(exeKeywordPanel);
        exePanel.add(exeFieldsPanel, BorderLayout.NORTH);

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
    }

    @Override
    public Dimension getPreferredSize() {
        if (collapsed) {
            return new Dimension(super.getPreferredSize().width, 50);
        }
        return new Dimension(super.getPreferredSize().width, currentHeight);
    }

    @Override
    public Dimension getMaximumSize() {
        if (collapsed) {
            return new Dimension(Integer.MAX_VALUE, 50);
        }
        return new Dimension(Integer.MAX_VALUE, currentHeight);
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
        } else {
            border.setTitle(jobTitle + " [-]");
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

    public String getJobName() {
        return jobNameField.getText().trim();
    }

    public boolean isSyntaxHighlightingEnabled() {
        String jobType = getSelectedJobType();
        if ("Java Job".equals(jobType)) return javaSyntaxCheckbox.isSelected();
        if ("Exe Job".equals(jobType)) return exeSyntaxCheckbox.isSelected();
        return false;
    }

    public String getKeywordFilePath() {
        String jobType = getSelectedJobType();
        if ("Java Job".equals(jobType)) return javaKeywordFileField.getText().trim();
        if ("Exe Job".equals(jobType)) return exeKeywordFileField.getText().trim();
        return "";
    }

    public String[] loadKeywords() {
        if (!isSyntaxHighlightingEnabled()) return null;
        String path = getKeywordFilePath();
        if (path.isEmpty()) return null;
        try {
            java.util.List<String> keywords = new ArrayList<>();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    keywords.add(trimmed);
                }
            }
            reader.close();
            return keywords.toArray(new String[0]);
        } catch (Exception e) {
            return null;
        }
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

    public String getVerilogInputFile() {
        return verilogInputField.getText().trim();
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
