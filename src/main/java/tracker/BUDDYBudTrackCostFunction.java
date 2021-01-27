package tracker;

import budDetector.Budobject;

public class BUDDYBudTrackCostFunction implements BUDDYCostFunction< Budobject, Budobject >
{

	
	// Alpha is the weightage given to distance and Beta is the weightage given to the ratio of pixels
		public final double beta;
		public final double alpha;
		
		

		
		public double getAlpha(){
			
			return alpha;
		}
		
	  
		public double getBeta(){
			
			return beta;
		}

		public BUDDYBudTrackCostFunction (double alpha, double beta){
			
			this.alpha = alpha;
			this.beta = beta;
			
		}
		
		
	@Override
	public double linkingCost( final Budobject source, final Budobject target )
	{
		return source.DistanceTo(target, alpha, beta);
	}
		



	}

