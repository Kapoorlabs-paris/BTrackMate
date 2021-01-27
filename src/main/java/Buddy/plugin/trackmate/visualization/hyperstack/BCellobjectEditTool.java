package Buddy.plugin.trackmate.visualization.hyperstack;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jgrapht.graph.DefaultWeightedEdge;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.util.TMUtils;
import budDetector.BCellobject;
import fiji.tool.AbstractTool;
import fiji.tool.ToolWithOptions;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.FreehandRoi;
import ij.gui.ImageCanvas;
import ij.gui.Toolbar;

public class BCellobjectEditTool extends AbstractTool implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, ToolWithOptions
{

	private static final boolean DEBUG = false;

	private static final double COARSE_STEP = 2;

	private static final double FINE_STEP = 0.2f;

	private static final String TOOL_NAME = "BCellobject edit tool";

	private static final String TOOL_ICON = "CeacD70Cd8bD80"
			+ "D71Cc69D81CfefD91"
			+ "CdbcD72Cb9bD82"
			+ "Cd9bD73Cc8aD83CfefD93"
			+ "CdddD54CbaaD64Cb69D74Cb59D84Cb9aD94CdddDa4"
			+ "CfefD25Cd9bD35Cb8aD45CaaaD55CcccD65CfdeL7585CdccD95CaaaDa5Cb8aDb5Cd7aDc5CfceDd5"
			+ "CfeeD26Cc69D36Cc8aD46CdacDb6Cb59Dc6CecdDd6"
			+ "Cb9aD37CdcdD47CeeeDb7Ca89Dc7"
			+ "CfefD28Cc7aD38Cd9cD48CecdDb8Cb79Dc8CfdeDd8"
			+ "CcabD29Cb59D39Cb69D49CedeD59CeacDb9Cc59Dc9CebdDd9"
			+ "CfdeD0aCc7aD1aCb8aD2aCedeD3aCcbcD4aCb7aD5aCe9cD6aCeeeDbaCa89DcaCfefDda"
			+ "CebdD0bCc59D1bCebdD2bCfefD4bCc7aL5b6bCeceDbbCb79DcbCfdeDdb"
			+ "CfeeD0cCa89D1cCfefD2cCcabL5c6cCc9bDbcCc59DccCdabDdc"
			+ "CedeD0dCb79D1dCedeD2dCc9bL5d6dCecdD9dCc8aDadCb9aDbdCdbcDcdCb8aDddCd8bDedCfceDfd"
			+ "CebdD0eCc59D1eCebdD2eCfeeD4eCc7aD5eCc6aD6eCfeeD7eCd9bD9eCc59DaeCfdeDbeCebdDdeCc59DeeCeacDfe"
			+ "CfefD0fCdbcD1fCdddD4fCdcdL5f6fCdddD7fCfdeD9fCdbdDafCebdDefCfefDff";

	/**
	 * Fall back default radius when the settings does not give a default radius
	 * to use.
	 */
	private static final double FALL_BACK_RADIUS = 5;

	/** The singleton instance. */
	private static BCellobjectEditTool instance;

	/** Stores the edited BCellobject in each {@link ImagePlus}. */
	private final HashMap< ImagePlus, BCellobject > editedBCellobjects = new HashMap< >();

	/** Stores the view possible attached to each {@link ImagePlus}. */
	HashMap< ImagePlus, HyperStackDisplayer > displayers = new HashMap< >();

	/** Stores the config panel attached to each {@link ImagePlus}. */
//	private final HashMap< ImagePlus, FloatingDisplayConfigFrame > configFrames = new HashMap<>();

	/** The radius of the previously edited BCellobject. */
	private Double previousRadius = null;

	private BCellobject quickEditedBCellobject;

	/** Flag for the auto-linking mode. */
	private boolean autolinkingmode = false;

	BCellobjectEditToolParams params = new BCellobjectEditToolParams();

	private Logger logger = Logger.IJTOOLBAR_LOGGER;

