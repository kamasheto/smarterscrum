package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Component;
import models.Project;
import models.Sprint;
import models.Story;
import models.Task;
import play.mvc.Controller;
import play.mvc.With;

@With (Secure.class)
// @Check("systemAdmin")
public class ImpedimentTasks extends Controller {
	/**
	 * @author ahmedkhaled7 C4 S12 Rendering the project to the index page in
	 *         the Impediment task view
	 * @param projectId
	 *            Is the pid of the project we are in
	 */
	public static void index(long projectId) {
		Project project = Project.findById(projectId);
		render(project);
	}

	/**
	 * @author ahmedkhaled7 C4 S12 Sending the description entered by the user
	 *         and the project we are in , to the Task constructor for the
	 *         impediment task to be created. Rendering the task id to the page,
	 *         to be sent to the selectDependentTasks page in the same view.
	 * @param description
	 *            Is the description of the impediment task
	 * @param projectId
	 *            Is the pid of the project we are in
	 */
	public static void save(String description, long projectId) {

		Project project = Project.findById(projectId);
		Task impedimentTask = new Task(description, project).save();
		// impedimentTask.dependentTasks=dTasks;
		// Logs.notifyUsers(task.reporter.email, "task is impediment",
		// "task is impediment", assignee);
		impedimentTask.reporter = Security.getConnected();

		impedimentTask.save();

		long sid;
		if (project.runningSprint() != -1)
			sid = project.runningSprint();
		else
			sid = project.sprints.get(project.sprints.size() - 1).id;
		Sprint s = Sprint.findById(sid);

		impedimentTask.taskSprint = s;
		impedimentTask.save();

		long id = impedimentTask.id;

		renderJSON(id);
	}

	/**
	 * @author ahmedkhaled7 C4 S12 Selecting the tasks that the impediment task
	 *         depends on.
	 * @param projectId
	 *            Is the pid of the project we are in
	 * @param itaskId
	 *            Is the impediment task is
	 */
	public static void selectDependentTasks(long projectId, long itaskId) {
		Project project = Project.findById(projectId);

		List<Task> Tasks = new ArrayList<Task>();
		List<Long> taskIds = new ArrayList<Long>();

		for (Component component : project.components) {
			for (Story story : component.componentStories) {
				for (Task task : story.storiesTask) {
					Tasks.add(task);
					taskIds.add(task.id);
				}
			}
		}

		render(itaskId, Tasks, taskIds, projectId, project);
	}

	/**
	 * @author ahmedkhaled7 C4 S12Adding the dependent task to the impediment
	 *         task and saving them.
	 * @param itaskId
	 *            Is the impediment task is
	 * @param dTasks
	 *            list of dependent tasks
	 */
	public static void save2(long taskId, long[] dTasks) {

		Task impedimentTask = Task.findById(taskId);

		// System.out.println(dTasks[0]);
		for (int i = 0; i < dTasks.length; i++) {
			Task n = Task.findById(dTasks[i]);
			impedimentTask.dependentTasks.add(n);

		}
		// Logs.notifyUsers(task.reporter.email, "task is impediment",
		// "task is impediment", assignee);

		impedimentTask.save();
		Task j = Task.findById(taskId);

		Sprint s = Sprint.findById(j.taskSprint.id);
		Project project = impedimentTask.taskSprint.project;
		Notifications.notifyUsers(project, "Impediment reported", impedimentTask.description, "reportImpediment", (byte) -1);

		Logs.addLog(project, "added", "Task", impedimentTask.id);

	}

}
