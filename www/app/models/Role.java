package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 * Role model
 * 
 * @author mahmoudsakr
 */
@Entity
public class Role extends SmartModel {
	public String name;

	@ManyToOne
	public Project project;

	@ManyToMany (mappedBy = "roles")
	public List<User> users;

	public boolean deleted;

	@ManyToMany
	public List<Permission> permissions;

	public Role (String name) {
		this();
		this.name = name;
	}

	public Role (String string, Project project2) {
		this();
		this.name = string;
		this.project = project2;
	}

	public Role () {
		users = new ArrayList<User>();
		permissions = new ArrayList<Permission>();
	}

	/**
	 * This method simply removes a user from the list of users corresponding to
	 * a role.
	 * 
	 * @author Amr Tj.Wallas
	 * @param user
	 *            The user who shouldn't have this role anymore.
	 * @see controllers.Requests
	 * @since Sprint2.
	 * @Task C1S14
	 */
	public void removeUser(User user) {
		this.users.remove(user);
		this.save();

	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Whether this role can perform this action or not (used in series with
	 * user.in(Project))
	 * 
	 * @param action
	 *            string of action to check against
	 * @return true if role could, false otherwise
	 */
	public boolean can(String action) {
		for (Permission perm : permissions) {
			if (perm.name.equalsIgnoreCase(action)) {
				return true;
			}
		}
		return false;
	}

	public void init(String string) {
		String[] perms = null;
		if (string.equalsIgnoreCase("projectOwner")) {
			perms = new String[] { "editProject" };
		} else if (string.equalsIgnoreCase("projectAdmin")) {
			perms = new String[] { "editProject" };
		} else if (string.equalsIgnoreCase("scrumMaster")) {
			perms = new String[] { "editProject" };
		} else if (string.equalsIgnoreCase("developer")) {
			perms = new String[] {};
		}

		// finally add the permissions
		for (String perm : perms) {
			Permission permission = Permission.find("byName", perm).first();
			permissions.add(permission);
		}

		save();
	}

	/**
	 * Object with basic outline of role for use with search
	 * 
	 * @author mahmoudsakr
	 */
	public static class Object {
		long id;

		String name;

		public Object (long id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
