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
 * <p>
 * The first method is for viewing and editing the sprint backlog, the second is
 * for viewing the burn down chart and the third is for assigning a task as
 * impediment.
 * 
 * @author Menna Ghoneim
 */

@With( Secure.class )
// @Check("systemAdmin")
public class SprintBacklog extends SmartController
{

	/**
	 * Renders to the sprint backlog view with the list of list of tasks in a
	 * certain sprint are for a certain component, in which each list of list of
	 * tasks encloses tasks in only one story for viewing purposes in the sprint
	 * backlog : 1,4,5,6.
	 * 
	 * @param componentID
	 *            : given compenent id
	 * @param id
	 *            : given sprint id
	 *@param projectId
	 *            the id of a given project
	 */

	public static void index( long componentID, long id )
	{

		User user = Security.getConnected();
		Sprint sprint = Sprint.findById( id );
		Security.check( user.projects.contains( sprint.project ) );
		String cs ="";
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
			for( int i = 0; i < project.components.size(); i++ )
			{
				for( int j = 0; j < project.components.get( i ).returnComponentTasks( sprint ).size(); j++ )
				{
					if(project.components.get( i ).returnComponentTasks( sprint ).get( j )!= null)
					tasks.add(project.components.get( i ).returnComponentTasks( sprint ).get( j ) );
				}
			}
		}
		String sNum = sprint.sprintNumber;
		boolean flag = user.isAdmin || user.in( project ).can( "editSprintBacklog" );
		render( tasks, id, daysHeader, sNum, componentID, project, flag, cs );

	}

	/**
	 * Renders the burndown chart for a certain sprint and or certain component.
	 * 
	 * @author eabdelrahman
	 * @author Hadeer Younis
	 * @param Sprint
	 *            id
	 * @param cid
	 *            this is the component id
	 * @return String containing the data of the sprint to draw the burn down
	 *         chart
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

	/**
	 * @author menna_ghoneim Renders a given taskId with a list of user and
	 *         option to say if the reviewer or the assignee is being changed to
	 *         a page to choose a reviewer or assignee
	 * @param taskId
	 *            the task to be edited
	 * @param aORr
	 *            whether reviewer or assignee
	 */

	public static List<User> chooseTaskAssiRev( long taskId, int aORr )
	{
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( taskId );

		if( aORr == 0 )
		{
			users = task.component.componentUsers;
			users.remove( task.reviewer );
		}
		else
		{

			users = task.component.componentUsers;
			users.remove( task.assignee );
			if( users.isEmpty() )
				users = task.component.componentUsers;

		}
		return users;
		// render(taskId, users, aORr);
	}

	/**
	 * @author menna_ghoneim Renders a given taskid with a likt of project types
	 *         and the session's user id to a page to choose
	 * @param taskId
	 *            the task to be edited
	 */

	public static List<TaskType> chooseTaskType( long taskId )
	{
		Task task = Task.findById( taskId );
		List<TaskType> types = task.taskSprint.project.taskTypes;
		// User user = Security.getConnected();
		return types;

		// render(taskId, types, user);
	}

	/**
	 * @author menna_ghoneim Renders a given taskId with project statuses and
	 *         the user in the session to a page to choose a task status
	 * @param taskId
	 *            the task to be edited
	 */
	public static List<TaskStatus> chooseTaskStatus( long taskId )
	{
		Task task = Task.findById( taskId );
		List<TaskStatus> states = task.taskSprint.project.taskStatuses;
		// User user = Security.getConnected();

		return states;
		// render(taskId, states, user);
	}
}
