package budDetector;

import java.awt.Color;

import ij.gui.OvalRoi;
import net.imglib2.RealLocalizable;

public class Roiobject {

	public Color color;

	public OvalRoi roi;

	public RealLocalizable point;

	public int Label;

	public Roiobject(final Color color, final OvalRoi roi, final RealLocalizable point, final int Label) {

		this.color = color;

		this.roi = roi;

		this.point = point;

		this.Label = Label;

	}

}
