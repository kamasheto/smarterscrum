package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import notifiers.Notifications;

import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.User;
import play.mvc.With;

@With (Secure.class)
public class MeetingAttendances extends SmartController {

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

	public static void confirm(String meetingHash) {
		MeetingAttendance attendance = MeetingAttendance.find("byMeetingHash", meetingHash).first();
		String status = attendance.status;
		Date currentDate = new Date();
		Date tempStart = new Date(attendance.meeting.endTime);
		boolean notYet = tempStart.after(currentDate);
		boolean setbefore = false;
		User user = Security.getConnected();
		boolean isUser = false;
		if (user.id == attendance.user.id)
			isUser = true;
		if (!status.equals("waiting")) {
			setbefore = true;
			attendance.save();
			render(attendance, setbefore, notYet, isUser);
		}
		attendance.status = "confirmed";
		Logs.addLog(Security.getConnected(), "confirm", "Meeting invitation", attendance.id, attendance.meeting.project, new Date(System.currentTimeMillis()));
		attendance.save();
		render(attendance, setbefore, notYet, isUser);

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

	public static void decline(String meetingHash) {
		MeetingAttendance attendance = MeetingAttendance.find("byMeetingHash", meetingHash).first();
		String status = attendance.status;
		Date currentDate = new Date();
		Date tempStart = new Date(attendance.meeting.endTime);
		boolean notYet = tempStart.after(currentDate);
		boolean setbefore = false;
		User user = Security.getConnected();
		boolean isUser = false;

		if (user.id == attendance.user.id)
			isUser = true;

		if (!status.equals("waiting")) {
			setbefore = true;
			render(attendance, setbefore, notYet, isUser);
			return;
		}
		attendance.status = "declined";
		Logs.addLog(Security.getConnected(), "decline", "Meeting invitation", attendance.id, attendance.meeting.project, new Date(System.currentTimeMillis()));
		attendance.save();
		if (notYet) {

			boolean flag = true;
			List<MeetingAttendance> attendees = MeetingAttendance.find("byMeeting.id", attendance.meeting.id).fetch();
			while (attendees.isEmpty() == false) {
				MeetingAttendance temp = (MeetingAttendance) attendees.remove(0);
				String tempStatus = temp.status;
				if (tempStatus.equals("confirmed") || tempStatus.equals("waiting"))
					flag = false;
			}
			if (flag == true) {

				attendance.meeting.status = false;
				attendance.meeting.save();
				attendance.save();
				attendees = MeetingAttendance.find("byMeeting.id", attendance.meeting.id).fetch();
				List<User> users = new ArrayList<User>();
				while (attendees.isEmpty() == false) {
					users.add(attendees.remove(0).user);
				}
				Notifications.notifyUsers(users, "Meeting Canceled", "All attendees declined invitations", (byte) -1);
			}

			render(attendance, notYet, setbefore, isUser);

		} else {
			render(attendance, notYet, setbefore, isUser);
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

	public static void setExcuse(String meetingHash, String excuse) {
		MeetingAttendance attendance = MeetingAttendance.find("byMeetingHash", meetingHash).first();
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
	public static void viewInvites(long projectID) {
		// long userID = Security.getConnected().id;
		User currentUser = Security.getConnected(); // User.findById(userID);
		Project currentProject = Project.findById(projectID);
		List<MeetingAttendance> allmeetings = MeetingAttendance.find("byUserAndMeeting.project.idAndDeletedAndStatusLike", currentUser, projectID, false, "waiting").fetch();
		List<MeetingAttendance> meetings = new ArrayList<MeetingAttendance>();
		Date currentDate = new Date();
		for (MeetingAttendance meeting : allmeetings) {
			Date meetingDate = new Date(meeting.meeting.endTime);
			if (currentDate.before(meetingDate)) {
				meetings.add(meeting);
			}
		}
		render(meetings, currentUser, currentProject);
	}

	/**
	 * Set attendance page that Renders to the user all the meetingAttendances
	 * associated to that meeting in order to change in the attendance of the
	 * meeting attendees
	 * 
	 * @param meetingID
	 */
	public static void setAttendance(long meetingID) {
		Meeting meeting = Meeting.findById(meetingID);
		Security.check(Security.getConnected().in(meeting.project).can("setMeetingAttendance") || Security.getConnected().equals(meeting.creator));
		List<MeetingAttendance> attendances = MeetingAttendance.find("byMeeting.idAndDeleted", meetingID, false).fetch();
		render(attendances, meeting);
	}

	/**
	 * The method setConfirmed is called from the setAttendance page in order to
	 * change the status of the user in the given meeting to attnded and send
	 * him an email to notify the user
	 * 
	 * @param id
	 *            which is the MeetingAttendance id
	 */

	public static void setConfirmed(long id) {
		MeetingAttendance ma = MeetingAttendance.findById(id);
		Security.check(Security.getConnected().in(ma.meeting.project).can("setMeetingAttendance"));
		ma.status = "confirmed";
		ma.reason = "";
		ma.save();
		String header = "Attendance to " + ma.meeting.name;
		String body = "Dear " + ma.user.getDisplayName(ma.meeting.project) + "\n";
		String body2 = "Your attendance to " + ma.meeting.name + " was confirmed.";
		Notifications.notifyUsers(ma.user, header, body + body2, (byte) 1);
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

	public static void setDeclined(long id, String reason) {
		MeetingAttendance ma = MeetingAttendance.findById(id);
		Security.check(Security.getConnected().in(ma.meeting.project).can("setMeetingAttendance"));
		ma.status = "declined";
		ma.reason = reason;
		ma.save();
		String header = "Attendance to " + ma.meeting.name;
		String body = "Dear " + ma.user.getDisplayName(ma.meeting.project) + "\n";
		String body2 = "Your attendance to " + ma.meeting.name + " was changed to NOT attended.";
		Notifications.notifyUsers(ma.user, header, body + body2, (byte) 1);
	}
	/**
	 * this method takes the meeting Id and change the status of the connected user in this meeting to attended
	 * @param meetingId
	 */
	public static void confirmAttendance(long meetingId) {
		Meeting m = Meeting.findById(meetingId);
		MeetingAttendance ma= MeetingAttendance.find("byMeetingAndUserAndDeleted",m,Security.getConnected(),false).first();
		ma.status = "confirmed";
		ma.reason = "";
		ma.save();
		String header = "Attendance to " + ma.meeting.name;
		String body = "Dear " + ma.user.getDisplayName(ma.meeting.project) + "\n";
		String body2 = "Your attendance to " + ma.meeting.name + " was confirmed.";
		Notifications.notifyUsers(ma.user, header, body + body2, (byte) 1);
	}

	/**
	 * this method takes the meeting Id and change the status of the connected user in this meeting to not attended
	 * @param meetingId
	 */
	public static void declineAttendance(long meetingId,String reason) {
		Meeting m = Meeting.findById(meetingId);
		MeetingAttendance ma= MeetingAttendance.find("byMeetingAndUserAndDeleted",m,Security.getConnected(),false).first();
		ma.status = "declined";
		ma.reason = reason;
		ma.save();
		String header = "Attendance to " + ma.meeting.name;
		String body = "Dear " + ma.user.getDisplayName(ma.meeting.project) + "\n";
		String body2 = "You Declined the attendance to "+ma.meeting.name+".";
		Notifications.notifyUsers(ma.user, header, body + body2, (byte) 1);
	}
}
