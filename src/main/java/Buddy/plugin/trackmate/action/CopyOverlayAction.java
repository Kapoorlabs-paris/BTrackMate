package Buddy.plugin.trackmate.action;

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
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.scijava.plugin.Plugin;

import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.TrackMateOptionUtils;
import Buddy.plugin.trackmate.features.edges.EdgeVelocityAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackIndexAnalyzer;
import Buddy.plugin.trackmate.gui.DisplaySettingsEvent;
import Buddy.plugin.trackmate.gui.DisplaySettingsListener;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.TrackMateGUIModel;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import Buddy.plugin.trackmate.gui.panels.ConfigureViewsPanel;
import Buddy.plugin.trackmate.gui.panels.components.ImagePlusChooser;
import Buddy.plugin.trackmate.visualization.BCellobjectColorGenerator;
import Buddy.plugin.trackmate.visualization.BCellobjectColorGeneratorPerTrackFeature;
import Buddy.plugin.trackmate.visualization.ManualBCellobjectColorGenerator;
import Buddy.plugin.trackmate.visualization.ManualEdgeColorGenerator;
import Buddy.plugin.trackmate.visualization.PerEdgeFeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.PerTrackFeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import Buddy.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import Buddy.plugin.trackmate.visualization.threedviewer.BCellobjectDisplayer3D;
import Buddy.plugin.trackmate.visualization.trackscheme.BCellobjectImageUpdater;
import Buddy.plugin.trackmate.visualization.trackscheme.TrackScheme;
import ij.ImagePlus;

public class CopyOverlayAction extends AbstractTMAction
{

	public static final ImageIcon ICON = new ImageIcon( TrackMateWizard.class.getResource( "images/page_copy.png" ) );

	public static final String NAME = "Copy overlay to...";

	public static final String KEY = "COPY_OVERLAY";

	public static final String INFO_TEXT = "<html>"
			+ "This action copies the overlay (BCellobjects and tracks) to a new existing ImageJ window <br> "
			+ "or to a new 3D viewer window. This can be useful to have the tracks and BCellobjects <br> "
			+ "displayed on a modified image. "
			+ "<p>"
			+ "The new view will be independent, and will have its own control panel.<br> "
			+ "</html>";

	/**
	 * The {@link ConfigureViewsPanel} created as a new GUI.
	 */
	private ConfigureViewsPanel panel;

	/**
	 * The new GUI model storing views and display settings.
	 */
	private TrackMateGUIModel guimodel;

	/**
	 * The new selection model for the GUI.
	 */
	private SelectionModel selectionModel;

	/**
	 * The <b>common</b> TrackMate instance given by the mother GUI.
	 */
	private TrackMate trackmate;

