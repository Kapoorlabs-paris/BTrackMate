package budDetector;

import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;

public class Cellobject {

	public final double cellVolume;

	public final double cellPerimeter;

	public final Localizable Location;

	public final int time;

	public final int label;

	public final double[] extents;

	public final double totalIntensity;

	public Cellobject(Localizable location, int time, int label, double totalIntensity, double cellVolume,
			double cellPerimeter, double[] extents) {

		this.Location = location;

		this.time = time;

		this.label = label;

		this.cellVolume = cellVolume;

		this.cellPerimeter = cellPerimeter;

		this.extents = extents;

		this.totalIntensity = totalIntensity;

	}

}
