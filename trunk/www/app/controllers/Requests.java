package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Component;
import models.Project;
import models.Request;
import models.Reviewer;
import models.Role;
import models.TaskType;
import models.Update;
import models.User;
import models.UserNotificationProfile;
import models.Log;
import notifiers.Notifications;
import play.mvc.Router;
import play.mvc.With;

@With( Secure.class )
public class Requests extends SmartCRUD
{
	
	/**
	 * This method fetches and renders all deletion requests corresponding to a
	 * project with id "id" .
	 * 
	 * @author Amr Tj.Wallas
	 * @param id
	 *            The id of The project whose corresponding deletion requests
	 *            are to be fetched.
	 * @throws Throwable
	 * @see models.Request
	 * @see views/Requests/deletionRequestRespond.html
	 * 
	 * 
	 */
	
	public static void requestRespond( long id )
	{
		Project pro = Project.findById( id );
		Security.check( pro, "manageRequests" );
		List<Request> requests = Request.find( "isDeletion = false and project = " + pro.id + " order by id desc" ).fetch();
		List<Request> drequests = Request.find( "isDeletion = true and project = " + pro.id + " order by id desc" ).fetch();
		List<Reviewer> revrequests = Reviewer.find("accepted = false and project = "+pro.id + " order by id desc" ).fetch();
		render( requests, drequests, pro, revrequests);
	}

	/**
	 * 
	 * this method performs the action of accepting a request once the request
	 * is accepted it adds the user to the list of roles and the role to that
	 * user
	 * 
	 * @author Moataz_Mekki
	 * @param hash
	 *            : to find the request that was accepted
	 */
	public static void requestAccept( String hash )
	{
		Request x = Request.find( "byHash", hash ).first();
		notFoundIfNull(x);
		Project y = x.project;
		x.user.addRole( x.role );
		String url = Router.getFullUrl("Application.externalOpen")+"?id="+x.project.id+"&isOverlay=false&url=/users/listUserProjects?userId="+x.user.id+"&x=2&projectId="+x.project.id+"&currentProjectId="+x.project.id;		
		Notifications.notifyUser( x.user, "accepted", url, "your Role Request", x.role.name, (byte) 1 , x.project);
		// User myUser = Security.getConnected();
		// Logs.addLog( myUser, "RequestAccept", "Request", x.id, y, new Date() );
		Log.addUserLog("Role request accepted", x.user, x.role, x.role.project);
		
		Update.update(x.user, "reload('roles')");
		Update.update(Security.getConnected(), "reload('project-requests')");
		x.delete();
	}

	/**
	 * This method performs the deletion request accept action when a User who
	 * has authority to manage project deletion requests decides to accept a
	 * request by clicking the accept link beside it. On invocation of this
	 * method, this user's role corresponding to that project won't include him
	 * anymore. <b>(Role entity stays in the database for other users
	 * corresponding to it)</b> Moreover, this user will be removed from the
	 * list of users in that project and all components in the project too.
	 * 
	 * @author Amr Tj.Wallas
	 * @param hash
	 *            The hash value of that deletion request.
	 * @throws Throwable
	 * @see models.Request
	 * @see views/Requests/deletionRequestRespond.html
	 *
	 */
	public static void deletionRequestAccept( String hash ) throws Throwable
	{
		if( hash == null )
			Secure.login();
		Request currentRequest = Request.find( "byHash", hash ).first();
		Security.check( currentRequest.project, "manageRequests" );
		if( currentRequest == null )
			Secure.login();
		UserNotificationProfile currentProfile = UserNotificationProfile.find( "user =  " + currentRequest.user.id + " and project = " + currentRequest.project.id ).first();
		if( currentProfile != null )
			currentProfile.delete();
		List<Role> projectRoles = Role.find( "project", currentRequest.project ).fetch();

		for( int i = 0; i < projectRoles.size(); i++ )
		{
			if( currentRequest.user.roles.contains( projectRoles.get( i ) ) )
			{
				currentRequest.user.roles.remove( projectRoles.get( i ) );
				currentRequest.user.save();
			}
		}

		currentRequest.user.projects.remove( currentRequest.project );
		currentRequest.user.save();
		if( !currentRequest.user.components.isEmpty() )
		{
			List<Component> currentComponents = Component.find( "project", currentRequest.project ).fetch();
			for( int i = 0; i < currentComponents.size(); i++ )
			{
				currentRequest.user.components.remove( currentComponents.get( i ) );
				currentRequest.user.save();
			}
		}
		String url = Router.getFullUrl("Application.externalOpen")+"?id="+currentRequest.project.id+"&isOverlay=false&url=#";
		Notifications.notifyUser( currentRequest.user, "accepted", url, "your Request to be deleted from project", currentRequest.project.name, (byte) 1 , null);		
		Log.addLog("Deletion request accepted", currentRequest.project, currentRequest.user);
		// Logs.addLog( Security.getConnected(), "DeletionRequestAccept", "Request", currentRequest.id, currentRequest.project, new Date() );
		
		
		Update.update(Security.getConnected(), "reload('project-requests')");
		// Update.update(Security.getConnected(), ""); // run javascript at client to close workspace?
		currentRequest.delete();
	}

