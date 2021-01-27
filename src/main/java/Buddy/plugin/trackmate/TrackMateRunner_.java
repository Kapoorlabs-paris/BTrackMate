package Buddy.plugin.trackmate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Buddy.plugin.trackmate.action.ExportTracksToXML;
import Buddy.plugin.trackmate.features.FeatureFilter;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;
import Buddy.plugin.trackmate.features.edges.EdgeAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackBranchingAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackIndexAnalyzer;
import Buddy.plugin.trackmate.gui.GuiUtils;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import Buddy.plugin.trackmate.io.TmXmlWriter;
import Buddy.plugin.trackmate.providers.EdgeAnalyzerProvider;
import Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackAnalyzerProvider;
import Buddy.plugin.trackmate.tracking.TrackerKeys;
import Buddy.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;
import Buddy.plugin.trackmate.util.LogRecorder;
import Buddy.plugin.trackmate.util.TMUtils;
import Buddy.plugin.trackmate.visualization.PerTrackFeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import Buddy.plugin.trackmate.visualization.ViewFactory;
import Buddy.plugin.trackmate.visualization.hyperstack.HyperStackDisplayerFactory;
import fiji.util.SplitString;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import net.imglib2.util.ValuePair;
import pluginTools.InteractiveBud;

public class TrackMateRunner_ extends TrackMatePlugIn_ {

	/*
	 * List of arguments usable in the macro.
	 */
	final InteractiveBud parent;

	public TrackMateRunner_(InteractiveBud parent) {
		super(parent);
		this.parent = parent;
	}

	/**
	 * The macro parameter to set the detection radius of particles. Accept double
	 * values in physical units.
	 */
	private static final String ARG_RADIUS = "radius";

	/**
	 * The macro parameter to set the quality threshold of the detector. Accept
	 * double values.
	 */
	private static final String ARG_THRESHOLD = "threshold";

	/**
	 * The macro parameter to set whether we do sub-pixel particle localization.
	 * Accept boolean values.
	 */
	private static final String ARG_SUBPIXEL = "subpixel";

	/**
	 * The macro parameter to set whether we pre-process the input image with a 3x3
	 * median filter. Accept boolean values.
	 */
	private static final String ARG_MEDIAN = "median";

	/**
	 * The macro parameter to set what channel in the input image to operate on.
	 * Accept integer values.
	 */
	private static final String ARG_CHANNEL = "channel";

	/**
	 * The macro parameter to set what is the maximal frame-to-frame linking
	 * distance. Accept double values in physical units.
	 */
	private static final String ARG_MAX_DISTANCE = "max_distance";

	/**
	 * The macro parameter to set what is the maximal gap-closing distance. Accept
	 * double values in physical units.
	 */
	private static final String ARG_MAX_GAP_DISTANCE = "max_gap_distance";

	/**
	 * The macro parameter to set what is the maximal acceptable frame gap when
	 * doing gap closing. Accept integer values.
	 */
	private static final String ARG_MAX_GAP_FRAMES = "max_frame_gap";

	/**
	 * The macro parameter to set whether we should launch the GUI to perform
	 * tracking. The other parameters entered with macro argument will be used as
	 * default.
	 */
	private static final String ARG_USE_GUI = "use_gui";

	/**
	 * The macro parameter to set the input image, identified by its ImageJ ID or
	 * number. See {@link WindowManager#getImage(int)}.
	 */
	private static final String ARG_INPUT_IMAGE_ID = "image_id";

	/**
	 * The macro parameter to set the input image, identified by its name. See
	 * {@link WindowManager#getImage(String)}.
	 */
	private static final String ARG_INPUT_IMAGE_NAME = "image_name";

	/**
	 * The macro parameter to set the input file, identified by its path. Use an
	 * empty path to open a dialog. See {@link IJ#openImage(String)}.
	 */
	private static final String ARG_INPUT_IMAGE_PATH = "image_path";

	/**
	 * The macro parameter to set the path to the XML file for saving to a TrackMate
	 * file, once the tracking process has completed. Is ignored if the
	 * {@link #ARG_USE_GUI} is set to <code>true</code>.
	 */
	private static final String ARG_SAVE_TO = "save_to";

