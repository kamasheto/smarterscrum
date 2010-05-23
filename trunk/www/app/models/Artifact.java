package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author menna_ghoneim
 * @author Hossam Amer
 */

@Entity
public class Artifact extends Model {
	@Required
	public String type;

	@Lob
	@Required
	@MaxSize (300)
	public String description;

	@ManyToMany (mappedBy = "artifacts")
	public List<Meeting> meetingsArtifacts;

	public boolean deleted;

	public Artifact () {
		meetingsArtifacts = new ArrayList<Meeting>();
	}

	/**
	 * Class constructor specifying:
	 * 
	 * @param type
	 *            String has the type of the artifact
	 * @param des
	 *            String containing the description of that artifact
	 * @param deleted
	 *            the status of the artifact either deleted or not
	 */

	public Artifact (String type, String des) {
		this();
		this.type = type;
		this.description = des;
		deleted = false;

	}

	/**
	 * Removes the artifact from the database by changing its status
	 */

	public void remove() {
		this.deleted = true;
	}

	/**
	 * Gets the artifacts of a certain project
	 * <p>
	 * Gets all the meeting in the database and then
	 * <p>
	 * checks if the project of that meeting is equal to the given project
	 * <p>
	 * Retrieves back all the artifacts of that meeting
	 * 
	 * @param projectID
	 *            the id of a given project
	 * @return a list of all artifacts of a given project
	 * @author Hossam Amer
	 */

	public List<Artifact> getArtifacts(long projectID) {
		List<Artifact> artifactsOfCertainProject = new ArrayList<Artifact>();
		Project projectTmp = Project.findById(projectID);
		List<Meeting> allMeetings = this.meetingsArtifacts;

		for (Meeting tmp : allMeetings) {
			if (tmp.project == projectTmp) {
				List<Artifact> artifactsOfTmp = tmp.artifacts;

				for (Artifact a : artifactsOfTmp) {
					artifactsOfCertainProject.add(a);
				}
			}
		}

		return artifactsOfCertainProject;
	}

	/**
	 * Checks if the given artifact of type Notes or not
	 * 
	 * @author Hossam Amer
	 * @return true if it is of type notes and false otherwise
	 */
	public boolean checkType() {
		return this.type.equals("Notes") ? true : false;
	}
}
