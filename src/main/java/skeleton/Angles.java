package skeleton;

import net.imglib2.Point;
import net.imglib2.RealPoint;
import net.imglib2.util.LinAlgHelpers;

import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public abstract class Angles {
	public static double angle2DToCoordinateSystemsAxisInDegrees(RealPoint point) {
		final double[] vector = Vectors.asDoubles(point);

		return angle2DToCoordinateSystemsAxisInDegrees(vector);
	}

	public static double angle2DToCoordinateSystemsAxisInDegrees(double[] vector) {

		double angleToZAxisInDegrees;

		if (vector[Constants.Y] == 0) {
			angleToZAxisInDegrees = Math.signum(vector[Constants.X]) * 90;
		} else {
			angleToZAxisInDegrees = toDegrees(atan(vector[Constants.X] / vector[Constants.Y]));

			if (vector[Constants.Y] < 0) {
				angleToZAxisInDegrees += 180;
			}
		}

		return angleToZAxisInDegrees;
	}

	public static double angleOfSpindleAxisToXAxisInRadians(final double[] vector) {
		double[] xAxis = new double[] { 1, 0, 0 };

		double angleInRadians = Transforms.getAngle(vector, xAxis);

		return angleInRadians;
	}

}
