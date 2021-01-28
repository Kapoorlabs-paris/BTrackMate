# BTrackMate

Fiji plugin for tracking cells like Trackmate but with integer labelled segmentations as input/csv file of cell attributes as input with Raw images.

To use this install the jars in Fiji (update site MTrack) and create an anaconda enviornment for Napari based visualization.

For creating napari based visualization:

conda create -n testenv python==3.9.0

conda activate testenv

pip install scikit-image napari==0.4.3 pyqt5 btrack natsort scipy opencv-python-headless tifffile matplotlib ffmpeg-python imageio_ffmpeg

Now input your raw image, instance segmentation image in 2D + t or 3D + t and optionally mask image in this [notebook](https://github.com/kapoorlab/BTrackMate/blob/main/PythonTools/BTrackMateLocalization.ipynb). This notebok creates a csv file of cell attributes required by the Fiji tracker.

After creating a csv file that starts with Fiji{filename}.csv start Fiji, load Raw image and select BTrack - > XYZT tracker plugin

Load the input Raw image (you can skip loading the segmentation and mask image if you did step 4)

Load the csv file and click start Tracker, after that you will be lead into the usual TrackMate interface for tracking.

After doing the tracking/ track editing in TM save the xml file for visualization in Napari.

Load the saved XML in this [notebook](https://github.com/kapoorlab/BTrackMate/blob/main/PythonTools/BTrackMateVisualization.ipynb)
