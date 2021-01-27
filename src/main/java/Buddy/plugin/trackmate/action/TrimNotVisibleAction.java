package Buddy.plugin.trackmate.action;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.TrackModel;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import budDetector.BCellobject;

public class TrimNotVisibleAction extends AbstractTMAction
{

	public static final String INFO_TEXT = "<html>"
			+ "This action trims the tracking data by removing anything that is "
			+ "not marked as visible. "
			+ "<p>"
			+ "The BCellobjects that do not belong to a visible track will be "
			+ "removed. The tracks that are not marked "
			+ "as visible will be removed as well. "
			+ "<p>"
			+ "This action is irreversible. It helps limiting the memory "
			+ "and disk space of tracking data that has been properly "
			+ "curated."
			+ "</html>";

	public static final String KEY = "TRIM_NOT_VISIBLE";

	public static final ImageIcon ICON = new ImageIcon( TrackMateWizard.class.getResource( "images/bin_empty.png" ) );

	public static final String NAME = "Trim non-visible data";

	public TrimNotVisibleAction()
	{
	}

	@Override
	public void execute( final TrackMate trackmate )
	{
		final Model model = trackmate.getModel();
		final TrackModel tm = model.getTrackModel();

		final BCellobjectCollection BCellobjects = new BCellobjectCollection();
		BCellobjects.setNumThreads( trackmate.getNumThreads() );
		final Collection< BCellobject > toRemove = new ArrayList<>();

		for ( final Integer trackID : tm.unsortedTrackIDs( false ) )
		{
			if ( !tm.isVisible( trackID ) )
			{
				for ( final BCellobject BCellobject : tm.trackBCellobjects( trackID ) )
				{
					toRemove.add( BCellobject );
				}
			}
			else
			{
				for ( final BCellobject BCellobject : tm.trackBCellobjects( trackID ) )
				{
					BCellobjects.add( BCellobject, BCellobject.getFeature( BCellobject.POSITION_T ).intValue() );
				}
			}

		}
		model.beginUpdate();
		try
		{
			for ( final BCellobject BCellobject : toRemove )
			{
				model.removeBCellobject( BCellobject );
			}
			model.setBCellobjects( BCellobjects, false );
		}
		finally
		{
			model.endUpdate();
		}
	}

	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new TrimNotVisibleAction();
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public String getName()
		{
			return NAME;
		}

	}
}
