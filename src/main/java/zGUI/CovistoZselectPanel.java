package zGUI;

import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.TextField;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoZselectPanel {
	
	public static final int scrollbarSize = 1000;
	public static JPanel Zselect = new JPanel();
	public static final String zstring = "Current Z";
	public static final String zgenstring = "Current Z / T";

	
	public static Label zText = new Label("Current Z = " + 1, Label.CENTER);
	public static Label zgenText = new Label("Current Z / T = " + 1, Label.CENTER);
	public static int thirdDimension;
	public static int thirdDimensionSize;
	
	public static int thirdDimensionslider = 1;
	public static int thirdDimensionsliderInit = 1;
	public static TextField inputFieldZ;
	
	public static JScrollBar zslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0,
			10 + scrollbarSize);
	
	
	public static void setZ(final int value) {
		thirdDimensionslider = value;
		thirdDimensionsliderInit = value;
		thirdDimension = value;
	}
	
	
	public static JPanel ZselectPanel(int ndims) {
		
		layoutManager.Setlayout.LayoutSetter(Zselect);
		setZ(thirdDimension);
		inputFieldZ = new TextField();
		inputFieldZ = new TextField(5);
		inputFieldZ.setText(Integer.toString(thirdDimension));
		
		Border zborder = new CompoundBorder(new TitledBorder("Select Z / T"), new EmptyBorder( layoutManager.Setlayout.insets));
		if (ndims > 3)
			Zselect.add(zText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		else
			Zselect.add(zgenText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		Zselect.add(zslider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));

		Zselect.add(inputFieldZ, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));

		Zselect.setBorder(zborder);
		
		return Zselect;
	}
	

}
