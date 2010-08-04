package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import models.Artifact;
import models.Component;
import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.Sprint;
import models.Task;
import models.User;
import notifiers.Notifications;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

/**
 * @author ghadafakhry
 */
@With( Secure.class )
// @Check( "admin" ) will be un-done in late stages
public class Meetings extends SmartCRUD
{
	/**
	 * This method Overrides the CRUD.blank() method that is executed on adding
	 * a new meeting, Because the project ID is needed in order to allow the
	 * user to create meeting only in the project that he was redirected from
	 * 
	 * @author Ghada Fakhry
	 * @param projectID
	 */
	// @Check ("canAddMeeting")
	public static void blank( long id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		Project currentProject = Project.findById( id );
		if( currentProject.deleted )
			notFound();
		List<Sprint> sprints = currentProject.upcomingSprints();
		User creator = Security.getConnected();
		List<String> types = currentProject.meetingTypes();

		Security.check( Security.getConnected().in( currentProject ).can( "addMeeting" ) );
		try
		{

			render( type, currentProject, creator, sprints, types );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/blank.html", type );
		}
	}

	/**
	 * added this method to extend a running meeting on board
	 * 
	 * @author amr Abdelwahab
	 * @param meetingid
	 */

	public static void extend( long meetingid )
	{
		Meeting M = Meeting.findById( meetingid );
		boolean mem = false;
		List<MeetingAttendance> att = MeetingAttendance.find( "meeting.id = ?1 and deleted = ?2 and status LIKE ?3", meetingid, false, "confirmed" ).fetch();
		for( MeetingAttendance at : att )
		{
			if( at.user == Security.getConnected() )
			{
				mem = true;
			}
		}
		if( !mem && !Security.getConnected().isAdmin )
		{
			forbidden();
		}
		M.endTime += 1000 * 60 * 60;
		M.save();
	}

	/**
	 * added this method to end a running meeting on board
	 * 
	 * @author amr Abdelwahab
	 * @param meetingid
	 */
	public static void end( long meetingid )
	{
		Meeting M = Meeting.findById( meetingid );
		boolean mem = false;
		for( MeetingAttendance att : M.users )
		{
			if( att.user == Security.getConnected() )
			{
				mem = true;
			}
		}
		if( !mem && !Security.getConnected().isAdmin )
		{
			forbidden();
		}
		M.endTime = new Date().getTime();
		M.save();
		List<MeetingAttendance> attendees = MeetingAttendance.find( "byMeeting.idAndDeleted", M.id, false ).fetch();
		Logs.addLog( Security.getConnected(), "Ended", "Meeting", M.id, M.project, new Date( System.currentTimeMillis() ) );
		for( int i = 0; i < attendees.size(); i++ )
		{
			String url = "@{Application.externalOpen(" + M.id + ", '/meetings/viewMeeting?id=" + M.id + "', false)}";
			Notifications.notifyUser( attendees.get( i ).user, "End", url, "Meeting", M.name, (byte) 0, M.project );
		}
	}

	/**
	 * added this method to render the sprints with the page
	 * 
	 * @author minazaki
	 * @param id
	 */
	public static void show( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Meeting meeting = (Meeting) object;
		Project currentProject = meeting.project;
		List<Sprint> sprints = currentProject.upcomingSprints();
		List<String> types = currentProject.meetingTypes();
		try
		{
			render( type, object, sprints, types );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/show.html", type, object );
		}
	}

	/**
	 * This method Overrides CRUD.create() method that is executed on posting
	 * the data of the new Meeting. CRUD.create() had an error on validating the
	 * data it redirects into an error because the project id was not sent again
	 * to the page so it gives an error so I just added the currentProject to
	 * the render method
	 * 
	 * @author Ghada Fakhry
	 * @throws Exception
	 */
	public static void create() throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.entityClass.newInstance();
		validation.valid( object.edit( "object", params ) );
		Meeting temp = (Meeting) object;
		Project currentProject = temp.project;
		Security.check( Security.getConnected().in( currentProject ).can( "addMeeting" ) );
		User creator = Security.getConnected();
		Date currentDate = new Date();
		long longCurrentDate = currentDate.getTime();
		List<Sprint> sprints = currentProject.upcomingSprints();
		List<String> types = currentProject.meetingTypes();
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				// Here is the only difference,, in order to make validation
				// redirect to the same page without giving error and with the
				// same project

