package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import notifiers.Notifications;

import models.Artifact;
import models.Component;
import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.Sprint;
import models.User;
import play.mvc.With;

@With(Secure.class)
public class ReviewLog extends SmartController {
	/**
	 * ReviewLog is the class for manipulating and viewing the review log which
	 * contains various information about the sprint review meetings as its'
	 * tasks and users.
	 * <p>
	 * Operations which gets the list of meetings are done by sending the list
	 * of meetings found in the database to the view by using the findById
	 * method.
	 * <p>
	 * Operations which retrieve the associated items of the selected meeting
	 * operate by getting the selected meeting's id from the view and then
	 * Retrieving the list of users and tasks of that meeting id.
	 * <p>
	 * Operations which edit the meetings and changing their review log status
	 * is done by allowing the user to input his data and reflect it back in the
	 * database.
	 * 
	 * @author Hossam Amer
	 * @version %I%, %G%
	 * @since 1.0
	 */

	/**
	 * changes done for sprint 3:
	 * 
	 * @author: menna.ghoneim
	 */

	/**
	 * S10, S11 Used for sending a list all the meetings of a certain project
	 * found in the database to be displayed by the view for any given scrum
	 * master
	 * <p>
	 * Displays all the meetings to any developer and sends them to the view
	 * using render().
	 * <p>
	 * Allows editing the description of notes' description plus the tasks'
	 * description
	 * 
	 * @param projectID
	 *            the id of a given project
	 *@param cid
	 *            the id of a given component
	 *@param sid
	 *            the id of a given sprint
	 */

	/**
	 * Sprint 3:
	 * 
	 * @author: menna.ghoneim This method renders PAST meetings to view their
	 *          review logs
	 */
	public static void showMeetings(long projectID, long cid, long sid) {
		boolean empty = false;
		boolean directLink = false;
		boolean sExist = (sid == 0) ? false : true;
		boolean cExist = (cid == 0) ? false : true;
		boolean pExist = (projectID == 0) ? false : true;

		Component c = Component.findById(cid);
		Project p = Project.findById(projectID);
		Sprint s = Sprint.findById(sid);

		Date currentDate = new Date();
		long longCurrentDate = currentDate.getTime();

		List<Meeting> reviewMeetings = Meeting.find("byProject.idAndDeleted",
				projectID, false).fetch();

		for (int i = 0; i < reviewMeetings.size(); i++) {
			if (reviewMeetings.get(i).endTime > longCurrentDate)
				reviewMeetings.remove(i);
		}

		if (reviewMeetings.isEmpty())
			empty = true;

		if (projectID == 0)
			directLink = true;

		User u = Security.getConnected();
		render(reviewMeetings, empty, directLink, projectID, cid, sid, c, p, s,
				sExist, cExist, pExist, u);
	}

	/**
	 * S10 Updates the note with the new description
	 * 
	 * @param id
	 *            the id of a given artifact of type note
	 * @param des
	 *            the new description of the note
	 * @param meetingID
	 *            the id of a given meeting
	 */

	public static boolean editNote(long id, String des) {
		try {
			Artifact note = Artifact.findById(id);
			note.description = des;
			note.save();

			List<Meeting> noteMeetings = note.meetingsArtifacts;

			User userWhoChanged = Security.getConnected();
			String header = "The Note " + id + " has been edited by "
					+ userWhoChanged.name;
			String body = "Note " + id + ":" + '\n' + "The new description is "
					+ note.description;

			/*
			 * Notifications.notifyUsers( User.find(//
			 * "from (User user inner join MeetingAttendance ma on ma.user = user) inner join (Meeting m inner join ma.meeting) where m = ? "
			 * , note.meetingsArtifacts.get( 0 ) ).<User> fetch(), header, body,
			 * (byte) 1 );
			 */

			for (Meeting m : noteMeetings) {
				Notifications.notifyUsers(getAttendanceConfirmed(m.id), header,
						body, (byte) 0);
			}
			Logs.addLog(userWhoChanged, body, "Note", id,
					note.meetingsArtifacts.get(0).project, new Date());
		}

		catch (Exception e) {
			render();
		}

		return true;

	}

	/**
	 * Gets the confirmed attendance of a given meeting
	 * 
	 * @param meetingID
	 *            the id of a given meeting
	 * @return a list of all users attended the given meeting
	 */

	private static List<User> getAttendanceConfirmed(long meetingID) {
		Meeting tmp = Meeting.findById(meetingID);
		List<User> out = new ArrayList<User>();

		for (MeetingAttendance mA : tmp.users)
			if (mA.status.equals("confirmed"))
				out.add(mA.user);

		return out;
	}

	/**
	 * S10 Gets the users from the meeting attendance list of a given meeting
	 * 
	 * @param tmp
	 *            a given meeting
	 */

	private static List<User> getUsersFromMeetingAttendaceInACertainMeeting(
			Meeting tmp) {
		List<User> out = new ArrayList<User>();
		List<MeetingAttendance> mA = tmp.users;

		for (int i = 0; i < mA.size(); i++)
			if (!mA.get(i).user.deleted && mA.get(i).status.equals("confirmed"))
				out.add(mA.get(i).user);

		return out;
	}
}