	/**
	 * 
	 * <p>
	 * this method performs the action of ignoring a request once the request is
	 * ignored it will be deleted from the DB
	 * 
	 * @author Moataz_Mekki
	 * @author Amr Tj.Wallas
	 * @param hash
	 *            : to find the request that was ignored
	 * @param body
	 *            The body of the notification message that will be sent.
	 * @throws Throwable
	 * @see models.Request
	 * 
	 */
	public static void requestIgnore( String hash, String body ) throws Throwable
	{
		if( hash == null )
			Secure.login();
		Request x = Request.find( "byHash", hash ).first();
		Security.check( x.project, "manageRequests" );
		if( x == null )
			Secure.login();
		Project y = x.project;
		if( !x.isDeletion )
		{
			String url = Router.getFullUrl("Application.externalOpen")+"?id="+x.project.id+"&isOverlay=false&url=#";
			Notifications.notifyUser( x.user, "declined", url, "your Role Request", x.role.name, (byte) -1 , x.project);			
		}
		else
		{
			/*
			 * if (body == null) Notifications.notifyUsers(x.user,
			 * "deletion request from project denied",
			 * "Your deletion request from project " + x.project.name +
			 * " has been denied.", (byte) -1); else
			 * Notifications.notifyUsers(x.user,
			 * "deletion request from project denied",
			 * "You deletion request from project " + x.project.name +
			 * " has been denied because " + body + ".", (byte) -1);
			 */
			{
				String b = body.replace( '+', ' ' );
				int i = body.indexOf( '&' );
				i += 6;
				b = b.substring( i );
				
				String url = Router.getFullUrl("Application.externalOpen")+"?id="+x.project.id+"&isOverlay=false&url=/users/listUserProjects?userId="+x.user.id+"&x=2&projectId="+x.project.id+"&currentProjectId="+x.project.id;
				Notifications.notifyUser( x.user, "accepted", url, "your Request to be deleted from project", x.project.name, (byte) -1 , null);				
			}
		}
		User myUser = Security.getConnected();
		Date dd = new Date();
		// Logs.addLog( myUser, "RequestDeny", "Request", x.id, y, dd );
		Log.addUserLog("Request "+(x.isDeletion ? "for deletion" : "role")+" denied", y);

		Update.update(myUser, "reload('project-requests')");
		
		x.delete();
	}

	/**
	 * This method takes user id and component id and
	 *         initiates a new request for that user to be deleted from that
	 *         component.
	 * @author OmarNabil 
	 * @param userId
	 * @param id
	 */

	public static void RequestDeleted( long id )
	{

		User myUser = Security.getConnected();
		Component myComponent = Component.findById( id );
		Request x = new Request( myUser, myComponent );
		flash.success( "your request has been sent" );
		x.save();
		// Show.project( myComponent.project.id );
		// Logs.addLog(myComponent, "request to be deleted", "Request", x.id );

	}

	/**
	 * This method lists and renders users who requested to be deleted from the 
	 * system.
	 * @author Amr TjWallas
	 * @see views/Requests/list
	 * 
	 */
	public static void list()
	{
		Security.check( Security.getConnected().isAdmin );
		List<User> users = User.find( "pendingDeletion = true" ).fetch();
		render( users );
	}

