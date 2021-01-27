package budDetector;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;

public class Budregionobject {
    public RandomAccessibleInterval<BitType> Boundaryimage;
    
    public RandomAccessibleInterval<BitType> Interiorimage;
    
    public double size;
    
    public Point minVal;
    
    
    public Budregionobject( RandomAccessibleInterval<BitType> Boundaryimage, RandomAccessibleInterval<BitType> Interiorimage, Point minVal, double size ) {
   	 
   	 this.Boundaryimage = Boundaryimage;
   	 
   	 this.Interiorimage = Interiorimage;
   	 
   	 this.minVal = minVal;
   	 
   	 this.size = size;
   	 
   	 
    }

}
