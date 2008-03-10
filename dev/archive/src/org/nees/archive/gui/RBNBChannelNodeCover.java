/**
 * 
 */
package org.nees.archive.gui;

import org.nees.archive.inter.ArchiveItemInterface.TYPE;

import com.rbnb.sapi.ChannelTree;

class RBNBChannelNodeCover
{
    ChannelTree.Node node;
    TYPE dataType = TYPE.unknown;
	
	RBNBChannelNodeCover(ChannelTree.Node node)
	{
        this.node = node;
        setDataType(node);
	}
	
	public String toString()
	{
		return node.getName();
	}
    
    private void setDataType(ChannelTree.Node node)
    {
        String mimeType = node.getMime();

        TYPE type = TYPE.unknown;
        
        if (mimeType == null) return;
        
        if (mimeType.contains("jpeg"))
        {
            type = TYPE.imageSequence;
        }
        else if (mimeType.contains("octet-stream"))
        {
            type = TYPE.multiChannelNumeric;
        }
        else if (mimeType.contains("audio"))
        {
            type = TYPE.audioStream;
        }
        else
        {
            type = TYPE.unknown;
        }
        dataType = type;
    }

    public TYPE getDataType() {
        return dataType;
    }

}