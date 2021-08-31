package listeners;

import fiji.plugin.globalbtrack.gui.components.CovistoKalmanPanel;
import pluginTools.InteractiveBud;

public class CreateINIfile {
	final InteractiveBud parent;

	public CreateINIfile(final InteractiveBud parent) {

		this.parent = parent;

	}

	public void RecordParent() {

		LocalPrefs.set("InitialSearch.int", CovistoKalmanPanel.initialSearchradiusInit);
		LocalPrefs.set("LinkLoosing.int", CovistoKalmanPanel.maxframegap);
		LocalPrefs.set("MinBudTrack.int", CovistoKalmanPanel.trackduration);
		LocalPrefs.set("MaxSearch.int", CovistoKalmanPanel.maxSearchradiusInit);
		LocalPrefs.set("TimeCalibration.double", parent.timecal);
		LocalPrefs.set("SpaceCalibrationX.double", parent.calibrationX);
		LocalPrefs.set("SpaceCalibrationY.double", parent.calibrationY);

		if (parent.saveFile != null)
			LocalPrefs.setHomeDir(parent.saveFile.getAbsolutePath());
		else
			LocalPrefs.setHomeDir(new java.io.File(".").getAbsolutePath());
		LocalPrefs.savePreferences();
		System.out.println(LocalPrefs.getHomeDir() + " " + parent.saveFile.getAbsolutePath());
		// System.exit(1);
	}

}