	/**
	 * The macro parameter to set the path to export to simplified XML. See
	 * {@link Buddy.plugin.trackmate.action.ExportTracksToXML}. Is ignored if the
	 * {@link #ARG_USE_GUI} is set to <code>true</code>.
	 */
	private static final String ARG_EXPORT_TO = "export_to";

	/**
	 * The macro parameter to set whether we should display results after
	 * processing. Accept boolean values. Is ignored if the {@link #ARG_USE_GUI} is
	 * set to <code>true</code>.
	 */
	private static final String ARG_DISPLAY_RESULTS = "display_results";

	/**
	 * The macro parameter to set the track filter value on the number of
	 * BCellobjects in tracks. If used, tracks made of less BCellobjects than the
	 * specified value will be filtered out.
	 */
	private static final String ARG_FILTER_TRACKS_NBCellobjectS_ABOVE = "filter_tracks_nBCellobjects_above";

	/**
	 * The collection of supported macro parameters.
	 */
	private static final Collection<String> SUPPORTED_ARGS = new ArrayList<>();

	static {
		SUPPORTED_ARGS.add(ARG_CHANNEL);
		SUPPORTED_ARGS.add(ARG_DISPLAY_RESULTS);
		SUPPORTED_ARGS.add(ARG_EXPORT_TO);
		SUPPORTED_ARGS.add(ARG_INPUT_IMAGE_ID);
		SUPPORTED_ARGS.add(ARG_INPUT_IMAGE_NAME);
		SUPPORTED_ARGS.add(ARG_INPUT_IMAGE_PATH);
		SUPPORTED_ARGS.add(ARG_MAX_DISTANCE);
		SUPPORTED_ARGS.add(ARG_MAX_GAP_DISTANCE);
		SUPPORTED_ARGS.add(ARG_MAX_GAP_FRAMES);
		SUPPORTED_ARGS.add(ARG_MEDIAN);
		SUPPORTED_ARGS.add(ARG_SAVE_TO);
		SUPPORTED_ARGS.add(ARG_SUBPIXEL);
		SUPPORTED_ARGS.add(ARG_THRESHOLD);
		SUPPORTED_ARGS.add(ARG_USE_GUI);
		SUPPORTED_ARGS.add(ARG_RADIUS);
		SUPPORTED_ARGS.add(ARG_FILTER_TRACKS_NBCellobjectS_ABOVE);
	}

	/*
	 * Other fields
	 */

	private Logger logger = new LogRecorder(Logger.DEFAULT_LOGGER);

