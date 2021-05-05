package fiji.plugin.btrack.gui.components;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class LoadQuadroImage {

	final String bordertitle;
	final String[] blankimageNames;
	
	public LoadQuadroImage(final String bordertitle, final String[] blankimageNames) {
		
		this.bordertitle = bordertitle;
		this.blankimageNames = blankimageNames;
	}
	
	public  JPanel panelFourChannel = new JPanel();
	
	public  JComboBox<String> ChooseImage;
	public  JComboBox<String> ChoosesecImage;
	public  JComboBox<String> ChoosethirdImage;
	public  JComboBox<String> ChoosefourthImage;
	public  JPanel FourChannelOption() {
		
		
		
		layoutManager.Setlayout.LayoutSetter(panelFourChannel);
		ChooseImage = new JComboBox<String>(blankimageNames);
		ChoosesecImage = new JComboBox<String>(blankimageNames);
		ChoosethirdImage = new JComboBox<String>(blankimageNames);
		ChoosefourthImage = new JComboBox<String>(blankimageNames);
		
		
		 Border chooseoriginalfile = new CompoundBorder(new TitledBorder(bordertitle),
				new EmptyBorder(layoutManager.Setlayout.c.insets));
		 
			
		
		 panelFourChannel.add(ChooseImage, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		 panelFourChannel.add(ChoosesecImage,  new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		 panelFourChannel.add(ChoosethirdImage,  new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		 panelFourChannel.add(ChoosefourthImage,  new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		 
		 panelFourChannel.setBorder(chooseoriginalfile);
		 
		 
		 
		 return panelFourChannel;
		
	}
	
}
