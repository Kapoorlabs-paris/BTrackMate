package pluginTools;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;

import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Cellobject;
import budDetector.Roiobject;
import fiji.plugin.globalbtrack.gui.components.CovistoKalmanPanel;
import fiji.plugin.globalbtrack.segmentation.CellSegmentation;
import fiji.plugin.globalbtrackmate.Spot;
import fiji.plugin.globalbtrackmate.SpotCollection;
import fileListeners.BTrackSaveDirectoryListener;
import net.imagej.ImageJ;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.plugin.PlugIn;
import listeners.AddBudKeyListener;
import listeners.BTrackAutoEndListener;
import listeners.BTrackAutoStartListener;
import listeners.BTrackFilenameListener;
import listeners.BudAlphaListener;
import listeners.BudCheckpointListener;
import listeners.BudLinkobjectListener;
import listeners.BudMastadonListener;
import listeners.BudPREIniSearchListener;
import listeners.BudPRELostFrameListener;
import listeners.BudPREMaxSearchTListener;
import listeners.BudRestartListener;
import listeners.BudSaveAllListener;
import listeners.BudSaveBatchListener;
import listeners.BudSaveListener;
import listeners.BudSkeletonListener;
import listeners.BudSkeletonTrackLengthListener;
import listeners.BudTimeListener;
import listeners.BudTlocListener;
import listeners.BudTrackidListener;
import listeners.BudZListener;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import tracker.BUDDYBudTrackModel;
import tracker.BUDDYCostFunction;
import tracker.BUDDYDisplaySelectedTrack;
import tracker.BUDDYTrackModel;

public class InteractiveBud extends JPanel implements PlugIn {

	private static final long serialVersionUID = 1L;
	public String usefolder = IJ.getDirectory("imagej");
	public String addToName = "BTrack_";
	public String inputstring;
	public final int scrollbarSize = 1000;
	public Set<Integer> pixellist;
	public NumberFormat nf;
	public Color Drawcolor;
	public RandomAccessibleInterval<FloatType> originalimg;
	public RandomAccessibleInterval<FloatType> originalSecimg;
	public RandomAccessibleInterval<IntType> Segoriginalimg;
	public RandomAccessibleInterval<FloatType> SegSecoriginalimg;
	public RandomAccessibleInterval<FloatType> CurrentView;
	public RandomAccessibleInterval<IntType> CurrentViewInt;
	public RandomAccessibleInterval<IntType> CurrentViewYellowInt;
	public HashMap<String, ArrayList<Roiobject>> BudOvalRois = new HashMap<String, ArrayList<Roiobject>>();
	public final String NameA;
	public int ndims;
	public MouseListener mvl;
	public KeyListener kvl;
	public MouseListener tvl;
	public String AddDot = "B";
	public MouseMotionListener tvml;
	public int borderexpand = 20;
	public int BudDotsize = 6;
	public Color BudColor = Color.PINK;
	public Color BudBeforeColor = Color.YELLOW;
	public Color BudSplitColor = Color.BLUE;
	public Color RemoveBudColor = Color.RED;

	public HashMap<String, ArrayList<Budpointobject>> AllBudpoints;
	public HashMap<String, ArrayList<Budobject>> AllBuds;
	public HashMap<String, Integer> BudLastTime;
	public BUDDYCostFunction<Budpointobject, Budpointobject> UserchosenCostFunction;
	public BUDDYCostFunction<Budobject, Budobject> BudUserchosenCostFunction;
	public int[] Clickedpoints;
	public HashMap<String, Integer> AccountedT;
	public ArrayList<ValuePair<String, Budpointobject>> Tracklist;
	public HashMap<String, Double> TrackMeanVelocitylist;
	public HashMap<String, Double> TrackMaxVelocitylist;
	public HashMap<Integer, HashMap<Integer, Double>> BudVelocityMap;

