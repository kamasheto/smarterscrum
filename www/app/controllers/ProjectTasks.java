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
			user.roles.add(role);
			if (!user.projects.contains(role.project))
				user.projects.add(role.project);
			user.save();
			flash.success("Successfully added role: " + role.name);
		} else {
			new Request(user, role).save();
			flash.success("Successfully requested role: " + role.name);
		}
		Show.project(role.project.id);
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