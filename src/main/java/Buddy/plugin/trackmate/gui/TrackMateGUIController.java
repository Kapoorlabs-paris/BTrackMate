package Buddy.plugin.trackmate.gui;

import static Buddy.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_HIGHLIGHT_COLOR;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_BCellobject_COLOR;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_TRACK_DISPLAY_DEPTH;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_TRACK_DISPLAY_MODE;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_COLOR;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_COLORMAP;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_DISPLAY_BCellobject_NAMES;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_HIGHLIGHT_COLOR;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_BCellobjectS_VISIBLE;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_BCellobject_COLORING;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_BCellobject_RADIUS_RATIO;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACKS_VISIBLE;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_COLORING;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_DISPLAY_DEPTH;
import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_DISPLAY_MODE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.object.ObjectService;
import Buddy.plugin.trackmate.visualization.BCellobjectColorGenerator;
import Buddy.plugin.trackmate.visualization.BCellobjectColorGeneratorPerTrackFeature;
import Buddy.plugin.trackmate.visualization.ManualBCellobjectColorGenerator;
import Buddy.plugin.trackmate.gui.descriptors.StartDialogDescriptor;
import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.TrackMateOptionUtils;
import Buddy.plugin.trackmate.action.AbstractTMAction;
import Buddy.plugin.trackmate.action.ExportAllBCellobjectsStatsAction;
import Buddy.plugin.trackmate.action.ExportStatsToIJAction;
import Buddy.plugin.trackmate.features.ModelFeatureUpdater;
import Buddy.plugin.trackmate.features.edges.EdgeVelocityAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackIndexAnalyzer;
import Buddy.plugin.trackmate.gui.descriptors.ActionChooserDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.BCellobjectFilterDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.GrapherDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.LoadDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.LogPanelDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.SaveDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.SomeDialogDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.TrackFilterDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.TrackerChoiceDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.TrackerConfigurationDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.TrackingDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.ViewChoiceDescriptor;
import Buddy.plugin.trackmate.gui.descriptors.WizardPanelDescriptor;
import Buddy.plugin.trackmate.gui.panels.components.ColorByFeatureGUIPanel;
import Buddy.plugin.trackmate.providers.ActionProvider;
import Buddy.plugin.trackmate.providers.EdgeAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackerProvider;
import Buddy.plugin.trackmate.providers.ViewProvider;
import Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider;
import Buddy.plugin.trackmate.tracking.ManualTrackerFactory;
import Buddy.plugin.trackmate.util.TMUtils;
import Buddy.plugin.trackmate.visualization.FeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.ManualEdgeColorGenerator;
import Buddy.plugin.trackmate.visualization.PerEdgeFeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.PerTrackFeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import Buddy.plugin.trackmate.visualization.trackscheme.BCellobjectImageUpdater;
import Buddy.plugin.trackmate.visualization.trackscheme.TrackScheme;
import budDetector.BCellobject;
import ij.IJ;
import ij.Prefs;
import pluginTools.InteractiveBud;

public class TrackMateGUIController implements ActionListener
{

	/*
	 * FIELDS
	 */

	private static final boolean DEBUG = false;

	protected final Logger logger;

	/** The trackmate piloted here. */
	protected final TrackMate trackmate;

	/** The GUI controlled by this controller. */
	protected final TrackMateWizard gui;

	protected final TrackMateGUIModel guimodel;

	protected BCellobjectAnalyzerProvider BCellobjectAnalyzerProvider;

	protected EdgeAnalyzerProvider edgeAnalyzerProvider;

	protected TrackAnalyzerProvider trackAnalyzerProvider;


	protected ViewProvider viewProvider;

	protected TrackerProvider trackerProvider;

	protected ActionProvider actionProvider;


	protected StartDialogDescriptor startDialoDescriptor;


	protected ViewChoiceDescriptor viewChoiceDescriptor;


	protected TrackerChoiceDescriptor trackerChoiceDescriptor;

	protected TrackerConfigurationDescriptor trackerConfigurationDescriptor;

	protected TrackingDescriptor trackingDescriptor;

	protected GrapherDescriptor grapherDescriptor;

	protected TrackFilterDescriptor trackFilterDescriptor;

	protected ConfigureViewsDescriptor configureViewsDescriptor;

	protected ActionChooserDescriptor actionChooserDescriptor;

	protected LogPanelDescriptor logPanelDescriptor;

	protected SaveDescriptor saveDescriptor;

	protected LoadDescriptor loadDescriptor;