				render( "Meetings/blank.html", type, currentProject, creator, sprints, types );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else if( !(temp.startTime > longCurrentDate && temp.startTime < temp.endTime) )
		{

			renderArgs.put( "error", "Please fix Meeting date" );
			render( "Meetings/blank.html", type, currentProject, creator, sprints, types );
			// render( request.controller.replace( ".", "/" ) + "/blank.html",
			// type );
		}
		/*
		 * adding the selected sprint to the meeting
		 * @author minazaki
		 */
		if( !params.get( "object.sprintid" ).equals( "none" ) )
		{
			Sprint sprint = Sprint.findById( Long.parseLong( params.get( "object.sprintid" ) ) );
			sprint.meetings.add( temp );
			sprint.save();
			temp.sprint = sprint;
		}
		if( params.get( "object.type" ) != null )
		{
			temp.type = params.get( "object.type" );
		}

		object.save();
		if( params.get( "attending" ) != null )
		{
			MeetingAttendance ma = new MeetingAttendance( temp.creator, temp );
			ma.status = "confirmed";
			ma.save();
		}

		Logs.addLog( Security.getConnected(), "create", "Meeting", temp.id, temp.project, new Date( System.currentTimeMillis() ) );
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			// redirect( request.controller + ".list" );
			// Meetings.viewMeetings( currentProject.id );
			Application.overlayKiller( "reload('meetings')" );
		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			redirect( request.controller + ".blank" );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/**
	 * this method takes a project ID and renders all meetings in that project
	 * 
	 * @author Ghada Fakhry
	 * @return void
	 * @param projectID
	 */

	public static void viewMeetings( long id )
	{
		/*
		 * View meetings controller which takes a projectID as an ID and returns
		 * the meeting to use it in the model view
		 */
		boolean empty = false;
		Date currDate = new Date();
		Project project = Project.findById( id );
		List<Meeting> meetings = Meeting.find( "byProject.idAndDeleted", id, false ).fetch();
		List<Meeting> upcoming = new ArrayList<Meeting>();
		List<Meeting> past = new ArrayList<Meeting>();
		List<Meeting> current = new ArrayList<Meeting>();
		if( meetings.size() == 0 )
		{
			empty = true;
		}
		while( meetings.isEmpty() == false )
		{
			Meeting temp = (meetings.remove( 0 ));
			Date tempStart = new Date( temp.startTime );
			Date tempEnd = new Date( temp.endTime );
			if( currDate.after( tempEnd ) )
				past.add( temp );
			else if( currDate.before( tempStart ) )
				upcoming.add( temp );
			else
				current.add( temp );
		}

		List<MeetingAttendance> invitations = MeetingAttendance.find( "meeting.project = ?1 and user = ?2 and deleted = ?3 and meeting.endTime > ?4 and status LIKE ?5", project, Security.getConnected(), false, new Date().getTime(), "waiting" ).fetch();

		String projectName = project.name;
		render( meetings, id, projectName, past, upcoming, current, project, invitations, empty );

	}

