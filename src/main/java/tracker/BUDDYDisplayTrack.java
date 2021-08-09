package tracker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.graph.DefaultWeightedEdge;

import budDetector.Budpointobject;
import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import net.imglib2.img.display.imagej.ImageJFunctions;
import pluginTools.InteractiveBud;

public class BUDDYDisplayTrack {

	public BUDDYTrackModel model;

	public InteractiveBud parent;

	public final int ndims;

	public BUDDYDisplayTrack(final InteractiveBud parent, final BUDDYTrackModel model) {

		this.parent = parent;

		this.model = model;

		parent.resultimp = ImageJFunctions.show(parent.originalimg);

		ndims = parent.resultimp.getNDimensions();

		new SliceObserver(parent.resultimp, new ImagePlusListener());
	}

	public ImagePlus getImp() {
		return parent.resultimp;
	}

	protected class ImagePlusListener implements SliceListener {
		@Override
		public void sliceChanged(ImagePlus arg0) {

			parent.resultimp.show();
			Overlay o = parent.resultimp.getOverlay();

			if (getImp().getOverlay() == null) {
				o = new Overlay();
				getImp().setOverlay(o);
			}

			o.clear();
			getImp().getOverlay().clear();

			String ID = (String) parent.table.getValueAt(parent.row, 0);
			int id = Integer.valueOf(ID);

			// Get the corresponding set for each id
			final HashSet<Budpointobject> Snakeset = model.trackBudpointobjects(id);
			ArrayList<Budpointobject> list = new ArrayList<Budpointobject>();

			Comparator<Budpointobject> ThirdDimcomparison = new Comparator<Budpointobject>() {

				@Override
				public int compare(final Budpointobject A, final Budpointobject B) {

					return A.t - B.t;

				}

			};

			Iterator<Budpointobject> Snakeiter = Snakeset.iterator();
			while (Snakeiter.hasNext()) {

				Budpointobject currentsnake = Snakeiter.next();

				for (int d = 0; d < ndims - 1; ++d)
					if (currentsnake.Location[d] != Double.NaN)
						list.add(currentsnake);

			}
			Collections.sort(list, ThirdDimcomparison);

			for (DefaultWeightedEdge e : model.edgeSet()) {

				Budpointobject Spotbase = model.getEdgeSource(e);
				Budpointobject Spottarget = model.getEdgeTarget(e);

				final double[] startedge = new double[ndims];
				final double[] targetedge = new double[ndims];
				for (int d = 0; d < ndims - 1; ++d) {

					startedge[d] = Spotbase.Location[d];

					targetedge[d] = Spottarget.Location[d];

				}

				if (model.trackIDOf(Spotbase) == id) {
					TextRoi newellipse = new TextRoi(list.get(0).Location[0], list.get(0).Location[1],
							"TrackID: " + id);

					o.add(newellipse);
					o.drawLabels(true);

					o.drawNames(true);

					Line newline = new Line(startedge[0], startedge[1], targetedge[0], targetedge[1]);
					newline.setStrokeColor(Color.GREEN);
					newline.setStrokeWidth(2);

					o.add(newline);

				}

			}

			parent.resultimp.updateAndDraw();
		}
	}

}
