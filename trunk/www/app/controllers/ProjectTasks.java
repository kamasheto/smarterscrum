package controllers;

import java.util.LinkedList;
import java.util.List;

import models.Project;
import models.Request;
import models.Role;
import models.Update;
import models.User;
import models.Log;

import notifiers.Notifications;
import play.mvc.Router;
import play.mvc.With;

/**
 * Project tasks
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class ProjectTasks extends SmartController {
	/**
	 * A method that handles the role requests done by the connected user.
	 * 
	 * @param id
	 *            role id
	 */
	public static void requestRole(long id) {
		User user = Security.getConnected();
		Role role = Role.findById(id);
		notFoundIfNull(role);
		Update.update(user, "reload('roles')");
		Update.update(role.project, "reload('project-requests')");
		Log.addUserLog("Requested role", role, role.project);
		if (user.in(role.project).can("manageRequests")) {
			user.addRole(role);
			renderText("Successfully added role!");
		} else {
			new Request(user, role).save();
			renderText("Request sent! - Waiting for a project admin to approve.");
		}
	}

	/**
	 * This method revokes a specific role from a specific user.
	 * Only the user him self or the user with can("revokeUserRole") permission can revoke a role.
	 * In case the role revoked is a base role the user is deleted from the project.
	 * 
	 * @author Dina Helal, Heba Elsherif
	 * @param id
	 *            role id.
	 * @return void
	 */
	public static void revokeRole(long roleId, long userId) 
	{
		User connectedUser = Security.getConnected();
		User user = User.findById(userId);
		Role role = Role.findById(roleId);
		String msg = "";
		notFoundIfNull(role);
		Log.addUserLog("Revoked role from user: " + user.name, role, role.project);
		if((!role.baseRole)&&((connectedUser.in(role.project).can("revokeUserRole"))||(user.id == connectedUser.id)))
		{
			user.removeRole(role, user);
			msg="You have revoked a role successfuly|reload('roles', 'project-'+projectId+'-in-user-'+userId);";			
			String url = Router.getFullUrl("Application.externalOpen")+"?id="+role.project.id+"&isOverlay=false&url=/users/listUserProjects?userId="+user.id+"&boxId=2&projectId="+role.project.id+"&currentProjectId="+role.project.id;			
			Notifications.notifyUser(user, "revoked", url, "your role", role.name, (byte)-1, role.project);
			renderText(msg);
		}
		
		if((role.baseRole))
		{
			if((connectedUser.in(role.project).can("revokeUserRole")))
			{
				user.removeRole(role, user);
				msg="You have revoked a role successfuly, The user is no longer a member in this project.|reload('roles', 'users', 'projects-in-user-'+userId, 'user-'+userId, 'project-'+projectId+'-in-user-'+userId);|$('#project-search-result-'+projectId).remove();";
				String url = Router.getFullUrl("Application.externalOpen")+"?id="+role.project.id+"&isOverlay=false&url=#";
				Notifications.notifyUser(user, "deleted", url, "you from project", role.project.name, (byte)-1, null);
				renderText(msg);
			}
			else if((user.id == connectedUser.id))
			{
				msg="You cannot revoke this role without requesting to be deleted from this project.";
				renderText(msg);
			}
		}
	}

	/**
	 * Returns the roles of the project specified by the parameter id.
	 * 
	 * @param id
	 *            project id
	 */
	public static void getRoles(long id) {
		List<Role.Object> roles = new LinkedList<Role.Object>();
		for (Role role : Project.<Project> findById(id).roles) {
			if(!role.deleted)
				roles.add(new Role.Object(role.id, role.name));
		}
		renderJSON(roles);
	}
}