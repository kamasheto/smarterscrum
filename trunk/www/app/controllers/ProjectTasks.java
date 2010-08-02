package controllers;

import java.util.LinkedList;
import java.util.List;

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
	 * Connected user revokes role
	 * @author 
	 * 			  Dina Helal
	 * @param id
	 *            role id
	 */
	public static void revokeRole(long id) {
		User user = Security.getConnected();
		Role role = Role.findById(id);
		notFoundIfNull(role);
		if (user.in(role.project).can("revokeUserRole") || role.users.contains(user)) {
			user.removeRole(role);
			if(user.roles.size()==0)
			{
			renderText("Role revoked Succesfully!"+ '\n' +"Note that by revoking this role you have been removed from the project");	
			}
			else
			{
				renderText("Role revoked Succesfully!");
			}
			
		}
		
	}

	
	/**
	 * Returns list of roles of this project
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