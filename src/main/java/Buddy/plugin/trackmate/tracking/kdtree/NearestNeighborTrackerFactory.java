package Buddy.plugin.trackmate.tracking.kdtree;

import static Buddy.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static Buddy.plugin.trackmate.io.IOUtils.writeAttribute;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.DEFAULT_LINKING_MAX_DISTANCE;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_MAX_DISTANCE;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.gui.ConfigurationPanel;
import Buddy.plugin.trackmate.gui.panels.tracker.NearestNeighborTrackerSettingsPanel;
import Buddy.plugin.trackmate.tracking.BCellobjectTracker;
import Buddy.plugin.trackmate.tracking.BCellobjectTrackerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = BCellobjectTrackerFactory.class, priority = Priority.VERY_LOW, visible = false )
public class NearestNeighborTrackerFactory implements BCellobjectTrackerFactory
{
	public static final String TRACKER_KEY = "NEAREST_NEIGHBOR_TRACKER";

	public static final String NAME = null;

	public static final String INFO_TEXT = "<html>" + " Not a valid choice for BTrackMate " + " </html>";

	private String errorMessage;

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public BCellobjectTracker create( final BCellobjectCollection BCellobjects, final Map< String, Object > settings )
	{
		return new NearestNeighborTracker( BCellobjects, settings );
	}

	@Override
	public ConfigurationPanel getTrackerConfigurationPanel( final Model model )
	{
		final String spaceUnits = model.getSpaceUnits();
		return new NearestNeighborTrackerSettingsPanel( NAME, INFO_TEXT, spaceUnits );
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		final StringBuilder str = new StringBuilder();
		final boolean ok = writeAttribute( settings, element, KEY_LINKING_MAX_DISTANCE, Double.class, str );
		if (!ok) {
			errorMessage = str.toString();
		}
		return ok;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		settings.clear();
		final StringBuilder errorHolder = new StringBuilder();
		final boolean ok = readDoubleAttribute( element, settings, KEY_LINKING_MAX_DISTANCE, errorHolder );
		if ( !ok )
		{
			errorMessage = errorHolder.toString();
		}
		return ok;
	}

	@Override
	public String toString( final Map< String, Object > sm )
	{
		return String.format( "  Max distance: %.1f\n", ( Double ) sm.get( KEY_LINKING_MAX_DISTANCE ) );
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > settings = new HashMap<>();
		settings.put( KEY_LINKING_MAX_DISTANCE, DEFAULT_LINKING_MAX_DISTANCE );
		return settings;
	}

	@Override
	public boolean checkSettingsValidity( final Map< String, Object > settings )
	{
		final StringBuilder str = new StringBuilder();
		final boolean ok = NearestNeighborTracker.checkInput( settings, str );
		if ( !ok )
		{
			errorMessage = str.toString();
		}
		return ok;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

}
