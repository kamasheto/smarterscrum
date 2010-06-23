package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Component;
import models.Project;
import models.Requestreviewer;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.User;
import play.mvc.With;

/**
 * This is the controller method that renders to views.SprintBacklog It is for
 * viewing the sprint either in a backlog or chart.
 * <p>
 * The first method is for viewing and editing the sprint backlog, the second is
 * for viewing the burn down chart and the third is for assigning a task as
 * impediment.
 * 
 * @author Menna Ghoneim
 */

@With(Secure.class)
// @Check("systemAdmin")
public class SprintBacklog extends SmartController {

	/**
	 * Renders to the sprint backlog view with the list of list of tasks in a
	 * certain sprint are for a certain component, in which each list of list of
	 * tasks encloses tasks in only one story for viewing purposes in the sprint
	 * backlog : 1,4,5,6.
	 * 
	 * @param componentID
	 *            : given compenent id
	 * @param id
	 *            : given sprint id
	 *@param projectId
	 *            the id of a given project
	 */

	public static void index(long componentID, long id) {

		User user = Security.getConnected();
		Sprint sprint = Sprint.findById(id);
		boolean incomp = user.isAdmin;

		Component component = Component.findById(componentID);
		ArrayList daysHeader = new ArrayList(sprint.getDuration());

		long projectId = component.project.id;
		Project project = Project.findById(projectId);
		for (int i = 0; i < sprint.getDuration(); i++) {
			daysHeader.add((i + 1));

		}

		List<Task> tasks = null;
		// for (int i = 0; i < user.roles.size(); i++) {
		// if (user.roles.get(i).project.equals(project)) {
		// incomp = user.roles.get(i).canEditSprintBacklog;
		// break;
		// }
		// }
		incomp = user.in(project).can("editSprintBacklog");
		if (componentID != 0 && id != 0
				&& (user.components.contains(component) || incomp)) {
			tasks = component.returnComponentSprintTasks(sprint);
			incomp = true;

		}
		List<List<Task>> taskOfStory = null;
		if (tasks != null) {
			taskOfStory = new ArrayList<List<Task>>();

			Task t;
			for (int i = 0; i < tasks.size(); i++) {
				t = tasks.get(i);
				List<Task> storyTask;
				if (taskOfStory.isEmpty()) {
					storyTask = new ArrayList<Task>();
					storyTask.add(t);
					taskOfStory.add(storyTask);
				} else {
					int j;
					for (j = 0; j < taskOfStory.size(); j++) {
						storyTask = taskOfStory.get(j);
						if (storyTask.get(0).taskStory == t.taskStory) {
							storyTask.add(t);
							break;
						}
					}
					if (j == taskOfStory.size()) {
						storyTask = new ArrayList<Task>();
						storyTask.add(t);
						taskOfStory.add(storyTask);

					}
				}
			}
		}

		boolean flag = false;
		String pName = project.name;
		String sNum = sprint.sprintNumber;

		render(taskOfStory, flag, user, id, daysHeader, projectId, incomp,
				pName, sNum, componentID, project);

	}

	/**
	 * Renders the burndown chart for a certain sprint and or certain component.
	 * 
	 * @author eabdelrahman
	 * @author Hadeer Younis
	 * @param Sprint
	 *            id
	 * @param cid
	 *            this is the component id
	 * @return String containing the data of the sprint to draw the burn down
	 *         chart
	 */
	public static void showGraph(long id, long componentID) {
		Boolean canSee = false;
		Sprint temp = Sprint.findById(id);
		Security.check(Security.getConnected().projects.contains(temp.project));
		canSee = true;
		String Data = temp.fetchData(componentID);
		if (Data.contains("NONE"))
			Data = null;
		render(Data, temp, componentID, canSee);
	}

	/**
	 * @author menna_ghoneim Renders a given taskid with a list of user and
	 *         opton to say if the reviewer or the assignee is being changed to
	 *         a page to choose a reviewer or assignee
	 * @param taskId
	 *            the task to be edited
	 * @param aORr
	 *            wether reviewer or assignee
	 */

	public static List<User> chooseTaskAssiRev(long taskId, int aORr) {
		List<User> users = new ArrayList<User>();
		Task task = Task.findById(taskId);

		if (aORr == 0) {
			users = task.taskStory.componentID.componentUsers;
			users.remove(task.reviewer);
		} else {

			users = task.taskStory.componentID.componentUsers;
			Project project = task.taskSprint.project;
			List<Requestreviewer> reviewers = new ArrayList<Requestreviewer>();
			for (int i = 0; i < project.components.size(); i++) {

				List<Requestreviewer> compRev = Requestreviewer.find(
						"byComponentAndTypesAndAccepted",
						project.components.get(i), task.taskType, true).fetch();
				reviewers.addAll(compRev);
			}

			if (reviewers == null || reviewers.isEmpty()) {
				users = task.taskStory.componentID.componentUsers;
			} else {
				for (int i = 0; i < reviewers.size(); i++)
					users.add(reviewers.get(i).user);
			}

			users.remove(task.assignee);

			if (users.isEmpty())
				users = task.taskStory.componentID.componentUsers;

		}
		return users;
		// render(taskId, users, aORr);
	}

	/**
	 * @author menna_ghoneim Renders a given taskid with a likt of project types
	 *         and the session's user id to a page to choose
	 * @param taskId
	 *            the task to be edited
	 */

	public static List<TaskType> chooseTaskType(long taskId) {
		Task task = Task.findById(taskId);
		List<TaskType> types = task.taskSprint.project.taskTypes;
		// User user = Security.getConnected();
		return types;

		// render(taskId, types, user);
	}

	/**
	 * @author menna_ghoneim Renders a given taskId with project statuses and
	 *         the user in the session to a page to choose a task status
	 * @param taskId
	 *            the task to be edited
	 */
	public static List<TaskStatus> chooseTaskStatus(long taskId) {
		Task task = Task.findById(taskId);
		List<TaskStatus> states = task.taskSprint.project.taskStatuses;
		// User user = Security.getConnected();

		return states;
		// render(taskId, states, user);
	}
}