	private BCellobjectEditToolConfigPanel configPanel;

	/**
	 * The last {@link ImagePlus} on which an action happened.
	 */
	ImagePlus imp;

	private FreehandRoi roiedit;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Singleton
	 */
	private BCellobjectEditTool()
	{}

	/**
	 * Return the singleton instance for this tool. If it was not previously
	 * instantiated, this calls instantiates it.
	 */
	public static BCellobjectEditTool getInstance()
	{
		if ( null == instance )
		{
			instance = new BCellobjectEditTool();
			if ( DEBUG )
				System.out.println( "[BCellobjectEditTool] Instantiating: " + instance );
		}
		if ( DEBUG )
			System.out.println( "[BCellobjectEditTool] Returning instance: " + instance );
		return instance;
	}

	/**
	 * Return true if the tool is currently present in ImageJ toolbar.
	 */
	public static boolean isLaunched()
	{
		final Toolbar toolbar = Toolbar.getInstance();
		if ( null != toolbar && toolbar.getToolId( TOOL_NAME ) >= 0 )
			return true;
		return false;
	}

	/*
	 * METHODS
	 */

	@Override
	public String getToolName()
	{
		return TOOL_NAME;
	}

	@Override
	public String getToolIcon()
	{
		return TOOL_ICON;
	}

	/**
	 * Overridden so that we can keep track of the last ImagePlus actions are
	 * taken on. Very much like ImageJ.
	 */
	@Override
	public ImagePlus getImagePlus( final ComponentEvent e )
	{
		imp = super.getImagePlus( e );
		return imp;
	}

	/**
	 * Register the given {@link HyperStackDisplayer}. If this method id not
	 * called, the tool will not respond.
	 */
	public void register( final ImagePlus lImp, final HyperStackDisplayer displayer )
	{
		if ( DEBUG )
			System.out.println( "[BCellobjectEditTool] Currently registered: " + displayers );

		if ( displayers.containsKey( lImp ) )
		{
			unregisterTool( lImp );
			if ( DEBUG )
				System.out.println( "[BCellobjectEditTool] De-registering " + lImp + " as tool listener." );
		}

		displayers.put( lImp, displayer );
		if ( DEBUG )
		{
			System.out.println( "[BCellobjectEditTool] Registering " + lImp + " and " + displayer + "." + " Currently registered: " + displayers );
		}
	}

	/*
	 * MOUSE AND MOUSE MOTION
	 */

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final ImagePlus lImp = getImagePlus( e );
		final HyperStackDisplayer displayer = displayers.get( lImp );
		
