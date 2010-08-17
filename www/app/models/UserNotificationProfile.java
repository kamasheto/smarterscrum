package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import play.data.validation.Required;

/**
 * Represents the UserNotificationProfile Entity in the Database and it's relations with other entities.
 * 
 * @author Amr Tj.Wallas
 * @see models.ProjectNotificationProfile
 */
@Entity
public class UserNotificationProfile extends SmartModel {
		
	@ManyToOne
	public User user;
	@ManyToOne
	public Project project;
	public boolean deleted;
	// *********** List Of Action Types Below ***********	
	public boolean setSprint;
	public boolean addRole;		
	public boolean onCreateComponent;
	public boolean onEditComponent;
	public boolean onDeleteComponent;
	public boolean addColumn;
	public boolean deleteColumn;	
	public boolean addTaskStatus;
	public boolean editTaskStatus;
	public boolean deleteTaskStatus;
	public boolean deleteProject;
	public boolean deletedFromProject;
	public boolean editTaskType;
	public boolean addReviewer;
	// *********** List Of Action Types Ended ***********

	/**
	 * Creates a new UserNotificationProfile for a specific
	 * user "user" in a specific project "project" with the default notification
	 * options as set in the ProjectNotificationProfile of that project.
	 * 
	 * @author Amr Tj.Wallas
	 * @param user An object User that the User Notification Profile belongs to.
	 * @param project An object Project that the User Notification Profile belongs to.
	 * @see models.ProjectNotificationProfile
	 * @see models.Project
	 * @see models.User
	 */
	public UserNotificationProfile (@Required User user, @Required Project project) {
		this.user = user;
		this.project = project;		
		this.setSprint = project.notificationProfile.setSprint;
		this.addRole = project.notificationProfile.addRole;
		this.onCreateComponent = project.notificationProfile.onCreateComponent;
		this.onEditComponent = project.notificationProfile.onEditComponent;
		this.onDeleteComponent = project.notificationProfile.onDeleteComponent;
		this.addColumn=project.notificationProfile.addColumn;
	    this.deleteColumn=project.notificationProfile.deleteColumn;
		this.addTaskStatus = project.notificationProfile.addTaskStatus;
		this.editTaskStatus = project.notificationProfile.editTaskStatus;
		this.deleteTaskStatus = project.notificationProfile.deleteTaskStatus;
	    this.deleteProject = project.notificationProfile.deleteProject;
	    this.deletedFromProject=project.notificationProfile.deletedFromProject;
	    this.editTaskType=project.notificationProfile.editTaskType;
	    this.addReviewer=project.notificationProfile.addReviewer;
	}

	/**
	 * Takes the action & returns the boolean variable of this action whether it's true or false
	 * 
	 * @author Moataz_Mekki 
	 * @param action
	 *             the string that describes the action
	 * @return boolean
	 */
	public boolean checkAction(String action) {
		if (action.equalsIgnoreCase("setSprint"))
			return setSprint;
		else if (action.equalsIgnoreCase("addRole"))
			return addRole;
		else if (action.equalsIgnoreCase("onCreateComponent"))
			return onCreateComponent;
		else if (action.equalsIgnoreCase("onEditComponent"))
			return onEditComponent;
		else if (action.equalsIgnoreCase("onDeleteComponent"))
			return onDeleteComponent;
		else if (action.equalsIgnoreCase("addColumn"))
			return addColumn;
		else if (action.equalsIgnoreCase("deleteColumn"))
			return deleteColumn;
		else if (action.equalsIgnoreCase("addTaskStatus"))
			return addTaskStatus;
		else if (action.equalsIgnoreCase("editTaskStatus"))
			return editTaskStatus;
		else if (action.equalsIgnoreCase("deleteTaskStatus"))
			return deleteTaskStatus;
		else if (action.equalsIgnoreCase("deleteProject"))
			return deleteProject;
		else if (action.equalsIgnoreCase("deletedFromProject"))
			return deletedFromProject;
		else if (action.equalsIgnoreCase("editTaskType"))
			return editTaskType;
		else if (action.equalsIgnoreCase("addReviewer"))
			return addReviewer;
		else
			return false;
	}

}
