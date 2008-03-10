package org.nees.archive.gui;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractListModel;

import com.rbnb.sapi.ChannelTree.Node;

public class CaptureThreadListModel extends AbstractListModel {

    Vector<RBNBChannelNodeCover> base;
    
    CaptureThreadListModel(Vector<RBNBChannelNodeCover> data)
    {
        base = data;
    }

    public int getSize() {
        return base.size();
    }

    public Object getElementAt(int index) {
        return base.elementAt(index);
    }
    
    public void add(RBNBChannelNodeCover node)
    {
        addElementAt(base.size(),node);
    }

    public void addElement(RBNBChannelNodeCover node)
    {
        add(node);
    }
    
    public void addElementAt(int index, RBNBChannelNodeCover node)
    {
        if (contains(node)) return; // no duplicates; no problem
        base.add(index,node);
        fireIntervalAdded(this, index, index);
    }
    
    public Iterator iterator()
    {
        return base.iterator();
    }

    public boolean contains(Object o)
    {
        if (base.contains(o)) return true;
        // also duplicated by name
        if (!(o instanceof RBNBChannelNodeCover)) return false;
        Node n = ((RBNBChannelNodeCover)o).node;
        if (n == null) return false;
        String test = n.getFullName();
        Iterator i = iterator();
        while (i.hasNext())
        {
            RBNBChannelNodeCover listNodeCover = (RBNBChannelNodeCover)i.next();
            Node listNode = listNodeCover.node;
            if ((listNode != null) && (test.endsWith(listNode.getFullName()))) return true;
        }
        return false;
    }
    
    public int indexOf(Object o)
    {
        int i = base.indexOf(o);
        if (i > -1) return i;
        if (!contains(o)) return -1;
        // which means that the node is here by name
        Node n = ((RBNBChannelNodeCover)o).node;
        if (n == null) return -1;
        String test = n.getFullName();
        for (int ndx = 0; ndx < base.size(); ndx++)
        {
            RBNBChannelNodeCover listNodeCover = (RBNBChannelNodeCover)getElementAt(ndx);
            Node listNode = listNodeCover.node;
            if ((listNode != null) && (test.endsWith(listNode.getFullName()))) return ndx;
        }
        return -1;
    }
}
