package fiji.plugin.globalbtrack.gui.components;

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

public class CovistoTimeselectPanel {

	public static final int scrollbarSize = 1000;

	public static JPanel Timeselect = new JPanel();
	public static Label timeText = new Label("Current T = " + 1, Label.CENTER);

	public static int fourthDimensionslider = 1;
	public static int fourthDimensionsliderInit = 1;
	public static int fourthDimensionSize;
	public static final String timestring = "Current T";
	public static JScrollBar timeslider = new JScrollBar(Scrollbar.HORIZONTAL, fourthDimensionsliderInit, 10, 0,
			scrollbarSize + 10);
	public static int fourthDimension;
	public static TextField inputFieldT;

	public static void setTime(final int value) {

		fourthDimensionslider = value;
		fourthDimensionsliderInit = value;
		fourthDimension = value;
	}

	public int getTimeMax() {

		return fourthDimensionSize;
	}

	public static JPanel TimeselectPanel(int ndims) {

		layoutManager.Setlayout.LayoutSetter(Timeselect);
		setTime(fourthDimension);

		inputFieldT = new TextField();
		inputFieldT = new TextField(5);
		inputFieldT.setText(Integer.toString(fourthDimension));

		// Put time slider
		Border timeborder = new CompoundBorder(new TitledBorder("Select time"),
				new EmptyBorder(layoutManager.Setlayout.c.insets));

		if (ndims < 4) {

			timeslider.setEnabled(false);
			inputFieldT.setEnabled(false);
		}
		Timeselect.add(timeText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));

		Timeselect.add(timeslider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));

		Timeselect.add(inputFieldT, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, layoutManager.Setlayout.insets, 0, 0));

		Timeselect.setBorder(timeborder);

		return Timeselect;
	}

}
