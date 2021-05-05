package fiji.plugin.btrackmate.gui.wizard.descriptors;

import static fiji.plugin.btrackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.btrackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.btrackmate.gui.Fonts.TEXTFIELD_DIMENSION;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fiji.plugin.btrackmate.Logger;
import fiji.plugin.btrackmate.Settings;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.GuiUtils;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.btrackmate.util.TMUtils;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;

public class StartDialogDescriptorOrig extends WizardPanelDescriptor
{

	private static final String KEY = "Start";

	private final Settings settings;

	private final Logger logger;

	public StartDialogDescriptorOrig( final Settings settings, final Logger logger )
	{
		super( KEY );
		this.settings = settings;
		this.logger = logger;
		this.targetPanel = new RoiSettingsPanel( settings.imp );
	}

	@Override
	public void aboutToDisplayPanel()
	{
		final String welcomeMessage = TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION + " started on:\n" + TMUtils.getCurrentTimeString() + '\n';
		// Log GUI processing start
		logger.log( welcomeMessage, Logger.BLUE_COLOR );
		logger.log( "Please note that TrackMate is available through Fiji, and is based on a publication. "
				+ "If you use it successfully for your research please be so kind to cite our work:\n" );
		logger.log( "Tinevez, JY.; Perry, N. & Schindelin, J. et al. (2017), 'TrackMate: An open and extensible platform for single-particle tracking.', "
				+ "Methods 115: 80-90, PMID 27713081.\n", Logger.GREEN_COLOR );
		logger.log( "https://www.ncbi.nlm.nih.gov/pubmed/27713081\n", Logger.BLUE_COLOR );
		logger.log( "https://www.sciencedirect.com/science/article/pii/S1046202316303346\n", Logger.BLUE_COLOR );
		logger.log( "\nNumerical feature analyzers:\n", Logger.BLUE_COLOR );
		logger.log( settings.toStringFeatureAnalyzersInfo() );
	}

	@Override
	public void aboutToHidePanel()
	{
		// Copy the values in the panel to the settings object.
		final RoiSettingsPanel panel = ( RoiSettingsPanel ) targetPanel;
	
		// Log
		logger.log( "\nImage region of interest:\n", Logger.BLUE_COLOR );
		logger.log( settings.toStringImageInfo() );
	}

	private static class RoiSettingsPanel extends JPanel
	{

		private static final long serialVersionUID = -1L;

		private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat( "#.###" );

		private static final String TOOLTIP = "<html>" +
				"Pressing this button will make the current <br>" +
				"ImagePlus the source for TrackMate. If the <br>" +
				"image has a ROI, it will be used to set the <br>" +
				"crop rectangle as well.</html>";



		public RoiSettingsPanel( final ImagePlus imp )
		{
			this.setPreferredSize( new Dimension( 291, 491 ) );
			final GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0 };
			setLayout( gridBagLayout );

			final JLabel lblCitation = new JLabel( "<html>"
					+ "Please note that TrackMate is available through Fiji, "
					+ "and is based on a publication. If you use it successfully "
					+ "for your research please be so kind to cite our work:"
					+ "<p>"
					+ "<b>Tinevez, JY.; Perry, N. & Schindelin, J. et al. (2017), "
					+ "<i>TrackMate: An open and extensible platform for single-particle "
					+ "tracking.</i></b> Methods 115: 80-90."
					+ "</html>" );
			lblCitation.setFont( SMALL_FONT );

			final GridBagConstraints gbcLblCitation = new GridBagConstraints();
			gbcLblCitation.fill = GridBagConstraints.BOTH;
			gbcLblCitation.insets = new Insets( 5, 5, 5, 5 );
			gbcLblCitation.gridwidth = 4;
			gbcLblCitation.gridx = 0;
			gbcLblCitation.gridy = 0;
			add( lblCitation, gbcLblCitation );

