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

/**
 * This is the controller method that renders to views.SprintBacklog It is for
 * viewing the sprint either in a backlog or chart.
 * 
 * @author Menna Ghoneim
 */

@With( Secure.class )
public class SprintBacklog extends SmartController
{

	/**
	 * Renders to the sprint backlog view with the list of list of tasks in a
	 * certain sprint are for a certain component, in which each list of list of
	 * tasks encloses tasks in only one story for viewing purposes in the sprint
	 * backlog : 1,4,5,6.
	 * 
	 * @param componentID
	 *            The given compenent id.
	 * @param id
	 *            The given sprint id.
	 *@param projectId
	 *            The id of a given project.
	 *@return void
	 */

	public static void index( long componentID, long id )
	{

		User user = Security.getConnected();
		Sprint sprint = Sprint.findById( id );
		Security.check( user.projects.contains( sprint.project ) );
		String cs = "";
		Component component = null;
		if( componentID != 0 )
		{
			component = Component.findById( componentID );
			if( component.deleted )
				notFound();
			cs = component.name;
		}
		ArrayList daysHeader = new ArrayList( sprint.getDuration() );
		Project project = Project.findById( sprint.project.id );
		if( project.deleted )
			notFound();
		for( int i = 0; i < sprint.getDuration(); i++ )
		{
			daysHeader.add( (i + 1) );
		}
		List<Task> tasks = new ArrayList();
		if( componentID != 0 && id != 0 )
		{
			tasks = component.returnComponentSprintTasks( sprint );

		}
		else if( componentID == 0 && id != 0 )
		{
			tasks = sprint.tasks;
		}
		String sNum = sprint.sprintNumber;
		List<TaskType> types = sprint.project.taskTypes;
		List<TaskStatus> statuses = sprint.project.taskStatuses;
		render( tasks, id, daysHeader, sNum, componentID, project, cs, types, statuses,sprint );

	}

	/**
	 * Renders the burn down chart for a certain sprint and or certain
	 * component.
	 * 
	 * @author eabdelrahman
	 * @author Hadeer Younis
	 * @param id
	 *            The sprint id.
	 * @param cid
	 *            The component id.
	 * @return String containing the data of the sprint to draw the burn down
	 *         chart.
	 */
	public static void showGraph( long id, long componentID )
	{
		Sprint temp = Sprint.findById( id );
		Security.check( Security.getConnected().projects.contains( temp.project ) );
		String Data = temp.fetchData( componentID );
		if( Data.contains( "NONE" ) )
			Data = null;
		render( Data, temp, componentID );
	}
}
