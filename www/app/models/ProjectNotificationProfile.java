package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.data.validation.Required;

/**
 * @author moataz_mekki
 */

@Entity
public class ProjectNotificationProfile extends SmartModel {
	@OneToOne
	public Project project;
	public boolean deleted;
	public boolean setMeeting;
	public boolean setSprint;
	public boolean addRole;
	public boolean addProductRole;
	public boolean editProductRole;
	public boolean deleteProductRole;
	public boolean reportImpediment;
	public boolean swapColumns;
	public boolean editColumnPosition;
	public boolean renameColumn;
	public boolean onCreateComponent;
	public boolean onEditComponent;
	public boolean onDeleteComponent;
	public boolean addColumn;
	public boolean deleteColumn;
	public boolean assignStoryToSprint;
	public boolean RequestToBeReviewer;
	public boolean AcceptToBeReviewerRequest;
	public boolean RejectToBeReviewerRequest;

	/**
	 * the constructor that's responsible for adding this profile to the DB
	 * 
	 * @param project
	 *            : the project that this notification profile belongs to
	 */
	public ProjectNotificationProfile (@Required Project project) {
		this.project = project;
		setMeeting = true;
		setSprint = true;
		addRole = true;
		addProductRole = true;
		editProductRole = true;
		deleteProductRole = true;
		swapColumns = true;
		editColumnPosition = true;
		renameColumn = true;
		reportImpediment = true;
		onCreateComponent = true;
		onEditComponent = true;
		onDeleteComponent = true;
		addColumn=true;
	    deleteColumn=true;
	    assignStoryToSprint=true;
		RequestToBeReviewer=true;
		AcceptToBeReviewerRequest=true;
		RejectToBeReviewerRequest=true;
	}

	/**
	 * @author Moataz_Mekki this helper method takes the action & returns the
	 *         boolean variable of this action whether it's true or false
	 * @param action
	 *            : the string that describes the action
	 * @return: returns the boolean variable related to the action
	 */
	public boolean checkAction(String action) {
		if (action.equalsIgnoreCase("setMeeting"))
			return setMeeting;
		else if (action.equalsIgnoreCase("setSprint"))
			return setSprint;
		else if (action.equalsIgnoreCase("addRole"))
			return addRole;
		else if (action.equalsIgnoreCase("addProductRole"))
			return addProductRole;
		else if (action.equalsIgnoreCase("editProductRole"))
			return editProductRole;
		else if (action.equalsIgnoreCase("deleteProductRole"))
			return deleteProductRole;
		else if (action.equalsIgnoreCase("swapColumns"))
			return swapColumns;
		else if (action.equalsIgnoreCase("renameColumn"))
			return renameColumn;
		else if (action.equalsIgnoreCase("editColumnPosition"))
			return editColumnPosition;
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
		else if (action.equalsIgnoreCase("RequestToBeReviewer"))
			return RequestToBeReviewer;
		else if (action.equalsIgnoreCase("AcceptToBeReviewerRequest"))
			return AcceptToBeReviewerRequest;
		else if (action.equalsIgnoreCase("RejectToBeReviewerRequest"))
			return RejectToBeReviewerRequest;
		else			
			return false;
	}

}
