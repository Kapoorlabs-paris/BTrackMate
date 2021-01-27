package budDetector;

import net.imglib2.RealLocalizable;

public class Distance {

	
	
	public static double AngularDistance( final double[] pointA, final double[] pointB, final double[] pointmid ) {
		
		
		
		
		double radiusA = Math.sqrt(DistanceSq(pointA, pointmid));
		double radiusB = Math.sqrt(DistanceSq(pointB, pointmid));
		
		double meanradius = 0.5 * (radiusA + radiusB);
		double Eucdistance = Math.sqrt(DistanceSq(pointA, pointB));

		double angulardistance = (Eucdistance / meanradius) * (360 / (2* 3.14));
		
		return (angulardistance);
	}
	
	
	/**
	 * Retruns the squared distance between two double[]'S
	 * 
	 * @param pointA
	 * @param pointB
	 * @return
	 */

	public static double DistanceSq(final double[] pointA, final double[] pointB) {

		double distance = 0;
		int numDim = pointA.length;

		for (int d = 0; d < numDim; ++d) {

			distance += (pointA[d] - pointB[d])
					* (pointA[d] - pointB[d]);

		}
		return distance;
	}
	
	
	/**
	 * Retruns the squared distance between two double[]'S
	 * 
	 * @param pointA
	 * @param pointB
	 * @return
	 */

	public static double DistanceSq(final double[] pointA, final long[] pointB) {

		double distance = 0;
		int numDim = pointA.length;

		for (int d = 0; d < numDim; ++d) {

			distance += (pointA[d] - pointB[d])
					* (pointA[d] - pointB[d]);

		}
		return distance;
	}
	public static double DistanceSq(final long[] pointA, final double[] pointB) {

		double distance = 0;
		int numDim = pointA.length;

		for (int d = 0; d < numDim; ++d) {

			distance += (pointA[d] - pointB[d])
					* (pointA[d] - pointB[d]);

		}
		return distance;
	}
	
	/**
	 * Retruns the squared distance between two long[]'S
	 * 
	 * @param pointA
	 * @param pointB
	 * @return
	 */

	public static double DistanceSq(final long[] pointA, final long[] pointB) {

		double distance = 0;
		int numDim = pointA.length;

		for (int d = 0; d < numDim; ++d) {

			distance += (pointA[d] - pointB[d])
					* (pointA[d] - pointB[d]);

		}
		return distance;
	}
	
	/**
	 * Returns the square root of the distance between two RealLocalizables
	 * 
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public static double DistanceSqrt(final RealLocalizable pointA, final RealLocalizable pointB) {

		double distance = 0;
		
		double[] pointAA = new double[pointA.numDimensions()];
		double[] pointBB = new double[pointB.numDimensions()];
		
		pointA.localize(pointAA);
		pointB.localize(pointBB);
		
		int numDim = pointAA.length;

		for (int d = 0; d < numDim; ++d) {

			distance += (pointAA[d] - pointBB[d])
					* (pointAA[d] - pointBB[d]);

		}
		return Math.sqrt(distance);
	}
	
	
	public static double SlopeVectors(final RealLocalizable pointA, final RealLocalizable midpoint) {
		
		
		double[] pointAA = new double[pointA.numDimensions()];
		double[] pointmid = new double[midpoint.numDimensions()];
		pointA.localize(pointAA);
		midpoint.localize(pointmid);
		final double[] vA = new double[] { pointAA[0] - pointmid[0], pointAA[1] - pointmid[1]  };
		final double slope = Math.atan(vA[1] / vA[0]);
		
		return slope;
	}
	
	/**
	 * Returns the angle between the vectors
	 * 
	 * @param pointA
	 * @param pointB
	 * @param midpoint
	 * @return
	 */
	
	public static double AngleVectors(final RealLocalizable pointA, final RealLocalizable pointB, final RealLocalizable midpoint) {
		
		
		double[] pointAA = new double[pointA.numDimensions()];
		double[] pointBB = new double[pointB.numDimensions()];
		double[] pointmid = new double[midpoint.numDimensions()];
		pointA.localize(pointAA);
		pointB.localize(pointBB);
		midpoint.localize(pointmid);
		
		final double[] vA = new double[] { pointAA[0] - pointmid[0], pointAA[1] - pointmid[1]  };
		
		final double[] vB = new double[] { pointBB[0] - pointmid[0], pointBB[1] - pointmid[1]  };
		
		double slopeA = vA[1] / (vA[0] + 1.0e-10);
		double slopeB = vB[1] / (vB[0] + 1.0e-10);
		
        double argument = (slopeA - slopeB ) / (1 + slopeA * slopeB);
		
	
		
		return argument;
	}
	
	public static double AngleVectorsDouble(final double[] pointA, final double[] pointB, final double[] midpoint) {
		
		
		
		
		final double[] vA = new double[] { pointA[0] - midpoint[0], pointA[1] - midpoint[1]  };
		
		final double[] vB = new double[] { pointB[0] - midpoint[0], pointB[1] - midpoint[1]  };
		
		double slopeA = vA[1] / (vA[0]+ 1.0e-10);
		double slopeB = vB[1] / (vB[0]+ 1.0e-10);
	    double argument = (slopeA - slopeB ) / (1 + slopeA * slopeB);
		
			return argument;
	}
	

 
   
     

}
