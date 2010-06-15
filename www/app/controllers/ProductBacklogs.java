package controllers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import models.Component;
import models.Project;
import models.Sprint;
import models.Story;
import models.User;
import play.mvc.With;

@With (Secure.class)
public class ProductBacklogs extends SmartController {
	/**
	 * Gets the list of stories in the project or the component that has the id
	 * sent from the project or component page , and renders this list with the
	 * index.html file in the ProductBacklogs View
	 * <p>
	 * the boolean variable running says if this project has a running sprint so
	 * as not to allow editing
	 * 
	 * @param id
	 *            this is the id of the Project
	 * @param isComp
	 *            is an int value that is set to 0 if the id is of a project and
	 *            1 if the id is of a component(@author menna_ghoneim)
	 * @see list of user stories
	 */
	public static void index(long id, int isComp) {
		User user = Security.getConnected();
		boolean inproj = user.isAdmin;
		long componentId = -3;
		if (isComp == 0) {
			Project project = Project.findById(id);
			componentId = -1;
			List<Story> stories = new LinkedList<Story>();
			List<Sprint> sprints = project.sprints;
			boolean running = false;
			long isRunning = project.runningSprint();
			for (int i = 0; i < sprints.size(); i++) {
				System.out.println("in loop");
				if (sprints.get(i).id == isRunning)
					running = true;
			}
			for (Component component : project.components) {
				for (Story story : component.componentStories) {

					stories.add(story);
				}
			}
			// for (int i = 0; i < user.roles.size(); i++) {
			// if (user.roles.get(i).project.equals(project)) {
			// inproj = user.roles.get(i).canEditBacklog;
			// break;
			// }
			// }
			inproj = user.in(project).can("editBacklog");
			if (!(project.users.contains(user) || inproj)) {
				stories = null;
				inproj = false;
			} else {
				inproj = true;
			}
			render(stories, project, running, isComp, componentId, inproj);

		} else {
			Component component = Component.findById(id);
			componentId = id;
			List<Story> stories = component.componentStories;
			Project project = component.project;
			List<Sprint> sprints = project.sprints;
			boolean running = false;
			Date date = new Date();
			long currentDate = date.getTime();
			long sprintEnd;
			long sprintStart;
			for (Sprint sprint : sprints) {
				sprintEnd = sprint.endDate.getTime();
				sprintStart = sprint.startDate.getTime();
				if (currentDate >= sprintStart && currentDate <= sprintEnd)
					running = true;
			}
			// for (int i = 0; i < user.roles.size(); i++) {
			// if (user.roles.get(i).project.equals(project)) {
			// inproj = user.roles.get(i).canEditBacklog;
			// break;
			// }
			// }
			inproj = user.in(project).can("editBacklog");
			if (!(user.components.contains(component) || inproj)) {
				stories = null;
				inproj = false;
			} else {
				inproj = true;
			}
			render(stories, project, running, componentId, isComp, inproj);
		}

	}

	/**
	 * @author eabdelrahman
	 * @param Project
	 *            id
	 * @param isComp
	 *            component id
	 * @param c
	 *            this is a paramater passed on from the index page to build
	 *            links.
	 * @return renders the string containg the data and the method of project to
	 *         generate graph and the sprints in it
	 */

	public static void showGraph(long id, long isComp, int c) {
		boolean canSee = false;
		Project temp = Project.findById(id);

		String Data = temp.fetchData(isComp);
		List<Sprint> SprintsInProject = temp.sprints;
		if (Security.getConnected().isAdmin) {
			canSee = true;
		} else if (c == 0) {
			if (Security.getConnected().projects.contains(temp))
				canSee = true;
		} else {
			Component comp = Component.findById(isComp);
			if (Security.getConnected().projects.contains(temp) && comp.componentUsers.contains(Security.getConnected()))
				canSee = true;

		}
		render(Data, SprintsInProject, temp, isComp, c, canSee);

	}
}
