package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import models.Board;
import models.Component;
import models.Meeting;
import models.Project;
import models.Request;
import models.Role;
import models.Sprint;
import models.Story;
import models.Task;
import models.User;
import play.mvc.With;

@With (Secure.class)
public class Show extends SmartController {

	public static void roles(long id) {
		Project project = Project.findById(id);
		List<Role> roles = null;
		if (project == null) {
			Security.check(Security.getConnected().isAdmin);
			roles = Role.find("byProjectIsNull").fetch();
		} else {
			Security.check(project, "manageRoles");
			roles = project.roles;
		}
		render(project, roles);
	}

	public static void index() {
		users(0);
	}

	public static void users(int page) {
		if (page < 0) {
			page = 0;
		}
		List<User> users = User.find("byDeleted", false).from(page * 20).fetch(20);
		long total = User.count();
		render(users, page, total);
	}

	public static void projects(int page) {
		List<Project> projects = Project.find("byDeleted", false).from(page * 20).fetch(20);
		long total = Project.count();
		render(projects, page, total);
	}

	public static void boards() {
		List<Board> boards = Board.find("byDeleted", false).fetch();
		render(boards);
	}

	/**
	 * Show user profile
	 * 
	 * @param id
	 *            user id
	 */
	public static void user(long id) {
		User user = User.findById(id);
		if (user == null || user.deleted) {
			notFound();
		}
		User me = Security.getConnected();
		List<Project> myProjects = new LinkedList<Project>();
		for (Project project : Project.<Project> findAll()) {
			if (me.in(project).can("invite"))
				myProjects.add(project);
		}
		render(user, myProjects, me);
	}

	public static void project(long id) {
		Project project = Project.findById(id);
		List<Request> requests = Request.find("byUser", Security.getConnected()).fetch();
		List<Role> requestedRoles = new LinkedList<Role>();
		for (Request request : requests) {
			requestedRoles.add(request.role);
		}
		List<User> members = project.users;
		int memberscount = members.size();
		List<Sprint> sprints = project.sprints;
		int sprintscount = sprints.size();
		int taskscount = 0;
		for (int i = 0; i < sprintscount; i++) {
			List<Task> tasks = sprints.get(i).tasks;
			taskscount += tasks.size();
		}
		List<Meeting> meetings = project.meetings;
		int meetingscount = meetings.size();
		List<Component> components = project.components;
		int componentscount = components.size();
		int storiescount = 0;
		for (int i = 0; i < componentscount; i++) {
			List<Story> stories = components.get(i).componentStories;
			storiescount += stories.size();
		}
		User connectedUser = Security.getConnected();
		render(storiescount, taskscount, componentscount, project, meetingscount, requestedRoles, memberscount, sprintscount, connectedUser);
	}

	public static void tasks(long id) {
		Project project = Project.findById(id);
		List<Task> tasks = new ArrayList<Task>();
		List<models.Component> components = project.components;
		for (models.Component C : components) {
			for (Story S : C.componentStories) {
				List<Task> tasks2 = Task.find("byTaskStoryAndDeleted", S, false).fetch();
				tasks.addAll(tasks2);
			}

		}

		render(project, tasks);
	}

	/**
	 * this method takes the id of the project to be deleted and sets the
	 * deleted attribute to true and then sends notification e-mail to the
	 * project members that the project has been deleted Also,When the project
	 * is deleted the associated Meetings,Sprints,Components,Roles,
	 * ,Board,ChatRoom are deleted (deletion marker set to true).
	 * 
	 * @Author Ghada Fakhry
	 * @param id
	 */
	public static void deleteProject(long id) {
		Project project = Project.findById(id);
		Security.check(Security.getConnected().in(project).can("deleteproject"));
		// if (Security.getConnected().in(project).can("deleteproject")) {
		project.deleted = true;

		project.board.deleted = true;
		project.board.save();

		project.chatroom.deleted = true;
		project.chatroom.save();

		for (Meeting temp : project.meetings) {
			temp.deleted = true;
			temp.save();
		}

		for (Component temp : project.components) {
			temp.deleted = true;
			temp.save();
		}

		for (Sprint temp : project.sprints) {
			temp.deleted = true;
			temp.save();
		}

		for (Request temp : project.requests) {
			temp.deleted = true;
			temp.save();
		}

		project.save();
		String body = "Please note that the project " + project.name + " has been deleted and all upcoming meetings and events are cancelled !";
		String header = project.name + " deletion notification";
		List<User> projectMembers = project.users;
		Notifications.notifyUsers(projectMembers, header, body, (byte) -1);
		Logs.addLog(Security.getConnected(), "Deleted Project", "project", id, project, new Date());
		// } else {
		// forbidden();
		// }
	}
	
	public static void role(long id) {
		Role role = Role.findById(id);
		render(role);
	}
}
