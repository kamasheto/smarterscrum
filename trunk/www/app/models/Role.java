package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * Role model
 * 
 * @author mahmoudsakr
 */
@Entity
public class Role extends Model {
	/**
	 * Role name
	 */
	public String name;

	/**
	 * *..1 rel with project
	 */
	@ManyToOne
	public Project project;

	public boolean canManageRoles; // id => Project
	public boolean canCreateRole; // id => Project
	public boolean canEditRoles; // id => Role
	public boolean canDeleteRole; // id => Role

	public boolean canEditProject;
	public boolean canEditBacklog;
	public boolean canEditProjectNotificationProfile; // Moataz_Mekki
	public boolean canEditUserNotificationProfile; // Tj.Wallas_
	/*
	 * Component permissions
	 */
	public boolean canAddComponent;
	public boolean canEditComponent;
	public boolean canDeleteComponent;

	/*
	 * Sprint permissions
	 */
	public boolean canAddSprint;
	public boolean canEditSprint;
	public boolean canEditSprintBacklog;
	/*
	 * Meeting permissions
	 */
	public boolean canAddMeeting;
	public boolean canEditMeeting;
	public boolean canDeleteMeeting;

	/*
	 * Story permissions
	 */
	public boolean canAddStory;
	public boolean canEditStory;
	public boolean canDeleteStory;

	/*
	 * Task permessions
	 */

	public boolean canChangeEstimations;
	public boolean canChangeTaskType;
	public boolean canChangeTaskStatus;
	public boolean canChangeTaskDescreption;
	public boolean canChangeReviewer;
	public boolean canChangeAssignee;
	public boolean canChangeAssigneeInSprint;

	public boolean canInvite;
	public boolean canManageRequests;

	public boolean canEditColumn;
	/*
	 * choose task assignee ,reviewer,reporter permession
	 */
	public boolean canGetcomponentMembers;
	public boolean CanChooseAssignee;
	public boolean CanChooseReporter;
	public boolean CanChoooseReviewer;

	/*
	 * Other permissions
	 */
	public boolean canAddProductRole;
	public boolean canEditProductRole;
	public boolean canDeleteProductRole;

	public boolean canRequest;
	public boolean canSetDependentStories;
	public boolean canEditColumnsPositions;
	public boolean canassignStorytoSprint;

	public boolean canrespond;
	public boolean canaccept;

	public boolean canRenameColumns;
	public boolean canAddReviewLog;// Hossam Amer

	public boolean canAddTask; // Component ID .. U will need to check that the
	// user is a member of the given component

	public boolean canModifyTask; // Task ID ..

	public boolean canChangeStatus; // Task ID ..

	public boolean canAddTaskStatus; // Project ID ..

	public boolean canEditTaskStatus; // TaskStatus ID ..

	public boolean canAddTaskType; // Project ID ..

	public boolean canEditTaskType; // TaskStatus ID ..

	public boolean canViewReviewLog; // id => project id

	public boolean canStartGame;
	public boolean canViewChat; // id-->project ID
	// ---------------------------------------------------
	// TODO mahmoudsakr: add new flags BELOW this comment
	// ---------------------------------------------------

	// ---------------------------------------------------
	// TODO mahmoudsakr: add new flags BELOW this comment
	// ---------------------------------------------------

	/**
	 * 
	 public boolean canAssociateTaskToMeeting; public boolean
	 * canReportImpediment; public boolean canStartGame;
	 */
	public boolean canAssociateTaskToMeeting;
	public boolean canReportImpediment;
	// ---------------------------------------------------
	// TODO mahmoudsakr: add new flags ABOVE this comment
	// ---------------------------------------------------

	/**
	 * *..* rel with users
	 */
	@ManyToMany (mappedBy = "roles")
	public List<User> users;

	/**
	 * deleted flag
	 */
	public boolean deleted;

	/**
	 * semi-full constructor
	 * 
	 * @param name
	 *            Role name
	 */
	public Role (String name) {
		this();
		this.name = name;
	}

	/**
	 * Full constructor
	 * 
	 * @param string
	 *            role name
	 * @param project2
	 *            project this role belongs to
	 */
	public Role (String string, Project project2) {
		this();
		this.name = string;
		this.project = project2;
	}

	public Role () {
		users = new ArrayList<User>();
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
	 * Object with basic outline of role for use with search
	 * 
	 * @author mahmoudsakr
	 */
	public static class Object {
		/**
		 * role id
		 */
		long id;

		/**
		 * role name
		 */
		String name;

		/**
		 * full constructor
		 * 
		 * @param id
		 *            role id
		 * @param name
		 *            role name
		 */
		public Object (long id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
