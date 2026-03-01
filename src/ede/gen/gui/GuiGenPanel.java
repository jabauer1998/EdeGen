package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuiGenPanel extends JPanel {
    private GuiJobSpecifierList jobs;
    private GuiMachineSpecifier machine;
    private GuiEdeLog log;
    private JTabbedPane tabbedPane;
    private Map<GuiJobSpecifier, JPanel> javaTabPanels;

    public GuiGenPanel(double width, double height) {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension((int) width, (int) height));

        tabbedPane = new JTabbedPane();
        javaTabPanels = new LinkedHashMap<>();

        this.log = new GuiEdeLog(width, height / 6);
        this.jobs = new GuiJobSpecifierList(width / 2, 5 * height / 6, this);
        this.machine = new GuiMachineSpecifier(width / 2, 5 * height / 6, this.jobs, this.log);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.jobs, this.machine);
        horizontalSplit.setResizeWeight(0.5);
        horizontalSplit.setDividerLocation((int) (width / 2));
        horizontalSplit.setContinuousLayout(true);

        JPanel logPanel = new JPanel(new BorderLayout());
        JLabel logTitle = new JLabel("Ede Generator Tool Log", SwingConstants.CENTER);
        JScrollPane logScroll = new JScrollPane(this.log);
        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, logPanel);
        verticalSplit.setResizeWeight(0.85);
        verticalSplit.setDividerLocation((int) (5 * height / 6));
        verticalSplit.setContinuousLayout(true);

        tabbedPane.addTab("Home", verticalSplit);

        this.add(tabbedPane, BorderLayout.CENTER);
    }

    public void addJavaTab(GuiJobSpecifier spec) {
        if (javaTabPanels.containsKey(spec)) return;
        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(spec.getJavaEditorPanel(), BorderLayout.CENTER);
        tabbedPane.addTab(spec.getTabTitle(), tabPanel);
        javaTabPanels.put(spec, tabPanel);
        tabbedPane.setSelectedComponent(tabPanel);
    }

    public void removeJavaTab(GuiJobSpecifier spec) {
        JPanel tabPanel = javaTabPanels.remove(spec);
        if (tabPanel != null) {
            tabbedPane.remove(tabPanel);
        }
    }

    public void updateJavaTabTitle(GuiJobSpecifier spec) {
        JPanel tabPanel = javaTabPanels.get(spec);
        if (tabPanel != null) {
            int idx = tabbedPane.indexOfComponent(tabPanel);
            if (idx >= 0) {
                tabbedPane.setTitleAt(idx, spec.getTabTitle());
            }
        }
    }
}
