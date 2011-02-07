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

/**
 * Product role in a project
 */
@Entity
public class ProductRole extends SmartModel {
	
	/**
	 * Whether this product role is deleted
	 * 
	 * @deprecated
	 */
	public boolean deleted;

	/**
	 * Name of this product role, eg: TA
	 */
	@Required
	public String name;

	/**
	 * Description of this product role
	 */
	public String description;

	/**
	 * Project of this role (one role > one project, one project > many roles)
	 */
	@ManyToOne
	public Project project;

	/**
	 * Tasks that use this role (one role > many tasks, one task > one role)
	 */
	@OneToMany (mappedBy = "productRole", cascade = CascadeType.ALL)
	public List<Task> Tasks;

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
	 */
	public ProductRole (long projectId, String name, String description) {
		this.deleted = false;
		this.name = name;
		this.description = description;
		this.project = Project.findById(projectId);
		this.Tasks = new ArrayList<Task>();
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
	 **/
	public static boolean hasUniqueName(String name, long projectId) {
		Project project = Project.findById(projectId);
		for (int i = 0; i < project.productRoles.size(); i++) {
			if (project.productRoles.get(i).name.equalsIgnoreCase(name) && !project.productRoles.get(i).deleted) {
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
	 * @return boolean value indecating if the product role included in a story
	 *         that has some tasks in the current sprint.
	 **/
	public boolean inSprint() {
		Date now = Calendar.getInstance().getTime();
		for (Task task : Tasks) {
			if (task.sprint.startDate.before(now) && task.sprint.endDate.after(now)) {
				return true;
			}
			for (Task task2 : task.subTasks) {
				if (task2.sprint.startDate.before(now) && task.sprint.endDate.after(now)) {
					return true;
				}
			}
		}
		return false;
	}
}
