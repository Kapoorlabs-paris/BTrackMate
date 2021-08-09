package fiji.plugin.btrack.segmentation;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import fiji.plugin.btrackmate.TrackMatePlugIn;
import pluginTools.GreenCellTrack;
import pluginTools.InteractiveBud;

public class CellSegmentation extends SwingWorker<Void, Void> {

	final InteractiveBud parent;
	final JProgressBar jpb;

	public CellSegmentation(final InteractiveBud parent, final JProgressBar jpb) {

		this.parent = parent;
		this.jpb = jpb;

	}

	public Void doInBackground() throws Exception {

		GreenCellTrack newtrack = new GreenCellTrack(parent, jpb);
		newtrack.ShowCellTime();

		return null;

	}

	@Override
	protected void done() {

		parent.jpb.setIndeterminate(false);
		if (parent.jpb != null)
			utility.BudProgressBar.SetProgressBar(parent.jpb, 100, "Cell Collection complete, starting TrackMate");

		// Save the cell colllection as btrack compatiable csv file
		SaveCellSegmentation savecsv = new SaveCellSegmentation(parent);
		savecsv.Saver();

		parent.imp.close();
		// Initiate the TM plugin
		// TrackMatePlugIn plugin = new TrackMatePlugIn(parent);
		// plugin.run(null);

	}

}
