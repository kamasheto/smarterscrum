package notifiers;

import play.mvc.*;
import java.util.*;
import models.Notification;
import models.Project;
import models.User;
import models.UserNotificationProfile;
import controllers.Security;

public class Notifications extends Mailer{

	/**
	 * sends an email to a user in order to activate his account or his new email address
	 * that he requested to change
	 * @param userEmail: the new email or the email he registered on
	 * @param userName: name of the user to activate
	 * @param url: the url that activates the account which contains the hash
	 * @param changeEmail: to differentiate between first activation on registration and changing his email address 
	 */
	public static void activate(String userEmail, String userName, String url, boolean changeEmail)
	{
		addRecipient(userEmail);
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("SmarterScrum Activation");
		send(userName, url, changeEmail);
	}
	
	/**
	 * sends an email when the user is invited to a project or ameeting
	 * @param user: the invied user
	 * @param objectURL: the Url of the meeting or project
	 * @param objectName: the name of the meeting (only meeting)
	 * @param confirmURL: the url that enables the user to confirm the invitation
	 * @param declineURL: the url that enables the user to decline the invitation
	 * @param project: the project of the meeting , or the project the user invited to
	 * @param meeting: to check if it's an invitation for a meeting or a project
	 */
	public static void invite(User user, String objectURL, String objectName, String confirmURL, String declineURL, Project project, boolean meeting)
	{
		User actionPerformer = Security.getConnected();
		if(meeting)
			{
				Notification n = new Notification(user, actionPerformer, "invited", objectURL, "you to the meeting", objectName, (byte)0).save();
				n.project=project;
				n.save();
			}
		else
			new Notification(user, actionPerformer, "Invited", objectURL, "you to the project", project.name, (byte)0).save();
		
		if (user.enableEmails) {
			addRecipient(user.email);
			setFrom("se.smartsoft.2@gmail.com");
			if (meeting)
				setSubject("Meeting Invitation");
			else
				setSubject("Project Invitation");
			String projectURL = Router.getFullUrl("Application.externalOpen")+"?id="+project.id+"&isOverlay=false&url=#";
			send(actionPerformer, user, meeting, objectURL, objectName,	confirmURL, declineURL, project, projectURL);
		}
	}
	
	/**
	 * sends an email to the user who forgot his password with a url to change it
	 * @param user: the user who forgot his password & will receive this email
	 * @param url: the url to change the password
	 */
	public static void lostPass(User user, String url)
	{
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("Password Recovery");
		addRecipient(user.email);
		send(user, url);
	}
	
	/**
	 * sends an email to welcome a new registered user or to confirm changing the email address of a user successfully
	 * @param user: the receiver of this email
	 * @param welcome: to check is it a welcome message or just a confirmations email
	 */
	public static void welcome(User user, boolean welcome)
	{
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("Welcome To SmarterScrum");
		addRecipient(user.email);
		send(user, welcome);
	}
	
	/**
	 * sends an email to tell the user that he's been deleted from the system
	 * @param user: the deleted user
	 * @param request: to check if the user requested to be deleted or he was deleted by a system admin
	 */
	public static void byeBye(User user, boolean request)
	{
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("Account Deleted");
		addRecipient(user.email);
		send(user, request);
	}
	
	/**
	 * sends notifications for specific actions to all the project members
	 * @param project: the project that its users will be notified
	 * @param actionType: the boolean variable in the projectNotificationProfile model to check if it's set to true or false
	 * @param resourceURL: the resource that the action was done upon
	 * @param resourceType: the type of that resource
	 * @param resourceName: the name of that resource
	 * @param importance: 1 for acceptance, 0 for announcements, -1 for deletion
	 */
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
		else if (actionType.equalsIgnoreCase("addReviewer"))
			actionType2= "been added";
		else if (actionType.equalsIgnoreCase("deleteReviewer"))
			actionType2= "been removed";
		else if (actionType.equalsIgnoreCase("addProductRole"))
			actionType2 = "added";
		
