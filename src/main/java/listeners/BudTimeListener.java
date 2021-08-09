package listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import fiji.plugin.btrack.gui.components.CovistoKalmanPanel;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import pluginTools.InteractiveBud;
import pluginTools.InteractiveBud.ValueChange;
import utility.BudShowView;

public class BudTimeListener implements AdjustmentListener {
	final Label label;
	final String string;
	InteractiveBud parent;
	final float min, max;
	final int scrollbarSize;

	final JScrollBar deltaScrollbar;

	public BudTimeListener(final InteractiveBud parent, final Label label, final String string, final float min,
			final float max, final int scrollbarSize, final JScrollBar deltaScrollbar) {
		this.label = label;
		this.parent = parent;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;

		this.deltaScrollbar = deltaScrollbar;
		// deltaScrollbar.addMouseMotionListener(new BudMouseListener(parent,
		// ValueChange.THIRDDIMmouse));
		deltaScrollbar.addMouseListener(new BudStandardMouseListener(parent, ValueChange.THIRDDIMmouse));
		deltaScrollbar
				.setBlockIncrement(utility.BudSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		deltaScrollbar
				.setUnitIncrement(utility.BudSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {

		if (CovistoKalmanPanel.Skeletontime.isEnabled()) {
			parent.imp.getOverlay().clear();
			parent.imp.updateAndDraw();
		}

		parent.thirdDimension = (int) Math
				.round(utility.BudSlicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize));

		deltaScrollbar.setValue(
				utility.BudSlicer.computeScrollbarPositionFromValue(parent.thirdDimension, min, max, scrollbarSize));

		label.setText(string + " = " + parent.thirdDimension);

		parent.inputFieldT.setText(Integer.toString((int) parent.thirdDimension));
		parent.panelFirst.validate();
		parent.panelFirst.repaint();

		BudShowView show = new BudShowView(parent);
		show.shownewT();

	}

}