package Buddy.plugin.trackmate.io;


/**
 * Contains the key string used for xml marshaling.
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;  2010-2011
  */
public class TmXmlKeys {

	/*
	 * GENERIC ATTRIBUTES
	 */

	public static final String FRAME_ATTRIBUTE_NAME 				= "frame";
	public static final String BCellobject_ID_ATTRIBUTE_NAME 				= "ID";
	public static final String BCellobject_NAME_ATTRIBUTE_NAME 			= "name";

	/*
	 * ROOT ELEMENT
	 */

	public static final String ROOT_ELEMENT_KEY 					= "TrackMate";
	public static final String PLUGIN_VERSION_ATTRIBUTE_NAME		= "version";
	public static final String MODEL_ELEMENT_KEY 					= "Model";
	public static final String SETTINGS_ELEMENT_KEY 				= "Settings";
	public static final String SPATIAL_UNITS_ATTRIBUTE_NAME 		= "spatialunits";
	public static final String TIME_UNITS_ATTRIBUTE_NAME 			= "timeunits";



	/*
	 * LOG
	 */

	public static final String LOG_ELEMENT_KEY 						= "Log";

	/*
	 * BASE SETTINGS elements
	 */

	public static final String CROP_ELEMENT_KEY 				= "BasicSettings";
	public static final String CROP_XSTART_ATTRIBUTE_NAME 		= "xstart";
	public static final String CROP_YSTART_ATTRIBUTE_NAME 		= "ystart";
	public static final String CROP_ZSTART_ATTRIBUTE_NAME 		= "zstart";
	public static final String CROP_TSTART_ATTRIBUTE_NAME 		= "tstart";
	public static final String CROP_XEND_ATTRIBUTE_NAME 		= "xend";
	public static final String CROP_YEND_ATTRIBUTE_NAME 		= "yend";
	public static final String CROP_ZEND_ATTRIBUTE_NAME 		= "zend";
	public static final String CROP_TEND_ATTRIBUTE_NAME 		= "tend";
	public static final String CROP_DETECTION_CHANNEL_ATTRIBUTE_NAME 		= "detectionchannel";

	/*
	 * DETECTOR SETTINGS
	 */

	public static final String DETECTOR_SETTINGS_ELEMENT_KEY 			= "DetectorSettings";

	/*
	 * TRACKER SETTINGS
	 */

	public static final String TRACKER_SETTINGS_ELEMENT_KEY				= "TrackerSettings";
	public static final String TRACKER_SETTINGS_CLASS_ATTRIBUTE_NAME	= "trackersettingsclass";
	public static final String TRACKER_ATTRIBUTE_NAME					= "trackername";

	/*
	 * IMAGE element
	 */

	public static final String IMAGE_ELEMENT_KEY 					= "ImageData";
	public static final String IMAGE_FILENAME_ATTRIBUTE_NAME 		= "filename";
	public static final String IMAGE_FOLDER_ATTRIBUTE_NAME 			= "folder";
	public static final String IMAGE_PIXEL_WIDTH_ATTRIBUTE_NAME		= "pixelwidth";
	public static final String IMAGE_WIDTH_ATTRIBUTE_NAME 			= "width";
	public static final String IMAGE_PIXEL_HEIGHT_ATTRIBUTE_NAME 	= "pixelheight";
	public static final String IMAGE_HEIGHT_ATTRIBUTE_NAME 			= "height";
	public static final String IMAGE_VOXEL_DEPTH_ATTRIBUTE_NAME 	= "voxeldepth";
	public static final String IMAGE_NSLICES_ATTRIBUTE_NAME 		= "nslices";
	public static final String IMAGE_TIME_INTERVAL_ATTRIBUTE_NAME 	= "timeinterval";
	public static final String IMAGE_NFRAMES_ATTRIBUTE_NAME 		= "nframes";

	/*
	 * ALL BCellobjectS element
	 */

	public static final String BCellobject_COLLECTION_ELEMENT_KEY 				= "AllBCellobjects";
	public static final String BCellobject_COLLECTION_NBCellobjectS_ATTRIBUTE_NAME 	= "nBCellobjects";
	public static final String BCellobject_FRAME_COLLECTION_ELEMENT_KEY 		= "BCellobjectsInFrame";
	public static final String BCellobject_ELEMENT_KEY 						= "BCellobject";

