package tracker;

import budDetector.Budpointobject;

public class BUDDYBudPointTrackCostFunction implements BUDDYCostFunction< Budpointobject, Budpointobject >
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

		public BUDDYBudPointTrackCostFunction (double alpha, double beta){
			
			this.alpha = alpha;
			this.beta = beta;
			
		}
		
		
	@Override
	public double linkingCost( final Budpointobject source, final Budpointobject target )
	{
		return source.DistanceTo(target, alpha, beta);
	}
		



	}

