package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Component;
import models.Log;
import models.Project;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.User;
import notifiers.Notifications;

public class ImpedimentTasks extends SmartController
{
	/**
	 * C4 S12 Rendering the project to the index page in the Impediment task
	 * view
	 * 
	 * @author ahmedkhaled7
	 * @param projectId
	 *            Is the pid of the project we are in
	 */
	public static void index( long projectId )
	{
		Project project = Project.findById( projectId );
		Security.check( project.users.contains( Security.getConnected() ) );
		render( project );
	}

	/**
	 * Sending the description entered by the user and the project we are in ,
	 * to the Task constructor for the impediment task to be created. Rendering
	 * the task id to the page, to be sent to the selectDependentTasks page in
	 * the same view.
	 * 
	 * @author ahmedkhaled7 C4 S12
	 * @param description
	 *            Is the description of the impediment task
	 * @param projectId
	 *            Is the pid of the project we are in
	 */
	public static void save( String description, long projectId )
	{

		Project project = Project.findById( projectId );
		Security.check( Security.getConnected().projects.contains( project ) );
		Task impedimentTask = new Task( description, project ).save();

		// impedimentTask.dependentTasks=dTasks;
		// Logs.notifyUsers(task.reporter.email, "task is impediment",
		// "task is impediment", assignee);
		int taskType = 0;
		impedimentTask.reporter = Security.getConnected();
		for( int i = 0; i < project.taskTypes.size(); i++ )
		{
			if( project.taskTypes.get( i ).name.equalsIgnoreCase( "Impediment" ) )
				taskType = i;
		}
		impedimentTask.taskType = project.taskTypes.get( taskType );
		project.taskTypes.get( taskType ).Tasks.add( impedimentTask );
		project.taskTypes.get( taskType ).save();
		impedimentTask.save();

		long sid;
		if( project.runningSprint() != -1 )
			sid = project.runningSprint();
		else
			sid = project.sprints.get( project.sprints.size() - 1 ).id;
		Sprint s = Sprint.findById( sid );

		impedimentTask.taskSprint = s;
		impedimentTask.save();

		long id = impedimentTask.id;

		renderJSON( id );
	}

	/**
	 * C4 S12 Selecting the tasks that the impediment task depends on.
	 * 
	 * @author ahmedkhaled7
	 * @param projectId
	 *            Is the pid of the project we are in
	 * @param itaskId
	 *            Is the impediment task is
	 */
	public static void selectDependentTasks( long projectId, long itaskId )
	{
		Project project = Project.findById( projectId );
		Security.check( project.users.contains( Security.getConnected() ) );
		List<Task> Tasks = new ArrayList<Task>();
		List<Long> taskIds = new ArrayList<Long>();

		for( Component component : project.components )
		{
			if( !component.deleted )
				for( Task task : component.componentTasks )
				{

					Tasks.add( task );
					taskIds.add( task.id );
				}

		}

		render( itaskId, Tasks, taskIds, projectId, project );
	}

	/**
	 * Adding the dependent task to the impediment task and saving them.
	 * 
	 * @author ahmedkhaled7 C4 S12
	 * @param itaskId
	 *            Is the impediment task is
	 * @param dTasks
	 *            list of dependent tasks
	 */
	public static void save2( long taskId, long[] dTasks )
	{
		Task impedimentTask = Task.findById( taskId );
		Security.check( impedimentTask.project.users.contains( Security.getConnected() ) );
		for( int i = 0; i < dTasks.length; i++ )
		{
			Task n = Task.findById( dTasks[i] );
			impedimentTask.dependentTasks.add( n );
		}

		impedimentTask.save();
		Task j = Task.findById( taskId );

		Sprint s = Sprint.findById( j.taskSprint.id );
		Project project = impedimentTask.taskSprint.project;
		String url = "@{Application.externalOpen("+project.id+", '/components/viewImpedimentLog?Proj_id="+project.id+"', false)}";
		Notifications.notifyProjectUsers(project, "reportImpediment", url, "Impediment Task", ""+impedimentTask.number, (byte) -1);
		// Logs.addLog( project, "added", "Task", impedimentTask.id );
		Log.addUserLog("Added impediment task: " + impedimentTask.getSummary(), project, impedimentTask);
	}

	/**
	 * Renders a list of all the impediment tasks in a certain project.
	 * 
	 * @author Hadeer Younis
	 * @param Proj_id
	 *            is the id of the project.
	 */
	public static void viewImpedimentLog( long Proj_id )
	{
		Project proj = Project.findById( Proj_id );
		Security.check( proj.users.contains( Security.getConnected() ) );
		Boolean canEdit = false;
		if( Security.getConnected().isAdmin )
			canEdit = true;
		else
		{
			for( int i = 0; i < proj.roles.size(); i++ )
			{
				if( proj.roles.get( i ).name.equalsIgnoreCase( "Scrum Master" ) )
					if( proj.roles.get( i ).users.contains( Security.getConnected() ) )
						canEdit = true;

			}
		}

		List<Task> tasks = null;
		for( int i = 0; i < proj.taskTypes.size(); i++ )
		{
			if( proj.taskTypes.get( i ).name.equalsIgnoreCase( "Impediment" ) )
				tasks = proj.taskTypes.get( i ).Tasks;
		}

		String name = proj.name;

		List<TaskStatus> TaskStat = proj.taskStatuses;
		render( tasks, TaskStat, proj, canEdit );
	}

	/**
	 * Updates the status of the impediment task.
	 * 
	 * @author Hadeer Younis
	 * @param taskId
	 *            is the ID of the impediment task to be updated
	 * @param type
	 *            is the name of the new status
	 * @description Updates a task to a new type
	 */
	public static void changeStatus( long taskId, String type )
	{
		Task t = Task.findById( taskId );
		Project proj = t.taskSprint.project;
		Security.check( proj.users.contains( Security.getConnected() ) );
		TaskStatus newType = null;
		for( int i = 0; i < proj.taskStatuses.size(); i++ )
		{
			if( proj.taskStatuses.get( i ).name.equalsIgnoreCase( type ) )
				newType = proj.taskStatuses.get( i );
		}
		t.taskStatus = newType;
		t.save();
		Sprint s = Sprint.findById( t.taskSprint.id );
		List<User> users = new ArrayList<User>( 2 );
		for( int i = 0; i < proj.roles.size(); i++ )
		{
			if( proj.roles.get( i ).name.equalsIgnoreCase( "Scrum Master" ) )
				users = proj.roles.get( i ).users;
		}
		users.add( t.reporter );
		String url = "@{Application.externalOpen("+proj.id+", '/components/viewImpedimentLog?Proj_id="+proj.id+"', false)}";
		Notifications.notifyProjectUsers(proj, "reportImpediment", url, "Impediment Task", ""+t.number, (byte) 0);		
		// Logs.addLog( proj, "updated", "Task", t.id );
		Log.addUserLog("Updated impediment task: " + t.getSummary(), t, t.project);
	}

}
