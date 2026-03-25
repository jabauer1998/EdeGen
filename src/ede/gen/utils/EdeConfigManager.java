package ede.gen.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EdeConfigManager {

    public static class IoSectionConfig {
        public String tabName = "";
        public String sectionTitle = "";
        public boolean readOnly = false;

        public IoSectionConfig() {}
        public IoSectionConfig(String tabName, String sectionTitle, boolean readOnly) {
            this.tabName = tabName;
            this.sectionTitle = sectionTitle;
            this.readOnly = readOnly;
        }
    }

    public static class JobConfig {
        public String jobTitle = "";
        public String jobName = "";
        public String jobType = "Verilog Job";
        public boolean syntaxHighlighting = false;
        public String imports = "";
        public String code = "";
        public String keywordFile = "";
        public List<String> jarPaths = new ArrayList<>();
        public String verilogPath = "";
        public String verilogInputFile = "";
        public String verilogMainModule = "";
        public String exePath = "";
    }

    public static class MachineConfig {
        public String title = "";
        public String ramBytesPerRow = "16";
        public String registerFormat = "Binary";
        public String ramAddressFormat = "Binary";
        public String ramFormat = "Binary";
        public List<IoSectionConfig> ioSections = new ArrayList<>();
    }

    public static class EdeConfig {
        public MachineConfig machine = new MachineConfig();
        public List<JobConfig> jobs = new ArrayList<>();
    }

    public static void save(File file, EdeConfig config) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("EdeConfig");
        doc.appendChild(root);

        Element machineEl = doc.createElement("Machine");
        root.appendChild(machineEl);
        appendText(doc, machineEl, "Title", config.machine.title);
        appendText(doc, machineEl, "RamBytesPerRow", config.machine.ramBytesPerRow);
        appendText(doc, machineEl, "RegisterFormat", config.machine.registerFormat);
        appendText(doc, machineEl, "RamAddressFormat", config.machine.ramAddressFormat);
        appendText(doc, machineEl, "RamFormat", config.machine.ramFormat);

        Element ioSectionsEl = doc.createElement("IoSections");
        machineEl.appendChild(ioSectionsEl);
        for (IoSectionConfig io : config.machine.ioSections) {
            Element ioEl = doc.createElement("IoSection");
            appendText(doc, ioEl, "TabName", io.tabName);
            appendText(doc, ioEl, "SectionTitle", io.sectionTitle);
            appendText(doc, ioEl, "ReadOnly", String.valueOf(io.readOnly));
            ioSectionsEl.appendChild(ioEl);
        }

        Element jobsEl = doc.createElement("Jobs");
        root.appendChild(jobsEl);
        for (JobConfig job : config.jobs) {
            Element jobEl = doc.createElement("Job");
            appendText(doc, jobEl, "JobTitle", job.jobTitle);
            appendText(doc, jobEl, "JobName", job.jobName);
            appendText(doc, jobEl, "JobType", job.jobType);
            appendText(doc, jobEl, "SyntaxHighlighting", String.valueOf(job.syntaxHighlighting));
            appendText(doc, jobEl, "Imports", job.imports);
            appendText(doc, jobEl, "Code", job.code);
            appendText(doc, jobEl, "KeywordFile", job.keywordFile);
            Element jarPathsEl = doc.createElement("JarPaths");
            for (String jar : job.jarPaths) {
                appendText(doc, jarPathsEl, "JarPath", jar);
            }
            jobEl.appendChild(jarPathsEl);
            appendText(doc, jobEl, "VerilogPath", job.verilogPath);
            appendText(doc, jobEl, "VerilogInputFile", job.verilogInputFile);
            appendText(doc, jobEl, "VerilogMainModule", job.verilogMainModule);
            appendText(doc, jobEl, "ExePath", job.exePath);
            jobsEl.appendChild(jobEl);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(doc), new StreamResult(file));
    }

    public static EdeConfig load(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        EdeConfig config = new EdeConfig();

        Element root = doc.getDocumentElement();

        Element machineEl = firstChild(root, "Machine");
        if (machineEl != null) {
            config.machine.title = text(machineEl, "Title", "");
            config.machine.ramBytesPerRow = text(machineEl, "RamBytesPerRow", "16");
            config.machine.registerFormat = text(machineEl, "RegisterFormat", "Binary");
            config.machine.ramAddressFormat = text(machineEl, "RamAddressFormat", "Binary");
            config.machine.ramFormat = text(machineEl, "RamFormat", "Binary");

            Element ioSectionsEl = firstChild(machineEl, "IoSections");
            if (ioSectionsEl != null) {
                NodeList ioList = ioSectionsEl.getElementsByTagName("IoSection");
                for (int i = 0; i < ioList.getLength(); i++) {
                    Element ioEl = (Element) ioList.item(i);
                    IoSectionConfig io = new IoSectionConfig();
                    io.tabName = text(ioEl, "TabName", "");
                    io.sectionTitle = text(ioEl, "SectionTitle", "");
                    io.readOnly = Boolean.parseBoolean(text(ioEl, "ReadOnly", "false"));
                    config.machine.ioSections.add(io);
                }
            }
        }

        Element jobsEl = firstChild(root, "Jobs");
        if (jobsEl != null) {
            NodeList jobList = jobsEl.getElementsByTagName("Job");
            for (int i = 0; i < jobList.getLength(); i++) {
                Element jobEl = (Element) jobList.item(i);
                JobConfig job = new JobConfig();
                job.jobTitle = text(jobEl, "JobTitle", "Job " + (i + 1));
                job.jobName = text(jobEl, "JobName", "");
                job.jobType = text(jobEl, "JobType", "Verilog Job");
                job.syntaxHighlighting = Boolean.parseBoolean(text(jobEl, "SyntaxHighlighting", "false"));
                job.imports = text(jobEl, "Imports", "");
                job.code = text(jobEl, "Code", "");
                job.keywordFile = text(jobEl, "KeywordFile", "");
                job.verilogPath = text(jobEl, "VerilogPath", "");
                job.verilogInputFile = text(jobEl, "VerilogInputFile", "");
                job.verilogMainModule = text(jobEl, "VerilogMainModule", "");
                job.exePath = text(jobEl, "ExePath", "");
                Element jarPathsEl = firstChild(jobEl, "JarPaths");
                if (jarPathsEl != null) {
                    NodeList jarList = jarPathsEl.getElementsByTagName("JarPath");
                    for (int j = 0; j < jarList.getLength(); j++) {
                        String path = jarList.item(j).getTextContent();
                        if (path != null && !path.trim().isEmpty()) {
                            job.jarPaths.add(path.trim());
                        }
                    }
                }
                config.jobs.add(job);
            }
        }

        return config;
    }

    private static void appendText(Document doc, Element parent, String tag, String value) {
        Element el = doc.createElement(tag);
        el.setTextContent(value == null ? "" : value);
        parent.appendChild(el);
    }

    private static Element firstChild(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getParentNode() == parent) return (Element) n;
        }
        return null;
    }

    private static String text(Element parent, String tag, String defaultValue) {
        Element el = firstChild(parent, tag);
        if (el == null) return defaultValue;
        String v = el.getTextContent();
        return v == null ? defaultValue : v;
    }
}
