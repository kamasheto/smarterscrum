package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import models.Board;
import models.Column;
import models.Comment;
import models.Component;
import models.Log;
import models.Meeting;
import models.ProductRole;
import models.Project;
import models.Requestreviewer;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With( Secure.class )
public class Tasks extends SmartCRUD
{

	/**
	 * A Method that renders the form of creating a Task.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	public static void blank( long componentId, long taskId, long projectId )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		User user = Security.getConnected();
		Project project = null;
		Component component = null;
		Task task = null;
		Project p = null;
		if( projectId != 0 ) // adding task to project (put drop down list of
								// components)
		{
			p = Project.findById( projectId );
			project = p;
			if( project.deleted )
				notFound();
			Security.check( user.in( project ).can( "AddTask" ) );
		}
		else
		{
			if( componentId != 0 ) // adding task to component
			{
				component = Component.findById( componentId );
				project = component.project;
				if( component.deleted )
					notFound();
				Security.check( user.in( component.project ).can( "AddTask" ) );
			}
			else
			{
				if( taskId != 0 ) // adding subtask to parent task with id
									// taskId
				{
					task = Task.findById( taskId );
					if( task.deleted )
						notFound();
					if( task.component != null )
					{
						Security.check( user.in( task.component.project ).can( "AddTask" ) );
						project = task.component.project;
					}
					else if( task.project != null )
					{
						Security.check( user.in( task.project ).can( "AddTask" ) );
						project = task.project;
					}

				}
			}
		}

		List<Sprint> sprints = new ArrayList<Sprint>();
		for( int i = 0; i < project.sprints.size(); i++ )
		{
			Sprint sprint = project.sprints.get( i );
			java.util.Date End = sprint.endDate;
			Calendar cal = new GregorianCalendar();
			if( End!=null && End.after( cal.getTime() ) )
			{
				sprints.add( sprint );
			}
		}

		String productRoles = "";
		for( int i = 0; i < project.productRoles.size(); i++ )
		{
			if( project.productRoles.get( i ).name.charAt( 0 ) == 'a' || project.productRoles.get( i ).name.charAt( 0 ) == 'e' || project.productRoles.get( i ).name.charAt( 0 ) == 'i' || project.productRoles.get( i ).name.charAt( 0 ) == 'o' || project.productRoles.get( i ).name.charAt( 0 ) == 'u' || project.productRoles.get( i ).name.charAt( 0 ) == 'A' || project.productRoles.get( i ).name.charAt( 0 ) == 'E' || project.productRoles.get( i ).name.charAt( 0 ) == 'I' || project.productRoles.get( i ).name.charAt( 0 ) == 'O' || project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + project.productRoles.get( i ).name + ",-";
		}

		try
		{
			render( project, p, component, task, type, sprints, productRoles, projectId, componentId, taskId );

		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/blank.html", type );
		}

	}

	/**
	 * A Method that Creates a Task and checks the validation of inputs by users
	 * in the create form.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	// public static void create() throws Exception {
	// ObjectType type = ObjectType.get(getControllerClass());
	// notFoundIfNull(type);
	// JPASupport object = type.entityClass.newInstance();
	// validation.valid(object.edit("object", params));
	// String message = "";
	// Task tmp = (Task) object;
	// Security.check(Security.getConnected().in(tmp.project).can("AddTask"));
	// List<Story> stories = tmp.taskStory.componentID.componentStories;
	// List<User> users = tmp.taskStory.componentID.componentUsers;
	// List<TaskStatus> statuses =
	// tmp.taskStory.componentID.project.taskStatuses;
	// List<TaskType> types = tmp.taskStory.componentID.project.taskTypes;
	// List<Sprint> sprints = new ArrayList<Sprint>();
	// Component component = tmp.taskStory.componentID;
	// List<Requestreviewer> reviewers =
	// Requestreviewer.find("byComponentAndAccepted", tmp.taskStory.componentID,
	// true).fetch();
	// for (int i = 0; i < tmp.taskStory.componentID.project.sprints.size();
	// i++) {
	// Sprint sprint = tmp.taskStory.componentID.project.sprints.get(i);
	// java.util.Date End = sprint.endDate;
	// Calendar cal = new GregorianCalendar();
	// if (End.after(cal.getTime())) {
	// sprints.add(sprint);
	// }
	//
	// }
	//
	// if (validation.hasErrors()) {
	// if (tmp.description.equals("")) {
	// message = "A Task must have a description";
	// } else if (tmp.estimationPoints == 0) {
	// message = "Please enter an estimation greater than Zero";
	//
	// } else if (tmp.assignee == null || tmp.reviewer == null) {
	// message = "A task must have an assignee and a reviewer";
	// try {
	// render(request.controller.replace(".", "/") + "/blank.html", component,
	// type, stories, users, statuses, types, message, sprints, reviewers,
	// taskStory);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/blank.html", type);
	// }
	// } else if (tmp.assignee.equals(tmp.reviewer)) {
	// message = "A task can't have the same user as an assignee and reviewer";
	// }
	// try {
	// render(request.controller.replace(".", "/") + "/blank.html", component,
	// type, stories, users, statuses, types, message, sprints, reviewers,
	// taskStory);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/blank.html", type);
	// }
	// }
	// if (tmp.estimationPoints == 0) {
	// message = "Please enter an estimation greater than Zero";
	// try {
	// render(request.controller.replace(".", "/") + "/blank.html", component,
	// type, stories, users, statuses, types, message, sprints, reviewers,
	// taskStory);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/blank.html", type);
	// }
	// } else if (tmp.assignee == null || tmp.reviewer == null) {
	// message = "A task must have an assignee and a reviewer";
	// try {
	// render(request.controller.replace(".", "/") + "/blank.html", component,
	// type, stories, users, statuses, types, message, sprints, reviewers,
	// taskStory);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/blank.html", type);
	// }
	// } else if (tmp.assignee.equals(tmp.reviewer)) {
	// message = "A task can't have the same user as an assignee and reviewer";
	// try {
	// render(request.controller.replace(".", "/") + "/blank.html", component,
	// type, stories, users, statuses, types, message, sprints, taskStory);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/blank.html", type);
	// }
	// } else if (tmp.estimationPoints > 100) {
	// message = "An estimation point greater than 100 is a total nonsense !";
	// try {
	// render(request.controller.replace(".", "/") + "/blank.html", component,
	// type, stories, users, statuses, types, message, sprints, taskStory);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/blank.html", type);
	// }
	// }
	//
	// tmp.reporter = Security.getConnected();
	// System.out.println("here");
	// object.save();
	// tmp = (Task) object;
	// tmp.init();
	// Logs.addLog(tmp.reporter, "Create", "Task", tmp.id,
	// tmp.taskStory.componentID.project, new Date(System.currentTimeMillis()));
	// String header = "A new Task has been added to Story: 'S" +
	// tmp.taskStory.id + "\'" + ".";
	// String body = "In Project: " + "\'" +
	// tmp.taskStory.componentID.project.name + "\'" + "." + '\n' +
	// " In Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + "." +
	// '\n' + " Task: 'T" + tmp.id + "\'" + "." + '\n' + " Added by: " + "\'" +
	// tmp.reporter.name + "\'" + ".";
	//
	// Notifications.notifyUsers(tmp.taskStory.componentID.componentUsers,
	// header, body, (byte) 1);
	// // tmp.init();
	// flash.success(Messages.get("crud.created", type.modelName,
	// object.getEntityId()));
	// if (params.get("_save") != null) {
	// //Application.overlayKiller();
	// }
	// if (params.get("_saveAndAddAnother") != null) {
	// redirect(request.controller + ".blank");
	// }
	// redirect(request.controller + ".show", object.getEntityId());
	// }
	public static void create() throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.entityClass.newInstance();
		validation.valid( object.edit( "object", params ) );
		Task tmp = (Task) object;
		User user = Security.getConnected();
		Project project = null;
		Component component = null;
		Task task = null;
		Project p=null;
		System.out.println(tmp.parent);
		if(tmp.project!=null && tmp.parent==null){
			p = tmp.project;
			project = tmp.project;
			System.out.println("here");
			Security.check(Security.getConnected().in(tmp.project).can("AddTask"));
			
		}else{
			if(tmp.parent !=null){
				task = tmp.parent;
				project = tmp.project;
			}else{
				component = tmp.component;
				project=component.project;
				tmp.project=project;
				Security.check(user.in(component.project).can("AddTask"));
			}
		}
		List<Sprint> sprints = new ArrayList<Sprint>();
		for( int i = 0; i < project.sprints.size(); i++ )
		{
			Sprint sprint = project.sprints.get( i );
			java.util.Date End = sprint.endDate;
			Calendar cal = new GregorianCalendar();
			if( End != null && End.after( cal.getTime() ) )
			{
				sprints.add( sprint );
			}
		}
		String newdesc = tmp.description;
		String productRoles = "";
		for (int i = 0; i < project.productRoles.size(); i++) 
		{
			if(project.productRoles.get(i).name.charAt(0)=='a'||project.productRoles.get(i).name.charAt(0)=='e'||project.productRoles.get(i).name.charAt(0)=='i'||project.productRoles.get(i).name.charAt(0)=='o'||project.productRoles.get(i).name.charAt(0)=='u'||project.productRoles.get(i).name.charAt(0)=='A'||project.productRoles.get(i).name.charAt(0)=='E'||project.productRoles.get(i).name.charAt(0)=='I'||project.productRoles.get(i).name.charAt(0)=='O'||project.productRoles.get(i).name.charAt(0)=='U')
			productRoles=productRoles+"As an "+project.productRoles.get(i).name+",-";
			else
			productRoles=productRoles+"As a "+project.productRoles.get(i).name+",-";
		}

		
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/blank.html",  project, p, component, task, type, sprints, productRoles );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		tmp.init();
		// System.out.println(tmp.parent.id + "toffa7");
		
		String[] desc = newdesc.split(",");
		if (desc.length == 1) {
		 tmp.description = desc[0];
		 } else {
		 String[] desc2 = desc[0].split(" ");
		 if (desc2.length >= 3) {
		 if (desc2[0].equalsIgnoreCase("as") && (desc2[1].equalsIgnoreCase("a") ||
		 desc2[1].equalsIgnoreCase("an"))) {
		 boolean flag = false;
		 String productrole = "";
		 for (int k = 2; k < desc2.length; k++) {
		 if (k == desc2.length - 1)
		 productrole = productrole + desc2[k];
		 else
		 productrole = productrole + desc2[k] + " ";
		
		 }
		 for (int j = 0; j < tmp.project.productRoles.size(); j++) {
		 if
		 (tmp.project.productRoles.get(j).name.equalsIgnoreCase(productrole))
		 flag = true;
		 }
		 if (!flag) {
		 ProductRole pr = new ProductRole(tmp.project.id,
		 productrole, "");
		 pr.save();
		 tmp.productRole = pr;
		 } else {
		 for (int j = 0; j < tmp.project.productRoles.size(); j++) {
		 if
		 (tmp.project.productRoles.get(j).name.equalsIgnoreCase(productrole))
		 {
		 tmp.productRole = tmp.project.productRoles.get(j);
		 }
		 }
		 }
		 for (int i = 1; i < desc.length; i++) {
		 tmp.description = desc[i] + " ";
		 }
		 }
		 } else {
		 tmp.description = tmp.description;
		 }
		 }
		tmp.reporter=Security.getConnected();
		object.save();
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{

			if(tmp.parent!=null)
			Application.overlayKiller("reload('tasks','task-"+tmp.parent.id+"')", "");
			else
				Application.overlayKiller("reload('tasks','task-"+"')", "");
		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			redirect( request.controller + ".blank" );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/*
	 * * A Method that renders the form of editting a Task.
	 * @author Monayri
	 * @category C3 17.1
	 */
	public static void show( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		List<User> users = null;
		if(tmp.component.id==1)
			users = tmp.project.users;
		else
			users = tmp.component.componentUsers;
		
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Task> dependencies = new ArrayList<Task>();
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		if( comments == null )
			comments = new ArrayList<Comment>();
		String message2 = "Are you Sure you want to delete the task ?!";
		List<Requestreviewer> reviewers = Requestreviewer.find( "byComponentAndAccepted", tmp.component, true ).fetch();
		boolean deletable = tmp.isDeletable();
		dependencies = Task.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		String productRoles = "";
		for( int i = 0; i < tmp.project.productRoles.size(); i++ )
		{
			if( tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'a' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'e' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'i' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'o' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'u' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'A' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'E' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'I' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'O' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + tmp.project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + tmp.project.productRoles.get( i ).name + ",-";
		}
		try
		{
			render( type, object, users, statuses, types, dependencies, message2, deletable, reviewers, comments, productRoles );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/show.html", type, object );
		}
	}

	/**
	 * A Method that checks the validation of input data done by user in the
	 * edit Task form, if its correct it saves the changes.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 */
	// public static void save(String id) throws Exception {
	// String changes = "";
	// ObjectType type = ObjectType.get(getControllerClass());
	// notFoundIfNull(type);
	// JPASupport object = type.findById(id);
	// Task tmp = (Task) object;
	// Security.check(Security.getConnected().in(tmp.taskStory.componentID.project).can("modifyTask"));
	// String oldDescription = tmp.description;// done
	// long oldTaskType = tmp.taskType.id;// done
	// long oldTaskStatus = tmp.taskStatus.id;// done
	// double oldEstPoints = tmp.estimationPoints;// done
	// long oldAssignee = tmp.assignee.id;// done
	// long oldReviewer = tmp.reviewer.id;// done
	// ArrayList<Task> oldDependencies = new ArrayList<Task>();
	// for (Task current : tmp.dependentTasks) {
	// oldDependencies.add(current);
	// }
	// validation.valid(object.edit("object", params));
	// List<User> users = tmp.taskStory.componentID.componentUsers;
	// List<TaskStatus> statuses =
	// tmp.taskStory.componentID.project.taskStatuses;
	// List<TaskType> types = tmp.taskStory.componentID.project.taskTypes;
	// User myUser = Security.getConnected();
	// List<Requestreviewer> reviewers =
	// Requestreviewer.find("byComponentAndAccepted", tmp.taskStory.componentID,
	// true).fetch();
	// List<Task> dependencies = new ArrayList<Task>();
	// List<Comment> comments = Comment.find("byTask", tmp).fetch();
	// if (comments == null)
	// comments = new ArrayList<Comment>();
	// String message = "";
	// String message2 = "Are you Sure you want to delete the task ?!";
	// boolean deletable = tmp.isDeletable();
	// boolean pAdmin = false;
	// for (int i = 0; i < myUser.roles.size(); i++) {
	// if (myUser.roles.get(i).project.id ==
	// tmp.taskStory.componentID.project.id &&
	// myUser.roles.get(i).name.equalsIgnoreCase("project Admin"))
	// pAdmin = true;
	// }
	// boolean isReporter = tmp.reporter.equals(myUser) || pAdmin ||
	// myUser.isAdmin;
	// boolean isAssignee = tmp.assignee.equals(myUser) || pAdmin ||
	// myUser.isAdmin;
	// boolean isReviewer = tmp.assignee.equals(myUser) || pAdmin ||
	// myUser.isAdmin;
	// for (int i = 0; i < tmp.taskStory.dependentStories.size(); i++) {
	// for (int j = 0; j <
	// tmp.taskStory.dependentStories.get(i).storiesTask.size(); j++) {
	// dependencies.add(tmp.taskStory.dependentStories.get(i).storiesTask.get(j));
	// }
	// }
	// dependencies.addAll(tmp.taskStory.storiesTask);
	// if (validation.hasErrors()) {
	// if (tmp.description.equals("") || tmp.description.equals(null)) {
	// message = "A Task must have a description";
	// } else if (tmp.estimationPoints == 0) {
	// message = "Please enter an estimation greater than Zero";
	//
	// } else if (tmp.assignee.equals(tmp.reviewer)) {
	// message = "A task can't have the same user as an assignee and reviewer";
	// }
	// try {
	//
	// render(request.controller.replace(".", "/") + "/show.html", type, object,
	// users, statuses, types, dependencies, message2, deletable, reviewers,
	// message, isReporter, isReviewer, isAssignee, comments);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/show.html", type);
	// }
	// }
	// if (tmp.estimationPoints == 0) {
	// message = "Please enter an estimation greater than Zero";
	// try {
	// render(request.controller.replace(".", "/") + "/show.html", type, object,
	// users, statuses, types, dependencies, message2, deletable, reviewers,
	// message, isReporter, isReviewer, isAssignee, comments);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/show.html", type);
	// }
	// } else if (tmp.assignee.equals(tmp.reviewer)) {
	// message = "A task can't have the same user as an assignee and reviewer";
	// try {
	// render(request.controller.replace(".", "/") + "/show.html", type, object,
	// users, statuses, types, dependencies, message2, deletable, reviewers,
	// message, isReporter, isReviewer, isAssignee, comments);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/show.html", type);
	// }
	// } else if ((tmp.taskStatus.name.equals("Closed") ||
	// tmp.taskStatus.name.equals("Verified") ||
	// tmp.taskStatus.name.equals("Reopened"))) {
	// message = "Only Task reviewer can set the task to this status";
	// boolean Check = Security.check(tmp.taskStory.componentID.project,
	// "changeTaskStatus");
	// if (!Check) {
	// try {
	// render(request.controller.replace(".", "/") + "/show.html", type, object,
	// users, statuses, types, dependencies, message2, deletable, reviewers,
	// message, isReporter, isReviewer, isAssignee, comments);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/show.html", type);
	// }
	// }
	//
	// } else {
	// if (tmp.estimationPoints > 100) {
	// message = "An estimation greater than 100 is a total nonsense";
	// try {
	// render(request.controller.replace(".", "/") + "/show.html", type, object,
	// users, statuses, types, dependencies, message2, deletable, reviewers,
	// message, isReporter, isReviewer, isAssignee, comments);
	// } catch (TemplateNotFoundException e) {
	// render("CRUD/show.html", type);
	// }
	// }
	// // if (!Security.check(tmp.taskStory.componentID.project,
	// // "changeTaskStatus")) {
	// // message = "Only Task Assignee can set the task to this status";
	// // // boolean Check = Security.check("CanSetStatusTo" +
	// // // tmp.taskStatus.name);
	// // // if (!Check) {
	// // try {
	// // render(request.controller.replace(".", "/") + "/show.html", type,
	// // object, users, statuses, types, dependencies, message2,
	// // deletable, reviewers, message, isReporter, isReviewer,
	// // isAssignee, comments);
	// // } catch (TemplateNotFoundException e) {
	// // render("CRUD/show.html", type);
	// // }
	// // // }
	// //
	// // } else {
	// // // removed from here
	// // }
	// }
	// String header = "Task: 'T" + tmp.id + "\'" + " has been edited.";
	// String body = "In Project: " + "\'" +
	// tmp.taskStory.componentID.project.name + "\'" + "." + '\n' +
	// " In Component: " + "\'" + tmp.taskStory.componentID.name + "\'" + "." +
	// '\n' + " Story: 'S" + tmp.taskStory.id + "\'" + "." + '\n' +
	// " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
	// /*
	// * ////Long Informative Notification message. Not suitable for online
	// * notification. String header = "A Task has been edited in Component: "
	// * + "\'" + tmp.taskStory.componentID.name + "\'" + " in Project: " +
	// * "\'" + tmp.taskStory.componentID.project.name + "\'" + "."; String
	// * body = "The Task:" + '\n' + " " + "\'" + oldDescription + "\'" + '\n'
	// * + " has been edited in Component: " + "\'" +
	// * tmp.taskStory.componentID.name + "\'" + " in Project: " + "\'" +
	// * tmp.taskStory.componentID.project.name + "\'" + "." + '\n' + '\n' +
	// * "Task Description: " + tmp.description + "." + '\n' + " Story: " +
	// * tmp.taskStory.description + "." + '\n' + " Type: " +
	// * tmp.taskType.name + "." + '\n' + " Status: " + tmp.taskStatus.name+
	// * "." + '\n' + " Sprint: " + tmp.taskSprint.sprintNumber+ "." + '\n' +
	// * " Assignee: " + tmp.assignee.name + "." + '\n' + " Reporter: " +
	// * tmp.reporter.name + "." + '\n' + " Reviewer: " + tmp.reviewer.name +
	// * "." + '\n' + " Edited by: " + tmp.reporter.name + ".";
	// */
	// object.save();
	// /*********** Changes as Comment by Galal Aly **************/
	//
	// if (!(tmp.description.equals(oldDescription)))
	// changes += "Description changed from <i>" + oldDescription +
	// "</i> to <i>" + tmp.description + "</i><br>";
	// if (tmp.taskType.id != oldTaskType) {
	// TaskType temp = TaskType.findById(oldTaskType);
	// changes += "Task's Type was changed from <i>" + temp.name + "</i> to <i>"
	// + tmp.taskType.name + "</i><br>";
	// }
	// if (tmp.taskStatus.id != oldTaskStatus) {
	// TaskStatus temp = TaskStatus.findById(oldTaskStatus);
	// changes += "Task's status was changed from <i>" + temp.name +
	// "</i> to <i>" + tmp.taskStatus.name + "</i><br>";
	// }
	// if (tmp.estimationPoints != oldEstPoints)
	// changes += "Estimation points for the task were changed from <i>" +
	// oldEstPoints + "</i> to <i>" + tmp.estimationPoints + "</i><br>";
	// if (tmp.assignee.id != oldAssignee) {
	// User temp = User.findById(oldAssignee);
	// changes += "Task's assignee was changed from <i>" + temp.name +
	// "</i> to <i>" + tmp.assignee.name + "</i><br>";
	// }
	// if (tmp.reviewer.id != oldReviewer) {
	// User temp = User.findById(oldReviewer);
	// changes += "Task's reviewer was changed from <i>" + temp.name +
	// "</i> to <i>" + tmp.reviewer.name + "</i><br>";
	// }
	// for (Task oldTask : oldDependencies) {
	// if (!(tmp.dependentTasks.contains(oldTask))) {
	// changes += "Task " + oldTask.number +
	// " was removed from Dependent tasks.<br>";
	// }
	// }
	// for (Task newTask : tmp.dependentTasks) {
	// if (!(oldDependencies.contains(newTask))) {
	// changes += "Task " + newTask.number +
	// " was added to dependent tasks.<br>";
	// }
	// }
	//
	// // Now finally save the comment
	// Comment changesComment = new Comment(Security.getConnected(), tmp.id,
	// changes);
	// changesComment.save();
	// /********** End of Changes as Comment ********/
	// if (tmp.comment.trim().length() > 0) {
	// Comment comment = new Comment(Security.getConnected(), tmp.id,
	// tmp.comment);
	// comment.save();
	// }
	// Logs.addLog(myUser, "Edit", "Task", tmp.id, tmp.project, new
	// Date(System.currentTimeMillis()));
	// Notifications.notifyUsers(tmp.component.componentUsers, header, body,
	// (byte) 0);
	// flash.success(Messages.get("crud.saved", type.modelName,
	// object.getEntityId()));
	// if (params.get("_save") != null)
	//
	// {
	// //Application.overlayKiller();
	//
	// }
	// redirect(request.controller + ".show", object.getEntityId());
	// }
	public static void save( String id ) throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		String changes="";
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		List<User> users = tmp.component.componentUsers;
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Task> dependencies = Task.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		String message2 = "Are you Sure you want to delete the task ?!";
		List<Requestreviewer> reviewers = Requestreviewer.find( "byComponentAndAccepted", tmp.component, true ).fetch();
		boolean deletable = tmp.isDeletable();
		 String oldDescription = tmp.description;// done
		 long oldTaskType;
		 if(tmp.taskType != null)
			 oldTaskType = tmp.taskType.id;// done
		 else
			 oldTaskType = 0;
		 long oldTaskStatus;
		 if(tmp.taskStatus != null)
			 oldTaskStatus = tmp.taskStatus.id;// done
		 else
			 oldTaskStatus = 0;
		 double oldEstPoints = tmp.estimationPoints;// done
		 long oldAssignee;
		 if(tmp.assignee != null)
			 oldAssignee = tmp.assignee.id;// done
		 else
			 oldAssignee = 0;
		 long oldReviewer;
		 if(tmp.reviewer != null)
			 oldReviewer = tmp.reviewer.id;// done
		 else
			 oldReviewer = 0;
		 ArrayList<Task> oldDependencies = new ArrayList<Task>();
		 for (Task current : tmp.dependentTasks) {
			 oldDependencies.add(current);
		 }
		object = object.edit( "object", params );
		// Look if we need to deserialize
//		for( ObjectType.ObjectField field : type.getFields() )
//		{
//			if( field.type.equals( "serializedText" ) && params.get( "object." + field.name ) != null )
//			{
//				Field f = object.getClass().getDeclaredField( field.name );
//				f.set( object, CRUD.collectionDeserializer( params.get( "object." + field.name ), (Class) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0] ) );
//			}
//		}
		
		validation.valid( object );
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/show.html", type, object, users, statuses, types, dependencies, message2, deletable, reviewers, comments );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object );
			}
		}
		object.save();
		/*********** Changes as Comment by Galal Aly **************/
		String newdesc = tmp.description;
		String[] desc = newdesc.split(",");
		if (desc.length == 1) {
		 tmp.description = desc[0];
		 } else {
		 String[] desc2 = desc[0].split(" ");
		 if (desc2.length >= 3) {
		 if (desc2[0].equalsIgnoreCase("as") && (desc2[1].equalsIgnoreCase("a") ||
		 desc2[1].equalsIgnoreCase("an"))) {
		 boolean flag = false;
		 String productrole = "";
		 for (int k = 2; k < desc2.length; k++) {
		 if (k == desc2.length - 1)
		 productrole = productrole + desc2[k];
		 else
		 productrole = productrole + desc2[k] + " ";
		
		 }
		 for (int j = 0; j < tmp.project.productRoles.size(); j++) {
		 if
		 (tmp.project.productRoles.get(j).name.equalsIgnoreCase(productrole))
		 flag = true;
		 }
		 if (!flag) {
		 ProductRole pr = new ProductRole(tmp.project.id,
		 productrole, "");
		 pr.save();
		 tmp.productRole = pr;
		 } else {
		 for (int j = 0; j < tmp.project.productRoles.size(); j++) {
		 if
		 (tmp.project.productRoles.get(j).name.equalsIgnoreCase(productrole))
		 {
		 tmp.productRole = tmp.project.productRoles.get(j);
		 }
		 }
		 }
		 for (int i = 1; i < desc.length; i++) {
		 tmp.description = desc[i] + " ";
		 }
		 }
		 } else {
		 tmp.description = tmp.description;
		 }
		 }

		tmp.save();
		 if (!(tmp.description.equals(oldDescription)))
		 changes += "Description changed from <i>" + oldDescription +
		 "</i> to <i>" + tmp.description + "</i><br>";
		 if(tmp.taskType != null && oldTaskType != 0)
			 if (tmp.taskType.id != oldTaskType) {
			 TaskType temp = TaskType.findById(oldTaskType);
			 changes += "Task's Type was changed from <i>" + temp.name + "</i> to <i>"
			 + tmp.taskType.name + "</i><br>";
			 }
		 if(tmp.taskStatus != null && oldTaskStatus != 0)
			 if (tmp.taskStatus.id != oldTaskStatus) {
			 TaskStatus temp = TaskStatus.findById(oldTaskStatus);
			 changes += "Task's status was changed from <i>" + temp.name +
			 "</i> to <i>" + tmp.taskStatus.name + "</i><br>";
			 }
		 if (tmp.estimationPoints != oldEstPoints)
		 changes += "Estimation points for the task were changed from <i>" +
		 oldEstPoints + "</i> to <i>" + tmp.estimationPoints + "</i><br>";
		 if(tmp.assignee != null && oldAssignee != 0){
			 if (tmp.assignee.id != oldAssignee) {
				 User temp = User.findById(oldAssignee);
				 changes += "Task's assignee was changed from <i>" + temp.name +
				 "</i> to <i>" + tmp.assignee.name + "</i><br>";
				 }
		 }
		 else if(tmp.assignee != null && oldAssignee == 0){
			 changes += "Task's assignee is now <i>" + tmp.assignee.name + "</i><br>";
		 }
		 if(tmp.reviewer != null && oldReviewer != 0){
			 if (tmp.reviewer.id != oldReviewer) {
			 User temp = User.findById(oldReviewer);
			 changes += "Task's reviewer was changed from <i>" + temp.name +
			 "</i> to <i>" + tmp.reviewer.name + "</i><br>";
			 }
		 }
		 else if(tmp.reviewer != null && oldReviewer == 0){
			 changes += "Task's reviewer is now <i>" + tmp.reviewer.name + "</i><br>";
		 }
		 for (Task oldTask : oldDependencies) {
		 if (!(tmp.dependentTasks.contains(oldTask))) {
		 changes += "Task " + oldTask.number +
		 " was removed from Dependent tasks.<br>";
		 }
		 }
		 for (Task newTask : tmp.dependentTasks) {
		 if (!(oldDependencies.contains(newTask))) {
		 changes += "Task " + newTask.number +
		 " was added to dependent tasks.<br>";
		 }
		 }
		
		 // Now finally save the comment
		 if(!changes.equals(""))
		 {
			 Comment changesComment = new Comment(Security.getConnected(), tmp.id,
		 changes);
		 	changesComment.save();
		 }
		// /********** End of Changes as Comment ********/
		if(tmp.comment.trim().length() != 0){
			Comment comment = new Comment(Security.getConnected(), tmp.id, tmp.comment);
			comment.save();
		}
		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			Application.overlayKiller( "reload('tasks','task-" + tmp.id + "')", "" );
			Logs.addLog( tmp.project, "edit", "Task", tmp.id );
		}
	}

	/**
	 * A Method that deletes a Task
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return its a void method.
	 */
	public static void delete( long id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		try
		{
			tmp.deleted = true;
			String header = "Task: 'T" + tmp.id + "\'" + " has been deleted.";
			String body = "In Project: " + "\'" + tmp.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + tmp.component.name + "\'" + "." + '\n' + "." + '\n' + " Deleted by: " + "\'" + Security.getConnected().name + "\'" + ".";
			Logs.addLog( Security.getConnected(), "delete", "Task", tmp.id, tmp.project, new Date( System.currentTimeMillis() ) );
			// Notifications.notifyUsers( tmp.component.componentUsers, header,
			// body, (byte) -1 );
			object.save();
			String text = "The Task was deleted successfully";
			System.out.println( "here" );
			renderText( text );
		}
		catch( Exception e )
		{
			flash.error( Messages.get( "crud.delete.error", type.modelName, object.getEntityId() ) );
			renderText( "Task can't be deleted" );
		}
		flash.success( Messages.get( "crud.deleted", type.modelName, object.getEntityId() ) );
		renderText( "Task deleted successfully." );
	}

	public static void reviewers( long id, long id2 )
	{
		List<User> users= null;
		Component component = Component.findById( id );
		User Assignee = User.findById( id2 );
		if(id==1)
		users = component.project.users;
		else
		users = component.componentUsers;
		List<User.Object> reviewers = new ArrayList<User.Object>();
		for( User user : users )
		{
			if( user != Assignee )
			{
				reviewers.add( new User.Object( user.id, user.name ) );
			}
		}
		renderJSON( reviewers );
	}
	public static void setDependency( long id, long id2 )
	{	
		Task taskFrom = Task.findById(id);
		Task taskTo = Task.findById(id2);
		Security.check( Security.getConnected().in( taskFrom.project ).can( "modifyTask" ) );
		taskFrom.dependentTasks.add(taskTo);
		taskFrom.save();
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
	public static void enterEffort( long id, double effort, int day )
	{
		Task temp = Task.findById( id );
		Security.check( Security.getConnected().in( temp.project ).can( "modifyTask" ) || temp.assignee == Security.getConnected() );
		User userWhoChanged = Security.getConnected();
		Component t = temp.component;

		Security.check( t.componentUsers.contains( userWhoChanged ) );

		Calendar timeChanged = Calendar.getInstance();
		String changeType = "";

		if( temp.getEffortPerDay( day ) != -1 )
		{
			changeType = "Edit Attribute Effort";
		}
		else
		{
			changeType = "Insert Attribute Effort";
		}

		temp.setEffortOfDay( effort, day );
		temp.save();
		Logs.addLog( userWhoChanged, changeType, "Task", id, temp.taskSprint.project, timeChanged.getTime() );

	}

	/**
	 * Fetches all the data needed to generate a report on a given task.
	 * 
	 * @category C4 S15
	 * @author Hadeer Younis
	 * @param id
	 *            The id of the task whose report will be generated.
	 */
	public static void getReport( long id )
	{
		List<Log> temp = Log.findAll();
		Task theTask = Task.findById( id );
		Security.check( theTask.taskStatus.project.users.contains( Security.getConnected() ) );
		if( theTask.deleted )
			notFound();
		boolean empty = temp.isEmpty();
		String lastModified = null;
		int numberOfModifications = 0;
		String efforts = "[";
		boolean flag = false;
		double n = theTask.getEffortPerDay( 0 );
		String changes = "[";
		if( theTask.taskSprint != null )
		{
			for( int j = 0; j < theTask.taskSprint.getDuration(); j++ )
			{
				if( !flag )
					n = theTask.getEffortPerDay( j );
				if( n == -1 )
				{
					flag = true;
					n = theTask.getEffortPerDay( j - 1 );
				}
				if( j == theTask.taskSprint.getDuration() - 1 )
					efforts = efforts + "[" + j + "," + n + "]]";
				else
					efforts = efforts + "[" + j + "," + n + "],";
			}
		}
		else
		{
			efforts = "[]";
		}
		for( int i = 0; i < temp.size(); i++ )
		{
			if( temp.get( i ).resource_id != id )
				temp.remove( i );
		}
		for( int i = 0; i < temp.size(); i++ )
		{
			int k = 1;
			if( i < temp.size() - 2 )
			{
				keepLoop : while( temp.get( i ).date.toString().substring( 0, 10 ).equals( temp.get( i + 1 ).date.toString().substring( 0, 10 ) ) )
				{
					i++;
					if( i == temp.size() - 1 )
						break keepLoop;
					k++;
				}
			}
			if( i == temp.size() - 1 )
				changes = changes + "['" + temp.get( i ).date.toString().substring( 0, 10 ) + "'," + k + "]]";
			else
				changes = changes + "['" + temp.get( i ).date.toString().substring( 0, 10 ) + "'," + k + "],";

		}

		if( !empty )
		{
			lastModified = temp.get( temp.size() - 1 ).date.toString().substring( 0, 10 ) + " @ " + temp.get( temp.size() - 1 ).date.toString().substring( 11 );
			numberOfModifications = temp.size();
		}
		Date maxdate = temp.get( temp.size() - 1 ).date;
		maxdate.setTime( temp.get( temp.size() - 1 ).date.getTime() + (3 * 86400000) );
		String maxDate = maxdate.toString().substring( 0, 10 );

		Date mindate = temp.get( 0 ).date;
		mindate.setTime( temp.get( 0 ).date.getTime() - (3 * 86400000) );
		String minDate = mindate.toString().substring( 0, 10 );

		Project myProject = theTask.taskType.project;
		render( myProject, minDate, temp, lastModified, empty, efforts, changes, numberOfModifications, theTask, maxDate );
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
	public static boolean editTaskDesc( long id, String desc )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		// String oldDescription = task1.description;
		task1.description = desc;
		task1.save();
		String header = "Task: 'T" + task1.id + "\'" + " Description has been edited.";
		String body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";

		Logs.addLog( Security.getConnected(), "Edit", "Task Description", id, task1.project, new Date( System.currentTimeMillis() ) );
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
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
	public static void editTaskDescJSON( long id, String desc )
	{
		String zero = "0";
		String one = "1";
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			renderJSON( zero );
		task1.description = desc;
		task1.save();
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		// Notifications.notifyUsers(m, "TASk editing", "task " + id +
		// " description is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.project;
		User myUser = Security.getConnected();
		Logs.addLog( myUser, "EditDesc", "Task", id, y, cal.getTime() );
		renderJSON( one );

	}

	/**
	 * This method changes the given task description.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            the id of the given task.
	 * @param userId
	 *            the id of the user who will do the change in description.
	 * @param desc
	 *            The new description.
	 * @return boolean
	 * @story C3S36
	 */
	public static boolean editTaskDesc2( long id, long userId, String desc )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskDescreption" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}
		task1.description = desc;
		task1.save();
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		String body = "";
		String header = "Task: 'T" + task1.id + "\'" + " Task Type has been edited.";
		if( userId == Security.getConnected().id )
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";

		}
		else
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		if( userId == Security.getConnected().id )
		{
			Logs.addLog( user1, "Edit", "Task Description", id, task1.project, new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Logs.addLog( user1 + " has performed action (Edit) using resource (Task Description) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
		}
		return true;
	}

	/**
	 * This method changes the given task type.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            the id of the given task.
	 * @param type
	 *            The new Task Type.
	 * @param userId
	 *            the id of the user who will change the task Type.
	 * @return boolean
	 * @story C3S36
	 */
	public static boolean editTaskType( long id, long typeId, long userId )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskType" );
		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}
		TaskType type = TaskType.findById( typeId );
		task1.taskType = type;
		task1.save();
		String body = "";
		String header = "Task: 'T" + task1.id + "\'" + " Task Type has been edited.";
		// String header = "A Task Type has been edited in Component: " + "\'" +
		// task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" +
		// task1.taskStory.componentID.project.name + "\'" + ".";
		if( userId == Security.getConnected().id )
		{
			Logs.addLog( user1, "Edit", "Task Type", id, task1.project, new Date( System.currentTimeMillis() ) );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";

		}
		else
		{
			Logs.addLog( user1 + " has performed action (Edit) using resource (Task Type) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
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
	public static void editTaskTypeJSON( long id, long typeId, long userId )
	{
		String zero = "0";
		String one = "1";
		TaskType type = TaskType.findById( typeId );
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		Security.check( task1.project == type.project );
		if( task1 == null )
			renderJSON( zero );

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			renderJSON( zero );

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskType" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				renderJSON( zero );

		}
		task1.taskType = type;
		task1.save();

		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		// Notifications.notifyUsers(m, "TASK editing", "task " + id +
		// " task type is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.project;
		User myUser = Security.getConnected();
		Logs.addLog( myUser, "EditTasktype", "Task", id, y, cal.getTime() );
		renderJSON( one );

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
	public static void changeTaskStatusHelper( long id, int columnSequence, String taskString, long user_id )
	{
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}

		// setting the variable needed for the method
		// defining the appropriate sprint
		Sprint s = Sprint.findById( id );
		// defining the project of the sprint
		Project p = s.project;
		// defining the board of the project
		Board b = p.board;
		// defining the status
		TaskStatus status = new TaskStatus();
		// defining the cols of the board only
		// List<Column> cols = b.columns;
		// defining the final task id and its helper string
		String task_id_helper[];
		String task_id_helper2[];
		long task_id;
		// defining a flag for the second loop
		// boolean flag = true;

		// getting the actual status
		Column col;
		col = Column.find( "bySequenceAndBoard", columnSequence, b ).first();
		status = col.taskStatus;

		// get the actual task_id in an int
		task_id_helper = taskString.split( "_" );
		task_id_helper2 = task_id_helper[0].split( "-" );
		task_id = Integer.parseInt( task_id_helper2[1] );

		editTaskStatus( task_id, user_id, status );
	}

	/**
	 * this method filter the method for taskid and user_id and the new assignee
	 * and gives them to editTaskAssignee
	 * 
	 * @author josephhajj
	 * @param id
	 * @param taskString
	 * @param user_id
	 * @param row
	 */
	public static void changeTaskAssigneeHelper( long id, String taskString, long user_id, int row )
	{
		// if user is not selected take the one in the session
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}

		// getting the whole list of users
		// User user = User.findById(user_id);
		Component component = Component.findById( id );
		List<User> users = component.componentUsers;

		String task_id_helper[];
		String task_id_helper2[];
		long task_id;

		// filtering the task id
		task_id_helper = taskString.split( "_" );
		task_id_helper2 = task_id_helper[0].split( "-" );
		task_id = Integer.parseInt( task_id_helper2[1] );

		// calling the method
		editTaskAssignee2( task_id, user_id, users.get( row ).id );

	}

	/**
	 * This method changes the given task status.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            the id of the given task.
	 * @param newStatus
	 *            The new task status.
	 * @param userId
	 *            the id of the user who will change the task status.
	 * @return boolean story C3S36
	 */
	public static boolean editTaskStatus( long id, long userId, TaskStatus newStatus )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskStatus" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}

		if( newStatus.name.equals( "New" ) && user1.id != task1.assignee.id )
			if( !permession )
				return false;
		if( newStatus.name.equals( "Started" ) && user1.id != task1.assignee.id )
			if( !permession )
				return false;
		if( newStatus.name.equals( "Resovled" ) && user1.id != task1.assignee.id )
			if( !permession )
				return false;

		if( newStatus.name.equals( "Reopened" ) && user1.id != task1.reviewer.id )
			if( !permession )
				return false;

		if( newStatus.name.equals( "Verified" ) && user1.id != task1.reviewer.id )
			if( !permession )
				return false;

		if( newStatus.name.equals( "Closed" ) && user1.id != task1.reviewer.id )
			if( !permession )
				return false;

		// if (newStatus.name.equals("Reopened"))
		// task1.taskStory.done = false;
		// String oldStatus = task1.taskStatus.name;
		task1.taskStatus = newStatus;
		task1.save();

		// if (newStatus != null && newStatus.name == "Closed") {
		// StoryComplete(id);
		// }
		// // if (newStatus.name.equals("Reopened"))
		// task1.taskStory.done = false;
		String body = "";
		// String header = "A Task Status has been edited in Component: " + "\'"
		// + task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" +
		// task1.taskStory.componentID.project.name + "\'" + ".";
		String header = "Task: 'T" + task1.id + "\'" + " Task Status has been edited.";
		if( userId == Security.getConnected().id )
		{
			Logs.addLog( user1, "Edit", "Task Status", id, task1.project, new Date( System.currentTimeMillis() ) );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";
		}
		else
		{
			Logs.addLog( user1 + " has performed action (Edit) using resource (Task Status) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
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
	public static void editTaskStatusJSON( long id, long userId, long statusId )
	{
		String zero = "0";
		String one = "1";
		TaskStatus newStatus = TaskStatus.findById( statusId );
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			renderJSON( zero );
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			renderJSON( zero );

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskStatus" );

		if( task1.reviewer.id != userId && task1.assignee.id != userId )
		{
			if( !permession )
				renderJSON( zero );
		}

		if( newStatus.name.equals( "New" ) && user1.id != task1.assignee.id )
			if( !permession )
				renderJSON( zero );
		if( newStatus.name.equals( "Started" ) && user1.id != task1.assignee.id )
			if( !permession )
				renderJSON( zero );
		if( newStatus.name.equals( "Resovled" ) && user1.id != task1.assignee.id )
			if( !permession )
				renderJSON( zero );

		if( newStatus.name.equals( "Reopened" ) && user1.id != task1.reviewer.id )
			if( !permession )
				renderJSON( zero );

		if( newStatus.name.equals( "Verified" ) && user1.id != task1.reviewer.id )
			if( !permession )
				renderJSON( zero );

		if( newStatus.name.equals( "Closed" ) && user1.id != task1.reviewer.id )
			if( !permession )
				renderJSON( zero );

		// if (newStatus.name.equals("Reopened"))
		// task1.taskStory.done = false;

		task1.taskStatus = newStatus;
		task1.save();

		// if (newStatus != null && newStatus.name == "Closed") {
		// StoryComplete(id);
		// }
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		// Notifications.notifyUsers(m, "TASK editing", "task " + id +
		// " taskstatus is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.project;

		Logs.addLog( user1, "Edit task status", "Task", id, y, cal.getTime() );
		renderJSON( one );

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
	public static boolean editTaskEstimation( long id, double estimation )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		// Double oldEstimation = task1.estimationPoints;
		if( estimation < 0 )
			return false;
		task1.estimationPoints = estimation;
		task1.save();
		String header = "Task: 'T" + task1.id + "\'" + " Estimation Points have been edited.";
		String body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";
		Logs.addLog( Security.getConnected(), "Edit", "Task estimation", id, task1.project, new Date( System.currentTimeMillis() ) );
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
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
	public static void editTaskEstimationJSON( long id, double estimation )
	{
		String one = "1";
		String zero = "0";
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			renderJSON( zero );
		if( estimation < 0 )
			renderJSON( zero );
		task1.estimationPoints = estimation;
		task1.save();
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		// Notifications.notifyUsers(m, "TASK editing", "task " + id +
		// " estimation points is edited", (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.project;
		User myUser = Security.getConnected();
		Logs.addLog( myUser, "Edit task estimation", "Task", id, y, cal.getTime() );
		renderJSON( one );
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
	public static boolean editTaskAssignee( long id, long assigneeId )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		User assignee = User.findById( assigneeId );
		if( assignee == null )
			return false;
		if( task1.reviewer.getId() == assigneeId )
			return false;
		// String oldAssignee = task1.assignee.name;
		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add( task1 );
		assignee.save();
		String header = "Task: 'T" + task1.id + "\'" + " Assignee has been edited.";
		String body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";

		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		Logs.addLog( Security.getConnected(), "Edit", "Task Assignee", id, task1.project, new Date( System.currentTimeMillis() ) );
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
	public static void editTaskAssigneeJSON( long id, long assigneeId )
	{
		String zero = "0";
		String one = "1";
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			renderJSON( zero );
		User assignee = User.findById( assigneeId );
		if( assignee == null )
			renderJSON( zero );
		if( task1.reviewer.getId() == assigneeId )
			renderJSON( zero );

		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add( task1 );
		assignee.save();
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		// Notifications.notifyUsers(m, "TASK editing", "task " + id +
		// " assignee is now changed to" + assignee.email, (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.project;
		User myUser = Security.getConnected();
		Logs.addLog( myUser, "change  task assignee", "Task", id, y, cal.getTime() );
		renderJSON( one );
	}

	/**
	 * This method changes the given task assignee.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            the id of the given task.
	 * @param userId
	 *            the id of the user who will do the change.
	 * @param assigneId
	 *            the id of the user who will be the assignee of the task.
	 * @return boolean
	 * @story C3S36
	 */
	public static boolean editTaskAssignee2( long id, long userId, long assigneeId )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		User assignee = User.findById( assigneeId );
		if( assignee == null )
			return false;
		if( task1.reviewer.getId() == assigneeId )
			return false;

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeAssignee" );

		if( !permession )
			return false;
		// String oldAssignee = task1.assignee.name;
		task1.assignee = assignee;
		task1.save();
		assignee.tasks.add( task1 );
		assignee.save();
		String header = "Task: 'T" + task1.id + "\'" + " Assignee has been edited.";
		/*
		 * ////Long Informative Notification message. Not suitable for online
		 * notification. String header =
		 * "A Task Assignee has been changed in Component: " + "\'" +
		 * task1.taskStory.componentID.name + "\'" + " in Project: " + "\'" +
		 * task1.taskStory.componentID.project.name + "\'" + ".";
		 */
		String body = "";
		if( userId == Security.getConnected().id )
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";
		}
		else
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";
		}

		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		if( userId == Security.getConnected().id )
		{
			Logs.addLog( user1, "Edit", "Task Assignee", id, task1.project, new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Logs.addLog( user1 + " has performed action (Edit) using resource (Task Assignee) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
		}
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
	public static boolean editTaskReviewer( long id, long reviewerId )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		User reviewer = User.findById( reviewerId );
		if( reviewer == null )
			return false;
		if( task1.assignee.getId() == reviewerId )
			return false;
		// String oldReviewer = task1.reviewer.name;
		task1.reviewer = reviewer;
		task1.save();
		reviewer.tasks.add( task1 );
		reviewer.save();
		String header = "Task: 'T" + task1.id + "\'" + " Reviewer has been edited.";
		String body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + Security.getConnected().name + "\'" + ".";

		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		Logs.addLog( Security.getConnected(), "Edit", "Task Reviewer", id, task1.project, new Date( System.currentTimeMillis() ) );
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
	public static void editTaskReviewerJSON( long id, long reviewerId )
	{
		String zero = "0";
		String one = "1";
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			renderJSON( zero );
		User reviewer = User.findById( reviewerId );
		if( reviewer == null )
			renderJSON( zero );
		if( task1.assignee.getId() == reviewerId )
			renderJSON( zero );
		task1.reviewer = reviewer;
		task1.save();
		reviewer.tasks.add( task1 );
		reviewer.save();
		List<User> m = new ArrayList();
		m.add( task1.assignee );
		m.add( task1.reporter );
		m.add( task1.reviewer );
		// Notifications.notifyUsers(m, "TASK editing", "task " + id +
		// "reviewer is changed to " + reviewer.email, (byte) 1);
		Calendar cal = new GregorianCalendar();
		Project y = task1.project;
		User myUser = Security.getConnected();
		Logs.addLog( myUser, "Edit task reviewer", "Task", id, y, cal.getTime() );
		renderJSON( one );
	}

	/**
	 * This method changes the given task reviewer.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            the id of the given task.
	 * @param userId
	 *            the id of the user who will be doing the change.
	 * @param reviewerId
	 *            the id of the user who will be the reviewer of the task.
	 * @return boolean
	 * @story C3S36
	 */
	public static boolean editTaskReviewer2( long id, long userId, long reviewerId )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		User reviewer = User.findById( reviewerId );
		if( reviewer == null )
			return false;
		if( task1.assignee.getId() == reviewerId )
			return false;

		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeReviewer" );

		if( !permession )
			return false;
		// String oldReviewer = task1.reviewer.name;
		task1.reviewer = reviewer;
		task1.save();
		reviewer.tasks.add( task1 );
		reviewer.save();
		String body = "";
		// String header = "A Task Reviewer has been changed in Component: " +
		// "\'" + task1.taskStory.componentID.name + "\'" + " in Project: " +
		// "\'" + task1.taskStory.componentID.project.name + "\'" + ".";
		//String header = "Task: 'T" + task1.id + "\'" + " Reviewer has been edited.";
		if( userId == Security.getConnected().id )
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ".";

		}
		else
		{
			body = "In Project: " + "\'" + task1.project.name + "\'" + "." + '\n' + " In Component: " + "\'" + task1.component.name + "\'" + "." + '\n' + "\'" + "." + '\n' + " Edited by: " + "\'" + user1.name + "\'" + ", From " + "\'" + Security.getConnected().name + "\'" + "'s account.";

		}
		// Notifications.notifyUsers(task1.component.getUsers(), header, body,
		// (byte) 0);
		if( userId == Security.getConnected().id )
		{
			Logs.addLog( user1, "Edit", "Task Reviewer", id, task1.project, new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Logs.addLog( user1 + " has performed action (Edit) using resource (Task Reviewer) in project " + task1.project.name + " from the account of " + Security.getConnected().name );
		}
		return true;
	}

	public static void chooseTaskPerson()
	{
		render();
	}

	// /**
	// * @author emadabdelrahman
	// * @param Task
	// * id
	// * @Description Checks if all the tasks of a story is completed. if all of
	// * the tasks are complete, then it marks the story as done
	// */
	// public static void StoryComplete(long taskId) {
	// Task t = Task.findById(taskId);
	// Story s = t.taskStory;
	// List<Task> AllTasksInStory = s.storiesTask;
	//
	// for (int i = 0; i < AllTasksInStory.size(); i++) {
	// if (AllTasksInStory.get(i).taskStatus.name != "Done") {
	// return;
	// }
	//
	// }
	//
	// s.done = true;
	// s.save();
	// }

	/**
	 * @author menna_ghoneim Renders a given taskid with a list of user and
	 *         option to say if the reviewer or the assignee is being changed to
	 *         a page to choose a reviewer or assignee
	 * @param taskId
	 *            the task to be edited
	 * @param aORr
	 *            whether reviewer or assignee
	 */

	public static void chooseTaskAssiRev( long taskId, int aORr )
	{
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( taskId );

		if( aORr == 0 )
		{
			users = task.component.componentUsers;
			users.remove( task.reviewer );
		}
		else
		{

			users = task.component.componentUsers;
			Project project = task.taskSprint.project;
			List<Requestreviewer> reviewers = new ArrayList<Requestreviewer>();
			for( int i = 0; i < project.components.size(); i++ )
			{

				List<Requestreviewer> compRev = Requestreviewer.find( "byComponentAndTypesAndAccepted", project.components.get( i ), task.taskType, true ).fetch();
				reviewers.addAll( compRev );
			}

			if( reviewers == null || reviewers.isEmpty() )
			{
				users = task.component.componentUsers;
			}
			else
			{
				for( int i = 0; i < reviewers.size(); i++ )
					users.add( reviewers.get( i ).user );
			}

			users.remove( task.assignee );

			if( users.isEmpty() )
				users = task.component.componentUsers;

		}
		render( taskId, users, aORr );
	}

	/**
	 * @author Dina Helal
	 * @param taskId
	 *            the task to be edited
	 * @param compId
	 *            component of the users
	 */

	public static void chooseTaskAssi( long taskId, long compId, long userId )
	{
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( taskId );
		Component c = Component.findById( compId );
		users = c.componentUsers;
		users.remove( task.reviewer );
		render( taskId, users, user1 );
	}

	/**
	 * @author Dina Helal
	 * @param taskId
	 *            the task to be edited
	 * @param compId
	 *            component of the users
	 */

	public static void chooseRev( long taskId, long compId, long userId )
	{
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( taskId );
		Component c = Component.findById( compId );
		users = c.componentUsers;
		users.remove( task.assignee );
		render( taskId, users, user1 );
	}

	/**
	 * @author menna_ghoneim Renders a given taskid with a likt of project types
	 *         and the session's user id to a page to choose
	 * @param taskId
	 *            the task to be edited
	 */

	public static void chooseTaskType( long taskId )
	{
		Task task = Task.findById( taskId );
		List<TaskType> types = task.taskSprint.project.taskTypes;
		User user = Security.getConnected();
		render( taskId, types, user );
	}

	/**
	 * @author dina_helal takes a taskid, and renders it to a page to choose
	 *         task type
	 * @param taskId
	 *            the task to be edited
	 */

	public static void chooseType( long taskId, long userId )
	{
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		Task task = Task.findById( taskId );
		List<TaskType> types = task.taskSprint.project.taskTypes;
		render( taskId, types, userId );
	}

	/**
	 * @author menna_ghoneim Renders a given taskId with project statuses and
	 *         the user in the session to a page to choose a task status
	 * @param taskId
	 *            the task to be edited
	 */
	public static void chooseTaskStatus( long taskId )
	{
		Task task = Task.findById( taskId );
		List<TaskStatus> states = task.taskSprint.project.taskStatuses;
		User user = Security.getConnected();
		render( taskId, states, user );
	}

	public static void magicShow( long projectId, long componentId, int mine, long meetingId, long taskId )
	{
		String title;
		if( componentId != 0 )
		{
			Component component = Component.findById( componentId );
			title = "C" + component.number + ": Tasks";
			List<Task> tasks = new ArrayList<Task>();
			tasks = Task.find( "byComponentAndDeleted", component, false ).fetch();
			render( tasks, title, mine, projectId );
		}
		else
		{
			if( taskId != 0 )
			{
				Task task = Task.findById( taskId );
				if( task.deleted )
					notFound();
				if( task.parent != null )
					title = "Task " + task.parent.number + "." + task.number;
				else
					title = "Task " + task.number;
				List<Task> tasks = task.subTasks;
				render( task, title, tasks, projectId );
			}
			else
			{
				if( mine == 1 )
				{
					title = "My Tasks";
					User user = Security.getConnected();
					Project project = Project.findById( projectId );
					List<Task> tasks = new ArrayList<Task>();
					for( Task task1 : project.projectTasks )
					{
						if( task1.assignee != null && task1.reviewer != null && (task1.assignee.equals( user ) || task1.reviewer.equals( user )) && task1.checkUnderImpl() )
						{
							tasks.add( task1 );
						}
					}

					render( tasks, title, mine, projectId );
				}
				else
				{
					if( projectId != 0 )
					{
						title = "Project Tasks";
						Project project = Project.findById( projectId );
						List<Task> tasks = Task.find( "byProjectAndDeletedAndParentIsNull", project, false ).fetch();
						// System.out.println( task );
						render( tasks, title, mine, projectId );
					}
					else
					{
						if( meetingId != 0 )
						{
							Meeting meeting = Meeting.findById( meetingId );
							List<Task> tasks = new ArrayList<Task>();
							for( Task task2 : meeting.tasks )
							{
								if( !task2.deleted )
								{
									tasks.add( task2 );
								}
							}

							title = "Meetings Tasks";
							render( title, tasks, projectId );
						}
					}
				}
			}
		}
	}

	/**
	 * Associate task to component
	 * 
	 * @author mahmoudsakr
	 */
	public static void associateToComponent( long taskId, long componentId )
	{
		Task task = Task.findById( taskId );
		Component component = Component.findById( componentId );
		User connected = Security.getConnected();
		Security.check( connected.in( task.project ).can( "modifyTask" ) && task.project == component.project && task.component.project == component.project && task.parent == null );

		// first remove task from the component
		task.component.componentTasks.remove( task );
		task.component.save();

		task.component = component;
		task.save();

		renderText( "Associated successfully|reload('component-" + componentId + "', 'task-" + taskId + "')" );
	}
	public static void assignTaskAssignee (long taskId, long assigneeId)
	{
		Task task = Task.findById(taskId);
		User user = User.findById(assigneeId);
		User connected = Security.getConnected();
		if(task.reviewer==user)
			renderText("You can't be the assignee & reviewer of the same task");	
		Security.check(connected.in(task.project).can("modifyTask") && user.projects.contains(task.project) && task.reviewer!=user && (task.component==null || user.components.contains(task.component)));
		task.assignee = user;
		task.save();
		renderText("Assignee added successfully|reload('task-"+taskId+"')");
	}
	public static void assignTaskReviewer (long taskId, long reviewerId)
	{
		Task task = Task.findById(taskId);
		User user = User.findById(reviewerId);
		User connected = Security.getConnected();
		if(task.assignee==user)
			renderText("You can't be the reviewer & assignee of the same task");	
		Security.check(connected.in(task.project).can("modifyTask") && user.projects.contains(task.project) && task.assignee!=user && (task.component==null || user.components.contains(task.component)));
		task.reviewer = user;
		task.save();
		renderText("Reviewer assigned successfully|reload('task-"+taskId+"')");
	}
	public static void componentUsers (long cid)
	{
		Component c = Component.findById(cid);
		List <User> users = null;
		if(cid==1)
		{
			users = c.project.users;
		}
		else
		{
			users = c.componentUsers;
		}
		List<User.Object> u = new ArrayList<User.Object>();
		for( User user : users )
		{
			u.add( new User.Object( user.id, user.name ) );
		}
		renderJSON( u );
	}
}
