import java.util.ArrayList;
import java.util.Date;

import controllers.Application;

import models.Artifact;
import models.Component;
import models.HelpTopic;
import models.Log;
import models.Meeting;
import models.MeetingAttendance;
import models.Permission;
import models.ProductRole;
import models.Project;
import models.Role;
import models.Setting;
import models.Sprint;
import models.Task;
import models.User;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Initially insert data into db
 * 
 * @author mahmoudsakr
 */
@OnApplicationStart
public class Bootstrap extends Job {
	public void doJob() {
		if (User.count() == 0) {
			new Setting().save();

			// add help topics as well
			HelpTopic.Object[] topics = { // remember the trailing comment, even
											// if empty!
			new HelpTopic.Object( "Join Project", "request-role.swf" ), //
			new HelpTopic.Object( "Create new task", "create-task.swf" ), //
			new HelpTopic.Object( "Set task assignee and reviewer", "task-assignee-reviewer.swf" ), //

			};

			for (HelpTopic.Object obj : topics) {
				new HelpTopic( obj.summary, obj.filename ).save();
			}

			String[] perms = { "manageLogs", "manageReviewerRequests", "addNote", "manageRoles", "createRole", "editRole", "deleteRole", "editProject", "editBacklog", "editProjectNotificationprofile", "editUserNotificationProfile", "addComponent", "editComponent", "deleteComponent", "addSprint", "editSprint", "editSprintBacklog", "addMeeting", "editMeeting", "deleteMeeting", "changeEstimations", "changeTaskType", "changeTaskStatus", "changeTaskDescreption", "changeReviewer", "changeAssignee", "changeAssigneeInSprint", "invite", "manageRequests", "editColumn", "getComponentMembers", "chooseAssignee", "chooseReporter", "chooseReviewer", "addProductRole", "editProductRole", "deleteProductRole", "request", "setDependentStories", "editColumnsPositions", "assignStoryToSprint", "respond", "accept", "renameColumns", "addReviewLog", "AddTask", "modifyTask", "changeStatus", "addTaskStatus", "editTaskStatus", "addTaskType", "editTaskType", "viewReviewLog", "startGame", "viewChat", "associateTaskToMeeting", "reportImpediment", "editTask", "setMeetingAttendance", "deleteProject", "ShowColumn", "HideColumn", "AssociateArtifacts", "AssociateSprinttoMeeting", "joinMeeting", "revokeUserRole", "deleteTask","assignUserToComponent" };
			for (String perm : perms) {
				new Permission(perm).save();
			}
			
			Role r = new Role("Project Owner").save();
			for (Permission p : Permission.<Permission> findAll()) {
				r.permissions.add( p );
			}
			r.save();
			new Role("Project Admin").save();
			new Role("Scrum Master").save();
			new Role("Developer").save();
			r = new Role("Project Member").save();
			r.baseRole = true;
			r.save();
			User admin = new User();
			admin.email = "mail@gmail.com";
			admin.isActivated = true;
			admin.isAdmin = true;
			admin.name="admin";
			admin.password = "admin";
			admin.pwdHash = Application.hash( "admin" );
			admin.save();
			
			
		}
	}
}