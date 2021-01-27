package Buddy.plugin.trackmate.io;

import static Buddy.plugin.trackmate.io.TmXmlKeys.ANALYSER_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.ANALYSER_KEY_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.ANALYZER_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_TEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_TSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_XEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_XSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_YEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_YSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_ZEND_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.CROP_ZSTART_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.DETECTOR_SETTINGS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.EDGE_ANALYSERS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.EDGE_FEATURES_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_DECLARATIONS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_DIMENSION_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_ISINT_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_NAME_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FEATURE_SHORT_NAME_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FILTERED_TRACK_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FILTER_ABOVE_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FILTER_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FILTER_FEATURE_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FILTER_VALUE_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.FRAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.GUI_STATE_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.GUI_STATE_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.GUI_VIEW_ATTRIBUTE;
import static Buddy.plugin.trackmate.io.TmXmlKeys.GUI_VIEW_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_FILENAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_FOLDER_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_HEIGHT_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_NFRAMES_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_NSLICES_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_PIXEL_HEIGHT_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_PIXEL_WIDTH_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_TIME_INTERVAL_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_VOXEL_DEPTH_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.IMAGE_WIDTH_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.INITIAL_BCellobject_FILTER_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.LOG_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.MODEL_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.PLUGIN_VERSION_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.ROOT_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.SETTINGS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.SPATIAL_UNITS_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_ANALYSERS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_COLLECTION_NBCellobjectS_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_FEATURES_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_FILTER_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_FRAME_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_ID_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.BCellobject_NAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TIME_UNITS_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACKER_SETTINGS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_ANALYSERS_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_EDGE_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_FEATURES_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_FILTER_COLLECTION_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_ID_ELEMENT_KEY;
import static Buddy.plugin.trackmate.io.TmXmlKeys.TRACK_NAME_ATTRIBUTE_NAME;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.XML_ATTRIBUTE_TRACKER_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jgrapht.graph.DefaultWeightedEdge;

import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.Dimension;
import Buddy.plugin.trackmate.FeatureModel;
import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.features.FeatureFilter;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;
import Buddy.plugin.trackmate.features.edges.EdgeAnalyzer;
import Buddy.plugin.trackmate.features.edges.EdgeTargetAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackIndexAnalyzer;
import Buddy.plugin.trackmate.gui.TrackMateGUIModel;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import budDetector.BCellobject;

public class TmXmlWriter
{

	/*
	 * FIELD
	 */

	protected final Element root;

	protected final Logger logger;

	private final File file;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Creates a new XML file writer for TrackMate.
	 *
	 * @param file
	 *            the xml file to write to, will be overwritten.
	 */
	public TmXmlWriter( final File file )
	{
		this( file, new Logger.StringBuilderLogger() );
	}

