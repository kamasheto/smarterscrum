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
	public static void index(long lastUpdate, String currentlyOnline) {
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
		CollaborateUpdate.delete("user = ? and timestamp < ?", user, lastUpdate);
		
		// fetch new updates
		List<CollaborateUpdate> updates = CollaborateUpdate.find("(user = ? or user is null) and timestamp >= ?", user, lastUpdate).fetch();
		for (CollaborateUpdate update : updates) {
			// remove the users on the fly
			// System.out.println(update);
			update.user = null;
		}
		response.updates = updates;
		
		/**
		 * Get online users
		 */
		Session.update();
		Session.delete("lastClick < ?", new Date().getTime() - 1000 * 60 * 60);
		List<Session> sessions = Session.find( "order by lastClick desc" ).fetch();

		List<User.Object> users = new ArrayList<User.Object>();
		for (Session session : sessions) {
			users.add(new User.Object(session.user.id, session.user.name, session.lastClick, session.user.isAdmin));
		}
		response.online_users = users;
		
		String[] idsStr = currentlyOnline.split(",");
		boolean newUsers = false;
		int realLength = 0;
		outerLoop : for (String idStr : idsStr) {
			if (idStr.length() < 1)
				continue;
			realLength++;
			long id = Long.parseLong(idStr);
			boolean contains = false;
			innerLoop : for (User.Object usrObj : users) {
				if (usrObj.id == id) {
					contains = true;
					break innerLoop;
				}
			}
			newUsers |= !contains;
			if (newUsers) {
				break outerLoop;
			}
		}
		
		newUsers |= realLength != users.size();
		
		/**
		 * Suspend 
		 */
		if (response.news.isEmpty() && response.updates.isEmpty() && !newUsers) {
			suspend("1s");
		}
		// System.out.println(response.updates);
		
		/**
		 * Finally render our response, with all details attached
		 */
		renderJSON(response);
	}
}

