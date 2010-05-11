package controllers;

import java.util.List;

import models.Notification;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

@With (Secure.class)
public class NotificationTasks extends Controller {

	public static void getLatestNews() {
		User myUser = Security.getConnected();
		List<Notification> news = Notification.find("unread = true and user = ?", myUser).fetch();
		for (int i = 0; i < news.size(); i++) {
			news.get(i).unread = false;
			news.get(i).save();
			news.get(i).user = null;
		}

		renderJSON(news);
	}
}
