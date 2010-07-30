package notifiers;

import play.*;
import play.libs.Mail;
import play.mvc.*;
import java.util.*;

import net.sf.cglib.transform.impl.AddStaticInitTransformer;

import models.Notification;
import models.Project;
import models.ProjectNotificationProfile;
import models.User;
import models.UserNotificationProfile;
import controllers.Security;

public class Notifications extends Mailer{
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

		//Notification notif = new Notification(user, header, body, importance).save();
		setSubject("SmarterScrum Notification System");
		addRecipient(user.email);
		setFrom("moataz.mekki@gmail.com");
		send();
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
		for (int i = 0; i < users.size(); i++){
			//notifyUsers(users.get(i), header, body, importance);
			//Notification notif = new Notification(user, header, body, importance).save();
			addRecipient(users.get(i).email);}
		setSubject("SmarterScrum Notification System");	
		setFrom("moataz.mekki@gmail.com");
			send();
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

	public static void notifyProjectUsers(Project project, String header, String body, String action_type, byte importance) {

		ProjectNotificationProfile projPro = project.notificationProfile;
		if (projPro.checkAction(action_type)) {

			List<UserNotificationProfile> pros = project.userNotificationProfiles;
			for (int i = 0; i < pros.size(); i++) {
				if (pros.get(i).checkAction(action_type)) {					
					Notification notif = new Notification(pros.get(i).user, header, body, importance).save();
					setSubject("SmarterScrum Notification System");
					setFrom("moataz.mekki@gmail.com");
					addRecipient(notif.user.email);
					send(notif);
				}
			}
		}
	}

	/*public static void notifyProjectUsers(Project project , String actionType, String resourceURL, String resourceType, String resourceName, byte importance)
	{
		
	}
	
	public static void notifyUsers(User receiver, String actionType, String resourceURL, String resourceType, String resourceName, byte importance)
	{		
		User usr = Security.getConnected();
		Notification n = new Notification(receiver, usr, actionType, resourceURL, resourceType, resourceName, importance).save();		
		
		setSubject("SmarterScrum Notification System");
		addRecipient(receiver.email);		
		
		send(usr, actionType, resourceURL, resourceName);
	}
	
	public static boolean notifiable(Project p , User u, String actionType)
	{
		if(p.notificationProfile.checkAction(actionType))
			{
				UserNotificationProfile unp = UserNotificationProfile.find("byUserAndProject", u, p).first();
				if(unp.checkAction(actionType))
					return true;
			}		
		return false;
	}*/

}
