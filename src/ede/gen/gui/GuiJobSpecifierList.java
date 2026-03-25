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

        listPanel = new JPanel();
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
            listPanel.add(holder[0]);
            listPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
