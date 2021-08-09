package fiji.plugin.btrack.gui.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.TextField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoKalmanPanel {

	public static final int scrollbarSize = 1000;
	public static JPanel KalmanPanel = new JPanel();

	public static final String maxSearchstringKalman = "Maximum search radius";
	public static final String initialSearchstring = "Initial search radius (um)";

	public static final String alphastring = "Distance/Angle cost ratio";
	public static final String betastring = "Weightage for pixel ratio based cost";
	public static int alphaInit = 1;
	public static int betaInit = 0;
	public static float initialSearchradius = 100;
	public static int initialSearchradiusInit = (int) initialSearchradius;
	public static float initialSearchradiusMin = 1;
	public static float initialSearchradiusMax = 1000;
	public static float alphaMin = 0;
	public static float alphaMax = 1;
	public static float betaMin = 0;
	public static float betaMax = 1;
	public static float alpha = 0.5f;
	public static float beta = 0.5f;
	public static float maxSearchradius = 100;
	public static int maxSearchradiusInit = (int) maxSearchradius;
	public static float maxSearchradiusMin = 1;
	public static float maxSearchradiusMax = 1000;

	public static int maxframegap = 3;
	public static int trackduration = 10;
	public static Label lostlabel;
	public static Label mintracklength;

	public static TextField lostframe;
	public static TextField tracklength;
	public static Label alphaText = new Label(alphastring + " = " + alphaInit, Label.CENTER);
	public static Label betaText = new Label(betastring + " = " + betaInit, Label.CENTER);

	public static Label maxSearchTextKalman = new Label(maxSearchstringKalman + " = " + maxSearchradiusInit,
			Label.CENTER);
	public static Label iniSearchText = new Label(initialSearchstring + " = " + initialSearchradiusInit, Label.CENTER);

	public static final JScrollBar maxSearchSS = new JScrollBar(Scrollbar.HORIZONTAL, maxSearchradiusInit, 10, 0,
			10 + scrollbarSize);
	public static final JScrollBar initialSearchS = new JScrollBar(Scrollbar.HORIZONTAL, initialSearchradiusInit, 10, 0,
			10 + scrollbarSize);
	public static final JScrollBar alphaS = new JScrollBar(Scrollbar.HORIZONTAL, alphaInit, 10, 0, 10 + scrollbarSize);
	public static final JScrollBar betaS = new JScrollBar(Scrollbar.HORIZONTAL, betaInit, 10, 0, 10 + scrollbarSize);
	public static final JButton Skeletontime = new JButton("Skeletonize Buddies");
	public static final JButton Timetrack = new JButton("Track Buddies");
	public static final JButton Restart = new JButton("Restart");
	public static final JScrollBar maxSearchKalman = new JScrollBar(Scrollbar.HORIZONTAL, maxSearchradiusInit, 10, 0,
			10 + scrollbarSize);

	public static void setInitialAlpha(final float value) {
		alpha = value;
		alphaInit = scrollbar.Utility.computeScrollbarPositionFromValue(alpha, alphaMin, alphaMax, scrollbarSize);
	}

	public static double getInitialAlpha(final float value) {

		return alpha;

	}

	public static void setInitialBeta(final float value) {
		beta = value;
		betaInit = scrollbar.Utility.computeScrollbarPositionFromValue(beta, betaMin, betaMax, scrollbarSize);
	}

	public static double getInitialBeta(final float value) {

		return beta;

	}

	public static void setInitialsearchradius(final float value) {
		initialSearchradius = value;
		initialSearchradiusInit = scrollbar.Utility.computeScrollbarPositionFromValue(initialSearchradius,
				initialSearchradiusMin, initialSearchradiusMax, scrollbarSize);
	}

	public static void setInitialmaxsearchradius(final float value) {
		maxSearchradius = value;
		maxSearchradiusInit = scrollbar.Utility.computeScrollbarPositionFromValue(maxSearchradius, maxSearchradiusMin,
				maxSearchradiusMax, scrollbarSize);
	}

	public double getInitialsearchradius(final float value) {

		return initialSearchradius;

	}

	public static JPanel KalmanPanel() {

		setInitialAlpha(alphaInit);
		setInitialBeta(betaInit);
		setInitialsearchradius(initialSearchradiusInit);
		setInitialmaxsearchradius(maxSearchradius);
		layoutManager.Setlayout.LayoutSetter(KalmanPanel);
		lostframe = new TextField(1);
		lostframe.setText(Integer.toString(maxframegap));

		tracklength = new TextField(1);
		tracklength.setText(Integer.toString(trackduration));

		lostlabel = new Label("Allow link loosing for #frames");
		mintracklength = new Label("Minimum BudTrack length as percent of timeframes");

		alphaText = new Label(alphastring + " = " + alpha, Label.CENTER);
		betaText = new Label(betastring + " = " + beta, Label.CENTER);

		iniSearchText = new Label(initialSearchstring + " = " + initialSearchradius, Label.CENTER);

		maxSearchradius = scrollbar.Utility.computeValueFromScrollbarPosition(maxSearchKalman.getValue(),
				maxSearchradiusMin, maxSearchradiusMax, scrollbarSize);
		initialSearchradius = scrollbar.Utility.computeValueFromScrollbarPosition(initialSearchS.getValue(),
				initialSearchradiusMin, initialSearchradiusMax, scrollbarSize);
		alpha = scrollbar.Utility.computeValueFromScrollbarPosition(alphaS.getValue(), alphaMin, alphaMax,
				scrollbarSize);
		beta = scrollbar.Utility.computeValueFromScrollbarPosition(betaS.getValue(), betaMin, betaMax, scrollbarSize);

		alphaS.setValue(
				scrollbar.Utility.computeScrollbarPositionFromValue(alphaInit, alphaMin, alphaMax, scrollbarSize));
		betaS.setValue(scrollbar.Utility.computeScrollbarPositionFromValue(betaInit, betaMin, betaMax, scrollbarSize));

		Border Kalmanborder = new CompoundBorder(new TitledBorder("Kalman Filter Search in T"),
				new EmptyBorder(layoutManager.Setlayout.c.insets));
		KalmanPanel.add(iniSearchText, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(initialSearchS, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		KalmanPanel.add(alphaText, new GridBagConstraints(5, 2, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(alphaS, new GridBagConstraints(5, 3, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		// KalmanPanel.add(betaText, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
		// GridBagConstraints.EAST,
		// GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		// KalmanPanel.add(betaS, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
		// GridBagConstraints.EAST,
		// GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		KalmanPanel.add(lostlabel, new GridBagConstraints(5, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(lostframe, new GridBagConstraints(5, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		KalmanPanel.add(mintracklength, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(tracklength, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		// KalmanPanel.add(maxSearchTextKalman, new GridBagConstraints(5, 2, 3, 1, 0.0,
		// 0.0, GridBagConstraints.EAST,
		// GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		// KalmanPanel.add(maxSearchKalman, new GridBagConstraints(5, 3, 3, 1, 0.0, 0.0,
		// GridBagConstraints.EAST,
		// GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		KalmanPanel.add(Restart, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(Skeletontime, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(Timetrack, new GridBagConstraints(5, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.setBorder(Kalmanborder);

		return KalmanPanel;
	}

}
