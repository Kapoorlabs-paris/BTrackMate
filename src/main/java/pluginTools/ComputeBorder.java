package pluginTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import budDetector.Budobject;
import budDetector.Roiobject;
import displayBud.DisplayListOverlay;
import ij.gui.OvalRoi;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import utility.GetNearest;

public class ComputeBorder extends SwingWorker<Void, Void> {

	final InteractiveBud parent;
	final JProgressBar jpb;

	public ComputeBorder(final InteractiveBud parent, final JProgressBar jpb) {

		this.parent = parent;
		this.jpb = jpb;

	}

	public Void doInBackground() throws Exception {

		BoundaryTrack newtrack = new BoundaryTrack(parent, jpb);
		newtrack.ShowBoundary();

		return null;

	}

	@Override
	protected void done() {

		parent.jpb.setIndeterminate(false);
		if (parent.jpb != null)
			utility.BudProgressBar.SetProgressBar(parent.jpb, 100,
					"Bud endpoints computed for all buds present at timepoint " + parent.thirdDimension);

	}

}
