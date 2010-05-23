package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Task extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Required
	@Lob
	@MaxSize (300)
	public String description;

	public boolean deleted;

	public String type;

	@Required
	public double estimationPoints;

	// ADDED BY HADEER YOUNIS. DO NOT REMOVE.
	public ArrayList<Double> estimationPointsPerDay;

	@ManyToOne
	public Story taskStory;

	@ManyToOne
	public User userTask;

	@ManyToMany (mappedBy = "tasks")
	public List<Meeting> meeting;

	@Required
	@OneToOne
	public User assignee;

	@OneToOne
	public User reporter;

	@Required
	@OneToOne
	public User reviewer;

	@ManyToMany
	public List<Task> dependentTasks;

	public int status;

	@ManyToOne
	public TaskStatus taskStatus;

	@ManyToOne
	public TaskType taskType;

	@ManyToOne
	public Sprint taskSprint;

	// @ManyToOne
	// public Column Status_on_Board;

	// @ManyToMany
	// public List<Day> taskDays;

	/**
	 * Returns the effort points of a specific task in a specific day.
	 * 
	 * @author Hadeer Younis
	 * @category C4S8 and C4S9
	 * @param dayId
	 *            An Integer used to find a specific day by it's ID.
	 * @return The number of effort points for this task in a specific day.
	 */
	public double getEffortPerDay(int dayId) {
		if (estimationPointsPerDay.size() == 0)
			return estimationPoints;
		if (dayId >= estimationPointsPerDay.size())
			return estimationPointsPerDay.get(estimationPointsPerDay.size() - 1);
		return estimationPointsPerDay.get(dayId);
	}

	/**
	 * Sets the effort points of a specific task in a specific day.
	 * 
	 * @author Hadeer Younis
	 * @category C4S1
	 * @param day
	 *            An Integer used to find a specific day by it's ID.
	 * @param effort
	 *            It is the number of effort point for the corresponding day.
	 */
	public void setEffortOfDay(double effort, int day) {
		if (estimationPointsPerDay.size() == 0 && day == 0) {
			estimationPointsPerDay.add(effort);
		} else if (estimationPointsPerDay.size() == 0 && day > 0) {
			while (estimationPointsPerDay.size() < day)
				estimationPointsPerDay.add(effort);
		}
		if (estimationPointsPerDay.size() <= day) {
			double temp = estimationPointsPerDay.get(estimationPointsPerDay.size() - 1);
			for (int i = estimationPointsPerDay.size() - 1; i < day; i++) {
				estimationPointsPerDay.add(temp);
			}
		}
		estimationPointsPerDay.set(day, effort);
	}

	public Task () {
		meeting = new ArrayList<Meeting>();
		dependentTasks = new ArrayList<Task>();
		this.estimationPointsPerDay = new ArrayList<Double>(1);
	}

	public Task (String des, boolean deleted, String type, double estimationPoints, int status) {
		this();
		this.description = des;
		this.deleted = false;
		this.type = type;
		this.status = status;
		this.estimationPoints = estimationPoints;
		this.estimationPointsPerDay = new ArrayList<Double>(1);
		this.status = status;
		this.save();
	}

	public Task (String des, Project project) {
		this();
		this.description = des;
		this.deleted = false;

		this.taskType = new TaskType();
		this.taskType.name = "Impediment";
		this.taskType.save();
		this.taskType.project = project;

		this.dependentTasks = new ArrayList<Task>();
		this.taskStatus = new TaskStatus();

		this.estimationPointsPerDay = new ArrayList<Double>(1);
		this.taskStatus = new TaskStatus().save();

		this.taskStatus.name = "New";
		this.taskStatus.save();
		this.taskStatus.project = project;

		this.estimationPoints = 0.0;

	}

	/**
	 * This method checks if a Task is Under Implementation Story 37 Component 3
	 * 
	 * @author Monayri
	 * @param void
	 * @return boolean
	 */
	public boolean checkUnderImpl() {
		Sprint taskSprint = this.taskSprint;
		if (taskSprint != null) {
			Date Start = taskSprint.startDate;
			Date End = taskSprint.endDate;
			Calendar cal = new GregorianCalendar();
			if (Start.before(cal.getTime()) && End.after(cal.getTime()))
				return true;
		}

		return false;
	}

	/**
	 * A Method that deletes a Task
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return its a void method.
	 */
	public void DeleteTask() {
		Project project = taskStory.componentID.project;
		List<Component> components = project.components;

		for (Component component : components) {
			for (Story story : component.componentStories) {
				if (story.dependentStories.contains(this.taskStory)) {
					for (Task task : story.storiesTask) {
						if (task.dependentTasks.contains(this)) {
							task.dependentTasks.remove(this);
						}
					}
				}
			}
		}
	}

	/**
	 * A Method that checks whether the Task has dependencies hence deletable.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return it return a boolean variable that represents whether the task is
	 *         deletable.
	 */
	public boolean isDeletable() {
		Project project = taskStory.componentID.project;
		List<Component> components = project.components;

		for (Component component : components) {
			for (Story story : component.componentStories) {
				if (story.dependentStories.contains(this.taskStory)) {
					for (Task task : story.storiesTask) {
						if (task.dependentTasks.contains(this)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public static class Object {

		long id;

		String description;

		public Object (long id, String description) {
			this.id = id;
			this.description = description;
		}
	}

	@Override
	public String toString() {
		return this.description;
	}

}