	public static void save( String id ) throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		validation.valid( object.edit( "object", params ) );
		Meeting temp = (Meeting) object;
		Project currentProject = temp.project;
		Security.check( Security.getConnected().in( currentProject ).can( "editMeeting" ) || temp.creator.equals( Security.getConnected() ) || temp.endTime < new Date().getTime() );
		Date currentDate = new Date();
		long longCurrentDate = currentDate.getTime();
		List<Sprint> sprints = currentProject.upcomingSprints();
		List<String> types = currentProject.meetingTypes();
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( "Meetings/show.html", type, object, sprints, types );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object, sprints, types );
			}
		}
		else if( !(temp.startTime > longCurrentDate && temp.startTime < temp.endTime) )
		{

			renderArgs.put( "error", "Please fix Meeting date" );
			render( "Meetings/show.html", type, object, sprints, types );
			// render( request.controller.replace( ".", "/" ) + "/blank.html",
			// type );
		}
		/*
		 * adding the selected sprint to the meeting
		 * @author minazaki
		 */
		if( !params.get( "object.sprintid" ).equals( "none" ) )
		{
			Sprint sprint = Sprint.findById( Long.parseLong( params.get( "object.sprintid" ) ) );
			sprint.meetings.add( temp );
			sprint.save();
			temp.sprint = sprint;
		}
		if( params.get( "object.type" ) != null )
		{
			temp.type = params.get( "object.type" );
		}

		object.save();

		Logs.addLog( Security.getConnected(), "edit", "Meeting", temp.id, temp.project, new Date( System.currentTimeMillis() ) );
		flash.success( "Meeting edited successfully" );
		if( params.get( "_save" ) != null )
		{
			// redirect( request.controller + ".list" );
			// Meetings.viewMeetings( currentProject.id );
			Application.overlayKiller( "reload('meeting-" + temp.id + "')" );
		}
	}

	public static void associations( long id )
	{
		// amr hany part :
		User currentUser = Security.getConnected();
		// here will go our tasks
		// ghada();
		// behairy();
		// hossam();
		// mina();
		Meeting meeting = Meeting.findById( id );
		Security.check( Security.getConnected().in( meeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( meeting.creator ) );
		List<Artifact> temp = Artifact.findAll();
		List<Artifact> artifacts = new ArrayList<Artifact>();
		for( int i = 0; i < temp.size(); i++ )
		{
			if( !temp.get( i ).meetingsArtifacts.contains( meeting ) )
			{
				artifacts.add( temp.get( i ) );
			}
		}
		List<Task> temp2 = Task.findAll();
		List<Task> tasks = new ArrayList<Task>();
		for( int i = 0; i < temp2.size(); i++ )
		{
			if( !temp2.get( i ).meeting.contains( meeting ) )
				tasks.add( temp2.get( i ) );
		}
		render( meeting, currentUser, artifacts, tasks );
	}

	/**
	 * this method to add artifact from the meeting
	 * 
	 * @author minazaki
	 * @param id
	 * @param artifact
	 */
	public static void addArtifact( long id, long artifact )
	{
		Artifact temp = Artifact.findById( artifact );
		Meeting meeting = Meeting.findById( id );
		Security.check( Security.getConnected().in( meeting.project ).can( "AssociateArtifacts" ) );
		meeting.artifacts.add( temp );
		temp.meetingsArtifacts.add( meeting );
		meeting.save();
		temp.save();
	}

	/**
	 * @author minazakiz
	 * @param id
	 *            - the meeting id
	 * @param tas
	 *            - the task id this method to add the task to the meeting
	 */
	public static void addTask( long id, long task )
	{
		Task temp = Task.findById( task );
		Meeting meeting = Meeting.findById( id );
		Security.check( Security.getConnected().in( meeting.project ).can( "associateTaskToMeeting" ) );
		meeting.tasks.add( temp );
		temp.meeting.add( meeting );
		meeting.save();
		temp.save();
	}

	/**
	 * this method to remove artifact from the meeting
	 * 
	 * @author minazaki
	 * @param id
	 * @param artifact
	 */
	public static void removeArtifact( long id, long artifact )
	{
		Artifact temp = Artifact.findById( artifact );
		Meeting meeting = Meeting.findById( id );
		Security.check( Security.getConnected().in( meeting.project ).can( "AssociateArtifacts" ) );
		meeting.artifacts.remove( temp );
		temp.meetingsArtifacts.remove( temp );
		meeting.save();
		temp.save();
	}

	/**
	 * @author minazaki
	 * @param id
	 *            - meeting id
	 * @param task
	 *            - task id this method to remove the task from the meeting
	 */
	public static void removetask( long id, long task )
	{
		Task temp = Task.findById( task );
		Meeting meeting = Meeting.findById( id );
		Security.check( Security.getConnected().in( meeting.project ).can( "associateTaskToMeeting" ) );
		meeting.tasks.remove( temp );
		temp.meeting.remove( temp );
		meeting.save();
		temp.save();
	}

	/**
	 * this method takes as a parameter the meeting ID and renders the
	 * information of the meeting
	 * 
	 * @author Ghada Fakhry
	 * @return void
	 * @param meetingID
	 */

	public static void viewMeeting( long id )
	{
		Meeting meeting = Meeting.find( "byIdAndDeleted", id, false ).first();
		notFoundIfNull( meeting );
		MeetingAttendance ma = MeetingAttendance.find( "byMeetingAndUserAndDeleted", meeting, Security.getConnected(), false ).first();
		boolean invited = false;
		boolean attending = false;
		boolean declined = false;
		if( ma == null )
		{
			invited = false;
		}
		else if( ma.status.equals( "confirmed" ) )
		{
			invited = true;
			attending = true;
		}
		else if( ma.status.equals( "declined" ) )
		{
			invited = true;
			declined = true;
		}
		else if( ma.status.equals( "waiting" ) )
		{
			invited = true;
		}

		boolean past = false;
		if( meeting.endTime < new Date().getTime() )
			past = true;

		render( meeting, invited, attending, declined, past );
	}

	/**
	 * this method takes as an input the meeting id to be deleted and then sets
	 * the attribute deleted to true and redirects back to the page of all
	 * meetings. It sends notifications to the meeting attendees if there were
	 * notes (artifacts of type notes) associated with that meeting. if the
	 * meeting date is after current date, it cancels the meeting and sends
	 * notification to users as well
	 * 
	 * @author Ghada Fakhry
	 * @param id
	 */
	// @Check ("canDeleteMeeting")
	public static void deleteMeeting( long id )
	{
		Meeting meeting = Meeting.findById( id );
		Project currentProject = meeting.project;
		long longTempStart = meeting.startTime;
		Security.check( Security.getConnected().in( currentProject ).can( "deleteMeeting" ) );
		meeting.deleted = true;
		String message = "";
		Date currDate = new Date();
		long longCurrDate = currDate.getTime();
		List<MeetingAttendance> attendees = MeetingAttendance.find( "byMeeting.id", meeting.id ).fetch();
		List<User> users = new ArrayList<User>();
		while( attendees.isEmpty() == false )
		{
			users.add( attendees.remove( 0 ).user );
		}

		if( longCurrDate < longTempStart )
		{
			meeting.status = false;
			if( users.isEmpty() == false )
				message = "@{Application.externalOpen(" + meeting.project.id + ", '/meetings/viewMeetings?id=" + meeting.project.id + "', false)}";
			Notifications.notifyUsers( users, "Cancel", message, "Meeting", meeting.name, (byte) -1, meeting.project );
		}

		List<Artifact> artifacts = meeting.artifacts;
		boolean flag = false;
		while( artifacts.isEmpty() == false )
		{
			Artifact temp = artifacts.remove( 0 );
			String type = temp.type;
			if( type.equals( "Notes" ) )
				flag = true;
		}
		if( flag )
		{
			if( users.isEmpty() == false )
				message = "unfortunately " + meeting.name + " meeting notes are deleted.";
			// Notifications.notifyUsers( users, "Meeting Notes deleted",
			// message, (byte) -1 );
		}
		Logs.addLog( Security.getConnected(), "delete", "Meeting", meeting.id, meeting.project, new Date( System.currentTimeMillis() ) );
		meeting.save();
		// redirect( "/projects/" + meeting.project.id + "/meetings" );
		//
		// } else
		// forbidden();

	}

	/**
	 * Invite user method that send a notification to a given user on inviting
	 * him to attend a meeting
	 * 
	 * @param meetingID
	 * @param userID
	 */

	public static void inviteUser( long meetingID, long userID )
	{
		Meeting currentMeeting = Meeting.findById( meetingID );
		Security.check( Security.getConnected().in( currentMeeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( currentMeeting.creator ) );
		User invitedUser = User.findById( userID );
		if( currentMeeting.endTime > new Date().getTime() )
		{
			if( MeetingAttendance.find( "byMeetingAndUserAndDeleted", currentMeeting, invitedUser, false ).first() == null )
			{
				MeetingAttendance attendance = new MeetingAttendance( invitedUser, currentMeeting );
				attendance.save();
				List<User> userList = new LinkedList<User>();
				userList.add( invitedUser );
				String meetingHash = attendance.meetingHash;
				String confirmURL = "http://localhost:9000/meetingAttendances/confirm?meetingHash=" + meetingHash;
				String declineURL = "http://localhost:9000/meetingAttendances/decline?meetingHash=" + meetingHash;
				String header = "Invitation to Meeting in " + currentMeeting.project.name + " project";
				String body1 = "Hello " + invitedUser.name;
				String body2 = "You have been invited to attend " + currentMeeting.name + " ";
				String body3 = "To confirm attending please click on this link : " + confirmURL + " ";
				String body4 = "To Decline the invitation please click this link: " + declineURL + " ";
				String body = body1 + "\n" + "\n" + body2 + "\n" + body3 + "\n\n" + body4;
				// Notifications.notifyUsers( userList, header, body, (byte) 0
				// );
				renderText( "User invited to meeting successfully.|reload('meetingAttendees-" + currentMeeting.id + "')" );
			}
			else
			{
				renderText( "User is already invited to this meeting." );
			}
		}
		else
		{
			renderText( "The meeting has already passed" );
		}
	}

	/**
	 * This method is used on associating all members of the project to a
	 * meeting where it takes the meeting ID and get all the users of this
	 * project and start sending notifications to the users that were not
	 * invited before to this meeting.
	 * 
	 * @param meetingID
	 */

	public static void inviteAllMembers( long meetingID )
	{
		Meeting meeting = Meeting.findById( meetingID );
		List<User> projectMembers = meeting.project.users;
		Security.check( Security.getConnected().in( meeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( meeting.creator ) );
		for( User invitedUser : projectMembers )
		{
			if( invitedUser.deleted == false )
			{
				if( invitedUser.meetingStatus( meetingID ).equals( "notInvited" ) )
				{
					MeetingAttendance attendance = new MeetingAttendance( invitedUser, meeting );
					attendance.save();
					List<User> userList = new LinkedList<User>();
					userList.add( invitedUser );
					String meetingHash = attendance.meetingHash;
					String confirmURL = "http://localhost:9000/meetingAttendances/confirm?meetingHash=" + meetingHash;
					String declineURL = "http://localhost:9000/meetingAttendances/decline?meetingHash=" + meetingHash;
					String header = "Invitation to Meeting in " + meeting.project.name + " project";
					String body1 = "Hello " + invitedUser.name;
					String body2 = "You have been invited to attend " + meeting.name + " ";
					String body3 = "To confirm attending please click on this link : " + confirmURL + " ";
					String body4 = "To Decline the invitation please click this link: " + declineURL + " ";
					String body = body1 + "\n" + "\n" + body2 + "\n" + body3 + "\n\n" + body4;
					// Notifications.notifyProjectUsers( meeting.project,
					// header, body, "setMeeting", (byte) 0 );
				}
			}
		}
		for( Component c : meeting.project.components )
		{
			meeting.components.add( c );
			meeting.save();
		}
	}

	/**
	 * Invite component method that takes the meeting id and the component id
	 * and send an invitation to each member inside this component IF he was not
	 * invited before else it just pass by this user.
	 * 
	 * @author Amr Hany
	 * @param meetingID
	 * @param componentID
	 */
	public static void inviteComponent( long meetingID, long componentID )
	{
		Meeting meeting = Meeting.findById( meetingID );
		Component component = Component.findById( componentID );

		Security.check( Security.getConnected().in( meeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( meeting.creator ) );
		if( !component.deleted )
		{
			for( User user : component.componentUsers )
			{
				if( !user.deleted )
				{
					if( user.meetingStatus( meetingID ).equals( "notInvited" ) )
					{
						MeetingAttendance attendance = new MeetingAttendance( user, meeting );
						attendance.save();
						List<User> userList = new LinkedList<User>();
						userList.add( user );
						String meetingHash = attendance.meetingHash;
						String confirmURL = "http://localhost:9000/meetingAttendances/confirm?meetingHash=" + meetingHash;
						String declineURL = "http://localhost:9000/meetingAttendances/decline?meetingHash=" + meetingHash;
						String header = "Invitation to Meeting in " + meeting.project.name + " project";
						String body1 = "Hello " + user.name;
						String body2 = "You have been invited to attend " + meeting.name + " ";
						String body3 = "To confirm attending please click on this link : " + confirmURL + " ";
						String body4 = "To Decline the invitation please click this link: " + declineURL + " ";
						String body = body1 + "\n" + "\n" + body2 + "\n" + body3 + "\n\n" + body4;
						// Notifications.notifyUsers( userList, header, body,
						// (byte) 0 );
					}
				}
			}
			meeting.components.add( component );
			meeting.save();
		}
	}

	/**
	 * Associating a Task to Meeting
	 * 
	 * @author hossam sharaf
	 * @param meetingID
	 *            , taskID
	 */
	public static void toggleTask( long meetingID, long taskID )
	{
		Meeting M = Meeting.findById( meetingID );
		Task T = Task.findById( taskID );
		Security.check( Security.getConnected().in( M.project ).can( "AssociateTaskToMeeting" ) );
		boolean B = false;
		if( M.tasks.contains( T ) )
		{
			M.tasks.remove( T );
			B = true;
		}
		else
			M.tasks.add( T );
		M.save();
		Logs.addLog( Security.getConnected(), "Associated task to meeting", "task", meetingID, M.project, new Date() );
		renderText( B );
	}

	/**
	 * This action takes the id of a meeting as a parameter and notifies
	 * attending users with the modifications.
	 * 
	 * @param id
	 * @author Behairy
	 */
	public static void notifyUsersWithModifications( long id )
	{
		boolean flag = false;
		Meeting meeting = Meeting.findById( id );
		List<MeetingAttendance> attendees = MeetingAttendance.find( "byMeeting.id", meeting.id ).fetch();
		List<User> users = new ArrayList<User>();
		while( attendees.isEmpty() == false )
		{
			users.add( attendees.remove( 0 ).user );
		}
		String message = "This is to Notify you that the Meeting " + meeting.name + " has been modified.";

		if( users.isEmpty() == false )
		{
			String url = "@{Application.externalOpen(" + meeting.id + ", '/meetings/viewMeeting?id=" + meeting.id + "', false)}";
			Notifications.notifyUsers( users, "Modifi", url, "Meeting", meeting.name, (byte) 0, meeting.project );
			flag = true;
		}
		renderJSON( flag );
	}

	/**
	 * This action takes the id of a meeting as a parameter and the note the
	 * user has taken and creates that note as an artifact and add it in the
	 * meeting artifacts.
	 * 
	 * @param id
	 *            , note
	 * @author menna_ghoneim
	 */

	public static void addNote( long id, String note )
	{

		Meeting meeting = Meeting.findById( id );
		Security.check( meeting.project, "addNote" );
		Artifact n = new Artifact( "Notes", note );
		n.save();
		meeting.artifacts.add( n );
		meeting.save();
		renderJSON( true );

	}

	/**
	 * Passes on the meeting id to the add a note page.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , meeting id
	 */
	public static void newNote( long id )
	{
		Meeting meeting = Meeting.findById( id );
		render( meeting );
	}

	/**
	 * Renders a note.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , note id
	 */
	public static void note( long id, int i, boolean noteFlag )
	{
		Artifact note = Artifact.findById( id );
		render( note, i, noteFlag );
	}

	/**
	 * Renders a page listing all the notes found in a certain meeting.
	 * 
	 * @param id
	 *            , meeting id
	 */
	public static void notes( long id )
	{
		Meeting myMeeting = Meeting.findById( id );
		boolean noteFlag = false;
		long longCurrentDate = new Date().getTime();
		List notes = myMeeting.artifacts;

		User theUser = Security.getConnected();
		if( (theUser.meetingStatus( id ).equals( "confirmed" )) && (myMeeting.endTime > longCurrentDate) )
			noteFlag = true;

		render( noteFlag, notes, id, myMeeting );

	}

	/**
	 * This method Used by C5 board in order to allow the user to directly join
	 * the meeting.
	 * 
	 * @author Amr Hany.
	 * @param meetingID
	 */

	public static void joinMeeting( long meetingID )
	{
		Meeting m = Meeting.findById( meetingID );
		if( m.endTime > new Date().getTime() )
		{
			if( Security.getConnected().in( m.project ).can( "joinMeeting" ) )
			{
				MeetingAttendance ma = MeetingAttendance.find( "user = ?1 and meeting =?2", Security.getConnected(), m ).first();
				if( ma != null )
				{
					ma.status = "confirmed";
					ma.reason = "";
					ma.save();
				}
				if( ma == null )
				{
					MeetingAttendance attendance = new MeetingAttendance( Security.getConnected(), m );
					attendance.status = "confirmed";
					attendance.save();
				}
				flash.success( "You have succesfully joined meeting " + m.name );
				renderJSON( true );
			}
			else
				renderJSON( false );
		}
		else
			renderJSON( false );
	}

	public static void invitedMembers( long meetingId )
	{
		Meeting meeting = Meeting.findById( meetingId );
		List<MeetingAttendance> attendance = MeetingAttendance.find( "byMeeting.idAndDeleted", meetingId, false ).fetch();
		List<MeetingAttendance> confirmed = new ArrayList<MeetingAttendance>();
		List<MeetingAttendance> declined = new ArrayList<MeetingAttendance>();
		List<MeetingAttendance> waiting = new ArrayList<MeetingAttendance>();
		while( attendance.isEmpty() == false )
		{
			MeetingAttendance ma = attendance.remove( 0 );
			if( ma.status.equals( "confirmed" ) )
			{
				confirmed.add( ma );
				continue;
			}

			if( ma.status.equals( "declined" ) )
			{
				declined.add( ma );
				continue;
			}
			if( ma.status.equals( "waiting" ) )
				;
			{
				waiting.add( ma );
				continue;
			}
		}

		render( confirmed, declined, waiting, meeting );

	}

	public static void meetingTasks( long meetingId )
	{
		render();
	}

	public static void viewAttendeeStatus( long id )
	{
		MeetingAttendance attendance = MeetingAttendance.findById( id );
		boolean past = (attendance.meeting.endTime < new Date().getTime());
		String status = "";
		if( !past )
		{
			if( attendance.status.equals( "waiting" ) )
			{
				status = "awaiting reply";
			}
			if( attendance.status.equals( "confirmed" ) )
			{
				status = "attending";
			}
			if( attendance.status.equals( "declined" ) )
			{
				status = "not attending";
			}

		}
		else
		{
			if( attendance.status.equals( "waiting" ) )
			{
				status = "did not reply";
			}
			if( attendance.status.equals( "confirmed" ) )
			{
				status = "attended";
			}
			if( attendance.status.equals( "declined" ) )
			{
				status = "did not attend";
			}
		}

		render( attendance, past, status );
	}
}