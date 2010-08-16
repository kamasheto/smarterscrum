package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Log;
import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.Update;
import models.User;
import notifiers.Notifications;
import play.mvc.Router;
import play.mvc.With;

@With( Secure.class )
public class MeetingAttendances extends SmartController
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
		MeetingAttendance attendance = MeetingAttendance.find( "byMeetingHashAndDeleted", meetingHash, false ).first();
		if( attendance == null )
			notFound();
		if( !attendance.user.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		if( attendance.status.equals( "waiting" ) )
		{
			if( attendance.meeting.endTime > new Date().getTime() )
			{
				attendance.status = "confirmed";
				attendance.reason = "";
				attendance.save();
				flash.success( "You accepted the invitation" );
				Log.addUserLog( "Confirmed meeting invitation", attendance, attendance.meeting, attendance.meeting.project );
				Update.update( attendance.meeting.project.users, Security.getConnected(), "reload('meeting-" + attendance.meeting.id + "')" );
			}
			else
			{
				flash.error( "Meeting has already ended." );
			}
		}
		else
		{
			flash.error( "You have already cofirmed/declined this invitation" );
		}

		String meetingURL = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + attendance.meeting.project.id + "&isOverlay=false&url=/meetings/viewMeeting?id=" + attendance.meeting.id;
		redirect( meetingURL );
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
		if( attendance == null )
			notFound();
		String meetingURL = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + attendance.meeting.project.id + "&isOverlay=false&url=/meetings/viewMeeting?id=" + attendance.meeting.id;
		if( !attendance.user.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		if( attendance.status.equals( "waiting" ) )
		{
			if( attendance.meeting.endTime > new Date().getTime() )
			{
				String url = "/meetings/viewMeeting?id=" + attendance.meeting.id;
				long pId = attendance.meeting.project.id;
				long mId = attendance.meeting.id;
				render( url, pId, mId );
			}
			else
			{
				flash.error( "Meeting has already ended." );
				redirect( meetingURL );
			}
		}
		else
		{
			flash.error( "You have already confirmed/declined this invitation" );
			redirect( meetingURL );
		}

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
		// long userID = Security.getConnected().id;
		User currentUser = Security.getConnected(); // User.findById(userID);
		Project currentProject = Project.findById( projectID );
		if( currentProject.deleted )
			notFound();
		List<MeetingAttendance> allmeetings = MeetingAttendance.find( "byUserAndMeeting.project.idAndDeletedAndStatusLike", currentUser, projectID, false, "waiting" ).fetch();
		List<MeetingAttendance> meetings = new ArrayList<MeetingAttendance>();
		Date currentDate = new Date();
		for( MeetingAttendance meeting : allmeetings )
		{
			Date meetingDate = new Date( meeting.meeting.endTime );
			if( currentDate.before( meetingDate ) )
			{
				meetings.add( meeting );
			}
		}
		render( meetings, currentUser, currentProject );
	}

	/**
	 * Set attendance page that Renders to the user all the meetingAttendances
	 * associated to that meeting in order to change in the attendance of the
	 * meeting attendees
	 * 
	 * @param meetingID
	 */
	public static void setAttendance( long meetingID )
	{
		Meeting meeting = Meeting.findById( meetingID );
		if( meeting.deleted )
			notFound();
		Security.check( Security.getConnected().in( meeting.project ).can( "setMeetingAttendance" ) || Security.getConnected().equals( meeting.creator ) );
		List<MeetingAttendance> attendances = MeetingAttendance.find( "byMeeting.idAndDeleted", meetingID, false ).fetch();
		render( attendances, meeting );
	}

	/**
	 * The method setConfirmed is called from the setAttendance page in order to
	 * change the status of the user in the given meeting to attnded and send
	 * him an email to notify the user
	 * 
	 * @param id
	 *            which is the MeetingAttendance id
	 */

	public static void setConfirmed( long id )
	{
		MeetingAttendance ma = MeetingAttendance.findById( id );
		Security.check( Security.getConnected().in( ma.meeting.project ).can( "setMeetingAttendance" ) );
		ma.status = "confirmed";
		ma.reason = "";
		ma.save();
		Update.update( ma.meeting.project, "reload('meetingAttendees-" + ma.meeting.id + "')" );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + ma.meeting.project.id + "&isOverlay=false&url=/meetings/viewAttendeeStatus?id=" + ma.meeting.project.id;
		Notifications.notifyUser( ma.user, "Confirm", url, "Meeting Attendence", ma.meeting.name, (byte) 1, ma.meeting.project );
		Log.addLog( "Changed meeting attendance to attended", Security.getConnected(), ma.user, ma.meeting, ma.meeting.project );
	}

	/**
	 * The method setDeclined is called from the setAttendance page in order to
	 * change the status of the user in the given meeting to did not attend and
	 * send him an email to notify the user
	 * 
	 * @param id
	 *            which is the MeetingAttendance id
	 * @param reason
	 *            which is the reason of decline.
	 */

	public static void setDeclined( long id, String reason )
	{
		MeetingAttendance ma = MeetingAttendance.findById( id );
		Security.check( Security.getConnected().in( ma.meeting.project ).can( "setMeetingAttendance" ) );
		ma.status = "declined";
		ma.reason = reason;
		ma.save();
		Update.update( ma.meeting.project, "reload('meetingAttendees-" + ma.meeting.id + "')" );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + ma.meeting.project.id + "&isOverlay=false&url=/meetings/viewAttendeeStatus?id=" + ma.id;
		Notifications.notifyUser( ma.user, "declin", url, "Meeting Attendence", ma.meeting.name, (byte) -1, ma.meeting.project );
		Log.addLog( "Changed meeting attendance to did not attend", Security.getConnected(), ma.user, ma.meeting, ma.meeting.project );
	}

	/**
	 * this method takes the meeting Id and change the status of the connected
	 * user in this meeting to attended
	 * 
	 * @param meetingId
	 */
	public static void confirmAttendance( long meetingId )
	{
		Meeting m = Meeting.findById( meetingId );
		if( m.endTime > new Date().getTime() )
		{
			MeetingAttendance ma = MeetingAttendance.find( "byMeetingAndUserAndDeleted", m, Security.getConnected(), false ).first();
			ma.status = "confirmed";
			ma.reason = "";
			ma.save();
			Log.addUserLog( "Confirmed meeting invitation", ma.meeting, ma.meeting.project );
			Update.update( m.project.users, Security.getConnected(), "reload('meeting-" + m.id + "')" );
			renderJSON( true );
		}
		renderJSON( false );
	}

	/**
	 * this method takes the meeting Id and change the status of the connected
	 * user in this meeting to not attended
	 * 
	 * @param meetingId
	 */
	public static void declineAttendance( long meetingId, String reason )
	{
		Meeting m = Meeting.findById( meetingId );
		if( m.endTime > new Date().getTime() )
		{
			MeetingAttendance ma = MeetingAttendance.find( "byMeetingAndUserAndDeleted", m, Security.getConnected(), false ).first();
			ma.status = "declined";
			ma.reason = reason;
			ma.save();
			Log.addUserLog( "Declined meeting invitation", ma.meeting, ma.meeting.project );
			Update.update( m.project.users, Security.getConnected(), "reload('meeting-" + m.id + "')" );
			renderJSON( true );
		}
		renderJSON( false );
	}
}