			final JLabel lblLinkPubMed = new JLabel( "<html>"
					+ "<a href=https://www.ncbi.nlm.nih.gov/pubmed/27713081>on PubMed (PMID 27713081)</a></html>" );
			lblLinkPubMed.setFont( SMALL_FONT );
			lblLinkPubMed.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
			lblLinkPubMed.addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( final java.awt.event.MouseEvent e )
				{
					try
					{
						Desktop.getDesktop().browse( new URI( "https://www.ncbi.nlm.nih.gov/pubmed/27713081" ) );
					}
					catch ( URISyntaxException | IOException ex )
					{
						ex.printStackTrace();
					}
				}
			} );
			final GridBagConstraints gbcLblLinkPubMed = new GridBagConstraints();
			gbcLblLinkPubMed.anchor = GridBagConstraints.NORTH;
			gbcLblLinkPubMed.fill = GridBagConstraints.HORIZONTAL;
			gbcLblLinkPubMed.gridwidth = 4;
			gbcLblLinkPubMed.insets = new Insets( 0, 10, 5, 5 );
			gbcLblLinkPubMed.gridx = 0;
			gbcLblLinkPubMed.gridy = 1;
			add( lblLinkPubMed, gbcLblLinkPubMed );

			final JLabel lblImageName = new JLabel( "Target image: " + imp.getShortTitle() );
			lblImageName.setFont( BIG_FONT );
			final GridBagConstraints gbcLabelImageName = new GridBagConstraints();
			gbcLabelImageName.anchor = GridBagConstraints.SOUTH;
			gbcLabelImageName.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelImageName.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelImageName.gridwidth = 4;
			gbcLabelImageName.gridx = 0;
			gbcLabelImageName.gridy = 3;
			add( lblImageName, gbcLabelImageName );

			final JLabel lblCalibrationSettings = new JLabel( "Calibration settings:" );
			lblCalibrationSettings.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelCalibration = new GridBagConstraints();
			gbcLabelCalibration.anchor = GridBagConstraints.SOUTH;
			gbcLabelCalibration.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelCalibration.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelCalibration.gridwidth = 4;
			gbcLabelCalibration.gridx = 0;
			gbcLabelCalibration.gridy = 4;
			add( lblCalibrationSettings, gbcLabelCalibration );

			final JLabel lblPixelWidth = new JLabel( "Pixel width:" );
			lblPixelWidth.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelPixelWidth = new GridBagConstraints();
			gbcLabelPixelWidth.anchor = GridBagConstraints.EAST;
			gbcLabelPixelWidth.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelPixelWidth.gridwidth = 2;
			gbcLabelPixelWidth.gridx = 0;
			gbcLabelPixelWidth.gridy = 5;
			add( lblPixelWidth, gbcLabelPixelWidth );

			final JLabel lblPixelWidthVal = new JLabel();
			lblPixelWidthVal.setHorizontalAlignment( SwingConstants.CENTER );
			lblPixelWidthVal.setFont( SMALL_FONT );
			final GridBagConstraints gbcTextFieldPixelWidth = new GridBagConstraints();
			gbcTextFieldPixelWidth.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldPixelWidth.anchor = GridBagConstraints.NORTH;
			gbcTextFieldPixelWidth.insets = new Insets( 5, 5, 5, 5 );
			gbcTextFieldPixelWidth.gridx = 2;
			gbcTextFieldPixelWidth.gridy = 5;
			add( lblPixelWidthVal, gbcTextFieldPixelWidth );

			final JLabel lblSpatialUnits1 = new JLabel();
			lblSpatialUnits1.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelSpatialUnits = new GridBagConstraints();
			gbcLabelSpatialUnits.anchor = GridBagConstraints.WEST;
			gbcLabelSpatialUnits.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelSpatialUnits.gridx = 3;
			gbcLabelSpatialUnits.gridy = 5;
			add( lblSpatialUnits1, gbcLabelSpatialUnits );

			final JLabel lblPixelHeight = new JLabel( "Pixel height:" );
			lblPixelHeight.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelPixelHeight = new GridBagConstraints();
			gbcLabelPixelHeight.anchor = GridBagConstraints.EAST;
			gbcLabelPixelHeight.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelPixelHeight.gridwidth = 2;
			gbcLabelPixelHeight.gridx = 0;
			gbcLabelPixelHeight.gridy = 6;
			add( lblPixelHeight, gbcLabelPixelHeight );

			final JLabel lblPixelHeightVal = new JLabel();
			lblPixelHeightVal.setHorizontalAlignment( SwingConstants.CENTER );
			lblPixelHeightVal.setFont( SMALL_FONT );
			final GridBagConstraints gbcLblPixelHeight = new GridBagConstraints();
			gbcLblPixelHeight.anchor = GridBagConstraints.NORTH;
			gbcLblPixelHeight.fill = GridBagConstraints.HORIZONTAL;
			gbcLblPixelHeight.insets = new Insets( 5, 5, 5, 5 );
			gbcLblPixelHeight.gridx = 2;
			gbcLblPixelHeight.gridy = 6;
			add( lblPixelHeightVal, gbcLblPixelHeight );

			final JLabel lblTimeInterval = new JLabel( "Time interval:" );
			lblTimeInterval.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelTimeInterval = new GridBagConstraints();
			gbcLabelTimeInterval.anchor = GridBagConstraints.EAST;
			gbcLabelTimeInterval.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelTimeInterval.gridwidth = 2;
			gbcLabelTimeInterval.gridx = 0;
			gbcLabelTimeInterval.gridy = 8;
			add( lblTimeInterval, gbcLabelTimeInterval );

			final JLabel lblSpatialUnits2 = new JLabel();
			lblSpatialUnits2.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelTimeUnits = new GridBagConstraints();
			gbcLabelTimeUnits.anchor = GridBagConstraints.WEST;
			gbcLabelTimeUnits.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelTimeUnits.gridx = 3;
			gbcLabelTimeUnits.gridy = 6;
			add( lblSpatialUnits2, gbcLabelTimeUnits );

			final JLabel lblVoxelDepthVal = new JLabel();
			lblVoxelDepthVal.setHorizontalAlignment( SwingConstants.CENTER );
			lblVoxelDepthVal.setFont( SMALL_FONT );
			final GridBagConstraints gbcLblVoxelDepth = new GridBagConstraints();
			gbcLblVoxelDepth.anchor = GridBagConstraints.NORTH;
			gbcLblVoxelDepth.fill = GridBagConstraints.HORIZONTAL;
			gbcLblVoxelDepth.insets = new Insets( 5, 5, 5, 5 );
			gbcLblVoxelDepth.gridx = 2;
			gbcLblVoxelDepth.gridy = 7;
			add( lblVoxelDepthVal, gbcLblVoxelDepth );

			final JLabel lblVoxelDepth = new JLabel( "Voxel depth:" );
			lblVoxelDepth.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelVoxelDepth = new GridBagConstraints();
			gbcLabelVoxelDepth.anchor = GridBagConstraints.EAST;
			gbcLabelVoxelDepth.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelVoxelDepth.gridwidth = 2;
			gbcLabelVoxelDepth.gridx = 0;
			gbcLabelVoxelDepth.gridy = 7;
			add( lblVoxelDepth, gbcLabelVoxelDepth );

			final JLabel lblSpatialUnits3 = new JLabel();
			lblSpatialUnits3.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelUnits3 = new GridBagConstraints();
			gbcLabelUnits3.anchor = GridBagConstraints.WEST;
			gbcLabelUnits3.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelUnits3.gridx = 3;
			gbcLabelUnits3.gridy = 7;
			add( lblSpatialUnits3, gbcLabelUnits3 );

			final JLabel lblTimeUnits = new JLabel();
			lblTimeUnits.setFont( SMALL_FONT );
			final GridBagConstraints gbcLabelUnits4 = new GridBagConstraints();
			gbcLabelUnits4.anchor = GridBagConstraints.WEST;
			gbcLabelUnits4.insets = new Insets( 5, 5, 5, 5 );
			gbcLabelUnits4.gridx = 3;
			gbcLabelUnits4.gridy = 8;
			add( lblTimeUnits, gbcLabelUnits4 );

			final JLabel lblTimeIntervalVal = new JLabel();
			lblTimeIntervalVal.setHorizontalAlignment( SwingConstants.CENTER );
			lblTimeIntervalVal.setFont( SMALL_FONT );
			final GridBagConstraints gbcTextFieldTimeInterval = new GridBagConstraints();
			gbcTextFieldTimeInterval.anchor = GridBagConstraints.NORTH;
			gbcTextFieldTimeInterval.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldTimeInterval.insets = new Insets( 5, 5, 5, 5 );
			gbcTextFieldTimeInterval.gridx = 2;
			gbcTextFieldTimeInterval.gridy = 8;
			add( lblTimeIntervalVal, gbcTextFieldTimeInterval );

			
			/*
			 * Set values from source image.
			 */

			final Calibration cal = imp.getCalibration();
			lblPixelWidthVal.setText( DOUBLE_FORMAT.format( cal.pixelWidth ) );
			lblPixelHeightVal.setText( DOUBLE_FORMAT.format( cal.pixelHeight ) );
			lblVoxelDepthVal.setText( DOUBLE_FORMAT.format( cal.pixelDepth ) );

			if ( cal.frameInterval == 0. )
			{
				cal.frameInterval = 1.;
				cal.setTimeUnit( "frame" );
				lblTimeIntervalVal.setText( DOUBLE_FORMAT.format( 1. ) );
				lblTimeUnits.setText( "frame" );
			}
			else
			{
				lblTimeIntervalVal.setText( DOUBLE_FORMAT.format( cal.frameInterval ) );
				lblTimeUnits.setText( cal.getTimeUnit() );
			}
			lblSpatialUnits1.setText( cal.getXUnit() );
			lblSpatialUnits2.setText( cal.getYUnit() );
			lblSpatialUnits3.setText( cal.getZUnit() );
		}
	}

}
