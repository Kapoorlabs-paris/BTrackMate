package fiji.plugin.btrackmate;

import static fiji.plugin.btrackmate.gui.Icons.TRACKMATE_ICON;
import static fiji.plugin.btrackmate.gui.Icons.TRACKMATE_ICON;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.btrackmate.gui.GuiUtils;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettingsIO;
import fiji.plugin.btrackmate.gui.wizard.BTrackMateWizardSequence;
import fiji.plugin.btrackmate.gui.wizard.WizardSequence;
import fiji.plugin.btrackmate.visualization.TrackMateModelView;
import fiji.plugin.btrackmate.visualization.hyperstack.HyperStackDisplayer;
import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.SelectionModel;
import fiji.plugin.btrackmate.Settings;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.TrackMatePlugIn;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class TrackMatePlugIn implements PlugIn
{

	
	
	public static Settings settings;
	public static Model model;
	public static TrackMate btrackmate;
	public static JFrame globalframe; 
	public static ImagePlus globalimp;
	public static DisplaySettings displaySettings;
	@Override
	public void run( final String imagePath )
	{
		GuiUtils.setSystemLookAndFeel();
		
		if ( imagePath != null && imagePath.length() > 0 )
		{
			globalimp = IJ.openImage( imagePath );
			if ( null == globalimp.getOriginalFileInfo() )
			{
				IJ.error( TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION, "Could not load image with path " + imagePath + "." );
				return;
			}
		}
		else
		{
			globalimp = WindowManager.getCurrentImage();
			if ( null == globalimp )
			{
				IJ.error( TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION,
						"Please open an image before running TrackMate." );
				return;
			}
			else if ( globalimp.getType() == ImagePlus.COLOR_RGB )
			{
				IJ.error( TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION,
						"TrackMate does not work on RGB images." );
				return;
			}
		}

		globalimp.setOpenAsHyperStack( true );
		globalimp.setDisplayMode( IJ.COMPOSITE );
		if ( !globalimp.isVisible() )
			globalimp.show();

		GuiUtils.userCheckImpDimensions( globalimp );

		// Main objects.
		
		final SelectionModel selectionModel = new SelectionModel( model );
		displaySettings = createDisplaySettings();
		 settings = createSettings( globalimp );
		 model = createModel( globalimp );
		 btrackmate = createTrackMate( model, settings );
		// Main view.
				
		 final TrackMateModelView displayer = new HyperStackDisplayer( model, selectionModel, globalimp, createDisplaySettings() );
		 displayer.render();
		// Wizard.
		 final WizardSequence sequence = createSequence( btrackmate, selectionModel, displaySettings, false );
		 globalframe = sequence.run( "BTrackMate");
		 globalframe.setIconImage( TRACKMATE_ICON.getImage() );
		GuiUtils.positionWindow( globalframe, globalimp.getWindow() );
		globalframe.setVisible( true );
		//Call pack on the JFrame to have panels sized with preferred size
		globalframe.pack();
	}
	
	
	/**
	 * Hook for subclassers: <br>
	 * Will create and position the sequence that will be played by the wizard
	 * launched by this plugin.
	 * 
	 * @param trackmate
	 * @param selectionModel
	 * @param displaySettings
	 * @return
	 */
	protected WizardSequence createSequence( final TrackMate btrackmate, final SelectionModel selectionModel, final DisplaySettings displaySettings, final Boolean secondrun)
	{
		return new BTrackMateWizardSequence( btrackmate, selectionModel, displaySettings, secondrun);
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the {@link Model} instance that will be used to store data in the
	 * {@link TrackMate} instance.
	 * 
	 * @param imp
	 *
	 * @return a new {@link Model} instance.
	 */
	protected Model createModel( final ImagePlus imp )
	{
		final Model model = new Model();
		model.setPhysicalUnits(
				imp.getCalibration().getUnit(),
				imp.getCalibration().getTimeUnit() );
		return model;
	}
	public static Model localcreateModel( final ImagePlus imp )
	{
		final Model model = new Model();
		model.setPhysicalUnits(
				imp.getCalibration().getUnit(),
				imp.getCalibration().getTimeUnit() );
		return model;
	}
	
	public static void ModelUpdate(final Logger logger,final ImagePlus imp,  final ImagePlus localimp) 
	
	{
		
		
		 
		 
		 if (TrackMate.CsvSpots!=null) {
		        model.setSpots(TrackMate.CsvSpots, true);
				}
		        model.setLogger( logger );
		        
		
		 
		 
		 localimp.setOpenAsHyperStack( true );
		 localimp.setDisplayMode( IJ.COMPOSITE );
			if ( !localimp.isVisible() )
				localimp.show();

			GuiUtils.userCheckImpDimensions( localimp );

			// Main objects.
			
			final SelectionModel selectionModel = new SelectionModel( model );
			// Main view.
					
			 final TrackMateModelView displayer = new HyperStackDisplayer( model, selectionModel, localimp, displaySettings );
			 displayer.render();
			 final WizardSequence sequence = PseudocreateSequence( btrackmate, selectionModel, displaySettings, true );
			 final JFrame frame = sequence.run( "BTrackMate");
			frame.setIconImage( TRACKMATE_ICON.getImage() );
			GuiUtils.positionWindow( frame, localimp.getWindow() );
			frame.setVisible( true );
			//Call pack on the JFrame to have panels sized with preferred size
			frame.pack();
			globalframe.dispose();
			globalimp.close();
		 
        
		
	}
	
	
	
	protected static DisplaySettings PseudocreateDisplaySettings()
	{
		return DisplaySettingsIO.readUserDefault().copy( "CurrentDisplaySettings" );
	}
	protected  static WizardSequence PseudocreateSequence( final TrackMate btrackmate, final SelectionModel selectionModel, final DisplaySettings displaySettings, final Boolean secondrun)
	{
		return new BTrackMateWizardSequence( btrackmate, selectionModel, displaySettings, secondrun);
	}
	/**
	 * Hook for subclassers: <br>
	 * Creates the {@link Settings} instance that will be used to tune the
	 * {@link TrackMate} instance. It is initialized by default with values
	 * taken from the current {@link ImagePlus}.
	 *
	 * @param imp
	 *            the {@link ImagePlus} to operate on.
	 * @return a new {@link Settings} instance.
	 */
	protected Settings createSettings( final ImagePlus imp )
	{
		final Settings ls = new Settings();
		ls.setFrom( imp );
		ls.addAllAnalyzers();
		return ls;
	}
	
	public static Settings localcreateSettings( final ImagePlus imp )
	{
		final Settings ls = new Settings();
		ls.setFrom( imp );
		ls.addAllAnalyzers();
		return ls;
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the TrackMate instance that will be controlled in the GUI.
	 *
	 * @return a new {@link TrackMate} instance.
	 */
	protected TrackMate createTrackMate( final Model model, final Settings settings )
	{
		/*
		 * Since we are now sure that we will be working on this model with this
		 * settings, we need to pass to the model the units from the settings.
		 */
		final String spaceUnits = settings.imp.getCalibration().getXUnit();
		final String timeUnits = settings.imp.getCalibration().getTimeUnit();
		model.setPhysicalUnits( spaceUnits, timeUnits );

		return new TrackMate( model, settings );
	}
	
	public static TrackMate localcreateTrackMate( final Model model, final Settings settings )
	{
		/*
		 * Since we are now sure that we will be working on this model with this
		 * settings, we need to pass to the model the units from the settings.
		 */
		final String spaceUnits = settings.imp.getCalibration().getXUnit();
		final String timeUnits = settings.imp.getCalibration().getTimeUnit();
		model.setPhysicalUnits( spaceUnits, timeUnits );

		return new TrackMate( model, settings );
	}

	protected DisplaySettings createDisplaySettings()
	{
		return DisplaySettingsIO.readUserDefault().copy( "CurrentDisplaySettings" );
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );
//		new TrackMatePlugIn().run( "samples/Stack.tif" );
//		new TrackMatePlugIn().run( "samples/Merged.tif" );
		new TrackMatePlugIn().run("/Users/aimachine/Downloads/CellTracking/SEG-1.tif");
//		new TrackMatePlugIn().run( "samples/Mask.tif" );
//		new TrackMatePlugIn().run( "samples/FakeTracks.tif" );
	}
}
