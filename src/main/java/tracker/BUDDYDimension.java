package tracker;

public enum BUDDYDimension {

	NONE, distBud, distDynamicBud, POSITION, VELOCITY, LENGTH, // we separate length and
	// position so that
	// x,y,z are plotted on
	// a different graph
	// from spot sizes
TIME, ANGLE, RATE, // count per frames
STRING; // for non-numeric features
	
}
