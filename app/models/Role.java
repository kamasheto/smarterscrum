package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.data.validation.Required;

/**
 * Role model
 * 
 * @author mahmoudsakr
 */
@Entity
public class Role extends SmartModel {
	/**
	 * name of the role
	 */
	@Required
	public String name;
	/**
	 * whether this role is being invoked on a system admin
	 */
	public boolean systemAdmin;
	/**
	 * the project that the role belongs to
	 */
	@ManyToOne
	public Project project;

	/**
	 * users who have this role
	 */
	@ManyToMany (mappedBy="roles")
	public List<User> users;

	/**
	 * whether this role is deleted
	 */
	public boolean deleted;
	
	/**
	 * whether the role is the base role in the project
	 */
	public boolean baseRole;

	/**
	 * Role's permissions
	 */
	@ManyToMany
	public List<Permission> permissions;
	/**
	 * creates a new role with a certain name
	 * @param name name or title of a role
	 */
	public Role (String name) {
		this();
		this.name = name;
	}
	
	/**
	 * Creates a new role in a project
	 * @param string name or title of a role.
	 * @param project2 which project to save this role to.
	 */
	public Role (String string, Project project2) {
		this();
		this.name = string;
		this.project = project2;
	}
	/**
	 * initializes the users and permissions ArrayList of a role.
	 */
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
		user.removeRole(this);
	}
	/*
	 * (non-Javadoc)
	 * @see play.db.jpa.JPASupport#toString()
	 */
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
		// System admins can, really, do anything..
		if (systemAdmin) {
			return true;
		}

		for (Permission perm : permissions) {
			if (perm.name.equalsIgnoreCase(action)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gives the permissions to a role based on the input string for example
	 * a project owner can edit a project as well as a project Admin. However,
	 * a developer can not edit a project.
	 * @param string
	 */
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
