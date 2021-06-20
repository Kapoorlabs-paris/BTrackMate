package fiji.plugin.btrackmate.gui.wizard.descriptors;

import static fiji.plugin.btrackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.btrackmate.gui.Fonts.SMALL_FONT;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import budDetector.Cellobject;
import fiji.plugin.btrack.gui.components.LoadDualImage;
import fiji.plugin.btrack.gui.components.LoadSingleImage;
import fiji.plugin.btrackmate.Logger;
import fiji.plugin.btrackmate.Settings;
import fiji.plugin.btrackmate.Spot;
import fiji.plugin.btrackmate.SpotCollection;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.TrackMatePlugIn;
import fiji.plugin.btrackmate.detection.LabelImageDetector;
import fiji.plugin.btrackmate.detection.MaskUtils;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.btrackmate.util.TMUtils;
import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.SelectionModel;
import fiji.util.NumberParser;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.Calibration;
import net.imagej.ImgPlus;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import pluginTools.simplifiedio.SimplifiedIO;

public class StartDialogDescriptor extends WizardPanelDescriptor {

	private static final String KEY = "Start";

	public Settings settings;

	public Model model;

	private final Logger logger;
	
	public boolean secondrun;

	public StartDialogDescriptor(final Model model, final Settings settings, final Logger logger, final boolean secondrun) {
		super(KEY);
		
		this.settings = settings;
		this.logger = logger;
		this.model = model;
        this.secondrun = secondrun;
		updatemodel = model;
		updatesettings = settings;
		updateimp = settings.imp;
		updatelogger = logger;
		this.targetPanel = new RoiSettingsPanel(settings.imp, this.secondrun);
		
	}

	@Override
	public void aboutToDisplayPanel() {
		final RoiSettingsPanel panel = (RoiSettingsPanel) targetPanel;
		final String welcomeMessage = TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION + '\n';
		// Log GUI processing start
		logger.log(welcomeMessage, Logger.BLUE_COLOR);
		logger.log("BTrackmate is a slim version of Trackmate "
				+ "If you input label images/csv file we skip detection process of Trackmate.\n");
		logger.log("Made for Claudia Carabana Garcia " + "by Varun Kapoor, TrackMate is based on:\n");
		logger.log(
				"Tinevez, JY.; Perry, N. & Schindelin, J. et al. (2017), 'TrackMate: An open and extensible platform for single-particle tracking.', "
						+ "Methods 115: 80-90, PMID 27713081.\n",
				Logger.GREEN_COLOR);
		logger.log("https://www.ncbi.nlm.nih.gov/pubmed/27713081\n", Logger.BLUE_COLOR);
		logger.log("https://www.sciencedirect.com/science/article/pii/S1046202316303346\n", Logger.BLUE_COLOR);
		logger.log("\nNumerical feature analyzers:\n", Logger.BLUE_COLOR);
		logger.log(settings.toStringFeatureAnalyzersInfo());
		logger.setProgress(0);
		panel.FreeMode.setVisible(true);
		panel.MaskMode.setVisible(true);
		panel.CsvMode.setVisible(true);
		panel.ImageMode.setVisible(true);
	}

	@Override
	public void aboutToHidePanel() {
		// Copy the values in the panel to the settings object.
		final RoiSettingsPanel panel = (RoiSettingsPanel) targetPanel;
		settings.xstart = ((Number) panel.tfXStart.getValue()).intValue();
		settings.xend = ((Number) panel.tfXEnd.getValue()).intValue();
		settings.ystart = ((Number) panel.tfYStart.getValue()).intValue();
		settings.yend = ((Number) panel.tfYEnd.getValue()).intValue();
		settings.zstart = ((Number) panel.tfZStart.getValue()).intValue();
		settings.zend = ((Number) panel.tfZEnd.getValue()).intValue();
		settings.tstart = ((Number) panel.tfTStart.getValue()).intValue();
		settings.tend = ((Number) panel.tfTEnd.getValue()).intValue();
		// Log
		logger.log("\nImage region of interest:\n", Logger.BLUE_COLOR);
		logger.log(settings.toStringImageInfo());
		panel.FreeMode.setVisible(false);
		panel.MaskMode.setVisible(false);
		panel.CsvMode.setVisible(false);
		panel.ImageMode.setVisible(false);
	}

	public static ImagePlus updateimp;
	public static Settings updatesettings;
	public static TrackMate updatedbtrackmate;
	public static Model updatemodel;
	public static SpotCollection spots;
	public static Pair<SpotCollection,HashMap<Integer, ArrayList<Spot>>>  SpotListFrame;
	public static Logger updatelogger;

	private  static  class  RoiSettingsPanel extends JPanel {

		private static final long serialVersionUID = -1L;
		/** ActionEvent fired when the user press the refresh button. */
		private final ActionEvent IMAGEPLUS_REFRESHED = new ActionEvent(this, 0, "ImagePlus refreshed");
		private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat("#.###");

		private static final String TOOLTIP = "<html>" + "Pressing this button will make the current <br>"
				+ "ImagePlus the source for BTrackMate..</html>";

