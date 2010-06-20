package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author Moataz_Mekki
 */
@Entity
public class Notification extends SmartModel {

	@ManyToOne
	public User user;

	public String header;
	public String body;
	public long date;
	public byte importance;
	public boolean unread;
	public boolean madeBySysAdmin;

	/**
	 * the constructor which is responsible of saving the notifications in the
	 * DB
	 * 
	 * @param user
	 *            : the user that receives this notification
	 * @param header
	 *            : a brief description of the notification
	 * @param body
	 *            : the full description of the notification
	 * @param importance
	 *            : -1 for negative notifications like decline , cancel ... 0
	 *            for neutral notifications like reminders , invitations ... 1
	 *            for positive notifications like acceptance , success ...
	 */
	public Notification (User user, String header, String body, byte importance) {
		this.user = user;
		this.body = body;
		this.header = header;
		this.date = new Date().getTime();
		this.importance = importance;
		this.unread = true;
		if (user.isAdmin)
			this.madeBySysAdmin = true;
	}

}
