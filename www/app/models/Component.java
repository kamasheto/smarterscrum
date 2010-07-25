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

import play.data.validation.MaxSize;
import play.data.validation.Required;

@Entity
public class Component extends SmartModel {
	/*
	 * Component Entity : consists of name,description up till now
	 */
	@Required
	public String name;

	@Lob
	@Required
	@MaxSize (10000)
	public String description;

	// deletion marker
	public boolean deleted;

	// Relation with Project Entity
	@ManyToOne
	@Required
	public Project project;

	// Relation with User Entity
	@ManyToMany (mappedBy = "components", cascade = CascadeType.ALL)
	public List<User> componentUsers;

	// Relation with Story Entity
	@OneToMany (mappedBy = "componentID", cascade = CascadeType.ALL)
	public List<Story> componentStories;

	// Relation with Meeting Entity
	@ManyToMany (mappedBy = "components", cascade = CascadeType.ALL)
	public List<Meeting> componentMeetings;

	// Relation with Board Entity
	@OneToOne (mappedBy = "component")
	public Board componentBoard;
	
	public int number;

	public Component () {
		componentUsers = new ArrayList<User>();
		componentStories = new ArrayList<Story>();
		componentMeetings = new ArrayList<Meeting>();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * this method takes an input component & returns a list of the users in
	 * this component made for C3 & it's S28
	 * 
	 * @return List of component's users
	 */

	public List<User> getUsers() {
		return componentUsers;
	}

	/**
	 * @author menna_ghoneim Returns a list of tasks associated to a certain
	 *         sprint for a certain component Story: 4,5,6.
	 * @param s
	 *            : given a sprint
	 * @return : List of tasks in this sprint of this component
	 */

	@SuppressWarnings ("null")
	public List<Task> returnComponentSprintTasks(Sprint s) {

		List<Story> stories = this.componentStories;
		int storiesNo = stories.size();
		System.out.println(storiesNo);
		List<Task> tasks = new ArrayList<Task>();

		for (int i = 0; i < storiesNo; i++) {

			Story story = stories.get(i);
			tasks.addAll(story.storiesTask);

		}

		int tasksNo = tasks.size();

		int j = 0;

		for (int i = 0; i < tasksNo; i++) {

			Task task = tasks.get(i - j);
			if (task.taskSprint != s || task.deleted) {
				tasks.remove(task);
				j++;
			}
		}

		return tasks;
	}

	/**
	 * @author Hadeer Diwan Returns a list of tasks associated to a certain
	 *         sprint for a certain component .
	 * @param s
	 *            : given a sprint
	 * @return : List of tasks in this sprint of this component
	 */
	public List<Task> returnComponentTasks(Sprint s) {

		List<Story> stories = this.componentStories;
		int storiesNo = stories.size();
		List<Task> tasks = new ArrayList<Task>();

		for (int i = 0; i < storiesNo; i++) {
			Story story = stories.get(i);
			tasks.addAll(story.storiesTask);
		}

		int tasksNo = tasks.size();
		int j = 0;
		for (int i = 0; i < tasksNo; i++) {
			Task task = tasks.get(i - j);
			if (task.taskSprint != s) {
				tasks.remove(task);
				j++;
			}
		}
		return tasks;
	}

	/**
	 * This method deletes the Component
	 * 
	 * @author Amr Hany
	 * @return boolean varaiable the shows if the component is deleted
	 *         successfully or not
	 */
	public boolean deleteComponent() {
		if (this.deleted == false) {
			this.deleted = true;
			this.save();
			return true;
		}
		return false;

	}

	/**
	 * meeting status method returns the status of the component in attending
	 * the meeting either all invited or confirmed or waiting or declined or not
	 * invited
	 * 
	 * @author Amr Hany
	 * @param meetingID
	 * @return the status of the meeting
	 */

	public String meetingStatus(long meetingID) {
		boolean confirmed = true;
		for (User user : this.componentUsers) {
			if (!user.meetingStatus(meetingID).equals("confirmed")) {
				confirmed = false;
				break;
			}
		}
		if (confirmed)
			return "confirmed";

		boolean waiting = true;
		for (User user : this.componentUsers) {
			if (!user.meetingStatus(meetingID).equals("waiting")) {
				waiting = false;
				break;
			}
		}
		if (waiting)
			return "waiting";

		boolean declined = true;
		for (User user : this.componentUsers) {
			if (!user.meetingStatus(meetingID).equals("declined")) {
				declined = false;
				break;
			}
		}
		if (declined)
			return "declined";

		for (User user : this.componentUsers) {
			if (user.meetingStatus(meetingID).equals("notInvited"))
				return "notInvited";
		}
		return "allInivited";

	}

	public List<Story> getStories() {
		return this.componentStories;
	}

	public void init() {
		componentBoard = new Board(this).save();
		if(this.project==null){
			this.number=1;
		}else{
			this.number = this.project.components.size();
			for ( Component component : this.project.components){
				if(component.number>= this.number && ! component.equals(this)){
				this.number= component.number;
				}
			}
		}
		this.save();
		// board.component = this;?
		// board.save();
	}

	public static class ComponentRowh extends ArrayList<ArrayList<String>> {
		long id;
		String title;

		public ComponentRowh (long id, String title) {
			this.id = id;
			this.title = title;
		}
	}

	public static class ComponentRow extends ArrayList<ArrayList<Task>> {
		long id;
		String title;

		public ComponentRow (long id, String title) {
			this.id = id;
			this.title = title;
		}
	}

}
