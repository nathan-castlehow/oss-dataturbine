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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;


public class InitializerGUI{
	
	private JFrame frame = new JFrame("Initializer");
	private JPanel controls = new JPanel();
	
	private JButton apply;
	
	private JComboBox sourceType;
	private JComboBox destinationType;
	
	private JPanel subControls = new JPanel();
	
	private final JPanel archiveControls;
	private final JTextField archiveUrl;
	private final JSpinner archiveStartDateTime, archiveEndDateTime; 
	
	private final JPanel fileControls;
	private JButton fileBrowse = new JButton("Browse");
	private final JFileChooser fileChooser = new JFileChooser();

	private JPanel rbnbControls= new JPanel();
	
	
	public InitializerGUI() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		
		
		sourceType = new JComboBox(new String[]{ "TFRI Web Archive", "RBNB Source", "Single Image"});
		controls.add(new JLabel("Source Type: "));
		controls.add(sourceType);
		
		archiveControls = new JPanel();
		archiveUrl = new JTextField("http://");
		
		archiveStartDateTime = new JSpinner(new SpinnerDateModel());
		archiveEndDateTime = new JSpinner(new SpinnerDateModel());
		archiveControls.add(archiveUrl);
		archiveControls.add(archiveStartDateTime);
		archiveControls.add(archiveEndDateTime);
		
		fileControls = new JPanel();
		fileChooser.setMultiSelectionEnabled(true);
		fileBrowse.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(fileChooser);
				for(File file: fileChooser.getSelectedFiles())
					System.err.println(file.getName());
				
			}
		});
		fileControls.add(fileBrowse);
		
		
		subControls.add(fileControls);
		
		controls.add(subControls);
		
		
		destinationType = new JComboBox(new String[]{ "Console", "RBNB"});
		controls.add(new JLabel("Destination Type: "));
		controls.add(destinationType);
		
		apply = new JButton("Apply");
		controls.add(apply);
		frame.getContentPane().add(controls);
		
//		
//		controls = new JPanel();
//
//		blobCount = new JTextField("Counting...");
//		blobCount.setEditable(false);
//		
//		BlobRule rule = TFRI_Factory.BEE_RULE;
//		minHeightSpin = new JSpinner(new SpinnerNumberModel(rule.getMinHeight(),0,500,1));
//		
//		apply = new JButton("Apply");
//		
//		next = new JButton("Next");
//
//		controls.add(new JLabel("Blob Count:"));
//		controls.add(blobCount);
//
//		controls.add(apply);
//		
//		// Create the slider.
//		brightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
//		
//		// Turn on labels at major tick marks.
//		brightnessSlider.setMajorTickSpacing(10);
//		brightnessSlider.setMinorTickSpacing(5);
//		brightnessSlider.setPaintTicks(true);
//		brightnessSlider.setPaintLabels(true);
//		brightnessSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
//		brightnessSlider.setFont( new Font("Serif", Font.ITALIC, 15) );
//		brightnessSlider.setValue((int)(rule.getBrightness()*100));
		
		

//		frame.getContentPane().add(brightnessSlider, BorderLayout.SOUTH);
//		frame.getContentPane().add(controls, BorderLayout.EAST);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	
	public static void main(String[] args) {
		new InitializerGUI();
	}
}
