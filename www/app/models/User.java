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
import play.db.jpa.Model;
import controllers.Application;

/**
 * @author moataz_mekki
 * @author Amr Tj.Wallas
 * @version 670
 */
@Entity
public class User extends Model {
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
	 * history boards?
	 */
	// @OneToMany( mappedBy = "user" )
	// public List<HistoryBoard> historyBoards;

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

	public User () {
		roles = new ArrayList<Role>();
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
		this.email = email;
		this.pwdHash = Application.hash(password);
		// System.out.println( "Registering " + email + " with password " +
		// password + " and hash: " + pwdHash );
		// this.isAdmin = false;
		this.avatar = "";
		this.activationHash = Application.randomHash(32);
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
	public Role getPermission(Project project) {
		List<Role> rs = this.roles;
		List<Role> temp = new LinkedList<Role>();
		for (Role r : rs) {
			if (r.project == project) {
				temp.add(r);
			}
		}
		Role result = new Role(null);

		for (int i = 0; i < temp.size(); i++) {
			result.canEditColumnsPositions |= temp.get(i).canEditColumnsPositions;
			result.canSetDependentStories |= temp.get(i).canSetDependentStories;
			result.canRequest |= temp.get(i).canRequest;
			result.canAddProductRole |= temp.get(i).canAddProductRole;
			result.canEditBacklog |= temp.get(i).canEditBacklog;
			result.canEditSprint |= temp.get(i).canEditSprint;
			result.canAddSprint |= temp.get(i).canAddSprint;
			result.CanChoooseReviewer |= temp.get(i).CanChoooseReviewer;
			result.CanChooseReporter |= temp.get(i).CanChooseReporter;
			result.CanChooseAssignee |= temp.get(i).CanChooseAssignee;
			result.canGetcomponentMembers |= temp.get(i).canGetcomponentMembers;
			result.canDeleteStory |= temp.get(i).canDeleteStory;
			result.canEditStory |= temp.get(i).canEditStory;
			result.canAddStory |= temp.get(i).canAddStory;
			result.canEditColumn |= temp.get(i).canEditColumn;
			result.canAddComponent |= temp.get(i).canAddComponent;
			result.canEditComponent |= temp.get(i).canEditComponent;
			result.canDeleteComponent |= temp.get(i).canDeleteComponent;
			result.canAddMeeting |= temp.get(i).canAddMeeting;
			result.canEditMeeting |= temp.get(i).canEditMeeting;
			result.canDeleteMeeting |= temp.get(i).canDeleteMeeting;
			result.canInvite |= temp.get(i).canInvite;
			result.canManageRequests |= temp.get(i).canManageRequests;
			result.canEditSprintBacklog |= temp.get(i).canEditSprintBacklog;
			result.canEditProjectNotificationProfile |= temp.get(i).canEditProjectNotificationProfile;
			result.canEditUserNotificationProfile |= temp.get(i).canEditUserNotificationProfile;
			result.canRenameColumns |= temp.get(i).canRenameColumns;
			result.canEditProject |= temp.get(i).canEditProject;
			result.canManageRoles |= temp.get(i).canManageRoles;
			result.canCreateRole |= temp.get(i).canCreateRole;
			result.canEditRoles |= temp.get(i).canEditRoles;
			result.canDeleteRole |= temp.get(i).canDeleteRole;
			result.canAddReviewLog |= temp.get(i).canAddReviewLog;
			/*
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 * result.canDOSOMETHING |= temp.get( i ).canDOSOMETHING;
			 */

		}
		return result;
	}

	// Omar Nabil
	// this method takes Project and returns all permissions this user have in
	// all roles
	// he is assigned to. IT returns variable "result" of type Role that contain
	// boolean variables
	// that states these permissions

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

}