	public ArrayList<ValuePair<String, Budobject>> BudTracklist;
	public HashMap<String, ArrayList<Spot>> AllBudcells;
	public Overlay overlay;
	public ImagePlus imp;
	public String selectedID;
	public boolean mouseremoved = false;
	public boolean BudAnalysis = true;
	public int row;
	public int tablesize;
	public RealLocalizable Refcord;
	public HashMap<String, RealLocalizable> AllRefcords;
	public ArrayList<RealLocalizable> AllBudcenter;
	public ArrayList<RealLocalizable> ChosenBudcenter;
	public HashMap<String, RealLocalizable> SelectedAllRefcords;
	public int thirdDimension;
	public int fourthDimension;
	public BUDDYTrackModel Globalmodel;
	public BUDDYBudTrackModel BudGlobalModel;
	public int thirdDimensionSize;
	public ImagePlus impA;
	public int rowchoice;
	public Frame jFreeChartFrameRate;
	public int maxframegap = 30;
	public int thirdDimensionslider = 1;
	public int thirdDimensionsliderInit = 1;
	public int fourthDimensionslider = 1;
	public int fourthDimensionsliderInit = 1;
	public int fourthDimensionSize;
	public JProgressBar jpb;
	public ArrayList<int[]> ZTRois;
	public HashMap<Integer, ArrayList<Cellobject>> CSVGreen = new HashMap<Integer, ArrayList<Cellobject>>();
	public MouseMotionListener ml;
	public ImagePlus resultimp;
	public ImageJ ij;
	public double calibrationX;
	public double calibrationY;
	public double calibrationZ;
	public double timecal;
	public File saveFile;
	public final File defaultDirectory;
	public RandomAccessibleInterval<IntType> SegYelloworiginalimg;
	public RandomAccessibleInterval<IntType> SegRedoriginalimg;
	public RandomAccessibleInterval<IntType> SegGreenoriginalimg;
	public SpotCollection budcells = new SpotCollection();
	public HashMap<Integer, Integer> IDlist = new HashMap<Integer, Integer>();
	public HashMap<String, Budpointobject> Finalresult;

	// Input Bud and its segmentation
	public InteractiveBud(final RandomAccessibleInterval<FloatType> originalimg,
			final RandomAccessibleInterval<IntType> Segoriginalimg,
			final HashMap<String, ArrayList<Roiobject>> BudOvalRois, final File defaultDirectory, final String NameA,
			final double calibrationX, double calibrationY, final double timecal, String inputstring,
			Boolean BudAnalysis) {

		this.originalimg = originalimg;
		this.Segoriginalimg = Segoriginalimg;
		this.NameA = NameA;
		this.calibrationX = calibrationX;
		this.calibrationY = calibrationY;
		this.BudOvalRois = BudOvalRois;
		this.calibrationZ = 1;
		this.defaultDirectory = defaultDirectory;
		this.timecal = timecal;
		this.ndims = originalimg.numDimensions();
		this.inputstring = inputstring;
		this.BudAnalysis = BudAnalysis;

	}

	// Input RGB and one flourescent channel segmentation images
	public InteractiveBud(final RandomAccessibleInterval<FloatType> originalimg,
			final RandomAccessibleInterval<IntType> Segoriginalimg,
			final RandomAccessibleInterval<IntType> SegYelloworiginalimg, final File defaultDirectory,
			final String NameA, final double calibrationX, final double calibrationY, final double timecal,
			String inputstring) {

		this.originalimg = originalimg;
		this.Segoriginalimg = Segoriginalimg;
		this.SegYelloworiginalimg = SegYelloworiginalimg;
		this.NameA = NameA;
		this.calibrationX = calibrationX;
		this.calibrationY = calibrationY;
		this.defaultDirectory = defaultDirectory;
		this.calibrationZ = 1;
		this.timecal = timecal;
		this.ndims = originalimg.numDimensions();
		this.inputstring = inputstring;

	}

