package Buddy.plugin.trackmate.io;


/**
 * Contains the key string used for xml marshaling.
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;  2010-2011
  */
class TmXmlKeys_v20 {

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

	/*
	 * LOG
	 */

	public static final String LOG_ELEMENT_KEY 						= "Log";

	/*
	 * SETTINGS elements
	 */

	public static final String SETTINGS_ELEMENT_KEY 				= "BasicSettings";
	public static final String SETTINGS_XSTART_ATTRIBUTE_NAME 		= "xstart";
	public static final String SETTINGS_YSTART_ATTRIBUTE_NAME 		= "ystart";
	public static final String SETTINGS_ZSTART_ATTRIBUTE_NAME 		= "zstart";
	public static final String SETTINGS_TSTART_ATTRIBUTE_NAME 		= "tstart";
	public static final String SETTINGS_XEND_ATTRIBUTE_NAME 		= "xend";
	public static final String SETTINGS_YEND_ATTRIBUTE_NAME 		= "yend";
	public static final String SETTINGS_ZEND_ATTRIBUTE_NAME 		= "zend";
	public static final String SETTINGS_TEND_ATTRIBUTE_NAME 		= "tend";
	public static final String SETTINGS_DETECTION_CHANNEL_ATTRIBUTE_NAME 		= "detectionchannel";

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
	public static final String IMAGE_SPATIAL_UNITS_ATTRIBUTE_NAME 	= "spatialunits";
	public static final String IMAGE_TIME_UNITS_ATTRIBUTE_NAME 		= "timeunits";

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
	 * BCellobject FILTERED elements
	 */

	public static final String FILTERED_BCellobject_ELEMENT_KEY 				= "FilteredBCellobjects";
	public static final String FILTERED_BCellobject_COLLECTION_ELEMENT_KEY 	= "FilteredBCellobjectsInFrame";
	public static final String BCellobject_ID_ELEMENT_KEY 						= "BCellobjectID";

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

}
