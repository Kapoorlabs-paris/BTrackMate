package fiji.plugin.globalbtrackmate.gui.components;

import org.jfree.data.statistics.HistogramDataset;

/**
 * A {@link HistogramDataset} that returns the log of the count in each bin
 * (plus one), so as to have a logarithmic plot.
 * 
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt; Dec 28, 2010
 *
 */
public class LogHistogramDataset extends HistogramDataset {

	private static final long serialVersionUID = 6012084169414194555L;

	@Override
	public Number getY(int series, int item) {
		Number val = super.getY(series, item);
		return Math.log(1 + val.doubleValue());
	}

}
