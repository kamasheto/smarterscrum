package controllers;

/*
 * @author minazai
 */
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import models.Meeting;
import models.Project;
import models.Sprint;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With( Secure.class )
public class Sprints extends SmartCRUD
{
	/**
	 * to render the showsprints page with the sprints and the projectId
	 * 
	 * @author minazaki
	 * @param projectId
	 */
	public static void showsprints( long projectId )
	{
		Project project = (Project) (Project.findById( projectId ));
		List<Sprint> sprints = project.sprints;
		long runningSprint = project.runningSprint();
		render( sprints, project, runningSprint );
	}

	/**
	 * to render sprint info page
	 * 
	 * @author minazaki
	 * @param id
	 * @param projectId
	 */
	public static void showsprint( long id, long projectId )
	{
		Project proj = (Project) (Project.findById( projectId ));
		Sprint sprint = Sprint.findById( id );
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
	 * is to have the blank page of crud to create inside the project so there
	 * is no need to choose the project just the projectId is sent with the page
	 * 
	 * @author minazaki
	 * @param projectId
	 */
	@Check( "canAddSprint" )
	public static void projectblank( long projectId )
	{
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
	}

	/**
	 * to be called from the sprint creation page inside a certain project thats
	 * not to let the user choose the project
	 * 
	 * @author minazki
	 * @param projectId
	 * @throws Exception
	 */
	@Check( "canAddSprint" )
	public static void projectcreate( long projectId ) throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		Sprint object = (Sprint) type.entityClass.newInstance();
		Project proj = Project.findById( projectId );
		Date startDate=null;
		Date endDate=null;
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
			startDate=new GregorianCalendar( startyear, startmonth-1	, startday ).getTime();
		
			if( params.get( "object.endDate" ).length() < 2 )
			{
				int defaultDays = proj.sprintDuration;
				System.out.println(defaultDays);
				endDate=new GregorianCalendar().getTime();
				endDate.setTime( startDate.getTime() + (86400000 * defaultDays) );
			}
			else
			{
				String end = params.get( "object.endDate" );
				String[] enddate = end.split( "-" );
				int endyear = Integer.parseInt( enddate[0] );
				int endmonth = Integer.parseInt( enddate[1] );
				int endday = Integer.parseInt( enddate[2] );
				endDate=new GregorianCalendar( endyear, endmonth-1, endday ).getTime();
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
		else if( startDate.before( new Date() ) || endDate.before( new Date() ) )
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
			object = new Sprint( startDate,endDate, proj );
			object.save();
		}
		}
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			Logs.addLog( (User) User.find( "byEmail", Security.connected() ).first(), "Create", "Sprint", object.id, proj, Calendar.getInstance().getTime() );
			redirect( "/show/project?id=" + projectId );
		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			Logs.addLog( (User) User.find( "byEmail", Security.connected() ).first(), "Create", "Sprint", object.id, proj, Calendar.getInstance().getTime() );
			redirect( "/sprints/projectblank?projectId=" + projectId );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	@Check( "canEditSprint" )
	public static void projectshow( long id, long projId )
	{
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
	}

	@Check( "canEditSprint" )
	public static void projectsave( long id, long projId ) throws Exception
	{
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
		if( object.endDate == null || object.startDate == null )
		{
			renderArgs.put( "error", "Please Enter Missing Dates" );

			render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
		}
		else if( object.startDate.after( object.endDate ) )
		{
			renderArgs.put( "error", "Sprint Start Date is after Sprint End Date" );

			render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );

		}
		else if( object.startDate.before( new Date() ) || object.endDate.before( new Date() ) )
		{
			renderArgs.put( "error", "Cant Create Sprint with Past Date" );

			render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
		}
		else if( (proj.inSprint( object.startDate, object.endDate )) )
		{
			renderArgs.put( "error", "Sprint is Overlapping with other Sprint time" );

			render( request.controller.replace( ".", "/" ) + "/projectshow.html", type, object, projId );
		}
		else
		{
			object.save();
		}
		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{

			Logs.addLog( (User) User.find( "byEmail", Security.connected() ).first(), "Edit", "Sprint", object.id, proj, Calendar.getInstance().getTime() );
			redirect( "/show/project?id=" + projId );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

}
