package controllers;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import models.Artifact;
import models.Column;
import models.Component;
import models.Meeting;
import models.MeetingAttendance;
import models.Priority;
import models.ProductRole;
import models.Project;
import models.ProjectNotificationProfile;
import models.Request;
import models.Role;
import models.Snapshot;
import models.Sprint;
import models.TaskStatus;
import models.TaskType;
import models.User;
import models.UserNotificationProfile;
import notifiers.Notifications;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With( Secure.class )
public class Projects extends SmartCRUD
{
	public static void Artifacts( long projectId )
	{
		Project project = (Project) (Project.findById( projectId ));
		List<Sprint> sprints = project.sprints;
		long runningSprint = project.runningSprint();
		render( sprints, project, runningSprint );
	}

	/**
	 * overriden to init roles by default from CRUD
	 * 
	 * @throws Exception
	 */
	public static void create() throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.entityClass.newInstance();
		Project projectObject = (Project) object;
		User user = Security.getConnected();
		validation.valid( object.edit( "object", params ) );
		if( validation.hasErrors() )
		{
			flash.error( Messages.get( "Please Fill in All The Required Fields." ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/blank.html", type );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else if( Project.userRequstedProjectBefore( user.id, java.net.URLEncoder.encode( projectObject.name, "UTF-8" ) ) )
		{
			flash.error( Messages.get( "You Have Already Created a Project with the Same Name :'" + projectObject.name + "'. You Will Be notified Upon Approval." ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/blank.html", type );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else if( Project.isUnique( (java.net.URLEncoder.encode( projectObject.name, "UTF-8" )) ) )
		{
			flash.error( "Project Name is Already Taken." );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/blank.html", type );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else
		{
			if( Security.getConnected().isAdmin )
			{

				projectObject.approvalStatus = true;
			}
			projectObject.user = user;
			if( params.get( "object_isPrivate" ) != null )
			{
				projectObject.isPrivate = true;
			}
			if( params.get( "object_isScrum" ) != null )
			{
				projectObject.isScrum = true;
			}
			object.save();
			Project pro = (Project) object;
			pro.init( projectObject.isScrum );
			Role proAdmin = Role.find( "name= 'Project Creator' and project =" + pro.id ).first();
			user.addRole( proAdmin );

			Logs.addLog( Security.getConnected(), "Create", "Project", projectObject.id, projectObject, new Date( System.currentTimeMillis() ) );
			if( Security.getConnected().isAdmin )
			{

				flash.success( "' " + projectObject.name + " '" + " Project Has Been Successfully Created." );
				Application.overlayKiller( "" );
			}
			else
			{
				flash.success( "Your Project Request Has Been Sent.You Will Be Notified Upon Approval" );
				Application.overlayKiller( "" );
			}
		}
	}

	public static void list( int page, String search, String searchFields, String orderBy, String order )
	{
		Security.check( Security.getConnected().isAdmin );
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		if( page < 1 )
		{
			page = 1;
		}
		// List<JPASupport> objects = type.findPage(page, search, searchFields,
		// orderBy, order, (String) request.args.get("where"));
		List<Project> objects = Project.find( "approvalStatus=true AND deleted=false" ).fetch();
		Long totalCount = (long) objects.size();
		Long count = (long) objects.size();

		try
		{
			render( type, objects, count, totalCount, page, orderBy, order );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/list.html", type, objects, count, totalCount, page, orderBy, order );
		}
	}

	/**
	 * This action takes the name of a project as input and checks whether it
	 * already exists or not.
	 * 
	 * @param name
	 * @author behairy
	 * @throws UnsupportedEncodingException
	 */
	public static void checkAvailability( String name ) throws UnsupportedEncodingException
	{
		boolean flag = !Project.isUnique( java.net.URLEncoder.encode( name, "UTF-8" ) );
		renderJSON( flag );
	}

	/**
	 * This action method add a meeting type to the array list of meeting types
	 * in project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param meetingType
	 *            String
	 * @author Behairy
	 */

	public static void addMeetingType( long id, String meetingType, boolean inSprint )
	{
		Security.check( Security.getConnected().in( (Project) Project.findById( id ) ).can( "editProject" ) );

		Project p = Project.findById( id );
		p.meetingsTypes.add( meetingType );
		p.meetingsTypesInSprint.add( inSprint );
		p.save();

		Logs.addLog( Security.getConnected(), "Add", "Project Defualt Meeting Types", p.id, p, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This action method removes a meeting from the array list of meeting types
	 * in project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param meetingType
	 *            String
	 * @author Behairy
	 */

	public static void removeMeetingType( long id, String meetingType )
	{
		Project p = Project.findById( id );
		// if () {
		Security.check( Security.getConnected().in( p ).can( "editProject" ) );
		int index = p.meetingsTypes.indexOf( meetingType );
		p.meetingsTypes.remove( meetingType );
		p.meetingsTypesInSprint.remove( index );
		p.save();
		Logs.addLog( Security.getConnected(), "Remove", "Project Default Meeting Types ", p.id, p, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This action method checks if the given meeting type is associated to
	 * sprints using the array list meetingTypesInArraylist
	 * 
	 * @param id
	 * @param meetingType
	 * @author Behairy
	 */

	public static void isMeetingTypeAssociatedToSprint( long id, String meetingType )
	{
		Project p = Project.findById( id );
		// if () {
		Security.check( Security.getConnected().in( p ).can( "editProject" ) );
		int index = p.meetingsTypes.indexOf( meetingType );
		boolean inSprint = p.meetingsTypesInSprint.get( index );

		renderJSON( inSprint );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This action method adds a task status to the list of task statuses in
	 * project specified by the parameter id.
	 * 
	 * @author Behairy, Heba Elsherif
	 * @param id
	 *            the id of the project which the Task Status is added to.
	 * @param taskStatus
	 *            the name of the Task Status.
	 * @parm indicator an indicator to indicate weather the Task Status
	 *       indicates pending or closed.
	 * @return void
	 * @issue 219
	 * @sprint 2, 4
	 */
	public static void addTaskStatus( long id, String taskStatus, String indicator )
	{
		Project p = Project.findById( id );
		Security.check( Security.getConnected().in( p ).can( "editProject" ) );
		TaskStatus t = new TaskStatus();
		t.project = p;
		t.name = taskStatus;
		t.pending = false;
		t.closed = false;
		if( indicator.equalsIgnoreCase( "Pending" ) )
			t.pending = true;
		if( indicator.equalsIgnoreCase( "Closed" ) )
			t.closed = true;
		p.save();
		t.save();
		t.init();		
		Logs.addLog( Security.getConnected(), "Create", "TaskStatus", t.id, p, new Date( System.currentTimeMillis() ) );
		String url = "#";
		Notifications.notifyProjectUsers(p, "addTaskStatus", url, "Task Status", t.name, (byte) 0);		
		renderJSON( t.id );
	}

	/**
	 * This method edits a selected Task Status from the list of the task
	 * statuses in a specific project.
	 * 
	 * @author Heba Elsherif
	 * @parm statusID the id of the selected Task Status.
	 * @parm newName the new name of the selected Task Status.
	 * @parm indicator an indicator to indicate weather the Task Status
	 *       indicates pending or closed.
	 * @return void
	 * @issue 224
	 * @sprint 4
	 */
	public static void editTaskStatus( long statusID, String newName, String indicator )
	{
		TaskStatus taskStatus = TaskStatus.findById( statusID );		
		Project p = Project.findById( taskStatus.project.id );
		Security.check( Security.getConnected().in( p ).can( "editProject" ) );
		taskStatus.name = newName;
		taskStatus.pending = false;
		taskStatus.closed = false;
		if( indicator.equalsIgnoreCase( "Pending" ) )
			taskStatus.pending = true;
		if( indicator.equalsIgnoreCase( "Closed" ) )
			taskStatus.closed = true;
		taskStatus.save();		
		Logs.addLog( Security.getConnected(), "Edit", "TaskStatus", taskStatus.id, p, new Date( System.currentTimeMillis() ) );		
		String url = "#";
		Notifications.notifyProjectUsers(p, "editTaskStatus", url, "Task Status", taskStatus.name, (byte) 0);		
		renderJSON( true );
	}

	/**
	 * This method removes a task status from the list of task statuses in
	 * project.
	 * 
	 * @author Behairy, Heba Elsherif
	 * @param statusID
	 *            the id of the selected Task Status.
	 * @return void
	 * @issue 236
	 * @sprint 2, 4
	 */
	public static void removeTaskStatus( long statusID )
	{
		TaskStatus taskStatus = TaskStatus.findById( statusID );
		Security.check( Security.getConnected().in( taskStatus.project ).can( "editProject" ) );
		taskStatus.deleted = true;
		taskStatus.save();
		for( int i = 0; i < taskStatus.columns.size(); i++ )
		{
			taskStatus.columns.get( i ).deleted = true;
			taskStatus.columns.get( i ).save();
		}		
		Logs.addLog( Security.getConnected(), "Delete", "TaskStatus", taskStatus.id, taskStatus.project, new Date( System.currentTimeMillis() ) );
		String url = "#";
		Notifications.notifyProjectUsers(taskStatus.project, "deleteTaskStatus", url, "Task Status", taskStatus.name, (byte) -1);		
		renderJSON( true );
	}

	/**
	 * task status checks method that will be used to check for the task status
	 * before adding a new meeting type in the list of task status
	 * 
	 * @author Amr Hany
	 * @param id
	 * @param status
	 */
	public static void taskStatusCheck( long id, String status )
	{
		Project p = Project.findById( id );
		boolean statusExists = false;
		for( TaskStatus taskStatus : p.taskStatuses )
		{
			if( taskStatus.deleted == false )
			{
				if( taskStatus.name.equalsIgnoreCase( status ) )
				{
					statusExists = true;
					break;
				}
			}
		}
		renderJSON( statusExists );
	}

	/**
	 * This method checks that the task status name isn't used before in the
	 * list of task status in a specific project before editing.
	 * 
	 * @author Heba Elsherif
	 * @parm statusID the id of the selected Task Status.
	 * @param id
	 *            project id
	 * @param status
	 *            task status name
	 * @return void
	 * @issue 224
	 * @sprint 4
	 */
	public static void newTaskStatusCheck( long statusID, long id, String status )
	{
		Project p = Project.findById( id );
		TaskStatus t = TaskStatus.findById( statusID );
		boolean statusExists = false;
		for( TaskStatus taskStatus : p.taskStatuses )
		{
			if( taskStatus.deleted == false )
			{
				if( taskStatus.name.equalsIgnoreCase( status ) && !(t.name.equalsIgnoreCase( status )) )
				{
					statusExists = true;
					break;
				}
			}
		}
		renderJSON( statusExists );
	}

	/**
	 * This action method adds a task type to the array list of task types in
	 * project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param taskType
	 *            String
	 * @author Behairy
	 */

	public static void addTaskType( long id, String taskType )
	{
		Project p = Project.findById( id );
		// if () {
		Security.check( Security.getConnected().in( p ).can( "editProject" ) );
		TaskType t = new TaskType();
		t.project = p;
		t.name = taskType;

		p.save();
		t.save();
		Logs.addLog( Security.getConnected(), "Add", "Project Default Task Types ", t.id, p, new Date( System.currentTimeMillis() ) );
		renderJSON( t.id );
		// } else {
		// forbidden();
		// }

	}

	/**
	 * This action method removes a task type from the array list of task types
	 * in project.
	 * 
	 * @param taskID
	 *            long
	 * @author Behairy
	 */
	// 3ayzen id
	// @Check ("canEditProject")
	public static void removetaskType( long taskID )
	{
		TaskType taskType = TaskType.findById( taskID );
		Security.check( Security.getConnected().in( taskType.project ).can( "editProject" ) );
		taskType.deleted = true;

		taskType.save();
		Logs.addLog( Security.getConnected(), "Remove", "Project Default Task Type", taskType.id, taskType.project, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
	}

	/**
	 * This action method add a story priority to the list of story priorities
	 * in project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param storyType
	 *            String
	 * @author Behairy
	 */

	public static void addStoryType( long id, String storyType, String unit )
	{
		Project p = Project.findById( id );
		// if () {
		Security.check( Security.getConnected().in( p ).can( "editProject" ) );
		Priority x = new Priority();
		x.project = p;
		x.title = storyType;
		x.priority = Integer.parseInt( unit );

		x.save();

		p.save();
		Logs.addLog( Security.getConnected(), "Add", "Project Default Story Priroity ", x.id, x.project, new Date( System.currentTimeMillis() ) );
		renderJSON( x.id );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This action method removes a story priority from the list of priorities
	 * in project.
	 * 
	 * @param priorityID
	 *            int
	 * @author Behairy
	 */
	// 3ayzen id
	// @Check ("canEditProject")
	public static void removeStoryType( long priorityID )
	{

		Priority priorityInstance = Priority.findById( priorityID );
		Security.getConnected().in( priorityInstance.project ).can( "editProject" );
		priorityInstance.deleted = true;

		priorityInstance.save();
		Logs.addLog( Security.getConnected(), "Remove", "Project Default Story Priroity ", priorityInstance.id, priorityInstance.project, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
	}

	/**
	 * This action method changes the boolean status of the auto-reschedule of
	 * meeting for the project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param autoReschedule
	 *            boolean
	 * @author Behairy
	 */

	public static void changeAutoRescheduleStatus( long id, boolean autoReschedule )
	{
		Project p = Project.findById( id );
		Security.getConnected().in( p ).can( "editProject" );
		// if (Security.getConnected().in(p).can("editProject")) {
		p.autoReschedule = autoReschedule;
		p.save();
		Logs.addLog( Security.getConnected(), "Edit", "Project Default Auto Meeting Reschedule Option ", p.id, p, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This action method sets the default sprint duration of the specified
	 * project by id.
	 * 
	 * @param id
	 *            long
	 * @param duration
	 *            String
	 * @author Behairy
	 */

	public static void setDefaultSprintDuartion( long id, String duration )
	{
		Project p = Project.findById( id );
		Security.getConnected().in( p ).can( "editProject" );
		// if (Security.getConnected().in(p).can("editProject")) {
		p.sprintDuration = Integer.parseInt( duration );
		p.save();
		Logs.addLog( Security.getConnected(), "Edit", "Project Default Sprint Duration ", p.id, p, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This action method sets the effort estimation unit of the specified
	 * project by id. The effort estimation is set to 0 if in hours and 1 if in
	 * points.
	 * 
	 * @param id
	 * @param unit
	 * @author Behairy
	 */

	public static void setEffortEstimationUnit( long id, String unit )
	{
		Project p = Project.findById( id );
		Security.getConnected().in( p ).can( "editProject" );
		// if (Security.getConnected().in(p).can("editProject")) {
		int selectedUnit;
		if( unit == "Hours" )
			selectedUnit = 0;
		else
			selectedUnit = 1;

		p.effortEstimationUnit = selectedUnit;
		p.save();
		Logs.addLog( Security.getConnected(), "Edit", "Project Default Effort Estimation Unit", p.id, p, new Date( System.currentTimeMillis() ) );
		renderJSON( true );
		// } else {
		// forbidden();
		// }
	}

	/**
	 * This method takes project id and initiates a new request for that user to
	 * be deleted from that project
	 * 
	 * @author Amr Hany
	 * @param projectID
	 */
	public static void RequestDeletedFromProject( long id )
	{

		User u = Security.getConnected();
		Project project = Project.findById( id );

		if( !u.in( project ).can( "manageRequests" ) )
		{
			if( Request.find( "byIsDeletionAndUserAndProject", true, u, project ).first() == null )
			{
				Request x = new Request( u, project );

				x.save();
				renderJSON( true );
			}
			else
			{

				renderJSON( false );
			}
		}
		else
		{
			project.users.remove( u );
			project.save();
			u.projects.remove( project );
			u.save();
			if( u.roles != null )
			{
				for( Role r : u.roles )
				{
					if( r.project.equals( project ) )
					{
						u.roles.remove( r );
					}
				}
			}
			u.save();
			renderJSON( true );
			Logs.addLog( "User: " + Security.getConnected().name + " has deleted him/herself from project: " + project.name );
			String url = "/show/user?id="+u.id;
			Notifications.notifyProjectUsers(project, "deletedFromProject", url, "himself", u.name, (byte) -1);			

		}

	}

	/**
	 * the page that display to the user the confirmation of being deleted from
	 * the project
	 * 
	 * @author Amr Hany
	 * @param id
	 */
	public static void projectDeletionRequest( long id )
	{
		Project project = Project.findById( id );
		render( project );
	}

	/**
	 * This method fetches and renders the corresponding
	 * ProjectNotificationProfile when The scrum master clicks the Edit
	 * notifications link corresponding to a certain project.
	 * 
	 * @author Moataz_Mekki
	 * @param projectId
	 *            The id of that project the user wants to manage his
	 *            notifications in.
	 * @throws ClassNotFoundException
	 */
	// @Check ("canEditProjectNotificationProfile")
	public static void manageNotificationProfile( long projectId ) throws ClassNotFoundException
	{
		Project currentProject = Project.findById( projectId );
		Security.check( currentProject, "editProjectNotificationProfile" );
		ProjectNotificationProfile currentNotificationProfile = currentProject.notificationProfile;
		ObjectType type = ObjectType.get( ProjectNotificationProfiles.class );
		notFoundIfNull( type );
		if( currentNotificationProfile == null || currentNotificationProfile.deleted )
			error( "Could not find a notification profile for this project" );
		else
		{
			JPASupport object = type.findById( currentNotificationProfile.id );
			try
			{

				render( currentNotificationProfile, type, object );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object );
			}
		}
	}

	/**
	 * This method saves any modifications made by the scrum in a given
	 * ProjectNotificationProfile in the UI Side to the database. And renders a
	 * success message.
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            The id of that project that the scrum master is editing its
	 *            notification profile
	 * @throws Exception
	 */
	public static void saveNotificationProfile( String id ) throws Exception
	{
		ObjectType type = ObjectType.get( ProjectNotificationProfiles.class );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Security.check( ((ProjectNotificationProfile) object).project, "editProjectNotificationProfile" );
		validation.valid( object.edit( "object", params ) );
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/show.html", type, object );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object );
			}
		}
		object.save();
		flash.success( "The Project Notificaton Profile modifications have been saved" );
		if( params.get( "_save" ) != null )
		{
			redirect( "/projects/managenotificationprofile?projectId=" + id );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/**
	 * @author Moataz_Mekki
	 * @param id
	 *            : the id of the project this method renders the list of
	 *            project members to the html page
	 */
	public static void getProjectMembers( long id )
	{
		Project pro = Project.findById( id );
		List<User> users = pro.users;
		render( users, pro );
	}

	/**
	 * @author mahmoudsakr this method renders the projects of the connected
	 *         user
	 */
	public static void myProjects()
	{
		User user = Security.getConnected();
		List<Project> projects = user.projects;
		render( projects );
	}

	/**
	 * Action for Manage Project Request Page, renders list of all pending
	 * projects requests.
	 * 
	 * @author Behairy
	 */
	public static void manageProjectRequests()
	{
		Security.check( Security.getConnected().isAdmin );
		List<Project> pendingProjects = Project.find( "approvalStatus=false AND deleted=false" ).fetch();
		render( pendingProjects );
	}

	/**
	 * This method approves the pending request for the project given by the ID
	 * as a parameter. It Notifies project owner with project request status.
	 * 
	 * @param id
	 *            projectID
	 * @author Behairy
	 */
	public static void approveRequest( long id, String message )
	{
		Security.check( Security.getConnected().isAdmin );
		Project p = Project.findById( id );
		User user = p.user;
		p.approvalStatus = true;		
		String url = "@{Application.externalOpen("+p.id+", '#', false)}";
		Notifications.notifyUser(user, "Approv", url, "Project", p.name, (byte) 1, null);
		p.save();
		p.init();
		Role proAdmin = Role.find( "name= 'Project Creator' and project =" + p.id ).first();
		Security.getConnected().addRole( proAdmin );
		renderJSON( true );
	}

	/**
	 * This method declines the pending request for the project given by the ID
	 * as a parameter, by deleting the project (i.e setting flag). It Notifies
	 * project owner with project request status.
	 * 
	 * @param id
	 *            projectID
	 * @author Behairy
	 */
	public static void declineRequest( long id, String message )
	{
		Security.check( Security.getConnected().isAdmin );
		Project p = Project.findById( id );
		p.deleted = true;
		User user = p.user;
		String url = "#";
		Notifications.notifyUser(user, "Declin", url, "Project", p.name, (byte) -1, null);		
		p.save();
		renderJSON( true );
	}

	/**
	 * meeting types checks method that will be used to check for the meeting
	 * type before adding a new meeting type in the list of meeting types
	 * 
	 * @author Amr Hany
	 * @param id
	 * @param type
	 */
	public static void meetingTypesCheck( long id, String type )
	{
		Project p = Project.findById( id );
		boolean typeExists = false;
		for( String meetingType : p.meetingsTypes )
		{
			if( meetingType.equalsIgnoreCase( type ) )
			{
				typeExists = true;
				break;
			}
		}
		renderJSON( typeExists );
	}

	/**
	 * task types checks method that will be used to check for the task type
	 * before adding a new task type in the list of task types
	 * 
	 * @author Amr Hany
	 * @param id
	 * @param type
	 */
	public static void taskTypesCheck( long id, String type )
	{
		Project p = Project.findById( id );
		boolean typeExists = false;
		for( TaskType taskType : p.taskTypes )
		{
			if( taskType.deleted == false )
			{
				if( taskType.name.equalsIgnoreCase( type ) )
				{
					typeExists = true;
					break;
				}
			}
		}
		renderJSON( typeExists );
	}

	/**
	 * Story proirity check method that checks for the priority name before
	 * adding it to a project
	 * 
	 * @author Amr Hany
	 * @param id
	 * @param pName
	 */
	public static void storyPriorityCheck( long id, String pName )
	{
		Project p = Project.findById( id );
		boolean typeExists = false;
		for( Priority priority : p.priorities )
		{
			if( priority.deleted == false )
			{
				if( priority.title.equalsIgnoreCase( pName ) )
				{

					typeExists = true;
					break;
				}
			}
		}
		renderJSON( typeExists );
	}

	/**
	 * Passes along the project id to the events page
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            the project id
	 **/
	public static void events( long id )
	{
		Project project = Project.findById( id );
		render( project );
	}

	/**
	 * Passes along the project id to the settings page
	 * 
	 * @author Hadeer Younis
	 * @param the
	 *            project
	 */
	public static void settings( long id )
	{
		Project project = Project.findById( id );
		render( project );
	}

	/**
	 * shows requests in this project
	 */
	public static void requests( long id )
	{
		Project project = Project.findById( id );
		render( project );
	}

	public static void delete()
	{
		forbidden();
	}

	public static void save()
	{
		forbidden();
	}

	public static void list()
	{
		forbidden();
	}

	/**
	 * this method takes the id of the project to be deleted and sets the
	 * deleted attribute to true and then sends notification e-mail to the
	 * project members that the project has been deleted Also,When the project
	 * is deleted the associated Meetings,Sprints,Components,Roles,
	 * ,Board,ChatRoom are deleted (deletion marker set to true).
	 * 
	 * @Author Ghada Fakhry & Amr Hany
	 * @param id
	 */
	public static void deleteProject( long id )
	{
		Project project = Project.findById( id );
		Security.check( Security.getConnected().in( project ).can( "deleteproject" ) );
		if( project.hasRunningSprints() )
		{
			forbidden();
		}
		project.deleted = true;

		project.board.deleted = true;
		for( Column c : project.board.columns )
		{
			c.deleted = true;
			c.save();
		}
		for( Snapshot s : project.board.snapshot )
		{
			s.deleted = true;
			s.save();
		}
		project.board.save();

		// ChatRoom room = project.chatroom;
		// room.deleted = true;
		// room.save();
		project.notificationProfile.deleted = true;
		project.notificationProfile.save();

		for( ProductRole temp : project.productRoles )
		{
			temp.deleted = true;
			temp.save();
		}

		for( Meeting temp : project.meetings )
		{
			temp.deleted = true;
			temp.save();
			List<MeetingAttendance> attendees = MeetingAttendance.find( "byMeeting", temp ).fetch();
			for( MeetingAttendance ma : attendees )
			{
				ma.deleted = true;
				ma.save();
			}
			for( Artifact note : temp.artifacts )
			{
				note.deleted = true;
				note.save();
			}

		}

		for( Role temp : project.roles )
		{
			temp.deleted = true;
			temp.save();
		}

		for( Sprint temp : project.sprints )
		{
			temp.deleted = true;
			temp.save();
		}
		for( Priority temp : project.priorities )
		{
			temp.deleted = true;
			temp.save();
		}
		for( TaskStatus temp : project.taskStatuses )
		{
			temp.deleted = true;
			temp.save();
		}
		for( TaskType temp : project.taskTypes )
		{
			temp.deleted = true;
			temp.save();
		}

		for( Component temp : project.components )
		{
			if( temp.componentBoard != null )
			{
				temp.componentBoard.deleted = true;
				temp.componentBoard.save();

				for( Column c : temp.componentBoard.columns )
				{
					c.deleted = true;
					c.save();
				}
				for( Snapshot s : temp.componentBoard.snapshot )
				{
					s.deleted = true;
					s.save();
				}
			}
			temp.deleted = true;
			temp.save();
		}

		for( UserNotificationProfile temp : project.userNotificationProfiles )
		{
			temp.deleted = true;
			temp.save();
		}

		project.save();		
		String url = "#";
		Notifications.notifyProjectUsers(project, "deleteProject", url, "Project", project.name, (byte) -1);		
		Logs.addLog( Security.getConnected(), "Deleted Project", "project", id, project, new Date() );
		renderJSON( true );

	}

	/**
	 * The method that renders the project deletion page
	 * 
	 * @param id
	 */
	public static void projectDeletion( long id )
	{
		Project project = Project.findById( id );
		if( project.deleted )
			notFound();
		boolean runningSprints = project.hasRunningSprints();
		boolean upcomingMeetings = Meeting.find( "byProjectAndDeleted", project, false ).fetch().size() > 0;
		List<Sprint> Sprints = Sprint.find( "byProjectAndDeleted", project, false ).fetch();
		boolean upcomingSprints = false;
		for( Sprint s : Sprints )
		{
			if( s.startDate.after( new Date() ) )
			{
				upcomingSprints = true;
				break;
			}
		}

		render( project, runningSprints, upcomingSprints, upcomingMeetings );

	}

}