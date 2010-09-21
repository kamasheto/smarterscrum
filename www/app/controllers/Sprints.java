package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import models.Component;
import models.Log;
import models.Meeting;
import models.Project;
import models.Sprint;
import models.Task;
import models.CollaborateUpdate;
import models.User;
import notifiers.Notifications;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.Router;
import play.mvc.With;

/**
 * Represents the Sprint Entity in the Database and it's relations with other
 * entities.
 * 
 * @author minazai
 */
@With( Secure.class )
public class Sprints extends SmartCRUD
{
	/**
	 * Renders a list of the sprints on the project and a the running sprint of
	 * a specific project for the board view.
	 * 
	 * @param projectId
	 *            The project id.
	 * @return void
	 */
	public static void BoardSprints( long projectId )
	{
		Project project = (Project) (Project.findById( projectId ));
		List<Sprint> sprints = project.sprints;
		long runningSprint = project.runningSprint();
		render( sprints, project, runningSprint );
	}

	/**
	 * Renders a list of the sprints on the project and a the running sprint of
	 * a specific project for the chart view.
	 * 
	 * @param projectId
	 *            The project id.
	 * @return void
	 */
	public static void ChartSprints( long projectId )
	{
		Project project = (Project) (Project.findById( projectId ));
		List<Sprint> sprints = project.sprints;
		long runningSprint = project.runningSprint();
		render( sprints, project, runningSprint );
	}

	/**
	 * Renders a list of the sprints on the project and a the running sprint of
	 * a specific project for the backlog view.
	 * 
	 * @param projectId
	 *            The project id.
	 * @return void
	 */
	public static void BacklogSprints( long projectId )
	{
		Project project = (Project) (Project.findById( projectId ));
		List<Sprint> sprints = project.sprints;
		long runningSprint = project.runningSprint();
		render( sprints, project, runningSprint );
	}

	/**
	 * Renders the shows prints page with the sprints and a the running sprint
	 * of a specific project and the project it self.
	 * 
	 * @author minazaki
	 * @param projectId
	 *            The project id.
	 * @return void
	 */
	public static void showsprints( long projectId )
	{
		Project project = (Project) (Project.findById( projectId ));
		List<Sprint> sprints = project.sprints;
		long runningSprint = project.runningSprint();
		render( sprints, project, runningSprint );
	}

	/**
	 * Renders sprint info to the html page.
	 * 
	 * @author minazaki
	 * @param id
	 *            The sprint id.
	 * @return void
	 */
	public static void showsprint( long id )
	{
		Sprint sprint = Sprint.findById( id );
		// Project proj = (Project) (Project.findById(projectId));
		Project proj = sprint.project;
		long runningSprint = proj.runningSprint();
		boolean running = runningSprint == id ? true : false;
		boolean ended = false;
		Date now = Calendar.getInstance().getTime();
		if( sprint.endDate.before( now ) )
		{
			ended = true;
		}
		render( sprint, proj, running, ended );
	}

	/**
	 * Overrides the CRUD blank method that renders the create form, in order to
	 * take the project id to create a sprint into it.
	 * 
	 * @author minazaki
	 * @param projectId
	 *            The project id.
	 * @return void
	 */
	public static void projectblank( long projectId )
	{
		Security.check( Security.getConnected().in( (Project) Project.findById( projectId ) ).can( "addSprint" ) );
		// if( Security.getConnected().in( (Project) Project.findById( projectId
		// ) ).can( "addSprint" ) )
		// {
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		try
		{
			render( type, projectId );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/blank.html", type );
		}
		// }
		// else
		// {
		// forbidden();
		// }
	}

	/**
	 * Renders the sprint to the html backlogs page.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            The sprint id
	 * @return void
	 */
	public static void backlogs( long id )
	{
		Sprint sprint = Sprint.findById( id );
		render( sprint );
	}

	/**
	 * Renders the sprint to the html charts page.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            The sprint id.
	 * @return void
	 */
	public static void charts( long id )
	{
		Sprint sprint = Sprint.findById( id );
		render( sprint );
	}

	/**
	 * Overrides the CRUD create method that is invoked to submit the creation
	 * of a sprint on the database.
	 * 
	 * @author minazki
	 * @param projectId
	 *            The project id.
	 * @throws Exception
	 * @return void
	 */

