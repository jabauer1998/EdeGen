package ede.gen.gui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;

public class JavaSyntaxHighlighter implements DocumentListener {
    private final JTextPane textPane;
    private final StyledDocument doc;
    private final Style defaultStyle;
    private final Style keywordStyle;
    private final Style stringStyle;
    private final Style commentStyle;
    private final Style numberStyle;
    private final Style annotationStyle;
    private final Style typeStyle;
    private boolean updating = false;

    private static final Set<String> KEYWORDS = new HashSet<>();
    static {
        String[] kw = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double",
            "else", "enum", "extends", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            "true", "false", "null"
        };
        for (String k : kw) {
            KEYWORDS.add(k);
        }
    }

    private static final Set<String> TYPES = new HashSet<>();
    static {
        String[] t = {
            "String", "Integer", "Long", "Double", "Float", "Boolean",
            "Character", "Byte", "Short", "Object", "Class", "System",
            "Math", "Thread", "Runnable", "Exception", "Error",
            "ArrayList", "HashMap", "List", "Map", "Set", "Collection"
        };
        for (String tp : t) {
            TYPES.add(tp);
        }
    }

    public JavaSyntaxHighlighter(JTextPane pane) {
        this.textPane = pane;
        this.doc = pane.getStyledDocument();

        defaultStyle = doc.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, Color.WHITE);
        StyleConstants.setFontFamily(defaultStyle, "Monospaced");
        StyleConstants.setFontSize(defaultStyle, 13);

        keywordStyle = doc.addStyle("keyword", null);
        StyleConstants.setForeground(keywordStyle, new Color(204, 120, 50));
        StyleConstants.setBold(keywordStyle, true);
        StyleConstants.setFontFamily(keywordStyle, "Monospaced");
        StyleConstants.setFontSize(keywordStyle, 13);

        stringStyle = doc.addStyle("string", null);
        StyleConstants.setForeground(stringStyle, new Color(106, 135, 89));
        StyleConstants.setFontFamily(stringStyle, "Monospaced");
        StyleConstants.setFontSize(stringStyle, 13);

        commentStyle = doc.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128));
        StyleConstants.setItalic(commentStyle, true);
        StyleConstants.setFontFamily(commentStyle, "Monospaced");
        StyleConstants.setFontSize(commentStyle, 13);

        numberStyle = doc.addStyle("number", null);
        StyleConstants.setForeground(numberStyle, new Color(104, 151, 187));
        StyleConstants.setFontFamily(numberStyle, "Monospaced");
        StyleConstants.setFontSize(numberStyle, 13);

        annotationStyle = doc.addStyle("annotation", null);
        StyleConstants.setForeground(annotationStyle, new Color(187, 181, 41));
        StyleConstants.setFontFamily(annotationStyle, "Monospaced");
        StyleConstants.setFontSize(annotationStyle, 13);

        typeStyle = doc.addStyle("type", null);
        StyleConstants.setForeground(typeStyle, new Color(78, 154, 190));
        StyleConstants.setFontFamily(typeStyle, "Monospaced");
        StyleConstants.setFontSize(typeStyle, 13);

        pane.setBackground(new Color(43, 43, 43));
        pane.setCaretColor(Color.WHITE);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        highlightLater();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        highlightLater();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void highlightLater() {
        if (!updating) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    highlightAll();
                }
            });
        }
    }

    private void highlightAll() {
        updating = true;
        try {
            String text = doc.getText(0, doc.getLength());
            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);

            highlightKeywordsAndTypes(text);
            highlightNumbers(text);
            highlightAnnotations(text);
            highlightStrings(text);
            highlightComments(text);
        } catch (BadLocationException e) {
        } finally {
            updating = false;
        }
    }

    private void highlightComments(String text) {
        Pattern singleLine = Pattern.compile("//[^\n]*");
        Matcher m = singleLine.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), commentStyle, true);
        }

        Pattern multiLine = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        m = multiLine.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), commentStyle, true);
        }
    }

    private void highlightStrings(String text) {
        Pattern stringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
        Matcher m = stringPattern.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), stringStyle, true);
        }

        Pattern charPattern = Pattern.compile("'([^'\\\\]|\\\\.)*'");
        m = charPattern.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), stringStyle, true);
        }
    }

    private void highlightAnnotations(String text) {
        Pattern annotationPattern = Pattern.compile("@\\w+");
        Matcher m = annotationPattern.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), annotationStyle, true);
        }
    }

    private void highlightKeywordsAndTypes(String text) {
        Pattern wordPattern = Pattern.compile("\\b[A-Za-z_]\\w*\\b");
        Matcher m = wordPattern.matcher(text);
        while (m.find()) {
            String word = m.group();
            if (KEYWORDS.contains(word)) {
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), keywordStyle, true);
            } else if (TYPES.contains(word)) {
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), typeStyle, true);
            }
        }
    }

    private void highlightNumbers(String text) {
        Pattern numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b");
        Matcher m = numberPattern.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), numberStyle, true);
        }
    }
}
