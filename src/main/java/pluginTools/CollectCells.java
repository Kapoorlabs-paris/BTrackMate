package pluginTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import Buddy.plugin.trackmate.TrackMatePlugIn_;


public class CollectCells extends SwingWorker<Void, Void> {
	
	
	final InteractiveBud parent;
	final JProgressBar jpb;
	
	
	public CollectCells(final InteractiveBud parent, final JProgressBar jpb) {
		
		
		this.parent = parent;
		this.jpb = jpb;
		
	}

	
	public Void doInBackground() throws Exception {
		
		
		
		// set up executor service
		final ExecutorService taskExecutor = Executors.newCachedThreadPool();
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		
		tasks.add(Executors.callable(new CellTrack(parent, jpb)));
		taskExecutor.invokeAll(tasks);
		
		
		
		
		
		return null;
		
	}
	@Override
	protected void done() {
		
		parent.jpb.setIndeterminate(false);
		if(parent.jpb!=null )
			utility.BudProgressBar.SetProgressBar(parent.jpb, 100 ,
					"Collected all cells, starting TrackMate");
		
		
		TrackMatePlugIn_ plugin = new TrackMatePlugIn_(parent);
		plugin.run(null);
	}
	
}