	// Input Image and one flourescent channel and mask images
	public InteractiveBud(final RandomAccessibleInterval<FloatType> originalimg,
			final HashMap<Integer, ArrayList<Cellobject>> CSVGreen,
			final RandomAccessibleInterval<IntType> Segoriginalimg,
			final RandomAccessibleInterval<IntType> SegYelloworiginalimg, final File defaultDirectory,
			final String NameA, final double calibrationX, final double calibrationY, final double calibrationZ,
			final double timecal, String inputstring, Boolean BudAnalysis) {

		this.originalimg = originalimg;
		this.Segoriginalimg = Segoriginalimg;
		this.SegYelloworiginalimg = SegYelloworiginalimg;
		this.NameA = NameA;
		this.CSVGreen = CSVGreen;
		this.calibrationX = calibrationX;
		this.calibrationY = calibrationY;
		this.defaultDirectory = defaultDirectory;
		this.calibrationZ = calibrationZ;
		this.timecal = timecal;
		this.ndims = originalimg.numDimensions();
		this.inputstring = inputstring;
		this.BudAnalysis = BudAnalysis;

	}

	public InteractiveBud(final RandomAccessibleInterval<FloatType> originalimg,
			final HashMap<Integer, ArrayList<Cellobject>> CSVGreen,
			final RandomAccessibleInterval<IntType> SegYelloworiginalimg, final File defaultDirectory,
			final String NameA, final double calibrationX, final double calibrationY, final double calibrationZ,
			final double timecal, String inputstring, Boolean BudAnalysis) {

		this.originalimg = originalimg;
		this.SegYelloworiginalimg = SegYelloworiginalimg;
		this.NameA = NameA;
		this.CSVGreen = CSVGreen;
		this.calibrationX = calibrationX;
		this.calibrationY = calibrationY;
		this.defaultDirectory = defaultDirectory;
		this.calibrationZ = calibrationZ;
		this.timecal = timecal;
		this.ndims = originalimg.numDimensions();
		this.inputstring = inputstring;
		this.BudAnalysis = BudAnalysis;

	}

	public ImageStack prestack;
	public JTable table;
	public JTableHeader header;

	public static enum ValueChange {

		THIRDDIMmouse, All;

	}

	public void setTime(final int value) {
		thirdDimensionslider = value;
		thirdDimensionsliderInit = 1;
		thirdDimension = 1;
	}

	public void setZ(final int value) {
		fourthDimensionslider = value;
		fourthDimensionsliderInit = 1;
		fourthDimension = 1;
	}

	public int getTimeMax() {

		return thirdDimensionSize;
	}

