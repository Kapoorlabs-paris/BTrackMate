package tracker;

public enum BUDDimension {

	NONE, POSITION, VELOCITY, LENGTH, // we separate length and
	// position so that
	// x,y,z are plotted on
	// a different graph
	// from spot sizes
	TIME, // count per frames
	STRING; // for non-numeric features

}