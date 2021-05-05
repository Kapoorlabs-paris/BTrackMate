package saveTracks;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoSavePanel {

	public static JPanel SavePanel = new JPanel();
	
	public static JButton Savebutton = new JButton("Save Track");
	public static JButton SaveAllbutton = new JButton("Save All Tracks");
	
	public static JLabel inputLabel = new JLabel("Filename:");
	public static TextField inputField;
	public static final JButton ChooseDirectory = new JButton("Choose Directory to save results in");
	public static TextField inputtrackField;
	
	public static JLabel inputtrackLabel = new JLabel("Enter trackID to save");
	
	public static JPanel SavePanel() {
		inputtrackField = new TextField(5);
		inputField = new TextField(5);
		layoutManager.Setlayout.LayoutSetter(SavePanel);
		Border origborder = new CompoundBorder(new TitledBorder("Enter filename for results files"),
				new EmptyBorder(layoutManager.Setlayout.c.insets));
		
		
		SavePanel.add(inputLabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SavePanel.add(inputField, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SavePanel.add(inputtrackLabel, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SavePanel.add(inputtrackField, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SavePanel.add(ChooseDirectory, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		SavePanel.add(Savebutton, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		
		SavePanel.add(SaveAllbutton, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		
		SavePanel.setBorder(origborder);
		

		
		
		return SavePanel;
		
	}
	
}
