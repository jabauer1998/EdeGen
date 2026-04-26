package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import ede.gen.utils.EdeConfigManager;

public class GuiJobSpecifierList extends JPanel {
    private JPanel listPanel;
    private ArrayList<GuiJobSpecifier> jobSpecifiers;
    private int jobCounter;
    private JScrollPane scrollPane;
    private GuiGenPanel genPanel;

    private int dragSourceIdx = -1;
    private int dropTargetIdx = -1;
    private GuiJobSpecifier draggingSpec = null;

    public GuiJobSpecifierList(double width, double height, GuiGenPanel genPanel) {
        this.genPanel = genPanel;
        jobSpecifiers = new ArrayList<GuiJobSpecifier>();
        jobCounter = 0;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension((int) width, (int) height));

        TitledBorder outerBorder = new TitledBorder("Ede Job Specifications");
        outerBorder.setTitleColor(Color.BLACK);
        setBorder(outerBorder);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        JButton addBtn = new JButton("Add Job");
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addJobSpecifier();
            }
        });

        JButton collapseAllBtn = new JButton("Collapse All");
        collapseAllBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (GuiJobSpecifier spec : jobSpecifiers) {
                    if (!spec.isCollapsed()) {
                        spec.toggleCollapsed();
                    }
                }
            }
        });

        JButton expandAllBtn = new JButton("Expand All");
        expandAllBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (GuiJobSpecifier spec : jobSpecifiers) {
                    if (spec.isCollapsed()) {
                        spec.toggleCollapsed();
                    }
                }
            }
        });

        toolBar.add(addBtn);
        toolBar.add(collapseAllBtn);
        toolBar.add(expandAllBtn);

        listPanel = new JPanel() {
            @Override
            protected void paintChildren(Graphics g) {
                super.paintChildren(g);
                if (dropTargetIdx >= 0 && draggingSpec != null) {
                    int lineY;
                    if (dropTargetIdx < jobSpecifiers.size()) {
                        lineY = jobSpecifiers.get(dropTargetIdx).getBounds().y - 2;
                    } else if (!jobSpecifiers.isEmpty()) {
                        Rectangle b = jobSpecifiers.get(jobSpecifiers.size() - 1).getBounds();
                        lineY = b.y + b.height + 2;
                    } else {
                        lineY = 0;
                    }
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(0, 102, 204));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(4, lineY, getWidth() - 4, lineY);
                    int arc = 4;
                    g2.fillOval(0, lineY - arc / 2, arc, arc);
                    g2.fillOval(getWidth() - arc, lineY - arc / 2, arc, arc);
                }
            }
        };
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.add(listPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(listWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addJobSpecifier();
    }

    private void installDragDrop(GuiJobSpecifier spec) {
        JLabel handle = spec.getDragHandle();

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragSourceIdx = jobSpecifiers.indexOf(spec);
                draggingSpec = spec;
                dropTargetIdx = dragSourceIdx;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingSpec == null) return;
                Point pt = SwingUtilities.convertPoint(handle, e.getPoint(), listPanel);
                dropTargetIdx = computeDropIndex(pt.y);
                listPanel.repaint();

                Rectangle visible = scrollPane.getViewport().getViewRect();
                Point ptInScroll = SwingUtilities.convertPoint(handle, e.getPoint(), scrollPane.getViewport());
                int edge = 30;
                JScrollBar vsb = scrollPane.getVerticalScrollBar();
                if (ptInScroll.y < edge) {
                    vsb.setValue(Math.max(0, vsb.getValue() - 12));
                } else if (ptInScroll.y > visible.height - edge) {
                    vsb.setValue(Math.min(vsb.getMaximum(), vsb.getValue() + 12));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggingSpec == null) {
                    reset();
                    return;
                }
                int src = dragSourceIdx;
                int dst = dropTargetIdx;
                reset();
                if (src < 0 || dst < 0 || src == dst || src + 1 == dst) return;
                GuiJobSpecifier moving = jobSpecifiers.remove(src);
                int insertAt = (dst > src) ? dst - 1 : dst;
                jobSpecifiers.add(insertAt, moving);
                rebuildListPanel();
                renumberJobs();
            }

            private void reset() {
                dragSourceIdx = -1;
                dropTargetIdx = -1;
                draggingSpec = null;
                listPanel.repaint();
            }
        };

        handle.addMouseListener(adapter);
        handle.addMouseMotionListener(adapter);
    }

    private int computeDropIndex(int y) {
        for (int i = 0; i < jobSpecifiers.size(); i++) {
            Rectangle b = jobSpecifiers.get(i).getBounds();
            if (y < b.y + b.height / 2) {
                return i;
            }
        }
        return jobSpecifiers.size();
    }

    private void rebuildListPanel() {
        listPanel.removeAll();
        for (GuiJobSpecifier spec : jobSpecifiers) {
            listPanel.add(spec);
            listPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    public void addJobSpecifier() {
        jobCounter++;
        final String title = "Job " + jobCounter;
        final GuiJobSpecifier[] holder = new GuiJobSpecifier[1];
        holder[0] = new GuiJobSpecifier(title, new Runnable() {
            public void run() {
                removeJobSpecifier(holder[0]);
            }
        }, genPanel);
        jobSpecifiers.add(holder[0]);
        installDragDrop(holder[0]);
        listPanel.add(holder[0]);
        listPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        listPanel.revalidate();
        listPanel.repaint();
    }

    public void removeJobSpecifier(GuiJobSpecifier spec) {
        if ("Java Job".equals(spec.getSelectedJobType()) && genPanel != null) {
            genPanel.removeJavaTab(spec);
        }
        jobSpecifiers.remove(spec);
        listPanel.remove(spec);
        renumberJobs();
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void renumberJobs() {
        for (int i = 0; i < jobSpecifiers.size(); i++) {
            jobSpecifiers.get(i).setJobTitle("Job " + (i + 1));
        }
        jobCounter = jobSpecifiers.size();
    }

    public ArrayList<GuiJobSpecifier> getJobSpecifiers() {
        return jobSpecifiers;
    }

    public void loadFromConfig(List<EdeConfigManager.JobConfig> jobConfigs) {
        for (GuiJobSpecifier spec : new ArrayList<>(jobSpecifiers)) {
            removeJobSpecifier(spec);
        }
        jobCounter = 0;
        for (EdeConfigManager.JobConfig cfg : jobConfigs) {
            jobCounter++;
            final String title = cfg.jobTitle.isEmpty() ? "Job " + jobCounter : cfg.jobTitle;
            final GuiJobSpecifier[] holder = new GuiJobSpecifier[1];
            holder[0] = new GuiJobSpecifier(title, new Runnable() {
                public void run() {
                    removeJobSpecifier(holder[0]);
                }
            }, genPanel);
            holder[0].setJobType(cfg.jobType);
            holder[0].setJobName(cfg.jobName);
            holder[0].setSyntaxHighlighting(cfg.syntaxHighlighting);
            holder[0].setImportsText(cfg.imports);
            holder[0].setText(cfg.code);
            holder[0].setKeywordFilePath(cfg.keywordFile);
            holder[0].setJarPaths(cfg.jarPaths);
            holder[0].setVerilogPath(cfg.verilogPath);
            holder[0].setVerilogInputFile(cfg.verilogInputFile);
            holder[0].setVerilogMainModule(cfg.verilogMainModule);
            holder[0].setExePath(cfg.exePath);
            jobSpecifiers.add(holder[0]);
            installDragDrop(holder[0]);
            listPanel.add(holder[0]);
            listPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
