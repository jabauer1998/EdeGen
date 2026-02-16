package ede.gen.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.LinkedList;
import java.awt.event.*;


    public class GuiCollapsableFrame<ElemType> extends JPanel {
        private TitledBorder border;
        private Dimension collapsedSize;
        private boolean collapsible;
	private boolean collapsed;
        final String collapsedKey;
        private JPanel placeholderPanel = new JPanel();
        private Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR),
                uncollapseCursor = new Cursor(Cursor.N_RESIZE_CURSOR),
                collapseCursor = new Cursor(Cursor.S_RESIZE_CURSOR);
	private LinkedList<ElemType> elems;

        public GuiCollapsableFrame(String title) {
	    elems = new LinkedList<ElemType>();
            setName(title);
            collapsedKey = "GroupPanel." + getName() + "." + "collapsed";
            border = new TitledBorder(getName());
            border.setTitleColor(Color.black);
            setToolTipText(String.format("Group %s (click title to collapse or expand)", title));
	    JPanel thisPanel = this;

            setAlignmentX(LEFT_ALIGNMENT);
            setAlignmentY(TOP_ALIGNMENT);
            // because TitledBorder has no access to the Label we fake the size data ;)
            final JLabel l = new JLabel(title);
            Dimension d = l.getPreferredSize(); // size of title text of TitledBorder
            collapsedSize = new Dimension(getMaximumSize().width, d.height + 2); // l.getPreferredSize(); // size of title text of TitledBorder

	    collapsible = false;
	    collapsed = true;
            setTitle(title);

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    if (ifMouseInHotArea(e)) {
                        if (collapsed) {
                            setCursor(uncollapseCursor);
                        } else {
                            setCursor(collapseCursor);
                        }
                    } else {
                        setCursor(normalCursor);
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!collapsible) {
                        return;
                    }

                    if (getBorder() != null && getBorder().getBorderInsets(thisPanel) != null) {
                        Insets i = getBorder().getBorderInsets(thisPanel);
		    }
		}
	    });

	    setBorder(border);
	}

	public boolean ifMouseInHotArea(MouseEvent e){
	    Insets i = getBorder().getBorderInsets(this);
	    if(e.getX() >= i.left && e.getX() <= i.right)
		if(e.getY() <= i.bottom && e.getY() >= i.top)
		    return true;
	    return false;
	}

        public void setCollapsible(boolean collapsible) {
            this.collapsible = collapsible;
        }

        public boolean isCollapsible() {
            return this.collapsible;
        }

        public void setTitle(String title) {
            border.setTitle(title);
        }

        /**
         * @return the collapsed
         */
        public boolean isCollapsed() {
            return collapsed;
        }
    }
