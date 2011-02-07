package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import play.data.validation.MaxSize;
import play.data.validation.Required;

/**
 * @author menna_ghoneim
 * @author Hossam Amer
 */

@Entity
public class Artifact extends SmartModel {
	
	/***
	 * Artifact type
	 */
	@Required
	public String type;

	/***
	 * Artifact description
	 */
	@Lob
	@Required
	@MaxSize (300)
	public String description;

	/***
	 * one meeting can have many artifacts & an artifact can belong to many meetings
	 */
	@ManyToMany (mappedBy = "artifacts")
	public List<Meeting> meetings_artifacts;

	/***
	 * flag to determine whether this artifact is deleted or not
	 */
	public boolean deleted;

	/***
	 * Artifact constructor
	 */
	public Artifact () {
		meetings_artifacts = new ArrayList<Meeting>();
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

	public Artifact (String type, String description) {
		this();
		this.type = type;
		this.description = description;
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
		List<Artifact> project_artifacts = new ArrayList<Artifact>();
		Project project = Project.findById(projectID);
		List<Meeting> meetings = this.meetings_artifacts;

		for (Meeting meeting : meetings) {
			if (meeting.project == project) {
				List<Artifact> meeting_artifacts = meeting.artifacts;

				for (Artifact artifact : meeting_artifacts) {
					project_artifacts.add(artifact);
				}
			}
		}

		return project_artifacts;
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
