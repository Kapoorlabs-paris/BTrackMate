package Buddy.plugin.trackmate.providers;

import Buddy.plugin.trackmate.action.TrackMateActionFactory;

public class ActionProvider extends AbstractProvider< TrackMateActionFactory >
{

	public ActionProvider()
	{
		super( TrackMateActionFactory.class );
	}

	public static void main( final String[] args )
	{
		final ActionProvider provider = new ActionProvider();
		System.out.println( provider.echo() );
	}

}