	protected Collection< WizardPanelDescriptor > registeredDescriptors;

	protected SelectionModel selectionModel;

	protected PerTrackFeatureColorGenerator trackColorGenerator;

	protected PerEdgeFeatureColorGenerator edgeColorGenerator;

	protected FeatureColorGenerator< BCellobject > BCellobjectColorGenerator;

	protected ManualEdgeColorGenerator manualEdgeColorGenerator;
	protected ManualBCellobjectColorGenerator manualBCellobjectColorGenerator;

	protected FeatureColorGenerator< BCellobject > BCellobjectColorGeneratorPerTrackFeature;

	/**
	 * The listener in charge of listening to display settings changes and
	 * forwarding them to the views registered in the {@link #guimodel}.
	 */
	protected DisplaySettingsListener displaySettingsListener;

	/*
	 * CONSTRUCTOR
	 */

	public TrackMateGUIController(final InteractiveBud parent, final TrackMate trackmate )
	{

		// I can't stand the metal look. If this is a problem, contact me
		// (jeanyves.tinevez@gmail.com)
		if ( IJ.isMacOSX() || IJ.isWindows() )
		{
			try
			{
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			catch ( final ClassNotFoundException e )
			{
				e.printStackTrace();
			}
			catch ( final InstantiationException e )
			{
				e.printStackTrace();
			}
			catch ( final IllegalAccessException e )
			{
				e.printStackTrace();
			}
			catch ( final UnsupportedLookAndFeelException e )
			{
				e.printStackTrace();
			}
		}

		this.trackmate = trackmate;
		trackmate.setNumThreads( Prefs.getThreads() );

		/*
		 * Instantiate GUI
		 */

		this.gui = new TrackMateWizard( this );
		this.logger = gui.getLogger();

		/*
		 * Add this TrackMate instance to the ObjectService
		 */
		ObjectService objectService = TMUtils.getContext().service( ObjectService.class );
		if ( objectService != null )
			objectService.addObject( trackmate );

		// Feature updater
		final ModelFeatureUpdater modelFeatureUpdater = new ModelFeatureUpdater( trackmate.getModel(), trackmate.getSettings() );
		modelFeatureUpdater.setNumThreads( trackmate.getNumThreads() );

		// Feature colorers
		this.BCellobjectColorGenerator = createBCellobjectColorGenerator();
		this.edgeColorGenerator = createEdgeColorGenerator();
		this.trackColorGenerator = createTrackColorGenerator();
		this.manualEdgeColorGenerator = createManualEdgeColorGenerator();
		this.manualBCellobjectColorGenerator = createManualBCellobjectColorGenerator();
		this.BCellobjectColorGeneratorPerTrackFeature = createBCellobjectColorGeneratorPerTrackFeature();

		// 0.
		this.guimodel = new TrackMateGUIModel();
		this.guimodel.setDisplaySettings( createDisplaySettings( trackmate.getModel() ) );
		this.displaySettingsListener = new DisplaySettingsListener()
		{
			@Override
			public void displaySettingsChanged( final DisplaySettingsEvent event )
			{
				guimodel.getDisplaySettings().put( event.getKey(), event.getNewValue() );
				for ( final TrackMateModelView view : guimodel.views )
				{
					view.setDisplaySettings( event.getKey(), event.getNewValue() );
					view.refresh();
				}
			}
		};

		// 1.
		createSelectionModel();
		// 2.
		createProviders();
		// 3.
		registeredDescriptors = createDescriptors();

		trackmate.getModel().setLogger( logger );
		gui.setVisible( true );
		gui.addActionListener( this );

		init();
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Creates a new {@link TrackMateGUIController} instance, set to operate on
	 * the specified {@link TrackMate} instance.
	 * <p>
	 * Subclassers want to override this method to return the correct type.
	 *
	 * @param lTrackmate
	 *            the instance that will be piloted by the new controller.
	 * @return a new instance of the controller.
	 */
	public TrackMateGUIController createOn(final InteractiveBud parent, final TrackMate lTrackmate )
	{
		return new TrackMateGUIController( parent,  lTrackmate );
	}

	/**
	 * Closes the GUI controlled by this instance.
	 */
	public void quit()
	{
		gui.dispose();
	}

	/**
	 * Exposes the {@link TrackMateWizard} instance controlled here.
	 */
	public TrackMateWizard getGUI()
	{
		return gui;
	}

	/**
	 * Exposes the {@link TrackMate} trackmate piloted by the wizard.
	 */
	public TrackMate getPlugin()
	{
		return trackmate;
	}

	/**
	 * Exposes the {@link SelectionModel} shared amongst all
	 * {@link Buddy.plugin.trackmate.SelectionChangeListener}s controlled by this
	 * instance.
	 *
	 * @return the {@link SelectionModel}.
	 */
	public SelectionModel getSelectionModel()
	{
		return selectionModel;
	}

	public TrackMateGUIModel getGuimodel()
	{
		return guimodel;
	}

	/**
	 * Sets the GUI current state via a key string. Registered descriptors are
	 * parsed until one is found that has a matching key (
	 * {@link WizardPanelDescriptor#getKey()}). Then it is displayed. If a
	 * matching key is not found, nothing is done, and an error is logged in the
	 * {@link LogPanel}.
	 * <p>
	 * This method is typically called to restore a saved GUI state.
	 *
	 * @param stateKey
	 *            the target state string.
	 */
	public void setGUIStateString( final String stateKey )
	{
		for ( final WizardPanelDescriptor descriptor : registeredDescriptors )
		{

			if ( stateKey.equals( descriptor.getKey() ) )
			{

				guimodel.currentDescriptor = descriptor;
				gui.show( descriptor );
				if ( null == nextDescriptor( descriptor ) )
				{
					gui.setNextButtonEnabled( false );
				}
				else
				{
					gui.setNextButtonEnabled( true );
				}
				if ( null == previousDescriptor( descriptor ) )
				{
					gui.setPreviousButtonEnabled( false );
				}
				else
				{
					gui.setPreviousButtonEnabled( true );
				}
				descriptor.displayingPanel();
				return;

			}
		}

		logger.error( "Cannot move to state " + stateKey + ". Unknown state.\n" );
	}

	/**
	 * Returns the {@link ViewProvider} instance, serving
	 * {@link TrackMateModelView}s to this GUI
	 *
	 * @return the view provider.
	 */
	public ViewProvider getViewProvider()
	{
		return viewProvider;
	}

	/**
	 * Returns the {@link DetectorProvider} instance, serving
	 * {@link Buddy.plugin.trackmate.detection.BCellobjectDetectorFactory}s to this GUI
	 *
	 * @return the detector provider.
	 */

	/**
	 * Returns the {@link BCellobjectAnalyzerProvider} instance, serving
	 * {@link Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory}s to this
	 * GUI.
	 *
	 * @return the BCellobject analyzer provider.
	 */
	public BCellobjectAnalyzerProvider getBCellobjectAnalyzerProvider()
	{
		return BCellobjectAnalyzerProvider;
	}

	/**
	 * Returns the {@link EdgeAnalyzerProvider} instance, serving
	 * {@link Buddy.plugin.trackmate.features.edges.EdgeAnalyzer}s to this GUI.
	 *
	 * @return the edge analyzer provider.
	 */
	public EdgeAnalyzerProvider getEdgeAnalyzerProvider()
	{
		return edgeAnalyzerProvider;
	}
	
	protected ManualBCellobjectColorGenerator createManualBCellobjectColorGenerator() {
		return new ManualBCellobjectColorGenerator();
	}

	/**
	 * Returns the {@link TrackAnalyzerProvider} instance, serving
	 * {@link Buddy.plugin.trackmate.features.track.TrackAnalyzer}s to this GUI.
	 *
	 * @return the track analyzer provider.
	 */
	public TrackAnalyzerProvider getTrackAnalyzerProvider()
	{
		return trackAnalyzerProvider;
	}

	/**
	 * Returns the {@link TrackerProvider} instance, serving
	 * {@link Buddy.plugin.trackmate.tracking.BCellobjectTracker}s to this GUI.
	 *
	 * @return the tracker provider.
	 */
	public TrackerProvider getTrackerProvider()
	{
		return trackerProvider;
	}

	/*
	 * PROTECTED METHODS
	 */

	protected void createSelectionModel()
	{
		selectionModel = new SelectionModel( trackmate.getModel() );
	}

	protected FeatureColorGenerator< BCellobject > createBCellobjectColorGenerator()
	{
		return new BCellobjectColorGenerator( trackmate.getModel() );
	}

	protected PerEdgeFeatureColorGenerator createEdgeColorGenerator()
	{
		return new PerEdgeFeatureColorGenerator( trackmate.getModel(), EdgeVelocityAnalyzer.VELOCITY );
	}

	protected PerTrackFeatureColorGenerator createTrackColorGenerator()
	{
		final PerTrackFeatureColorGenerator generator = new PerTrackFeatureColorGenerator( trackmate.getModel(), TrackIndexAnalyzer.TRACK_INDEX );
		return generator;
	}



	protected ManualEdgeColorGenerator createManualEdgeColorGenerator()
	{
		return new ManualEdgeColorGenerator( trackmate.getModel() );
	}

	protected FeatureColorGenerator< BCellobject > createBCellobjectColorGeneratorPerTrackFeature()
	{
		final FeatureColorGenerator< BCellobject > generator = new BCellobjectColorGeneratorPerTrackFeature( trackmate.getModel(), TrackIndexAnalyzer.TRACK_INDEX );
		return generator;
	}

	protected void createProviders()
	{
		BCellobjectAnalyzerProvider = new BCellobjectAnalyzerProvider();
		edgeAnalyzerProvider = new EdgeAnalyzerProvider();
		trackAnalyzerProvider = new TrackAnalyzerProvider();
		viewProvider = new ViewProvider();
		trackerProvider = new TrackerProvider();
		actionProvider = new ActionProvider();
	}

	/**
	 * Creates the map of next descriptor for each descriptor.
	 */
	protected Collection< WizardPanelDescriptor > createDescriptors()
	{

		/*
		 * Logging panel: receive message, share with the TrackMateModel
		 */
		final LogPanel logPanel = gui.getLogPanel();
		logPanelDescriptor = new LogPanelDescriptor( logPanel );

		/*
		 * Start panel
		 */
		startDialoDescriptor = new StartDialogDescriptor( this )
		{
			@Override
			public void aboutToHidePanel()
			{
				super.aboutToHidePanel();
				// Reset the default save location.
				SomeDialogDescriptor.file = null;
			}

			@Override
			public void displayingPanel()
			{
				super.displayingPanel();
				if ( startDialoDescriptor.isImpValid() )
				{
					// Ensure we reset default save location
					gui.setNextButtonEnabled( true );
				}
				else
				{
					gui.setNextButtonEnabled( false );
				}
			}
		};
		// Listen if the selected imp is valid and toggle next button
		// accordingly.
		startDialoDescriptor.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( startDialoDescriptor.isImpValid() )
				{
					// Ensure we reset default save location
					gui.setNextButtonEnabled( true );
				}
				else
				{
					gui.setNextButtonEnabled( false );
				}
			}
		} );