		private final JFormattedTextField tfXStart;
		private final JFormattedTextField tfXEnd;
		private final JFormattedTextField tfYStart;
		private final JFormattedTextField tfYEnd;
		private final JFormattedTextField tfZStart;
		private final JFormattedTextField tfZEnd;
		private final JFormattedTextField tfTStart;
		private final JFormattedTextField tfTEnd;
		
		
		public JPanel Panelfile = new JPanel();
		public JPanel PanelDualfile = new JPanel();
		public boolean DoMask = false;
		public boolean NoMask = true;
		public JButton Checkpointbutton = new JButton("Load Data From CSV");
		public String chooseSegstring = "Segmentation Image";
		public String chooseMaskstring = "Segmentation and Mask Image";
		public boolean LoadImage = false;
		public boolean LoadCSV = true;
		public CheckboxGroup SegLoadmode = new CheckboxGroup();

		public CheckboxGroup cellmode = new CheckboxGroup();
		public Checkbox FreeMode = new Checkbox("No Mask", NoMask, cellmode);
		public Checkbox MaskMode = new Checkbox("With Mask", DoMask, cellmode);
		public ImagePlus impSeg, impMask;

		public final String[] imageNames, blankimageNames;

		JLabel lblPixelWidthVal = new JLabel();
		JLabel lblImageName = new JLabel("Target image: ");
		JLabel lblCalibrationSettings = new JLabel("Calibration settings:");
		JLabel lblPixelWidth = new JLabel("Pixel width:");
		JLabel lblSpatialUnits1 = new JLabel();
		JLabel lblPixelHeight = new JLabel("Pixel height:");
		JLabel lblPixelHeightVal = new JLabel();
		JLabel lblTimeInterval = new JLabel("Time interval:");
		JLabel lblSpatialUnits2 = new JLabel();
		JLabel lblVoxelDepthVal = new JLabel();
		JLabel lblVoxelDepth = new JLabel("Voxel depth:");
		JLabel lblSpatialUnits3 = new JLabel();
		JLabel lblTimeUnits = new JLabel();
		JLabel lblTimeIntervalVal = new JLabel();

		Checkbox ImageMode = new Checkbox("Segmentation as Images", LoadImage, SegLoadmode);
		Checkbox CsvMode = new Checkbox("Segmentation as csv", LoadCSV, SegLoadmode);
		GridBagConstraints gbcChooseFree = new GridBagConstraints();

		GridBagConstraints gbcChooseMask = new GridBagConstraints();
		GridBagConstraints gbcChooseCheck = new GridBagConstraints();
		GridBagConstraints gbcExecDet = new GridBagConstraints();

		GridBagConstraints gbcChooseSegLoad = new GridBagConstraints();
		GridBagConstraints gbcChooseMaskLoad = new GridBagConstraints();
		public JButton ExecuteDetection = new JButton("Create Dataset");

		double[] calibration;
		public HashMap<Integer, ArrayList<Cellobject>> CSV = new HashMap<Integer, ArrayList<Cellobject>>();

		public< T extends Type< T > > RoiSettingsPanel(final ImagePlus imp, final Boolean secondrun) {
			
			this.setPreferredSize(new Dimension(400, 500));
			
			final GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
					0.0, 1.0 };
			gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0 };
			setLayout(gridBagLayout);
			final JLabel lblCitation = new JLabel("<html>" + "BTrackmate is a slim version of Trackmate "
					+ "If you input label images/csv file we skip detection process of Trackmate.\n" + "<p>"
					+ "Made for Claudia Carabana Garcia " + "by Varun Kapoor." + "<p>" + " TrackMate is based on:\n"
					+ "<p>" + "<b>Tinevez, JY.; Perry, N. & Schindelin, J. et al. (2017), "
					+ "<i>TrackMate: An open and extensible platform for single-particle "
					+ "tracking.</i></b> Methods 115: 80-90." + "</html>");
			lblCitation.setFont(SMALL_FONT);

			final GridBagConstraints gbcLblCitation = new GridBagConstraints();
			gbcLblCitation.fill = GridBagConstraints.BOTH;
			gbcLblCitation.insets = new Insets(5, 5, 5, 5);
			gbcLblCitation.gridwidth = 4;
			gbcLblCitation.gridx = 0;
			gbcLblCitation.gridy = 0;
			add(lblCitation, gbcLblCitation);

