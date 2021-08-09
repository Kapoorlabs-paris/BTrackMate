package skeleton;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingWorker;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.Budpointobject;
import ij.ImageStack;
import net.imglib2.RealLocalizable;
import pluginTools.BoundaryTrack;
import pluginTools.InteractiveBud;

public class SkeletonizeBuds extends SwingWorker<Void, Void> {

	final InteractiveBud parent;

	public SkeletonizeBuds(final InteractiveBud parent) {

		this.parent = parent;
	}

	@Override
	protected Void doInBackground() throws Exception {

		parent.jpb.setIndeterminate(false);
		parent.Cardframe.validate();
		parent.overlay.clear();
		parent.imp.updateAndDraw();

		BoundaryTrack newtrack = new BoundaryTrack(parent, parent.jpb);
		newtrack.ShowBoundaryTime();

		return null;
	}

}