	/**
	 * Creates a new XML file writer for TrackMate.
	 *
	 * @param file
	 *            the xml file to write to, will be overwritten.
	 */
	public TmXmlWriter( final File file, final Logger logger )
	{
		this.root = new Element( ROOT_ELEMENT_KEY );
		root.setAttribute( PLUGIN_VERSION_ATTRIBUTE_NAME, Buddy.plugin.trackmate.TrackMate.PLUGIN_NAME_VERSION );
		this.logger = logger;
		this.file = file;
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Writes the document to the file. Content must be appended first.
	 *
	 * @see #appendLog(String)
	 * @see #appendModel(Model)
	 * @see #appendSettings(Settings)
	 */
	public void writeToFile() throws FileNotFoundException, IOException
	{
		try (FileOutputStream fos = new FileOutputStream( file ))
		{
			logger.log( "  Writing to file.\n" );
			final Document document = new Document( root );
			final XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
			outputter.output( document, fos );
		}
	}

	@Override
	public String toString()
	{
		final Document document = new Document( root );
		final XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
		final StringWriter writer = new StringWriter();
		try
		{
			outputter.output( document, writer );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * Appends the content of a {@link Model} to the file generated by this
	 * writer.
	 *
	 * @param model
	 *            the {@link Model} to write.
	 */
	public void appendModel( final Model model )
	{
		final Element modelElement = new Element( MODEL_ELEMENT_KEY );
		modelElement.setAttribute( SPATIAL_UNITS_ATTRIBUTE_NAME, model.getSpaceUnits() );
		modelElement.setAttribute( TIME_UNITS_ATTRIBUTE_NAME, model.getTimeUnits() );

		final Element featureDeclarationElement = echoFeaturesDeclaration( model );
		modelElement.addContent( featureDeclarationElement );

		final Element BCellobjectElement = echoBCellobjects( model );
		modelElement.addContent( BCellobjectElement );

		final Element trackElement = echoTracks( model );
		modelElement.addContent( trackElement );

		final Element filteredTrackElement = echoFilteredTracks( model );
		modelElement.addContent( filteredTrackElement );

		root.addContent( modelElement );
	}

	/**
	 * Appends the content of a {@link Settings} object to the document.
	 *
	 * @param settings
	 *            the {@link Settings} to write.
	 */
	public void appendSettings( final Settings settings )
	{
		final Element settingsElement = new Element( SETTINGS_ELEMENT_KEY );

		final Element imageInfoElement = echoImageInfo( settings );
		settingsElement.addContent( imageInfoElement );

		final Element cropElement = echoCropSettings( settings );
		settingsElement.addContent( cropElement );

	

		final Element initFilter = echoInitialBCellobjectFilter( settings );
		settingsElement.addContent( initFilter );

		final Element BCellobjectFiltersElement = echoBCellobjectFilters( settings );
		settingsElement.addContent( BCellobjectFiltersElement );

		final Element trackerElement = echoTrackerSettings( settings );
		settingsElement.addContent( trackerElement );

		final Element trackFiltersElement = echoTrackFilters( settings );
		settingsElement.addContent( trackFiltersElement );

		final Element analyzersElement = echoAnalyzers( settings );
		settingsElement.addContent( analyzersElement );

		root.addContent( settingsElement );
	}

	/**
	 * Appends the log content to the document.
	 *
	 * @param log
	 *            the log content, as a String.
	 */
	public void appendLog( final String log )
	{
		if ( null != log )
		{
			final Element logElement = new Element( LOG_ELEMENT_KEY );
			logElement.addContent( log );
			root.addContent( logElement );
			logger.log( "  Added log.\n" );
		}
	}

	/**
	 * Appends the current GUI state as a state string to the document.
	 *
	 * @param guimodel
	 *            the GUI model from which the GUI state and registered view
	 *            will be read.
	 */
	public void appendGUIState( final TrackMateGUIModel guimodel )
	{
		final Element guiel = new Element( GUI_STATE_ELEMENT_KEY );
		// state
		guiel.setAttribute( GUI_STATE_ATTRIBUTE, guimodel.getGUIStateString() );
		// views
		for ( final TrackMateModelView view : guimodel.getViews() )
		{
			final Element viewel = new Element( GUI_VIEW_ELEMENT_KEY );
			viewel.setAttribute( GUI_VIEW_ATTRIBUTE, view.getKey() );
			guiel.addContent( viewel );
		}

		root.addContent( guiel );
		logger.log( "  Added GUI current state.\n" );
	}

	/*
	 * PRIVATE METHODS
	 */

	private Element echoCropSettings( final Settings settings )
	{
		final Element settingsElement = new Element( CROP_ELEMENT_KEY );
		settingsElement.setAttribute( CROP_XSTART_ATTRIBUTE_NAME, "" + settings.xstart );
		settingsElement.setAttribute( CROP_XEND_ATTRIBUTE_NAME, "" + settings.xend );
		settingsElement.setAttribute( CROP_YSTART_ATTRIBUTE_NAME, "" + settings.ystart );
		settingsElement.setAttribute( CROP_YEND_ATTRIBUTE_NAME, "" + settings.yend );
		settingsElement.setAttribute( CROP_ZSTART_ATTRIBUTE_NAME, "" + settings.zstart );
		settingsElement.setAttribute( CROP_ZEND_ATTRIBUTE_NAME, "" + settings.zend );
		settingsElement.setAttribute( CROP_TSTART_ATTRIBUTE_NAME, "" + settings.tstart );
		settingsElement.setAttribute( CROP_TEND_ATTRIBUTE_NAME, "" + settings.tend );
		logger.log( "  Added crop settings.\n" );
		return settingsElement;
	}

	

	protected Element echoTrackerSettings( final Settings settings )
	{
		final Element el = new Element( TRACKER_SETTINGS_ELEMENT_KEY );

		if ( null == settings.trackerFactory ) { return el; }

		// Set the tracker factory key NOW.
		el.setAttribute( XML_ATTRIBUTE_TRACKER_NAME, settings.trackerFactory.getKey() );

		// Marshal the rest.
		if ( null != settings.trackerFactory )
		{
			final boolean ok = settings.trackerFactory.marshall( settings.trackerSettings, el );
			if ( !ok )
				logger.error( settings.trackerFactory.getErrorMessage() );
			else
				logger.log( "  Added tracker settings.\n" );
		}

		return el;
	}

	private Element echoTracks( final Model model )
	{

		/*
		 * Some numerical features are REQUIRED to be able to save to XML.
		 * Namely: the track ID feature for track and the edge BCellobject source and
		 * BCellobject target for edges. Whether the model provides them as features or
		 * not, we get them from the model and put them in the XML.
		 */

		final Element allTracksElement = new Element( TRACK_COLLECTION_ELEMENT_KEY );

		// Prepare track features for writing: we separate ints from doubles
		final List< String > trackFeatures = new ArrayList<>( model.getFeatureModel().getTrackFeatures() );
		// TrackID is treated separately.
		trackFeatures.remove( TrackIndexAnalyzer.TRACK_ID );

		// Same thing for edge features
		final List< String > edgeFeatures = new ArrayList<>( model.getFeatureModel().getEdgeFeatures() );
		// We will treat edge source and target separately.
		edgeFeatures.remove( EdgeTargetAnalyzer.BCellobject_SOURCE_ID );
		edgeFeatures.remove( EdgeTargetAnalyzer.BCellobject_TARGET_ID );

		final Set< Integer > trackIDs = model.getTrackModel().trackIDs( false );
		for ( final int trackID : trackIDs )
		{

			final Element trackElement = new Element( TRACK_ELEMENT_KEY );

			// Track name.
			trackElement.setAttribute( TRACK_NAME_ATTRIBUTE_NAME, model.getTrackModel().name( trackID ) );
			// Track ID.
			trackElement.setAttribute( TrackIndexAnalyzer.TRACK_ID, Integer.toString( trackID ) );

			for ( final String feature : trackFeatures )
			{
				final Double val = model.getFeatureModel().getTrackFeature( trackID, feature );
				if ( null == val )
				{
					// Skip missing features.
					continue;
				}
				final String str;
				if ( model.getFeatureModel().getTrackFeatureIsInt().get( feature ).booleanValue() )
					str = "" + val.intValue();
				else
					str = val.toString();
				trackElement.setAttribute( feature, str );
			}

			// Echo edges
			final Set< DefaultWeightedEdge > track = model.getTrackModel().trackEdges( trackID );
			if ( track.isEmpty() )
			{
				/*
				 * Special case: the track has only one BCellobject in it, therefore no
				 * edge. It just should not be, since the model never returns a
				 * track with less than one edge. So we skip writing it.
				 */
				continue;
			}

			for ( final DefaultWeightedEdge edge : track )
			{
				final Element edgeElement = new Element( TRACK_EDGE_ELEMENT_KEY );

				/*
				 * Make sure the edge has the right orientation: forward in
				 * time.
				 */
				final int sourceFrame = model.getTrackModel().getEdgeSource( edge ).getFeature( BCellobject.POSITION_T ).intValue();
				final int targetFrame = model.getTrackModel().getEdgeTarget( edge ).getFeature( BCellobject.POSITION_T ).intValue();
				final int sourceID;
				final int targetID;
				if ( targetFrame >= sourceFrame )
				{
					sourceID = model.getTrackModel().getEdgeSource( edge ).ID();
					targetID = model.getTrackModel().getEdgeTarget( edge ).ID();
				}
				else
				{
					sourceID = model.getTrackModel().getEdgeTarget( edge ).ID();
					targetID = model.getTrackModel().getEdgeSource( edge ).ID();
				}
				edgeElement.setAttribute( EdgeTargetAnalyzer.BCellobject_SOURCE_ID, Integer.toString( sourceID ) );
				edgeElement.setAttribute( EdgeTargetAnalyzer.BCellobject_TARGET_ID, Integer.toString( targetID ) );

				for ( final String feature : edgeFeatures )
				{
					final Double val = model.getFeatureModel().getEdgeFeature( edge, feature );
					if ( null == val )
					{
						// Skip missing features.
						continue;
					}
					final String str;
					if ( model.getFeatureModel().getEdgeFeatureIsInt().get( feature ).booleanValue() )
						str = "" + val.intValue();
					else
						str = val.toString();
					edgeElement.setAttribute( feature, str );
				}

				trackElement.addContent( edgeElement );
			}
			allTracksElement.addContent( trackElement );
		}
		logger.log( "  Added tracks.\n" );
		return allTracksElement;
	}

	private Element echoFilteredTracks( final Model model )
	{
		final Element filteredTracksElement = new Element( FILTERED_TRACK_ELEMENT_KEY );
		final Set< Integer > filteredTrackKeys = model.getTrackModel().trackIDs( true );
		for ( final int trackID : filteredTrackKeys )
		{
			final Element trackIDElement = new Element( TRACK_ID_ELEMENT_KEY );
			trackIDElement.setAttribute( TrackIndexAnalyzer.TRACK_ID, "" + trackID );
			filteredTracksElement.addContent( trackIDElement );
		}
		logger.log( "  Added filtered tracks.\n" );
		return filteredTracksElement;
	}

	protected Element echoImageInfo( final Settings settings )
	{
		final Element imEl = new Element( IMAGE_ELEMENT_KEY );
		imEl.setAttribute( IMAGE_FILENAME_ATTRIBUTE_NAME, settings.imageFileName );
		imEl.setAttribute( IMAGE_FOLDER_ATTRIBUTE_NAME, settings.imageFolder );
		imEl.setAttribute( IMAGE_WIDTH_ATTRIBUTE_NAME, "" + settings.width );
		imEl.setAttribute( IMAGE_HEIGHT_ATTRIBUTE_NAME, "" + settings.height );
		imEl.setAttribute( IMAGE_NSLICES_ATTRIBUTE_NAME, "" + settings.nslices );
		imEl.setAttribute( IMAGE_NFRAMES_ATTRIBUTE_NAME, "" + settings.nframes );
		imEl.setAttribute( IMAGE_PIXEL_WIDTH_ATTRIBUTE_NAME, "" + settings.dx );
		imEl.setAttribute( IMAGE_PIXEL_HEIGHT_ATTRIBUTE_NAME, "" + settings.dy );
		imEl.setAttribute( IMAGE_VOXEL_DEPTH_ATTRIBUTE_NAME, "" + settings.dz );
		imEl.setAttribute( IMAGE_TIME_INTERVAL_ATTRIBUTE_NAME, "" + settings.dt );
		logger.log( "  Added image information.\n" );
		return imEl;
	}

	private Element echoBCellobjects( final Model model )
	{
		final BCellobjectCollection BCellobjects = model.getBCellobjects();

		final Element BCellobjectCollectionElement = new Element( BCellobject_COLLECTION_ELEMENT_KEY );
		// Store total number of BCellobjects
		BCellobjectCollectionElement.setAttribute( BCellobject_COLLECTION_NBCellobjectS_ATTRIBUTE_NAME, "" + BCellobjects.getNBCellobjects( ) );

		for ( final int frame : BCellobjects.keySet() )
		{

			final Element frameBCellobjectsElement = new Element( BCellobject_FRAME_COLLECTION_ELEMENT_KEY );
			frameBCellobjectsElement.setAttribute( FRAME_ATTRIBUTE_NAME, "" + frame );

			for ( final Iterator< BCellobject > it = BCellobjects.iterator( frame ); it.hasNext(); )
			{
				final Element BCellobjectElement = marshalBCellobject( it.next(), model.getFeatureModel() );
				frameBCellobjectsElement.addContent( BCellobjectElement );
			}
			BCellobjectCollectionElement.addContent( frameBCellobjectsElement );
		}
		logger.log( "  Added " + BCellobjects.getNBCellobjects(  ) + " BCellobjects.\n" );
		return BCellobjectCollectionElement;
	}

	private Element echoFeaturesDeclaration( final Model model )
	{

		final FeatureModel fm = model.getFeatureModel();
		final Element featuresElement = new Element( FEATURE_DECLARATIONS_ELEMENT_KEY );

		// BCellobjects
		final Element BCellobjectFeaturesElement = new Element( BCellobject_FEATURES_ELEMENT_KEY );
		Collection< String > features = fm.getBCellobjectFeatures();
		Map< String, String > featureNames = fm.getBCellobjectFeatureNames();
		Map< String, String > featureShortNames = fm.getBCellobjectFeatureShortNames();
		Map< String, Dimension > featureDimensions = fm.getBCellobjectFeatureDimensions();
		Map< String, Boolean > featureIsInt = fm.getBCellobjectFeatureIsInt();
		for ( final String feature : features )
		{
			final Element fel = new Element( FEATURE_ELEMENT_KEY );
			fel.setAttribute( FEATURE_ATTRIBUTE, feature );
			fel.setAttribute( FEATURE_NAME_ATTRIBUTE, featureNames.get( feature ) );
			fel.setAttribute( FEATURE_SHORT_NAME_ATTRIBUTE, featureShortNames.get( feature ) );
			fel.setAttribute( FEATURE_DIMENSION_ATTRIBUTE, featureDimensions.get( feature ).name() );
			fel.setAttribute( FEATURE_ISINT_ATTRIBUTE, featureIsInt.get( feature ).toString() );
			BCellobjectFeaturesElement.addContent( fel );
		}
		featuresElement.addContent( BCellobjectFeaturesElement );

		// Edges
		final Element edgeFeaturesElement = new Element( EDGE_FEATURES_ELEMENT_KEY );
		features = fm.getEdgeFeatures();
		featureNames = fm.getEdgeFeatureNames();
		featureShortNames = fm.getEdgeFeatureShortNames();
		featureDimensions = fm.getEdgeFeatureDimensions();
		featureIsInt = fm.getEdgeFeatureIsInt();
		for ( final String feature : features )
		{
			final Element fel = new Element( FEATURE_ELEMENT_KEY );
			fel.setAttribute( FEATURE_ATTRIBUTE, feature );
			fel.setAttribute( FEATURE_NAME_ATTRIBUTE, featureNames.get( feature ) );
			fel.setAttribute( FEATURE_SHORT_NAME_ATTRIBUTE, featureShortNames.get( feature ) );
			fel.setAttribute( FEATURE_DIMENSION_ATTRIBUTE, featureDimensions.get( feature ).name() );
			fel.setAttribute( FEATURE_ISINT_ATTRIBUTE, featureIsInt.get( feature ).toString() );
			edgeFeaturesElement.addContent( fel );
		}
		featuresElement.addContent( edgeFeaturesElement );

		// Tracks
		final Element trackFeaturesElement = new Element( TRACK_FEATURES_ELEMENT_KEY );
		features = fm.getTrackFeatures();
		featureNames = fm.getTrackFeatureNames();
		featureShortNames = fm.getTrackFeatureShortNames();
		featureDimensions = fm.getTrackFeatureDimensions();
		featureIsInt = fm.getTrackFeatureIsInt();
		for ( final String feature : features )
		{
			final Element fel = new Element( FEATURE_ELEMENT_KEY );
			fel.setAttribute( FEATURE_ATTRIBUTE, feature );
			fel.setAttribute( FEATURE_NAME_ATTRIBUTE, featureNames.get( feature ) );
			fel.setAttribute( FEATURE_SHORT_NAME_ATTRIBUTE, featureShortNames.get( feature ) );
			fel.setAttribute( FEATURE_DIMENSION_ATTRIBUTE, featureDimensions.get( feature ).name() );
			fel.setAttribute( FEATURE_ISINT_ATTRIBUTE, featureIsInt.get( feature ).toString() );
			trackFeaturesElement.addContent( fel );
		}
		featuresElement.addContent( trackFeaturesElement );

		logger.log( "  Added BCellobject, edge and track feature declarations.\n" );
		return featuresElement;
	}

	protected Element echoInitialBCellobjectFilter( final Settings settings )
	{
		final Element itElement = new Element( INITIAL_BCellobject_FILTER_ELEMENT_KEY );
		itElement.setAttribute( FILTER_VALUE_ATTRIBUTE_NAME, "" + settings.initialBCellobjectFilterValue );
		itElement.setAttribute( FILTER_ABOVE_ATTRIBUTE_NAME, "" + true );
		logger.log( "  Added initial BCellobject filter.\n" );
		return itElement;
	}

	protected Element echoBCellobjectFilters( final Settings settings )
	{
		final List< FeatureFilter > featureThresholds = settings.getBCellobjectFilters();

		final Element filtersElement = new Element( BCellobject_FILTER_COLLECTION_ELEMENT_KEY );
		for ( final FeatureFilter threshold : featureThresholds )
		{
			final Element thresholdElement = new Element( FILTER_ELEMENT_KEY );
			thresholdElement.setAttribute( FILTER_FEATURE_ATTRIBUTE_NAME, threshold.feature );
			thresholdElement.setAttribute( FILTER_VALUE_ATTRIBUTE_NAME, threshold.value.toString() );
			thresholdElement.setAttribute( FILTER_ABOVE_ATTRIBUTE_NAME, "" + threshold.isAbove );
			filtersElement.addContent( thresholdElement );
		}
		logger.log( "  Added BCellobject feature filters.\n" );
		return filtersElement;
	}

	protected Element echoTrackFilters( final Settings settings )
	{
		final List< FeatureFilter > filters = settings.getTrackFilters();

		final Element trackFiltersElement = new Element( TRACK_FILTER_COLLECTION_ELEMENT_KEY );
		for ( final FeatureFilter filter : filters )
		{
			final Element thresholdElement = new Element( FILTER_ELEMENT_KEY );
			thresholdElement.setAttribute( FILTER_FEATURE_ATTRIBUTE_NAME, filter.feature );
			thresholdElement.setAttribute( FILTER_VALUE_ATTRIBUTE_NAME, filter.value.toString() );
			thresholdElement.setAttribute( FILTER_ABOVE_ATTRIBUTE_NAME, "" + filter.isAbove );
			trackFiltersElement.addContent( thresholdElement );
		}
		logger.log( "  Added track feature filters.\n" );
		return trackFiltersElement;
	}

	protected Element echoAnalyzers( final Settings settings )
	{
		final Element analyzersElement = new Element( ANALYZER_COLLECTION_ELEMENT_KEY );

		// BCellobject analyzers
		final Element BCellobjectAnalyzersEl = new Element( BCellobject_ANALYSERS_ELEMENT_KEY );
		for ( final BCellobjectAnalyzerFactory< ? > analyzer : settings.getBCellobjectAnalyzerFactories() )
		{
			final Element el = new Element( ANALYSER_ELEMENT_KEY );
			el.setAttribute( ANALYSER_KEY_ATTRIBUTE, analyzer.getKey() );
			BCellobjectAnalyzersEl.addContent( el );
		}
		analyzersElement.addContent( BCellobjectAnalyzersEl );

		// Edge analyzers
		final Element edgeAnalyzersEl = new Element( EDGE_ANALYSERS_ELEMENT_KEY );
		for ( final EdgeAnalyzer analyzer : settings.getEdgeAnalyzers() )
		{
			final Element el = new Element( ANALYSER_ELEMENT_KEY );
			el.setAttribute( ANALYSER_KEY_ATTRIBUTE, analyzer.getKey() );
			edgeAnalyzersEl.addContent( el );
		}
		analyzersElement.addContent( edgeAnalyzersEl );

		// Track analyzers
		final Element trackAnalyzersEl = new Element( TRACK_ANALYSERS_ELEMENT_KEY );
		for ( final TrackAnalyzer analyzer : settings.getTrackAnalyzers() )
		{
			final Element el = new Element( ANALYSER_ELEMENT_KEY );
			el.setAttribute( ANALYSER_KEY_ATTRIBUTE, analyzer.getKey() );
			trackAnalyzersEl.addContent( el );
		}
		analyzersElement.addContent( trackAnalyzersEl );

		logger.log( "  Added BCellobject, edge and track analyzers.\n" );
		return analyzersElement;
	}

	/*
	 * STATIC METHODS
	 */

	private static final Element marshalBCellobject( final BCellobject BCellobject, final FeatureModel fm )
	{
		final Collection< Attribute > attributes = new ArrayList<>();
		final Attribute IDattribute = new Attribute( BCellobject_ID_ATTRIBUTE_NAME, "" + BCellobject.ID() );
		attributes.add( IDattribute );
		final Attribute nameAttribute = new Attribute( BCellobject_NAME_ATTRIBUTE_NAME, BCellobject.getName() );
		attributes.add( nameAttribute );
		Double val;
		Attribute featureAttribute;

		for ( final String feature : BCellobject.getFeatures().keySet() )
		{
			val = BCellobject.getFeature( feature );
			if ( null == val )
			{
				// Skip missing features.
				continue;
			}

			final String str;

			if ( fm.getBCellobjectFeatureIsInt().get( feature ).booleanValue() )
			{
				str = "" + val.intValue();
			}
			else
			{
				str = val.toString();
			}
			featureAttribute = new Attribute( feature, str );
			attributes.add( featureAttribute );
		}

		final Element BCellobjectElement = new Element( BCellobject_ELEMENT_KEY );
		BCellobjectElement.setAttributes( attributes );
		return BCellobjectElement;
	}
}
