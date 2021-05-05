package layoutManager;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class Setlayout {

	
	public final static Insets insets = new Insets(10, 10, 0, 10);
	public final static GridBagLayout layout = new GridBagLayout();
	public final static GridBagConstraints c = new GridBagConstraints();
	
	public static void LayoutSetter(JPanel panel) {
		
		panel.setLayout(layout);
		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 5;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;
	}
	
	
	
	
}
