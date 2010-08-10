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
	public static void index() {
		CollaborateResponse response = new CollaborateResponse();
		User user = Security.getConnected();
		
		/**
		 * Get latest news
		 */
		List<Notification> news = Notification.find("unread = true and receiver = ?", user).fetch();
		for (int i = 0; i < news.size(); i++) {
			news.get(i).unread = false;
			news.get(i).save();
			news.get(i).receiver = null;
			news.get(i).project = null;
			news.get(i).actionPerformer = null;
		}
		response.news = news;
		
		/**
		 * Get workspace changes
		 */
		
		
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

