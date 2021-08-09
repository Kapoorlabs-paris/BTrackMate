package fiji.plugin.btrackmate.visualization;

import java.awt.Color;

import fiji.plugin.btrackmate.Spot;

/**
 * A dummy spot color generator that always return the default color.
 *
 * @author Jean-Yves Tinevez - 2013. Revised December 2020.
 */
public class UniformSpotColorGenerator implements FeatureColorGenerator<Spot> {

	private final Color color;

	public UniformSpotColorGenerator(final Color color) {
		this.color = color;
	}

	@Override
	public Color color(final Spot obj) {
		return color;
	}
}
