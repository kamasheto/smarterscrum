package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import others.*;

/**
 * Controller for all collaborate actions
 */
@With(Secure.class)
public class Collaborate extends SmartController {

	/**
	 * Renders a JSON object of all changes performed for this user (for dynamic udpate)
	 */
	public static void index(long lastUpdate) {
		CollaborateResponse response = new CollaborateResponse();
		User user = Security.getConnected();
		
		/**
		 * Get latest news
		 */
		List<Notification> news = Notification.find("unread = true and receiver = ?", user).fetch();
		for (Notification noti : news) {
			noti.unread = false;
			noti.save();
			
			noti.receiver = null;
			noti.project = null;
			noti.actionPerformer = null;
		}
		response.news = news;
		
		/**
		 * Get workspace changes
		 */
		// first delete previous updates
		Update.delete("user = ? and timestamp < ?", user, lastUpdate);
		
		// fetch new updates
		List<Update> updates = Update.find("(user = ? or user is null) and timestamp >= ?", user, lastUpdate).fetch();
		for (Update update : updates) {
			// remove the users on the fly
			update.user = null;
		}
		response.updates = updates;
		
		/**
		 * Get online users
		 */
		Session.update();

		List<Session> sessions = Session.find( "order by lastClick desc" ).fetch();

		List<User.Object> users = new ArrayList<User.Object>();
		for( Session session : sessions )
		{
			users.add( new User.Object( session.user.id, session.user.name, session.lastClick, session.user.isAdmin ) );
		}
		response.online_users = users;
		
		/**
		 * Finally render our response, with all details attached
		 */
		renderJSON(response);
	}
}

