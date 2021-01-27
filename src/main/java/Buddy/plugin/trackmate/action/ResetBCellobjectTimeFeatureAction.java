/**
 *
 */
package Buddy.plugin.trackmate.action;

import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import budDetector.BCellobject;

import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

public class ResetBCellobjectTimeFeatureAction extends AbstractTMAction {


	public static final ImageIcon ICON = new ImageIcon(TrackMateWizard.class.getResource("images/time.png"));
	public static final String NAME = "Reset BCellobject time";
	public static final String INFO_TEXT = "<html>" +
			"Reset the time feature of all BCellobjects: it is set to the frame number "  +
			"times the frame interval. " +
			"</html>";

	private static final String KEY = "RESET_BCellobject_TIME";

	@Override
	public void execute(final TrackMate trackmate) {
		logger.log("Reset BCellobject time.\n");
		double dt = trackmate.getSettings().dt;
		if (dt == 0) {
			dt = 1;
		}
		final BCellobjectCollection BCellobjects = trackmate.getModel().getBCellobjects();
		final Set<Integer> frames = BCellobjects.keySet();
		for(final int frame : frames) {
			for (final Iterator<BCellobject> iterator = BCellobjects.iterator(frame); iterator.hasNext();) {
				iterator.next().putFeature(BCellobject.POSITION_T, frame * dt);
			}
			logger.setProgress((double) (frame + 1) / frames.size());
		}
		logger.log("Done.\n");
		logger.setProgress(0);
	}

	@Plugin( type = TrackMateActionFactory.class, visible = false )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getName()
		{
			return NAME;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new ResetBCellobjectTimeFeatureAction();
		}
	}
}
