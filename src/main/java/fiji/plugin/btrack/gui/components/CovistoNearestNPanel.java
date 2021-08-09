package fiji.plugin.btrack.gui.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Scrollbar;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoNearestNPanel {

	public static final int scrollbarSize = 1000;
	public static JPanel NearestNPanel = new JPanel();
	public static final String maxSearchstringNearest = "Maximum search radius";
	public static int maxSearchInit = 100;
	public static float maxSearchradiusNearest = 15;
	public static Label maxSearchTextNearest = new Label(maxSearchstringNearest + " = " + maxSearchInit, Label.CENTER);
	public static final JScrollBar maxSearchNearest = new JScrollBar(Scrollbar.HORIZONTAL, maxSearchInit, 10, 0,
			10 + scrollbarSize);

	public static float maxSearchradiusMinNearest = 1;
	public static float maxSearchradiusMaxNearest = 1000;
	public static final JButton AllthreeD = new JButton("Track in Z");

	public static JPanel NearestNPanel() {

		layoutManager.Setlayout.LayoutSetter(NearestNPanel);
		Border NNborder = new CompoundBorder(new TitledBorder("NearestNeighbour Search in Z"),
				new EmptyBorder(layoutManager.Setlayout.c.insets));
		maxSearchTextNearest = new Label(maxSearchstringNearest + " = " + maxSearchradiusNearest, Label.CENTER);

		NearestNPanel.add(maxSearchTextNearest, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		NearestNPanel.add(maxSearchNearest, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		NearestNPanel.add(AllthreeD, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		NearestNPanel.setBorder(NNborder);

		return NearestNPanel;

	}

}
