package Buddy.plugin.trackmate.io;

import static Buddy.plugin.trackmate.io.IOUtils.readBooleanAttribute;
import static Buddy.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static Buddy.plugin.trackmate.io.IOUtils.readIntAttribute;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_NAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.DETECTOR_SETTINGS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FILTERED_BCellobject_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FILTERED_BCellobject_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FILTER_ABOVE_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FILTER_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FILTER_FEATURE_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FILTER_VALUE_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.FRAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_FILENAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_FOLDER_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_HEIGHT_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_NFRAMES_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_NSLICES_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_PIXEL_HEIGHT_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_PIXEL_WIDTH_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_SPATIAL_UNITS_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_TIME_INTERVAL_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_TIME_UNITS_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_VOXEL_DEPTH_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.IMAGE_WIDTH_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.INITIAL_BCellobject_FILTER_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.LOG_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.PLUGIN_VERSION_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_TEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_TSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_XEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_XSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_YEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_YSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_ZEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.SETTINGS_ZSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_COLLECTION_NBCellobjectS_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_FILTER_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_FRAME_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_ID_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_ID_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.BCellobject_NAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.TRACKER_SETTINGS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys_v20.TRACK_FILTER_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.XML_ATTRIBUTE_TRACKER_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import Buddy.plugin.trackmate.FeatureModel;
import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.features.FeatureFilter;
import Buddy.plugin.trackmate.features.edges.EdgeAnalyzer;
import Buddy.plugin.trackmate.features.edges.EdgeTargetAnalyzer;
import Buddy.plugin.trackmate.features.edges.EdgeTimeLocationAnalyzer;
import Buddy.plugin.trackmate.features.edges.EdgeVelocityAnalyzer;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;
import Buddy.plugin.trackmate.features.track.TrackAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackBranchingAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackDurationAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackIndexAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackLocationAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackSpeedStatisticsAnalyzer;
import Buddy.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import Buddy.plugin.trackmate.providers.EdgeAnalyzerProvider;
import Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackerProvider;
import Buddy.plugin.trackmate.providers.ViewProvider;
import Buddy.plugin.trackmate.tracking.BCellobjectTrackerFactory;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import Buddy.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import budDetector.BCellobject;
import ij.IJ;
import ij.ImagePlus;
import pluginTools.InteractiveBud;

