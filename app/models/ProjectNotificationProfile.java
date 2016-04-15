package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.data.validation.Required;

/**
 * @author moataz_mekki
 */

@Entity
public class ProjectNotificationProfile extends SmartModel {
	/**
	 * All variables represents an option to the user to be notified when it happens. The boolean variable
	 * is whether to notify the user.
	 */
	@OneToOne
	public Project project;
	public boolean deleted;	
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
	public boolean deleteReviewer;
	public boolean addProductRole;
	/**
	 * the constructor that's responsible for adding this profile to the DB
	 * 
	 * @param project
	 *            : the project that this notification profile belongs to
	 */
	public ProjectNotificationProfile (@Required Project project) {
		this.project = project;		
		setSprint = true;
		addRole = true;
		onCreateComponent = true;
		onEditComponent = true;
		onDeleteComponent = true;
		addColumn=true;
	    deleteColumn=true;
	    addTaskStatus=true;
		editTaskStatus=true;
		deleteTaskStatus=true;
		deleteProject=true;
		deletedFromProject=true;
		editTaskType=true;
		addReviewer=true;
		deleteReviewer=true;
		addProductRole=true;
	}

	/**
	 * @author Moataz_Mekki this helper method takes the action & returns the
	 *         boolean variable of this action whether it's true or false
	 * @param action
	 *            : the string that describes the action
	 * @return: returns the boolean variable related to the action
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
		else if (action.equalsIgnoreCase("deleteReviewer"))
			return deleteReviewer;
		else if (action.equalsIgnoreCase("addProductRole"))
			return addProductRole;		
		else
			return false;
	}

}