	@Override
	public void run(String arg0) {

		BudLastTime = new HashMap<String, Integer>();
		AllRefcords = new HashMap<String, RealLocalizable>();
		AllBudcenter = new ArrayList<RealLocalizable>();
		ChosenBudcenter = new ArrayList<RealLocalizable>();
		Finalresult = new HashMap<String, Budpointobject>();

		ZTRois = new ArrayList<int[]>();
		BudVelocityMap = new HashMap<Integer, HashMap<Integer, Double>>();
		SelectedAllRefcords = new HashMap<String, RealLocalizable>();
		AccountedT = new HashMap<String, Integer>();
		jpb = new JProgressBar();
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(false);
		Clickedpoints = new int[2];
		pixellist = new HashSet<Integer>();
		Tracklist = new ArrayList<ValuePair<String, Budpointobject>>();
		TrackMeanVelocitylist = new HashMap<String, Double>();
		TrackMaxVelocitylist = new HashMap<String, Double>();
		BudTracklist = new ArrayList<ValuePair<String, Budobject>>();
		AllBudpoints = new HashMap<String, ArrayList<Budpointobject>>();
		AllBuds = new HashMap<String, ArrayList<Budobject>>();
		ij = new ImageJ();
		ij.ui().showUI();
		if (ndims == 3) {

			thirdDimension = 1;

			thirdDimensionSize = (int) originalimg.dimension(2);
			AutostartTime = thirdDimension;
			AutoendTime = thirdDimensionSize;
			maxframegap = thirdDimensionSize / 4;

			setTime(thirdDimension);
			CurrentView = utility.BudSlicer.getCurrentBudView(originalimg, thirdDimension, thirdDimensionSize);
			if (Segoriginalimg != null)
				CurrentViewInt = utility.BudSlicer.getCurrentBudView(Segoriginalimg, thirdDimension,
						thirdDimensionSize);

			if (SegYelloworiginalimg != null) {
				CurrentViewYellowInt = utility.BudSlicer.getCurrentBudView(SegYelloworiginalimg, thirdDimension,
						thirdDimensionSize);

			}
			imp = ImageJFunctions.show(CurrentView, "Original Image");
			imp.setTitle("Active Image" + " " + "time point : " + thirdDimension);

			if (BudAnalysis) {

				StartDisplayer();
				Card();

			}

		}

		if (ndims > 3) {

			thirdDimension = 1;

			thirdDimensionSize = (int) originalimg.dimension(2);

			fourthDimension = 1;

			fourthDimensionSize = (int) originalimg.dimension(3);

			setZ(thirdDimension);
			setTime(fourthDimension);

			CurrentView = utility.BudSlicer.getCurrentGreenView(originalimg, thirdDimension, thirdDimensionSize,
					fourthDimension, fourthDimensionSize);
			if (Segoriginalimg != null)
				CurrentViewInt = utility.BudSlicer.getCurrentGreenView(Segoriginalimg, thirdDimension,
						thirdDimensionSize, fourthDimension, fourthDimensionSize);

			if (SegYelloworiginalimg != null) {
				CurrentViewYellowInt = utility.BudSlicer.getCurrentGreenView(SegYelloworiginalimg, thirdDimension,
						thirdDimensionSize, fourthDimension, fourthDimensionSize);

			}
			imp = ImageJFunctions.show(CurrentView, "Original Image");

			imp.setTitle("Active Image" + " " + "time point : " + fourthDimension);

			GreenCellCollector();

		}

		Cardframe.repaint();
		Cardframe.validate();
		panelFirst.repaint();
		panelFirst.validate();
		saveFile = new java.io.File(".");
		// Get Labelled images

	}

	public void updatePreview(final ValueChange change) {

		if (overlay == null) {

			overlay = new Overlay();
			imp.setOverlay(overlay);

		}

		if (change == ValueChange.THIRDDIMmouse) {

			// create a view on the source with this interval

			if (ndims == 3) {
				imp.setTitle("Active Image" + " " + "time point : " + thirdDimension);
				String TID = Integer.toString(thirdDimension);
				AccountedT.put(TID, thirdDimension);
				CurrentView = utility.BudSlicer.getCurrentBudView(originalimg, thirdDimension, thirdDimensionSize);
				if (Segoriginalimg != null)
					CurrentViewInt = utility.BudSlicer.getCurrentBudView(Segoriginalimg, thirdDimension,
							thirdDimensionSize);
				else {

					CurrentViewInt = null;
				}

				if (SegYelloworiginalimg != null) {
					CurrentViewYellowInt = utility.BudSlicer.getCurrentBudView(SegYelloworiginalimg, thirdDimension,
							thirdDimensionSize);
				}
				repaintView(CurrentView);
				if (BudAnalysis) {
					if (CovistoKalmanPanel.Skeletontime.isEnabled()) {
						imp.getOverlay().clear();
						imp.updateAndDraw();

						StartDisplayer();

					}
				}

				else {

					imp.getOverlay().clear();
					imp.updateAndDraw();

				}
			}

			if (ndims > 3) {
				imp.setTitle("Active Image" + " " + "time point : " + fourthDimension);
				String TID = Integer.toString(fourthDimension);
				AccountedT.put(TID, fourthDimension);

				CurrentView = utility.BudSlicer.getCurrentGreenView(originalimg, thirdDimension, thirdDimensionSize,
						fourthDimension, fourthDimensionSize);
				if (Segoriginalimg != null)
					CurrentViewInt = utility.BudSlicer.getCurrentGreenView(Segoriginalimg, thirdDimension,
							thirdDimensionSize, fourthDimension, fourthDimensionSize);
				else {

					CurrentViewInt = null;
				}

				if (SegYelloworiginalimg != null) {
					CurrentViewYellowInt = utility.BudSlicer.getCurrentGreenView(SegYelloworiginalimg, thirdDimension,
							thirdDimensionSize, fourthDimension, fourthDimensionSize);

				}
				repaintView(CurrentView);
			}

		}

	}

