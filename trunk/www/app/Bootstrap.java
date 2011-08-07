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
			admin.name="elle";
			admin.pwdHash = Application.hash( "elle" );
			admin.save();
			User dalia = new User();
			dalia.email = "mail2@gmail.com";
			dalia.isActivated = true;
			dalia.isAdmin = false;
			dalia.name="dalia";
			dalia.pwdHash = Application.hash( "dalia" );
			dalia.save();
			User abdullah = new User();
			abdullah.email = "mail3@gmail.com";
			abdullah.isActivated = true;
			abdullah.isAdmin = false;
			abdullah.name="abdullah";
			abdullah.pwdHash = Application.hash( "abdullah" );
			abdullah.save();
			User dina = new User();
			dina.email = "mail4@gmail.com";
			dina.isActivated = true;
			dina.isAdmin = false;
			dina.name="dina";
			dina.pwdHash = Application.hash( "dina" );
			dina.save();
			User maryam = new User();
			maryam.email = "mail5@gmail.com";
			maryam.isActivated = true;
			maryam.isAdmin = false;
			maryam.name="maryam";
			maryam.pwdHash = Application.hash( "maryam" );
			maryam.save();
			User nataly = new User();
			nataly.email = "mail6@gmail.com";
			nataly.isActivated = true;
			nataly.isAdmin = false;
			nataly.name="nataly";
			nataly.pwdHash = Application.hash( "nataly" );
			nataly.save();
			User sherief = new User();
			sherief.email = "mail7@gmail.com";
			sherief.isActivated = true;
			sherief.isAdmin = false;
			sherief.name="sherief";
			sherief.pwdHash = Application.hash( "sherief" );
			sherief.save();
			User ahmedHadi = new User();
			ahmedHadi.email = "mail8@gmail.com";
			ahmedHadi.isActivated = true;
			ahmedHadi.isAdmin = false;
			ahmedHadi.name="ahmedHadi";
			ahmedHadi.pwdHash = Application.hash( "ahmedHadi" );
			ahmedHadi.save();
			User alia = new User();
			alia.email = "mail9@gmail.com";
			alia.isActivated = true;
			alia.isAdmin = false;
			alia.name="alia";
			alia.pwdHash = Application.hash( "alia" );
			alia.save();
			User fadwa = new User();
			fadwa.email = "mail10@gmail.com";
			fadwa.isActivated = true;
			fadwa.isAdmin = false;
			fadwa.name="fadwa";
			fadwa.pwdHash = Application.hash( "fadwa" );
			fadwa.save();
			User lama = new User();
			lama.email = "mail11@gmail.com";
			lama.isActivated = true;
			lama.isAdmin = false;
			lama.name="lama";
			lama.pwdHash = Application.hash( "lama" );
			lama.save();
			User faruki = new User();
			faruki.email = "mail12@gmail.com";
			faruki.isActivated = true;
			faruki.isAdmin = false;
			faruki.name="faruki";
			faruki.pwdHash = Application.hash( "faruki" );
			faruki.save();
			User mostafaYasser = new User();
			mostafaYasser.email = "mail13@gmail.com";
			mostafaYasser.isActivated = true;
			mostafaYasser.isAdmin = false;
			mostafaYasser.name="mostafaYasser";
			mostafaYasser.pwdHash = Application.hash( "mostafaYasser" );
			mostafaYasser.save();
			User samiha = new User();
			samiha.email = "mail14@gmail.com";
			samiha.isActivated = true;
			samiha.isAdmin = false;
			samiha.name="samiha";
			samiha.pwdHash = Application.hash( "samiha" );
			samiha.save();
			User zein = new User();
			zein.email = "mail15@gmail.com";
			zein.isActivated = true;
			zein.isAdmin = false;
			zein.name="zein";
			zein.pwdHash = Application.hash( "zein" );
			zein.save();
			User ahmedFat7y = new User();
			ahmedFat7y.email = "mail16@gmail.com";
			ahmedFat7y.isActivated = true;
			ahmedFat7y.isAdmin = false;
			ahmedFat7y.name="ahmedFat7y";
			ahmedFat7y.pwdHash = Application.hash( "ahmedFat7y" );
			ahmedFat7y.save();
			User alaaSamer = new User();
			alaaSamer.email = "mail17@gmail.com";
			alaaSamer.isActivated = true;
			alaaSamer.isAdmin = false;
			alaaSamer.name="alaaSamer";
			alaaSamer.pwdHash = Application.hash( "alaaSamer" );
			alaaSamer.save();
			User ibrahimAdel = new User();
			ibrahimAdel.email = "mail18@gmail.com";
			ibrahimAdel.isActivated = true;
			ibrahimAdel.isAdmin = false;
			ibrahimAdel.name="ibrahimAdel";
			ibrahimAdel.pwdHash = Application.hash( "ibrahimAdel" );
			ibrahimAdel.save();
			User tarekSheasha = new User();
			tarekSheasha.email = "mail19@gmail.com";
			tarekSheasha.isActivated = true;
			tarekSheasha.isAdmin = false;
			tarekSheasha.name="tarekSheasha";
			tarekSheasha.pwdHash = Application.hash( "tarekSheasha" );
			tarekSheasha.save();
			User karimAbdo = new User();
			karimAbdo.email = "mail20@gmail.com";
			karimAbdo.isActivated = true;
			karimAbdo.isAdmin = false;
			karimAbdo.name="karimAbdo";
			karimAbdo.pwdHash = Application.hash( "karimAbdo" );
			karimAbdo.save();
			User user1 = new User();
			user1.email = "mail21@gmail.com";
			user1.isActivated = true;
			user1.isAdmin = false;
			user1.name="user1";
			user1.pwdHash = Application.hash( "user1" );
			user1.save();
			User user2 = new User();
			user2.email = "mail22@gmail.com";
			user2.isActivated = true;
			user2.isAdmin = false;
			user2.name="user2";
			user2.pwdHash = Application.hash( "user2" );
			user2.save();
			User user3 = new User();
			user3.email = "mail23@gmail.com";
			user3.isActivated = true;
			user3.isAdmin = false;
			user3.name="user3";
			user3.pwdHash = Application.hash( "user3" );
			user3.save();
			User user4 = new User();
			user4.email = "mail24@gmail.com";
			user4.isActivated = true;
			user4.isAdmin = false;
			user4.name="user4";
			user4.pwdHash = Application.hash( "user4" );
			user4.save();
			User user5 = new User();
			user5.email = "mail25@gmail.com";
			user5.isActivated = true;
			user5.isAdmin = false;
			user5.name="user5";
			user5.pwdHash = Application.hash( "user5" );
			user5.save();
			User user6 = new User();
			user6.email = "mail26@gmail.com";
			user6.isActivated = true;
			user6.isAdmin = false;
			user6.name="user6";
			user6.pwdHash = Application.hash( "user6" );
			user6.save();
			User user7 = new User();
			user7.email = "mail27@gmail.com";
			user7.isActivated = true;
			user7.isAdmin = false;
			user7.name="user7";
			user7.pwdHash = Application.hash( "user7" );
			user7.save();
			User user8 = new User();
			user8.email = "mail28@gmail.com";
			user8.isActivated = true;
			user8.isAdmin = false;
			user8.name="user8";
			user8.pwdHash = Application.hash( "user8" );
			user8.save();
			User user9 = new User();
			user9.email = "mail29@gmail.com";
			user9.isActivated = true;
			user9.isAdmin = false;
			user9.name="user9";
			user9.pwdHash = Application.hash( "user9" );
			user9.save();
		}
	}
}