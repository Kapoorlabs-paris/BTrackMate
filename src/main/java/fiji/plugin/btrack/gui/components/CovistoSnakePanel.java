package fiji.plugin.btrack.gui.components;

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

public class CovistoSnakePanel {

	public static JPanel SnakePanel = new JPanel();

	public static Label Snakelabel, gradientlabel, distlabel;
	public static TextField Snakeiter, gradientthresh, maxdist;

	public static int snakeiterations = 200;
	public static int displaysnake = snakeiterations / 2;
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
	public static final JButton Singlesnake = new JButton("Apply snakes to CurrentView");
	public static JButton AllSnake = new JButton("Snake in 3D/4D");

	public static final Checkbox advanced = new Checkbox("Display advanced Snake parameters");

	public static JPanel SnakePanel(int ndims) {

		regmin = reg / 2.0;
		regmax = reg;

		layoutManager.Setlayout.LayoutSetter(SnakePanel);
		Border snakeborder = new CompoundBorder(new TitledBorder("Active Contour refinement"),
				new EmptyBorder(layoutManager.Setlayout.c.insets));
		Snakelabel = new Label("Enter number of max snake iterations");
		gradientlabel = new Label("Enter gradient threshold");
		distlabel = new Label("Enter max distance to search for edges");

		Snakeiter = new TextField(1);
		gradientthresh = new TextField(1);
		maxdist = new TextField(1);

		Snakeiter.setText(Integer.toString(snakeiterations));
		gradientthresh.setText(Integer.toString(Gradthresh));
		maxdist.setText(Integer.toString(DistMax));
		SnakePanel.add(Snakelabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		SnakePanel.add(Snakeiter, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SnakePanel.add(gradientlabel, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		SnakePanel.add(gradientthresh, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SnakePanel.add(distlabel, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		SnakePanel.add(maxdist, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SnakePanel.add(Singlesnake, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		if (ndims > 2)
			SnakePanel.add(AllSnake, new GridBagConstraints(5, 2, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SnakePanel.add(advanced, new GridBagConstraints(5, 3, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		SnakePanel.setBorder(snakeborder);

		return SnakePanel;

	}

}