	public static void projectcreate( long projectId ) throws Exception
	{
		Security.check( Security.getConnected().in( (Project) Project.findById( projectId ) ).can( "addSprint" ) );
		// if( Security.getConnected().in( (Project) Project.findById( projectId
		// ) ).can( "addSprint" ) )
		// {
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		Sprint object = (Sprint) type.entityClass.newInstance();
		Project proj = Project.findById( projectId );
		Date startDate = null;
		Date endDate = null;
		validation.valid( object.edit( "object", params ) );
		if( validation.hasErrors() )
		{

			renderArgs.put( "error", "Please Correct Date Format Error" );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/projectblank.html", type, projectId );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else
		{
			String[] startdate = params.get( "object.startDate" ).split( "-" );
			int startyear = Integer.parseInt( startdate[0] );
			int startmonth = Integer.parseInt( startdate[1] );
			int startday = Integer.parseInt( startdate[2] );
			startDate = new GregorianCalendar( startyear, startmonth - 1, startday ).getTime();
			String today = new Date().toString();
			if( object.endDate==null)
			{
				int defaultDays = proj.sprintDuration;
				System.out.println( defaultDays );
				endDate = new GregorianCalendar().getTime();
				endDate.setTime( startDate.getTime() + (86400000 * defaultDays) );
			}
			else
			{
				String end = params.get( "object.endDate" );
				String[] enddate = end.split( "-" );
				int endyear = Integer.parseInt( enddate[0] );
				int endmonth = Integer.parseInt( enddate[1] );
				int endday = Integer.parseInt( enddate[2] );
				endDate = new GregorianCalendar( endyear, endmonth - 1, endday ).getTime();
			}
			if( object.startDate == null )
			{
				renderArgs.put( "error", "Please Enter Missing Dates" );
				render( request.controller.replace( ".", "/" ) + "/projectblank.html", type, projectId );
			}
			else if( startDate.after( endDate ) )
			{
				renderArgs.put( "error", "Sprint Start Date is after Sprint End Date" );
				render( request.controller.replace( ".", "/" ) + "/projectblank.html", type, projectId );
			}
			else if( today.contains( object.startDate.toString() ) || today.contains( endDate.toString() ) )
			{
				renderArgs.put( "error", "Cant Create Sprint with Past Date" );
				render( request.controller.replace( ".", "/" ) + "/projectblank.html", type, projectId );
			}
			else if( proj.inSprint( startDate, endDate ) )
			{
				renderArgs.put( "error", "Sprint Start Date and End Date are overlapping with other Sprint" );
				render( request.controller.replace( ".", "/" ) + "/projectblank.html", type, projectId );
			}
			else
			{
				// why create it again? o.O
				// commented by sakr
				// now i know why .. because this constructor assigns the sprint
				// a static number based on the project current sprint count
				object = new Sprint( startDate, endDate, proj );
				object.save();
			}
		}
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			// Logs.addLog( Security.getConnected(), "Create", "Sprint",
			// object.id, proj, Calendar.getInstance().getTime() );
			// redirect( "/show/project?id=" + projectId );
			Application.overlayKiller( "reload('sprints')", "window.parent.$.bar({message:'Sprint created successfully'})" );
		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			// Logs.addLog( Security.getConnected(), "Create", "Sprint",
			// object.id, proj, Calendar.getInstance().getTime() );
			redirect( "/sprints/projectblank?projectId=" + projectId );
		}

		Log.addUserLog( "Created sprint", object, proj );
		String resourceURL = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + proj.id + "&isOverlay=false&url=/sprints/showsprint?id=" + object.id;
		Notifications.notifyProjectUsers( proj, "setSprint", resourceURL, "Sprint", object.sprintNumber, (byte) 0 );
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/**
	 * Overrides the CRUD show method that renders the edit form, in order to
	 * take the project id.
	 * 
	 * @param id
	 *            The sprint id.
	 * @return void
	 */
	public static void projectshow( long id )
	{
		Sprint sprint = Sprint.findById( id );
		long projId = sprint.project.id;
		Security.check( Security.getConnected().in( sprint.project ).can( "editSprint" ) );
		// if( Security.getConnected().in( (Project) Project.findById( projId )
		// ).can( "editSprint" ) )
		// {
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Project p = Project.findById( projId );
		List<Meeting> meetings = p.meetingsAssoccToEndOfSprint( (Sprint) object );
		try
		{
			render( type, object, projId, meetings );
		}
		catch( TemplateNotFoundException e )
		{
			render( "Sprints/show.html", type, object );
		}
		// }
		// else
		// {
		// forbidden();
		// }
	}

	/**
	 * Overrides the CRUD save method that is invoked to submit the edit, in
	 * order to check if the edits are acceptable.
	 * 
	 * @param id
	 *            The sprint id.
	 * @throws Exception
	 * @return void
	 */
	public static void projectsave( long id ) throws Exception
	{
		Sprint sprint = Sprint.findById( id );
		long projId = sprint.project.id;
		Security.check( Security.getConnected().in( sprint.project ).can( "editSprint" ) );
		if( sprint.deleted )
			notFound();
		String today = new Date().toString();
		Project proj = Project.findById( projId );
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		Sprint object = (Sprint) type.findById( id );
		validation.valid( object.edit( "object", params ) );
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", "Correct Date Format Errors" );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object );
			}
		}
		if( object.id == object.project.runningSprint() )
		{
			object.startDate = sprint.startDate;
			if( object.ended )
			{
				object.ended = false;
				object.endDate = new Date();

				object.Last = object.startDate;
			}
			else
			{
				if( object.endDate == null )
				{
					object.endDate = sprint.endDate;
				}
				if( object.startDate.after( object.endDate ) )
				{
					renderArgs.put( "error", "Sprint Start Date is after Sprint End Date" );

					render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );

				}
				if( today.contains( object.startDate.toString() ) )
				{
					renderArgs.put( "error", "Cant Create Sprint with Past Date" );

					render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
				}
			}
		}
		else
		{
			if( object.ended )
			{
				object.ended = false;
				object.endDate = new Date();
				object.Last = object.startDate;
			}
			else
			{
				if( object.endDate == null )
				{
					object.endDate = sprint.endDate;
				}
				if( object.startDate == null )
				{
					object.startDate = sprint.endDate;
				}
				if( object.startDate.after( object.endDate ) )
				{
					renderArgs.put( "error", "Sprint Start Date is after Sprint End Date" );

					render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );

				}
				if( today.contains( object.startDate.toString() ) || today.contains( object.endDate.toString() ) )

				{
					renderArgs.put( "error", "Cant Create Sprint with Past Date" );

					render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
				}
				if( (proj.inSprint( object.startDate, object.endDate )) )
				{
					renderArgs.put( "error", "Sprint is Overlapping with other Sprint time" );

					render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
				}
			}
		}

		object.save();

		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{

			// Logs.addLog( Security.getConnected(), "Edit", "Sprint",
			// object.id, proj, Calendar.getInstance().getTime() );
			Log.addUserLog( "Edited sprint", object, proj );
			// redirect( "/show/project?id=" + projId );
			Application.overlayKiller( "reload('sprints', 'sprint-" + object.id + "')", "" );
		}
		redirect( request.controller + ".show", object.getEntityId() );
		// }
		// else
		// {
		// forbidden();
		// }
	}

	/**
	 * Overrides the CRUD list method, in order to list the sprints of the
	 * project of a given component.
	 * 
	 * @param componentId
	 *            The component id.
	 * @param type
	 *            The type.
	 * @return void
	 */
	public static void listSprintsInComponent( long componentId, int type )
	{
		Component component = ((Component) Component.findById( componentId ));
		List<Sprint> sprints = component.project.sprints;
		render( sprints, component, type );
	}

	/**
	 * Overrides the CRUD show method, in order to make the crud view not
	 * accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void show()
	{
		forbidden();
	}

	/**
	 * Overrides the CRUD delete method, in order to make the crud delete action
	 * not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void delete()
	{
		forbidden();
	}

	/**
	 * Overrides the CRUD blank method, in order to make the crud view not
	 * accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void blank()
	{
		forbidden();
	}

	/**
	 * Overrides the CRUD create method, in order to make the crud create action
	 * not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void create()
	{
		forbidden();
	}

	/**
	 * Overrides the CRUD save method, in order to make the crud edit action not
	 * accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void save()
	{
		forbidden();
	}

	/**
	 * Overrides the CRUD list method, in order to make the crud view not
	 * accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void list()
	{
		forbidden();
	}

	/**
	 * Adds a task to a given sprint.
	 * 
	 * @author Hadeer Younis
	 * @param taskId
	 *            The id of the task to be added.
	 * @param sprintId
	 *            The id of the sprint which will accept the task.
	 * @return void
	 */
	public static void addTask( long taskId, long sprintId )
	{
		Task task = Task.findById( taskId );
		Sprint sprint = Sprint.findById( sprintId );
		User connected = Security.getConnected();
		Security.check( connected.in( task.project ).can( "modifyTask" ) && connected.projects.contains( task.project ) && sprint.project == task.project );
		if( sprint.deleted || task.deleted )
			notFound();
		else if( sprint.ended )
			renderText( "Sorry the requested sprint has ended." );
		else if( sprint.tasks.contains( task ) )
			renderText( "Sorry the requested task already belongs to that sprint." );
		else if( sprint.project.isScrum && sprint.startDate.getTime() <= new Date().getTime() )
			renderText( "Sorry you can't add a task to a running sprint" );
		sprint.tasks.add( task );
		task.taskSprint = sprint;
		task.save();
		sprint.save();
		renderText( "The task was assigned to the requested sprint|reload('task-" + taskId + "','sprint-" + sprintId + ")" );
	}

	/**
	 * reload the sprint when the sprint start date come
	 * 
	 * @param id
	 */
	public static void reloadSprint( long id )
	{
		Sprint sprint = Sprint.findById( id );
		while( sprint.startDate.getTime() > new Date().getTime() )
		{

		}
		CollaborateUpdate.update( sprint.project, "reload('sprint-" + sprint.id + "','sprints-" + sprint.project.id + "')" );
	}
}
