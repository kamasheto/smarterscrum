package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Component;
import models.Project;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.User;
import play.mvc.With;

@With( Secure.class )
public class SprintBacklog extends SmartController
{

	/**
	 * Renders either a component specific or whole company sprint backlog view.
	 * 
	 * @param component_id
	 *            The given component id.
	 * @param sprint_id
	 *            The given sprint id.
	 *@return void
	 */

	public static void index( long component_id, long sprint_id )
	{

		User user = Security.getConnected();
		Sprint sprint = Sprint.findById( sprint_id );
		Security.check( user.projects.contains( sprint.project ) );
		String cs = "";
		Component component = null;
		if( component_id != 0 )
		{
			component = Component.findById( component_id );
			if( component.deleted )
				notFound();
			cs = component.name;
		}
		ArrayList day_headers = new ArrayList( sprint.getDuration() );
		Project project = Project.findById( sprint.project.id );
		if( project.deleted )
			notFound();
		for( int i = 0; i < sprint.getDuration(); i++ )
		{
			day_headers.add( (i + 1) );
		}
		List<Task> tasks = new ArrayList();
		if( component_id != 0 && sprint_id != 0 )
		{
			tasks = component.returnComponentSprintTasks( sprint );

		}
		else if( component_id == 0 && sprint_id != 0 )
		{
			tasks = sprint.tasks;
		}
		String sprint_number = sprint.number;
		List<TaskType> types = sprint.project.taskTypes;
		List<TaskStatus> statuses = sprint.project.taskStatuses;
		render( tasks, sprint_id, day_headers, sprint_number, component_id, project, cs, types, statuses,sprint );
	}

	/**
	 * Renders the burn down chart for a certain sprint and or certain
	 * component.
	 * 
	 * @author Hadeer Younis
	 * @param sprint_id
	 *            The sprint id.
	 * @param component_id
	 *            The component id.
	 * @return String containing the data of the sprint to draw the burn down
	 *         chart.
	 */
	public static void show_graph( long sprint_id, long component_id )
	{
		Sprint sprint = Sprint.findById( sprint_id );
		Security.check( Security.getConnected().projects.contains( sprint.project ) );
		String data = sprint.fetchData( component_id );
		String name;
		if(component_id !=0 )
		{
			Component component = Component.findById( component_id );
			name = component.getFullName();
		}
		else
			name = sprint.project.name;
		
		if( data.contains( "NONE" ) )
			data = null;
		String sprint_number = sprint.number;
		render( data, sprint_number, name );
	}
}
