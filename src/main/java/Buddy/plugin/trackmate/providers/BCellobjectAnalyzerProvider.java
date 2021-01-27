package Buddy.plugin.trackmate.providers;

import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;

/**
 * A provider for the Bcellobject analyzer factories provided in the GUI.
 */
@SuppressWarnings( "rawtypes" )
public class BCellobjectAnalyzerProvider extends AbstractProvider< BCellobjectAnalyzerFactory >
{

	public BCellobjectAnalyzerProvider()
	{
		super( BCellobjectAnalyzerFactory.class );
	}

	public static void main( final String[] args )
	{
		final BCellobjectAnalyzerProvider provider = new BCellobjectAnalyzerProvider();
		System.out.println( provider.echo() );
	}
}
