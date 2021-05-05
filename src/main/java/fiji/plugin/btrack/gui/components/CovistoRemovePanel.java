package fiji.plugin.btrack.gui.components;

import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.TextField;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoRemovePanel {
	
	
	
	
	public static JPanel RemovePanel = new JPanel();
	public static Label removeText = new Label("Distance Threshold = ", Label.CENTER);
	
	public static TextField inputFieldSpot;
	public static double distthreshold = 10;
	
	public static JPanel RemoveselectPanel() {
		
		layoutManager.Setlayout.LayoutSetter(RemovePanel);
		
		inputFieldSpot = new TextField(5);
		inputFieldSpot.setText(Double.toString(distthreshold));
		
		
		Border removeborder = new CompoundBorder(new TitledBorder("Spurios Spot removal"), new EmptyBorder(layoutManager.Setlayout.c.insets));
		
		
		RemovePanel.add(removeText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		
		RemovePanel.add(inputFieldSpot, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		
		
		RemovePanel.setBorder(removeborder);
		
		return RemovePanel;
		
	}
	
	

}
