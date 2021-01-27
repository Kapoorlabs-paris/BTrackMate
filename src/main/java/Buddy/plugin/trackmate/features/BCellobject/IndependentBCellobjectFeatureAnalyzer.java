package Buddy.plugin.trackmate.features.BCellobject;

import java.util.Iterator;

import budDetector.BCellobject;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;

public abstract class IndependentBCellobjectFeatureAnalyzer< T extends RealType< T >> implements BCellobjectAnalyzer< T >
{

	protected final ImgPlus< T > img;

	protected final Iterator< BCellobject > BCellobjects;

	protected String errorMessage;

	private long processingTime;

	public IndependentBCellobjectFeatureAnalyzer( final ImgPlus< T > img, final Iterator< BCellobject > BCellobjects )
	{
		this.img = img;
		this.BCellobjects = BCellobjects;
	}

	public abstract void process( final BCellobject BCellobject );

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();
		while ( BCellobjects.hasNext() )
		{
			process( BCellobjects.next() );
		}
		processingTime = System.currentTimeMillis() - start;
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

}
