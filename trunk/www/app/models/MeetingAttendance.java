package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;
import controllers.Application;

@Entity
public class MeetingAttendance extends Model {

	// Relation with user model
	@ManyToOne
	public User user;

	// Relation with meeting model
	@ManyToOne
	public Meeting meeting;

	// Meeting status has to be "waiting" or "confirmed" or "declined"
	public String status;

	// is Deleted?
	public boolean deleted;

	// IF status ="decline" then reason is why
	public String reason;

	// Meeting hash
	public String meetingHash;

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
		return this.status.equals("confirmed") ? true : false;
	}
}
