package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import budDetector.Budobject;
import budDetector.Budpointobject;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import pluginTools.InteractiveBud;

public class BudSaveListener implements ActionListener {

	final InteractiveBud parent;

	public BudSaveListener(final InteractiveBud parent) {

		this.parent = parent;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		parent.saveFile.mkdir();
		String ID = parent.selectedID;
		BudSaveAllListener.savePinks(parent);
		if (ID != null) {

			BudSaveAllListener.saveTrackMovie(parent, ID);

			try {
				File budfile = new File(
						parent.saveFile + "//" + "AllBudInformation" + parent.addToName + "Buds" + ".txt");

				FileWriter fwbud = new FileWriter(budfile);
				BufferedWriter bwbud = new BufferedWriter(fwbud);
				bwbud.write("TrackLabel, Time, LocationX , LocationY , Perimeter \n");

				for (ValuePair<String, Budobject> Track : parent.BudTracklist) {

					String TrackLabel = Track.getA();

					double time = Track.getB().t * parent.timecal;
					double LocationX = Track.getB().Budcenter.getDoublePosition(0) * parent.calibrationX;
					double LocationY = Track.getB().Budcenter.getDoublePosition(1) * parent.calibrationY;
					double Perimeter = Track.getB().perimeter;

					bwbud.write(TrackLabel + "," + (int) time + "," + parent.nf.format(LocationX) + ","
							+ parent.nf.format(LocationY) + "," + parent.nf.format(Perimeter) + "\n");

				}

				bwbud.close();
				fwbud.close();

			} catch (IOException te) {
			}
		}

		ArrayList<double[]> Trackinfo = new ArrayList<double[]>();
		HashMap<Integer, Double> VelocityID = parent.BudVelocityMap.get(Integer.parseInt(ID));
		double maxRate = parent.TrackMaxVelocitylist.get(ID);
		double meanRate = parent.TrackMeanVelocitylist.get(ID);
		for (Pair<String, Budpointobject> Track : parent.Tracklist) {

			if (Track.getA().equals(ID)) {

				double time = Track.getB().t * parent.timecal;
				double LocationX = Track.getB().Location[0] * parent.calibrationX;
				double LocationY = Track.getB().Location[1] * parent.calibrationY;
				double Velocity = 0;
				if (VelocityID.get(Track.getB().t) != null)
					Velocity = VelocityID.get(Track.getB().t);

				Trackinfo.add(new double[] { time, LocationX, LocationY, Velocity, meanRate, maxRate });

			}

		}

		try {

			File fichier = new File(parent.saveFile + "//" + "BudGrowth" + parent.addToName + "TrackID" + ID + ".txt");

			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(" Time, LocationX , LocationY , Velocity, Mean Growth Rate, Max Growth Rate \n");

			for (int i = 0; i < Trackinfo.size(); ++i) {

				double[] current = Trackinfo.get(i);
				double time = current[0];
				double LocationX = current[1];
				double LocationY = current[2];
				double Velocity = current[3];

				bw.write((int) time + "," + parent.nf.format(LocationX) + "," + parent.nf.format(LocationY) + ","
						+ parent.nf.format(Velocity) + "," + parent.nf.format(meanRate) + ","
						+ parent.nf.format(maxRate) + "\n");

			}

			bw.close();
			fw.close();
		} catch (IOException te) {
		}

	}

}