		/*
		 * Choose detector
		 */

		/*
		 * Configure chosen detector
		 */

		/*
		 * Select and render a view
		 */
		// We need the GUI model to register the created view there.
		viewChoiceDescriptor = new ViewChoiceDescriptor( viewProvider, guimodel, this );

		
		/*
		 * Choose a tracker
		 */
		trackerChoiceDescriptor = new TrackerChoiceDescriptor( trackerProvider, trackmate, this );

		/*
		 * Configure chosen tracker
		 */
		trackerConfigurationDescriptor = new TrackerConfigurationDescriptor( trackerProvider, trackmate, this );

		/*
		 * Execute tracking
		 */
		trackingDescriptor = new TrackingDescriptor( this );

		/*
		 * Track filtering
		 */
		trackFilterDescriptor = new TrackFilterDescriptor( trackmate, trackColorGenerator, this );
		trackFilterDescriptor.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent event )
			{
				if ( trackFilterDescriptor.getComponent().getColorCategory().equals( ColorByFeatureGUIPanel.Category.DEFAULT ) )
				{
					trackColorGenerator.setFeature( null );
				}
				else
				{
					trackColorGenerator.setFeature( trackFilterDescriptor.getComponent().getColorFeature() );
				}
				for ( final TrackMateModelView view : guimodel.views )
				{
					view.setDisplaySettings( TrackMateModelView.KEY_TRACK_COLORING, trackColorGenerator );
					view.refresh();
				}
			}
		} );
		trackFilterDescriptor.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent event )
			{
				// We set the thresholds field of the model but do not touch its
				// selected BCellobject field yet.
				trackmate.getSettings().setTrackFilters( trackFilterDescriptor.getComponent().getFeatureFilters() );
				trackmate.execTrackFiltering( false );
			}
		} );

		/*
		 * Finished, let's change the display settings.
		 */
		configureViewsDescriptor = new ConfigureViewsDescriptor( trackmate, BCellobjectColorGenerator, edgeColorGenerator, trackColorGenerator, BCellobjectColorGeneratorPerTrackFeature, manualBCellobjectColorGenerator, manualEdgeColorGenerator, this );
		configureViewsDescriptor.getComponent().addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent event )
			{
				if ( event == configureViewsDescriptor.getComponent().TRACK_SCHEME_BUTTON_PRESSED )
				{
					launchTrackScheme();

				}
				else if ( event == configureViewsDescriptor.getComponent().DO_ANALYSIS_BUTTON_PRESSED )
				{
					launchDoAnalysis( false );

				}
				else if ( event == configureViewsDescriptor.getComponent().DO_ANALYSIS_BUTTON_WITH_SHIFT_PRESSED )
				{
					launchDoAnalysis( true );

				}
				else
				{
					System.out.println( "[TrackMateGUIController] Caught unknown event: " + event );
				}
			}
		} );
		configureViewsDescriptor.getComponent().addDisplaySettingsChangeListener( displaySettingsListener );

		/*
		 * Export and graph features.
		 */
		grapherDescriptor = new GrapherDescriptor( trackmate, this );

		/*
		 * Offer to take some actions on the data.
		 */
		actionChooserDescriptor = new ActionChooserDescriptor( actionProvider, trackmate, this );

		/*
		 * Save descriptor
		 */
		saveDescriptor = new SaveDescriptor( this );

		/*
		 * Load descriptor
		 */
		loadDescriptor = new LoadDescriptor( this );

		/*
		 * Store created descriptors
		 */
		final ArrayList< WizardPanelDescriptor > descriptors = new ArrayList< >( 16 );
		descriptors.add( actionChooserDescriptor );
		descriptors.add( configureViewsDescriptor );
		descriptors.add( grapherDescriptor );
		descriptors.add( loadDescriptor );
		descriptors.add( logPanelDescriptor );
		descriptors.add( saveDescriptor );
		descriptors.add( startDialoDescriptor );
		descriptors.add( trackFilterDescriptor );
		descriptors.add( trackerChoiceDescriptor );
		descriptors.add( trackerConfigurationDescriptor );
		descriptors.add( trackingDescriptor );
		descriptors.add( viewChoiceDescriptor );
		return descriptors;
	}

	protected WizardPanelDescriptor getFirstDescriptor()
	{
		return startDialoDescriptor;
	}

	protected WizardPanelDescriptor nextDescriptor( final WizardPanelDescriptor currentDescriptor )
	{

		if ( currentDescriptor == startDialoDescriptor )
		{
			return trackerChoiceDescriptor;

		}
		
		else if ( currentDescriptor == trackerChoiceDescriptor )
		{
			if ( null == trackmate.getSettings().trackerFactory  )
				return trackFilterDescriptor;

			return trackerConfigurationDescriptor;

		}
		else if ( currentDescriptor == trackerConfigurationDescriptor )
		{
			return trackingDescriptor;

		}
		else if ( currentDescriptor == trackingDescriptor )
		{
			return trackFilterDescriptor;

		}
		else if ( currentDescriptor == trackFilterDescriptor )
		{
			return configureViewsDescriptor;

		}
		else if ( currentDescriptor == configureViewsDescriptor )
		{
			return grapherDescriptor;

		}
		else if ( currentDescriptor == grapherDescriptor )
		{
			return actionChooserDescriptor;

		}
		else if ( currentDescriptor == actionChooserDescriptor )
		{
			return null;

		}
		else
		{
			throw new IllegalArgumentException( "Next descriptor for " + currentDescriptor + " is unknown." );
		}
	}

	protected WizardPanelDescriptor previousDescriptor( final WizardPanelDescriptor currentDescriptor )
	{

		if ( currentDescriptor == startDialoDescriptor )
		{
			return null;

		}
		
		
		else if ( currentDescriptor == trackerChoiceDescriptor )
		{
			return startDialoDescriptor;

		}
		else if ( currentDescriptor == trackerConfigurationDescriptor )
		{
			return trackerChoiceDescriptor;

		}
		else if ( currentDescriptor == trackingDescriptor )
		{
			return trackerConfigurationDescriptor;

		}
		else if ( currentDescriptor == trackFilterDescriptor )
		{
			if ( null == trackmate.getSettings().trackerFactory || trackmate.getSettings().trackerFactory.getKey().equals( ManualTrackerFactory.TRACKER_KEY ) )
				return trackerChoiceDescriptor;

			return trackerConfigurationDescriptor;

		}
		else if ( currentDescriptor == configureViewsDescriptor )
		{
			return trackFilterDescriptor;

		}
		else if ( currentDescriptor == grapherDescriptor )
		{
			return configureViewsDescriptor;

		}
		else if ( currentDescriptor == actionChooserDescriptor )
		{
			return grapherDescriptor;

		}
		else
		{
			throw new IllegalArgumentException( "Previous descriptor for " + currentDescriptor + " is unknown." );
		}
	}

	/**
	 * Display the first panel
	 */
	protected void init()
	{
		// We need to listen to events happening on the View configuration
		configureViewsDescriptor.getComponent().addActionListener( this );

		// Get start panel id
		gui.setPreviousButtonEnabled( false );
		final WizardPanelDescriptor panelDescriptor = getFirstDescriptor();
		guimodel.currentDescriptor = panelDescriptor;

		final String welcomeMessage = TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION + " started on:\n" + TMUtils.getCurrentTimeString() + '\n';
		// Log GUI processing start
		gui.getLogger().log( welcomeMessage, Logger.BLUE_COLOR );
		gui.getLogger().log( "Please note that TrackMate is available through Fiji, and is based on a publication. "
				+ "If you use it successfully for your research please be so kind to cite our work:\n" );
		gui.getLogger().log( "Tinevez, JY.; Perry, N. & Schindelin, J. et al. (2017), 'TrackMate: An open and extensible platform for single-particle tracking.', "
				+ "Methods 115: 80-90, PMID 27713081.\n", Logger.GREEN_COLOR );
		gui.getLogger().log( "https://www.ncbi.nlm.nih.gov/pubmed/27713081\n", Logger.BLUE_COLOR );
		gui.getLogger().log( "https://scholar.google.com/scholar?cluster=9846627681021220605\n", Logger.BLUE_COLOR );
		// Execute about to be displayed action of the new one
		panelDescriptor.aboutToDisplayPanel();

		// Display matching panel
		gui.show( panelDescriptor );

		// Show the panel in the dialog, and execute action after display
		panelDescriptor.displayingPanel();
	}

	/**
	 * Returns the starting display settings that will be passed to any new view
	 * registered within this GUI.
	 *
	 * @param model
	 *            the model this GUI will configure; might be required by some
	 *            display settings.
	 * @return a map of display settings mappings.
	 */
	protected Map< String, Object > createDisplaySettings( final Model model )
	{
		final Map< String, Object > displaySettings = new HashMap< >();
		displaySettings.put( KEY_COLOR, DEFAULT_BCellobject_COLOR );
		displaySettings.put( KEY_HIGHLIGHT_COLOR, DEFAULT_HIGHLIGHT_COLOR );
		displaySettings.put( KEY_BCellobjectS_VISIBLE, true );
		displaySettings.put( KEY_DISPLAY_BCellobject_NAMES, false );
		displaySettings.put( KEY_BCellobject_COLORING, BCellobjectColorGenerator );
		displaySettings.put( KEY_BCellobject_RADIUS_RATIO, 1.0d );
		displaySettings.put( KEY_TRACKS_VISIBLE, true );
		displaySettings.put( KEY_TRACK_DISPLAY_MODE, DEFAULT_TRACK_DISPLAY_MODE );
		displaySettings.put( KEY_TRACK_DISPLAY_DEPTH, DEFAULT_TRACK_DISPLAY_DEPTH );
		displaySettings.put( KEY_TRACK_COLORING, trackColorGenerator );
		displaySettings.put( KEY_COLORMAP, TrackMateOptionUtils.getOptions().getPaintScale() );
		return displaySettings;
	}

	/*
	 * ACTION LISTENER
	 */

	@Override
	public void actionPerformed( final ActionEvent event )
	{
		if ( DEBUG )
			System.out.println( "[TrackMateGUIController] Caught event " + event );

		if ( event == gui.NEXT_BUTTON_PRESSED && guimodel.actionFlag )
		{

			next();

		}
		else if ( event == gui.PREVIOUS_BUTTON_PRESSED && guimodel.actionFlag )
		{

			previous();

		}
		else if ( event == gui.LOAD_BUTTON_PRESSED && guimodel.actionFlag )
		{

			/*
			 * TODO: There is actually NO load button anymore. The user load the
			 * data directly through another plugin call. We left this code here
			 * intact in case I change my mind. Removing it will actually
			 * trigger an appreciable simplification of the code, but I let it
			 * linger here a bit more. - Sep 2013
			 */

			guimodel.actionFlag = false;
			gui.jButtonNext.setText( "Resume" );
			disableButtonsAndStoreState();
			load();
			restoreButtonsState();

		}
		else if ( event == gui.SAVE_BUTTON_PRESSED && guimodel.actionFlag )
		{

			guimodel.actionFlag = false;
			gui.jButtonNext.setText( "Resume" );
			disableButtonsAndStoreState();
			new Thread( "TrackMate saving thread" )
			{
				@Override
				public void run()
				{
					save();
					gui.jButtonNext.setEnabled( true );
				}
			}.start();

		}
		else if ( ( event == gui.NEXT_BUTTON_PRESSED || event == gui.PREVIOUS_BUTTON_PRESSED || event == gui.LOAD_BUTTON_PRESSED || event == gui.SAVE_BUTTON_PRESSED ) && !guimodel.actionFlag )
		{

			// Display previous panel, but do not execute its actions
			guimodel.actionFlag = true;
			gui.show( guimodel.previousDescriptor );

			// Put back buttons
			gui.jButtonNext.setText( "Next" );
			restoreButtonsState();

		}
		else if ( event == gui.LOG_BUTTON_PRESSED )
		{

			if ( guimodel.displayingLog )
			{

				restoreButtonsState();
				gui.show( guimodel.previousDescriptor );
				guimodel.displayingLog = false;

			}
			else
			{
				disableButtonsAndStoreState();
				guimodel.previousDescriptor = guimodel.currentDescriptor;
				gui.show( logPanelDescriptor );
				gui.setLogButtonEnabled( true );
				guimodel.displayingLog = true;
			}
		}
		else if ( event == gui.DISPLAY_CONFIG_BUTTON_PRESSED )
		{
			if ( guimodel.displayingDisplayConfig )
			{

				restoreButtonsState();
				gui.show( guimodel.previousDescriptor );
				guimodel.displayingDisplayConfig = false;

			}
			else
			{
				disableButtonsAndStoreState();
				guimodel.previousDescriptor = guimodel.currentDescriptor;
				trackmate.computeBCellobjectFeatures( true );
				trackmate.computeEdgeFeatures( true );
				trackmate.computeTrackFeatures( true );
				configureViewsDescriptor.getComponent().refreshGUI();
				configureViewsDescriptor.getComponent().refreshColorFeatures();
				gui.show( configureViewsDescriptor );
				gui.setDisplayConfigButtonEnabled( true );
				guimodel.displayingDisplayConfig = true;
			}
		}
	}

	private void next()
	{

		gui.setNextButtonEnabled( false );

		// Execute leave action of the old panel
		final WizardPanelDescriptor oldDescriptor = guimodel.currentDescriptor;
		if ( oldDescriptor != null )
		{
			oldDescriptor.aboutToHidePanel();
		}

		// Find and store new one to display
		final WizardPanelDescriptor panelDescriptor = nextDescriptor( oldDescriptor );
		guimodel.currentDescriptor = panelDescriptor;

		// Re-enable the previous button, in case it was disabled
		gui.setPreviousButtonEnabled( true );

		// Execute about to be displayed action of the new one
		panelDescriptor.aboutToDisplayPanel();

		// Display matching panel
		gui.show( panelDescriptor );

		// Show the panel in the dialog, and execute action after display
		panelDescriptor.displayingPanel();
	}

	private void previous()
	{
		// Move to previous panel, but do not execute its forward-navigation
		// actions.
		final WizardPanelDescriptor olDescriptor = guimodel.currentDescriptor;
		final WizardPanelDescriptor panelDescriptor = previousDescriptor( olDescriptor );
		// Execute its backward-navigation actions.
		panelDescriptor.comingBackToPanel();
		// Do whatever we do when the panel is shown.
		panelDescriptor.displayingPanel();
		gui.show( panelDescriptor );
		guimodel.currentDescriptor = panelDescriptor;

		// Check if the new panel has a next panel. If not, disable the next
		// button
		if ( null == previousDescriptor( panelDescriptor ) )
		{
			gui.setPreviousButtonEnabled( false );
		}

		// Re-enable the previous button, in case it was disabled
		gui.setNextButtonEnabled( true );
	}

	private void load()
	{
		// Store current state
		guimodel.previousDescriptor = guimodel.currentDescriptor;

		// Move to load state and show log panel
		loadDescriptor.aboutToDisplayPanel();
		gui.show( loadDescriptor );

		// Instantiate GuiReader, ask for file, and load it in memory
		loadDescriptor.displayingPanel();
	}

	private void save()
	{
		// Store current state
		guimodel.previousDescriptor = guimodel.currentDescriptor;

		/*
		 * Special case: if we are currently configuring a detector or a
		 * tracker, stores the settings currently displayed in TrackMate.
		 */

		if (guimodel.currentDescriptor.equals( trackerConfigurationDescriptor )
				)
		{
			// This will flush currently displayed settings to TrackMate.
			guimodel.currentDescriptor.aboutToHidePanel();
		}


		// Move to save state and execute
		saveDescriptor.aboutToDisplayPanel();

		gui.show( saveDescriptor );
		gui.getLogger().log( TMUtils.getCurrentTimeString() + '\n', Logger.BLUE_COLOR );
		saveDescriptor.displayingPanel();
	}

	/**
	 * Disable the 4 bottom buttons and memorize their state to that they can be
	 * restored when calling {@link #restoreButtonsState()}.
	 */
	public void disableButtonsAndStoreState()
	{
		guimodel.loadButtonState = gui.jButtonLoad.isEnabled();
		guimodel.saveButtonState = gui.jButtonSave.isEnabled();
		guimodel.previousButtonState = gui.jButtonPrevious.isEnabled();
		guimodel.nextButtonState = gui.jButtonNext.isEnabled();
		guimodel.displayConfigButtonState = gui.jButtonDisplayConfig.isEnabled();
		guimodel.logButtonState = gui.jButtonLog.isEnabled();
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				gui.jButtonLoad.setEnabled( false );
				gui.jButtonNext.setEnabled( false );
				gui.jButtonPrevious.setEnabled( false );
				gui.jButtonSave.setEnabled( false );
				gui.jButtonLog.setEnabled( false );
				gui.jButtonDisplayConfig.setEnabled( false );
			}
		} );
	}

	/**
	 * Restore the button state saved when calling
	 * {@link #disableButtonsAndStoreState()}. Do nothing if
	 * {@link #disableButtonsAndStoreState()} was not called before.
	 */
	public void restoreButtonsState()
	{
		gui.setLoadButtonEnabled( guimodel.loadButtonState );
		gui.setSaveButtonEnabled( guimodel.saveButtonState );
		gui.setPreviousButtonEnabled( guimodel.previousButtonState );
		gui.setNextButtonEnabled( guimodel.nextButtonState );
		gui.setDisplayConfigButtonEnabled( guimodel.displayConfigButtonState );
		gui.setLogButtonEnabled( guimodel.logButtonState );
	}

	private void launchTrackScheme()
	{
		final JButton button = configureViewsDescriptor.getComponent().getTrackSchemeButton();
		button.setEnabled( false );
		new Thread( "Launching TrackScheme thread" )
		{
			@Override
			public void run()
			{
				final TrackScheme trackscheme = new TrackScheme( trackmate.getParent(), trackmate.getModel(), selectionModel );
				final BCellobjectImageUpdater thumbnailUpdater = new BCellobjectImageUpdater( trackmate.getSettings() );
				trackscheme.setBCellobjectImageUpdater( thumbnailUpdater );
				for ( final String settingKey : guimodel.getDisplaySettings().keySet() )
				{
					trackscheme.setDisplaySettings( settingKey, guimodel.getDisplaySettings().get( settingKey ) );
				}
				trackscheme.render();
				guimodel.addView( trackscheme );
				// De-register
				trackscheme.getGUI().addWindowListener( new WindowAdapter()
				{
					@Override
					public void windowClosing( final WindowEvent e )
					{
						guimodel.removeView( trackscheme );
					}
				} );

				button.setEnabled( true );
			}
		}.start();
	}

	private void launchDoAnalysis( final boolean showAllBCellobjectStats )
	{
		final JButton button = configureViewsDescriptor.getComponent().getDoAnalysisButton();
		button.setEnabled( false );
		if ( guimodel.displayingLog == false && guimodel.displayingDisplayConfig == false )
			disableButtonsAndStoreState();
		gui.show( logPanelDescriptor );
		new Thread( "TrackMate export analysis to IJ thread." )
		{
			@Override
			public void run()
			{
				try
				{
					AbstractTMAction action;
					if ( showAllBCellobjectStats )
						action = new ExportAllBCellobjectsStatsAction( selectionModel );
					else
						action = new ExportStatsToIJAction( selectionModel );

					action.execute( trackmate );

				}
				finally
				{
					gui.show( configureViewsDescriptor );
					button.setEnabled( true );
					if ( guimodel.displayingLog == false && guimodel.displayingDisplayConfig == false )
						restoreButtonsState();
				}
			}
		}.start();
	}

}
