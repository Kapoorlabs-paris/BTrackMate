package listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JScrollBar;

import fiji.plugin.globalbtrack.gui.components.CovistoKalmanPanel;
import ij.IJ;
import ij.gui.OvalRoi;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import pluginTools.InteractiveBud;
import pluginTools.InteractiveBud.ValueChange;
import utility.BudShowView;

public class BudZListener implements AdjustmentListener {
	final String string;
	InteractiveBud parent;
	final float min, max;
	final int scrollbarSize;
	final Label label;
	final JScrollBar deltaScrollbar;

	public BudZListener(final InteractiveBud parent, final Label label, final String string, final float min,
			final float max, final int scrollbarSize, final JScrollBar deltaScrollbar) {
		this.parent = parent;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;
		this.label = label;
		this.deltaScrollbar = deltaScrollbar;
		deltaScrollbar.addMouseMotionListener(new BudMouseListener(parent, ValueChange.THIRDDIMmouse));
		deltaScrollbar
				.setBlockIncrement(utility.BudSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		deltaScrollbar
				.setUnitIncrement(utility.BudSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {

		parent.thirdDimension = (int) Math
				.round(utility.BudSlicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize));

		deltaScrollbar.setValue(
				utility.BudSlicer.computeScrollbarPositionFromValue(parent.thirdDimension, min, max, scrollbarSize));

		parent.overlay.clear();

		label.setText(string + " = " + parent.thirdDimension);

		parent.panelFirst.validate();
		parent.panelFirst.repaint();

	}

}