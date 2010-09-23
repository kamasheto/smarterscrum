package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import notifiers.Notifications;

import models.Board;
import models.BoardColumn;
import models.Comment;
import models.Component;
import models.Log;
import models.Meeting;
import models.ProductRole;
import models.Project;
import models.Reviewer;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.CollaborateUpdate;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.Router;
import play.mvc.With;

/**
 * Represents the Task Entity in the Database and it's relations with other
 * entities.
 * 
 * @see models.Task
 */
@With( Secure.class )
public class Tasks extends SmartCRUD
{

	/**
	 * Overrides the CRUD blank method that renders the create form to create a
	 * task.
	 * 
	 * @author Monayri
	 * @param componentId
	 *            The component id that the task is added to.
	 * @param taskId
	 *            The super task id in case the task is a sub task.
	 * @param projectId
	 *            The project id that the task is added to.
	 * @return void
	 */
	public static void blank( long componentId, long taskId, long projectId )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		User user = Security.getConnected();
		Project project = null;
		Component component = null;
		Task task = null;
		Project p = null;
		if( projectId != 0 ) // adding task to project (put drop down list of
		// components)
		{
			p = Project.findById( projectId );
			project = p;
			if( project.deleted )
				notFound();
			Security.check( user.in( project ).can( "AddTask" ) );
		}
		else
		{
			if( componentId != 0 ) // adding task to component
			{
				component = Component.findById( componentId );
				project = component.project;
				if( component.deleted )
					notFound();
				Security.check( user.in( component.project ).can( "AddTask" ) );
			}
			else
			{
				if( taskId != 0 ) // adding subtask to parent task with id
				// taskId
				{
					task = Task.findById( taskId );
					if( task != null && task.deleted )
						notFound();
					if( task.component != null )
					{
						Security.check( user.in( task.component.project ).can( "AddTask" ) );
						project = task.component.project;
					}
					else if( task.project != null )
					{
						Security.check( user.in( task.project ).can( "AddTask" ) );
						project = task.project;
					}

				}
			}
		}

		List<Sprint> sprints = new ArrayList<Sprint>();
		for( int i = 0; i < project.sprints.size(); i++ )
		{
			Sprint sprint = project.sprints.get( i );
			java.util.Date Start = sprint.startDate;
			Calendar cal = new GregorianCalendar();
			if( Start.after( cal.getTime() ) && !sprint.deleted )
			{
				sprints.add( sprint );
			}
		}

		String productRoles = "";
		for( int i = 0; i < project.productRoles.size(); i++ )
		{
			if( project.productRoles.get( i ).name.charAt( 0 ) == 'a' || project.productRoles.get( i ).name.charAt( 0 ) == 'e' || project.productRoles.get( i ).name.charAt( 0 ) == 'i' || project.productRoles.get( i ).name.charAt( 0 ) == 'o' || project.productRoles.get( i ).name.charAt( 0 ) == 'u' || project.productRoles.get( i ).name.charAt( 0 ) == 'A' || project.productRoles.get( i ).name.charAt( 0 ) == 'E' || project.productRoles.get( i ).name.charAt( 0 ) == 'I' || project.productRoles.get( i ).name.charAt( 0 ) == 'O' || project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + project.productRoles.get( i ).name + ",-";
		}

		try
		{
			render( project, p, component, task, type, sprints, productRoles, projectId, componentId, taskId );

		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/blank.html", type );
		}

	}

