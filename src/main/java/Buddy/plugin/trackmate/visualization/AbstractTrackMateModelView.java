package Buddy.plugin.trackmate.visualization;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.ModelChangeListener;
import Buddy.plugin.trackmate.SelectionChangeEvent;
import Buddy.plugin.trackmate.SelectionChangeListener;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.TrackMateOptionUtils;
import budDetector.BCellobject;
import pluginTools.InteractiveBud;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for BCellobject displayers, that can overlay detected BCellobjects and
 * tracks on top of the image data.
 * <p>
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt; Jan 2011
 */
public abstract class AbstractTrackMateModelView implements SelectionChangeListener, TrackMateModelView, ModelChangeListener
{

	/*
	 * FIELDS
	 */

	/**
	 * A map of String/Object that configures the look and feel of the display.
	 */
	protected Map< String, Object > displaySettings;

	/** The model displayed by this class. */
	protected Model model;

	protected InteractiveBud parent;
	protected final SelectionModel selectionModel;

	/*
	 * PROTECTED CONSTRUCTOR
	 */

	protected AbstractTrackMateModelView( InteractiveBud parent, final Model model, final SelectionModel selectionModel )
	{
		this.selectionModel = selectionModel;
		this.model = model;
		this.parent = parent;
		this.displaySettings = initDisplaySettings( model );
		model.addModelChangeListener( this );
		selectionModel.addSelectionChangeListener( this );
	}

	/*
	 * PUBLIC METHODS
	 */

	@Override
	public void setDisplaySettings( final String key, final Object value )
	{
		displaySettings.put( key, value );
	}

	@Override
	public Object getDisplaySettings( final String key )
	{
		return displaySettings.get( key );
	}

	@Override
	public Map< String, Object > getDisplaySettings()
	{
		return displaySettings;
	}

	/**
	 * This needs to be overriden for concrete implementation to display
	 * selection.
	 */
	@Override
	public void selectionChanged( final SelectionChangeEvent event )
	{
		// Center on selection if we added one BCellobject exactly
		final Map< BCellobject, Boolean > BCellobjectsAdded = event.getBCellobjects();
		if ( BCellobjectsAdded != null && BCellobjectsAdded.size() == 1 )
		{
			final boolean added = BCellobjectsAdded.values().iterator().next();
			if ( added )
			{
				final BCellobject BCellobject = BCellobjectsAdded.keySet().iterator().next();
				centerViewOn( BCellobject );
			}
		}
	}

	@Override
	public Model getModel()
	{
		return model;
	}

	/**
	 * Provides default display settings.
	 *
	 * @param lModel
	 *            the model this view operate on. Needed for some display
	 *            settings.
	 */
	protected Map< String, Object > initDisplaySettings( final Model lModel )
	{
		final Map< String, Object > lDisplaySettings = new HashMap<>( 11 );
		lDisplaySettings.put( KEY_COLOR, DEFAULT_BCellobject_COLOR );
		lDisplaySettings.put( KEY_HIGHLIGHT_COLOR, DEFAULT_HIGHLIGHT_COLOR );
		lDisplaySettings.put( KEY_BCellobjectS_VISIBLE, true );
		lDisplaySettings.put( KEY_DISPLAY_BCellobject_NAMES, false );
		lDisplaySettings.put( KEY_BCellobject_COLORING, new DummyBCellobjectColorGenerator() );
		lDisplaySettings.put( KEY_BCellobject_RADIUS_RATIO, 1.0d );
		lDisplaySettings.put( KEY_TRACKS_VISIBLE, true );
		lDisplaySettings.put( KEY_TRACK_DISPLAY_MODE, DEFAULT_TRACK_DISPLAY_MODE );
		lDisplaySettings.put( KEY_TRACK_DISPLAY_DEPTH, DEFAULT_TRACK_DISPLAY_DEPTH );
		lDisplaySettings.put( KEY_TRACK_COLORING, new DummyTrackColorGenerator() );
		lDisplaySettings.put( KEY_COLORMAP, TrackMateOptionUtils.getOptions().getPaintScale() );
		lDisplaySettings.put( KEY_LIMIT_DRAWING_DEPTH, DEFAULT_LIMIT_DRAWING_DEPTH );
		lDisplaySettings.put( KEY_DRAWING_DEPTH, DEFAULT_DRAWING_DEPTH );
		return lDisplaySettings;
	}

}
