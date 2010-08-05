package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import controllers.Application;

/**
 * a meeting attendance holds information about the attendance of a user in a
 * meeting information included in this entity include: User, Meeting, Status,
 * etc.
 */
@Entity
public class MeetingAttendance extends SmartModel {

	/**
	 * User involved
	 */
	@ManyToOne
	public User user;

	/**
	 * Meeting involved
	 */
	@ManyToOne
	public Meeting meeting;

	/**
	 * Status of this attendance, one of: waiting, confirmed, declined
	 */
	public String status;

	/**
	 * Deleted marker
	 * 
	 * @deprecated
	 */
	public boolean deleted;

	/**
	 * Only exists if reasons == declined
	 */
	public String reason;

	/**
	 * Meeting hash
	 */
	public String meetingHash;

	/**
	 * Default constructor
	 * 
	 * @param user
	 *            User of this attendance
	 * @param meeting
	 *            Meeting of this attedance
	 */
	public MeetingAttendance (User user, Meeting meeting) {
		this.user = user;
		this.meeting = meeting;
		this.status = "waiting";
		this.meetingHash = Application.randomHash();

	}

	/**
	 * Checks if the given meeting attendance is confirmed or not
	 * 
	 * @author Hossam Amer
	 * @return true if it is confirmed and false otherwise
	 */
	public boolean checkConfirmed() {
		return this.status.equals("confirmed");
	}
}
