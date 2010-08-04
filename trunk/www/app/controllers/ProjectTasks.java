package controllers;

import java.util.LinkedList;
import java.util.List;

import notifiers.Notifications;

import models.Project;
import models.Request;
import models.Role;
import models.User;
import play.mvc.With;

/**
 * Project tasks
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class ProjectTasks extends SmartController {
	/**
	 * Connected user requests role
	 * 
	 * @param id
	 *            role id
	 */
	public static void requestRole(long id) {
		User user = Security.getConnected();
		Role role = Role.findById(id);
		notFoundIfNull(role);
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
	 * @issue 94, 96
	 * @sprint 4
	 */
	public static void revokeRole(long roleId, long userId) 
	{
		User connectedUser = Security.getConnected();
		User user = User.findById(userId);
		Role role = Role.findById(roleId);
		String msg = "";
		notFoundIfNull(role);
		if((!role.baseRole)&&((connectedUser.in(role.project).can("revokeUserRole"))||(user.id == connectedUser.id)))
		{
			user.removeRole(role, user);
			msg="You have revoked a role successfuly.1";
			String url = "@{Application.externalOpen("+role.project+", '#', false)}";
			Notifications.notifyUser(user, "Delet", url, "you from project", role.project.name, (byte)-1, null);
			renderText(msg);
		}
		
		if((role.baseRole))
		{
			if((connectedUser.in(role.project).can("revokeUserRole")))
			{
				user.removeRole(role, user);
				msg="You have revoked a role successfuly, The user is no longer a member in this project.1";
				String url = "@{Application.externalOpen("+role.project+", '/users/listUserProjects?userId="+user.id+"&boxId=2&projectId="+role.project.id+"&currentProjectId="+role.project+"', false)}";
				Notifications.notifyUser(user, "Revok", url, "your role", role.name, (byte)-1, role.project);
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
	 * R
	 * eturns list of roles of this project
	 * 
	 * @param id
	 *            project id
	 */
	public static void getRoles(long id) {
		List<Role.Object> roles = new LinkedList<Role.Object>();
		for (Role role : Project.<Project> findById(id).roles) {
			roles.add(new Role.Object(role.id, role.name));
		}
		renderJSON(roles);
	}
}