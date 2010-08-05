package models;

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
	
	/**
	 * Meeting name
	 */
	@Required
	public String name;

	/**
	 * Meeting description
	 */
	@Lob
	public String description;

	/**
	 * Meeting start time (unix timestamp in milliseconds)
	 */
	@Required
	public long startTime;

	/**
	 * Meeting end time (unix timestamp in milliseconds)
	 */
	@Required
	public long endTime;

	/**
	 * location of this meeting
	 */
	@Required
	public String location;

	/**
	 * Snapshot of this meeting
	 */
	@OneToOne
	public Snapshot snapshot;

	/**
	 * whether or not this meeting is a review log?
	 */
	public boolean isReviewLog;

	/**
	 * The meeting type
	 */
	public String type;

	/**
	 * Whether this meeting is deleted or not
	 */
	public boolean deleted;

	/**
	 * Stauts of this meeting - false if canceled, true otherwise
	 */
	public boolean status;

	/**
	 * Creator of this meeting - a user may create many meetings, a meeting may
	 * only have one user
	 */
	@ManyToOne
	public User creator;

	/**
	 * The sprint of this meeting - a sprint may have many meetings, a meeting
	 * may only have a single sprint
	 */
	@ManyToOne
	public Sprint sprint;

	/**
	 * The project of this meeting - a project may have many meetings, a meeting
	 * may only have a single project
	 */
	@ManyToOne
	public Project project;

	/**
	 * Artifacts associated to this meeting - a meeting may have many artifacts,
	 * an artifact may belong to many meetings
	 */
	@ManyToMany
	public List<Artifact> artifacts;

	/**
	 * Tasks associated to this meeting - a meeting may have many tasks, a task
	 * may belong to many meetings
	 */
	@ManyToMany
	public List<Task> tasks;

	/**
	 * Components associated to this meeting - a meeting may be associated to
	 * many components, a component may be associated to many meetings
	 */
	@ManyToMany
	public List<Component> components;

	/**
	 * Meeting attendees - a meeting may have many meeting attendees, and each
	 * meeting attendee belongs to a single meeting (notice a meeting attendance
	 * contains a single meeting and a single user, and a user may have many
	 * attendees, so its basically a many to many relation with the users)
	 */
	@OneToMany (mappedBy = "meeting", cascade = CascadeType.ALL)
	public List<MeetingAttendance> users;

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
	 * Gets the list of artifacts of a given meeting
	 * 
	 * @return a list of artifacts of a given meeting
	 */
	public List<Artifact> getMeetingArtifacts() {
		return this.artifacts;
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
