package controllers;

import java.util.List;

import models.Component;
import models.Project;
import models.Sprint;
import play.mvc.With;

@With( Secure.class )
public class ProductBacklogs extends SmartController
{

	/**
	 * @author eabdelrahman
	 * @author Hadeer younis
	 * @param id
	 *            is the ID of the project of the requested chart
	 * @param componentID
	 *            is the ID of the component of the requested chart
	 * @return renders the string containing the data and the method of project
	 *         to generate graph and the sprints in it
	 */

	public static void showGraph( long id, long componentId )
	{
		Project temp = Project.findById( id );
		Security.check( Security.getConnected().projects.contains( temp ) );
		if( temp.deleted )
			notFound();
		String pName = temp.name;
		Component myComponent = Component.findById( componentId );
		if( componentId != 0 )
		{
			if( myComponent.deleted )
				notFound();
		}
		String Data = temp.fetchData( componentId );
		List<Sprint> SprintsInProject = temp.sprints;
		int maxDays = 0;
		for( int i = 0; i < SprintsInProject.size(); i++ )
		{
			if( SprintsInProject.get( i ).tasks.size() == 0 )
				SprintsInProject.remove( i );
			else
			{
				for( int j = 0; j < SprintsInProject.size(); j++ )
				{
					if( !SprintsInProject.get( j ).deleted && SprintsInProject.get( j ).getDuration() >= SprintsInProject.get( i ).getDuration() )
						maxDays = SprintsInProject.get( j ).getDuration();
				}
			}
		}
		if( Data.startsWith( "GenerateFullGraph([[[]]" ) )
			Data = null;
		else
			Data = Data.substring( 0, 18 ) + maxDays + "," + Data.substring( 18 );
		render( Data, SprintsInProject, temp, componentId, myComponent, pName );
	}
}