	/**
	 * Overrides the CRUD create method that is invoked to submit the creation
	 * of the task on the database.
	 * 
	 * @param void
	 * @throws Exception
	 * @return void
	 */
	public static void create() throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.entityClass.newInstance();
		validation.valid( object.edit( "object", params ) );
		Task tmp = (Task) object;
		User user = Security.getConnected();
		Project project = null;
		Component component = null;
		Task task = null;
		Project p = null;
		if( tmp.project != null && tmp.parent == null )
		{
			p = tmp.project;
			project = tmp.project;
			Security.check( Security.getConnected().in( tmp.project ).can( "AddTask" ) );

		}
		else
		{
			if( tmp.parent != null )
			{
				task = tmp.parent;
				project = tmp.project;
			}
			else
			{
				component = tmp.component;
				project = component.project;
				tmp.project = project;
				Security.check( user.in( component.project ).can( "AddTask" ) );
			}
		}
		List<Sprint> sprints = new ArrayList<Sprint>();
		for( int i = 0; i < project.sprints.size(); i++ )
		{
			Sprint sprint = project.sprints.get( i );
			java.util.Date Start = sprint.startDate;
			Calendar cal = new GregorianCalendar();
			if( Start.after( cal.getTime() ) && !sprint.deleted )
			{
				sprints.add( sprint );
			}
		}
		String productRoles = "";
		for( int i = 0; i < project.productRoles.size(); i++ )
		{
			if( project.productRoles.get( i ).name.charAt( 0 ) == 'a' || project.productRoles.get( i ).name.charAt( 0 ) == 'e' || project.productRoles.get( i ).name.charAt( 0 ) == 'i' || project.productRoles.get( i ).name.charAt( 0 ) == 'o' || project.productRoles.get( i ).name.charAt( 0 ) == 'u' || project.productRoles.get( i ).name.charAt( 0 ) == 'A' || project.productRoles.get( i ).name.charAt( 0 ) == 'E' || project.productRoles.get( i ).name.charAt( 0 ) == 'I' || project.productRoles.get( i ).name.charAt( 0 ) == 'O' || project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + project.productRoles.get( i ).name + ",-";
		}
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/blank.html", project, p, component, task, type, sprints, productRoles );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		tmp.init();
		tmp.getProductRole( tmp.description );
		tmp.reporter = Security.getConnected();
		Double t = tmp.estimationPoints;
		if( t.isNaN() )
		{
			tmp.estimationPoints = 0.0;
		}
		if( tmp.parent != null )
		{
			double sum = 0;
			for( int i = 0; i < tmp.parent.subTasks.size(); i++ )
			{
				if( !tmp.parent.subTasks.get( i ).deleted )
				{
					sum = tmp.parent.subTasks.get( i ).estimationPoints + sum;
				}
			}
			tmp.parent.estimationPoints = sum+tmp.estimationPoints;
			tmp.parent.save();
		}
		for( int i = 0; i < tmp.estimationPointsPerDay.size(); i++ )
		{
			tmp.estimationPointsPerDay.set( i, tmp.estimationPoints );
		}
		object.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + tmp.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + tmp.id;
		ArrayList<User> users = new ArrayList<User>();
		if( tmp.assignee != null )
			users.add( tmp.assignee );
		if( tmp.reviewer != null )
			users.add( tmp.reviewer );
		Notifications.notifyUsers( users, "added", url, "task", "task " + tmp.number, (byte) 0, tmp.project );
		Log.addUserLog( "Created new task", tmp, tmp.project );
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{

			if( tmp.parent != null )
			{
				CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "','task-" + tmp.parent.id + "')" );
				Application.overlayKiller( "", "" );
			}
			else
			{
				CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "','task-" + tmp.id + "')" );
				Application.overlayKiller( "", "" );
			}

		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			redirect( request.controller + ".blank" );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/**
	 * Overrides the CRUD show method that renders the edit form.
	 * 
	 * @author Monayri
	 * @param id
	 *            the task been edited id.
	 * @return void
	 */
	public static void show( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) || Security.getConnected() == tmp.assignee || Security.getConnected() == tmp.reviewer );
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		if( comments == null )
			comments = new ArrayList<Comment>();
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		String productRoles = "";
		for( int i = 0; i < tmp.project.productRoles.size(); i++ )
		{
			if( tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'a' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'e' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'i' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'o' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'u' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'A' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'E' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'I' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'O' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + tmp.project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + tmp.project.productRoles.get( i ).name + ",-";
		}
		boolean insprint = false;
		Date now = Calendar.getInstance().getTime();
		if( tmp.taskSprint != null )
		{
			if( tmp.taskSprint.startDate.before( now ) && tmp.taskSprint.endDate.after( now ) )
			{
				insprint = true;
			}
		}
		List<Sprint> sprints = new ArrayList<Sprint>();
		for( Sprint sprint : tmp.project.sprints )
		{
			java.util.Date Start = sprint.startDate;
			Calendar cal = new GregorianCalendar();
			if( Start.after( cal.getTime() ) && !sprint.deleted )
			{
				sprints.add( sprint );
			}
		}
		TaskType taskType = tmp.taskType;
		Component component = tmp.component;
		List<Reviewer> reviewers = new ArrayList();
		List<TaskType> projectTypes = TaskType.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		if( taskType != null )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", tmp.project, true, taskType ).fetch();
		else if( projectTypes.size() > 0 )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", tmp.project, true, projectTypes.get( 0 ) ).fetch();
		List<User> users = new ArrayList<User>();
		for( Reviewer rev : reviewers )
		{
			if( component != null && component.number != 0 )
			{
				if( rev.user.components.contains( component ) && ((tmp.assignee == null) || (tmp.assignee != null && rev.user.id != tmp.assignee.id)) )
					users.add( rev.user );
			}
			else
			{
				if( (tmp.assignee == null) || (tmp.assignee != null && rev.user.id != tmp.assignee.id) )
					users.add( rev.user );
			}
		}
		try
		{
			render( type, object, statuses, types, message2, deletable, comments, productRoles, insprint, sprints, users );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/show.html", type, object );
		}
	}

	/**
	 * Overrides the CRUD save method that is invoked to submit the edit, in
	 * order to check if the edits are acceptable.
	 * 
	 * @author Monayri
	 * @param id
	 *            the id of the task being edited.
	 * @throws Exception
	 * @return void
	 */
	public static void save( String id ) throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		String changes = "";
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) || Security.getConnected() == tmp.assignee || Security.getConnected() == tmp.reviewer );
		List<User> users = tmp.component.componentUsers;
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Task> dependencies = Task.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		String oldDescription = tmp.description;// done
		long oldComponent = 0;
		if( tmp.component != null )
			oldComponent = tmp.component.id;
					
		long oldTaskType;
		if( tmp.taskType != null )
			oldTaskType = tmp.taskType.id;
		else
			oldTaskType = 0;
		long oldTaskStatus;
		if( tmp.taskStatus != null )
			oldTaskStatus = tmp.taskStatus.id;// done
		else
			oldTaskStatus = 0;

		double oldEstPoints = tmp.estimationPoints;
		if( !tmp.subTasks.isEmpty() )
		{
			double sum = 0;
			for( int i = 0; i < tmp.subTasks.size(); i++ )
			{
				sum = tmp.subTasks.get( i ).estimationPoints + sum;
			}
			tmp.estimationPoints = sum;
		}
		if( tmp.parent != null )
		{
			double sum = 0;
			for( int i = 0; i < tmp.parent.subTasks.size(); i++ )
			{
				if( !tmp.parent.subTasks.get( i ).deleted )
				{
					sum = tmp.parent.subTasks.get( i ).estimationPoints + sum;
					System.out.println(sum+"        hola");
				}
			}
			tmp.parent.estimationPoints = sum+tmp.estimationPoints;
			tmp.parent.save();
		}
		long oldAssignee;
		if( tmp.assignee != null )
			oldAssignee = tmp.assignee.id;// done
		else
			oldAssignee = 0;
		long oldReviewer;
		if( tmp.reviewer != null )
			oldReviewer = tmp.reviewer.id;// done
		else
			oldReviewer = 0;
		ArrayList<Task> oldDependencies = new ArrayList<Task>();
		for( Task current : tmp.dependentTasks )
		{
			oldDependencies.add( current );
		}
		object = object.edit( "object", params );
		validation.valid( object );
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/show.html", type, object, users, statuses, types, dependencies, message2, deletable, comments );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object );
			}
		}
		object.save();
		/*********** Changes as Comment by Galal Aly **************/
		tmp.productRole = null;
		tmp.getProductRole( tmp.description );
		// start resetting the deadline
		if( tmp.assignee == null || oldAssignee != tmp.assignee.id )
		{
			tmp.deadline = 0;
		}
		// end resetting the deadline
		tmp.save();
		if( !(tmp.description.equals( oldDescription )) )
			changes += "Description changed from <i>" + oldDescription + "</i> to <i>" + tmp.description + "</i><br>";
		if( tmp.taskType != null && oldTaskType != 0 )
			if( tmp.taskType.id != oldTaskType )
			{
				TaskType temp = TaskType.findById( oldTaskType );
				changes += "Task's Type was changed from <i>" + temp.name + "</i> to <i>" + tmp.taskType.name + "</i><br>";
			}
		if( tmp.taskStatus != null && oldTaskStatus != 0 )
			if( tmp.taskStatus.id != oldTaskStatus )
			{
				TaskStatus temp = TaskStatus.findById( oldTaskStatus );
				changes += "Task's status was changed from <i>" + temp.name + "</i> to <i>" + tmp.taskStatus.name + "</i><br>";
			}
		if( tmp.estimationPoints != oldEstPoints )
			changes += "Estimation points for the task were changed from <i>" + oldEstPoints + "</i> to <i>" + tmp.estimationPoints + "</i><br>";
		if( tmp.assignee != null && oldAssignee != 0 )
		{
			if( tmp.assignee.id != oldAssignee )
			{
				User temp = User.findById( oldAssignee );
				changes += "Task's assignee was changed from <i>" + temp.name + "</i> to <i>" + tmp.assignee.name + "</i><br>";
			}
		}
		else if( tmp.assignee != null && oldAssignee == 0 )
		{
			changes += "Task's assignee is now <i>" + tmp.assignee.name + "</i><br>";
		}
		else if( tmp.assignee == null && oldAssignee != 0 )
		{
			changes += "Task's assignee was removed<br>";
		}
		if( tmp.reviewer != null && oldReviewer != 0 )
		{
			if( tmp.reviewer.id != oldReviewer )
			{
				User temp = User.findById( oldReviewer );
				changes += "Task's reviewer was changed from <i>" + temp.name + "</i> to <i>" + tmp.reviewer.name + "</i><br>";
			}
		}
		else if( tmp.reviewer != null && oldReviewer == 0 )
		{
			changes += "Task's reviewer is now <i>" + tmp.reviewer.name + "</i><br>";
		}
		else if( tmp.reviewer == null && oldReviewer != 0 )
		{
			changes += "Task's reviewer was removed.<br>";
		}
		for( Task oldTask : oldDependencies )
		{
			if( !(tmp.dependentTasks.contains( oldTask )) )
			{
				changes += "Task " + oldTask.number + " was removed from Dependent tasks.<br>";
			}
		}
		for( Task newTask : tmp.dependentTasks )
		{
			if( !(oldDependencies.contains( newTask )) )
			{
				changes += "Task " + newTask.number + " was added to dependent tasks.<br>";
			}
		}
		if( tmp.component != null && oldComponent != 0 )
		{
			if( tmp.component.id != oldComponent )
			{
				Component c = Component.findById( oldComponent );
				changes += "Component changed from <i>" + c.name + "</i> to <i>" + tmp.component.name + "</i><br>";
			}
		}
		else if( tmp.component != null && oldComponent == 0 )
		{
			changes += "Task's component is now <i>" + tmp.component.name + "</i><br>";
		}

		// Now finally save the comment
		if( !changes.equals( "" ) )
		{
			changes = "<font color=\"green\">" + changes;
			changes = changes + "</font>";
			Comment changesComment = new Comment( Security.getConnected(), tmp.id, changes );
			changesComment.save();
		}
		// /********** End of Changes as Comment ********/
		if( tmp.comment != null && tmp.comment.trim().length() != 0 )
		{
			Comment comment = new Comment( Security.getConnected(), tmp.id, tmp.comment );
			comment.save();
		}

		boolean flag = true;
		Date now = Calendar.getInstance().getTime();
		if( tmp.taskSprint != null )
		{
			if( tmp.taskSprint.startDate.before( now ) && tmp.taskSprint.endDate.after( now ) )
			{
				flag = true;
			}
		}
		long compId = 0;
		if( tmp.component != null )
			compId = tmp.component.id;
		if( tmp.taskSprint != null && (!(tmp.description.equals( oldDescription )) || (tmp.assignee != null && oldAssignee != 0 && tmp.assignee.id != oldAssignee) || (tmp.reviewer != null && oldReviewer != 0 && tmp.reviewer.id != oldReviewer) || (tmp.taskType != null && oldTaskType != 0 && tmp.taskType.id != oldTaskType)) )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + tmp.taskSprint.id + "," + tmp.id + "," + compId + ",0)" );
			CollaborateUpdate.update( tmp.project.users, Security.getConnected(), "reload_note_close(" + tmp.taskSprint.id + "," + tmp.id + "," + compId + ");sprintLoad(" + tmp.id + ",'" + tmp.id + "_des')" );
		}

		if( tmp.taskSprint != null && (tmp.component != null && (tmp.assignee != null && oldAssignee != 0 && tmp.assignee.id != oldAssignee)) )
		{
			CollaborateUpdate.update( tmp.project, "drag_note_assignee(" + tmp.taskSprint.id + "," + oldAssignee + "," + tmp.assignee.id + "," + tmp.taskStatus.id + "," + compId + "," + tmp.id + ")" );
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + tmp.taskSprint.id + "," + tmp.id + "," + compId + ",0)" );
			CollaborateUpdate.update( tmp.project.users, Security.getConnected(), "reload_note_close(" + tmp.taskSprint.id + "," + tmp.id + "," + compId + ")" );
		}
		if( tmp.taskSprint != null && tmp.taskStatus != null && oldTaskStatus != 0 && tmp.taskStatus.id != oldTaskStatus )
		{
			CollaborateUpdate.update( tmp.project, "drag_note_status(" + tmp.taskSprint.id + "," + tmp.assignee.id + "," + oldTaskStatus + "," + tmp.taskStatus.id + "," + compId + "," + tmp.id + ")" );
		}

		for( Task subTask : tmp.subTasks )
		{
			subTask.component = tmp.component;
			subTask.assignee = tmp.assignee;
			subTask.reviewer = tmp.reviewer;
			subTask.taskStatus = tmp.taskStatus;
			subTask.taskSprint = tmp.taskSprint;
			subTask.save();
		}
		
		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			CollaborateUpdate.update( tmp.project, "reload('tasks','task-" + tmp.id + "'" + (tmp.parent != null ? ",'task-" + tmp.parent.id + "'" : "") + ")" );
			String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + tmp.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + tmp.id;
			ArrayList<User> nusers = new ArrayList<User>();
			if( tmp.assignee != null )
				nusers.add( tmp.assignee );
			if( tmp.reviewer != null )
				nusers.add( tmp.reviewer );
			if( tmp.reporter != null )
				nusers.add( tmp.reporter );
			User oldAssign = User.findById( oldAssignee );
			User oldrev = User.findById( oldAssignee );
			if( oldAssign != null )
			{
				if( !oldAssign.equals( tmp.assignee ) )
					nusers.add( oldAssign );
			}
			if( oldrev != null )
			{
				if( !oldrev.equals( tmp.reviewer ) )
					nusers.add( oldrev );
			}
			Notifications.notifyUsers( nusers, "edited", url, "task", "task " + tmp.number, (byte) 0, tmp.project );
			Log.addUserLog( "Edit task", tmp, tmp.project );
			Application.overlayKiller( "", "" );
			// Logs.addLog( tmp.project, "edit", "Task", tmp.id );
		}
	}

	/**
	 * Overrides the CRUD delete method that is invoked to delete a task, in
	 * order to delete the task by setting the deleted boolean variable to true
	 * instead of deleting it from the data base.
	 * 
	 * @author Monayri
	 * @param id
	 *            the task id.
	 * @return void
	 */
	public static void delete( long id )
	{
		Task tmp = Task.findById( id );
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		try

		{
			tmp.DeleteTask();
			tmp.save();
			if( tmp.parent != null )
			{
				double sum = 0;
				for( int i = 0; i < tmp.parent.subTasks.size(); i++ )
				{
					if( !tmp.parent.subTasks.get( i ).deleted )
					{
						sum = tmp.parent.subTasks.get( i ).estimationPoints + sum;
					}
				}
				tmp.parent.estimationPoints = sum;
				tmp.parent.save();
				CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "', 'task-" + tmp.id + "', 'task-" + tmp.parent.id + "', 'tasks')" );
			}
			CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "', 'task-" + tmp.id + "', 'tasks')" );
			deleteSubTasks( id );
			renderText( "Task deleted successfully." );

		}
		catch( Exception e )
		{
			flash.error( Messages.get( "crud.delete.error", "Task", tmp.getEntityId() ) );
		}

	}

	/**
	 * Returns a list of all the reviewers on a component
	 * 
	 * @param id
	 *            the component id.
	 * @param id2
	 *            the task assignee id.
	 * @return void
	 */
	public static void reviewers( long id, long id2, long compId, long projId )
	{
		List<User> users = new ArrayList<User>();
		Component component = Component.findById( compId );
		Project project = Project.findById( projId );
		User Assignee = User.findById( id2 );
		if( component != null )
		{
			if( component.number == 0 )
				users = component.project.users;
			else
				users = component.componentUsers;
		}
		else
		{
			users = project.users;
		}
		List<User.Object> reviewers = new ArrayList<User.Object>();
		for( User user : users )
		{
			if( user != Assignee )
			{
				reviewers.add( new User.Object( user.id, user.name ) );
			}
		}
		renderJSON( reviewers );
	}

	/**
	 * Sets a task to be dependent on another task.
	 * 
	 * @param id
	 *            the task id.
	 * @param id2
	 *            the task id.
	 * @return void
	 */
	public static void setDependency( long id, long id2 )
	{
		Task taskFrom = Task.findById( id );
		Task taskTo = Task.findById( id2 );
		Security.check( Security.getConnected().in( taskFrom.project ).can( "modifyTask" ) );
		taskFrom.dependentTasks.add( taskTo );
		taskFrom.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + taskFrom.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + taskFrom.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( taskFrom.assignee != null )
			nusers.add( taskFrom.assignee );
		if( taskFrom.reviewer != null )
			nusers.add( taskFrom.reviewer );
		if( taskFrom.reporter != null )
			nusers.add( taskFrom.reporter );
		if( taskTo.assignee != null )
			nusers.add( taskTo.assignee );
		if( taskTo.reviewer != null )
			nusers.add( taskTo.reviewer );
		if( taskTo.reporter != null )
			nusers.add( taskTo.reporter );
		Notifications.notifyUsers( nusers, "added", url, "task " + taskTo.number + "to the dependent tasks on", "task " + taskFrom.number, (byte) 0, taskFrom.project );
	}

	/**
	 * Saves a specific effort of a given day for a certain task in a specific
	 * sprint. It also Notifies all the users in the corresponding component of
	 * the change and type of change. It also logs the change.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            The id of the task to be updated.
	 * @param effort
	 *            The effort points of a specific day.
	 * @param day
	 *            The number of the day to which the effort belongs.
	 * @return void
	 */
	public static void enterEffort( long id, double effort, int day )
	{
		Task temp = Task.findById( id );
		Security.check( Security.getConnected().in( temp.project ).can( "modifyTask" ) || temp.assignee == Security.getConnected() );
		Double e = effort;
		if( e.isNaN() )
		{
			renderText( "Please enter a number!" );
		}
		else if( temp.estimationPoints < effort )
		{
			renderText( "The entered effort cannot be more than the estimated effort" );
		}
		else if( effort < 0 )
		{
			renderText( "The entered effort cannot be less than 0" );
		}

		temp.setEffortOfDay( effort, day );
		CollaborateUpdate.update( temp.project, "sprintLoad(" + id + ",'" + id + "_day_" + day + "');" );
		temp.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + temp.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + temp.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( temp.assignee != null )
			nusers.add( temp.assignee );
		if( temp.reviewer != null )
			nusers.add( temp.reviewer );
		if( temp.reporter != null )
			nusers.add( temp.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the effort points for the", "task " + temp.number, (byte) 0, temp.project );
		Log.addLog( "Effort entered for task", Security.getConnected(), temp, temp.project );
		renderText( "Effort changed successfully" );
	}

	/**
	 * Returns a the inputed date in the yyyy-mm-dd format
	 * 
	 * @author Hadeer Youni
	 * @param c
	 *            , A certain date
	 * @return
	 */
	public static String getStrDate( GregorianCalendar c )
	{
		int m = c.get( GregorianCalendar.MONTH ) + 1;
		int d = c.get( GregorianCalendar.DATE );
		String mm = Integer.toString( m );
		String dd = Integer.toString( d );
		return "" + c.get( GregorianCalendar.YEAR ) + '-' + (m < 10 ? "0" + mm : mm) + '-' + (d < 10 ? "0" + dd : dd);
	}

	/**
	 * Fetches all the data needed to generate a report on a given task.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , The id of the task whose report will be generated.
	 * @return void
	 */
	public static void getReport( long id )
	{
		Task theTask = Task.findById( id );
		List<Log> temp = theTask.logs;
		Security.check( theTask.project.users.contains( Security.getConnected() ) );
		if( theTask.deleted )
			notFound();
		System.out.println( temp );
		boolean empty = temp.isEmpty();
		String lastModified = null;
		int numberOfModifications = 0;
		String efforts = "[";
		boolean flag = false;
		double n = theTask.getEffortPerDay( 0 );
		String changes = "[";
		if( theTask.taskSprint != null )
		{
			for( int j = 0; j < theTask.taskSprint.getDuration(); j++ )
			{
				if( !flag )
					n = theTask.getEffortPerDay( j );
				if( n == -1 )
				{
					flag = true;
					n = theTask.getEffortPerDay( j - 1 );
				}
				if( j == theTask.taskSprint.getDuration() - 1 )
					efforts = efforts + "[" + j + "," + n + "]]";
				else
					efforts = efforts + "[" + j + "," + n + "],";
			}
		}
		else
		{
			efforts = "[]";
		}
		for( int i = 0; i < temp.size(); i++ )
		{
			int k = 1;

			GregorianCalendar today = new GregorianCalendar();
			today.setTimeInMillis( temp.get( i ).timestamp );
			if( i < temp.size() - 1 )
			{
				GregorianCalendar tomorrow = new GregorianCalendar();
				tomorrow.setTimeInMillis( temp.get( i + 1 ).timestamp );

				keepLoop : while( getStrDate( today ).equals( getStrDate( tomorrow ) ) )
				{
					i++;
					k++;
					if( i == temp.size() - 1 )
						break keepLoop;

				}
			}
			if( i == temp.size() - 1 )
				changes = changes + "['" + getStrDate( today ) + "'," + k + "]]";
			else
				changes = changes + "['" + getStrDate( today ) + "'," + k + "],";

		}

		if( !empty )
		{
			lastModified = new Date( temp.get( temp.size() - 1 ).timestamp ).toString().substring( 0, 10 ) + " @ " + new Date( temp.get( temp.size() - 1 ).timestamp ).toString().substring( 11 );
			numberOfModifications = temp.size();
		}
		GregorianCalendar maxdate = new GregorianCalendar();
		maxdate.setTimeInMillis( temp.get( temp.size() - 1 ).timestamp );
		if( !temp.isEmpty() || temp.size() == 1 )
			maxdate.setTimeInMillis( temp.get( temp.size() - 1 ).timestamp + (3 * 86400000) );
		String maxDate = getStrDate( maxdate );
		GregorianCalendar mindate = new GregorianCalendar();
		mindate.setTimeInMillis( temp.get( 0 ).timestamp );
		if( !temp.isEmpty() || temp.size() == 1 )
			mindate.setTimeInMillis( temp.get( 0 ).timestamp - (3 * 86400000) );
		String minDate = getStrDate( mindate );
		Project myProject = theTask.project;
		render( myProject, minDate, temp, lastModified, empty, efforts, changes, numberOfModifications, theTask, maxDate );
	}

	/**
	 * This method changes the given task description.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            , The id of the given task.
	 * @param userId
	 *            , The id of the user who will do the change in description.
	 * @param desc
	 *            , The new description.
	 * @return boolean
	 */
	public static boolean editTaskDesc2( long id, long userId, String desc )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskDescreption" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}
		task1.description = desc;
		task1.save();
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.taskSprint != null )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.taskSprint.id + "," + task1.id + "," + compId + "," + userId + ")" );
			CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.taskSprint.id + "," + task1.id + "," + compId + ");sprintLoad(" + task1.id + ",'" + task1.id + "_des')" );
		}
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		if( userId == Security.getConnected().id )
		{
			// Logs.addLog( user1, "Edit", "Task Description", id,
			// task1.project, new Date( System.currentTimeMillis() ) );
			Log.addUserLog( "Edited task description", task1, task1.project );
		}
		else
		{
			Log.addUserLog( user1.name + " has edited task description", task1, user1, task1.project );
			// Logs.addLog( user1 +
			// " has performed action (Edit) using resource (Task Description) in project "
			// + task1.project.name + " from the account of " +
			// Security.getConnected().name );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the description of the", "task " + task1.number, (byte) 0, task1.project );
		renderText( "The description was changed successfully" );
		return true;
	}

	/**
	 * FilterS for task id and new status and user_id.
	 * 
	 * @author josephhajj
	 * @param id
	 *            The Sprint id.
	 * @param columnSequence
	 *            The position of the column indicating this task status on the
	 *            board.
	 * @param taskString
	 *            The task status sticky note id = "task-" + task.id
	 * @param user_id
	 *            Tthe user id.
	 * @return void
	 */
	public static void changeTaskStatusHelper( long id, int columnSequence, String taskString, long user_id, long cid )
	{
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}

		// setting the variable needed for the method
		// defining the appropriate sprint
		Sprint s = Sprint.findById( id );
		// defining the project of the sprint
		Project p = s.project;
		// defining the board of the project
		Board b = p.board;
		Component component = null;
		if( cid != 0 )
			component = Component.findById( cid );
		// defining the status
		TaskStatus status = new TaskStatus();
		// defining the cols of the board only
		// List<Column> cols = b.columns;
		// defining the final task id and its helper string
		String task_id_helper[];
		String task_id_helper2[];
		long task_id;
		// defining a flag for the second loop
		// boolean flag = true;

		// getting the actual status
		BoardColumn col;
		if( cid == 0 )
			col = BoardColumn.find( "bySequenceAndBoard", columnSequence, b ).first();
		else
			col = BoardColumn.find( "bySequenceAndBoard", columnSequence, component.componentBoard ).first();
		status = col.taskStatus;

		// get the actual task_id in an int
		task_id_helper = taskString.split( "_" );
		task_id_helper2 = task_id_helper[0].split( "-" );
		task_id = Integer.parseInt( task_id_helper2[1] );
		editTaskStatus( task_id, user_id, status );
	}

	/**
	 * Filters for taskid and user_id and the new assignee.
	 * 
	 * @author josephhajj
	 * @param id
	 *            The component id.
	 * @param taskString
	 *            The task status sticky note id = "task-" + task.id
	 * @param user_id
	 *            The user id.
	 * @param row
	 *            The row id.
	 * @return void
	 */
	public static void changeTaskAssigneeHelper( long id, String taskString, long user_id, int row )
	{
		// if user is not selected take the one in the session
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}

		// getting the whole list of users
		// User user = User.findById(user_id);
		Component component = Component.findById( id );
		List<User> users = component.componentUsers;

		String task_id_helper[];
		String task_id_helper2[];
		long task_id;

		// filtering the task id
		task_id_helper = taskString.split( "_" );
		task_id_helper2 = task_id_helper[0].split( "-" );
		task_id = Integer.parseInt( task_id_helper2[1] );

		// calling the method
		editTaskAssignee2( task_id, user_id, users.get( row ).id );

	}

	/**
	 * Changes the given task status.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param newStatus
	 *            The new task status.
	 * @param userId
	 *            The id of the user who will change the task status.
	 * @return boolean
	 */
	public static boolean editTaskStatus( long id, long userId, TaskStatus newStatus )
	{
		Task task1 = Task.findById( id );
		if( task1 == null )
			return false;
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;
		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 );
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskStatus" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}
		long oldstatus = task1.taskStatus.id;
		task1.taskStatus = newStatus;
		task1.save();
		long newstatus = task1.taskStatus.id;
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		CollaborateUpdate.update( task1.project, "drag_note_status(" + task1.taskSprint.id + "," + task1.assignee.id + "," + oldstatus + "," + newstatus + "," + compId + "," + task1.id + ")" );
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog( "Edited task status", task1, task1.project );

		}
		else
		{
			Log.addUserLog( user1.name + " edited task status", user1, task1, task1.project );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the status of the", "task " + task1.number, (byte) 0, task1.project );
		return true;
	}

	/**
	 * Changes the status of a given task to the specified status
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , the id of the task to be edited
	 * @param statusId
	 *            , the id of the new status
	 */
	public static void changeTaskStatus( long id, long statusId )
	{
		Task task = Task.findById( id );
		TaskStatus stat = TaskStatus.findById( statusId );
		if( task == null )
			notFound();
		if( stat == null )
			notFound();
		Security.check( Security.getConnected().in( task.project ).can( "modifyTask" ) || task.assignee == Security.getConnected() );
		task.taskStatus.Tasks.remove( task );
		task.taskStatus.save();
		task.taskStatus = stat;
		if( task.parent != null )
		{
			boolean flag = true;
			for( int i = 0; i < task.parent.subTasks.size(); i++ )
			{
				if( task.parent.subTasks.get( i ).taskStatus != stat )
					flag = false;
			}
			if( flag )
			{
				task.parent.taskStatus = stat;
				task.parent.save();
			}
			CollaborateUpdate.update( task.project, "sprintLoad(" + task.parent.id + ",'" + task.parent.id + "_status');" );
		}
		if( task.subTasks.size() > 0 )
		{
			for( int i = 0; i < task.subTasks.size(); i++ )
			{
				task.subTasks.get( i ).taskStatus = stat;
				task.subTasks.get( i ).save();
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_status');" );

			}
		}
		CollaborateUpdate.update( task.project, "reload('task-" + id + "');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + id + ",'" + id + "_status');" );
		task.save();
		renderText( "Status updates successfully" );
		Log.addUserLog( "Edited task status", task, task.project );
	}

	/**
	 * Changes the given task estimation points.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param estimation
	 *            The value of the new estimation.
	 * @return boolean
	 */
	public static boolean editTaskEstimation( long id, double estimation )
	{
		Task task = Task.findById( id );
		Security.check( Security.getConnected().in( task.project ).can( "modifyTask" ) || task.assignee == Security.getConnected() );
		if( task == null )
			notFound();
		if( estimation < 0 )
		{
			renderText( "Please enter a number more than 0" );
		}
		Double e = estimation;
		if( e.isNaN() )
		{
			renderText( "Please enter a number!" );
		}
		task.estimationPoints = estimation;
		task.save();

		CollaborateUpdate.update( task.project, "reload('task-" + id + "');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + id + ",'" + id + "_points');" );
		if( task.parent != null )
		{
			task.parent.estimationPoints = 0;
			for( int i = 0; i < task.parent.subTasks.size(); i++ )
			{
				task.parent.estimationPoints += task.parent.subTasks.get( i ).estimationPoints;
			}
			task.parent.save();
			CollaborateUpdate.update( task.project, "sprintLoad(" + task.parent.id + ",'" + task.parent.id + "_points');" );
		}
		Log.addUserLog( "Edit task estimation", task, task.project );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the estimation points of the", "task " + task.number, (byte) 0, task.project );
		renderText( "The task's total points was updated successfully" );
		return true;
	}

	/**
	 * This method changes the given task assignee.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param userId
	 *            The id of the user who will do the change.
	 * @param assigneId
	 *            The id of the user who will be the assignee of the task.
	 * @return boolean
	 */
	public static boolean editTaskAssignee2( long id, long userId, long assigneeId )
	{
		Task task1 = Task.findById( id );
		if( task1 == null )
			return false;
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;
		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 );

		User oldAssignee = task1.assignee;
		User assignee = User.findById( assigneeId );
		if( assignee == null )
			return false;
		if( task1.reviewer.getId() == assigneeId )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeAssignee" );

		if( !permession )
			return false;
		// String oldAssignee = task1.assignee.name;

		User oldassi = task1.assignee;

		task1.assignee = assignee;
		if( !oldAssignee.equals( assignee ) )
		{
			task1.deadline = 0;
		}
		task1.save();

		long newassi = task1.assignee.id;

		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		assignee.tasks.add( task1 );
		assignee.save();
		if( task1.taskSprint != null )
		{
			if( compId != 0 )

			{
				CollaborateUpdate.update( task1.project, "drag_note_assignee(" + task1.taskSprint.id + "," + oldassi.id + "," + newassi + "," + task1.taskStatus.id + "," + compId + "," + task1.id + ")" );

				CollaborateUpdate.update( Security.getConnected(), "note_open(" + task1.taskSprint.id + "," + task1.id + "," + compId + "," + userId + ")" );
				CollaborateUpdate.update( task1.project.users, Security.getConnected(), "note_close(" + task1.taskSprint.id + "," + task1.id + "," + compId + ")" );
			}
			else

			{
				CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.taskSprint.id + "," + task1.id + "," + compId + "," + userId + ")" );
				CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.taskSprint.id + "," + task1.id + "," + compId + ")" );
			}
		}
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		if( oldassi != null )
			nusers.add( oldassi );
		Notifications.notifyUsers( nusers, "changed", url, "the assignee of the", "task " + task1.number, (byte) 0, task1.project );
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog( "Edited task assignee", task1, task1.project );
			// Logs.addLog( user1, "Edit", "Task Assignee", id, task1.project,
			// new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Log.addUserLog( user1.name + " has edited task assignee", user1, task1, task1.project );
			// Logs.addLog( user1 +
			// " has performed action (Edit) using resource (Task Assignee) in project "
			// + task1.project.name + " from the account of " +
			// Security.getConnected().name );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		return true;
	}

	/**
	 * Changes the given task reviewer.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param userId
	 *            The id of the user who will be doing the change.
	 * @param reviewerId
	 *            The id of the user who will be the reviewer of the task.
	 * @return boolean
	 */
	public static boolean editTaskReviewer2( long id, long userId, long reviewerId )
	{
		Task task1 = Task.findById( id );
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;
		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 );
		if( task1 == null )
			return false;
		User reviewer = User.findById( reviewerId );
		if( reviewer == null )
			return false;
		if( task1.assignee.getId() == reviewerId )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeReviewer" );

		if( !permession )
			return false;
		User oldReviewer = task1.reviewer;
		task1.reviewer = reviewer;
		task1.save();
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.taskSprint != null )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.taskSprint.id + "," + task1.id + "," + compId + "," + userId + ")" );
			CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.taskSprint.id + "," + task1.id + "," + compId + ")" );
		}
		reviewer.tasks.add( task1 );
		reviewer.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		if( oldReviewer != null )
			nusers.add( oldReviewer );
		Notifications.notifyUsers( nusers, "changed", url, "the reviewer of the", "task " + task1.number, (byte) 0, task1.project );
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog( "Edit task reviewer", task1, task1.project );
			// Logs.addLog( user1, "Edit", "Task Reviewer", id, task1.project,
			// new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Log.addUserLog( user1.name + " has edited task reviewer", user1, task1, task1.project );
			// Logs.addLog( user1 +
			// " has performed action (Edit) using resource (Task Reviewer) in project "
			// + task1.project.name + " from the account of " +
			// Security.getConnected().name );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		return true;
	}

	/**
	 * This method changes the given task type.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param type
	 *            The new Task Type.
	 * @param userId
	 *            The id of the user who will change the task Type.
	 * @return boolean
	 */
	public static boolean editTaskType( long id, long typeId, long userId )
	{
		Task task1 = Task.findById( id );
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;

		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 || task1.reviewer == user1 );
		if( task1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskType" );
		if( task1.reviewer != user1 && task1.assignee != user1 )
		{
			if( !permession )
				return false;
		}
		TaskType type = TaskType.findById( typeId );
		if( task1.subTasks.size() > 0 )
		{
			String n = "";
			for( int i = 0; i < task1.subTasks.size(); i++ )
			{
				task1.subTasks.get( i ).taskType = type;
				task1.subTasks.get( i ).save();
				n += "sprintLoad(" + task1.subTasks.get( i ).id + ",'" + task1.subTasks.get( i ).id + "_type');";
			}
			CollaborateUpdate.update( task1.project, n );

		}
		TaskType oldType = task1.taskType;
		if(task1.taskType != type)
			task1.reviewer = null;
		task1.taskType = type;
		task1.save();

		CollaborateUpdate.update( task1.project, "parent_message_bar('The task type was changed successfully');" );
		CollaborateUpdate.update( task1.project, "sprintLoad(" + id + ",'" + id + "_type');" );
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.taskSprint != null )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.taskSprint.id + "," + task1.id + "," + compId + "," + userId + ")" );
			CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.taskSprint.id + "," + task1.id + "," + compId + ")" );
		}
		if( task1.subTasks.size() > 0 )
		{
			for( int i = 0; i < task1.subTasks.size(); i++ )
			{
				task1.subTasks.get( i ).taskType = type;
				if(oldType != task1.taskType)
					task1.subTasks.get( i ).reviewer = null;
				task1.subTasks.get( i ).save();
			}
		}
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the type of the", "task " + task1.number, (byte) 0, task1.project );

		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );

		return true;
	}

	public static void chooseTaskPerson()
	{
		render();
	}

	/**
	 * Renders a list of the users on the component to select an assignee from
	 * them.
	 * 
	 * @author Dina Helal
	 * @param taskId
	 *            The task to be edited.
	 * @param compId
	 *            Component of the users.
	 */

	public static void chooseTaskAssi( long taskId, long compId, long userId )
	{
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( taskId );
		Component c = Component.findById( compId );
		users = c.componentUsers;
		for(User u : users)
		{
			if(u.deleted)
				users.remove( u );
		}
		users.remove( task.reviewer );
		render( task, users, user1 );
	}

	/**
	 * Renders a list of the users on the component except the assignee to
	 * select an assignee from them.
	 * 
	 * @author Dina Helal
	 * @param taskId
	 *            The task to be edited.
	 * @param compId
	 *            Component of the users.
	 * @return void
	 */

	public static void chooseRev( long taskId, long compId, long userId )
	{
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		Task task = Task.findById( taskId );
		Component component = Component.findById( compId );
		List<Reviewer> reviewers = new ArrayList();
		if(task.taskType!=null)
		reviewers = Reviewer.find("byProjectAndAcceptedAndtaskType", task.project, true, task.taskType).fetch();
	
		List<User> users = new ArrayList<User>();
		for(Reviewer rev : reviewers){
		if(component!=null && component.number!=0)
		{
			if(rev.user.components.contains(component) && ((task.assignee==null)||(task.assignee!=null && rev.user.id!= task.assignee.id)) )
				users.add(rev.user);
		}
		else
		{
			if((task.assignee==null)||(task.assignee!=null && rev.user.id!= task.assignee.id))
			users.add(rev.user);
		}
		}
		User user1 = User.findById( userId );
		users.remove( task.assignee );
		render( task, users, user1 );
	}

	/**
	 * Takes a task id, and renders it to a page to choose task type.
	 * 
	 * @author dina_helal
	 * @param taskId
	 *            The task id to be edited.
	 * @param userId
	 *            The user id who is editing the task.
	 * @return void
	 */
	public static void chooseType( long taskId, long userId )
	{
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		Task task = Task.findById( taskId );
		List<TaskType> types = TaskType.find("byProjectAndDeleted", task.project, false).fetch();
		render( task, types, userId );
	}

	/**
	 * Takes renders to the view task(s) and the title and the project id and an
	 * indicator to list the tasks
	 * 
	 * @param projectId
	 *            The task'(s) project id.
	 * @param componentId
	 *            The task'(s) component id.
	 * @param mine
	 *            An indicator whether the list of my tasks or all project
	 *            tasks.
	 * @param meetingId
	 *            The meeting id.
	 * @param taskId
	 *            The task id.
	 * @return void
	 */
	public static void magicShow( long projectId, long componentId, int mine, long meetingId, long taskId )
	{
		String title;
		if( componentId != 0 )
		{
			Component component = Component.findById( componentId );
			title = "C" + component.number + ": Tasks";
			List<Task> tasks = new ArrayList<Task>();
			tasks = Task.find( "byComponentAndDeleted", component, false ).fetch();
			int counter = tasks.size();
			for( int i = 0; i < tasks.size(); i++ )
			{

				if( tasks.contains( tasks.get( i ).parent ) )
				{
					tasks.remove( tasks.get( i ) );
					i = 0;
					counter--;
				}

			}
			boolean isComponent = true;
			projectId = component.project.id;
			render( counter, tasks, title, mine, projectId, isComponent );
		}
		else
		{
			if( taskId != 0 )
			{
				Task task = Task.findById( taskId );
				if( task.deleted )
					notFound();
				if( task.parent != null )
					title = "Task " + task.parent.number + "." + task.number;
				else
					title = "Task " + task.number;
				List<Task> tasks = new ArrayList<Task>();
				for( Task task2 : task.subTasks )
				{
					if( !task2.deleted )
						tasks.add( task2 );
				}
				int counter = tasks.size();
				projectId = task.project.id;
				render( counter, task, title, tasks, projectId );
			}
			else
			{
				if( mine == 1 )
				{
					title = "My Tasks";
					User user = Security.getConnected();
					Project project = Project.findById( projectId );
					List<Task> tasks = new ArrayList<Task>();
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.assignee != null && task1.assignee.equals( user ) && task1.checkUnderImpl() && task1.taskStatus != null && !task1.taskStatus.closed )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.reviewer != null && task1.reviewer.equals( user ) && task1.checkUnderImpl() && task1.taskStatus != null && task1.taskStatus.pending )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.assignee != null && task1.assignee.equals( user ) && task1.taskStatus != null && !task1.taskStatus.closed && !tasks.contains( task1 ) )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.reviewer != null && task1.reviewer.equals( user ) && task1.taskStatus != null && task1.taskStatus.pending && !tasks.contains( task1 ) )
						{
							tasks.add( task1 );
						}
					}
					int counter = tasks.size();
					render( counter, tasks, title, mine, projectId );
				}
				else
				{
					if( projectId != 0 )
					{
						title = "Project Tasks";
						Project project = Project.findById( projectId );
						List<Task> tasks = Task.find( "byProjectAndDeletedAndParentIsNull", project, false ).fetch();
						boolean isProject = true;
						int counter = tasks.size();
						render( counter, tasks, title, mine, projectId, isProject );
					}
					else
					{
						if( meetingId != 0 )
						{
							Meeting meeting = Meeting.findById( meetingId );
							List<Task> tasks = new ArrayList<Task>();
							for( Task task2 : meeting.tasks )
							{
								if( !task2.deleted )
								{
									tasks.add( task2 );
								}
							}

							title = "Meeting " + meeting.name + " Tasks";
							int counter = tasks.size();
							projectId = meeting.project.id;
							render( counter, title, tasks, projectId );
						}
					}
				}
			}
		}
	}

	/**
	 * Associates task to component.
	 * 
	 * @author mahmoudsakr
	 * @param taskId
	 *            The task id.
	 * @param componentId
	 *            The component id.
	 * @return void
	 */
	public static void associateToComponent( long taskId, long componentId )
	{
		Task task = Task.findById( taskId );
		Component component = Component.findById( componentId );
		User connected = Security.getConnected();
		boolean flag = false;
		Date now = Calendar.getInstance().getTime();
		if( task.taskSprint != null )
		{
			if( task.taskSprint.startDate.before( now ) && task.taskSprint.endDate.after( now ) )
			{
				flag = true;
			}
		}
		Security.check( connected.in( task.project ).can( "modifyTask" ) && task.project == component.project && task.component.project == component.project && task.parent == null && !flag );

		// first remove task from the component
		task.component.componentTasks.remove( task );
		task.component.save();

		task.component = component;
		task.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/components/viewthecomponent?componentId=" + component.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		for( User u : component.componentUsers )
		{
			if( !nusers.contains( u ) )
				nusers.add( u );
		}
		Notifications.notifyUsers( nusers, "associated", url, "task " + task.number + " to the component", component.getFullName(), (byte) 0, task.project );
		Log.addUserLog( "Assigned task to component", task, component, component.project );
		CollaborateUpdate.update( task.project, "reload('component-" + componentId + "', 'task-" + taskId + "')" );
		renderText( "Associated successfully" );
	}

	/**
	 * Assigns a given user as an assignee for a given task.
	 * 
	 * @param taskId
	 *            The task id.
	 * @param assigneeId
	 *            The user id.
	 * @return void
	 */
	public static void assignTaskAssignee( long taskId, long assigneeId )
	{
		Task task = Task.findById( taskId );
		User user = User.findById( assigneeId );
		User connected = Security.getConnected();
		if( task.reviewer == user )
			renderText( "The reviewer can't be the assignee" );
		if( task.component.number != 0 && !user.components.contains( task.component ) )
			renderText( "The task & the assignee can't be in different components" );
		Security.check( connected.in( task.project ).can( "modifyTask" ) && user.projects.contains( task.project ) && task.reviewer != user && (task.component == null || task.component.number == 0 || user.components.contains( task.component )) );
		task.assignee = user;
		task.save();

		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "assigned", url, user.name + " to the", "task " + task.number, (byte) 0, task.project );
		Log.addUserLog( "Assigned task assignee", task, user, task.project );
		CollaborateUpdate.update( task.project, "reload('task-" + taskId + "');" );
		if( task.subTasks.size() > 0 )
		{
			for( int i = 0; i < task.subTasks.size(); i++ )
			{
				task.subTasks.get( i ).assignee = user;
				task.subTasks.get( i ).save();
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_reviewer');" );
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_assignee');" );
			}
		}
		CollaborateUpdate.update( task.project, "sprintLoad(" + taskId + ",'" + taskId + "_reviewer');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + taskId + ",'" + taskId + "_assignee');" );
		renderText( "Assignee added successfully" );
	}

	/**
	 * Assigns a given user as a reviewer for a given task.
	 * 
	 * @param taskId
	 *            The task id.
	 * @param reviewerId
	 *            The user id.
	 * @return void
	 */
	public static void assignTaskReviewer( long reviewerId, long taskId )
	{
		Task task = Task.findById( taskId );
		User user = User.findById( reviewerId );
		User connected = Security.getConnected();
		if( task.assignee == user )
			renderText( "The assignee can't be the reviewer" );
		if( task.component.number != 0 && !(user.components.contains( task.component )) )
			renderText( "The task & the reviewer can't be in different components" );
		Security.check( connected.in( task.project ).can( "modifyTask" ) && user.projects.contains( task.project ) && task.assignee != user && (task.component == null || task.component.number == 0 || user.components.contains( task.component )) && (task.taskType != null) );
		Component component = null;
		if( task.component != null && task.component.number != 0 )
			component = task.component;
		List<Reviewer> reviewers = new ArrayList();
		reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", task.taskType.project, true, task.taskType ).fetch();
		List<User> users = new ArrayList<User>();
		for( Reviewer rev : reviewers )
		{
			if( component != null )
			{
				if( rev.user.components.contains( component ) )
					users.add( rev.user );
			}
			else
			{
				users.add( rev.user );
			}
		}
		if( users.contains( user ) )
		{
			task.reviewer = user;
			task.save();
		}
		else
		{
			renderText( user.name + " is not a reviewer for task type " + task.taskType.name );
		}
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "assigned", url, user.name + " to review the", "task " + task.number, (byte) 0, task.project );
		Log.addUserLog( "Assigned task reviewer", task, user, task.project );
		CollaborateUpdate.update( task.project, "reload('task-" + taskId + "');" );
		if( task.subTasks.size() > 0 )
		{
			for( int i = 0; i < task.subTasks.size(); i++ )
			{
				task.subTasks.get( i ).reviewer = user;
				task.subTasks.get( i ).save();
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_reviewer');" );
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_assignee');" );
			}
		}
		CollaborateUpdate.update( task.project, "sprintLoad(" + taskId + ",'" + taskId + "_reviewer');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + taskId + ",'" + taskId + "_assignee');" );
		renderText( "Reviewer assigned successfully" );
	}

	/**
	 * Renders a list of users on a component. In case the user is not in a
	 * component it renders all the users on the project.
	 * 
	 * @param cid
	 *            The component id.
	 * @return void
	 */
	public static void componentUsers( long cid )
	{
		Component c = Component.findById( cid );
		List<User> users = new ArrayList<User>();
		if( c.number == 0 )
		{
			users = c.project.users;
		}
		else
		{
			users = c.componentUsers;
		}
		List<User.Object> u = new ArrayList<User.Object>();
		for( User user : users )
		{
			u.add( new User.Object( user.id, user.name ) );
		}
		renderJSON( u );
	}

	/**
	 * Set a deadline for a task to remind the assignee to it.
	 * 
	 * @param id
	 */
	public static void setDeadline( long id )
	{
		Task task = Task.findById( id );
		if( !task.assignee.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		else
			render( task );
	}

	public static void changeTaskDeadline( long id, long newDeadline )
	{
		Task task = Task.findById( id );
		if( !task.assignee.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		if( newDeadline < new Date().getTime() )
		{
			renderJSON( false );
		}
		task.deadline = newDeadline;
		task.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the deadline of the", "task " + task.number, (byte) 0, task.project );
		CollaborateUpdate.update( Security.getConnected(), "reload('task-" + task.id + "','tasks" + task.project.id + "')" );
		renderJSON( true );

	}

	/**
	 * remove the task deadline
	 * 
	 * @param id
	 */
	public static void removeTaskDeadline( long id )
	{
		Task task = Task.findById( id );
		if( !task.assignee.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		task.deadline = 0;
		task.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/magicShow?taskId=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "removed", url, "the deadline of the", "task " + task.number, (byte) -1, task.project );
		CollaborateUpdate.update( Security.getConnected(), "reload('task-" + task.project.id + "')" );
		renderJSON( true );
	}

	/**
	 * reloads the task when the deadline comes
	 * 
	 * @param id
	 */
	public static void reloadTask( long id )
	{
		Task task = Task.findById( id );
		while( task.deadline >= new Date().getTime() )
		{

		}
		CollaborateUpdate.update( Security.getConnected(), "reload('task-" + task.id + "','tasks-" + task.project.id + "')" );
	}

	/**
	 * A method that renders the reviewer of a certain type in a certain
	 * component. and if that reviewer doesn't exist then it returns the
	 * component users.
	 * 
	 * @param typeId
	 *            the Id of the required type to be reviewed.
	 * @param componentId
	 *            the Id of the component in which the task belong.
	 */
	public static void typeReviewer( long typeId, long componentId, long assigneeId )
	{
		TaskType type = TaskType.findById( typeId );
		Component component = Component.findById( componentId );
		List<Reviewer> reviewers = new ArrayList();
		if( typeId != 0 )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", type.project, true, type ).fetch();
		List<User.Object> users = new ArrayList<User.Object>();
		for( Reviewer rev : reviewers )
		{
			if( component != null && component.number != 0 )
			{
				if( rev.user.components.contains( component ) && rev.user.id != assigneeId )
					users.add( new User.Object( rev.user ) );
			}
			else
			{
				if( rev.user.id != assigneeId )
					users.add( new User.Object( rev.user ) );
			}
		}
		renderJSON( users );
	}

	public static void deleteSubTasks( long id )
	{
		Task task = Task.findById( id );
		for( Task sub : task.subTasks )
		{
			sub.DeleteTask();
			CollaborateUpdate.update( sub.project, "reload('tasks-" + sub.project.id + "', 'task-" + sub.id + "')" );
		}
		renderText( "Task deleted successfully." );
	}

}