	public void StartDisplayer() {

		BUDDYDisplaySelectedTrack.Select(this, null);
		BUDDYDisplaySelectedTrack.Mark(this, null);

		ComputeBorder display = new ComputeBorder(this, jpb);

		display.execute();
	}

	public void GreenCellCollector() {

		CellSegmentation display = new CellSegmentation(this, jpb);
		display.execute();
	}

	public void repaintView(RandomAccessibleInterval<FloatType> Activeimage) {

		imp.getCanvas().addKeyListener(new AddBudKeyListener(this));
		IJ.selectWindow(imp.getTitle());
		if (imp == null || !imp.isVisible()) {
			imp = ImageJFunctions.show(Activeimage);

		}

		else {

			final float[] pixels = (float[]) imp.getProcessor().getPixels();

			final Cursor<FloatType> c = Views.iterable(Activeimage).cursor();

			for (int i = 0; i < pixels.length; ++i)

				pixels[i] = c.next().get();

			imp.updateAndDraw();

		}

	}

	public JFrame Cardframe = new JFrame("Bud n Cell Tracker");
	public JPanel panelFirst = new JPanel();
	public JPanel Original = new JPanel();
	public JPanel PanelSelectFile = new JPanel();
	public JPanel Timeselect = new JPanel();
	public JPanel KalmanPanel = new JPanel();
	public JPanel panelCont = new JPanel();
	public final Insets insets = new Insets(10, 0, 0, 0);
	public final GridBagLayout layout = new GridBagLayout();
	public final GridBagConstraints c = new GridBagConstraints();
	public JScrollPane scrollPane;
	public JFileChooser chooserA = new JFileChooser();
	public String choosertitleA;
	int SizeX = 400;
	int SizeY = 200;
	public Border selectfile = new CompoundBorder(new TitledBorder("Select Track"), new EmptyBorder(c.insets));
	public Label autoTstart, autoTend;
	public TextField startT, endT;
	public Label timeText = new Label("Current T = " + 1, Label.CENTER);

	public Label inputZ = new Label("Current Z = " + 1, Label.CENTER);

	public Label thirdexplain = new Label("Press Esc on active image to stop calculation", Label.CENTER);
	public Label fourthexplain = new Label("Click Skeletonize buddies after selection", Label.CENTER);
	public Label fifthexplain = new Label("Left click to remove/re-add point. Left+a to make a new point",
			Label.CENTER);
	public String timestring = "Current T";
	public String Zstring = "Current Z";

