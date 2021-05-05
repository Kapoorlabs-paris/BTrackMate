package fiji.plugin.btrackmate.gui.wizard.descriptors;

import java.util.Map;

import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.components.ModuleChooserPanel;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.btrackmate.providers.TrackerProvider;
import fiji.plugin.btrackmate.tracking.SpotTrackerFactory;
import fiji.plugin.btrackmate.tracking.sparselap.SparseLAPTrackerFactory;

public class ChooseTrackerDescriptor extends WizardPanelDescriptor
{

	private static final String KEY = "ChooseTracker";

	private final TrackMate btrackmate;

	private final TrackerProvider trackerProvider;

	public ChooseTrackerDescriptor( final TrackerProvider trackerProvider, final TrackMate btrackmate )
	{
		super( KEY );
		this.btrackmate = btrackmate;
		this.trackerProvider = trackerProvider;

		String selectedTracker = SparseLAPTrackerFactory.THIS_TRACKER_KEY; // default
		if ( null != btrackmate.getSettings().trackerFactory )
			selectedTracker = btrackmate.getSettings().trackerFactory.getKey();

		this.targetPanel = new ModuleChooserPanel<>( trackerProvider, "tracker", selectedTracker );
	}

	private void setCurrentChoiceFromPlugin()
	{
		String key = SparseLAPTrackerFactory.THIS_TRACKER_KEY; // default
		if ( null != btrackmate.getSettings().trackerFactory )
			key = btrackmate.getSettings().trackerFactory.getKey();

		@SuppressWarnings( "unchecked" )
		final ModuleChooserPanel< SpotTrackerFactory > component = (fiji.plugin.btrackmate.gui.components.ModuleChooserPanel< SpotTrackerFactory > ) targetPanel;
		component.setSelectedModuleKey( key );
	}

	@Override
	public void displayingPanel()
	{
		setCurrentChoiceFromPlugin();
	}

	@Override
	public void aboutToHidePanel()
	{
		// Configure the detector provider with choice made in panel
		@SuppressWarnings( "unchecked" )
		final ModuleChooserPanel< SpotTrackerFactory > component = (fiji.plugin.btrackmate.gui.components.ModuleChooserPanel< SpotTrackerFactory > ) targetPanel;
		final String trackerKey = component.getSelectedModuleKey();

		// Configure btrackmate settings with selected detector
		final SpotTrackerFactory factory = trackerProvider.getFactory( trackerKey );

		if ( null == factory )
		{
			btrackmate.getModel().getLogger().error( "[ChooseTrackerDescriptor] Cannot find tracker named "
					+ trackerKey
					+ " in current TrackMate modules." );
			return;
		}
		btrackmate.getSettings().trackerFactory = factory;

		/*
		 * Compare current settings with default ones, and substitute default
		 * ones only if the old ones are absent or not compatible with it.
		 */
		final Map< String, Object > currentSettings = btrackmate.getSettings().trackerSettings;
		if ( !factory.checkSettingsValidity( currentSettings ) )
		{
			final Map< String, Object > defaultSettings = factory.getDefaultSettings();
			btrackmate.getSettings().trackerSettings = defaultSettings;
		}
	}
}
