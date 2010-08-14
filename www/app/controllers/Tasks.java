package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import models.Board;
import models.Column;
import models.Comment;
import models.Component;
import models.Log;
import models.Meeting;
import models.ProductRole;
import models.Project;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.Update;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
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
					if( task.deleted )
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
			java.util.Date End = sprint.endDate;
			Calendar cal = new GregorianCalendar();
			if( End != null && End.after( cal.getTime() ) )
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
		System.out.println( tmp.parent );
		if( tmp.project != null && tmp.parent == null )
		{
			p = tmp.project;
			project = tmp.project;
			System.out.println( "here" );
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
			java.util.Date End = sprint.endDate;
			Calendar cal = new GregorianCalendar();
			if( End != null && End.after( cal.getTime() ) )
			{
				sprints.add( sprint );
			}
		}
		String newdesc = tmp.description;
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
		// System.out.println(tmp.parent.id + "toffa7");

		String[] desc = newdesc.split( "," );
		if( desc.length == 1 )
		{
			tmp.description = desc[0];
		}
		else
		{
			String[] desc2 = desc[0].split( " " );
			if( desc2.length >= 3 )
			{
				if( desc2[0].equalsIgnoreCase( "as" ) && (desc2[1].equalsIgnoreCase( "a" ) || desc2[1].equalsIgnoreCase( "an" )) )
				{
					boolean flag = false;
					String productrole = "";
					for( int k = 2; k < desc2.length; k++ )
					{
						if( k == desc2.length - 1 )
							productrole = productrole + desc2[k];
						else
							productrole = productrole + desc2[k] + " ";

					}
					for( int j = 0; j < tmp.project.productRoles.size(); j++ )
					{
						if( tmp.project.productRoles.get( j ).name.equalsIgnoreCase( productrole ) )
							flag = true;
					}
					if( !flag )
					{
						ProductRole pr = new ProductRole( tmp.project.id, productrole, "" );
						pr.save();
						tmp.productRole = pr;
					}
					else
					{
						for( int j = 0; j < tmp.project.productRoles.size(); j++ )
						{
							if( tmp.project.productRoles.get( j ).name.equalsIgnoreCase( productrole ) )
							{
								tmp.productRole = tmp.project.productRoles.get( j );
							}
						}
					}
					for( int i = 1; i < desc.length; i++ )
					{
						tmp.description = desc[i] + " ";
					}
				}
			}
			else
			{
				tmp.description = tmp.description;
				tmp.productRole = null;
			}
		}
		tmp.reporter = Security.getConnected();
		object.save();
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{

			if( tmp.parent != null )
			{
				Update.update( tmp.project, "reload('tasks','task-" + tmp.parent.id + "')" );
				Application.overlayKiller( "", "" );
			}
			else
			{
				Update.update( tmp.project, "reload('tasks','task-" + "')" );
				Application.overlayKiller( "", "" );
			}

		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			redirect( request.controller + ".blank" );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/*
	 * Overrides the CRUD show method that renders the edit form.
	 * @author Monayri
	 * @param id the task been edited id.
	 * @return void
	 */
	public static void show( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		List<User> users = null;
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Task> dependencies = new ArrayList<Task>();
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		if( comments == null )
			comments = new ArrayList<Comment>();
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		dependencies = Task.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		String productRoles = "";
		for( int i = 0; i < tmp.project.productRoles.size(); i++ )
		{
			if( tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'a' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'e' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'i' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'o' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'u' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'A' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'E' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'I' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'O' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + tmp.project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + tmp.project.productRoles.get( i ).name + ",-";
		}
		if(tmp.component!=null)
		{
			if(tmp.component.number==0)
			{
				users = tmp.project.users;
			}
			else
			{
				users = tmp.component.componentUsers;
			}
		}
		else
			users = tmp.project.users;
		
			
		try
		{
			render( type, object, users, statuses, types, dependencies, message2, deletable, comments, productRoles );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/show.html", type, object );
		}
	}

	/*
	 * Overrides the CRUD save method that is invoked to submit the edit, in
	 * order to check if the edits are acceptable.
	 * @author Monayri
	 * @param id the id of the task being edited.
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
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		List<User> users = tmp.component.componentUsers;
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Task> dependencies = Task.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		String oldDescription = tmp.description;// done
		long oldTaskType;
		if( tmp.taskType != null )
			oldTaskType = tmp.taskType.id;// done
		else
			oldTaskType = 0;
		long oldTaskStatus;
		if( tmp.taskStatus != null )
			oldTaskStatus = tmp.taskStatus.id;// done
		else
			oldTaskStatus = 0;
		double oldEstPoints = tmp.estimationPoints;// done
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
		// Look if we need to deserialize
		// for( ObjectType.ObjectField field : type.getFields() )
		// {
		// if( field.type.equals( "serializedText" ) && params.get( "object." +
		// field.name ) != null )
		// {
		// Field f = object.getClass().getDeclaredField( field.name );
		// f.set( object, CRUD.collectionDeserializer( params.get( "object." +
		// field.name ), (Class) ((ParameterizedType)
		// f.getGenericType()).getActualTypeArguments()[0] ) );
		// }
		// }

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
		String newdesc = tmp.description;
		String[] desc = newdesc.split( "," );
		if( desc.length == 1 )
		{
			tmp.description = desc[0];
		}
		else
		{
			String[] desc2 = desc[0].split( " " );
			if( desc2.length >= 3 )
			{
				if( desc2[0].equalsIgnoreCase( "as" ) && (desc2[1].equalsIgnoreCase( "a" ) || desc2[1].equalsIgnoreCase( "an" )) )
				{
					boolean flag = false;
					String productrole = "";
					for( int k = 2; k < desc2.length; k++ )
					{
						if( k == desc2.length - 1 )
							productrole = productrole + desc2[k];
						else
							productrole = productrole + desc2[k] + " ";

					}
					for( int j = 0; j < tmp.project.productRoles.size(); j++ )
					{
						if( tmp.project.productRoles.get( j ).name.equalsIgnoreCase( productrole ) )
							flag = true;
					}
					if( !flag )
					{
						ProductRole pr = new ProductRole( tmp.project.id, productrole, "" );
						pr.save();
						tmp.productRole = pr;
					}
					else
					{
						for( int j = 0; j < tmp.project.productRoles.size(); j++ )
						{
							if( tmp.project.productRoles.get( j ).name.equalsIgnoreCase( productrole ) )
							{
								tmp.productRole = tmp.project.productRoles.get( j );
							}
						}
					}
					for( int i = 1; i < desc.length; i++ )
					{
						if( i == desc.length - 1 )
							tmp.description = desc[i];
						else
							tmp.description = desc[i] + " ";
					}
				}
			}
			else
			{
				tmp.description = tmp.description;

			}
		}

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

		// Now finally save the comment
		if( !changes.equals( "" ) )
		{
			Comment changesComment = new Comment( Security.getConnected(), tmp.id, changes );
			changesComment.save();
		}
		// /********** End of Changes as Comment ********/
		if( tmp.comment.trim().length() != 0 )
		{
			Comment comment = new Comment( Security.getConnected(), tmp.id, tmp.comment );
			comment.save();
		}
		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			Update.update( tmp.project, "reload('tasks','task-" + tmp.id + "')" );
			Application.overlayKiller( "", "" );
			// Logs.addLog( tmp.project, "edit", "Task", tmp.id );
			Log.addUserLog("Edit task", tmp, tmp.project);
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
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		try
		{
			tmp.deleted = true;
			String header = "Task: 'T" + tmp.id + "\'" + " has been deleted.";
			String body = "In Project: " + "\'" + tmp.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + tmp.component.name + "\'" + "." + '\n' + "." + '\n' + " Deleted by: " + "\'" + Security.getConnected().name + "\'" + ".";
			// Logs.addLog( Security.getConnected(), "delete", "Task", tmp.id, tmp.project, new Date( System.currentTimeMillis() ) );
			Log.addUserLog("Deleted task", tmp, tmp.project);
			// Notifications.notifyUsers( tmp.component.componentUsers, header,
			// body, (byte) -1 );
			object.save();
			String text = "The Task was deleted successfully";
			System.out.println( "here" );
			renderText( text );
		}
		catch( Exception e )
		{
			flash.error( Messages.get( "crud.delete.error", type.modelName, object.getEntityId() ) );
			renderText( "Task can't be deleted" );
		}
		flash.success( Messages.get( "crud.deleted", type.modelName, object.getEntityId() ) );
		renderText( "Task deleted successfully." );
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
	public static void reviewers( long id, long id2, long compId, long taskId )
	{
		List<User> users = null;
		Component component = Component.findById( compId );
		Task task = Task.findById(taskId);
		User Assignee = User.findById( id2 );
		
		if(component!=null)
		{
		if( component.number == 0 )
			users = component.project.users;
		else
			users = component.componentUsers;
		}
		else
		{
			users = task.project.users;
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

		if( temp.estimationPoints < effort )
		{
			renderText( "The entered effort cannot be more than the estimated effort" );
			return;
		}
		else if( effort < 0 )
		{
			renderText( "The entered effort cannot be less than 0" );
			return;
		}
		temp.setEffortOfDay( effort, day );
		User userWhoChanged = Security.getConnected();
		Date timeChanged = new Date();
		Update.update( temp.project, ";sprintLoad('" + id + "');" );
		temp.save();
		// Logs.addLog( userWhoChanged, "Effort entered", "Task", id, temp.taskSprint.project, timeChanged );
		Log.addLog("Effort entered for task", userWhoChanged, temp, temp.project);

	}

	/**
	 * Fetches all the data needed to generate a report on a given task.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            The id of the task whose report will be generated.
	 * @return void
	 */
	public static void getReport( long id )
	{
		Task theTask = Task.findById( id );
		List<Log> temp = theTask.logs;
		Security.check( theTask.project.users.contains( Security.getConnected() ) );
		if( theTask.deleted )
			notFound();
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
		// for( int i = 0; i < temp.size(); i++ )
		// 		{
		// 			if( temp.get( i ).resource_id != id )
		// 				temp.remove( i );
		// 		}
		for( int i = 0; i < temp.size(); i++ )
		{
			int k = 1;
			if( i < temp.size() - 2 )
			{
				keepLoop : while( new Date(temp.get( i ).timestamp).toString().substring( 0, 10 ).equals( new Date(temp.get( i + 1 ).timestamp).toString().substring( 0, 10 ) ) )
				{
					i++;
					if( i == temp.size() - 1 )
						break keepLoop;
					k++;
				}
			}
			if( i == temp.size() - 1 )
				changes = changes + "['" + new Date(temp.get( i ).timestamp).toString().substring( 0, 10 ) + "'," + k + "]]";
			else
				changes = changes + "['" + new Date(temp.get( i ).timestamp).toString().substring( 0, 10 ) + "'," + k + "],";

		}

		if( !empty )
		{
			lastModified = new Date(temp.get( temp.size() - 1 ).timestamp).toString().substring( 0, 10 ) + " @ " + new Date(temp.get( temp.size() - 1 ).timestamp).toString().substring( 11 );
			numberOfModifications = temp.size();
		}
		Date maxdate = new Date();
		if (!temp.isEmpty()) maxdate.setTime( temp.get( temp.size() - 1 ).timestamp + (3 * 86400000) );
		String maxDate = maxdate.toString().substring( 0, 10 );

		Date mindate = new Date();
		if (!temp.isEmpty()) mindate.setTime( temp.get( 0 ).timestamp - (3 * 86400000) );
		String minDate = mindate.toString().substring( 0, 10 );

		Project myProject = theTask.project;
		render( myProject, minDate, temp, lastModified, empty, efforts, changes, numberOfModifications, theTask, maxDate );
	}
	
	/**
	 * This method changes the given task description.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param userId
	 *            The id of the user who will do the change in description.
	 * @param desc
	 *            The new description.
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
		Update.update( Security.getConnected(), "reload_note_open(" + task1.taskSprint.id + "," + task1.id + ")" );
		Update.update(task1.project.users,Security.getConnected(), "reload_note(" + task1.taskSprint.id + "," + task1.id + ")");		
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		String body = "";
		String header = "Task: 'T" + task1.id + "\'" + " Task Type has been edited.";
		if( userId == Security.getConnected().id )
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";

		}
		else
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		if( userId == Security.getConnected().id )
		{
			// Logs.addLog( user1, "Edit", "Task Description", id, task1.project, new Date( System.currentTimeMillis() ) );
			Log.addUserLog("Edited task description", task1, task1.project);
		}
		else
		{
			Log.addUserLog(user1.name +" has edited task description", task1, user1, task1.project);
			// Logs.addLog( user1 + " has performed action (Edit) using resource (Task Description) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
		}
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
	public static void changeTaskStatusHelper( long id, int columnSequence, String taskString, long user_id, long row, long oldcol )
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
		Column col;
		col = Column.find( "bySequenceAndBoard", columnSequence, b ).first();
		status = col.taskStatus;

		// get the actual task_id in an int
		task_id_helper = taskString.split( "_" );
		task_id_helper2 = task_id_helper[0].split( "-" );
		task_id = Integer.parseInt( task_id_helper2[1] );

		editTaskStatus( task_id, user_id, status, columnSequence, row, oldcol );
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
	public static boolean editTaskStatus( long id, long userId, TaskStatus newStatus, long newcol, long row, long oldcol )
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
		if( user1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskStatus" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}

		task1.taskStatus = newStatus;
		task1.save();
		Update.update( task1.project, "click_note(" + row + "," + oldcol + "," + newcol + "," + task1.taskSprint.id + "," + task1.id + ")" );
		String body = "";
		String header = "Task: 'T" + task1.id + "\'" + " Task Status has been edited.";
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog("Edited task status", task1, task1.project);
			// Logs.addLog( user1, "Edit", "Task Status", id, task1.project, new Date( System.currentTimeMillis() ) );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";
		}
		else
		{
			Log.addUserLog(user1.name + " edited task status", user1, task1, task1.project);
			// Logs.addLog( user1 + " has performed action (Edit) using resource (Task Status) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		return true;

	}

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
		stat.Tasks.add( task );
		stat.save();
		Update.update( task.project, "reload('reload-task-'" + id + ");sprintLoad(" + id + ")" );
		task.save();
		Log.addUserLog("Edited task estimation", task, task.project);
		// Logs.addLog( Security.getConnected(), "Edit", "Task estimation", id, task.project, new Date( System.currentTimeMillis() ) );

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
			return false;
		if( estimation < 0 )
			return false;
		task.estimationPoints = estimation;
		task.save();

		Update.update( task.project, "reload('reload-task-'" + id + ");sprintLoad(" + id + ")" );
		// Logs.addLog( Security.getConnected(), "Edit", "Task estimation", id, task.project, new Date( System.currentTimeMillis() ) );
		Log.addUserLog("Edit task estimation", task, task.project);
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
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		User assignee = User.findById( assigneeId );
		if( assignee == null )
			return false;
		if( task1.reviewer.getId() == assigneeId )
			return false;

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeAssignee" );

		if( !permession )
			return false;
		// String oldAssignee = task1.assignee.name;
		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add( task1 );
		assignee.save();
		Update.update( Security.getConnected(), "reload_sticky_note(" + task1.taskSprint.id + "," + task1.id + ")" );
		Update.update(task1.project.users,Security.getConnected(), "reload_note(" + task1.taskSprint.id + "," + task1.id + ")");		
		String header = "Task: 'T" + task1.id + "\'" + " Assignee has been edited.";
		/*
		 * ////Long Informative Notification message. Not suitable for online
		 * notification. String header =
		 * "A Task Assignee has been changed in Component: " + "\'" +
		 * task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" +
		 * task1.taskStory.componentID.project.name + "\'" + ".";
		 */
		String body = "";
		if( userId == Security.getConnected().id )
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";
		}
		else
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}

		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog("Edited task assignee", task1, task1.project);
			// Logs.addLog( user1, "Edit", "Task Assignee", id, task1.project, new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Log.addUserLog(user1.name + " has edited task assignee", user1, task1, task1.project);
			// Logs.addLog( user1 + " has performed action (Edit) using resource (Task Assignee) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
		}
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
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		User reviewer = User.findById( reviewerId );
		if( reviewer == null )
			return false;
		if( task1.assignee.getId() == reviewerId )
			return false;

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeReviewer" );

		if( !permession )
			return false;
		// String oldReviewer = task1.reviewer.name;
		task1.reviewer = reviewer;
		task1.save();
		Update.update( Security.getConnected(), "reload_sticky_note(" + task1.taskSprint.id + "," + task1.id + ")" );
		Update.update(task1.project.users,Security.getConnected(), "reload_note(" + task1.taskSprint.id + "," + task1.id + ")");		
		reviewer.tasks.add( task1 );
		reviewer.save();
		String body = "";
		// String header = "A Task Reviewer has been changed in Component: " +
		// "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " +
		// "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		// String header = "Task: 'T" + task1.id + "\'" +
		// " Reviewer has been edited.";
		if( userId == Security.getConnected().id )
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";

		}
		else
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";

		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog("Edit task reviewer", task1, task1.project);
			// Logs.addLog( user1, "Edit", "Task Reviewer", id, task1.project, new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Log.addUserLog(user1.name + " has edited task reviewer", user1, task1, task1.project);
			// Logs.addLog( user1 + " has performed action (Edit) using resource (Task Reviewer) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
		}
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
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskType" );
		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}
		TaskType type = TaskType.findById( typeId );
		task1.taskType = type;
		task1.save();
		Update.update( Security.getConnected(), "reload_sticky_note(" + task1.taskSprint.id + "," + task1.id + ")" );
		Update.update(task1.project.users,Security.getConnected(), "reload_note(" + task1.taskSprint.id + "," + task1.id + ")");		
		Update.update(task1.project, "sprintLoad(" + task1.id + ")");
		String body = "";
		String header = "Task: 'T" + task1.id + "\'" + " Task Type has been edited.";
		// String header = "A Task Type has been edited in Component: " + "\'" +
		// task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" +
		// task1.taskStory.componentID.project.name + "\'" + ".";
		if( userId == Security.getConnected().id )
		{
	//		Logs.addLog( user1, "Edit", "Task Type", id, task1.project, new Date( System.currentTimeMillis() ) );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";

		}
		else
		{
	//		Logs.addLog( user1 + " has performed action (Edit) using resource (Task Type) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
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
		User user1 = User.findById( userId );
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( taskId );
		Component c = Component.findById( compId );
		users = c.componentUsers;
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
		List<TaskType> types = task.taskSprint.project.taskTypes;
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
			System.out.println( tasks );
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
				List<Task> tasks = task.subTasks;
				int counter = tasks.size();
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
						if( task1.assignee != null && task1.assignee.equals( user ) && task1.checkUnderImpl() && task1.taskStatus != null && !task1.taskStatus.closed )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( task1.reviewer != null && task1.reviewer.equals( user ) && task1.checkUnderImpl() && task1.taskStatus != null && task1.taskStatus.pending )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( task1.assignee != null && task1.assignee.equals( user ) && task1.taskStatus != null && !task1.taskStatus.closed && !tasks.contains( task1 ) )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( task1.reviewer != null && task1.reviewer.equals( user ) && task1.taskStatus != null && task1.taskStatus.pending && !tasks.contains( task1 ) )
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
		Security.check( connected.in( task.project ).can( "modifyTask" ) && task.project == component.project && task.component.project == component.project && task.parent == null );

		// first remove task from the component
		task.component.componentTasks.remove( task );
		task.component.save();

		task.component = component;
		task.save();
		Log.addUserLog("Assigned task to component", task, component, component.project);
		Update.update( task.project, "reload('component-" + componentId + "', 'task-" + taskId + "')" );
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
		if( !user.components.contains( task.component ) )
			renderText( "The task & the assignee can't be in different components" );
		Security.check( connected.in( task.project ).can( "modifyTask" ) && user.projects.contains( task.project ) && task.reviewer != user && (task.component == null || user.components.contains( task.component )) );
		task.assignee = user;
		task.save();
		Log.addUserLog("Assigned task assignee", task, user, task.project);
		Update.update( task.project, "reload('task-" + taskId + "');sprintLoad(" + taskId + ");" );
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
	public static void assignTaskReviewer( long taskId, long reviewerId )
	{
		Task task = Task.findById( taskId );
		User user = User.findById( reviewerId );
		User connected = Security.getConnected();
		if( task.assignee == user )
			renderText( "The assignee can't be the reviewer" );
		if( !user.components.contains( task.component ) )
			renderText( "The task & the reviewer can't be in different components" );
		Security.check( connected.in( task.project ).can( "modifyTask" ) && user.projects.contains( task.project ) && task.assignee != user && (task.component == null || user.components.contains( task.component )) );
		task.reviewer = user;
		task.save();
		Log.addUserLog("Assigned task reviewer", task, user, task.project);
		Update.update( task.project, "reload('task-" + taskId + "');sprintLoad(" + taskId + ");" );
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
		List<User> users = null;
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
		Update.update( Security.getConnected(), "reload('task-" + task.id + "','tasks" + task.project.id + "')" );
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
		Update.update( Security.getConnected(), "reload('task-" + task.project.id + "')" );
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
		Update.update( Security.getConnected(), "reload('task-" + task.id + "','tasks-" + task.project.id + "')" );
	}
}