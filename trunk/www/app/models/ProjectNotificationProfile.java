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
	public boolean reportImpediment;	
	public boolean onCreateComponent;
	public boolean onEditComponent;
	public boolean onDeleteComponent;
	public boolean addColumn;
	public boolean deleteColumn;
	public boolean assignStoryToSprint;	
	public boolean addTaskStatus;
	public boolean editTaskStatus;
	public boolean deleteTaskStatus;
	public boolean deleteProject;
	public boolean deletedFromProject;
	public boolean editTaskType;

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
		reportImpediment = true;
		onCreateComponent = true;
		onEditComponent = true;
		onDeleteComponent = true;
		addColumn=true;
	    deleteColumn=true;
	    assignStoryToSprint=true;
	    addTaskStatus=true;
		editTaskStatus=true;
		deleteTaskStatus=true;
		deleteProject=true;
		deletedFromProject=true;
		editTaskType=true;
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
		else if (action.equalsIgnoreCase("reportImpediment"))
			return reportImpediment;
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
		else if (action.equalsIgnoreCase("assignStoryToSprint"))
			return assignStoryToSprint;
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
		else
			return false;
	}

}