	int textwidth = 5;
	public int AutostartTime, AutoendTime;
	public TextField inputFieldT;
	public JScrollBar timeslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0,
			scrollbarSize + 10);

	public JScrollBar Zslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0,
			scrollbarSize + 10);
	public JButton Savebutton = new JButton("Save Track");
	public JButton Cellbutton = new JButton("Enter BTrackmate");
	public JButton Restartbutton = new JButton("Restart");
	public JButton Checkpointbutton = new JButton("Checkpoint Save");
	public JButton Batchbutton = new JButton("Save Parameters for batch mode and exit");
	public JButton SaveAllbutton = new JButton("Save All Tracks");
	public Border timeborder = new CompoundBorder(new TitledBorder("Select time"), new EmptyBorder(c.insets));
	public Label inputtrackLabel;
	public TextField inputtrackField;
	public Border selectcell = new CompoundBorder(new TitledBorder("Select Cell"), new EmptyBorder(c.insets));
	public TextField inputField = new TextField();
	public Border origborder = new CompoundBorder(new TitledBorder("Results files"), new EmptyBorder(c.insets));
	public final JButton ChooseDirectory = new JButton("Choose Directory to save results in");

	public void Card() {

		Cellbutton.setEnabled(false);
		CardLayout cl = new CardLayout();

		c.insets = new Insets(5, 5, 5, 5);

		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 35;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");

		inputtrackLabel = new Label("Enter trackID to save");
		inputtrackField = new TextField(textwidth);

		Object[] colnames = new Object[] { "Track Id", "Location X", "Location Y", "Location T", "Growth Rate" };

		Object[][] rowvalues = new Object[0][colnames.length];
		if (Finalresult != null && Finalresult.size() > 0) {

			rowvalues = new Object[Finalresult.size()][colnames.length];

		}

		table = new JTable(rowvalues, colnames);
		header = table.getTableHeader();
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		autoTend = new Label("End time for tracking");
		endT = new TextField(textwidth);
		endT.setText(Integer.toString(AutoendTime));

		scrollPane = new JScrollPane(table);
		Original.setLayout(layout);
		scrollPane.getViewport().add(table);
		scrollPane.setAutoscrolls(true);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		PanelSelectFile.add(scrollPane, BorderLayout.CENTER);

		PanelSelectFile.setBorder(selectfile);
		int size = 100;
		table.getColumnModel().getColumn(0).setPreferredWidth(size);
		table.getColumnModel().getColumn(1).setPreferredWidth(size);
		table.getColumnModel().getColumn(2).setPreferredWidth(size);
		table.getColumnModel().getColumn(3).setPreferredWidth(size);
		table.getColumnModel().getColumn(4).setPreferredWidth(size);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(true);
		table.isOpaque();
		scrollPane.setMinimumSize(new Dimension(300, 200));
		scrollPane.setPreferredSize(new Dimension(300, 200));

		panelFirst.setLayout(layout);
		overlay = imp.getOverlay();
		if (overlay == null) {

			overlay = new Overlay();
			imp.setOverlay(overlay);

		}

		autoTstart = new Label("Start time for tracking");
		startT = new TextField(textwidth);
		startT.setText(Integer.toString(AutostartTime));

		autoTend = new Label("End time for tracking");
		endT = new TextField(textwidth);
		endT.setText(Integer.toString(AutoendTime));

		Timeselect.setLayout(layout);
		inputFieldT = new TextField(textwidth);
		inputFieldT.setText(Integer.toString(thirdDimension));

		// Put time slider

		Timeselect.add(timeText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Timeselect.add(timeslider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Timeselect.add(inputFieldT, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Timeselect.add(autoTstart, new GridBagConstraints(2, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Timeselect.add(startT, new GridBagConstraints(2, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Timeselect.add(autoTend, new GridBagConstraints(2, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Timeselect.add(endT, new GridBagConstraints(2, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Timeselect.add(thirdexplain, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		Timeselect.add(fourthexplain, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		Timeselect.add(fifthexplain, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		Timeselect.setBorder(timeborder);

		panelFirst.add(Timeselect, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		KalmanPanel = CovistoKalmanPanel.KalmanPanel();

		panelFirst.add(KalmanPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		if (SegYelloworiginalimg != null || SegRedoriginalimg != null || SegGreenoriginalimg != null)
			panelFirst.add(Cellbutton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		panelFirst.add(PanelSelectFile, new GridBagConstraints(3, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(inputtrackLabel, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(inputtrackField, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(ChooseDirectory, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Original.add(Savebutton, new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(SaveAllbutton, new GridBagConstraints(0, 9, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(Checkpointbutton, new GridBagConstraints(0, 10, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.setBorder(origborder);

		panelFirst.add(Original, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		inputFieldT.addTextListener(new BudTlocListener(this, false));

		timeslider.addAdjustmentListener(new BudTimeListener(this, timeText, timestring, thirdDimensionsliderInit,
				thirdDimensionSize, scrollbarSize, timeslider));

		Zslider.addAdjustmentListener(new BudZListener(this, inputZ, Zstring, thirdDimensionsliderInit,
				thirdDimensionSize, scrollbarSize, Zslider));

		endT.addTextListener(new BTrackAutoEndListener(this));
		startT.addTextListener(new BTrackAutoStartListener(this));
		CovistoKalmanPanel.Skeletontime.addActionListener(new BudSkeletonListener(this));
		CovistoKalmanPanel.Restart.addActionListener(new BudRestartListener(this));
		CovistoKalmanPanel.Timetrack.addActionListener(new BudLinkobjectListener(this));
		CovistoKalmanPanel.lostframe.addTextListener(new BudPRELostFrameListener(this));
		CovistoKalmanPanel.tracklength.addTextListener(new BudSkeletonTrackLengthListener(this));

		CovistoKalmanPanel.maxSearchKalman.addAdjustmentListener(new BudPREMaxSearchTListener(this,
				CovistoKalmanPanel.maxSearchTextKalman, CovistoKalmanPanel.maxSearchstringKalman,
				CovistoKalmanPanel.maxSearchradiusMin, CovistoKalmanPanel.maxSearchradiusMax,
				CovistoKalmanPanel.scrollbarSize, CovistoKalmanPanel.maxSearchSS));
		CovistoKalmanPanel.setInitialsearchradius((float) (CovistoKalmanPanel.initialSearchradius / calibrationX));

		CovistoKalmanPanel.initialSearchS.addAdjustmentListener(new BudPREIniSearchListener(this,
				CovistoKalmanPanel.iniSearchText, CovistoKalmanPanel.initialSearchstring,
				CovistoKalmanPanel.initialSearchradiusMin, CovistoKalmanPanel.initialSearchradiusMax,
				CovistoKalmanPanel.scrollbarSize, CovistoKalmanPanel.initialSearchS));
		CovistoKalmanPanel.alphaS.addAdjustmentListener(new BudAlphaListener(this, CovistoKalmanPanel.alphaText,
				CovistoKalmanPanel.alphastring, CovistoKalmanPanel.alphaMin, CovistoKalmanPanel.alphaMax,
				CovistoKalmanPanel.scrollbarSize, CovistoKalmanPanel.alphaS));
		CovistoKalmanPanel.setInitialAlpha(CovistoKalmanPanel.alphaInit);
		KalmanPanel.validate();
		KalmanPanel.repaint();
		inputtrackField.addTextListener(new BudTrackidListener(this));
		Batchbutton.addActionListener(new BudSaveBatchListener(this));
		inputField.addTextListener(new BTrackFilenameListener(this));
		Savebutton.addActionListener(new BudSaveListener(this));
		Checkpointbutton.addActionListener(new BudCheckpointListener(this));
		Cellbutton.addActionListener(new BudMastadonListener(this));
		SaveAllbutton.addActionListener(new BudSaveAllListener(this));
		ChooseDirectory.addActionListener(new BTrackSaveDirectoryListener(this));
		inputField.addTextListener(new BTrackFilenameListener(this));

		panelFirst.setVisible(true);
		cl.show(panelCont, "1");
		Cardframe.add(panelCont, "Center");
		Cardframe.add(jpb, "Last");

		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Cardframe.pack();
		Cardframe.setVisible(true);

		imp.getCanvas().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 27)
					EscapePressed = true;

			}

		});

	}

	public Boolean EscapePressed = false;

}