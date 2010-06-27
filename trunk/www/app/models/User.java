package models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import controllers.Application;

/**
 * @author moataz_mekki
 * @author Amr Tj.Wallas
 * @version 670
 */
@Entity
public class User extends SmartModel {
	/**
	 * username
	 */
	@Required
	@MaxSize (25)
	@Column (unique = true)
	public String name;

	/**
	 * email
	 */
	@Column (unique = true)
	@Required
	@Email
	public String email;

	/**
	 * password md5 hash (64 bits raw)
	 */
	@Required
	public String pwdHash;

	/**
	 * avatar url (online link, for now)
	 */
	public String avatar;

	/**
	 * system admin?
	 */
	public boolean isAdmin;

	/**
	 * deleted?
	 */
	public boolean deleted;

	/**
	 * pending deletion?
	 */
	public boolean pendingDeletion;

	/**
	 * @author Amr Tj.Wallas
	 * @Description Activation Hash
	 */
	public String activationHash;

	/**
	 * @author Amr Tj.Wallas
	 * @Description did this user confirm his Account?
	 */
	public boolean isActivated;

	/**
	 * user roles
	 */
	@ManyToMany
	public List<Role> roles;

	/**
	 * meeting attendances
	 */
	@OneToMany (mappedBy = "user")
	public List<MeetingAttendance> attendantusers;

	/**
	 * projects
	 */
	@ManyToMany
	public List<Project> projects;

	/**
	 * components
	 */
	@ManyToMany
	public List<Component> components;

	/**
	 * user logs
	 */
	@OneToMany (mappedBy = "user")
	public List<Log> logs;

	/**
	 * tasks
	 */
	@OneToMany (mappedBy = "userTask")
	public List<Task> tasks;

	/**
	 * UserNotificationProfiles
	 */
	@OneToMany (mappedBy = "user")
	public List<UserNotificationProfile> userNotificationProfiles;
	/**
	 * Notifications
	 */
	@OneToMany (mappedBy = "user", cascade = CascadeType.ALL)
	public List<Notification> notifications;

	@OneToMany (mappedBy = "user")
	public List<Request> requests;

	public ArrayList<ChatRoom>openChats;
	public User () {
		roles = new ArrayList<Role>();
		openChats = new ArrayList<ChatRoom>();
		attendantusers = new ArrayList<MeetingAttendance>();
		projects = new ArrayList<Project>();
		components = new ArrayList<Component>();
		logs = new ArrayList<Log>();
		tasks = new ArrayList<Task>();
		userNotificationProfiles = new ArrayList<UserNotificationProfile>();
		notifications = new ArrayList<Notification>();
		requests = new ArrayList<Request>();
	}

	/**
	 * Class constructor specifying:
	 * 
	 * @param name
	 *            String not less than 4 Chars & not longer than 25 & required
	 * @param email
	 *            String containing the email of that user
	 * @param pass
	 *            for the password not less than 4 Chars & required
	 * @param avatar
	 *            a string containing the link of the photo
	 * @param isAdmin
	 *            to specify if this user a systemAdmin or not
	 */
	public User (String name, String email, String pass, String avatar, boolean isAdmin) {
		this(name, email, pass);
		this.avatar = avatar;
		this.isAdmin = isAdmin;
		this.activationHash = Application.randomHash(32);

		openChats = new ArrayList<ChatRoom>();
	}

	/**
	 * method to set the picture of the user giving it a URL
	 * 
	 * @param avatar
	 *            given a String will be stored as string
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar.toString();
	}

	/**
	 * this method returns a list of the project related to a certain user
	 * requested by C1S24, C5S1
	 * 
	 * @return : List of user's projects
	 */
	public List<Project> getProjects() {
		return projects;
	}

	/**
	 * This is another easier constructor for construction a new user Entity in
	 * the database used an created by Amr Tj.Wallas.
	 * 
	 * @author Amr Tj.Wallas
	 * @param name
	 *            name of that new user
	 * @param email
	 *            email of that new user
	 * @param password
	 *            password of that new user
	 * @Task C1S4
	 */
	public User (String name, String email, String password) {
		this();
		this.name = name;
		this.email = email.toLowerCase();
		this.pwdHash = Application.hash(password);
		this.avatar = "";
		this.activationHash = Application.randomHash(32);

		openChats = new ArrayList<ChatRoom>();
	}
	
	public String getEmail() {
		return email.toLowerCase();
	}

	public String toString() {
		return name;
	}

	/**
	 * returns a role of all the roles this user has in this project
	 * 
	 * @param project
	 * @return oring of all permissions of all roles
	 */
	public Role in(Project project) {
		List<Role> rs = this.roles;
		List<Role> temp = new LinkedList<Role>();
		for (Role r : rs) {
			if (r.project == project) {
				temp.add(r);
			}
		}
		Role result = new Role(null);
		result.systemAdmin = isAdmin;

		for (Role r : temp) {
			for (Permission permission : r.permissions) {
				result.permissions.add(permission);
			}
		}
		return result;
	}

	/**
	 * This method takes a meeting ID and return the meeting status of the user
	 * in the specific meeting
	 * 
	 * @param meetingID
	 * @author Amr Hany
	 */

	public String meetingStatus(long meetingID) {
		List<MeetingAttendance> attendance = MeetingAttendance.find("byMeeting.idAndUser.idAnddeleted", meetingID, this.id, false).fetch();
		if (attendance.size() == 0) {
			return "notInvited";
		} else
			return attendance.get(0).status;
	}

	public static class Object {

		long id;

		String name;
		long lastClick;

		boolean isAdmin;

		public Object (long id, String username) {
			this.id = id;
			this.name = username;
		}

		public Object (long id, String username, long lastClick, boolean isAdmin) {
			this(id, username);
			this.lastClick = lastClick;
			this.isAdmin = isAdmin;
		}
	}

	/**
	 * @author hadeer_diwan Returns a list of tasks associated to a certain
	 *         sprint for a certain component to a certain user.
	 * @param s
	 *            : given a sprint
	 * @param componentID
	 *            :given a component
	 * @return : List of tasks in this sprint of this component of that user
	 */

	@SuppressWarnings ("null")
	public List<Task> returnUserTasks(Sprint s, long componentID) {
		Component c = Component.findById(componentID);
		List<Task> tasks = c.returnComponentTasks(s);
		List<Task> userTasks = new ArrayList<Task>();

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).assignee.equals(this))
				userTasks.add(tasks.get(i));
		}
		return userTasks;

	}

	/**
	 * Returns a styled display name of the user per project
	 * 
	 * @author mahmoudsakr
	 * @param project
	 *            project to display name in
	 * @return String of dimmed username
	 */
	public String getDisplayName(Project project) {
		return projects.contains(project) ? name : "<span class='userNotInProject'>" + name + "</span>";
	}
	
	/**
	 * @author Moataz
	 * @param project: the project we want to get the roles from
	 * this method returns the list of roles of a user in a
	 * specific project
	 */
	public String getUserRoles(Project project)
	{
		String res="";
		for(int i = 0 ; i<this.roles.size() ; i++)
		{
			if(this.roles.get(i).project.id == project.id)
				res += ", "+ this.roles.get(i).name;			
		}
		if(!res.isEmpty())
			res = res.substring(1);
		else
			res= "No Role !!";
		return res;
	}
	
	public void removeRole(Role role) {
		roles.remove(role);
		if (roles.size() < 0) {
			projects.remove(role.project);
		}
		save();
	}
}