			final JLabel lblLinkPubMed = new JLabel("<html>"
					+ "<a href=https://www.ncbi.nlm.nih.gov/pubmed/27713081>on PubMed (PMID 27713081)</a></html>");
			lblLinkPubMed.setFont(SMALL_FONT);
			lblLinkPubMed.setCursor(new Cursor(Cursor.HAND_CURSOR));
			lblLinkPubMed.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final java.awt.event.MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("https://www.ncbi.nlm.nih.gov/pubmed/27713081"));
					} catch (URISyntaxException | IOException ex) {
						ex.printStackTrace();
					}
				}
			});
			final GridBagConstraints gbcLblLinkPubMed = new GridBagConstraints();
			gbcLblLinkPubMed.anchor = GridBagConstraints.NORTH;
			gbcLblLinkPubMed.fill = GridBagConstraints.HORIZONTAL;
			gbcLblLinkPubMed.gridwidth = 4;
			gbcLblLinkPubMed.insets = new Insets(0, 10, 5, 5);
			gbcLblLinkPubMed.gridx = 0;
			gbcLblLinkPubMed.gridy = 1;
			add(lblLinkPubMed, gbcLblLinkPubMed);

			lblImageName = new JLabel("Target image: " + imp.getShortTitle());
			lblImageName.setFont(BIG_FONT);
			final GridBagConstraints gbcLabelImageName = new GridBagConstraints();
			gbcLabelImageName.anchor = GridBagConstraints.SOUTH;
			gbcLabelImageName.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelImageName.insets = new Insets(5, 5, 5, 5);
			gbcLabelImageName.gridwidth = 4;
			gbcLabelImageName.gridx = 0;
			gbcLabelImageName.gridy = 3;
			add(lblImageName, gbcLabelImageName);

			lblCalibrationSettings.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelCalibration = new GridBagConstraints();
			gbcLabelCalibration.anchor = GridBagConstraints.SOUTH;
			gbcLabelCalibration.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelCalibration.insets = new Insets(5, 5, 5, 5);
			gbcLabelCalibration.gridwidth = 4;
			gbcLabelCalibration.gridx = 0;
			gbcLabelCalibration.gridy = 4;
			add(lblCalibrationSettings, gbcLabelCalibration);

			lblPixelWidth.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelPixelWidth = new GridBagConstraints();
			gbcLabelPixelWidth.anchor = GridBagConstraints.EAST;
			gbcLabelPixelWidth.insets = new Insets(5, 5, 5, 5);
			gbcLabelPixelWidth.gridx = 0;
			gbcLabelPixelWidth.gridy = 5;
			add(lblPixelWidth, gbcLabelPixelWidth);

			lblPixelWidthVal.setHorizontalAlignment(SwingConstants.CENTER);
			lblPixelWidthVal.setFont(SMALL_FONT);
			final GridBagConstraints gbcTextFieldPixelWidth = new GridBagConstraints();
			gbcTextFieldPixelWidth.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldPixelWidth.anchor = GridBagConstraints.NORTH;
			gbcTextFieldPixelWidth.insets = new Insets(5, 5, 5, 5);
			gbcTextFieldPixelWidth.gridx = 2;
			gbcTextFieldPixelWidth.gridy = 5;
			add(lblPixelWidthVal, gbcTextFieldPixelWidth);

			lblSpatialUnits1.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelSpatialUnits = new GridBagConstraints();
			gbcLabelSpatialUnits.anchor = GridBagConstraints.WEST;
			gbcLabelSpatialUnits.insets = new Insets(5, 5, 5, 5);
			gbcLabelSpatialUnits.gridx = 3;
			gbcLabelSpatialUnits.gridy = 5;
			add(lblSpatialUnits1, gbcLabelSpatialUnits);

			lblPixelHeight.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelPixelHeight = new GridBagConstraints();
			gbcLabelPixelHeight.anchor = GridBagConstraints.EAST;
			gbcLabelPixelHeight.insets = new Insets(5, 5, 5, 5);
			gbcLabelPixelHeight.gridx = 0;
			gbcLabelPixelHeight.gridy = 6;
			add(lblPixelHeight, gbcLabelPixelHeight);

			lblPixelHeightVal.setHorizontalAlignment(SwingConstants.CENTER);
			lblPixelHeightVal.setFont(SMALL_FONT);
			final GridBagConstraints gbcLblPixelHeight = new GridBagConstraints();
			gbcLblPixelHeight.anchor = GridBagConstraints.NORTH;
			gbcLblPixelHeight.fill = GridBagConstraints.HORIZONTAL;
			gbcLblPixelHeight.insets = new Insets(5, 5, 5, 5);
			gbcLblPixelHeight.gridx = 2;
			gbcLblPixelHeight.gridy = 6;
			add(lblPixelHeightVal, gbcLblPixelHeight);

			lblTimeInterval.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelTimeInterval = new GridBagConstraints();
			gbcLabelTimeInterval.anchor = GridBagConstraints.EAST;
			gbcLabelTimeInterval.insets = new Insets(5, 5, 5, 5);
			gbcLabelTimeInterval.gridx = 0;
			gbcLabelTimeInterval.gridy = 8;
			add(lblTimeInterval, gbcLabelTimeInterval);

			lblSpatialUnits2.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelTimeUnits = new GridBagConstraints();
			gbcLabelTimeUnits.anchor = GridBagConstraints.WEST;
			gbcLabelTimeUnits.insets = new Insets(5, 5, 5, 5);
			gbcLabelTimeUnits.gridx = 3;
			gbcLabelTimeUnits.gridy = 6;
			add(lblSpatialUnits2, gbcLabelTimeUnits);

			lblVoxelDepthVal.setHorizontalAlignment(SwingConstants.CENTER);
			lblVoxelDepthVal.setFont(SMALL_FONT);
			final GridBagConstraints gbcLblVoxelDepth = new GridBagConstraints();
			gbcLblVoxelDepth.anchor = GridBagConstraints.NORTH;
			gbcLblVoxelDepth.fill = GridBagConstraints.HORIZONTAL;
			gbcLblVoxelDepth.insets = new Insets(5, 5, 5, 5);
			gbcLblVoxelDepth.gridx = 2;
			gbcLblVoxelDepth.gridy = 7;
			add(lblVoxelDepthVal, gbcLblVoxelDepth);

			lblVoxelDepth.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelVoxelDepth = new GridBagConstraints();
			gbcLabelVoxelDepth.anchor = GridBagConstraints.EAST;
			gbcLabelVoxelDepth.insets = new Insets(5, 5, 5, 5);
			gbcLabelVoxelDepth.gridx = 0;
			gbcLabelVoxelDepth.gridy = 7;
			add(lblVoxelDepth, gbcLabelVoxelDepth);

			lblSpatialUnits3.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelUnits3 = new GridBagConstraints();
			gbcLabelUnits3.anchor = GridBagConstraints.WEST;
			gbcLabelUnits3.insets = new Insets(5, 5, 5, 5);
			gbcLabelUnits3.gridx = 3;
			gbcLabelUnits3.gridy = 7;
			add(lblSpatialUnits3, gbcLabelUnits3);

			lblTimeUnits.setFont(SMALL_FONT);
			final GridBagConstraints gbcLabelUnits4 = new GridBagConstraints();
			gbcLabelUnits4.anchor = GridBagConstraints.WEST;
			gbcLabelUnits4.insets = new Insets(5, 5, 5, 5);
			gbcLabelUnits4.gridx = 3;
			gbcLabelUnits4.gridy = 8;
			add(lblTimeUnits, gbcLabelUnits4);

			lblTimeIntervalVal.setHorizontalAlignment(SwingConstants.CENTER);
			lblTimeIntervalVal.setFont(SMALL_FONT);
			final GridBagConstraints gbcTextFieldTimeInterval = new GridBagConstraints();
			gbcTextFieldTimeInterval.anchor = GridBagConstraints.NORTH;
			gbcTextFieldTimeInterval.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldTimeInterval.insets = new Insets(5, 5, 5, 5);
			gbcTextFieldTimeInterval.gridx = 2;
			gbcTextFieldTimeInterval.gridy = 8;
			add(lblTimeIntervalVal, gbcTextFieldTimeInterval);

			imageNames = WindowManager.getImageTitles();
			blankimageNames = new String[imageNames.length + 1];
			blankimageNames[0] = " ";

			for (int i = 0; i < imageNames.length; ++i)
				blankimageNames[i + 1] = imageNames[i];
			if(!secondrun) {
			ImageMode.setFont(SMALL_FONT);
			final GridBagConstraints gbcChooseImage = new GridBagConstraints();
			gbcChooseImage.anchor = GridBagConstraints.EAST;
			gbcChooseImage.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseImage.insets = new Insets(5, 5, 5, 5);
			gbcChooseImage.gridx = 0;
			gbcChooseImage.gridy = 9;
			add(ImageMode, gbcChooseImage);

			CsvMode.setFont(SMALL_FONT);
			final GridBagConstraints gbcChooseCSV = new GridBagConstraints();
			gbcChooseCSV.anchor = GridBagConstraints.EAST;
			gbcChooseCSV.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseCSV.insets = new Insets(5, 5, 5, 5);
			gbcChooseCSV.gridx = 0;
			gbcChooseCSV.gridy = 10;
			add(ImageMode, gbcChooseImage);

			add(CsvMode, gbcChooseCSV);
			}

			tfXStart = new JFormattedTextField(Integer.valueOf(0));

			tfXEnd = new JFormattedTextField(Integer.valueOf(0));

			tfYStart = new JFormattedTextField(Integer.valueOf(0));

			tfYEnd = new JFormattedTextField(Integer.valueOf(0));

			tfZStart = new JFormattedTextField(Integer.valueOf(0));

			tfZEnd = new JFormattedTextField(Integer.valueOf(0));

			tfTStart = new JFormattedTextField(Integer.valueOf(0));

			tfTEnd = new JFormattedTextField(Integer.valueOf(0));

			final JButton btnRefreshROI = new JButton("Refresh Raw");
			btnRefreshROI.setToolTipText(TOOLTIP);
			btnRefreshROI.setFont(SMALL_FONT);
			final GridBagConstraints gbcButtonRefresh = new GridBagConstraints();
			gbcButtonRefresh.anchor = GridBagConstraints.NORTHWEST;
			gbcButtonRefresh.insets = new Insets(5, 5, 5, 5);
			gbcButtonRefresh.gridwidth = 4;
			gbcButtonRefresh.gridx = 0;
			gbcButtonRefresh.gridy = 16;
			if(!secondrun)
			add(btnRefreshROI, gbcButtonRefresh);
			
			btnRefreshROI.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Roi roi = new Roi(0, 0, imp.getWidth(), imp.getHeight());

					final Rectangle boundingRect = roi.getBounds();
					tfXStart.setValue(Integer.valueOf(boundingRect.x));
					tfYStart.setValue(Integer.valueOf(boundingRect.y));
					tfXEnd.setValue(Integer.valueOf(boundingRect.width + boundingRect.x - 1));
					tfYEnd.setValue(Integer.valueOf(boundingRect.height + boundingRect.y - 1));
					tfZStart.setValue(Integer.valueOf(0));
					tfZEnd.setValue(Integer.valueOf(imp.getNSlices() - 1));
					tfTStart.setValue(Integer.valueOf(0));
					tfTEnd.setValue(Integer.valueOf(imp.getNFrames() - 1));

					updateimp = WindowManager.getCurrentImage();
					updatesettings.setFrom(updateimp);
					getFrom(updateimp);
					fireAction(IMAGEPLUS_REFRESHED);

				}
			});
			
			if(secondrun) {
				
				final JLabel secCitation = new JLabel("<html>" + "You have choosen your run mode,"
                          +  " the calibration settings are taken from your chosen image. \n"   + "<p>"
						  + " Click next to start the tracking process.\n" + "<p>"
						 + "</html>");
				secCitation.setFont(SMALL_FONT);

				final GridBagConstraints gbcsecCitation = new GridBagConstraints();
				gbcsecCitation.fill = GridBagConstraints.BOTH;
				gbcsecCitation.insets = new Insets(5, 5, 5, 5);
				gbcsecCitation.gridwidth = 4;
				gbcsecCitation.gridx = 0;
				gbcsecCitation.gridy = 13;
				add(secCitation, gbcsecCitation);
			}

			if(!secondrun) {
			FreeMode.setFont(SMALL_FONT);

			gbcChooseFree.anchor = GridBagConstraints.WEST;
			gbcChooseFree.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseFree.insets = new Insets(5, 5, 5, 5);
			gbcChooseFree.gridx = 3;
			gbcChooseFree.gridy = 9;

			MaskMode.setFont(SMALL_FONT);

			gbcChooseMask.anchor = GridBagConstraints.WEST;
			gbcChooseMask.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseMask.insets = new Insets(5, 5, 5, 5);
			gbcChooseMask.gridx = 3;
			gbcChooseMask.gridy = 10;

			Checkpointbutton.setFont(SMALL_FONT);

			gbcChooseCheck.anchor = GridBagConstraints.EAST;
			gbcChooseCheck.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseCheck.insets = new Insets(5, 5, 5, 5);
			gbcChooseCheck.gridx = 0;
			gbcChooseCheck.gridy = 11;
			add(Checkpointbutton, gbcChooseCheck);

			gbcChooseSegLoad.anchor = GridBagConstraints.EAST;
			gbcChooseSegLoad.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseSegLoad.insets = new Insets(5, 5, 5, 5);
			gbcChooseSegLoad.gridwidth = 4;
			gbcChooseSegLoad.gridx = 0;
			gbcChooseSegLoad.gridy = 13;

			gbcChooseMaskLoad.anchor = GridBagConstraints.EAST;
			gbcChooseMaskLoad.fill = GridBagConstraints.HORIZONTAL;
			gbcChooseMaskLoad.insets = new Insets(5, 5, 5, 5);
			gbcChooseMaskLoad.gridwidth = 4;
			gbcChooseMaskLoad.gridx = 0;
			gbcChooseMaskLoad.gridy = 14;

			gbcExecDet.anchor = GridBagConstraints.EAST;
			ExecuteDetection.setFont(SMALL_FONT);
			gbcExecDet.fill = GridBagConstraints.HORIZONTAL;
			gbcExecDet.insets = new Insets(5, 5, 5, 5);
			gbcExecDet.gridwidth = 4;
			gbcExecDet.gridx = 2;
			gbcExecDet.gridy = 14;
			}
			Panelfile.setFont(SMALL_FONT);
			Calibration cal = imp.getCalibration();
			calibration = new double[3];
			
			int ndims = imp.getNDimensions();
			if (ndims == 2)
				calibration = new double[] { cal.pixelWidth, cal.pixelHeight, 1 };
			else
				calibration = new double[] { cal.pixelWidth, cal.pixelHeight, cal.pixelDepth };

			ImageMode.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						add(FreeMode, gbcChooseFree);
						add(MaskMode, gbcChooseMask);
						add(Panelfile, gbcChooseSegLoad);
						add(ExecuteDetection, gbcExecDet);
						ExecuteDetection.setEnabled(false);
						remove(Checkpointbutton);
						validate();
						repaint();
						FreeMode.setEnabled(true);

					}

					else if (e.getStateChange() == ItemEvent.DESELECTED) {

						remove(FreeMode);
						remove(MaskMode);
						remove(Panelfile);
						remove(ExecuteDetection);
						validate();
						repaint();

					}

				}
			});
			LoadSingleImage segmentation = new LoadSingleImage(chooseSegstring, blankimageNames, gbcChooseSegLoad);
			Panelfile = segmentation.SingleChannelOption();

			LoadDualImage Dualsegmentation = new LoadDualImage(chooseMaskstring, blankimageNames, gbcChooseSegLoad,
					gbcChooseMaskLoad);
			PanelDualfile = Dualsegmentation.TwoChannelOption();

			segmentation.ChooseImage.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					String imagename = (String) segmentation.ChooseImage.getSelectedItem();
					impSeg = WindowManager.getImage(imagename);
					
					NoMask = true;
					DoMask = false;
					
					ExecuteDetection.setEnabled(true);
				    
					

				}
			});
			Dualsegmentation.ChooseImage.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					String imagename = (String) Dualsegmentation.ChooseImage.getSelectedItem();
					impSeg = WindowManager.getImage(imagename);
					
				}
			});

			Dualsegmentation.ChoosesecImage.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					String imagename = (String) Dualsegmentation.ChoosesecImage.getSelectedItem();
					impMask = WindowManager.getImage(imagename);
					ExecuteDetection.setEnabled(true);
					

				}
			});

			FreeMode.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					if (e.getStateChange() == ItemEvent.SELECTED) {

						remove(Panelfile);
						remove(PanelDualfile);
						// Listeneres
						TrackMate.CsvSpots = null;
						add(Panelfile, gbcChooseSegLoad);

						validate();
						repaint();

						


					} else if (e.getStateChange() == ItemEvent.DESELECTED) {

						NoMask = false;
						DoMask = false;

						remove(Panelfile);
						remove(PanelDualfile);
						validate();
						repaint();
					}

				}

			});

			MaskMode.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					if (e.getStateChange() == ItemEvent.SELECTED) {

						remove(Panelfile);
						remove(PanelDualfile);
						// Listeneres
						TrackMate.CsvSpots = null;
						add(PanelDualfile, gbcChooseSegLoad);
						validate();
						repaint();
						NoMask = false;
						DoMask = true;

						


					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						NoMask = false;
						DoMask = false;
						remove(Panelfile);
						remove(PanelDualfile);
						validate();
						repaint();
					}

				}

			});
			
			ExecuteDetection.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (impSeg != null&& impMask == null) {

						ImgPlus<FloatType> output =  createHyperStack(imp, impSeg);
						ImagePlus localimp = ImageJFunctions.show(output, "Channels");
						localimp.setCalibration(imp.getCalibration()); 
						
						TrackMatePlugIn.ModelUpdate( updatelogger,imp, localimp);
					}

					if (impSeg != null && impMask != null) {

						
						ImgPlus<FloatType> output =  createmaskedHyperStack(imp, impSeg, impMask);
						ImagePlus localimp = ImageJFunctions.show(output, "Channels");
					    localimp.setCalibration(imp.getCalibration()); 
						
						
						TrackMatePlugIn.ModelUpdate( updatelogger,imp, localimp);
					}

				}
			});

			CsvMode.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent ef) {

					if (ef.getStateChange() == ItemEvent.SELECTED) {

						add(Checkpointbutton, gbcChooseCheck);
						remove(FreeMode);
						remove(MaskMode);
						remove(Panelfile);
						remove(PanelDualfile);
						
						validate();
						repaint();

					}

					else if (ef.getStateChange() == ItemEvent.DESELECTED) {

						remove(Checkpointbutton);
						validate();
						repaint();

					}

				}
			});
			Checkpointbutton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent a) {

					JFileChooser csvfile = new JFileChooser();
					FileFilter csvfilter = new FileFilter() {
						// Override accept method
						public boolean accept(File file) {

							// if the file extension is .log return true, else false
							if (file.getName().endsWith(".csv")) {
								return true;
							}
							return false;
						}

						@Override
						public String getDescription() {

							return null;
						}
					};
					String line = "";
					String cvsSplitBy = ",";
					csvfile.setCurrentDirectory(new File(imp.getOriginalFileInfo().directory));
					csvfile.setDialogTitle(" Cell CSV file");
					csvfile.setFileSelectionMode(JFileChooser.FILES_ONLY);
					csvfile.setFileFilter(csvfilter);
					if (!CSV.isEmpty())
						CSV.clear();
					int count = 0;

					if (csvfile.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

						File budfile = new File(csvfile.getSelectedFile().getPath());
						ArrayList<Cellobject> reloadcell = new ArrayList<Cellobject>();

						try (BufferedReader br = new BufferedReader(new FileReader(budfile))) {

							while ((line = br.readLine()) != null) {

								// use comma as separator
								String[] budpoints = line.split(cvsSplitBy);

								if (count > 0) {

									int time = Integer.parseInt(budpoints[0]);
									double X = Double.parseDouble(budpoints[1]);
									double Y = Double.parseDouble(budpoints[2]);
									double Z = Double.parseDouble(budpoints[3]);
									int Label = Integer.parseInt(budpoints[4]);
									double Perimeter = Double.parseDouble(budpoints[5]);
									double Area = Double.parseDouble(budpoints[6]);
									int Intensity = Integer.parseInt(budpoints[7]);
									double sizeX = Double.parseDouble(budpoints[8]);
									double sizeY = Double.parseDouble(budpoints[9]);
									double sizeZ = Double.parseDouble(budpoints[10]);

									double[] extents = new double[] { sizeX, sizeY, sizeZ };
									Point point = new Point(new long[] { (long) X, (long) Y, (long) Z });

									Cellobject currentcell = new Cellobject(point, time, Label, Perimeter, Area,
											Intensity, extents);

									if (CSV.get(time) == null) {
										reloadcell = new ArrayList<Cellobject>();
										CSV.put(time, reloadcell);
									} else
										CSV.put(time, reloadcell);

									reloadcell.add(currentcell);

								}
								count = count + 1;
							}
						}

						catch (IOException ie) {
							ie.printStackTrace();
						}

						getFrom(updateimp);
						fireAction(IMAGEPLUS_REFRESHED);

						SpotListFrame = MaskUtils.fromSimpleCSV(CSV, ndims, calibration);
						
						TrackMate.CsvSpots = SpotListFrame.getA();
						TrackMate.Framespots = SpotListFrame.getB();
						
						ImagePlus localimp = imp.duplicate();
						localimp.show();
						TrackMatePlugIn.ModelUpdate(updatelogger,imp, localimp);

					} else
						csvfile = null;

				}

				// TODO Auto-generated method stub

			});
			lblPixelWidthVal.setText(DOUBLE_FORMAT.format(cal.pixelWidth));
			lblPixelHeightVal.setText(DOUBLE_FORMAT.format(cal.pixelHeight));
			lblVoxelDepthVal.setText(DOUBLE_FORMAT.format(cal.pixelDepth));

			if (cal.frameInterval == 0.) {
				cal.frameInterval = 1.;
				cal.setTimeUnit("frame");
				lblTimeIntervalVal.setText(DOUBLE_FORMAT.format(1.));
				lblTimeUnits.setText("frame");
			} else {
				lblTimeIntervalVal.setText(DOUBLE_FORMAT.format(cal.frameInterval));
				lblTimeUnits.setText(cal.getTimeUnit());
			}
			lblSpatialUnits1.setText(cal.getXUnit());
			lblSpatialUnits2.setText(cal.getYUnit());
			lblSpatialUnits3.setText(cal.getZUnit());
			btnRefreshROI.doClick();
			
			
			
		}

		protected ArrayList<ActionListener> actionListeners = new ArrayList<>();
		protected ArrayList<ItemListener> itemListeners = new ArrayList<>();

		protected void fireAction(ActionEvent e) {
			for (ActionListener l : actionListeners)
				l.actionPerformed(e);
		}

		protected void fireItem(ItemEvent e) {
			for (ItemListener l : itemListeners)
				l.itemStateChanged(e);
		}

		/**
		 * Fill the text fields with parameters grabbed from specified ImagePlus.
		 */
		public void getFrom(final ImagePlus lImp) {

			updateimp = lImp;
			updatesettings.setFrom(updateimp);
			if (null == lImp) {
				lblImageName.setText("No image selected.");
				return;
			}

			if (lImp.getType() == ImagePlus.COLOR_RGB) {
				// We do not know how to process RGB images
				lblImageName.setText(lImp.getShortTitle() + " is RGB: invalid.");
				return;
			}

			lblImageName.setText("Target: " + lImp.getShortTitle());
			lblPixelWidthVal.setText(DOUBLE_FORMAT.format(lImp.getCalibration().pixelWidth));
			lblPixelHeightVal.setText(DOUBLE_FORMAT.format(lImp.getCalibration().pixelHeight));
			lblVoxelDepthVal.setText(DOUBLE_FORMAT.format(lImp.getCalibration().pixelDepth));
			if (lImp.getCalibration().frameInterval == 0) {
				lblTimeIntervalVal.setText(DOUBLE_FORMAT.format(1.));
				lblTimeUnits.setText("frame");
			} else {
				lblTimeIntervalVal.setText(DOUBLE_FORMAT.format(lImp.getCalibration().frameInterval));
				lblTimeUnits.setText(lImp.getCalibration().getTimeUnit());
			}
			lblSpatialUnits1.setText(lImp.getCalibration().getXUnit());
			lblSpatialUnits2.setText(lImp.getCalibration().getYUnit());
			lblSpatialUnits3.setText(lImp.getCalibration().getZUnit());
			Roi roi = lImp.getRoi();
			if (null == roi)
				roi = new Roi(0, 0, lImp.getWidth(), lImp.getHeight());
			final Rectangle boundingRect = roi.getBounds();
			tfXStart.setText("" + (boundingRect.x));
			tfYStart.setText("" + (boundingRect.y));
			tfXEnd.setText("" + (boundingRect.width + boundingRect.x - 1));
			tfYEnd.setText("" + (boundingRect.height + boundingRect.y - 1));
			tfZStart.setText("" + 0);
			tfZEnd.setText("" + (lImp.getNSlices() - 1));
			tfTStart.setText("" + 0);
			tfTEnd.setText("" + (lImp.getNFrames() - 1));
			updateTo(updatemodel, updateimp);

		}

		public void updateTo(final Model model, final ImagePlus imp) {
			updatesettings.imp = imp;
			// Crop cube
			updatesettings.tstart = NumberParser.parseInteger(tfTStart.getText());
			updatesettings.tend = NumberParser.parseInteger(tfTEnd.getText());
			updatesettings.xstart = NumberParser.parseInteger(tfXStart.getText());
			updatesettings.xend = NumberParser.parseInteger(tfXEnd.getText());
			updatesettings.ystart = NumberParser.parseInteger(tfYStart.getText());
			updatesettings.yend = NumberParser.parseInteger(tfYEnd.getText());
			updatesettings.zstart = NumberParser.parseInteger(tfZStart.getText());
			updatesettings.zend = NumberParser.parseInteger(tfZEnd.getText());
			// Image info
			updatesettings.dx = NumberParser.parseDouble(lblPixelWidthVal.getText());
			updatesettings.dy = NumberParser.parseDouble(lblPixelHeightVal.getText());
			updatesettings.dz = NumberParser.parseDouble(lblVoxelDepthVal.getText());
			updatesettings.dt = NumberParser.parseDouble(lblTimeIntervalVal.getText());
			updatesettings.width = imp.getWidth();
			updatesettings.height = imp.getHeight();
			updatesettings.nslices = imp.getNSlices();
			updatesettings.nframes = imp.getNFrames();

			// Units
			model.setPhysicalUnits(lblSpatialUnits1.getText(), lblTimeUnits.getText());
			// Roi
			updatesettings.roi = imp.getRoi();

			// File info
			if (null != imp.getOriginalFileInfo()) {
				updatesettings.imageFileName = imp.getOriginalFileInfo().fileName;
				updatesettings.imageFolder = imp.getOriginalFileInfo().directory;
			}
			updatedbtrackmate = updatemodel(model, updatesettings);

		}
		
	



	}

	public TrackMate returnUpdatedtrackmate() {

		return updatedbtrackmate;
	}

	public Model returnUpdatedmodel() {

		return updatemodel;
	}

	public Settings returnUpdatesettings() {

		return updatesettings;
	}

	public SpotCollection returnSpotCollection() {

		return spots;
	}

	public static TrackMate updatemodel(final Model model, final Settings settings) {

		TrackMate updatedbtrackmate = new TrackMate(model, settings);

		return updatedbtrackmate;
	}
	public static ImgPlus<FloatType> createHyperStack(ImagePlus imp, ImagePlus impSeg) {
		
		RandomAccessibleInterval<FloatType> imageOrig = SimplifiedIO.openImage(
				imp.getOriginalFileInfo().directory + imp.getOriginalFileInfo().fileName, new FloatType());
		
		RandomAccessibleInterval<IntType> imageSeg = SimplifiedIO.openImage(
				impSeg.getOriginalFileInfo().directory + impSeg.getOriginalFileInfo().fileName, new IntType());
		
		
		long[] newDim = new long[] { imageOrig.dimension(0), imageOrig.dimension(1),2, imageOrig.dimension(2), imageOrig.dimension(3) };
		final Img<FloatType> out = new CellImgFactory<FloatType>(new FloatType()).create(newDim);
		ImgPlus<FloatType> output = new ImgPlus<FloatType>(out);
		RandomAccessibleInterval<FloatType> channelRaw = Views.hyperSlice(output, 2, 0);
    	RandomAccessibleInterval<FloatType> channelSeg = Views.hyperSlice(output, 2, 1);
		LoopBuilder.setImages(imageOrig, imageSeg, channelRaw, channelSeg).multiThreaded().forEachPixel(
			    (a, b, r, g) -> {
			    	
			            r.set(a.get());
			            g.set(b.get());
			        
			    }
			);
		
		return output;
		
	}
	
	public static ImgPlus<FloatType> createmaskedHyperStack(ImagePlus imp, ImagePlus impSeg, ImagePlus impMask) {
		
		RandomAccessibleInterval<FloatType> imageOrig = SimplifiedIO.openImage(
				imp.getOriginalFileInfo().directory + imp.getOriginalFileInfo().fileName, new FloatType());
		
		RandomAccessibleInterval<IntType> imageSeg = SimplifiedIO.openImage(
				impSeg.getOriginalFileInfo().directory + impSeg.getOriginalFileInfo().fileName, new IntType());
		
		RandomAccessibleInterval<IntType> imageMask = SimplifiedIO.openImage(
				impMask.getOriginalFileInfo().directory + impMask.getOriginalFileInfo().fileName, new IntType());
		
		if(imageSeg.numDimensions() > imageMask.numDimensions()) {
			
			imageMask = MaskUtils.copyUpIntImage(imageMask, imageSeg);
		}
		

		net.imglib2.Cursor<IntType> Bigcursor = Views.iterable(imageMask).localizingCursor();
		
		RandomAccessibleInterval<IntType> ClearedimageSeg = new CellImgFactory<IntType>(new IntType()).create(imageSeg);
		RandomAccess<IntType> oldsegimage = imageSeg.randomAccess();
		RandomAccess<IntType> segimage = ClearedimageSeg.randomAccess();
		
		while(Bigcursor.hasNext()) {
			
			Bigcursor.fwd();
			segimage.setPosition(Bigcursor);
			oldsegimage.setPosition(Bigcursor);
			if(Bigcursor.get().get() == 0 ) 
				
				segimage.get().setZero();
			else
				segimage.get().set(oldsegimage.get());
			
		}
		long[] newDim = new long[] { imageOrig.dimension(0), imageOrig.dimension(1),2, imageOrig.dimension(2), imageOrig.dimension(3) };
		final Img<FloatType> out = new CellImgFactory<FloatType>(new FloatType()).create(newDim);
		ImgPlus<FloatType> output = new ImgPlus<FloatType>(out);
		RandomAccessibleInterval<FloatType> channelRaw = Views.hyperSlice(output, 2, 0);
    	RandomAccessibleInterval<FloatType> channelSeg = Views.hyperSlice(output, 2, 1);
		LoopBuilder.setImages(imageOrig, ClearedimageSeg, channelRaw, channelSeg).multiThreaded().forEachPixel(
			    (a, b, r, g) -> {
			    	
			            r.set(a.get());
			            g.set(b.get());
			        
			    }
			);
		
		return output;
		
	}
	
	

}
