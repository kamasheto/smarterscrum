package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.MeetingAttendance;
import models.Project;
import models.User;
import play.mvc.With;

@With( Secure.class )
public class MeetingAttendances extends CRUD
{

	/**
	 * this method takes meetingHash as a parameter and set the status of the
	 * corresponding meetingAttendance to confirmed, it also performs two
	 * checks; the first one,if the user confirm after the meeting is done, and
	 * the second check if the user has has already confirmed his attendance
	 * before and renders the message accordingly
	 * 
	 * @author Ghada Fakhry
	 * @return void
	 * @param meetingHash
	 */

	public static void confirm( String meetingHash )
	{
		MeetingAttendance attendance = MeetingAttendance.find( "byMeetingHash", meetingHash ).first();
		String status = attendance.status;
		Date currentDate = new Date();
		Date tempStart = new Date( attendance.meeting.startTime );
		boolean notYet = tempStart.after( currentDate );
		boolean setbefore = false;
		User user = Security.getConnected();
		boolean isUser = false;
		if( user.id == attendance.user.id )
			isUser = true;
		if( !status.equals( "waiting" ) )
		{
			setbefore = true;
			attendance.save();
			render( attendance, setbefore, notYet, isUser );
		}
		attendance.status = "confirmed";
		Logs.addLog( Security.getConnected(), "confirm", "Meeting invitation", attendance.id, attendance.meeting.project, new Date( System.currentTimeMillis() ) );
		attendance.save();
		render( attendance, setbefore, notYet, isUser );

	}

	/**
	 * this method takes meetingHash as a parameter and set the status of the
	 * corresponding meetingAttendance to declined, it also performs two checks;
	 * the first one,if the user declines after the meeting is done, and the
	 * second check if the user has has already declined his attendance before
	 * and renders the message accordingly
	 * 
	 * @author Ghada Fakhry
	 * @return void
	 * @param meetingHash
	 */

	public static void decline( String meetingHash )
	{
		MeetingAttendance attendance = MeetingAttendance.find( "byMeetingHash", meetingHash ).first();
		String status = attendance.status;
		Date currentDate = new Date();
		Date tempStart = new Date( attendance.meeting.startTime );
		boolean notYet = tempStart.after( currentDate );
		boolean setbefore = false;
		User user = Security.getConnected();
		boolean isUser = false;

		if( user.id == attendance.user.id )
			isUser = true;

		if( !status.equals( "waiting" ) )
		{
			setbefore = true;
			render( attendance, setbefore, notYet, isUser );
			return;
		}
		attendance.status = "declined";
		Logs.addLog( Security.getConnected(), "decline", "Meeting invitation", attendance.id, attendance.meeting.project, new Date( System.currentTimeMillis() ) );
		attendance.save();
		if( notYet )
		{

			boolean flag = true;
			List<MeetingAttendance> attendees = MeetingAttendance.find( "byMeeting.id", attendance.meeting.id ).fetch();
			while( attendees.isEmpty() == false )
			{
				MeetingAttendance temp = (MeetingAttendance) attendees.remove( 0 );
				String tempStatus = temp.status;
				if( tempStatus.equals( "confirmed" ) || tempStatus.equals( "waiting" ) )
					flag = false;
			}
			if( flag == true )
			{

				attendance.meeting.status = false;
				attendance.meeting.save();
				attendance.save();
				attendees = MeetingAttendance.find( "byMeeting.id", attendance.meeting.id ).fetch();
				List<User> users = new ArrayList<User>();
				while( attendees.isEmpty() == false )
				{
					users.add( attendees.remove( 0 ).user );
				}
				Notifications.notifyUsers( users, "Meeting Canceled", "All attendees declined invitations", (byte) -1 );
			}

			render( attendance, notYet, setbefore, isUser );

		}
		else
		{
			render( attendance, notYet, setbefore, isUser );
		}

	}

	/**
	 * this method takes a status and a meetingHash and gets the corresponding
	 * meetingAttendance and sets it's excuse to the input excuse
	 * 
	 * @author Ghada Fakhry
	 * @param meetingHash
	 * @param excuse
	 */

	public static void setExcuse( String meetingHash, String excuse )
	{
		MeetingAttendance attendance = MeetingAttendance.find( "byMeetingHash", meetingHash ).first();
		attendance.reason = excuse;
		attendance.save();

	}

	/**
	 * View invites method It takes a user id which is actually the current user
	 * on the system sent from the view and
	 * 
	 * @param projectID
	 * @param userID
	 */
	public static void viewInvites( long projectID )
	{
		long userID = Security.getConnected().id;
		User currentUser = User.findById( userID );
		Project currentProject = Project.findById( projectID );
		List<MeetingAttendance> allmeetings = MeetingAttendance.find( "byUser.idAndMeeting.project.idAndDeletedAndStatusLike", userID, projectID, false, "waiting" ).fetch();
		List<MeetingAttendance> meetings = new ArrayList<MeetingAttendance>();
		Date currentDate = new Date();
		for( MeetingAttendance meeting : allmeetings )
		{
			Date meetingDate = new Date( meeting.meeting.startTime );
			if( currentDate.before( meetingDate ) )
			{
				meetings.add( meeting );
			}
		}
		render( meetings, currentUser, currentProject );
	}

}
