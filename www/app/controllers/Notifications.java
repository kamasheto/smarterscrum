package controllers;

import java.util.Date;
import java.util.List;

import models.Notification;
import models.Project;
import models.ProjectNotificationProfile;
import models.User;
import models.UserNotificationProfile;
import play.libs.Mail;
import play.mvc.Controller;

public class Notifications{

	/**
	 * This method performs the action of sending the notifications
	 * 
	 * @author Moataz_Mekki
	 * @param user
	 *            : the user that the notification will be sent to
	 * @param header
	 *            : the subject of the email
	 * @param body
	 *            : the content of the notification message
	 */
	public static void notifyUsers(User user, String header, String body, byte importance) {
		
		new Notification(user, header, body, importance).save();
		Mail.send("se.smartsoft@gmail.com", user.email, header, body);
	}

	/**
	 * @deprecated use notifyUsers(User, String, String, byte)
	 */
	public static void notifyUsers(User user, String header, String body) {
		notifyUsers(user, header, body, (byte) 1);
	}

	/**
	 * this method does the same as the previous one except that it sends the
	 * notifications to a list of users
	 * 
	 * @author Moataz_Mekki
	 * @param users
	 *            : the list of users that the notification will be sent to
	 * @param header
	 *            : the subject of the email
	 * @param body
	 *            : the content of the notification message This method performs
	 *            the action of sending
	 */

	public static void notifyUsers(List<User> users, String header, String body, byte importance) {
		
//		String[] a = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			notifyUsers(users.get(i), header, body, importance);
//			a[i] = users.get(i).email;
//			new Notification(users.get(i), header, body, importance).save();
		}
//		Mail.send("se.smartsoft@gmail.com", a, header, body);
	}

	/**
	 * @deprecated use notifyUsers(List<User>, String, String, byte)
	 */
	public static void notifyUsers(List<User> users, String header, String body) {
		notifyUsers(users, header, body, (byte) 1);
	}

	/**
	 * this method sends notification to all the project members who want these
	 * notifications to be sent to them if & only if the scrum master allows so
	 * 
	 * @author Moataz_Mekki
	 * @param project
	 *            : the project that the messages will be sent to its members
	 * @param header
	 *            : the subject of the email
	 * @param body
	 *            : the content of the notification message
	 * @param action_type
	 *            : the action that triggered the notification
	 */

	public static void notifyUsers(Project project, String header, String body, String action_type, byte importance) {
		
		ProjectNotificationProfile projPro = project.notificationProfile;
		if (projPro.checkAction(action_type)) {

			List<UserNotificationProfile> pros = project.userNotificationProfiles;
			for (int i = 0; i < pros.size(); i++) {
				if (pros.get(i).checkAction(action_type)) {
//					notifyUsers(users.get(i), header, body, importance);
					new Notification(pros.get(i).user, header, body, importance).save();
					Mail.send("se.smartsoft@gmail.com", pros.get(i).user.email, header, body);
				}
			}
		}
	}

	/**
	 * @deprecated use notifyUsers(Project, String, String String, byte)
	 */
	public static void notifyUsers(Project project, String header, String body, String action_type) {
		notifyUsers(project, header, body, action_type, (byte) 1);
	}

}
