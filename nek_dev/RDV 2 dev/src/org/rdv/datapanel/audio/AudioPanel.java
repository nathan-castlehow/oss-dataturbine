package org.rdv.datapanel.audio;

import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.rdv.datapanel.AbstractDataPanel;

public class AudioPanel extends AbstractDataPanel{

  private Player player = new Player();
  private JSpinner sampleTime   = new JSpinner(new SpinnerNumberModel(Player.DEFAULT_SAMPLE_TIME,1,1000,1));
  private JSpinner sampleRate   = new JSpinner(new SpinnerNumberModel(Player.DEFAULT_SAMPLE_RATE,1,100000,1));
  private JSpinner sampleSize   = new JSpinner(new SpinnerNumberModel(Player.DEFAULT_SAMPLE_SIZE,8,32,8));
  private JSpinner numChannels  = new JSpinner(new SpinnerNumberModel(Player.DEFAULT_NUM_CHANNELS,1,7,1));
  private JCheckBox bigEndian   = new JCheckBox();
  private JCheckBox signed      = new JCheckBox();
  private JLabel format         = new JLabel();

  
  public AudioPanel(){
    JPanel info = new JPanel(new GridLayout(7, 2));
     
    signed.setSelected(Player.DEFAULT_SIGNED);
    bigEndian.setSelected(Player.DEFUALT_BIGENDIAN);
     
    info.add( new JLabel("Sample Time: ") );
    info.add(sampleTime);
    sampleTime.addChangeListener(playerListener());
    
    info.add( new JLabel("Sample Rate: ") );
    info.add(sampleRate);
    sampleRate.addChangeListener(playerListener());
    
    info.add( new JLabel("Sample Size: ") );
    info.add(sampleSize);
    sampleSize.addChangeListener(playerListener());
    
    info.add( new JLabel("Num Channels: ") );
    info.add(numChannels);
    numChannels.addChangeListener(playerListener());
    
    info.add( new JLabel("Signed: ") );
    info.add(signed);
    signed.addChangeListener(playerListener());
    
    info.add( new JLabel("Big Endian: ") );
    info.add(bigEndian);
    bigEndian.addChangeListener(playerListener());
    
    info.add( new JLabel("Format: ") );
    info.add(format);
    format.setText(player.toString());
    
    setDataComponent(info);
  }

  public ChangeListener playerListener(){
    return new ChangeListener() {
      
      public void stateChanged(ChangeEvent arg0) {
        player = new Player(
          ( (Double)sampleTime.getValue()).floatValue(),
          ( (Double)sampleRate.getValue()).floatValue(),
          ( (Integer)sampleSize .getValue()),
          ( (Integer)numChannels.getValue()),
          (Boolean)signed.isSelected(),
          (Boolean)bigEndian.isSelected()
        );
        
        System.out.println("Changed player to:\n\t"+ player);
        
        format.setText(player.toString());
      }
     
    };
    
  }
  public boolean supportsMultipleChannels() { return false; }

  public boolean addChannel(String channelName) {
    
    // TODO MODIFY
    return super.addChannel(channelName);
  }
  
  public void postTime(double time) {
    super.postTime(time);
    
    //loop over all channels and see if there is data for them
    Iterator i = channels.iterator();
    while (i.hasNext() && channelMap!=null) {
      String channelName = (String)i.next();
      
      int channelIndex = channelMap.GetIndex(channelName);
      //if there is data for channel, post it
      if (channelIndex != -1) {
          byte[] audioData = channelMap.GetDataAsByteArray(channelIndex)[0];
          double dTime =  channelMap.GetTimeStart(channelIndex);
          if(player.isQueued(dTime)) continue;
          player.play(audioData, dTime);
      }
    }
  }
  
}
