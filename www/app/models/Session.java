package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import controllers.Security;

/**
 * Session model for keeping track of last sessions
 * 
 * @author mahmoudsakr
 */
@Entity
public class Session extends SmartModel {
	/**
	 * the session belong to the user
	 */
	@OneToOne
	public User user;

	/**
	 * timestamp of the last click in the session
	 */
	public long lastClick;

	/**
	 * Updates sessions by:
	 * <ul>
	 * <li>Deleting all old sessions (older than an hour)
	 * <li>updating this connected user's lastClick time
	 * </ul>
	 */
	public static void update() {
		Session.delete("lastClick < ?", new Date().getTime() - 1000 * 60 * 60);
		User user = Security.getConnected();
		Session session = Session.find("byUser", user).first();
		if (session == null) {
			session = new Session().save();
			session.user = user;
		}
		session.lastClick = new Date().getTime();
		session.save();
	}
}
