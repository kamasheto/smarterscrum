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
		if(meeting)
			new Notification(user, actionPerformer, "invited", objectURL, "you to the meeting", objectName, (byte)0).save();
		else
			new Notification(user, actionPerformer, "Invited", objectURL, "you to the project", project.name, (byte)0).save();
		addRecipient(user.email);
		setFrom("se.smartsoft.2@gmail.com");
		if(meeting)
			setSubject("Meeting Invitation");
		else
			setSubject("Project Invitation");
		send(actionPerformer, user, meeting, objectURL, objectName, confirmURL, declineURL, project);
	}
	
	public static void lostPass(User user, String url)
	{
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("Password Recovery");
		addRecipient(user.email);
		send(user, url);
	}
	
	public static void welcome(User user, boolean welcome)
	{
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("Welcome To SmarterScrum");
		addRecipient(user.email);
		send(user, welcome);
	}
	
	public static void byeBye(User user, boolean request)
	{
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("Account Deleted");
		addRecipient(user.email);
		send(user, request);
	}
	
	public static void notifyProjectUsers(Project project , String actionType, String resourceURL, String resourceType, String resourceName, byte importance)
	{
		User user = Security.getConnected();
		String actionType2="";
		if (actionType.equalsIgnoreCase("setSprint"))
			actionType2= "created";
		else if (actionType.equalsIgnoreCase("addRole"))
			actionType2= "added";		
		else if (actionType.equalsIgnoreCase("reportImpediment"))
			actionType2= "reported";
		else if (actionType.equalsIgnoreCase("onCreateComponent"))
			actionType2= "created";
		else if (actionType.equalsIgnoreCase("onEditComponent"))
			actionType2= "edited";
		else if (actionType.equalsIgnoreCase("onDeleteComponent"))
			actionType2= "deleted";
		else if (actionType.equalsIgnoreCase("addColumn"))
			actionType2= "added";
		else if (actionType.equalsIgnoreCase("deleteColumn"))
			actionType2= "deleted";
		else if (actionType.equalsIgnoreCase("assignStoryToSprint"))
			actionType2= "assigned";
		else if (actionType.equalsIgnoreCase("addTaskStatus"))
			actionType2= "added";	
		else if (actionType.equalsIgnoreCase("editTaskStatus"))
			actionType2= "edited";	
		else if (actionType.equalsIgnoreCase("deleteTaskStatus"))
			actionType2= "deleted";
		else if (actionType.equalsIgnoreCase("deleteProject"))
			actionType2= "deleted";
		else if (actionType.equalsIgnoreCase("deletedFromProject"))
			actionType2= "deleted";	
		else if (actionType.equalsIgnoreCase("editTaskType"))
			actionType2= "edited";
		
		
		if(project.notificationProfile.checkAction(actionType))
		{
			List<UserNotificationProfile> unps = project.userNotificationProfiles;
			for(int i =0 ; i<unps.size() ; i++)
			{
				if(unps.get(i).checkAction(actionType) && !(unps.get(i).user.equals(Security.getConnected())))
				{
					new Notification(unps.get(i).user, user, actionType2, resourceURL, resourceType, resourceName, importance).save();
					if(unps.get(i).user.enableEmails)
						addRecipient(unps.get(i).user.email);
				}
			}
			if (unps.size() > 0) {				
				setFrom("se.smartsoft.2@gmail.com");
				setSubject("SmarterScrum Notification System");
				send(user, project , actionType2, resourceURL, resourceType, resourceName, importance);	
			}
		}
	}
	
	public static void notifyUsers(List<User> receivers, String actionType, String resourceURL, String resourceType, String resourceName, byte importance, Project project)
	{		
		User user = Security.getConnected();
		for(int i=0 ; i<receivers.size(); i++)
		{			
			if (!receivers.get(i).equals(user)) {
				Notification nn = new Notification(receivers.get(i), user,
						actionType, resourceURL, resourceType, resourceName,
						importance).save();
				if (project != null) {
					nn.project = project;
					nn.save();
				}
				if (receivers.get(i).enableEmails)
					addRecipient(receivers.get(i).email);
			}
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
