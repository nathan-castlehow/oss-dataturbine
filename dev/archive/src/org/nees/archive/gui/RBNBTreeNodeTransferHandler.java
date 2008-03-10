/*
 * RBNBTreeNodeTransferHandler.java
 * 
 * Created on March 2006 (from a copy of ArchiveSegmentTransferHandler.java
 * from the sun tutorial site http://java.sun.com/docs/books/tutorial/uiswing/misc/dnd.html
 * used by the 1.4 DragListDemo.java example.)
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision: 153 $
 *   $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps-dev/archive/src/org/nees/archive/gui/RBNBTreeNodeTransferHandler.java $
 *   $LastChangedBy: ljmiller $
 * 
 */

package org.nees.archive.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * This transfer class supports MinimalArchiveViewer, a Graphical User Interface to a 
 * file-based archive. It represents the transfer of an RBNB tree node (representing
 * a source channel) to the CaptureTreadDialog. That is, it represents the transfer
 * of an RBNB channel for capture to the archive.
 * 
 * @author Terry E Weymouth
 *
 */
public class RBNBTreeNodeTransferHandler extends TransferHandler {
    static final String localFlavorType = DataFlavor.javaJVMLocalObjectMimeType +
    "; class=org.nees.archive.gui.RBNBChannelNodeCover";
    DataFlavor RBNBTreeNodeFlavor;
    JTree source = null;
    RBNBChannelNodeCover transferData;

    public RBNBTreeNodeTransferHandler() {
        try {
            RBNBTreeNodeFlavor = new DataFlavor(localFlavorType);
        } catch (ClassNotFoundException e) {
            System.out.println(
             "ArchiveSegmentTransferHandler: unable to create data flavor");
        }
    }

    public boolean importData(JComponent c, Transferable t) {
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        if (!(c instanceof JList))
            return false;
        JList target = (JList) c;
        try {
            if (hasLocalFlavor(t.getTransferDataFlavors())) {
                transferData = (RBNBChannelNodeCover) t
                        .getTransferData(RBNBTreeNodeFlavor);
            } else {
                return false;
            }
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("importData: unsupported data flavor");
            return false;
        } catch (IOException ioe) {
            System.out.println("importData: I/O exception");
            return false;
        }

        // drop it and select it
        try
        {
            CaptureThreadListModel listModel = (CaptureThreadListModel) target.getModel();
            int index = 0;
            if (!listModel.contains(transferData))
                listModel.add(transferData);
            index = listModel.indexOf(transferData);
            target.setSelectedIndex(index);
        }
        catch (Throwable problem)
        {
            System.out.println("importDate: problem = " + problem.toString());
            return false;
        }
        return true;
    }

    private boolean hasLocalFlavor(DataFlavor[] flavors) {
        if (RBNBTreeNodeFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(RBNBTreeNodeFlavor)) {
                return true;
            }
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasLocalFlavor(flavors))  { return true; }
        return false;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof JTree) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
               ((JTree)c).getLastSelectedPathComponent();
            if (node == null) return null;
            Object nodeInfo = node.getUserObject();
            if (!(nodeInfo instanceof RBNBChannelNodeCover))
                return null;
            RBNBChannelNodeCover nc = (RBNBChannelNodeCover)nodeInfo;
            return new RBNBTreeNodeTransferable(nc);
        }
        return null;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public class RBNBTreeNodeTransferable implements Transferable {
        RBNBChannelNodeCover data;

        public RBNBTreeNodeTransferable(RBNBChannelNodeCover d) {
            data = d;
        }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { RBNBTreeNodeFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.getMimeType().contains("RBNBChannelNodeCover"))
                return true;
            return false;
        }
        
    }
}
