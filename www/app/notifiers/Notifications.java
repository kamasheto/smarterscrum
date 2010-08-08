package notifiers;

import play.mvc.*;
import java.util.*;
import models.Notification;
import models.Project;
import models.User;
import models.UserNotificationProfile;
import controllers.Security;

public class Notifications extends Mailer{

	public static void activate(String userEmail, String userName, String url, boolean changeEmail)
	{
		addRecipient(userEmail);
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("SmarterScrum Activation");
		send(userName, url, changeEmail);
	}
	
	public static void invite(User user, String objectURL, String objectName, String confirmURL, String declineURL, Project project, boolean meeting)
	{
		User actionPerformer = Security.getConnected();
		new Notification(user, actionPerformer, "Invit", objectURL, "the meeting", objectName, (byte)0).save();
		addRecipient(user.email);
		setFrom("se.smartsoft.2@gmail.com");
		if(meeting)
			setSubject("Meeting Invitation");
		else
			setSubject("Project Invitation");
		send(actionPerformer, user, meeting, objectURL, objectName, confirmURL, declineURL, project);
	}
	
	public static void lostPass()
	{
		
	}
	
	public static void welcome()
	{
		
	}
	
	public static void byeBye()
	{
		
	}
	
	public static void notifyProjectUsers(Project project , String actionType, String resourceURL, String resourceType, String resourceName, byte importance)
	{
		if(project.notificationProfile.checkAction(actionType))
		{
			List<UserNotificationProfile> unps = project.userNotificationProfiles;
			for(int i =0 ; i<unps.size() ; i++)
			{
				if(unps.get(i).checkAction(actionType))
				{
					new Notification(unps.get(i).user, Security.getConnected(), actionType, resourceURL, resourceType, resourceName, importance).save();
					if(unps.get(i).user.enableEmails)
						addRecipient(unps.get(i).user.email);
				}
			}
			if (unps.size() > 0) {				
				setFrom("se.smartsoft.2@gmail.com");
				setSubject("SmarterScrum Notification System");
				send(project , actionType, resourceURL, resourceType, resourceName, importance);	
			}
		}
	}
	
	public static void notifyUsers(List<User> receivers, String actionType, String resourceURL, String resourceType, String resourceName, byte importance, Project project)
	{		
		User user = Security.getConnected();
		for(int i=0 ; i<receivers.size(); i++)
		{			
			Notification nn = new Notification(receivers.get(i), user, actionType, resourceURL, resourceType, resourceName, importance).save();
			if(project != null)
				{
					nn.project = project;
					nn.save();
				}
			if(receivers.get(i).enableEmails)
				addRecipient(receivers.get(i).email);
		}		
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("SmarterScrum Notification System");
		send(actionType, resourceURL, resourceType, resourceName, importance, project);
	}
	
	public static void notifyUser(User receiver, String actionType, String resourceURL, String resourceType, String resourceName, byte importance, Project project)
	{		
		User usr = Security.getConnected();
		if(!usr.equals(receiver))
		{
			Notification notif = new Notification(receiver, usr, actionType, resourceURL, resourceType, resourceName, importance).save();
			if(project != null)
				{
					notif.project = project;
					notif.save();
				}				
			if(receiver.enableEmails)
			{
				setFrom("se.smartsoft.2@gmail.com");
				setSubject("SmarterScrum Notification System");
				addRecipient(receiver.email);
				send(notif);
			}
		}
	}
	
}
