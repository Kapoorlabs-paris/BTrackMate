package fiji.plugin.globalbtrack.gui.components;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoFunctionPanel {

	public static JPanel FunctionPanel = new JPanel();

	public static Label Functionlabel, gradientlabel, distlabel;
	public static TextField Functioniter, gradientthresh, maxdist;

	public static int Functioniterations = 200;
	public static int displayFunction = Functioniterations / 2;
	public static int Gradthresh = 1;
	public static int DistMax = 100;
	public static double Displacement_min = 0.5;
	public static double Displacement_max = 5.0;
	public static double Threshold_dist_positive = 10;
	public static double Threshold_dist_negative = 10;
	public static double Inv_alpha_min = 0.2;
	public static double Inv_alpha_max = 10.0;
	public static double Mul_factor = 0.99;
	// maximum displacement
	public static double force = 10;
	// regulari1ation factors, min and max
	public static double reg = 5;
	public static double regmin, regmax;

	public static JPanel FunctionPanel() {

		regmin = reg / 2.0;
		regmax = reg;

		layoutManager.Setlayout.LayoutSetter(FunctionPanel);
		Border Functionborder = new CompoundBorder(new TitledBorder("User requested Function computer"),
				new EmptyBorder(layoutManager.Setlayout.c.insets));
		Functionlabel = new Label("My function parameter");
		gradientlabel = new Label("My second function parameter");
		distlabel = new Label("Just a little bit more");

		Functioniter = new TextField(1);
		gradientthresh = new TextField(1);
		maxdist = new TextField(1);

		Functioniter.setText(Integer.toString(Functioniterations));
		gradientthresh.setText(Integer.toString(Gradthresh));
		maxdist.setText(Integer.toString(DistMax));
		FunctionPanel.add(Functionlabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		FunctionPanel.add(Functioniter, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		FunctionPanel.add(gradientlabel, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		FunctionPanel.add(gradientthresh, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		FunctionPanel.add(distlabel, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		FunctionPanel.add(maxdist, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		FunctionPanel.setBorder(Functionborder);

		return FunctionPanel;

	}

}
