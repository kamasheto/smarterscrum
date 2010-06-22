package controllers;

import java.util.Date;
import java.util.List;

import controllers.CRUD.ObjectType;

import models.Priority;
import models.Project;
import models.ProjectNotificationProfile;
import models.Request;
import models.TaskStatus;
import models.TaskType;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With (Secure.class)
// @Check ("systemAdmin")
public class Projects extends SmartCRUD {
	/**
	 * overriden to init roles by default from CRUD
	 * 
	 * @throws Exception
	 */
	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.entityClass.newInstance();
		Project projectObject = (Project) object;

		validation.valid(object.edit("object", params));

		if (validation.hasErrors()) {

			flash.error(Messages.get("Please Fill in All The Required Fields."));

			try {
				render(request.controller.replace(".", "/") + "/blank.html", type);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else if (Project.userRequstedProjectBefore(session.get("username"), projectObject.name)) {

			flash.error(Messages.get("You Have Already Created a Projcet with the Same Name :'" + projectObject.name + "'. You Will Be notified Upon Approval."));

			try {
				render(request.controller.replace(".", "/") + "/blank.html", type);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else if (Project.isUnique((projectObject.name))) {

			flash.error("Project Name is Already Taken.");

			try {
				render(request.controller.replace(".", "/") + "/blank.html", type);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}

		}

		else {
			if(Security.getConnected().isAdmin){
				
				projectObject.approvalStatus=true;
			}
			User user = Security.getConnected();
			projectObject.user=user;
			object.save();
			((Project) object).init();
			
			Logs.addLog(Security.getConnected(), "Create", "Project", projectObject.id, projectObject, new Date(System.currentTimeMillis()));
			if(Security.getConnected().isAdmin){
				
				flash.success("' "+projectObject.name+" '" +" Project Has Been Successfully Created.");
			}
			else{
				flash.success("Your Project Request Has Been Sent.You Will Be Notified Upon Approval");
				
			}
			
			redirect(request.controller + ".show", object.getEntityId());

		}
	}
   

    public static void list(int page, String search, String searchFields, String orderBy, String order) {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
       // List<JPASupport> objects = type.findPage(page, search, searchFields, orderBy, order, (String) request.args.get("where"));
               List<Project> objects = Project.find("approvalStatus=true AND deleted=false").fetch();
        Long totalCount = (long)objects.size();
        Long count = (long)objects.size();

        try {
        	
        	
            render(type, objects, count, totalCount, page, orderBy, order);
        } catch (TemplateNotFoundException e) {
            render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
        }
    }
	/**
	 * This action takes the name of a project as input and checks whether it
	 * already exists or not.
	 * 
	 * @param name
	 * @author behairy
	 */
	public static void checkAvailability(String name) {

		boolean flag = !Project.isUnique(name);

		renderJSON(flag);

	}
	
	
	/**
	 * This action method add a meeting type to the array list of meeting types
	 * in project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param meetingType
	 *            String
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void addMeetingType(long id, String meetingType, boolean inSprint) {

		Project p = Project.findById(id);

		p.meetingsTypes.add(meetingType);
		p.meetingsTypesInSprint.add(inSprint);

		p.save();

		Logs.addLog(Security.getConnected(), "Add", "Project Defualt Meeting Types", p.id, p, new Date(System.currentTimeMillis()));
		renderJSON(true);

	}

	/**
	 * This action method removes a meeting from the array list of meeting types
	 * in project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param meetingType
	 *            String
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void removeMeetingType(long id, String meetingType) {
		Project p = Project.findById(id);
		int index = p.meetingsTypes.indexOf(meetingType);
		p.meetingsTypes.remove(meetingType);
		p.meetingsTypesInSprint.remove(index);
		p.save();
		Logs.addLog(Security.getConnected(), "Remove", "Project Default Meeting Types ", p.id, p, new Date(System.currentTimeMillis()));
		renderJSON(true);
	}

	/**
	 * This action method checks if the given meeting type is associated to
	 * sprints using the array list meetingTypesInArraylist
	 * 
	 * @param id
	 * @param meetingType
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void isMeetingTypeAssociatedToSprint(long id, String meetingType) {
		Project p = Project.findById(id);
		int index = p.meetingsTypes.indexOf(meetingType);
		boolean inSprint = p.meetingsTypesInSprint.get(index);

		renderJSON(inSprint);

	}

	/**
	 * This action method adds a task type to the array list of task types in
	 * project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param taskType
	 *            String
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void addTaskType(long id, String taskType) {

		Project p = Project.findById(id);
		TaskType t = new TaskType();
		t.project = p;
		t.name = taskType;

		p.save();
		t.save();
		Logs.addLog(Security.getConnected(), "Add", "Project Default Task Types ", t.id, p, new Date(System.currentTimeMillis()));
		renderJSON(t.id);

	}

	/**
	 * This action method adds a task status to the list of task statuses in
	 * project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param taskStatus
	 *            String
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void addTaskStatus(long id, String taskStatus) {

		Project p = Project.findById(id);
		TaskStatus t = new TaskStatus();
		t.project = p;
		t.name = taskStatus;

		p.save();
		t.save();
		Logs.addLog(Security.getConnected(), "Add", "Project Default Task Status", t.id, p, new Date(System.currentTimeMillis()));
		renderJSON(t.id);

	}

	/**
	 * This action method removes a task type from the array list of task types
	 * in project.
	 * 
	 * @param taskID
	 *            long
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void removetaskType(long taskID) {

		TaskType taskType = TaskType.findById(taskID);
		taskType.deleted = true;

		taskType.save();
		Logs.addLog(Security.getConnected(), "Remove", "Project Default Task Type", taskType.id, taskType.project, new Date(System.currentTimeMillis()));
		renderJSON(true);
	}

	/**
	 * This action method removes a task status from the list of task statuses
	 * in project.
	 * 
	 * @param statusID
	 *            long
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void removeTaskStatus(long statusID) {

		TaskStatus taskStatus = TaskStatus.findById(statusID);
		taskStatus.deleted = true;

		taskStatus.save();
		Logs.addLog(Security.getConnected(), "Remove", "Project Default Task Status ", taskStatus.id, taskStatus.project, new Date(System.currentTimeMillis()));
		renderJSON(true);
	}

	/**
	 * This action method add a story priority to the list of story priorities
	 * in project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param storyType
	 *            String
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void addStoryType(long id, String storyType, String unit) {
		Project p = Project.findById(id);
		Priority x = new Priority();
		x.project = p;
		x.title = storyType;
		x.priority = Integer.parseInt(unit);

		x.save();

		p.save();
		Logs.addLog(Security.getConnected(), "Add", "Project Default Story Priroity ", x.id, x.project, new Date(System.currentTimeMillis()));
		renderJSON(x.id);

	}

	/**
	 * This action method removes a story priority from the list of priorities
	 * in project.
	 * 
	 * @param priorityID
	 *            int
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void removeStoryType(long priorityID) {

		Priority priorityInstance = Priority.findById(priorityID);
		priorityInstance.deleted = true;

		priorityInstance.save();
		Logs.addLog(Security.getConnected(), "Remove", "Project Default Story Priroity ", priorityInstance.id, priorityInstance.project, new Date(System.currentTimeMillis()));
		renderJSON(true);
	}

	/**
	 * This action method changes the boolean status of the auto-reschedule of
	 * meeting for the project specified by the parameter id.
	 * 
	 * @param id
	 *            long
	 * @param autoReschedule
	 *            boolean
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void changeAutoRescheduleStatus(long id, boolean autoReschedule) {
		Project p = Project.findById(id);

		p.autoReschedule = autoReschedule;
		p.save();
		Logs.addLog(Security.getConnected(), "Edit", "Project Default Auto Meeting Reschedule Option ", p.id, p, new Date(System.currentTimeMillis()));
		renderJSON(true);
	}

	/**
	 * This action method sets the default sprint duration of the specified
	 * project by id.
	 * 
	 * @param id
	 *            long
	 * @param duration
	 *            String
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void setDefaultSprintDuartion(long id, String duration) {
		Project p = Project.findById(id);

		p.sprintDuration = Integer.parseInt(duration);
		p.save();
		Logs.addLog(Security.getConnected(), "Edit", "Project Default Sprint Duration ", p.id, p, new Date(System.currentTimeMillis()));
		renderJSON(true);

	}

	/**
	 * Deleting a project
	 * 
	 * @author hossam sharaf
	 * @param projectId
	 */
	public static void deleteProject(long projectId) {
		Project project = Project.findById(projectId);
		project.deleted = true;
		project.save();
		String body = "Please note that the project " + project.name + " has been deleted!";
		String header = project.name + " deletion notification";
		List<User> projectMembers = project.users;
		Notifications.notifyUsers(projectMembers, header, body, (byte) -1);
		Logs.addLog(Security.getConnected(), "Deleted Project", "project", projectId, project, new Date());
		Show.projects(0);
	}

	/**
	 * This action method sets the effort estimation unit of the specified
	 * project by id. The effort estimation is set to 0 if in hours and 1 if in
	 * points.
	 * 
	 * @param id
	 * @param unit
	 * @author Behairy
	 */
	@Check ("canEditProject")
	public static void setEffortEstimationUnit(long id, String unit) {
		Project p = Project.findById(id);
		int selectedUnit;
		if (unit == "Hours")
			selectedUnit = 0;
		else
			selectedUnit = 1;

		p.effortEstimationUnit = selectedUnit;
		p.save();
		Logs.addLog(Security.getConnected(), "Edit", "Project Default Effort Estimation Unit", p.id, p, new Date(System.currentTimeMillis()));
		renderJSON(true);

	}

	/**
	 * @author OmarNabil This method takes user id and project id and initiates
	 *         a new request for that user to be deleted from that project
	 * @param userId
	 * @param id
	 */
	public static void RequestDeleted(long id) {

		// User myUser=User.findById(userId);
		User myUser = Security.getConnected();
		Project myProject = Project.findById(id);
		System.out.println(myProject);
		Request x = new Request(myUser, myProject);
		flash.success("your request has been sent");
		x.save();
		Show.projects(0);
		// Logs.addLog(myProject, "request to be deleted", "Request", x.id );
		// Logs.addLog(Security.getConnected(), "request to be deleted",
		// "Request", x.id, myProject, new Date());
	}

	/**
	 * This method fetches and renders the corresponding
	 * ProjectNotificationProfile when The scrum master clicks the Edit
	 * notifications link corresponding to a certain project.
	 * 
	 * @author Moataz_Mekki
	 * @param projectId
	 *            The id of that project the user wants to manage his
	 *            notifications in.
	 * @throws ClassNotFoundException
	 */
	// @Check ("canEditProjectNotificationProfile")
	public static void manageNotificationProfile(long projectId) throws ClassNotFoundException {
		Project currentProject = Project.findById(projectId);
		Security.check(currentProject, "editProjectNotificationProfile");
		ProjectNotificationProfile currentNotificationProfile = currentProject.notificationProfile;
		ObjectType type = ObjectType.get(ProjectNotificationProfiles.class);
		notFoundIfNull(type);
		if (currentNotificationProfile == null)
			error("Could not find a notification profile for this project");
		else {
			JPASupport object = type.findById(currentNotificationProfile.id);
			try {

				render(currentNotificationProfile, type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
	}

	/**
	 * This method saves any modifications made by the scrum in a given
	 * ProjectNotificationProfile in the UI Side to the database. And renders a
	 * success message.
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            The id of that project that the scrum master is editing its
	 *            notification profile
	 * @throws Exception
	 */
	public static void saveNotificationProfile(String id) throws Exception {
		ObjectType type = ObjectType.get(ProjectNotificationProfiles.class);
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Security.check(((ProjectNotificationProfile) object).project, "editProjectNotificationProfile");
		validation.valid(object.edit("object", params));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/show.html", type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
		object.save();
		flash.success("The Project Notificaton Profile modifications have been saved");
		if (params.get("_save") != null) {
			redirect("/projects/managenotificationprofile?projectId=" + id);
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * @author Moataz_Mekki
	 * @param id
	 *            : the id of the project this method renders the list of
	 *            project members to the html page
	 */
	public static void getProjectMembers(long id) {
		Project pro = Project.findById(id);
		List<User> users = pro.users;
		render(users, pro);
	}

	/**
	 * @author mahmoudsakr this method renders the projects of the connected
	 *         user
	 */
	public static void myProjects() {
		User user = Security.getConnected();
		List<Project> projects = user.projects;
		render(projects);
	}
	/**
	 * Action for Manage Project Request Page, renders list of all pending projects requests.
	 * @author Behairy
	 */
	public static void manageProjectRequests(){
		 List<Project> pendingProjects = Project.find("approvalStatus=false AND deleted=false").fetch();
	render(pendingProjects);
	}
	/**
	 * This method approves the pending request for the project
	 * given by the ID as a parameter. It Notifies project owner with
	 * project request status.
	 * @param id projectID
	 * @author Behairy
	 */
	public static void approveRequest(long id){
		Project p=Project.findById( id );
		p.approvalStatus=true;
		List<User> users=User.find( "id="+p.user.id ).fetch();
		Notifications.notifyUsers( users,p.name+" Project Request", "This is to Kindly Inform you that your request for Project "+p.name+" has been Approved. You Can Now Start !",(byte)0 );
		p.save();
		renderJSON(true);
	}
	/**
	 * This method declines the pending request for the project
	 * given by the ID as a parameter, by deleting the project (i.e setting flag). It Notifies project owner with
	 * project request status.
	 * @param id projectID
	 * @author Behairy
	 */
	public static void declineRequest(long id){
		Project p=Project.findById( id );
		p.deleted=true;
		List<User> users=User.find( "id="+p.user.id ).fetch();
		Notifications.notifyUsers( users,p.name+" Project Request", "This is to Kindly Inform you that your request for Project "+p.name+" has been Declined. We Apologize for Any inconvenience/",(byte)-1);
		p.save();
		renderJSON(true);
	}
	
	
}