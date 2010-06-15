package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Session;
import models.User;
import play.mvc.With;

/**
 * Sessions controller
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class Sessions extends SmartController {
	/**
	 * pings the server, updates the session of the current user
	 */
	public static void ping() {
		Session.update();

		List<Session> sessions = Session.find("order by lastClick desc").fetch();

		List<User.Object> users = new ArrayList<User.Object>();
		for (Session session : sessions) {
			users.add(new User.Object(session.user.id, session.user.name, session.lastClick, session.user.isAdmin));
		}
		renderJSON(users);
	}

	/**
	 * Custom logout method to make sure session is removed from database before
	 * logging out
	 * 
	 * @throws Throwable
	 */
	public static void logout() throws Throwable {
		Session.delete("user = ?", Security.getConnected());
		Secure.logout();
	}
}