	@Override
	public void execute( final TrackMate tm )
	{
		this.trackmate = tm;
		final ImagePlusChooser impChooser = new ImagePlusChooser( "Copy overlay", "Copy overlay to:", "New 3D viewer" );
		impChooser.setLocationRelativeTo( null );
		impChooser.setVisible( true );
		final ActionListener copyOverlayListener = new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( e == impChooser.OK_BUTTON_PUSHED )
				{
					new Thread( "TrackMate copying thread" )
					{

						@Override
						public void run()
						{
							selectionModel = new SelectionModel( tm.getModel() );
							// Instantiate displayer
							final ImagePlus dest = impChooser.getSelectedImagePlus();
							impChooser.setVisible( false );
							TrackMateModelView newDisplayer;
							String title;
							
								logger.log( "Copying overlay to " + dest.getShortTitle() + "\n" );
								newDisplayer = new HyperStackDisplayer(tm.getParent(), tm.getModel(), selectionModel, dest );
								title = dest.getShortTitle() + " ctrl";
							
							newDisplayer.render();

							panel = new ConfigureViewsPanel( tm.getModel() );

							/*
							 * Deal with display settings listener.
							 */

							guimodel = new TrackMateGUIModel();
							final BCellobjectColorGenerator BCellobjectColorGenerator = new BCellobjectColorGenerator( tm.getModel() );
							final PerTrackFeatureColorGenerator trackColorGenerator = new PerTrackFeatureColorGenerator( tm.getModel(), TrackIndexAnalyzer.TRACK_INDEX );
							final PerEdgeFeatureColorGenerator edgeColorGenerator = new PerEdgeFeatureColorGenerator( tm.getModel(), EdgeVelocityAnalyzer.VELOCITY );
							final ManualEdgeColorGenerator manualEdgeColorGenerator = new ManualEdgeColorGenerator( tm.getModel() );
							final ManualBCellobjectColorGenerator manualBCellobjectColorGenerator = new ManualBCellobjectColorGenerator();
							final BCellobjectColorGeneratorPerTrackFeature BCellobjectColorGeneratorPerTrackFeature = new BCellobjectColorGeneratorPerTrackFeature( tm.getModel(), TrackIndexAnalyzer.TRACK_INDEX );

							panel.setBCellobjectColorGenerator( BCellobjectColorGenerator );
							panel.setBCellobjectColorGeneratorPerTrackFeature( BCellobjectColorGeneratorPerTrackFeature );
							panel.setEdgeColorGenerator( edgeColorGenerator );
							panel.setTrackColorGenerator( trackColorGenerator );
							panel.setManualEdgeColorGenerator( manualEdgeColorGenerator );
							panel.setManualBCellobjectColorGenerator( manualBCellobjectColorGenerator );

							final Map< String, Object > displaySettings = new HashMap<>();
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
							guimodel.setDisplaySettings( displaySettings );

							guimodel.addView( newDisplayer );
							final DisplaySettingsListener displaySettingsListener = new DisplaySettingsListener()
							{
								@Override
								public void displaySettingsChanged( final DisplaySettingsEvent event )
								{
									guimodel.getDisplaySettings().put( event.getKey(), event.getNewValue() );
									for ( final TrackMateModelView view : guimodel.getViews() )
									{
										view.setDisplaySettings( event.getKey(), event.getNewValue() );
										view.refresh();
									}
								}
							};
							panel.addDisplaySettingsChangeListener( displaySettingsListener );
							panel.refreshGUI();

							/*
							 * Deal with TrackScheme and analysis buttons.
							 */

							panel.addActionListener( new ActionListener()
							{
								@Override
								public void actionPerformed( final ActionEvent event )
								{
									if ( event == panel.TRACK_SCHEME_BUTTON_PRESSED )
									{
										launchTrackScheme();

									}
									else if ( event == panel.DO_ANALYSIS_BUTTON_PRESSED )
									{
										launchDoAnalysis();

									}
									else
									{
										System.out.println( "[CopyOverlayAction] Caught unknown event: " + event );
									}
								}
							} );

							/*
							 * Render it.
							 */

							final JFrame newFrame = new JFrame();
							newFrame.getContentPane().add( panel );
							newFrame.pack();
							newFrame.setTitle( title );
							newFrame.setSize( 300, 470 );
							newFrame.setLocationRelativeTo( null );
							newFrame.setVisible( true );
							logger.log( "Done.\n" );

						}
					}.start();
				}
				else
				{
					impChooser.removeActionListener( this );
					impChooser.setVisible( false );
				}
			}
		};
		impChooser.addActionListener( copyOverlayListener );
	}

	private void launchTrackScheme()
	{
		final JButton button = panel.getTrackSchemeButton();
		button.setEnabled( false );
		new Thread( "Launching TrackScheme thread" )
		{
			@Override
			public void run()
			{
				final TrackScheme trackscheme = new TrackScheme(trackmate.getParent(), trackmate.getModel(), selectionModel );
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

	private void launchDoAnalysis()
	{
		final JButton button = panel.getDoAnalysisButton();
		button.setEnabled( false );
		new Thread( "TrackMate export analysis to IJ thread." )
		{
			@Override
			public void run()
			{
				try
				{
					final ExportStatsToIJAction action = new ExportStatsToIJAction( selectionModel );
					action.execute( trackmate );
				}
				finally
				{
					button.setEnabled( true );
				}
			}
		}.start();
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
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new CopyOverlayAction();
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

	}
}
