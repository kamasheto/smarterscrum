package controllers;

import models.Column;
import models.Component;
import models.Game;
import models.Meeting;
import models.Project;
import models.Role;
import models.Sprint;
import models.Story;
import models.Task;
import models.TaskStatus;
import models.User;

/**
 * Security class/controller.. handles all security checks/permissions
 * 
 * @author mahmoudsakr
 */
public class Security extends Secure.Security {

	public static User getConnected() {
		return User.find("byEmail", connected()).first();
	}

	/**
	 * returns whether or not this email/pwd form a valid combination
	 * 
	 * @param email
	 *            user email
	 * @param password
	 *            user password
	 * @return true if such a user exist, false otherwise
	 */
	public static boolean authentify(String email, String password) {
		User user = User.find("select u from User u where u.email=? and u.pwdHash = ?", email, Application.hash(password)).first();
		/* By Tj.Wallas_ in Sprint2 */
		if (user != null && !user.isActivated) {
			flash.error("Your account is not activated, please follow the instructions in the Email we sent you to activate your account");
			try {
				Secure.login();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return user != null;
	}

	/**
	 * Checks whether the connected user has permission to perform a given
	 * action or not. Actions are checked against all roles this user has in a
	 * given project. To access the current project, one must use the "id" param
	 * to refer to a model that could link back to the required project.
	 * <p>
	 * Example, canAddComponent naturally refers to
	 * "Adding a component to a project", hence "id" should have the value of
	 * the projectId.
	 * <p>
	 * Example, canEditComponent refers to "Editing a component", hence "id"
	 * should have the value of the componentId needed to be edited. From there,
	 * the method checks whether this user has permission in that project or
	 * not.
	 * 
	 * @param profile
	 *            string value of attribute, just to make things easier
	 * @return true if user has permission to perform this action in the current
	 *         project, false otherwise
	 */
	public static boolean check(String profile) {
		if (profile == null) {
			profile = "";
		}

		User user = getConnected();
		if (user == null) {
			// logged in from previous session, send him to logout
			try {
				Secure.logout();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if (user.isAdmin) {
			// he.. cans.. does.. ay 7aga X(
			// System.out.println("System admin " + profile);
			return true;
		} else if (profile.equals("systemAdmin")) {
			return false;
		}

		long id;

		try {
			id = Long.parseLong(params.get("id"));
		} catch (NumberFormatException e) {
			id = 0;
		}

		Project project = null;
		if (profile.equals("loggedIn")) {
			return true;
		} else if (profile.equals("canAddComponent")) {
			project = Project.findById(id);
			// return user.getPermission(project).canAddComponent;
		} else if (profile.equals("canEditComponent")) {
			Component component = Component.findById(id);
			project = component.project;
			// return user.getPermission(component.project).canEditComponent;
		} else if (profile.equals("canDeleteComponent")) {
			Component component = Component.findById(id);
			project = component.project;
			// return user.getPermission(component.project).canDeleteComponent;
		} else if (profile.equals("canAddMeeting")) {
			Sprint sprint = Sprint.findById(id);
			project = sprint.project;
			// return user.getPermission(sprint.project).canAddMeeting;
		} else if (profile.equals("canEditMeeting")) {
			Meeting meeting = Meeting.findById(id);
			project = meeting.project;
			// return user.getPermission(meeting.project).canEditMeeting;
		} else if (profile.equals("canDeleteMeeting")) {
			Meeting meeting = Meeting.findById(id);
			project = meeting.project;
			// return user.getPermission(meeting.project).canDeleteMeeting;
		} else if (profile.equals("canInvite")) {
			Role role = Role.findById(id);
			project = role.project;
			// return user.getPermission(role.project).canInvite;
		} else if (profile.equals("canManageRequests")) {
			project = Project.findById(id);
			// return user.getPermission(project).canManageRequests;
		} else if (profile.equals("canEditColumn")) {
			Column col = Column.findById(id);
			project = col.board.project;
			// return user.getPermission(col.board.project).canEditColumn;
		} else if (profile.equals("canEditStory")) {
			Story s = Story.findById(id);
			project = s.componentID.project;
			// return user.getPermission(s.componentID.project).canEditStory;
		} else if (profile.equals("canDeleteStory")) {
			Story s = Story.findById(id);
			project = s.componentID.project;
			// return user.getPermission(s.componentID.project).canDeleteStory;
		} else if (profile.equals("canGetcomponentMembers")) {
			Component c = Component.findById(id);
			project = c.project;
			// return user.getPermission(c.project).canGetcomponentMembers;
		} else if (profile.equals("CanChooseAssignee")) {
			// TODO ambiguous
			Task t = Task.findById(id);
			project = t.taskStory.componentID.project;
			// return
			// user.getPermission(t.taskStory.componentID.project).CanChooseAssignee;
		} else if (profile.equals("CanChooseReporter")) {
			Task t = Task.findById(id);
			project = t.taskStory.componentID.project;
			// return
			// user.getPermission(t.taskStory.componentID.project).CanChooseReporter;
		} else if (profile.equals("CanChoooseReviewer")) {
			Task t = Task.findById(id);
			project = t.taskStory.componentID.project;
			// return
			// user.getPermission(t.taskStory.componentID.project).CanChoooseReviewer;
		} else if (profile.equals("canAddSprint")) {
			// TODO set to projectId, fix to id!
			project = Project.findById(Long.parseLong(params.get("projectId")));

			// return user.getPermission(p).canAddSprint;
		} else if (profile.equals("canEditSprint")) {
			Sprint s = Sprint.findById(id);
			project = s.project;
			// return user.getPermission(s.project).canEditSprint;

		} else if (profile.equals("canEditBacklog")) {
			project = Project.findById(id);

			// return user.getPermission(project).canEditBacklog;
		} else if (profile.equals("canEditSprintBacklog")) {
			Sprint sprint = Sprint.findById(id);
			project = sprint.project;

			// return user.getPermission(sprint.project).canEditSprintBacklog;
		} else if (profile.equals("canAddStory")) {
			// TODO ambiguous id
		} else if (profile.equals("canAddProductRole")) {
			// Project p =
			// Project.findById(Long.parseLong(params.get("projectId")));
			// return user.getPermission(p).canAddProductRole;
		} else if (profile.equals("canRequest")) {
			Story s = Story.findById(id);
			project = s.componentID.project;
			// return user.getPermission(s.componentID.project).canRequest;
		} else if (profile.equals("canSetDependentStories")) {
			Story s = Story.findById(Long.parseLong(params.get("storyId")));
			project = s.componentID.project;
			// return
			// user.getPermission(s.componentID.project).canSetDependentStories;
		} else if (profile.equals("canEditColumnsPositions")) {
			Sprint c = Sprint.findById(id);
			project = c.project;
			// return user.getPermission(c.project).canEditColumnsPositions;
		} else if (profile.equals("canEditProfile")) {
			return id > 0 ? id == getConnected().id : true;
		} else if (profile.equals("canEditProjectNotificationProfile")) {
			project = Project.findById(id);
			// return user.getPermission(p).canEditProjectNotificationProfile;
		} else if (profile.equals("canEditUserNotificationProfile")) {
			project = Project.findById(id);
			// return user.getPermission(p).canEditUserNotificationProfile;
		} else if (profile.equals("canRenameColumns")) {
			Column col = Column.findById(id);
			project = col.board.project;
			// return user.getPermission(col.board.project).canRenameColumns;
		} else if (profile.equals("canEditProject")) {
			project = Project.findById(id);

			// return user.getPermission(p).canEditProject;
		} else if (profile.equals("canManageRoles")) {
			project = Project.findById(id);
			// return user.getPermission(p).canManageRoles;
		} else if (profile.equals("canCreateRole")) {
			project = Project.findById(id);
			// return user.getPermission(p).canCreateRole;
		} else if (profile.equals("canEditRoles")) {
			Role p = Role.findById(id);
			project = p.project;
			// return user.getPermission(p.project).canEditRoles;
		} else if (profile.equals("canDeleteRole")) {
			Role p = Role.findById(id);
			project = p.project;
			// return user.getPermission(p.project).canDeleteRole;
		} else if (profile.equals("canAddReviewLog")) {
			project = Project.findById(Long.parseLong(params.get("projectID")));
			// return user.getPermission(p).canAddReviewLog;
		} else if (profile.equals("CanSetStatusToClosed") || profile.equals("CanSetStatusToVerified") || profile.equals("CanSetStatusToReopened")) {
			Task t = Task.findById(Long.parseLong(params.get("id")));
			if (t.assignee.equals(user)) {
				return false;
			} else
				return true;
		} else if (profile.equals("CanSetOtherStatus")) {
			Task t = Task.findById(Long.parseLong(params.get("id")));
			if (t.reviewer.equals(user)) {
				return false;
			} else
				return true;
		} else if (profile.equals("inGame")) {
			id = Long.parseLong(params.get("gameId"));
			Game g = Game.findById(id);
			return user.components.contains(g.component);
		} else if (profile.equals("canAddTask")) {
			Component c = Component.findById(id);
			return user.components.contains(c);
		} else if (profile.equals("canModifyTask")) {
			Task g = Task.findById(id);
			project = g.taskStory.componentID.project;
			// return
			// user.getPermission(g.taskStory.componentID.project).canModifyTask;
		} else if (profile.equals("canChangeStatus")) {
			Task g = Task.findById(id);
			project = g.taskStory.componentID.project;
			// return
			// user.getPermission(g.taskStory.componentID.project).canChangeStatus;
		} else if (profile.equals("canAddTaskStatus")) {
			project = Project.findById(id);
			// return user.getPermission(g).canAddTaskStatus;
		} else if (profile.equals("canEditTaskStatus")) {
			TaskStatus g = TaskStatus.findById(id);
			project = g.project;
			// return user.getPermission(g.project).canEditTaskStatus;
		} else if (profile.equals("canAddTaskType")) {
			project = Project.findById(id);
			// return user.getPermission(g).canAddTaskType;
		} else if (profile.equals("canEditTaskType")) {
			TaskStatus g = TaskStatus.findById(id);
			project = g.project;
			// return user.getPermission(g.project).canEditTaskType;
		} else if (profile.equals("canViewReviewLog")) {
			project = Project.findById(id);
			// return user.getPermission(g).canViewReviewLog;
		} else if (profile.equals("canViewChat")) {
			project = Project.findById(id);
			// return user.getPermission(g).canViewChat;
		} else if (profile.equals("canStartGame")) {
			Component c = Component.findById(Long.parseLong(params.get("cId")));
			project = c.project;
			// return user.getPermission(c.project).canStartGame;
		}

		if (project == null) {
			return false;
		}

		return user.in(project).can(profile);
		// just a default return thingie
		// boolean def = false;

		// and just for the record
		// System.out.println("Returning " + def +
		// " from controllers.Security: Did not match any profile for: " +
		// profile);
		// return def;
	}
}