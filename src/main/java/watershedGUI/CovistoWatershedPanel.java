package watershedGUI;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.Scrollbar;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoWatershedPanel {

	
	
	public static JPanel WaterPanel = new JPanel();
	public static final int scrollbarSize = 1000;
	public static final String waterstring = "Threshold for Watershedding";
	public static int thresholdsliderWaterInit = 125;
	public static float thresholdMinWater = 1f;
	public static float thresholdMaxWater = 255f;
	public static int thresholdInitWater = 0;
	public static float thresholdWater = 255f;
	
	public static Label watertext = new Label(waterstring + " = " + thresholdInitWater, Label.CENTER);
	public static JScrollBar thresholdWaterslider = new JScrollBar(Scrollbar.HORIZONTAL, thresholdsliderWaterInit, 10, 0,
			10 + scrollbarSize);
	public double getInitialThresholdWater() {
		return thresholdInitWater;
	}

	public void setInitialThresholdWater(final float value) {
		thresholdWater = value;
		thresholdInitWater = scrollbar.Utility.computeScrollbarPositionFromValue(thresholdWater, thresholdMinWater, thresholdMaxWater,
				scrollbarSize);
	}

	public static boolean disttransform = true;
	
	public static final Checkbox displayWater = new Checkbox("Display Watershed image", true);

	public static final Checkbox displayBinary = new Checkbox("Display Binary image");

	public static final Checkbox displayDist = new Checkbox("Display DTimage");

	public static final Checkbox autothreshold = new Checkbox("Auto Thresholding");
	
	public static final Checkbox dodist = new Checkbox("Distance transformed Watershed", disttransform);
	
	public static final JButton Water3D = new JButton("Watershed in 3D/4D");
	
	public static final JButton tryWater = new JButton("Watershed Current");
	
	
	
	public static JPanel WaterPanel() {
		
		
		thresholdWaterslider.setValue(scrollbar.Utility.computeScrollbarPositionFromValue(thresholdsliderWaterInit,
				thresholdMinWater, thresholdMaxWater, scrollbarSize));

		thresholdWater = scrollbar.Utility.computeValueFromScrollbarPosition(thresholdWaterslider.getValue(),
				thresholdMinWater, thresholdMaxWater, scrollbarSize);
		layoutManager.Setlayout.LayoutSetter(WaterPanel);
		Border waterborder = new CompoundBorder(new TitledBorder("Watershed detection"), new EmptyBorder(layoutManager.Setlayout.c.insets));

		watertext = new Label(waterstring + " = " + thresholdWater, Label.CENTER);
		WaterPanel.add(watertext, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.add(dodist, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.add(thresholdWaterslider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));

		WaterPanel.add(displayWater, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.add(displayBinary, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.add(displayDist, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.add(autothreshold, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.add(Water3D, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		
		WaterPanel.add(tryWater, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));
		WaterPanel.setBorder(waterborder);
		
		
		return WaterPanel;
		
	}
	
}
