package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import models.Board;
import models.Project;
import models.Request;
import models.Role;
import models.Story;
import models.Task;
import models.User;
import play.mvc.With;

@With (Secure.class)
public class Show extends SmartController {

	@Check ("canManageRoles")
	public static void roles(long id) {
		Project project = Project.findById(id);
		render(project);
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
		if (me.isAdmin)
			myProjects = Project.findAll();
		else
			for (Project project : me.projects) {
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
		User connectedUser = Security.getConnected();
		render(project, requestedRoles, connectedUser);
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
	 * this method takes the id of the project to be deleted and sets the deleted attribute to true and then 
	 * sends notification e-mail to the project members that the project has been deleted
	 * 
	 * @Author Ghada Fakhry
	 * @param id
	 */
	public static void deleteProject(long id) {
		Project project = Project.findById(id);
		if( Security.getConnected().in( project ).can( "deleteproject" ) )
		{
			project.deleted = true;
			project.save();
			String body = "Please note that the project " + project.name + " has been deleted and all upcoming meetings and events are cancelled !";
			String header = project.name + " deletion notification";
			List<User> projectMembers = project.users;
			Notifications.notifyUsers(projectMembers, header, body, (byte) -1);
			Logs.addLog(Security.getConnected(), "Deleted Project", "project", id, project, new Date());	
		}
		else
		{
			forbidden();
		}
	}
}
