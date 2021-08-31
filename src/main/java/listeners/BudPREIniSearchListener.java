package listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import fiji.plugin.globalbtrack.gui.components.CovistoKalmanPanel;
import pluginTools.InteractiveBud;
import zGUI.CovistoZselectPanel;

public class BudPREIniSearchListener implements AdjustmentListener {

	final Label label;
	final String string;
	final InteractiveBud parent;
	final float min, max;
	final int scrollbarSize;
	final JScrollBar scrollbar;

	public BudPREIniSearchListener(final InteractiveBud parent, final Label label, final String string, final float min,
			final float max, final int scrollbarSize, final JScrollBar scrollbar) {

		this.parent = parent;
		this.label = label;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;
		this.scrollbar = scrollbar;

		scrollbar.setBlockIncrement(utility.BudSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		scrollbar.setUnitIncrement(utility.BudSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
	}

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent event) {
		CovistoKalmanPanel.initialSearchradius = utility.BudSlicer.computeValueFromScrollbarPosition(event.getValue(),
				min, max, scrollbarSize);

		label.setText(string + " = " + parent.nf.format(CovistoKalmanPanel.initialSearchradius));

	}

}