	@Override
	public void run(String arg) {

		logger = new LogRecorder(Logger.IJ_LOGGER);

		/*
		 * Parse macro arguments.
		 */

		if (null == arg || arg.isEmpty()) {
			final String macroOption = Macro.getOptions();
			if (null != macroOption) {
				arg = macroOption;
			}
		}

		if (null != arg && !arg.isEmpty()) {

			try {
				final Map<String, String> macroOptions = SplitString.splitMacroOptions(arg);

				/*
				 * Unknown parameters.
				 */
				final Set<String> unknownParameters = new HashSet<>(macroOptions.keySet());
				unknownParameters.removeAll(SUPPORTED_ARGS);
				if (!unknownParameters.isEmpty()) {
					logger.error("The following parameters are unkown and were ignored:\n");
					for (final String unknownParameter : unknownParameters) {
						logger.error("  " + unknownParameter);
					}
				}

				/*
				 * Find what we will operate on: An opened image? A file?
				 */

				ImagePlus imp;
				if (macroOptions.containsKey(ARG_INPUT_IMAGE_ID)) {
					final String val = macroOptions.get(ARG_INPUT_IMAGE_ID);
					try {
						final int id = Integer.parseInt(val);
						imp = WindowManager.getImage(id);
						if (null == imp) {
							logger.error("There is not an opened image with ID " + id + ".\n");
							return;
						}
					} catch (final NumberFormatException nfe) {
						logger.error("Could not parse the image ID set by the " + ARG_INPUT_IMAGE_ID + " paramter. Got "
								+ val + ", expected an integer.\n");
						return;
					}
				} else if (macroOptions.containsKey(ARG_INPUT_IMAGE_NAME)) {
					final String imageName = macroOptions.get(ARG_INPUT_IMAGE_NAME);
					imp = WindowManager.getImage(imageName);
					if (null == imp) {
						logger.error("There is not an opened image with name " + imageName + ".\n");
						return;
					}
				} else if (macroOptions.containsKey(ARG_INPUT_IMAGE_PATH)) {
					final String imagePath = macroOptions.get(ARG_INPUT_IMAGE_PATH);
					imp = new ImagePlus(imagePath);
					if (null == imp.getOriginalFileInfo()) {
						logger.error("Could not load image with path " + imagePath + ".\n");
						return;
					}
				} else {
					imp = WindowManager.getCurrentImage();
					if (null == imp) {
						logger.error("Please open an image before running TrackMate.");
						return;
					}
				}

				/*
				 * Instantiate TrackMate.
				 */

				settings = createSettings(imp);
				model = createModel();
				model.setLogger(logger);
				trackmate = createTrackMate();

				/*
				 * Configure default settings.
				 */

				final Map<String, ValuePair<String, MacroArgumentConverter>> trackerParsers = prepareTrackerParsableArguments();
				final Map<String, FilterGenerator> trackFiltersParsers = prepareTrackFiltersParsableArguments();

				// Default tracker.
				settings.trackerFactory = new SimpleSparseLAPTrackerFactory();
				settings.trackerSettings = settings.trackerFactory.getDefaultSettings();

				/*
				 * Tracker parameters.
				 */

				for (final String parameter : macroOptions.keySet()) {
					final String value = macroOptions.get(parameter);
					final ValuePair<String, MacroArgumentConverter> parser = trackerParsers.get(parameter);
					if (parser == null) {
						continue;
					}

					final String key = parser.getA();
					final MacroArgumentConverter converter = parser.getB();
					try {

						final Object val = converter.convert(value);
						settings.trackerSettings.put(key, val);
					} catch (final NumberFormatException nfe) {
						logger.error(
								"Cannot interprete value for parameter " + parameter + ": " + value + ". Skipping.\n");
						continue;
					}

				}

				/*
				 * Track filters.
				 */

				for (final String parameter : macroOptions.keySet()) {
					final String value = macroOptions.get(parameter);
					final FilterGenerator converter = trackFiltersParsers.get(parameter);
					if (converter == null) {
						continue;
					}

					try {
						final FeatureFilter featureFilter = converter.get(value);
						settings.addTrackFilter(featureFilter);
					} catch (final NumberFormatException nfe) {
						logger.error(
								"Cannot interprete value for parameter " + parameter + ": " + value + ". Skipping.\n");
						continue;
					}

				}

				/*
				 * Check if we have to use the GUI.
				 */

				if (macroOptions.containsKey(ARG_USE_GUI) && macroOptions.get(ARG_USE_GUI).equalsIgnoreCase("true")) {

					if (!imp.isVisible()) {
						imp.setOpenAsHyperStack(true);
						imp.show();
					}
					GuiUtils.userCheckImpDimensions(imp);
					final TrackMateGUIController controller = new TrackMateGUIController(parent, trackmate);
					GuiUtils.positionWindow(controller.getGUI(), imp.getWindow());
					return;
				}

				final String welcomeMessage = TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION
						+ " started on:\n" + TMUtils.getCurrentTimeString() + '\n';
				logger.log(welcomeMessage);
				if (!trackmate.checkInput() || !trackmate.process()) {
					logger.error("Error while performing tracking:\n" + trackmate.getErrorMessage());
					return;
				}

				/*
				 * Save results.
				 */

				if (macroOptions.containsKey(ARG_SAVE_TO)) {
					final String save_path_str = macroOptions.get(ARG_SAVE_TO);
					final File save_path = new File(save_path_str);
					final TmXmlWriter writer = new TmXmlWriter(save_path, logger);

					writer.appendLog(logger.toString());
					writer.appendModel(trackmate.getModel());
					writer.appendSettings(trackmate.getSettings());

					try {
						writer.writeToFile();
						logger.log("Data saved to: " + save_path.toString() + '\n');
					} catch (final FileNotFoundException e) {
						logger.error("When saving to " + save_path + ", file not found:\n" + e.getMessage() + '\n');
						return;
					} catch (final IOException e) {
						logger.error("When saving to " + save_path + ", Input/Output error:\n" + e.getMessage() + '\n');
						return;
					}

				}

				/*
				 * Export results to simplified XML.
				 */

				if (macroOptions.containsKey(ARG_EXPORT_TO)) {
					final String export_path_str = macroOptions.get(ARG_EXPORT_TO);
					final File export_path = new File(export_path_str);

					try {
						ExportTracksToXML.export(model, settings, export_path);
						logger.log("Data exported to: " + export_path.toString() + '\n');
					} catch (final FileNotFoundException e) {
						logger.error(
								"When exporting to " + export_path + ", file not found:\n" + e.getMessage() + '\n');
						return;
					} catch (final IOException e) {
						logger.error(
								"When exporting to " + export_path + ", Input/Output error:\n" + e.getMessage() + '\n');
						return;
					}

				}

				/*
				 * Display results.
				 */

				if (macroOptions.containsKey(ARG_DISPLAY_RESULTS)
						&& macroOptions.get(ARG_DISPLAY_RESULTS).equalsIgnoreCase("true")) {
					// Make image visible.
					if (!settings.imp.isVisible()) {
						settings.imp.setOpenAsHyperStack(true);
						settings.imp.show();
					}

					// Add visualization.

					final SelectionModel selectionModel = new SelectionModel(model);

					final ViewFactory displayerFactory = new HyperStackDisplayerFactory();
					final TrackMateModelView view = displayerFactory.create(parent, model, settings, selectionModel);
					final PerTrackFeatureColorGenerator trackColor = new PerTrackFeatureColorGenerator(model,
							TrackIndexAnalyzer.TRACK_INDEX);
					view.setDisplaySettings(TrackMateModelView.KEY_TRACK_COLORING, trackColor);

					/*
					 * And show GUI.
					 */

					final TrackMateGUIController controller = new TrackMateGUIController(parent, trackmate);
					// GUI position
					GuiUtils.positionWindow(controller.getGUI(), settings.imp.getWindow());

					// GUI state
					final String guiState = ConfigureViewsDescriptor.KEY;
					controller.setGUIStateString(guiState);
					controller.getGuimodel().addView(view);
					final Map<String, Object> displaySettings = controller.getGuimodel().getDisplaySettings();
					for (final String key : displaySettings.keySet()) {
						view.setDisplaySettings(key, displaySettings.get(key));
					}
					view.render();

					// Log.
					controller.getGUI().getLogPanel().setTextContent(logger.toString());

				}

			} catch (final ParseException e) {
				logger.error("Could not parse plugin option string: " + e.getMessage() + ".\n");
				e.printStackTrace();
			}
		} else {
			/*
			 * No argument. We run the GUI as is.
			 */
			super.run(arg);
		}
	}