	/*
	 * INITIAL BCellobject FILTER element
	 */

	public static final String INITIAL_BCellobject_FILTER_ELEMENT_KEY		= "InitialBCellobjectFilter";

	/*
	 * FILTERS element for BCellobjectS and TRACKS
	 */

	public static final String BCellobject_FILTER_COLLECTION_ELEMENT_KEY		= "BCellobjectFilterCollection";
	public static final String TRACK_FILTER_COLLECTION_ELEMENT_KEY		= "TrackFilterCollection";
	public static final String FILTER_ELEMENT_KEY						= "Filter";
	public static final String FILTER_FEATURE_ATTRIBUTE_NAME 			= "feature";
	public static final String FILTER_VALUE_ATTRIBUTE_NAME 				= "value";
	public static final String FILTER_ABOVE_ATTRIBUTE_NAME 				= "isabove";

	/*
	 * TRACK elements
	 */

	public static final String TRACK_COLLECTION_ELEMENT_KEY			= "AllTracks";
	public static final String TRACK_ELEMENT_KEY 					= "Track";
//	public static final String TRACK_ID_ATTRIBUTE_NAME 				= "trackID";
	public static final String TRACK_NAME_ATTRIBUTE_NAME 			= "name";

	public static final String TRACK_EDGE_ELEMENT_KEY				= "Edge";
//	public static final String TRACK_EDGE_SOURCE_ATTRIBUTE_NAME	 	= "sourceID";
//	public static final String TRACK_EDGE_TARGET_ATTRIBUTE_NAME	 	= "targetID";
//	public static final String TRACK_EDGE_WEIGHT_ATTRIBUTE_NAME	 	= "weight";


	/*
	 * TRACK FILTERED elements
	 */

	public static final String FILTERED_TRACK_ELEMENT_KEY 				= "FilteredTracks";
	public static final String TRACK_ID_ELEMENT_KEY 					= "TrackID";

	/*
	 * FEATURES elements
	 */

	public static final String FEATURE_DECLARATIONS_ELEMENT_KEY 		= "FeatureDeclarations";
	public static final String BCellobject_FEATURES_ELEMENT_KEY 				= "BCellobjectFeatures";
	public static final String EDGE_FEATURES_ELEMENT_KEY 				= "EdgeFeatures";
	public static final String TRACK_FEATURES_ELEMENT_KEY 				= "TrackFeatures";
	public static final String FEATURE_ELEMENT_KEY 						= "Feature";
	public static final String FEATURE_ATTRIBUTE						= "feature";
	public static final String FEATURE_NAME_ATTRIBUTE					= "name";
	public static final String FEATURE_SHORT_NAME_ATTRIBUTE				= "shortname";
	public static final String FEATURE_DIMENSION_ATTRIBUTE				= "dimension";
	public static final String FEATURE_ISINT_ATTRIBUTE 					= "isint";

	/*
	 * BCellobject< EDGE AND TRACK FEATURE ANALYZERS elements
	 */

	public static final String ANALYZER_COLLECTION_ELEMENT_KEY 			= "AnalyzerCollection";
	public static final String BCellobject_ANALYSERS_ELEMENT_KEY 				= "BCellobjectAnalyzers";
	public static final String EDGE_ANALYSERS_ELEMENT_KEY 				= "EdgeAnalyzers";
	public static final String TRACK_ANALYSERS_ELEMENT_KEY 				= "TrackAnalyzers";
	public static final String ANALYSER_ELEMENT_KEY 					= "Analyzer";
	public static final String ANALYSER_KEY_ATTRIBUTE 					= "key";

	/*
	 * GUI
	 */

	public static final String GUI_STATE_ELEMENT_KEY 					= "GUIState";
	public static final String GUI_STATE_ATTRIBUTE 						= "state";
	public static final String GUI_VIEW_ELEMENT_KEY 					= "View";
	public static final String GUI_VIEW_ATTRIBUTE 						= "key";
	public static final String GUI_VIEW_ATTRIBUTE_POSITION_X 			= "x";
	public static final String GUI_VIEW_ATTRIBUTE_POSITION_Y 			= "y";
	public static final String GUI_VIEW_ATTRIBUTE_POSITION_WIDTH 		= "width";
	public static final String GUI_VIEW_ATTRIBUTE_POSITION_HEIGHT		= "height";

}

