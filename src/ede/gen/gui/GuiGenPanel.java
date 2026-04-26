package ede.gen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
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

        this.log = new GuiEdeLog();
        this.jobs = new GuiJobSpecifierList(width / 2, 5 * height / 6, this);
        this.machine = new GuiMachineSpecifier(width / 2, 5 * height / 6, this.jobs, this.log, this);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.jobs, this.machine);
        horizontalSplit.setResizeWeight(0.5);
        horizontalSplit.setDividerLocation((int) (width / 2));
        horizontalSplit.setContinuousLayout(true);

        JPanel logPanel = new JPanel(new BorderLayout());
        JLabel logTitle = new JLabel("Ede Generator Tool Log", SwingConstants.CENTER);
        JScrollPane logScroll = new JScrollPane(this.log);
        logScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, logPanel);
        verticalSplit.setResizeWeight(0.85);
        verticalSplit.setDividerLocation((int) (5 * height / 6));
        verticalSplit.setContinuousLayout(true);

        tabbedPane.addTab("Home", verticalSplit);

        this.add(buildMenuToolbar(), BorderLayout.NORTH);
        this.add(tabbedPane, BorderLayout.CENTER);
    }

    private JMenuBar buildMenuToolbar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JMenu testMenu = new JMenu("Test");
        testMenu.add(linkItem("Test Ede Environment", e -> machine.launchEdeEnvironment()));
        menuBar.add(testMenu);

        JMenu saveMenu = new JMenu("Save");
        saveMenu.add(linkItem("Save Ede As", e -> machine.saveEdeEnvironment(false)));
        saveMenu.add(linkItem("Save And Run Ede", e -> machine.saveEdeEnvironment(true)));
        menuBar.add(saveMenu);

        JMenu cleanMenu = new JMenu("Clean");
        cleanMenu.add(linkItem("Clear Ede Log", e -> machine.clearLog()));
        menuBar.add(cleanMenu);

        return menuBar;
    }

    private JMenuItem linkItem(String text, ActionListener action) {
        JMenuItem item = new JMenuItem("<html><u>" + text + "</u></html>");
        item.setForeground(new Color(0, 102, 204));
        item.setBorderPainted(false);
        item.addActionListener(action);
        return item;
    }

    public void addLog(String text){
        this.log.log(text);
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
