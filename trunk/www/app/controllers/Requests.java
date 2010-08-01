package controllers;

import java.util.Date;
import java.util.List;

import notifiers.Notifications;

import models.Component;
import models.Project;
import models.Request;
import models.Role;
import models.User;
import models.UserNotificationProfile;
import play.mvc.With;

@With (Secure.class)
public class Requests extends SmartCRUD {
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
	public static void requestRespond(long id) {
		Project pro = Project.findById(id);
		Security.check(pro, "manageRequests");
		List<Request> requests = Request.find("isDeletion = false and project = " + pro.id + " order by id desc").fetch();
		List<Request> drequests = Request.find("isDeletion = true and project = " + pro.id + " order by id desc").fetch();
		render(requests, drequests, pro);
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
	public static void requestAccept(String hash) {
		Request x = Request.find("byHash", hash).first();
		Project y = x.project;
		x.user.addRole(x.role);
		Notifications.notifyUsers(x.user, "Role Request Accepted", "Your Role request to be " + x.role.name + " in " + x.role.project.name + " has been accepted", (byte) 1);
		User myUser = Security.getConnected();
		Logs.addLog(myUser, "RequestAccept", "Request", x.id, y, new Date());
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
	public static void deletionRequestAccept(String hash) throws Throwable {
		if (hash == null)
			Secure.login();
		Request currentRequest = Request.find("byHash", hash).first();
		Security.check(currentRequest.project, "manageRequests");
		if (currentRequest == null)
			Secure.login();
		UserNotificationProfile currentProfile = UserNotificationProfile.find("user =  " + currentRequest.user.id + " and project = " + currentRequest.project.id).first();
		if (currentProfile != null)
			currentProfile.delete();
		List<Role> projectRoles = Role.find("project", currentRequest.project).fetch();

		for (int i = 0; i < projectRoles.size(); i++) {
			if (currentRequest.user.roles.contains(projectRoles.get(i))) {
				currentRequest.user.roles.remove(projectRoles.get(i));
				currentRequest.user.save();
			}
		}

		currentRequest.user.projects.remove(currentRequest.project);
		currentRequest.user.save();
		if (!currentRequest.user.components.isEmpty()) {
			List<Component> currentComponents = Component.find("project", currentRequest.project).fetch();
			for (int i = 0; i < currentComponents.size(); i++) {
				currentRequest.user.components.remove(currentComponents.get(i));
				currentRequest.user.save();
			}
		}
		Notifications.notifyUsers(currentRequest.user, "Project deletion Request Accepted", "Your request to be deleted from " + currentRequest.project.name + " has been accepted", (byte) 1);
		Logs.addLog(Security.getConnected(), "DeletionRequestAccept", "Request", currentRequest.id, currentRequest.project, new Date());
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
	public static void requestIgnore(String hash, String body) throws Throwable {
		if (hash == null)
			Secure.login();
		Request x = Request.find("byHash", hash).first();
		Security.check(x.project, "manageRequests");
		if (x == null)
			Secure.login();
		Project y = x.project;
		if (!x.isDeletion) {
			Notifications.notifyUsers(x.user, "Role Request Denied", "Your Role request to be " + x.role.name + " in " + y.name + " has been denied", (byte) -1);
		} else {
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
				String b = body.replace('+', ' ');
				int i = body.indexOf('&');
				i += 6;
				b = b.substring(i);
				Notifications.notifyUsers(x.user, "deletion request from project denied", "Your deletion request from project " + x.project.name + " has been denied because " + b + ".", (byte) -1);
			}
		}
		User myUser = Security.getConnected();
		Date dd = new Date();
		Logs.addLog(myUser, "RequestDeny", "Request", x.id, y, dd);
		x.delete();
	}

	/**
	 * @author OmarNabil This method takes user id and component id and
	 *         initiates a new request for that user to be deleted from that
	 *         component
	 * @param userId
	 * @param id
	 */

	public static void RequestDeleted(long id) {

		User myUser = Security.getConnected();
		Component myComponent = Component.findById(id);
		Request x = new Request(myUser, myComponent);
		flash.success("your request has been sent");
		x.save();
		Show.project(myComponent.project.id);
		// Logs.addLog(myComponent, "request to be deleted", "Request", x.id );

	}

	public static void list() {
		Security.check(Security.getConnected().isAdmin);
		List<User> users = User.find("pendingDeletion = true").fetch();
		render(users);
	}

	public static void deleteUsers(int[] users) {
		for (int i = 0; i < users.length; i++) {
			User currentUser = User.find("id = " + users[i]).first();
			currentUser.deleted = true;
			currentUser.pendingDeletion = false;
			currentUser.save();
			Notifications.notifyUsers(currentUser, "Your deletion request from our system has been approved.", "Dear " + currentUser.name + ", your deletion request from our system has been approved by a system admin. we hope to see you back again!", (byte) -1);
		}
		flash.success("Users are now deactivated ");
		redirect("/admin/requests");
	}
	
	
	public static void removeRequestRoleInProject(long roleId){
		Role role = Role.findById(roleId);
		User user = Security.getConnected();
		Request request = Request.find("byRoleAndUser", role, user).first();
		request.delete();
		renderText("Request removed!");
	}
	
	public static void show() {
		forbidden();
	}

	public static void save() {
		forbidden();
	}

	public static void blank() {
		forbidden();
	}

	public static void create() {
		forbidden();
	}

	public static void delete() {
		forbidden();
	}

}
