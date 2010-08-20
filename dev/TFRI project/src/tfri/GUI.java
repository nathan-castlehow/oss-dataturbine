package tfri;
/*  Blob Detection image analysis client program with a DataTurbine interface. 
    Copyright (C) 2009, Michael Nekrasov Robert J. Chen

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import imageTools.AnnotatedImage;
import imageTools.blobDetection.BlobRule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class GUI{
	
	private JLabel displayedImg = null;
	private JFrame frame = null;
	private JPanel controls;
	private JTextField blobCount;
	private JSpinner minHeightSpin;
	private JSpinner minWidthSpin;
	private JSpinner maxWidthSpin;
	private JSpinner maxHeightSpin;
	private JButton apply;
	private JButton next;
	private JSlider brightnessSlider;
	
	private AllEventHandler universalHandler;
	
	public GUI(ControlListener control) throws IOException {
		universalHandler = new AllEventHandler(control);

		frame = new JFrame("Blob Detection (C)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		controls = new JPanel();
		controls.setLayout(new GridLayout(18, 1));

		blobCount = new JTextField("Counting...");
		blobCount.setEditable(false);
		
		BlobRule rule = TFRI_Factory.BEE_RULE;
		minWidthSpin = new JSpinner(new SpinnerNumberModel(rule.getMinWidth(),0,500,1));
		minHeightSpin = new JSpinner(new SpinnerNumberModel(rule.getMinHeight(),0,500,1));
		maxWidthSpin = new JSpinner(new SpinnerNumberModel(rule.getMaxWidth(),0,500,1));
		maxHeightSpin = new JSpinner(new SpinnerNumberModel(rule.getMaxHeight(),0,500,1));
		
		apply = new JButton("Apply");
		apply.addActionListener(universalHandler);
		
		next = new JButton("Next");
		next.addActionListener(universalHandler);
		next.setEnabled(control.hasNext());

		controls.add(new JLabel("Blob Count:"));
		controls.add(blobCount);
		controls.add(new JLabel("Minimum Width"));
		controls.add(minWidthSpin);
		controls.add(new JLabel("Minimum Height"));
		controls.add(minHeightSpin);
		controls.add(new JLabel("Maximum Width"));
		controls.add(maxWidthSpin);
		controls.add(new JLabel("Maximum Height"));
		controls.add(maxHeightSpin);
		controls.add(apply);
		controls.add(next);
		
		//displayedImg = new JLabel(new ImageIcon(ImageIO.read(new File("loading.gif"))));
		displayedImg = new JLabel();
		
		// Create the slider.
		brightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		
		// Turn on labels at major tick marks.
		brightnessSlider.setMajorTickSpacing(10);
		brightnessSlider.setMinorTickSpacing(5);
		brightnessSlider.setPaintTicks(true);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		brightnessSlider.setFont( new Font("Serif", Font.ITALIC, 15) );
		brightnessSlider.setValue((int)(rule.getBrightness()*100));
		
		brightnessSlider.addChangeListener(universalHandler);
		
		frame.getContentPane().add(displayedImg, BorderLayout.CENTER);
		frame.getContentPane().add(brightnessSlider, BorderLayout.SOUTH);
		frame.getContentPane().add(controls, BorderLayout.EAST);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	// Event Handler class
	public class AllEventHandler implements ChangeListener, ActionListener {

		private ControlListener control;
		
		public AllEventHandler(ControlListener calibrator) {
			this.control = calibrator;
		}

		public void actionPerformed(ActionEvent e) {
			blobCount.setText("Counting");
			
			if(e.getSource() == next){
				control.nextImage();
				next.setEnabled(control.hasNext());
			}
			else{
				control.update();
			}
		}

		/** Listen to the slider. */
		public void stateChanged(ChangeEvent e) {
			Object source = e.getSource(); 
			if (source == brightnessSlider ) {
				if(!((JSlider)source).getValueIsAdjusting())
					control.update();
			}
			else 
				control.update();
		}
	}
	
	public int getMinHeight(){ 
		return ((Integer) minHeightSpin.getValue()).intValue();
	}
	public int getMinWidth(){ 
		return ((Integer) minWidthSpin.getValue()).intValue();
	}
	public int getMaxHeight(){ 
		return ((Integer) maxHeightSpin.getValue()).intValue();
	}
	public int getMaxWidth(){ 
		return ((Integer) maxWidthSpin.getValue()).intValue();
	}
	public float getBrightness(){
		return brightnessSlider.getValue() / 100f;
	}


	public void update(AnnotatedImage image) {		
		displayedImg.setIcon(new ImageIcon(image.graphicallyAnnotate(TFRI_Factory.BEE_RULE, Color.RED)));
		blobCount.setText(image.getAnnotation(TFRI_Factory.BEE_RULE).count()+"");
	}
	
}