	@Override
	protected Settings createSettings(final ImagePlus imp) {
		final Settings s = super.createSettings(imp);

		s.clearBCellobjectAnalyzerFactories();
		final BCellobjectAnalyzerProvider BCellobjectAnalyzerProvider = new BCellobjectAnalyzerProvider();
		final List<String> BCellobjectAnalyzerKeys = BCellobjectAnalyzerProvider.getKeys();
		for (final String key : BCellobjectAnalyzerKeys) {
			final BCellobjectAnalyzerFactory<?> BCellobjectFeatureAnalyzer = BCellobjectAnalyzerProvider
					.getFactory(key);
			s.addBCellobjectAnalyzerFactory(BCellobjectFeatureAnalyzer);
		}

		s.clearEdgeAnalyzers();
		final EdgeAnalyzerProvider edgeAnalyzerProvider = new EdgeAnalyzerProvider();
		final List<String> edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
		for (final String key : edgeAnalyzerKeys) {
			final EdgeAnalyzer edgeAnalyzer = edgeAnalyzerProvider.getFactory(key);
			s.addEdgeAnalyzer(edgeAnalyzer);
		}

		s.clearTrackAnalyzers();
		final TrackAnalyzerProvider trackAnalyzerProvider = new TrackAnalyzerProvider();
		final List<String> trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
		for (final String key : trackAnalyzerKeys) {
			final TrackAnalyzer trackAnalyzer = trackAnalyzerProvider.getFactory(key);
			s.addTrackAnalyzer(trackAnalyzer);
		}

		return s;
	}