		if(project.notificationProfile.checkAction(actionType))
		{
			List<UserNotificationProfile> unps = project.userNotificationProfiles;
			for(int i =0 ; i<unps.size() ; i++)
			{
				if(unps.get(i).checkAction(actionType) && !(unps.get(i).user.equals(Security.getConnected())))
				{
					Notification n = new Notification(unps.get(i).user, user, actionType2, resourceURL, resourceType, resourceName, importance).save();
					n.project=project;
					n.save();
					if(unps.get(i).user.enableEmails)
						addRecipient(unps.get(i).user.email);
				}
			}
			if (unps.size() > 0) {				
				setFrom("se.smartsoft.2@gmail.com");
				setSubject("SmarterScrum Notification System");
				String projectURL = Router.getFullUrl("Application.externalOpen")+"?id="+project.id+"&isOverlay=false&url=#";
				send(user, project , actionType2, resourceURL, resourceType, resourceName, importance, projectURL);	
			}
		}
	}
	
	/**
	 * sends a notification to a list of users who are involved with the resource
	 * @param receivers: the users who will receive this notification
	 * @param actionType: the type of the action (add, delete, create, remove, edit ...)
	 * @param resourceURL: the resource that the action was done upon
	 * @param resourceType: the type of that resource
	 * @param resourceName: the name of that resource
	 * @param importance: 1 for acceptance, 0 for announcements, -1 for deletion
	 * @param project: the project that this action took place in
	 */
	public static void notifyUsers(List<User> receivers, String actionType, String resourceURL, String resourceType, String resourceName, byte importance, Project project)
	{		
		User user = Security.getConnected();
		String projectURL ="";
		for(int i=0 ; i<receivers.size(); i++)
		{			
			if (!receivers.get(i).equals(user)) {
				Notification nn = new Notification(receivers.get(i), user,
						actionType, resourceURL, resourceType, resourceName,
						importance).save();
				if (project != null) {
					nn.project = project;
					projectURL = Router.getFullUrl("Application.externalOpen")+"?id="+project.id+"&isOverlay=false&url=#";
					nn.save();
				}
				if (receivers.get(i).enableEmails)
					addRecipient(receivers.get(i).email);
			}
		}		
		setFrom("se.smartsoft.2@gmail.com");
		setSubject("SmarterScrum Notification System");
		send(user, actionType, resourceURL, resourceType, resourceName, importance, project, projectURL);
	}
	
	/**
	 * sends a notification to only one user
	 * @param receiver: the user who will receive this notification
	 * @param actionType: the type of the action (add, delete, create, remove, edit ...)
	 * @param resourceURL: the resource that the action was done upon
	 * @param resourceType: the type of that resource
	 * @param resourceName: the name of that resource
	 * @param importance: 1 for acceptance, 0 for announcements, -1 for deletion
	 * @param project: the project that this action took place in
	 */
	public static void notifyUser(User receiver, String actionType, String resourceURL, String resourceType, String resourceName, byte importance, Project project)
	{		
		User usr = Security.getConnected();
		String projectURL="";
		if(!usr.equals(receiver))
		{
			Notification notif = new Notification(receiver, usr, actionType, resourceURL, resourceType, resourceName, importance).save();
			if(project != null)
				{
					notif.project = project;
					projectURL = Router.getFullUrl("Application.externalOpen")+"?id="+project.id+"&isOverlay=false&url=#";
					notif.save();
				}				
			if(receiver.enableEmails)
			{
				setFrom("se.smartsoft.2@gmail.com");
				setSubject("SmarterScrum Notification System");
				addRecipient(receiver.email);
				send(notif, projectURL);
			}
		}
	}
	
	/**
	 * sends an email to the support team when a user gives a feedback on the system
	 * @param sender: the user giving the feedback
	 * @param subject: the summary of this feedback
	 * @param message: the detailed description of the feedback
	 * @param priority: the priority of the feedback specified by the user
	 */
	public static void feedbacks(User sender, String subject, String message, int priority)
	{
		String importance="";
		switch(priority)
		{
		case 0: importance="low";
		case 1: importance="Medium";
		case 2: importance="High";
		}
		setFrom("se.smartsoft.2@gmail.com");
		addRecipient("se.smartsoft.2@gmail.com");
		setSubject(Router.getFullUrl("Feedbacks.sendFeedback")+" , "+subject);
		send(sender, message, importance);
	}
	
}