public class TmXmlReader_v20 extends TmXmlReader
{

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Initialize this reader to read the file given in argument.
	 */
	public TmXmlReader_v20( final File file )
	{
		super( file );
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * @return the log text saved in the specified file, or <code>null</code> if
	 *         log text was not saved.
	 */
	public String getLogText()
	{
		final Element logElement = root.getChild( LOG_ELEMENT_KEY );
		if ( null != logElement )
			return logElement.getTextTrim();

		return "";
	}

	/**
	 * We default to the main hyperstack view.
	 */
	@Override
	public Collection< TrackMateModelView > getViews( final InteractiveBud parent, final ViewProvider provider, final Model model, final Settings settings, final SelectionModel selectionModel )
	{
		final Collection< TrackMateModelView > views = new ArrayList< >( 1 );
		final TrackMateModelView view = provider.getFactory( HyperStackDisplayer.KEY ).create( parent, model, settings, selectionModel );
		views.add( view );
		return views;
	}

	/**
	 * We default to the configure view panel.
	 */
	@Override
	public String getGUIState()
	{
		return ConfigureViewsDescriptor.KEY;
	}

	/**
	 * @return the version string stored in the file.
	 */
	@Override
	public String getVersion()
	{
		return root.getAttribute( PLUGIN_VERSION_ATTRIBUTE_NAME ).getValue();
	}

	@Override
	public String getErrorMessage()
	{
		return logger.toString();
	}

	@Override
	public Model getModel()
	{
		final Model model = new Model();

		// Physical units - fetch them from the settings element
		final Element infoEl = root.getChild( IMAGE_ELEMENT_KEY );
		String spaceUnits;
		String timeUnits;
		if ( null != infoEl )
		{
			spaceUnits = infoEl.getAttributeValue( IMAGE_SPATIAL_UNITS_ATTRIBUTE_NAME );
			timeUnits = infoEl.getAttributeValue( IMAGE_TIME_UNITS_ATTRIBUTE_NAME );
		}
		else
		{
			spaceUnits = "pixel";
			timeUnits = "frame";
		}
		model.setPhysicalUnits( spaceUnits, timeUnits );

		// Feature declaration - has to be manual declaration
		final FeatureModel fm = model.getFeatureModel();

		fm.declareEdgeFeatures( BCellobject.FEATURES, BCellobject.FEATURE_NAMES, BCellobject.FEATURE_SHORT_NAMES, BCellobject.FEATURE_DIMENSIONS, BCellobject.IS_INT );

		fm.declareEdgeFeatures( EdgeTargetAnalyzer.FEATURES, EdgeTargetAnalyzer.FEATURE_NAMES, EdgeTargetAnalyzer.FEATURE_SHORT_NAMES, EdgeTargetAnalyzer.FEATURE_DIMENSIONS, EdgeTargetAnalyzer.IS_INT );
		fm.declareEdgeFeatures( EdgeVelocityAnalyzer.FEATURES, EdgeVelocityAnalyzer.FEATURE_NAMES, EdgeVelocityAnalyzer.FEATURE_SHORT_NAMES, EdgeVelocityAnalyzer.FEATURE_DIMENSIONS, EdgeVelocityAnalyzer.IS_INT );
		fm.declareEdgeFeatures( EdgeTimeLocationAnalyzer.FEATURES, EdgeTimeLocationAnalyzer.FEATURE_NAMES, EdgeTimeLocationAnalyzer.FEATURE_SHORT_NAMES, EdgeTimeLocationAnalyzer.FEATURE_DIMENSIONS, EdgeTimeLocationAnalyzer.IS_INT );

		fm.declareTrackFeatures( TrackIndexAnalyzer.FEATURES, TrackIndexAnalyzer.FEATURE_NAMES, TrackIndexAnalyzer.FEATURE_SHORT_NAMES, TrackIndexAnalyzer.FEATURE_DIMENSIONS, TrackIndexAnalyzer.IS_INT );
		fm.declareTrackFeatures( TrackDurationAnalyzer.FEATURES, TrackDurationAnalyzer.FEATURE_NAMES, TrackDurationAnalyzer.FEATURE_SHORT_NAMES, TrackDurationAnalyzer.FEATURE_DIMENSIONS, TrackDurationAnalyzer.IS_INT );
		fm.declareTrackFeatures( TrackBranchingAnalyzer.FEATURES, TrackBranchingAnalyzer.FEATURE_NAMES, TrackBranchingAnalyzer.FEATURE_SHORT_NAMES, TrackBranchingAnalyzer.FEATURE_DIMENSIONS, TrackBranchingAnalyzer.IS_INT );
		fm.declareTrackFeatures( TrackLocationAnalyzer.FEATURES, TrackLocationAnalyzer.FEATURE_NAMES, TrackLocationAnalyzer.FEATURE_SHORT_NAMES, TrackLocationAnalyzer.FEATURE_DIMENSIONS, TrackLocationAnalyzer.IS_INT );
		fm.declareTrackFeatures( TrackSpeedStatisticsAnalyzer.FEATURES, TrackSpeedStatisticsAnalyzer.FEATURE_NAMES, TrackSpeedStatisticsAnalyzer.FEATURE_SHORT_NAMES, TrackSpeedStatisticsAnalyzer.FEATURE_DIMENSIONS, TrackSpeedStatisticsAnalyzer.IS_INT );

		// BCellobjects - we can find them under the root element
		final BCellobjectCollection BCellobjects = getAllBCellobjects();
		setBCellobjectsVisibility();
		model.setBCellobjects( BCellobjects, false );

		// Tracks - we can find them under the root element
		if ( !readTracks( root, model ) )
		{
			ok = false;
		}

		// Track features
		try
		{
			final Map< Integer, Map< String, Double > > savedFeatureMap = readTrackFeatures( root );
			for ( final Integer savedKey : savedFeatureMap.keySet() )
			{

				final Map< String, Double > savedFeatures = savedFeatureMap.get( savedKey );
				for ( final String feature : savedFeatures.keySet() )
				{
					model.getFeatureModel().putTrackFeature( savedKey, feature, savedFeatures.get( feature ) );
				}
			}
		}
		catch ( final RuntimeException re )
		{
			logger.error( "Problem populating track features:\n" );
			logger.error( re.getMessage() );
			ok = false;
		}

		// Return
		return model;
	}

	@Override
	public void readSettings( final Settings settings, final TrackerProvider trackerProvider, final EdgeAnalyzerProvider edgeAnalyzerProvider, final TrackAnalyzerProvider trackAnalyzerProvider )
	{
		settings.imp = getImage();
		getBaseSettings( settings );
		getTrackerSettings( settings, trackerProvider );
		settings.initialBCellobjectFilterValue = getInitialFilter().value;
		settings.setBCellobjectFilters( getBCellobjectFeatureFilters() );
		settings.setTrackFilters( getTrackFeatureFilters() );

		// Analyzers - we add them all
		settings.clearBCellobjectAnalyzerFactories();


		settings.clearEdgeAnalyzers();
		final List< String > edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
		for ( final String key : edgeAnalyzerKeys )
		{
			final EdgeAnalyzer edgeAnalyzer = edgeAnalyzerProvider.getFactory( key );
			settings.addEdgeAnalyzer( edgeAnalyzer );
		}

		settings.clearTrackAnalyzers();
		final List< String > trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
		for ( final String key : trackAnalyzerKeys )
		{
			final TrackAnalyzer trackAnalyzer = trackAnalyzerProvider.getFactory( key );
			settings.addTrackAnalyzer( trackAnalyzer );
		}

	}

	/*
	 * PRIVATE METHODS
	 */

	/**
	 * Returns a map of the saved track features, as they appear in the file
	 */
	private Map< Integer, Map< String, Double > > readTrackFeatures( final Element modelElement )
	{

		final HashMap< Integer, Map< String, Double > > featureMap = new HashMap< >();

		final Element allTracksElement = modelElement.getChild( TRACK_COLLECTION_ELEMENT_KEY );
		if ( null == allTracksElement )
		{
			logger.error( "Cannot find the track collection in file.\n" );
			ok = false;
			return null;
		}

		// Load tracks
		final List< Element > trackElements = allTracksElement.getChildren( TRACK_ELEMENT_KEY );
		for ( final Element trackElement : trackElements )
		{

			int trackID = -1;
			try
			{
				trackID = trackElement.getAttribute( TrackIndexAnalyzer.TRACK_ID ).getIntValue();
			}
			catch ( final DataConversionException e1 )
			{
				logger.error( "Found a track with invalid trackID for " + trackElement + ". Skipping.\n" );
				ok = false;
				continue;
			}

			final HashMap< String, Double > trackMap = new HashMap< >();

			final List< Attribute > attributes = trackElement.getAttributes();
			for ( final Attribute attribute : attributes )
			{

				String attName = attribute.getName();
				if ( attName.equals( TRACK_NAME_ATTRIBUTE_NAME ) )
				{ // Skip trackID attribute
					continue;
				}
				else if ( attName.equals( "X_LOCATION" ) )
				{ // convert old names on the fly
					attName = TrackLocationAnalyzer.X_LOCATION;
				}
				else if ( attName.equals( "Y_LOCATION" ) )
				{
					attName = TrackLocationAnalyzer.Y_LOCATION;
				}
				else if ( attName.equals( "Z_LOCATION" ) )
				{
					attName = TrackLocationAnalyzer.Z_LOCATION;
				}

				Double attVal = Double.NaN;
				try
				{
					attVal = attribute.getDoubleValue();
				}
				catch ( final DataConversionException e )
				{
					logger.error( "Track " + trackID + ": Cannot read the feature " + attName + " value. Skipping.\n" );
					ok = false;
					continue;
				}

				trackMap.put( attName, attVal );

			}

			featureMap.put( trackID, trackMap );
		}

		return featureMap;

	}

	private ImagePlus getImage()
	{
		final Element imageInfoElement = root.getChild( IMAGE_ELEMENT_KEY );
		if ( null == imageInfoElement ) { return null; // value will still be
														// null
		}
		final String filename = imageInfoElement.getAttributeValue( IMAGE_FILENAME_ATTRIBUTE_NAME );
		String folder = imageInfoElement.getAttributeValue( IMAGE_FOLDER_ATTRIBUTE_NAME );
		if ( null == filename || filename.isEmpty() ) { return null; }
		if ( null == folder || folder.isEmpty() )
		{
			folder = file.getParent(); // it is a relative path, then
		}
		File imageFile = new File( folder, filename );
		if ( !imageFile.exists() || !imageFile.canRead() )
		{
			// Could not find it to the absolute path. Then we look for the same
			// path of the xml file
			folder = file.getParent();
			imageFile = new File( folder, filename );
			if ( !imageFile.exists() || !imageFile.canRead() ) { return null; }
		}
		return IJ.openImage( imageFile.getAbsolutePath() );
	}

	/**
	 * Return the initial threshold on quality stored in this file. Return
	 * <code>null</code> if the initial threshold data cannot be found in the
	 * file.
	 */
	private FeatureFilter getInitialFilter()
	{

		final Element itEl = root.getChild( INITIAL_BCellobject_FILTER_ELEMENT_KEY );
		if ( null == itEl ) { return null; }
		final String feature = itEl.getAttributeValue( FILTER_FEATURE_ATTRIBUTE_NAME );
		final Double value = readDoubleAttribute( itEl, FILTER_VALUE_ATTRIBUTE_NAME, logger );
		final boolean isAbove = readBooleanAttribute( itEl, FILTER_ABOVE_ATTRIBUTE_NAME, logger );
		final FeatureFilter ft = new FeatureFilter( feature, value, isAbove );
		return ft;
	}

	/**
	 * Return the list of {@link FeatureFilter} for BCellobjects stored in this file.
	 * Return <code>null</code> if the BCellobject feature filters data cannot be found
	 * in the file.
	 */
	private List< FeatureFilter > getBCellobjectFeatureFilters()
	{

		final List< FeatureFilter > featureThresholds = new ArrayList< >();
		final Element ftCollectionEl = root.getChild( BCellobject_FILTER_COLLECTION_ELEMENT_KEY );
		if ( null == ftCollectionEl ) { return null; }
		final List< Element > ftEls = ftCollectionEl.getChildren( FILTER_ELEMENT_KEY );
		for ( final Element ftEl : ftEls )
		{
			final String feature = ftEl.getAttributeValue( FILTER_FEATURE_ATTRIBUTE_NAME );
			final Double value = readDoubleAttribute( ftEl, FILTER_VALUE_ATTRIBUTE_NAME, logger );
			final boolean isAbove = readBooleanAttribute( ftEl, FILTER_ABOVE_ATTRIBUTE_NAME, logger );
			final FeatureFilter ft = new FeatureFilter( feature, value, isAbove );
			featureThresholds.add( ft );
		}
		return featureThresholds;
	}

	/**
	 * @return the list of {@link FeatureFilter} for tracks stored in this file.
	 *         Return <code>null</code> if the track feature filters data cannot
	 *         be found in the file.
	 */
	private List< FeatureFilter > getTrackFeatureFilters()
	{
		final List< FeatureFilter > featureThresholds = new ArrayList< >();
		final Element ftCollectionEl = root.getChild( TRACK_FILTER_COLLECTION_ELEMENT_KEY );
		if ( null == ftCollectionEl ) { return null; }
		final List< Element > ftEls = ftCollectionEl.getChildren( FILTER_ELEMENT_KEY );
		for ( final Element ftEl : ftEls )
		{
			final String feature = ftEl.getAttributeValue( FILTER_FEATURE_ATTRIBUTE_NAME );
			final Double value = readDoubleAttribute( ftEl, FILTER_VALUE_ATTRIBUTE_NAME, logger );
			final boolean isAbove = readBooleanAttribute( ftEl, FILTER_ABOVE_ATTRIBUTE_NAME, logger );
			final FeatureFilter ft = new FeatureFilter( feature, value, isAbove );
			featureThresholds.add( ft );
		}
		return featureThresholds;
	}

	private void getBaseSettings( final Settings settings )
	{
		// Basic settings
		final Element settingsEl = root.getChild( SETTINGS_ELEMENT_KEY );
		if ( null != settingsEl )
		{
			settings.xstart = readIntAttribute( settingsEl, SETTINGS_XSTART_ATTRIBUTE_NAME, logger, 1 );
			settings.xend = readIntAttribute( settingsEl, SETTINGS_XEND_ATTRIBUTE_NAME, logger, 512 );
			settings.ystart = readIntAttribute( settingsEl, SETTINGS_YSTART_ATTRIBUTE_NAME, logger, 1 );
			settings.yend = readIntAttribute( settingsEl, SETTINGS_YEND_ATTRIBUTE_NAME, logger, 512 );
			settings.zstart = readIntAttribute( settingsEl, SETTINGS_ZSTART_ATTRIBUTE_NAME, logger, 1 );
			settings.zend = readIntAttribute( settingsEl, SETTINGS_ZEND_ATTRIBUTE_NAME, logger, 10 );
			settings.tstart = readIntAttribute( settingsEl, SETTINGS_TSTART_ATTRIBUTE_NAME, logger, 1 );
			settings.tend = readIntAttribute( settingsEl, SETTINGS_TEND_ATTRIBUTE_NAME, logger, 10 );
			// settings.detectionChannel = readIntAttribute(settingsEl,
			// SETTINGS_DETECTION_CHANNEL_ATTRIBUTE_NAME, logger, 1);
		}
		// Image info settings
		final Element infoEl = root.getChild( IMAGE_ELEMENT_KEY );
		if ( null != infoEl )
		{
			settings.dx = readDoubleAttribute( infoEl, IMAGE_PIXEL_WIDTH_ATTRIBUTE_NAME, logger );
			settings.dy = readDoubleAttribute( infoEl, IMAGE_PIXEL_HEIGHT_ATTRIBUTE_NAME, logger );
			settings.dz = readDoubleAttribute( infoEl, IMAGE_VOXEL_DEPTH_ATTRIBUTE_NAME, logger );
			settings.dt = readDoubleAttribute( infoEl, IMAGE_TIME_INTERVAL_ATTRIBUTE_NAME, logger );
			settings.width = readIntAttribute( infoEl, IMAGE_WIDTH_ATTRIBUTE_NAME, logger, 512 );
			settings.height = readIntAttribute( infoEl, IMAGE_HEIGHT_ATTRIBUTE_NAME, logger, 512 );
			settings.nslices = readIntAttribute( infoEl, IMAGE_NSLICES_ATTRIBUTE_NAME, logger, 1 );
			settings.nframes = readIntAttribute( infoEl, IMAGE_NFRAMES_ATTRIBUTE_NAME, logger, 1 );
			settings.imageFileName = infoEl.getAttributeValue( IMAGE_FILENAME_ATTRIBUTE_NAME );
			settings.imageFolder = infoEl.getAttributeValue( IMAGE_FOLDER_ATTRIBUTE_NAME );
		}
	}

	/**
	 * Update the given {@link Settings} object with the
	 * {@link BCellobjectDetectorFactory} and settings map fields named
	 * {@link Settings#detectorFactory} and {@link Settings#detectorSettings}
	 * read within the XML file this reader is initialized with.
	 * <p>
	 * As a side effect, this method also configure the {@link DetectorProvider}
	 * stored in the passed TrackMate plugin for the found target
	 * detector factory.
	 * <p>
	 * If the detector settings XML element is not present in the file, the
	 * {@link Settings} object is not updated.
	 *
	 * @param settings
	 *            the base {@link Settings} object to update.
	 * @param provider
	 *            the {@link DetectorProvider} that can unmarshal detector and
	 *            detector settings.
	 */
	
	/**
	 * Update the given {@link Settings} object with BCellobjectTracker proper
	 * settings map fields named {@link Settings#trackerSettings} and
	 * Settings#tracker read within the XML file this reader is
	 * initialized with.
	 * <p>
	 * If the tracker settings XML element is not present in the file, the
	 * {@link Settings} object is not updated. If the tracker settings or the
	 * tracker info can be read, but cannot be understood (most likely because
	 * the class the XML refers to is unknown) then a default object is
	 * substituted.
	 *
	 * @param settings
	 *            the base {@link Settings} object to update.
	 * @param provider
	 *            the {@link TrackerProvider} that can unmarshal tracker and
	 *            tracker settings.
	 */
	private void getTrackerSettings( final Settings settings, final TrackerProvider provider )
	{
		final Element element = root.getChild( TRACKER_SETTINGS_ELEMENT_KEY );
		if ( null == element ) { return; }

		// Get the tracker key
		final String trackerKey = element.getAttributeValue( XML_ATTRIBUTE_TRACKER_NAME );
		if ( null == trackerKey )
		{
			logger.error( "Could not find the tracker element in file.\n" );
			return;
		}

		final BCellobjectTrackerFactory factory = provider.getFactory( trackerKey );
		if ( null == factory )
		{
			logger.error( "The tracker identified by the key " + trackerKey + " is unknown to TrackMate.\n" );
			this.ok = false;
			return;
		}

		// All the hard work is delegated to the factory.
		final Map< String, Object > ds = new HashMap< >();
		final boolean lOk = factory.unmarshall( element, ds );

		if ( !lOk )
		{
			logger.error( factory.getErrorMessage() );
			this.ok = false;
			return;
		}

		settings.trackerSettings = ds;
		settings.trackerFactory = factory;
	}

	/**
	 * Read the list of all BCellobjects stored in this file.
	 * <p>
	 * Internally, this methods also builds the cache field, which will be
	 * required by other methods.
	 *
	 * It is therefore sensible to call this method first, just after
	 * parsing the file. If not called, this method will be called
	 * anyway by the other methods to build the cache.
	 *
	 * @return a {@link BCellobjectCollection}. Return <code>null</code> if the BCellobject
	 *         section is not present in the file.
	 */
	private BCellobjectCollection getAllBCellobjects()
	{
		// Root element for collection
		final Element BCellobjectCollection = root.getChild( BCellobject_COLLECTION_ELEMENT_KEY );
		if ( null == BCellobjectCollection ) { return null; }

		// Retrieve children elements for each frame
		final List< Element > frameContent = BCellobjectCollection.getChildren( BCellobject_FRAME_COLLECTION_ELEMENT_KEY );

		// Determine total number of BCellobjects
		int nBCellobjects = readIntAttribute( BCellobjectCollection, BCellobject_COLLECTION_NBCellobjectS_ATTRIBUTE_NAME, Logger.VOID_LOGGER );
		if ( nBCellobjects == 0 )
		{
			// Could not find it or read it. Determine it by quick sweeping
			// through children element
			for ( final Element currentFrameContent : frameContent )
			{
				nBCellobjects += currentFrameContent.getChildren( BCellobject_ELEMENT_KEY ).size();
			}
		}

		// Instantiate cache
		cache = new ConcurrentHashMap< >( nBCellobjects );

		// Load collection and build cache
		int currentFrame = 0;
		ArrayList< BCellobject > BCellobjectList;
		final BCellobjectCollection allBCellobjects = new BCellobjectCollection();

		for ( final Element currentFrameContent : frameContent )
		{

			currentFrame = readIntAttribute( currentFrameContent, FRAME_ATTRIBUTE_NAME, logger );
			final List< Element > BCellobjectContent = currentFrameContent.getChildren( BCellobject_ELEMENT_KEY );
			BCellobjectList = new ArrayList< >( BCellobjectContent.size() );
			for ( final Element BCellobjectElement : BCellobjectContent )
			{
				final BCellobject BCellobject = createBCellobjectFrom( BCellobjectElement );
				BCellobjectList.add( BCellobject );
				cache.put( BCellobject.ID(), BCellobject );
			}

			allBCellobjects.put( currentFrame, BCellobjectList );
		}
		return allBCellobjects;
	}

	/**
	 * Sets the BCellobject visibility as stored in this file.
	 */
	private void setBCellobjectsVisibility()
	{
		final Element selectedBCellobjectCollection = root.getChild( FILTERED_BCellobject_ELEMENT_KEY );
		if ( null == selectedBCellobjectCollection ) { return; }

		if ( null == cache )
		{
			getAllBCellobjects(); // build it if it's not here
		}

		final List< Element > frameContent = selectedBCellobjectCollection.getChildren( FILTERED_BCellobject_COLLECTION_ELEMENT_KEY );

		for ( final Element currentFrameContent : frameContent )
		{
			final List< Element > BCellobjectContent = currentFrameContent.getChildren( BCellobject_ID_ELEMENT_KEY );
			// Loop over all BCellobject element
			for ( final Element BCellobjectEl : BCellobjectContent )
			{
				// Find corresponding BCellobject in cache
				final int ID = readIntAttribute( BCellobjectEl, BCellobject_ID_ATTRIBUTE_NAME, logger );
				final BCellobject BCellobject = cache.get( ID );
				BCellobject.putFeature( BCellobjectCollection.VISIBLITY, BCellobjectCollection.ONE );
			}
		}
	}

	private BCellobject createBCellobjectFrom( final Element BCellobjectEl )
	{
		final int ID = readIntAttribute( BCellobjectEl, BCellobject_ID_ATTRIBUTE_NAME, logger );
		final BCellobject BCellobject = new BCellobject( ID );

		final List< Attribute > atts = BCellobjectEl.getAttributes();
		removeAttributeFromName( atts, BCellobject_ID_ATTRIBUTE_NAME );

		String name = BCellobjectEl.getAttributeValue( BCellobject_NAME_ATTRIBUTE_NAME );
		if ( null == name || name.equals( "" ) )
		{
			name = "ID" + ID;
		}
		BCellobject.setName( name );
		removeAttributeFromName( atts, BCellobject_NAME_ATTRIBUTE_NAME );

		for ( final Attribute att : atts )
		{
			if ( att.getName().equals( BCellobject_NAME_ATTRIBUTE_NAME ) || att.getName().equals( BCellobject_ID_ATTRIBUTE_NAME ) )
			{
				continue;
			}
			try
			{
				BCellobject.putFeature( att.getName(), att.getDoubleValue() );
			}
			catch ( final DataConversionException e )
			{
				logger.error( "Cannot read the feature " + att.getName() + " value. Skipping.\n" );
			}
		}
		return BCellobject;
	}

}
