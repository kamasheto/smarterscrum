package models;

// @author ghadafakhry

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.Required;

@Entity
public class Meeting extends SmartModel {
	@Required
	public String name;
	@Lob
	public String description;
	@Required
	public long startTime;
	@Required
	public long endTime;
	@Required
	public String location;

	@OneToOne
	public Snapshot snapshot;
	public boolean isReviewLog;
	public String type;
	public boolean deleted;
	public boolean status;

	@ManyToOne
	public User creator;

	@ManyToOne
	public Sprint sprint;

	@ManyToOne
	public Project project;

	@ManyToMany
	public List<Artifact> artifacts;

	@ManyToMany
	public List<Task> tasks;

	@ManyToMany
	// mapping done in component Model
	public List<Component> components;

	@OneToMany (mappedBy = "meeting", cascade = CascadeType.ALL)
	public List<MeetingAttendance> users;

	public boolean infrontBoard;

	// C5-S19 , to indicate infront of Board Meeting

	/**
	 * Meeting attributes
	 * 
	 * @param name
	 * @param description
	 * @param start
	 * @param end
	 * @param location
	 * @param type
	 * @param project
	 * @param isReviewLog
	 * @param sprint
	 * @param status
	 *            (false canceled, true otherwise )
	 * @param creator
	 */

	public Meeting (String name, User creator, String description, long end, long start, String location, String type, Project project, Sprint sprint) {
		this.name = name;
		this.description = description;
		this.startTime = start;
		this.endTime = end;
		this.location = location;
		this.type = type;
		this.creator = creator;
		this.project = project;
		this.sprint = sprint;
		this.artifacts = new ArrayList<Artifact>();
		this.tasks = new ArrayList<Task>();
		this.users = new ArrayList<MeetingAttendance>();
		this.components = new ArrayList<Component>();
		this.status = true;

	}

	/**
	 * this method override the built in toString method
	 * 
	 * @author Ghada Fakhry
	 * @return String
	 */

	public String toString() {
		return this.name;
	}

	/**
	 * change the value of the attribute delete to true
	 * 
	 * @author Ghada Fakhry
	 */

	// the next 4 methods are not included in this sprint (I added them by
	// mistake) ghadafakhry
	public Meeting addArtifact(Artifact artifact) {
		this.artifacts.add(artifact);
		return this;
	}

	public Meeting addTask(Task task) {
		this.tasks.add(task);
		return this;
	}

	public Meeting addUser(MeetingAttendance user) {
		this.users.add(user);
		return this;
	}

	public Meeting addComponent(Component component) {
		this.components.add(component);
		return this;
	}

	/**
	 * S10, S11 Gets the list of artifacts of a given meeting
	 * 
	 * @return a list of artifacts of a given meeting
	 */
	public List<Artifact> getMeetingArtifacts() {
		return this.artifacts;
	}

	/**
	 * S10, S11 Retrieves a list of tasks of type Sprint Review for a given
	 * meeting
	 * 
	 * @return a list of tasks of a given meeting
	 */

	public List<Task> getMeetingSprintReviewTasks() {
		return this.tasks;
	}

	/**
	 * S10, S11 Gets the attendance of a given meeting
	 * 
	 * @return list of users of type MeetingAttendance for a given meeting
	 * @author Hossam Amer
	 */

	public List<MeetingAttendance> getReviewMeetingAttendingUsers() {
		return this.users;
	}

	/**
	 * Checks whether the given artifacts has notes or not
	 * 
	 * @return the status if there are notes in the given artifacts
	 * @author Hossam Amer
	 */
	public boolean hasNotes() {
		for (int i = 0; i < this.artifacts.size(); i++)
			if (this.artifacts.get(i).type.equals("Notes") && !this.artifacts.get(i).deleted)
				return true;

		return false;
	}

	/**
	 * Checks whether the given attendees are confirmed or not
	 * 
	 * @return the status if there are attendees in the given meeting
	 * @author Hossam Amer
	 */

	public boolean hasAttendees() {
		for (int i = 0; i < this.users.size(); i++)
			if (this.users.get(i).status.equals("confirmed") && !this.users.get(i).deleted)
				return true;

		return false;
	}

	/**
	 * Gets all the artifacts of type notes
	 * 
	 * @return a list of artifacts of type notes
	 * @author Hossam Amer
	 */

	public List<Artifact> getArtifactOfTypeNotes() {
		List<Artifact> tmpArtifactList = new ArrayList<Artifact>();

		try {
			for (int i = 0; i < this.artifacts.size(); i++)
				if (this.artifacts.get(i).type.equals("Notes") && !this.artifacts.get(i).deleted)
					tmpArtifactList.add(this.artifacts.get(i));

			return tmpArtifactList;
		}

		catch (NullPointerException e) {
			return null;
		}

	}
}
