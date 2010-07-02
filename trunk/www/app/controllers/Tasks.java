package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import models.Board;
import models.Column;
import models.Component;
import models.Log;
import models.Project;
import models.Requestreviewer;
import models.Sprint;
import models.Story;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With(Secure.class)
public class Tasks extends SmartCRUD {

	public static void add() {
		render();
	}

	/**
	 * A Method that renders the form of creating a Task.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	//@Check("canAddTask")
	public static void blank(long id, long id2) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		User user = Security.getConnected();
		Component component = Component.findById(id);
		Story taskStory = Story.findById(id2);
		Security.check(user.in(component.project).can("canAddTask"));
		List<Requestreviewer> reviewers = Requestreviewer.find(
				"byComponentAndAccepted", component, true).fetch();
		Story story = null;
		boolean storyChosen = true;
		if (taskStory != null)
			storyChosen = false;
		List<Story> stories = component.componentStories;
		System.out.println(stories.size());
		if (stories.size() != 0)
			story = stories.get(0);
		List<User> users = component.componentUsers;
		List<TaskStatus> statuses = component.project.taskStatuses;
		List<TaskType> types = component.project.taskTypes;
		List<Sprint> sprints = new ArrayList<Sprint>();
		List<Task> tasks = new ArrayList<Task>();
		if (story != null) {
			List<Story> depend = story.dependentStories;
			for (Story story2 : depend) {
				for (Task task2 : story2.storiesTask) {
					tasks.add(task2);
				}

			}
		}
		for (int i = 0; i < component.project.sprints.size(); i++) {
			Sprint sprint = component.project.sprints.get(i);
			java.util.Date End = sprint.endDate;
			Calendar cal = new GregorianCalendar();
			if (End.after(cal.getTime())) {
				sprints.add(sprint);
			}

		}

		try {
			render(type, component, stories, users, statuses, types, sprints,
					story, tasks, reviewers, taskStory, storyChosen);

		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}

	}

	/**
	 * A Method that Creates a Task and checks the validation of inputs by users
	 * in the create form.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.entityClass.newInstance();
		validation.valid(object.edit("object", params));
		String message = "";
		Task tmp = (Task) object;
		Story taskStory = tmp.taskStory;
		if (tmp.taskStory == null) {
			message = "A Task must have a story";
			try {
				render(request.controller.replace(".", "/") + "/blank.html",
						type, message);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}
		List<Story> stories = tmp.taskStory.componentID.componentStories;
		List<User> users = tmp.taskStory.componentID.componentUsers;
		List<TaskStatus> statuses = tmp.taskStory.componentID.project.taskStatuses;
		List<TaskType> types = tmp.taskStory.componentID.project.taskTypes;
		List<Sprint> sprints = new ArrayList<Sprint>();
		Component component = tmp.taskStory.componentID;
		List<Requestreviewer> reviewers = Requestreviewer.find(
				"byComponentAndAccepted", tmp.taskStory.componentID, true)
				.fetch();
		for (int i = 0; i < tmp.taskStory.componentID.project.sprints.size(); i++) {
			Sprint sprint = tmp.taskStory.componentID.project.sprints.get(i);
			java.util.Date End = sprint.endDate;
			Calendar cal = new GregorianCalendar();
			if (End.after(cal.getTime())) {
				sprints.add(sprint);
			}

		}

		if (validation.hasErrors()) {
			if (tmp.description.equals("")) {
				message = "A Task must have a description";
			} else if (tmp.estimationPoints == 0) {
				message = "Please enter an estimation greater than Zero";

			} else if (tmp.assignee == null || tmp.reviewer == null) {
				message = "A task must have an assignee and a reviewer";
				try {
					render(
							request.controller.replace(".", "/")
									+ "/blank.html", component, type, stories,
							users, statuses, types, message, sprints,
							reviewers, taskStory);
				} catch (TemplateNotFoundException e) {
					render("CRUD/blank.html", type);
				}
			} else if (tmp.assignee.equals(tmp.reviewer)) {
				message = "A task can't have the same user as an assignee and reviewer";
			}
			try {
				render(request.controller.replace(".", "/") + "/blank.html",
						component, type, stories, users, statuses, types,
						message, sprints, reviewers, taskStory);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}
		if (tmp.estimationPoints == 0) {
			message = "Please enter an estimation greater than Zero";
			try {
				render(request.controller.replace(".", "/") + "/blank.html",
						component, type, stories, users, statuses, types,
						message, sprints, reviewers, taskStory);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else if (tmp.assignee == null || tmp.reviewer == null) {
			message = "A task must have an assignee and a reviewer";
			try {
				render(request.controller.replace(".", "/") + "/blank.html",
						component, type, stories, users, statuses, types,
						message, sprints, reviewers, taskStory);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else if (tmp.assignee.equals(tmp.reviewer)) {
			message = "A task can't have the same user as an assignee and reviewer";
			try {
				render(request.controller.replace(".", "/") + "/blank.html",
						component, type, stories, users, statuses, types,
						message, sprints, taskStory);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else if (tmp.estimationPoints > 100) {
			message = "An estimation point greater than 100 is a total nonsense !";
			try {
				render(request.controller.replace(".", "/") + "/blank.html",
						component, type, stories, users, statuses, types,
						message, sprints, taskStory);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}

		tmp.reporter = Security.getConnected();
		System.out.println("here");
		object.save();
		tmp = (Task) object;
		Calendar cal = new GregorianCalendar();
		Logs.addLog(tmp.reporter, "Create", "Task", tmp.id, tmp.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		String header = "A new Task has been added to Story: 'S" + tmp.taskStory.id + "\'" + ".";
		String body = "In Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + "." + '\n' 
		            + " In Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + "." + '\n' 
					+ " Task: 'T" +  tmp.id + "\'" + "." + '\n'  
					+ " Added by: " + "\'" + tmp.reporter.name + "\'" + ".";		
		
		/*////Long Informative Notification message. Not suitable for online notification.
		String header = "New Task has been added to Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + " in Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + ".";
		String body = "New Task has been added to Component: " + "\'" + tmp.taskStory.componentID.name + "\'" 
				    + " in Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + "." + '\n' + '\n' 
					+ "Task Description: " +  tmp.description + "." + '\n' 
					+ " Story: " + tmp.taskStory.description + "." + '\n' 
					+ " Type: " + tmp.taskType.name + "." + '\n' 
					+ " Status: " + tmp.taskStatus.name+ "." + '\n'  
					+ " Assignee: " + tmp.assignee.name + "." + '\n' 
					+ " Reporter: " + tmp.reporter.name + "." + '\n' 
					+ " Reviewer: " + tmp.reviewer.name + "." + '\n' 
					+ " Added by: " + tmp.reporter.name + ".";*/
		Notifications.notifyUsers(tmp.taskStory.componentID.componentUsers, header, body, (byte) 1);
		// tmp.init();
		flash.success(Messages.get("crud.created", type.modelName, object
				.getEntityId()));
		if (params.get("_save") != null) {
			redirect("/storys/liststoriesinproject?projectId="
					+ tmp.taskStory.componentID.project.id + "&storyId="
					+ tmp.taskStory.id);
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * A Method that renders the form of editting a Task.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	public static void show(String id, int id2) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Task tmp = (Task) object;
		System.out.println(tmp.dependentTasks);
		List<User> users = tmp.taskStory.componentID.componentUsers;
		List<TaskStatus> statuses = tmp.taskStory.componentID.project.taskStatuses;
		List<TaskType> types = tmp.taskStory.componentID.project.taskTypes;
		List<Task> dependencies = new ArrayList<Task>();
		String message2 = "Are you Sure you want to delete the task ?!";
		List<Requestreviewer> reviewers = Requestreviewer.find(
				"byComponentAndAccepted", tmp.taskStory.componentID, true)
				.fetch();
		System.out.println(tmp.taskStory.componentID.name);
		boolean deletable = tmp.isDeletable();
		User user = Security.getConnected();
		boolean pAdmin=false;
		for(int i = 0 ; i<user.roles.size() ; i++)
		{
			if(user.roles.get(i).project.id == tmp.taskStory.componentID.project.id && user.roles.get(i).name.equalsIgnoreCase("project Admin"))
				pAdmin=true;			
		}
		boolean isReporter = tmp.reporter.equals(user)|| pAdmin || user.isAdmin;
		boolean isAssignee = tmp.assignee.equals(user)|| pAdmin || user.isAdmin;
		boolean isReviewer = tmp.assignee.equals(user)|| pAdmin || user.isAdmin;
		for (int i = 0; i < tmp.taskStory.dependentStories.size(); i++) {
			for (int j = 0; j < tmp.taskStory.dependentStories.get(i).storiesTask
					.size(); j++) {
				dependencies
						.add(tmp.taskStory.dependentStories.get(i).storiesTask
								.get(j));
			}
		}

		try {
			render(type, object, users, statuses, types, dependencies,
					message2, deletable, reviewers, id2, isReporter, isAssignee, isReviewer);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	/**
	 * A Method that checks the validation of input data done by user in the
	 * edit Task form, if its correct it saves the changes.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Task tmp = (Task) object;
		String oldDescription = tmp.description;
		validation.valid(object.edit("object", params));
		List<User> users = tmp.taskStory.componentID.componentUsers;
		List<TaskStatus> statuses = tmp.taskStory.componentID.project.taskStatuses;
		List<TaskType> types = tmp.taskStory.componentID.project.taskTypes;
		User myUser = Security.getConnected();
		List<Requestreviewer> reviewers = Requestreviewer.find(
				"byComponentAndAccepted", tmp.taskStory.componentID, true)
				.fetch();
		List<Task> dependencies = new ArrayList<Task>();
		String message = "";
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		boolean pAdmin=false;
		for(int i = 0 ; i<myUser.roles.size() ; i++)
		{
			if(myUser.roles.get(i).project.id == tmp.taskStory.componentID.project.id && myUser.roles.get(i).name.equalsIgnoreCase("project Admin"))
				pAdmin=true;			
		}
		boolean isReporter = tmp.reporter.equals(myUser)|| pAdmin || myUser.isAdmin;
		boolean isAssignee = tmp.assignee.equals(myUser)|| pAdmin || myUser.isAdmin;
		boolean isReviewer = tmp.assignee.equals(myUser)|| pAdmin || myUser.isAdmin;
		for (int i = 0; i < tmp.taskStory.dependentStories.size(); i++) {
			for (int j = 0; j < tmp.taskStory.dependentStories.get(i).storiesTask
					.size(); j++) {
				dependencies
						.add(tmp.taskStory.dependentStories.get(i).storiesTask
								.get(j));
			}
		}
		if (validation.hasErrors()) {
			if (tmp.description.equals("") || tmp.description.equals(null)) {
				message = "A Task must have a description";
			} else if (tmp.estimationPoints == 0) {
				message = "Please enter an estimation greater than Zero";

			} else if (tmp.assignee.equals(tmp.reviewer)) {
				message = "A task can't have the same user as an assignee and reviewer";
			}
			try {

				render(request.controller.replace(".", "/") + "/show.html",
						type, object, users, statuses, types, dependencies,
						message2, deletable, reviewers, message, isReporter, isReviewer, isAssignee);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type);
			}
		}
		if (tmp.estimationPoints == 0) {
			message = "Please enter an estimation greater than Zero";
			try {
				render(request.controller.replace(".", "/") + "/show.html",
						type, object, users, statuses, types, dependencies,
						message2, deletable, reviewers, message, isReporter, isReviewer, isAssignee);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type);
			}
		} else if (tmp.assignee.equals(tmp.reviewer)) {
			message = "A task can't have the same user as an assignee and reviewer";
			try {
				render(request.controller.replace(".", "/") + "/show.html",
						type, object, users, statuses, types, dependencies,
						message2, deletable, reviewers, message, isReporter, isReviewer, isAssignee);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type);
			}
		} else if ((tmp.taskStatus.name.equals("Closed")
				|| tmp.taskStatus.name.equals("Verified") || tmp.taskStatus.name
				.equals("Reopened"))) {
			message = "Only Task reviewer can set the task to this status";
			boolean Check = Security.check("CanSetStatusTo"
					+ tmp.taskStatus.name);
			if (!Check) {
				try {
					render(request.controller.replace(".", "/") + "/show.html",
							type, object, users, statuses, types, dependencies,
							message2, deletable, reviewers, message, isReporter, isReviewer, isAssignee);
				} catch (TemplateNotFoundException e) {
					render("CRUD/show.html", type);
				}
			}

		} else {
			if (!Security.check("CanSetOtherStatus")) {
				message = "Only Task Assignee can set the task to this status";
				boolean Check = Security.check("CanSetStatusTo"
						+ tmp.taskStatus.name);
				if (!Check) {
					try {
						render(request.controller.replace(".", "/")
								+ "/show.html", type, object, users, statuses,
								types, dependencies, message2, deletable,
								reviewers, message, isReporter, isReviewer, isAssignee);
					} catch (TemplateNotFoundException e) {
						render("CRUD/show.html", type);
					}
				}

			} else {
				if (tmp.estimationPoints > 100) {
					message = "An estimation greater than 100 is a total nonsense";
					try {
						render(request.controller.replace(".", "/")
								+ "/show.html", type, object, users, statuses,
								types, dependencies, message2, deletable,
								reviewers, message, isReporter, isReviewer, isAssignee);
					} catch (TemplateNotFoundException e) {
						render("CRUD/show.html", type);
					}
				}
			}
		}
		String header = "Task: 'T" +  tmp.id + "\'" + " has been edited.";
		String body = "In Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + tmp.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";		
		/*////Long Informative Notification message. Not suitable for online notification.
		String header = "A Task has been edited in Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + " in Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + ".";
		String body = "The Task:" + '\n' 
				    + " " + "\'" + oldDescription + "\'" + '\n' 
			        + " has been edited in Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + " in Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + "." + '\n' + '\n'  
			        + "Task Description: " +  tmp.description + "." + '\n' 
					+ " Story: " + tmp.taskStory.description + "." + '\n' 
					+ " Type: " + tmp.taskType.name + "." + '\n' 
					+ " Status: " + tmp.taskStatus.name+ "." + '\n' 
					+ " Sprint: " + tmp.taskSprint.sprintNumber+ "." + '\n' 
					+ " Assignee: " + tmp.assignee.name + "." + '\n' 
					+ " Reporter: " + tmp.reporter.name + "." + '\n' 
					+ " Reviewer: " + tmp.reviewer.name + "." + '\n' 
					+ " Edited by: " + tmp.reporter.name + ".";*/
		object.save();
		Logs.addLog(myUser, "Edit", "Task", tmp.id, tmp.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		Notifications.notifyUsers(tmp.taskStory.componentID.componentUsers, header, body, (byte) 0);
		flash.success(Messages.get("crud.saved", type.modelName, object
				.getEntityId()));
		if (params.get("_save") != null)

		{
			redirect("/storys/liststoriesinproject?projectId="
					+ tmp.taskStory.componentID.project.id + "&storyId="
					+ tmp.taskStory.id);

		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * A Method that deletes a Task
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return its a void method.
	 */
	public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Task tmp = (Task) object;
		try {
			tmp.deleted = true;
			String header = "Task: 'T" +  tmp.id + "\'" + " has been deleted.";
			String body = "In Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + "." + '\n' 
				+ " In Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + "." + '\n' 
				+ " Story: 'S" + tmp.taskStory.id + "\'" + "." + '\n' 
				+ " Deleted by: " + "\'" + Security.getConnected().name + "\'" + ".";		
			/*////Long Informative Notification message. Not suitable for online notification.
			String header = "A Task in Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + " in Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + " has been deleted.";
			String body = "The Task:" + '\n' 
			    + " " + "\'" + tmp.description + "\'" + '\n' 
				+ " in Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + " in Project: " + "\'" + tmp.taskStory.componentID.project.name + "\'" + " has been deleted." + '\n' + '\n'  
				+ " Story: " + tmp.taskStory.description + "." + '\n' 
				+ " Deleted by: " + Security.getConnected().name + ".";*/
			Logs.addLog(Security.getConnected(), "delete", "Task", tmp.id, tmp.taskStory.componentID.project, new Date(System.currentTimeMillis()));
			Notifications.notifyUsers(tmp.taskStory.componentID.componentUsers, header, body, (byte) -1);
			object.save();
		} catch (Exception e) {
			flash.error(Messages.get("crud.delete.error", type.modelName,
					object.getEntityId()));
			redirect(request.controller + ".show", object.getEntityId());
		}
		flash.success(Messages.get("crud.deleted", type.modelName, object
				.getEntityId()));
		redirect("/show/tasks?id=" + tmp.taskStory.componentID.project.id);
	}
	/**
	 * A Method that renders back the tasks of the dependent story of a given
	 * story
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return its a void method.
	 */
	public static void dependencies(long id) {
		Story chosen = Story.findById(id);
		List<Task.Object> tasks = new ArrayList<Task.Object>();
		if (chosen != null) {
			List<Story> depend = chosen.dependentStories;
			for (Story story2 : depend) {
				for (Task task2 : story2.storiesTask) {
					tasks.add(new Task.Object(task2.id, task2.description));
				}

			}
		}
		renderJSON(tasks);
	}
	
	public static void reviewers(long id, long id2) {
		System.out.println(id);
		Story chosen = Story.findById(id);
		User Assignee = User.findById(id2);
		List<User> users = chosen.componentID.componentUsers;
		List<User.Object> reviewers = new ArrayList<User.Object>();
		for( User user : users){
			if(!user.name.equals(Assignee.name)){
				reviewers.add(new User.Object(user.id, user.name));
			}
		}
		renderJSON(reviewers);
	}

	

	/**
	 * Saves a specific effort of a given day for a certain task in a specific
	 * sprint. It also Notifies all the users in the corresponding component of
	 * the change and type of change. It also logs the change.
	 * 
	 * @category C4 S1
	 * @author Hadeer Younis
	 * @param id
	 *            The id of the task to be updated.
	 * @param effort
	 *            The effort points of a specific day.
	 * @param day
	 *            The number of the day to which the effort belongs.
	 */
	public static void enterEffort(long id, double effort, int day) {
		Task temp = Task.findById(id);
		User userWhoChanged = Security.getConnected();
		Component t = temp.taskStory.componentID;

		Security.check(t.componentUsers.contains(userWhoChanged));

		Calendar timeChanged = Calendar.getInstance();
		String changeType = "";

		if (temp.getEffortPerDay(day) != -1) {
			changeType = "Edit Attribute Effort";
		} else {
			changeType = "Insert Attribute Effort";
		}

		temp.setEffortOfDay(effort, day);
		temp.save();
		Logs.addLog(userWhoChanged, changeType, "Task", id,
				temp.taskSprint.project, timeChanged.getTime());

	}

	/**
	 * Fetches all the data needed to generate a report on a given task.
	 * 
	 * @category C4 S15
	 * @author Hadeer Younis
	 * @param id
	 *            The id of the task whose report will be generated.
	 */
	public static void getReport(long id) {
		List<Log> temp = Log.findAll();
		Task theTask = Task.findById(id);
		Security.check(theTask.taskStatus.project.users.contains(Security
				.getConnected()));
		boolean empty = temp.isEmpty();
		String lastModified = null;
		int numberOfModifications = 0;
		String efforts = "[";
		boolean flag = false;
		double n = theTask.getEffortPerDay(0);
		String changes = "[";
		if (theTask.taskSprint != null) {
			for (int j = 0; j < theTask.taskSprint.getDuration(); j++) {
				if (!flag)
					n = theTask.getEffortPerDay(j);
				if (n == -1) {
					flag = true;
					n = theTask.getEffortPerDay(j - 1);
				}
				if (j == theTask.taskSprint.getDuration() - 1)
					efforts = efforts + "[" + j + "," + n + "]]";
				else
					efforts = efforts + "[" + j + "," + n + "],";
			}
		} else {
			efforts = "[]";
		}
		for (int i = 0; i < temp.size(); i++) {
			if (temp.get(i).resource_id != id)
				temp.remove(i);
		}
		for (int i = 0; i < temp.size(); i++) {
			int k = 1;
			if (i < temp.size() - 2) {
				keepLoop: while (temp.get(i).date.toString().substring(0, 10)
						.equals(
								temp.get(i + 1).date.toString()
										.substring(0, 10))) {
					i++;
					if (i == temp.size() - 1)
						break keepLoop;
					k++;
				}
			}
			if (i == temp.size() - 1)
				changes = changes + "['"
						+ temp.get(i).date.toString().substring(0, 10) + "',"
						+ k + "]]";
			else
				changes = changes + "['"
						+ temp.get(i).date.toString().substring(0, 10) + "',"
						+ k + "],";

		}

		if (!empty) {
			lastModified = temp.get(temp.size() - 1).date.toString().substring(
					0, 10)
					+ " @ "
					+ temp.get(temp.size() - 1).date.toString().substring(11);
			numberOfModifications = temp.size();
		}
		Date maxdate = temp.get(temp.size() - 1).date;
		maxdate.setTime(temp.get(temp.size() - 1).date.getTime()
				+ (3 * 86400000));
		String maxDate = maxdate.toString().substring(0, 10);

		Date mindate = temp.get(0).date;
		mindate.setTime(temp.get(0).date.getTime() - (3 * 86400000));
		String minDate = mindate.toString().substring(0, 10);
		boolean canSee = true;

		Project myProject = theTask.taskType.project;
		render(myProject, canSee, minDate, temp, lastModified, empty, efforts,
				changes, numberOfModifications, theTask, maxDate);
	}

	// A method that updates the Task's Status.
	public static void updateTaskStatus(long Task_id, int Status) {
		Task T = Task.findById(Task_id);
		T.status = Status;
		T.save();
	}

	/**
	 * This method direct to the CRUD admin page to create a new Task Story 35
	 * Component 3
	 * 
	 * @author Monayri
	 * @param void
	 * @return void
	 */
	public static void AddTaskAPI() {
		redirect("/admin/tasks/new");
	}

	/**
	 * This method divert to the CRUD admin page to edit a given Task Story 36
	 * Component 3
	 * 
	 * @author Monayri
	 * @param void
	 * @return void
	 */
	public static void EditTaskAPI(long TaskID) {
		// redirect( "/admin/tasks/" + TaskID );
	}

	/**
	 * changes the given task description
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the giventask
	 * @param desc
	 *            The new description
	 * @return boolean
	 */
	public static boolean editTaskDesc(long id, String desc) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		String oldDescription = task1.description;
		task1.description = desc;
		task1.save();
		String header = "Task: 'T" +  task1.id + "\'" + " Description has been edited.";
		String body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
		+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
		+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
		+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		/*////Long Informative Notification message. Not suitable for online notification.
		String header = "A Task Description has been edited in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		String body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Description:  " + "\'" + oldDescription + "\'" + ", and it has been edited." + '\n' + '\n'
			        + "The New Description: " +  task1.description + "." 
					+ " Edited by: " + Security.getConnected() + ".";*/
		Logs.addLog(Security.getConnected(), "Edit", "Task Description", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		return true;

	}

	/**
	 * changes the given task description
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the giventask
	 * @param desc
	 *            The new description
	 * @return void
	 */
	public static void editTaskDescJSON(long id, String desc) {
		String zero = "0";
		String one = "1";
		Task task1 = Task.findById(id);
		if (task1 == null)
			renderJSON(zero);
		task1.description = desc;
		task1.save();
		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		Notifications.notifyUsers(m, "TASk editing", "task " + id
				+ " description is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;
		User myUser = Security.getConnected();
		Logs.addLog(myUser, "EditDesc", "Task", id, y, cal.getTime());
		renderJSON(one);

	}

	/**
	 * changes the given task description
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the giventask
	 * @param userId
	 *            the id of the user who will do the change in description
	 * @param desc
	 *            The new description
	 * @return boolean
	 */
	public static boolean editTaskDesc2(long id, long userId, String desc) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can(
				"changeTaskDescreption");

		if (task1.reviewer.id != userId && task1.assignee.id != userId) {
			if (!permession)
				return false;
		}
		task1.description = desc;
		task1.save();
		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		String body="";
		if(userId==Security.getConnected().id)
		{
			
		}
		else
		{
			body="from "+Security.getConnected().name+"'s account";
		}
		Notifications.notifyUsers(m, "TASk editing", "task " + id
				+ " description is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;

		Logs.addLog(user1, "EditDesc", "Task", id, y, cal.getTime());
		return true;

	}

	/**
	 * changes the given task type
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param type
	 *            The new Tasktupe
	 * @param userId
	 *            the id of the user who will change the taskType
	 * @return boolean
	 */
	public static boolean editTaskType(long id, long typeId, long userId) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		String oldType = task1.taskType.name;
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		if (user1 == null)
			return false;
		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can("changeTaskType");
		if (task1.reviewer.id != userId && task1.assignee.id != userId) {
			if (!permession)
				return false;
		}
		TaskType type = TaskType.findById(typeId);
		task1.taskType = type;
		task1.save();
		String body="";
		String header = "Task: 'T" +  task1.id + "\'" + " Description has been edited.";
		//String header = "A Task Type has been edited in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		if(userId==Security.getConnected().id)
		{
			body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		/*////Long Informative Notification message. Not suitable for online notification.
		body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Type:  " + "\'" + oldType + "\'" + ", and it has been edited." + '\n' + '\n'
			        + "The New Task Type: " +  task1.taskType.name + "." 
					+ " Edited by: " + user1.name + ".";*/
		}
		else
		{
			body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'" 
			+ ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
			/*////Long Informative Notification message. Not suitable for online notification.
			body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
		    + " in Story: " + task1.taskStory.description + '\n' 
		    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
	        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
		    + " had Type:  " + "\'" + oldType + "\'" + ", and it has been edited." + '\n' + '\n'
	        + "The New Task Type: " +  task1.taskType.name + "." 
			+ " Edited by: " + user1.name + "." + '\n' 
			+ "From "+Security.getConnected().name+"'s account.";*/

		}
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		Logs.addLog(user1, "Edit", "Task Type", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		return true;
	}

	/**
	 * changes the given task type
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param type
	 *            The new Tasktupe
	 * @param userId
	 *            the id of the user who will change the taskType
	 * @return void
	 */
	public static void editTaskTypeJSON(long id, long typeId, long userId) {
		String zero = "0";
		String one = "1";
		TaskType type = TaskType.findById(typeId);
		Task task1 = Task.findById(id);
		if (task1 == null)
			renderJSON(zero);

		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		if (user1 == null)
			renderJSON(zero);

		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can("changeTaskType");

		if (task1.reviewer.id != userId && task1.assignee.id != userId) {
			if (!permession)
				renderJSON(zero);

		}
		task1.taskType = type;
		task1.save();

		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		Notifications.notifyUsers(m, "TASK editing", "task " + id
				+ " task type is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;
		User myUser = Security.getConnected();
		Logs.addLog(myUser, "EditTasktype", "Task", id, y, cal.getTime());
		renderJSON(one);

	}

	/**
	 * this method does filter for task id and new status and user_id and give
	 * it to editTaskStatus
	 * 
	 * @author josephhajj
	 * @param id
	 * @param columnSequence
	 * @param taskString
	 */
	public static void changeTaskStatusHelper(long id, int columnSequence,
			String taskString, long user_id) {
		if (user_id == 0) {
			user_id = Security.getConnected().id;
		}

		// setting the variable needed for the method
		// defining the appropriate sprint
		Sprint s = Sprint.findById(id);
		// defining the project of the sprint
		Project p = s.project;
		// defining the board of the project
		Board b = p.board;
		// defining the status
		TaskStatus status = new TaskStatus();
		// defining the cols of the board only
		List<Column> cols = b.columns;
		// defining the final task id and its helper string
		String task_id_helper[];
		String task_id_helper2[];
		long task_id;
		// defining a flag for the second loop
		boolean flag = true;

		// getting the actual status
		Column col;
		col = Column.find("bySequenceAndBoard", columnSequence,b).first();
		status = col.taskStatus;

		// get the actual task_id in an int
		task_id_helper = taskString.split("_");
		task_id_helper2 = task_id_helper[0].split("-");
		task_id = Integer.parseInt(task_id_helper2[1]);

		editTaskStatus(task_id, user_id, status);

	}

	/**
	 *this method filter the method for taskid and user_id and the new assignee
	 * and gives them to editTaskAssignee
	 * 
	 * @author josephhajj
	 * @param id
	 * @param taskString
	 * @param user_id
	 * @param row
	 */
	public static void changeTaskAssigneeHelper(long id, String taskString,
			long user_id, int row) {
		// if user is not selected take the one in the session
		if (user_id == 0) {
			user_id = Security.getConnected().id;
		}

		// getting the whole list of users
		User user = User.findById(user_id);
		Component component = Component.findById(id);
		List<User> users = component.componentUsers;

		String task_id_helper[];
		String task_id_helper2[];
		long task_id;

		// filtering the task id
		task_id_helper = taskString.split("_");
		task_id_helper2 = task_id_helper[0].split("-");
		task_id = Integer.parseInt(task_id_helper2[1]);

		// calling the method
		editTaskAssignee2(task_id, user_id, users.get(row).id);

	}

	/**
	 * changes the given task status
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param newStatus
	 *            The new taskstatus
	 * @param userId
	 *            the id of the user who will change the taskstatus
	 * @return boolean
	 */
	public static boolean editTaskStatus(long id, long userId, TaskStatus newStatus) {
		Task task1 = Task.findById(id);

		if (task1 == null)
			return false;
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		if (user1 == null)
			return false;

		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can("changeTaskStatus");

		if (task1.reviewer.id != userId && task1.assignee.id != userId) {
			if (!permession)
				return false;
		}

		if (newStatus.name.equals("New") && user1.id != task1.assignee.id)
			if (!permession)
				return false;
		if (newStatus.name.equals("Started") && user1.id != task1.assignee.id)
			if (!permession)
				return false;
		if (newStatus.name.equals("Resovled") && user1.id != task1.assignee.id)
			if (!permession)
				return false;

		if (newStatus.name.equals("Reopened") && user1.id != task1.reviewer.id)
			if (!permession)
				return false;

		if (newStatus.name.equals("Verified") && user1.id != task1.reviewer.id)
			if (!permession)
				return false;

		if (newStatus.name.equals("Closed") && user1.id != task1.reviewer.id)
			if (!permession)
				return false;

		if (newStatus.name.equals("Reopened"))
			task1.taskStory.done = false;
		String oldStatus = task1.taskStatus.name;
		task1.taskStatus = newStatus;
		task1.save();

		if (newStatus != null && newStatus.name == "Closed") {
			StoryComplete(id);
		}
		if (newStatus.name.equals("Reopened"))
			task1.taskStory.done = false;
		String body="";
		String header = "A Task Status has been edited in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		if(userId==Security.getConnected().id)
		{
		body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Status:  " + "\'" + oldStatus + "\'" + ", and it has been edited." + '\n' + '\n'
			        + "The New Status: " +  task1.taskStatus.name + "."
					+ " Edited by: " + user1.name + "." + '\n' 
					+ " Edited at: " + new Date(System.currentTimeMillis()) + ".";
		}
		else
		{
			body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
		    + " in Story: " + task1.taskStory.description + '\n' 
		    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
	        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
		    + " had Status:  " + "\'" + oldStatus + "\'" + ", and it has been edited." + '\n' + '\n'
	        + "The New Status: " +  task1.taskStatus.name + "."
			+ " Edited by: " + user1.name + "." + '\n' 
			+ "From "+Security.getConnected().name+"'s account"
			+ " Edited at: " + new Date(System.currentTimeMillis()) + ".";
		}
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		Logs.addLog(user1, "Edit", "task status", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		return true;

	}

	/**
	 * changes the given task status
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param statusId
	 *            The new taskstatus id
	 * @param userId
	 *            the id of the user who will change the taskstatus
	 * @return void
	 */
	public static void editTaskStatusJSON(long id, long userId, long statusId) {
		String zero = "0";
		String one = "1";
		TaskStatus newStatus = TaskStatus.findById(statusId);
		Task task1 = Task.findById(id);

		if (task1 == null)
			renderJSON(zero);
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		if (user1 == null)
			renderJSON(zero);

		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can("changeTaskStatus");

		if (task1.reviewer.id != userId && task1.assignee.id != userId) {
			if (!permession)
				renderJSON(zero);
		}

		if (newStatus.name.equals("New") && user1.id != task1.assignee.id)
			if (!permession)
				renderJSON(zero);
		if (newStatus.name.equals("Started") && user1.id != task1.assignee.id)
			if (!permession)
				renderJSON(zero);
		if (newStatus.name.equals("Resovled") && user1.id != task1.assignee.id)
			if (!permession)
				renderJSON(zero);

		if (newStatus.name.equals("Reopened") && user1.id != task1.reviewer.id)
			if (!permession)
				renderJSON(zero);

		if (newStatus.name.equals("Verified") && user1.id != task1.reviewer.id)
			if (!permession)
				renderJSON(zero);

		if (newStatus.name.equals("Closed") && user1.id != task1.reviewer.id)
			if (!permession)
				renderJSON(zero);

		if (newStatus.name.equals("Reopened"))
			task1.taskStory.done = false;

		task1.taskStatus = newStatus;
		task1.save();

		if (newStatus != null && newStatus.name == "Closed") {
			StoryComplete(id);
		}
		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		Notifications.notifyUsers(m, "TASK editing", "task " + id
				+ " taskstatus is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;

		Logs.addLog(user1, "Edit task status", "Task", id, y, cal.getTime());
		renderJSON(one);

	}

	/**
	 * changes the given task estimation points
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param estimation
	 *            the value of the new estimation
	 * @return boolean
	 */
	public static boolean editTaskEstimation(long id, double estimation) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		Double oldEstimation = task1.estimationPoints;
		if (estimation < 0)
			return false;
		task1.estimationPoints = estimation;
		task1.save();
		String header = "Task: 'T" +  task1.id + "\'" + " Estimation Points have been edited.";
		String body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
		+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
		+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
		+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		/*////Long Informative Notification message. Not suitable for online notification.
		String header = "A Task Estimation has been edited in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		String body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + "\'" + task1.taskStory.description + "\'" + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Estimation Points:  " + "\'" + oldEstimation + "\'" + ", and it has been edited." + '\n' + '\n'
			        + "The New Estimation Points: " +  task1.estimationPoints + "." 
					+ " Edited by: " + Security.getConnected().name + ".";*/
		Logs.addLog(Security.getConnected(), "Edit", "Task estimation", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		return true;
	}

	/**
	 * changes the given task estimation points
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param estimation
	 *            the value of the new estimation
	 * @return void
	 */
	public static void editTaskEstimationJSON(long id, double estimation) {
		String one = "1";
		String zero = "0";
		Task task1 = Task.findById(id);
		if (task1 == null)
			renderJSON(zero);
		if (estimation < 0)
			renderJSON(zero);
		task1.estimationPoints = estimation;
		task1.save();
		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		Notifications.notifyUsers(m, "TASK editing", "task " + id
				+ " estimation points is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;
		User myUser = Security.getConnected();
		Logs.addLog(myUser, "Edit task estimation", "Task", id, y, cal
				.getTime());
		renderJSON(one);
	}

	/**
	 * changes the given task assignee
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param assigneId
	 *            the id of the user who will be the assignee of the task
	 * @return boolean
	 */
	public static boolean editTaskAssignee(long id, long assigneeId) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		User assignee = User.findById(assigneeId);
		if (assignee == null)
			return false;
		if (task1.reviewer.getId() == assigneeId)
			return false;
		String oldAssignee = task1.assignee.name;
		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add(task1);
		assignee.save();
		String header = "Task: 'T" +  task1.id + "\'" + " Assignee has been edited.";
		String body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
		+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
		+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
		+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		/*////Long Informative Notification message. Not suitable for online notification.
		String header = "A Task Assignee has been changed in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		String body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Task Assignee: " + "\'" + oldAssignee + "\'" + ", and it has been changed." + '\n' + '\n'
			        + "The New Estimation Points: " +  task1.assignee.name + "." 
					+ " Edited by: " + Security.getConnected().name + "." + '\n' 
					+ " Edited at: " + new Date(System.currentTimeMillis()) + ".";*/
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		Logs.addLog(Security.getConnected(), "Edit", "Task Assignee", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		return true;
	}

	/**
	 * changes the given task assignee
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param assigneId
	 *            the id of the user who will be the assignee of the task
	 * @return void
	 */
	public static void editTaskAssigneeJSON(long id, long assigneeId) {
		String zero = "0";
		String one = "1";
		Task task1 = Task.findById(id);
		if (task1 == null)
			renderJSON(zero);
		User assignee = User.findById(assigneeId);
		if (assignee == null)
			renderJSON(zero);
		if (task1.reviewer.getId() == assigneeId)
			renderJSON(zero);

		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add(task1);
		assignee.save();
		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		Notifications.notifyUsers(m, "TASK editing", "task " + id
				+ " assignee is now changed to" + assignee.email, (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;
		User myUser = Security.getConnected();
		Logs.addLog(myUser, "change  task assignee", "Task", id, y, cal
				.getTime());
		renderJSON(one);
	}

	/**
	 * changes the given task assignee
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param userId
	 *            the id of the user who will do the change
	 * @param assigneId
	 *            the id of the user who will be the assignee of the task
	 * @return boolean
	 */
	public static boolean editTaskAssignee2(long id, long userId, long assigneeId) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		User assignee = User.findById(assigneeId);
		if (assignee == null)
			return false;
		if (task1.reviewer.getId() == assigneeId)
			return false;

		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		if (user1 == null)
			return false;

		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can("changeAssignee");

		if (!permession)
			return false;
		String oldAssignee = task1.assignee.name;
		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add(task1);
		assignee.save();
		String header = "Task: 'T" +  task1.id + "\'" + " Assignee has been edited.";
		/*////Long Informative Notification message. Not suitable for online notification.
		 * String header = "A Task Assignee has been changed in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		 */
		String body="";
		if(userId == Security.getConnected().id)
		{
			body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		/*body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Task Assignee: " + "\'" + oldAssignee + "\'" + ", and it has been changed." + '\n' + '\n'
			        + "The New Task Assignee: " +  task1.assignee.name + "." + '\n'
					+ " Edited by: " + user1.name + ".";*/
		}
		else
		{
			body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'"
			+ ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
			/*body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
		    + " in Story: " + task1.taskStory.description + '\n' 
		    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
	        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
		    + " had Task Assignee: " + "\'" + oldAssignee + "\'" + ", and it has been changed." + '\n' + '\n'
	        + "The New Task Assignee: " +  task1.assignee.name + "." + '\n'
			+ " Edited by: " + user1.name 
			+ ", From "+Security.getConnected().name+"'s account.";*/
		}
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		Logs.addLog(user1, "Edit", "Task Assignee", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		
		return true;
	}

	/**
	 * changes the given task reviewer
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param reviewerId
	 *            the id of the user who will be the reviewer of the task
	 * @return boolean
	 */
	public static boolean editTaskReviewer(long id, long reviewerId) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		User reviewer = User.findById(reviewerId);
		if (reviewer == null)
			return false;
		if (task1.assignee.getId() == reviewerId)
			return false;
		String oldReviewer = task1.reviewer.name;
		task1.reviewer = reviewer;
		task1.save();
		reviewer.tasks.add(task1);
		reviewer.save();
		String header = "Task: 'T" +  task1.id + "\'" + " Reviewer has been edited.";
		String body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
		+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
		+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
		+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
	/*////Long Informative Notification message. Not suitable for online notification.
		String header = "A Task Reviewer has been changed in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		String body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Task Assignee: " + "\'" + oldReviewer + "\'" + ", and it has been changed." + '\n' + '\n'
			        + "The New Estimation Points: " +  task1.reviewer.name + "." 
					+ " Edited by: " + Security.getConnected().name + ".";*/
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		Logs.addLog(Security.getConnected(), "Edit", "Task Reviewer", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		return true;
	}

	/**
	 * changes the given task reviewer
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param reviewerId
	 *            the id of the user who will be the reviewer of the task
	 * @return void
	 */
	public static void editTaskReviewerJSON(long id, long reviewerId) {
		String zero = "0";
		String one = "1";
		Task task1 = Task.findById(id);
		if (task1 == null)
			renderJSON(zero);
		User reviewer = User.findById(reviewerId);
		if (reviewer == null)
			renderJSON(zero);
		if (task1.assignee.getId() == reviewerId)
			renderJSON(zero);
		task1.reviewer = reviewer;
		task1.save();
		reviewer.tasks.add(task1);
		reviewer.save();
		List<User> m = new ArrayList();
		m.add(task1.assignee);
		m.add(task1.reporter);
		m.add(task1.reviewer);
		Notifications.notifyUsers(m, "TASK editing", "task " + id
				+ "reviewer is changed to " + reviewer.email, (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.taskStory.componentID.project;
		User myUser = Security.getConnected();
		Logs.addLog(myUser, "Edit task reviewer", "Task", id, y, cal.getTime());
		renderJSON(one);
	}

	/**
	 * changes the given task reviewer
	 * 
	 * @author Moumen Mohamed story=C3S36
	 * @param id
	 *            the id of the given task
	 * @param userId
	 *            the id of the user who will be doing the change
	 * @param reviewerId
	 *            the id of the user who will be the reviewer of the task
	 * @return boolean
	 */
	public static boolean editTaskReviewer2(long id, long userId, long reviewerId) {
		Task task1 = Task.findById(id);
		if (task1 == null)
			return false;
		User reviewer = User.findById(reviewerId);
		if (reviewer == null)
			return false;
		if (task1.assignee.getId() == reviewerId)
			return false;

		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		if (user1 == null)
			return false;

		Project currentProject = task1.taskStory.componentID.project;
		boolean permession = user1.in(currentProject).can("changeReviewer");

		if (!permession)
			return false;
		String oldReviewer = task1.reviewer.name;
		task1.reviewer = reviewer;
		task1.save();
		reviewer.tasks.add(task1);
		reviewer.save();
		String body="";
		//String header = "A Task Reviewer has been changed in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		String header = "Task: 'T" +  task1.id + "\'" + " Reviewer has been edited.";
		if(userId==Security.getConnected().id)
		{
			body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		/*////Long Informative Notification message. Not suitable for online notification.
		body = "The Task:" + "\'" + task1.description + "\'" + '\n' 
				    + " in Story: " + task1.taskStory.description + '\n' 
				    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
			        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
				    + " had Task Assignee: " + "\'" + oldReviewer + "\'" + ", and it has been changed." + '\n' + '\n'
			        + "The New Estimation Points: " +  task1.reviewer.name + "." 
					+ " Edited by: " + user1.name + ".";*/
		}
		else
		{
			body = "In Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + "." + '\n' 
			+ " In Component: " + "\'" + task1.taskStory.componentID.name + "\'" + "." + '\n' 
			+ " Story: 'S" + task1.taskStory.id + "\'" + "." + '\n' 
			+ " Edited by: " + "\'" + Security.getConnected().name + "\'"
			+ ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
			/*////Long Informative Notification message. Not suitable for online notification.
			body="The Task:" + "\'" + task1.description + "\'" + '\n' 
		    + " in Story: " + task1.taskStory.description + '\n' 
		    + " in Component: " + "\'" + task1.taskStory.componentID.name + "\'" + '\n' 
	        + " in Project: " + "\'" + task1.taskStory.componentID.project.name + "\'" + '\n' 
		    + " had Task Assignee: " + "\'" + oldReviewer + "\'" + ", and it has been changed." + '\n' + '\n'
	        + "The New Estimation Points: " +  task1.reviewer.name + "." 
			+ " Edited by: " + user1.name + "." + '\n' 
			+ "From "+Security.getConnected().name+"'s account.";*/
		}
		Notifications.notifyUsers(task1.taskStory.componentID.getUsers(), header, body, (byte) 0);
		Logs.addLog(user1, "Edit", "Task Reviewer", id, task1.taskStory.componentID.project, new Date(System.currentTimeMillis()));
		
		return true;
	}

	public static void chooseTaskPerson() {
		render();
	}

	/**
	 * @author emadabdelrahman
	 * @param Task
	 *            id
	 * @Description Checks if all the tasks of a story is completed. if all of
	 *              the tasks are complete, then it marks the story as done
	 */
	public static void StoryComplete(long taskId) {
		Task t = Task.findById(taskId);
		Story s = t.taskStory;
		List<Task> AllTasksInStory = s.storiesTask;

		for (int i = 0; i < AllTasksInStory.size(); i++) {
			if (AllTasksInStory.get(i).taskStatus.name != "Done") {
				return;
			}

		}

		s.done = true;
		s.save();

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

	public static void chooseTaskAssiRev(long taskId, int aORr) {
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
		render(taskId, users, aORr);
	}

	/**
	 * @author Dina Helal
	 * @param taskId
	 *            the task to be edited
	 * @param compId
	 *            component of the users
	 */

	public static void chooseTaskAssi(long taskId, long compId, long userId) {
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		List<User> users = new ArrayList<User>();
		Task task = Task.findById(taskId);
		Component c = Component.findById(compId);
		users = c.componentUsers;
		users.remove(task.reviewer);
		render(taskId, users, user1);
	}

	/**
	 * @author Dina Helal
	 * @param taskId
	 *            the task to be edited
	 * @param compId
	 *            component of the users
	 */

	public static void chooseRev(long taskId, long compId, long userId) {
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		User user1 = User.findById(userId);
		List<User> users = new ArrayList<User>();
		Task task = Task.findById(taskId);
		Component c = Component.findById(compId);
		users = c.componentUsers;
		users.remove(task.assignee);
		render(taskId, users, user1);
	}

	/**
	 * @author menna_ghoneim Renders a given taskid with a likt of project types
	 *         and the session's user id to a page to choose
	 * @param taskId
	 *            the task to be edited
	 */

	public static void chooseTaskType(long taskId) {
		Task task = Task.findById(taskId);
		List<TaskType> types = task.taskSprint.project.taskTypes;
		User user = Security.getConnected();
		render(taskId, types, user);
	}

	/**
	 * @author dina_helal takes a taskid, and renders it to a page to choose
	 *         task type
	 * @param taskId
	 *            the task to be edited
	 */

	public static void chooseType(long taskId, long userId) {
		if (userId == 0) {
			userId = Security.getConnected().id;
		}
		Task task = Task.findById(taskId);
		List<TaskType> types = task.taskSprint.project.taskTypes;
		render(taskId, types, userId);
	}

	/**
	 * @author menna_ghoneim Renders a given taskId with project statuses and
	 *         the user in the session to a page to choose a task status
	 * @param taskId
	 *            the task to be edited
	 */
	public static void chooseTaskStatus(long taskId) {
		Task task = Task.findById(taskId);
		List<TaskStatus> states = task.taskSprint.project.taskStatuses;
		User user = Security.getConnected();
		render(taskId, states, user);
	}

	public static void eee(long ngo) {

	}
}