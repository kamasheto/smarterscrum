package controllers;

import java.util.Date;
import java.util.List;

import models.Component;
import models.Project;
import models.Request;
import models.Role;
import models.User;
import models.UserNotificationProfile;
import notifiers.Notifications;
import play.mvc.With;

@With( Secure.class )
public class Requests extends SmartCRUD
{
	/**
	 * belongs to s15
	 * <p>
	 * this method renders to the html page a list of pending requests
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            : takes the id of the project first checks for the permission
	 *            then list the requests
	 */
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
	 * @since Sprint2.
	 * @Task C1S14
	 */
	// @Check ("canManageRequests")
	public static void requestRespond( long id )
	{
		Project pro = Project.findById( id );
		Security.check( pro, "manageRequests" );
		List<Request> requests = Request.find( "isDeletion = false and project = " + pro.id + " order by id desc" ).fetch();
		List<Request> drequests = Request.find( "isDeletion = true and project = " + pro.id + " order by id desc" ).fetch();
		render( requests, drequests, pro );
	}

	/**
	 * belongs to s15
	 * <p>
	 * this method performs the action of accepting a request once the request
	 * is accepted it added the user to the list of roles and the role to that
	 * user
	 * 
	 * @author Moataz_Mekki
	 * @param hash
	 *            : to find the request that was accepted
	 */
	public static void requestAccept( String hash )
	{
		Request x = Request.find( "byHash", hash ).first();
		Project y = x.project;
		x.user.addRole( x.role );
		String url = "@{Application.externalOpen("+x.project.id+", '/users/listUserProjects?userId="+x.user.id+"&x=2&projectId="+x.project.id+"&currentProjectId="+x.project.id+"', false)}";
		Notifications.notifyUser( x.user, "Accept", url, "your Role Request", x.role.name, (byte) 1 , x.project);
		User myUser = Security.getConnected();
		Logs.addLog( myUser, "RequestAccept", "Request", x.id, y, new Date() );
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
	 * @since Sprint2.
	 * @Task C1S14
	 */
	// @Check ("canManageRequests")
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
		String url = "@{Application.externalOpen("+currentRequest.project.id+", '#', false)}";
		Notifications.notifyUser( currentRequest.user, "Accept", url, "your Request to be deleted from project", currentRequest.project.name, (byte) 1 , null);		
		Logs.addLog( Security.getConnected(), "DeletionRequestAccept", "Request", currentRequest.id, currentRequest.project, new Date() );
		currentRequest.delete();
	}

	/**
	 * belongs to s15
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
	 * @Task C1S14
	 */
	// @Check ("canManageRequests")
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
			String url = "@{Application.externalOpen("+x.project.id+", '#', false)}";
			Notifications.notifyUser( x.user, "Deni", url, "your Role Request", x.role.name, (byte) -1 , x.project);			
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
				
				String url = "@{Application.externalOpen("+x.project.id+", '/users/listUserProjects?userId="+x.user.id+"&x=2&projectId="+x.project.id+"&currentProjectId="+x.project.id+"', false)}";
				Notifications.notifyUser( x.user, "Accept", url, "your Requestto be deleted from project", x.project.name, (byte) -1 , null);				
			}
		}
		User myUser = Security.getConnected();
		Date dd = new Date();
		Logs.addLog( myUser, "RequestDeny", "Request", x.id, y, dd );
		x.delete();
	}

	/**
	 * @author OmarNabil This method takes user id and component id and
	 *         initiates a new request for that user to be deleted from that
	 *         component
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

	public static void list()
	{
		Security.check( Security.getConnected().isAdmin );
		List<User> users = User.find( "pendingDeletion = true" ).fetch();
		render( users );
	}

	public static void deleteUsers( int[] users )
	{
		for( int i = 0; i < users.length; i++ )
		{
			User currentUser = User.find( "id = " + users[i] ).first();
			currentUser.deleted = true;
			currentUser.pendingDeletion = false;
			currentUser.save();
			Notifications.byeBye();
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
		renderText( "Request removed!" );
	}

	public static void show()
	{
		forbidden();
	}

	public static void save()
	{
		forbidden();
	}

	public static void blank()
	{
		forbidden();
	}

	public static void create()
	{
		forbidden();
	}

	public static void delete()
	{
		forbidden();
	}

}
