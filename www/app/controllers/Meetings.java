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
		List<Sprint> sprints = currentProject.upcomingSprints();
		User creator = Security.getConnected();
		List<String> types = currentProject.meetingTypes();

		if( Security.getConnected().in( currentProject ).can( "addMeeting" ) )
		{
			try
			{

				render( type, currentProject, creator, sprints, types );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else
			forbidden();
	}
	public static void extend( long meetingid ){
	Meeting M=Meeting.findById(meetingid);
	M.endTime+=1000*60*60;
	M.save();
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
		Project p = meeting.project;
		List<Sprint> sprints = p.upcomingSprints();
		try
		{
			render( type, object, sprints );
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
		User creator = Security.getConnected();
		Date currentDate = new Date();
		long longCurrentDate = currentDate.getTime();
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				// Here is the only difference,, in order to make validation
				// redirect to the same page without giving error and with the
				// same project

				render( "Meetings/blank.html", type, currentProject, creator );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		else if( !(temp.startTime > longCurrentDate && temp.startTime < temp.endTime) )
		{

			renderArgs.put( "error", "Please fix Meeting date" );
			render( "Meetings/blank.html", type, currentProject, creator );
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

		Logs.addLog( Security.getConnected(), "create", "Meeting", temp.id, temp.project, new Date( System.currentTimeMillis() ) );
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			// redirect( request.controller + ".list" );
			Meetings.viewMeetings( currentProject.id );
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
		Date currDate = new Date();
		Project project = Project.findById( id );
		List<Meeting> meetings = Meeting.find( "byProject.idAndDeleted", id, false ).fetch();
		List<Meeting> upcoming = new ArrayList<Meeting>();
		List<Meeting> past = new ArrayList<Meeting>();
		while( meetings.isEmpty() == false )
		{
			Meeting temp = (meetings.remove( 0 ));
			Date tempEnd = new Date( temp.endTime );
			if( tempEnd.before( currDate ) )
				past.add( temp );
			else

				upcoming.add( temp );

		}
		boolean pastIsEmpty = false;
		boolean upcomingIsEmpty = false;
		if( past.isEmpty() )
			pastIsEmpty = true;
		if( upcoming.isEmpty() )
			upcomingIsEmpty = true;
		Logs.addLog( Security.getConnected(), "view", "Meetings", project.id, project, new Date( System.currentTimeMillis() ) );
		String projectName = project.name;
		render( meetings, id, projectName, past, upcoming, upcomingIsEmpty, pastIsEmpty, project );

	}

	/**
	 * This action takes the meetings parameters and checks their validity
	 * before saving changes to meeting.
	 * 
	 * @param id
	 *            long
	 * @param name
	 *            String
	 * @param description
	 *            description
	 * @param startTime
	 *            long
	 * @param endTime
	 *            long
	 * @param location
	 *            String
	 * @param infrontBoard
	 *            boolean
	 *@author Behairy
	 */

	@Check( "canEditMeeting" )
	public static void saveChanges( long id, String name, String description, long startTime, long endTime, String location, String sprintId )
	{
		Meeting m = Meeting.findById( id );
		Date currentDate = new Date();
		long longCurrentDate = currentDate.getTime();
		boolean timeFlag = false;

		// Check Time Validity
		if( startTime > longCurrentDate && startTime < endTime )
		{

			timeFlag = true;

		}

		if( timeFlag )
		{
			m.startTime = startTime;
			m.endTime = endTime;
			m.description = description;
			m.location = location;
			m.name = name;
			/**
			 * adding the sprint to the meeting
			 * 
			 * @author minazaki
			 */
			if( !sprintId.equals( "none" ) )
			{
				Sprint sprint = Sprint.findById( Long.parseLong( sprintId ) );
				sprint.meetings.add( m );
				sprint.save();
				m.sprint = sprint;
			}
			Logs.addLog( Security.getConnected(), "Edit", "Meeting", m.id, m.project, new Date( System.currentTimeMillis() ) );
			m.save();
		}

		renderJSON( timeFlag );

	}

	public static void associations( long id )
	{

		// amr hany part :
		User currentUser = User.find( "byEmail", Security.connected() ).first();
		// here will go our tasks
		// ghada();
		// behairy();
		// hossam();
		// mina();
		Meeting meeting = Meeting.findById( id );
		if( Security.getConnected().in( meeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( meeting.creator ) )
		{
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
		else
			forbidden();
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

		Meeting meeting = Meeting.findById( id );
		List<MeetingAttendance> attendees = MeetingAttendance.find( "byMeeting.idAndDeleted", id, false ).fetch();
		User user = Security.getConnected();
		List<MeetingAttendance> theUser = MeetingAttendance.find( "byMeeting.idAndUser.id", id, user.id ).fetch();
		Date currentDate = new Date();
		long longCurrentDate = currentDate.getTime();
		boolean noteFlag = false;
		if( theUser == null || theUser.isEmpty() )
			render( meeting, attendees, noteFlag );

		if( (theUser.get( 0 ).status.equals( "confirmed" )) && (meeting.endTime < longCurrentDate) )
			noteFlag = true;
		System.out.println( noteFlag );
		Logs.addLog( Security.getConnected(), "view", "Meeting", meeting.id, meeting.project, new Date( System.currentTimeMillis() ) );
		render( meeting, attendees, noteFlag, id );
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
		if( Security.getConnected().in( currentProject ).can( "deleteMeeting" ) )
		{
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
					message = "unfortunately " + meeting.name + " meeting that you've been invited to is cancelled";
				Notifications.notifyUsers( users, "Meeting Canceled", message );
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
					message = "unfortunately " + meeting.name + " meeting notes are deleted :(";
				Notifications.notifyUsers( users, "Meeting Notes deleted", message );
			}
			Logs.addLog( Security.getConnected(), "delete", "Meeting", meeting.id, meeting.project, new Date( System.currentTimeMillis() ) );
			meeting.save();
			redirect( "/projects/" + meeting.project.id + "/meetings" );

		}
		else
			forbidden();

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
		if( Security.getConnected().in( currentMeeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( currentMeeting.creator ) )
		{
			User invitedUser = User.findById( userID );
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
			Notifications.notifyUsers( userList, header, body );
		}
		else
			forbidden();

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
		if( Security.getConnected().in( meeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( meeting.creator ) )
		{
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
						Notifications.notifyUsers( userList, header, body );
					}
				}
			}
			for( Component c : meeting.project.components )
			{
				meeting.components.add( c );
				meeting.save();
			}
		}
		else
			forbidden();
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

		if( Security.getConnected().in( meeting.project ).can( "manageMeetingAssociations" ) || Security.getConnected().equals( meeting.creator ) )
		{
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
							Notifications.notifyUsers( userList, header, body );
						}
					}
				}
				meeting.components.add( component );
				meeting.save();
			}
		}

		else
			forbidden();
	}

	/**
	 * Associating a Task to Meeting
	 * 
	 * @author hossam sharaf
	 * @param meetingID
	 *            , taskID
	 */
	@Check( "canAssociateTaskToMeeting" )
	public static void toggleTask( long meetingID, long taskID )
	{
		Meeting M = Meeting.findById( meetingID );
		Task T = Task.findById( taskID );
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
			Notifications.notifyUsers( users, " " + meeting.name + " Meeting Modification", message );
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
		Artifact n = new Artifact( "Notes", note );
		n.save();
		Meeting meeting = Meeting.findById( id );
		meeting.artifacts.add( n );
		meeting.save();

	}
}