	/**
	 * Prepare a map of all the arguments that are accepted by this macro for the
	 * particle-linking part.
	 *
	 * @return a map of parsers that can handle macro parameters.
	 */
	private Map<String, ValuePair<String, MacroArgumentConverter>> prepareTrackerParsableArguments() {
		// Map
		final Map<String, ValuePair<String, MacroArgumentConverter>> parsers = new HashMap<>();

		// Converters.
		final DoubleMacroArgumentConverter doubleConverter = new DoubleMacroArgumentConverter();
		final IntegerMacroArgumentConverter integerConverter = new IntegerMacroArgumentConverter();

		// Max linking distance.
		final ValuePair<String, MacroArgumentConverter> maxDistancePair = new ValuePair<>(
				TrackerKeys.KEY_LINKING_MAX_DISTANCE, doubleConverter);
		parsers.put(ARG_MAX_DISTANCE, maxDistancePair);

		// Max gap distance.
		final ValuePair<String, MacroArgumentConverter> maxGapDistancePair = new ValuePair<>(
				TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, doubleConverter);
		parsers.put(ARG_MAX_GAP_DISTANCE, maxGapDistancePair);

		// Target channel.
		final ValuePair<String, MacroArgumentConverter> maxGapFramesPair = new ValuePair<>(
				TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, integerConverter);
		parsers.put(ARG_MAX_GAP_FRAMES, maxGapFramesPair);

		return parsers;
	}

	private Map<String, FilterGenerator> prepareTrackFiltersParsableArguments() {
		// Map
		final Map<String, FilterGenerator> parsers = new HashMap<>();

		// Filter on NBCellobjects.
		final FilterGenerator nBCellobjectsFilter = new FilterAboveGenerator(
				TrackBranchingAnalyzer.NUMBER_BCellobjectS);

		parsers.put(ARG_FILTER_TRACKS_NBCellobjectS_ABOVE, nBCellobjectsFilter);
		return parsers;
	}

	/*
	 * PRIVATE CLASSES AND INTERFACES
	 */

	private static interface FilterGenerator {
		public FeatureFilter get(final String valStr) throws NumberFormatException;
	}

	private static class FilterAboveGenerator implements FilterGenerator {
		private final String feature;

		public FilterAboveGenerator(final String feature) {
			this.feature = feature;
		}

		@Override
		public FeatureFilter get(final String valStr) throws NumberFormatException {
			final double value = Double.parseDouble(valStr);
			final FeatureFilter ff = new FeatureFilter(feature, value, true);
			return ff;
		}
	}

	private static interface MacroArgumentConverter {
		public Object convert(String valStr) throws NumberFormatException;
	}

	private static final class DoubleMacroArgumentConverter implements MacroArgumentConverter {
		@Override
		public Object convert(final String valStr) throws NumberFormatException {
			return Double.valueOf(valStr);
		}
	}

	private static final class IntegerMacroArgumentConverter implements MacroArgumentConverter {
		@Override
		public Object convert(final String valStr) throws NumberFormatException {
			return Integer.valueOf(valStr);
		}
	}

	private static final class BooleanMacroArgumentConverter implements MacroArgumentConverter {
		@Override
		public Object convert(final String valStr) throws NumberFormatException {
			return Boolean.valueOf(valStr);
		}
	}

	/*
	 * MAIN METHOD
	 */

}