	/**
	 * This method takes a list of user ids and marks the corresponding users to
	 * these ids as deleted in the database. In other words, A System admin has
	 * approved their deletion requests.
	 * @author Amr TjWallas
	 * @param users The array consisting of user ids of these users.
	 * @see views/Requests/list
	 * 
	 */
	public static void deleteUsers( int[] users )
	{
		for( int i = 0; i < users.length; i++ )
		{
			User currentUser = User.find( "id = " + users[i] ).first();
			currentUser.deleted = true;
			currentUser.pendingDeletion = false;
			currentUser.save();
			Notifications.byeBye(currentUser, true);
		}
		flash.success( "Users are now deactivated " );
		redirect( "/admin/requests" );
	}

	public static void removeRequestRoleInProject( long roleId )
	{
		Role role = Role.findById( roleId );
		User user = Security.getConnected();
		Request request = Request.find( "byRoleAndUser", role, user ).first();
		request.delete();
		Update.update(role.project, "reload('roles')");
		renderText( "Request removed!" );
	}
	
	public static void viewTypesToReview(long pId)
	{		
		User user = Security.getConnected();
		Project pro = Project.findById(pId);
		List<TaskType> taskTypes = pro.taskTypes;
		List<Reviewer> rev = Reviewer.find("byUserAndProjectAndAccepted", user, pro, true).fetch();
		List<Reviewer> pend = Reviewer.find("byUserAndProjectAndAccepted", user, pro, false).fetch();
		ArrayList<TaskType> pending = new ArrayList<TaskType>();
		ArrayList<TaskType> taken = new ArrayList<TaskType>();
		for(Reviewer taskreviewer : rev)
			taken.add(taskreviewer.taskType);
		for(Reviewer pendingreviewer : pend)
			pending.add(pendingreviewer.taskType);
		render(taskTypes, taken, pending);
	}
		
	public static void requestReviewer(long taskTypeId)
	{
		TaskType tt = TaskType.findById(taskTypeId);
		User user = Security.getConnected();
		Reviewer rev = new Reviewer(user, tt.project, tt).save();		
		if (user.in(tt.project).can("manageRequests"))
		{
			rev.accepted=true;
			rev.save();
			Notifications.notifyProjectUsers(tt.project, "addReviewer", "", "to the reviewers for the task type", tt.name, (byte)0);
			Update.update(rev.user, "reload('reviewers')");
			Update.update(rev.project, "reload('project-"+rev.project.id+"-in-user-"+rev.user.id+"')");
			renderText("You are now "+tt.name+" reviewer in"+tt.project.name+"!");
		}
		else
			{
				Update.update(rev.user, "reload('reviewers')");
				Update.update(tt.project, "reload('project-requests')");
				renderText("Your request to be "+tt.name+" reviewer has been sent successfully!");
			}
	}
	
	public static void reviewRequestRespond(long revId, int response)
	{
		Reviewer rev = Reviewer.findById(revId);
		if(response == 1)
			{
				rev.accepted=true;
				rev.save();
				Update.update(rev.project, "reload('project-"+rev.project.id+"-in-user-"+rev.user.id+"')");
				Notifications.notifyProjectUsers(rev.project, "addReviewer", "", "to the reviewers for the task type", rev.taskType.name, (byte)0);
			}
		else
			{
				Notifications.notifyUser(rev.user, "declined", "", "your request to be reviewer for task type", rev.taskType.name, (byte)-1, rev.project);
				rev.delete();
			}
		Update.update(rev.project, "reload('project-requests')");
		Update.update(rev.user, "reload('reviewers')");
	}
	
	public static void revokeReviewer(long uId, long taskTypeId)
	{
		User user = User.findById(uId);
		TaskType tt = TaskType.findById(taskTypeId);
		Reviewer rev = Reviewer.find("byUserAndTaskType", user, tt).first();
		rev.delete();
		Notifications.notifyProjectUsers(tt.project, "deleteReviewer", "", "from the reviewers for the task type", tt.name, (byte)-1);
		Update.update(rev.user, "reload('reviewers')");
		Update.update(rev.project, "reload('project-"+rev.project.id+"-in-user-"+rev.user.id+"')");		
		renderText("The review role has been revoked successfully!");
	}
	
	/**
	 * Overriding the CRUD method show and making it forbidden
	 */
	public static void show()
	{
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method save and making it forbidden
	 */
	public static void save()
	{
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method blank and making it forbidden
	 */
	public static void blank()
	{
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method create and making it forbidden
	 */
	public static void create()
	{
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method delete and making it forbidden
	 */
	public static void delete()
	{
		forbidden();
	}

}