				System.out.println("Non Editable");
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		if ( null != roiedit )
		{
			new Thread( "BCellobjectEditTool roiedit processing" )
			{
				@Override
				public void run()
				{
					roiedit.mouseReleased( e );
					final ImagePlus lImp = getImagePlus( e );
					final HyperStackDisplayer displayer = displayers.get( lImp );
					final int frame = displayer.imp.getFrame() - 1;
					final Model model = displayer.getModel();
					final SelectionModel selectionModel = displayer.getSelectionModel();

					final Iterator< BCellobject > it;
					if ( IJ.shiftKeyDown() )
						it = model.getBCellobjects().iterator( true );
					else
						it = model.getBCellobjects().iterator( frame );

					final Collection< BCellobject > added = new ArrayList< >();
					final double calibration[] = TMUtils.getSpatialCalibration( lImp );

					while ( it.hasNext() )
					{
						final BCellobject BCellobject = it.next();
						final double x = BCellobject.getFeature( BCellobject.POSITION_X );
						final double y = BCellobject.getFeature( BCellobject.POSITION_Y );
						// In pixel units
						final int xp = ( int ) ( x / calibration[ 0 ] + 0.5f );
						final int yp = ( int ) ( y / calibration[ 1 ] + 0.5f );

						if ( null != roiedit && roiedit.contains( xp, yp ) )
						{
							added.add( BCellobject );
						}
					}

					if ( !added.isEmpty() )
					{
						selectionModel.addBCellobjectToSelection( added );
						if ( added.size() == 1 )
							logger.log( "Added one BCellobject to selection.\n" );
						else
							logger.log( "Added " + added.size() + " BCellobjects to selection.\n" );
					}
					roiedit = null;
				}
			}.start();
		}
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		final ImagePlus lImp = getImagePlus( e );
		final double[] calibration = TMUtils.getSpatialCalibration( lImp );
		final HyperStackDisplayer displayer = displayers.get( lImp );
		if ( null == displayer )
			return;
		final BCellobject editedBCellobject = editedBCellobjects.get( lImp );
		if ( null != editedBCellobject )
		{

			final Point mouseLocation = e.getPoint();
			final ImageCanvas canvas = getImageCanvas( e );
			final double x = ( -0.5 + canvas.offScreenXD( mouseLocation.x ) ) * calibration[ 0 ];
			final double y = ( -0.5 + canvas.offScreenYD( mouseLocation.y ) ) * calibration[ 1 ];
			final double z = ( lImp.getSlice() - 1 ) * calibration[ 2 ];
			editedBCellobject.putFeature( BCellobject.POSITION_X, x );
			editedBCellobject.putFeature( BCellobject.POSITION_Y, y );
			editedBCellobject.putFeature( BCellobject.POSITION_Z, z );
			displayer.imp.updateAndDraw();
			updateStatusBar( editedBCellobject, lImp.getCalibration().getUnits() );
		}
		else
		{
			if ( null == roiedit )
			{
				if ( !IJ.spaceBarDown() )
				{
					roiedit = new FreehandRoi( e.getX(), e.getY(), lImp )
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected void handleMouseUp( final int screenX, final int screenY )
						{
							type = FREEROI;
							super.handleMouseUp( screenX, screenY );
						}
					};
					lImp.setRoi( roiedit );
				}
			}
			else
			{
				roiedit.mouseDragged( e );
			}
		}
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		if ( quickEditedBCellobject == null )
			return;
		final ImagePlus lImp = getImagePlus( e );
		final double[] calibration = TMUtils.getSpatialCalibration( lImp );
		final HyperStackDisplayer displayer = displayers.get( lImp );
		if ( null == displayer )
			return;
		final BCellobject editedBCellobject = editedBCellobjects.get( lImp );
		if ( null != editedBCellobject )
			return;

		final Point mouseLocation = e.getPoint();
		final ImageCanvas canvas = getImageCanvas( e );
		final double x = ( -0.5 + canvas.offScreenXD( mouseLocation.x ) ) * calibration[ 0 ];
		final double y = ( -0.5 + canvas.offScreenYD( mouseLocation.y ) ) * calibration[ 1 ];
		final double z = ( lImp.getSlice() - 1 ) * calibration[ 2 ];

		quickEditedBCellobject.putFeature( BCellobject.POSITION_X, x );
		quickEditedBCellobject.putFeature( BCellobject.POSITION_Y, y );
		quickEditedBCellobject.putFeature( BCellobject.POSITION_Z, z );
		displayer.imp.updateAndDraw();

	}

	/*
	 * MOUSEWHEEL
	 */

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		final ImagePlus lImp = getImagePlus( e );
		final HyperStackDisplayer displayer = displayers.get( lImp );
		if ( null == displayer )
			return;
		final BCellobject editedBCellobject = editedBCellobjects.get( lImp );
		if ( null == editedBCellobject || !e.isAltDown() )
			return;
		double radius = editedBCellobject.getFeature( BCellobject.Size );
		final double dx = lImp.getCalibration().pixelWidth;
		if ( e.isShiftDown() )
			radius += e.getWheelRotation() * dx * COARSE_STEP;
		else
			radius += e.getWheelRotation() * dx * FINE_STEP;

		if ( radius < dx )
		{
			e.consume();
			return;
		}

		editedBCellobject.putFeature( BCellobject.Size, radius );
		displayer.imp.updateAndDraw();
		e.consume();
		updateStatusBar( editedBCellobject, lImp.getCalibration().getUnits() );
	}

	/*
	 * KEYLISTENER
	 */

	@Override
	public void keyTyped( final KeyEvent e )
	{}

	@Override
	public void keyPressed( final KeyEvent e )
	{

		if ( DEBUG )
			System.out.println( "[BCellobjectEditTool] keyPressed: " + e.getKeyChar() );

		final ImagePlus lImp = getImagePlus( e );
		if ( lImp == null )
			return;
		final HyperStackDisplayer displayer = displayers.get( lImp );
		if ( null == displayer )
			return;

		final Model model = displayer.getModel();
		final SelectionModel selectionModel = displayer.getSelectionModel();
		BCellobject editedBCellobject = editedBCellobjects.get( lImp );
		final ImageCanvas canvas = getImageCanvas( e );

		final int keycode = e.getKeyCode();

		switch ( keycode )
		{

//		case KeyEvent.VK_R:
//		{
//			FloatingDisplayConfigFrame configFrame = configFrames.get( imp );
//			if ( null == configFrame )
//			{
//				final String title = displayer.getImp().getShortTitle();
//				configFrame = new FloatingDisplayConfigFrame( model, displayer, title );
//				configFrame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
//				configFrame.setLocationRelativeTo( displayer.getImp().getWindow() );
//				configFrames.put( imp, configFrame );
//			}
//
//			configFrame.setVisible( !configFrame.isVisible() );
//			break;
//		}

		// Delete currently edited BCellobject
		case KeyEvent.VK_DELETE:
		{
			if ( null == editedBCellobject )
			{
				final ArrayList< BCellobject > BCellobjectSelection = new ArrayList< >( selectionModel.getBCellobjectSelection() );
				final ArrayList< DefaultWeightedEdge > edgeSelection = new ArrayList< >( selectionModel.getEdgeSelection() );
				model.beginUpdate();
				try
				{
					selectionModel.clearSelection();
					for ( final DefaultWeightedEdge edge : edgeSelection )
					{
						model.removeEdge( edge );
						logger.log( "Removed edge " + edge + ".\n" );
					}
					for ( final BCellobject BCellobject : BCellobjectSelection )
					{
						model.removeBCellobject( BCellobject );
						logger.log( "Removed BCellobject " + BCellobject + ".\n" );
					}
				}
				finally
				{
					model.endUpdate();
				}

			}
			else
			{
				model.beginUpdate();
				try
				{
					model.removeBCellobject( editedBCellobject );
					logger.log( "Removed " + editedBCellobject + ".\n" );
				}
				finally
				{
					model.endUpdate();
				}
				editedBCellobject = null;
				editedBCellobjects.put( lImp, null );
			}
			lImp.updateAndDraw();
			e.consume();
			break;
		}

		

		// Copy BCellobjects from previous frame
		case KeyEvent.VK_V:
		{
			if ( e.isShiftDown() )
			{

				final int currentFrame = lImp.getFrame() - 1;
				if ( currentFrame > 0 )
				{

					final BCellobjectCollection BCellobjects = model.getBCellobjects();
					if ( BCellobjects.getNBCellobjects( currentFrame - 1 ) == 0 )
					{
						e.consume();
						break;
					}
					final HashSet< BCellobject > copiedBCellobjects = new HashSet< >( BCellobjects.getNBCellobjects( currentFrame - 1 ) );
					final HashSet< String > featuresKey = new HashSet< >( BCellobjects.iterator( currentFrame - 1 ).next().getFeatures().keySet() );
					featuresKey.remove( BCellobject.POSITION_T ); // Deal with time
															// separately
					double dt = lImp.getCalibration().frameInterval;
					if ( dt == 0 )
					{
						dt = 1;
					}

					for ( final Iterator< BCellobject > it = BCellobjects.iterator( currentFrame - 1 ); it.hasNext(); )
					{
						final BCellobject BCellobject = it.next();
						final BCellobject newBCellobject = new BCellobject( BCellobject );
						// Deal with features
						Double val;
						for ( final String key : featuresKey )
						{
							val = BCellobject.getFeature( key );
							if ( val == null )
							{
								continue;
							}
							newBCellobject.putFeature( key, val );
						}
						newBCellobject.putFeature( BCellobject.POSITION_T, BCellobject.getFeature( BCellobject.POSITION_T ) + dt );
						copiedBCellobjects.add( newBCellobject );
					}

					model.beginUpdate();
					try
					{
						// Remove old ones
						final HashSet< BCellobject > toRemove = new HashSet< >(  );
						for ( final Iterator< BCellobject > it = BCellobjects.iterator( currentFrame ); it.hasNext(); )
						{
							toRemove.add( it.next() );
						}
						for ( final BCellobject BCellobject : toRemove )
						{
							model.removeBCellobject( BCellobject );
						}

						// Add new ones
						for ( final BCellobject BCellobject : copiedBCellobjects )
						{
							model.addBCellobjectTo( BCellobject, currentFrame );
						}
					}
					finally
					{
						model.endUpdate();
						lImp.updateAndDraw();
						logger.log( "Removed BCellobjects of frame " + currentFrame + ".\n" );
						logger.log( "Copied BCellobjects of frame " + ( currentFrame - 1 ) + " to frame " + currentFrame + ".\n" );
					}
				}

				e.consume();
			}
			break;
		}

		case KeyEvent.VK_L:
		{

			if ( e.isShiftDown() )
			{
				/*
				 * Toggle auto-linking mode
				 */
				autolinkingmode = !autolinkingmode;
				logger.log( "Toggled auto-linking mode " + ( autolinkingmode ? "on.\n" : "off.\n" ) );

			}
			else
			{
				/*
				 * Toggle a link between two BCellobjects.
				 */
				final Set< BCellobject > selectedBCellobjects = selectionModel.getBCellobjectSelection();
				if ( selectedBCellobjects.size() == 2 )
				{
					final Iterator< BCellobject > it = selectedBCellobjects.iterator();
					final BCellobject source = it.next();
					final BCellobject target = it.next();

					if ( model.getTrackModel().containsEdge( source, target ) )
					{
						/*
						 * Remove it
						 */
						model.beginUpdate();
						try
						{
							model.removeEdge( source, target );
							logger.log( "Removed edge between " + source + " and " + target + ".\n" );
						}
						finally
						{
							model.endUpdate();
						}

					}
					else
					{
						/*
						 * Create a new link
						 */
						final int ts = source.getFeature( BCellobject.POSITION_T ).intValue();
						final int tt = target.getFeature( BCellobject.POSITION_T ).intValue();

						if ( tt != ts )
						{
							model.beginUpdate();
							try
							{
								model.addEdge( source, target, -1 );
								logger.log( "Created an edge between " + source + " and " + target + ".\n" );
							}
							finally
							{
								model.endUpdate();
							}
							/*
							 * To emulate a kind of automatic linking, we put
							 * the last BCellobject to the selection, so several BCellobjects
							 * can be tracked in a row without having to
							 * de-select one
							 */
							BCellobject single;
							if ( tt > ts )
							{
								single = target;
							}
							else
							{
								single = source;
							}
							selectionModel.clearBCellobjectSelection();
							selectionModel.addBCellobjectToSelection( single );

						}
						else
						{
							logger.error( "Cannot create an edge between two BCellobjects belonging to the same frame.\n" );
						}
					}

				}
				else
				{
					logger.error( "Expected selection to contain 2 BCellobjects, found " + selectedBCellobjects.size() + ".\n" );
				}

			}
			e.consume();
			break;

		}

		case KeyEvent.VK_G:
		case KeyEvent.VK_F:
		{
			// Stepwise time browsing.
			final int currentT = lImp.getT() - 1;
			final int prevStep = ( currentT / params.stepwiseTimeBrowsing ) * params.stepwiseTimeBrowsing;
			int tp;
			if ( keycode == KeyEvent.VK_G )
			{
				tp = prevStep + params.stepwiseTimeBrowsing;
			}
			else
			{
				if ( currentT == prevStep )
				{
					tp = currentT - params.stepwiseTimeBrowsing;
				}
				else
				{
					tp = prevStep;
				}
			}
			lImp.setT( tp + 1 );

			e.consume();
			break;
		}

		case KeyEvent.VK_W:
		{
			e.consume(); // consume it: we do not want IJ to close the window
			break;
		}

		}

	}


	@Override
	public void keyReleased( final KeyEvent e )
	{
		if ( DEBUG )
			System.out.println( "[BCellobjectEditTool] keyReleased: " + e.getKeyChar() );

		switch ( e.getKeyCode() )
		{
		case KeyEvent.VK_SPACE:
		{
			if ( null == quickEditedBCellobject )
				return;
			final ImagePlus lImp = getImagePlus( e );
			if ( lImp == null )
				return;
			final HyperStackDisplayer displayer = displayers.get( lImp );
			if ( null == displayer )
				return;
			final Model model = displayer.getModel();
			model.beginUpdate();
			try
			{
				model.updateFeatures( quickEditedBCellobject );
			}
			finally
			{
				model.endUpdate();
			}
			quickEditedBCellobject = null;
			break;
		}
		}

	}

	/*
	 * PRIVATE METHODS
	 */

	private void updateStatusBar( final BCellobject BCellobject, final String units )
	{
		if ( null == BCellobject )
			return;
		String statusString = "";
		if ( null == BCellobject.getName() || BCellobject.getName().equals( "" ) )
		{
			statusString = String.format( Locale.US, "BCellobject ID%d, x = %.1f, y = %.1f, z = %.1f, r = %.1f %s", BCellobject.ID(), BCellobject.getFeature( BCellobject.POSITION_X ), BCellobject.getFeature( BCellobject.POSITION_Y ), BCellobject.getFeature( BCellobject.POSITION_Z ), BCellobject.getFeature( BCellobject.Size ), units );
		}
		else
		{
			statusString = String.format( Locale.US, "BCellobject %s, x = %.1f, y = %.1f, z = %.1f, r = %.1f %s", BCellobject.getName(), BCellobject.getFeature( BCellobject.POSITION_X ), BCellobject.getFeature( BCellobject.POSITION_Y ), BCellobject.getFeature( BCellobject.POSITION_Z ), BCellobject.getFeature( BCellobject.Size ), units );
		}
		IJ.showStatus( statusString );
	}

	

	@Override
	public void showOptionDialog()
	{
		if ( null == configPanel )
		{
			configPanel = new BCellobjectEditToolConfigPanel( this );
			configPanel.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final WindowEvent e )
				{
					logger = Logger.IJTOOLBAR_LOGGER;
				}
			} );
		}
		configPanel.setLocation( toolbar.getLocationOnScreen() );
		configPanel.setVisible( true );
		logger = configPanel.getLogger();
	}

	/*
	 * INNER CLASSES
	 */

	static class BCellobjectEditToolParams
	{

		/*
		 * Semi-auto tracking parameters
		 */
		/**
		 * The fraction of the initial quality above which we keep new BCellobjects.
		 * The highest, the more intolerant.
		 */
		double qualityThreshold = 0.5;

		/**
		 * How close must be the new BCellobject found to be accepted, in radius units.
		 */
		double distanceTolerance = 2d;

		/**
		 * We process at most nFrames. Make it 0 or negative to have no bounds.
		 */
		int nFrames = 10;

		/**
		 * By how many frames to jymp when we do step-wide time browsing.
		 */
		int stepwiseTimeBrowsing = 5;

		@Override
		public String toString()
		{
			return super.toString() + ": " + "QualityThreshold = " + qualityThreshold + ", DistanceTolerance = " + distanceTolerance + ", nFrames = " + nFrames;
		}
	}

}
