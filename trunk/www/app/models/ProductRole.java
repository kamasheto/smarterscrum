package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Required;

@Entity
public class ProductRole extends SmartModel {
	public boolean deleted;
	@Required
	public String name;
	public String description;
	@ManyToOne
	public Project project;
	@OneToMany (mappedBy = "productRole", cascade = CascadeType.ALL)
	public List<Story> stories;

	/**
	 * This is a Class Constructor Creates a new Product Role object with the
	 * basic product role requirements
	 * 
	 * @author Heba Elsherif
	 * @param projectID
	 *            id of the project of the product role
	 * @param name
	 *            name of the product role
	 * @param description
	 *            description of the product role
	 * @return void
	 * @task C3 S1 & S4
	 * @sprint 1
	 */
	public ProductRole (long projectId, String name, String description) {
		this.deleted = false;
		this.name = name;
		this.description = description;
		this.project = Project.findById(projectId);
		this.stories = new ArrayList<Story>();
	}

	/**
	 * Checks if a certain product role name exists in a certain project .
	 * 
	 * @author Heba Elsherif
	 * @param name
	 *            the name of a product role.
	 *@param projectId
	 *            the project id that the product role belongs to.
	 * @return boolean value indecating if the product role name already exists
	 *         or no.
	 * @task C3 S1 & S2
	 * @sprint 2
	 **/
	public static boolean hasUniqueName(String name, long projectId) {
		Project project = Project.findById(projectId);
		for (int i = 0; i < project.productRoles.size(); i++) {
			if (project.productRoles.get(i).name.equals(name) && !project.productRoles.get(i).deleted) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the product role included in a story that has some tasks in the
	 * current sprint.
	 * 
	 * @author Heba Elsherif
	 * @param void
	 * @return boolean value indecating if the product role included in a story
	 *         that has some tasks in the current sprint.
	 * @task C3 S2 & S3
	 * @sprint 2
	 **/
	public boolean inSprint() {
		Date now = Calendar.getInstance().getTime();
		for (Story story : stories) {
			for (Task task : story.storiesTask) {
				if (task.taskSprint.startDate.before(now) && task.taskSprint.endDate.after(now)) {
					return true;
				}
			}
		}
		return false;
	}
}
