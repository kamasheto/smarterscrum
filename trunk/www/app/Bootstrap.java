import java.util.ArrayList;
import java.util.Date;

import models.Artifact;
import models.Component;
import models.Log;
import models.Meeting;
import models.MeetingAttendance;
import models.Permission;
import models.ProductRole;
import models.Project;
import models.Sprint;
import models.Story;
import models.Task;
import models.User;
import models.UserNotificationProfile;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Initially insert data into db
 * 
 * @author mahmoudsakr
 */
@OnApplicationStart
public class Bootstrap extends Job
{
	public void doJob()
	{
		if( User.count() == 0 )
		{
			String[] perms = { "manageRoles", "createRole", "editRole", "deleteRole", "editProject", "editBacklog", "editProjectNotificationprofile", "editUserNotificationProfile", "addComponent", "editComponent", "deleteComponent", "addSprint", "editSprint", "editSprintBacklog", "addMeeting", "editMeeting", "deleteMeeting", "addStory", "editStory", "deleteStory", "changeEstimations", "changeTaskType", "changeTaskStatus", "changeTaskDescreption", "changeReviewer", "changeAssignee", "changeAssigneeInSprint", "invite", "manageRequests", "editColumn", "getComponentMembers", "chooseAssignee", "chooseReporter", "chooseReviewer", "addProductRole", "editProductRole", "deleteProductRole", "request", "setDependentStories", "editColumnsPositions", "assignStoryToSprint", "respond", "accept", "renameColumns", "addReviewLog", "canAddTask", "modifyTask", "changeStatus", "addTaskStatus", "editTaskStatus", "addTaskType", "editTaskType", "viewReviewLog", "startGame", "viewChat", "associateTaskToMeeting", "reportImpediment", "editTask", "setMeetingAttendance" };
			for( String perm : perms )
			{
				new Permission( perm, perm ).save();
			}
			ArrayList<String> emails = new ArrayList<String>();

			emails.addAll( Arrays.asList( new String[] { "eminem.virus2010@gmail.com", "me@sakr.me", "moataz.mekki@gmail.com", "omar.nabil10@gmail.com", "ahmed.behairy9@gmail.com", "amorotto@Gmail.com", "ghadafakhry@gmail.com", "hossam.sharaf@gmail.com", "minazakiz@gmail.com", "galalaly28@gmail.com", "justheba@gmail.com", "evilmonster.300@gmail.com", "mohamed.monayri@gmail.com", "moumen.elteir@gmail.com", "ahmed.k.abdelhameed@gmail.com", "eabdelrahman89@gmail.com", "iistcrimi@gmail.com", "hossam.amer12@gmail.com", "menna.ghoneim@gmail.com", "amr.mohamed.abdelwahab@gmail.com", "asmaa89alkomy@gmail.com", "dina.e.helal@gmail.com", "hadeer.diwan@gmail.com", "joseph.hajj90@gmail.com" } ) );
			// Fixtures.load( "dummy-data.yml" );
			ArrayList<User> users = new ArrayList<User>();
			ArrayList<Project> projects = new ArrayList<Project>();
			for( int i = 0; i < emails.size(); i++ )
			{
				User u = new User( emails.get( i ).split( "@" )[0], emails.get( i ), "test" );
				u.isActivated = u.isAdmin = true;
				// u.projects = new ArrayList<Project>();
				// u.components = new ArrayList<Component>();

				if( emails.get( i ).contains( "eminem.virus2010@gmail.com" ) )
					u.isAdmin = false;

				u.save();
				users.add( u );
			}

			Project p1 = new Project( "Smartsoft", "The best company project ever!" ).save();
			p1.init();

			Project p2 = new Project( "Sharesoft", "The second best company project ever!" ).save();
			p2.init();

			Project p3 = new Project( "Collabsoft", "The least best company project ever!" ).save();
			p3.init();

			projects.add( p1 );
			projects.add( p2 );
			projects.add( p3 );
			// p1.sprints = new ArrayList<Sprint>();
			// p1.save();
			// for (Project p : projects) {
			// // initialize the project with all proper data (including board,
			// // project default roles, etc.)
			// // p.sprints = new ArrayList<Sprint>();
			// p.init();
			// }

			// add more data below!

			// adding in the users relation the Projects
			for( User u : users )
			{
				u.projects.add( p1 );
				u.save();
			}

			// Components
			Component c1 = new Component();
			c1.name = "User and Roles";
			p1.addComponent( c1 );
			// c1.componentUsers = new ArrayList<User>();
			// c1.componentStories = new ArrayList<Story>();
			for( long i = 1; i <= 4; i++ )
			{
				User tmp = User.findById( i );
				tmp.components.add( c1 );
				c1.componentUsers.add( tmp );
				tmp.save();
			}
			c1.save();

			Component c2 = new Component();
			c2.name = "Projects and events";
			// c2.project = p1;
			p1.addComponent( c2 );
			// c2.componentUsers = new ArrayList<User>();
			// c2.componentStories = new ArrayList<Story>();
			for( long i = 5; i <= 9; i++ )
			{
				User tmp = User.findById( i );
				tmp.components.add( c2 );
				c2.componentUsers.add( tmp );
				tmp.save();
			}
			c2.save();

			Component c3 = new Component();
			c3.name = "User Stories and Tasks";
			// c3.project = p1;
			p1.addComponent( c3 );
			// c3.componentUsers = new ArrayList<User>();
			// c3.componentStories = new ArrayList<Story>();
			for( long i = 10; i <= 14; i++ )
			{

				User tmp = User.findById( i );
				tmp.components.add( c3 );
				c3.componentUsers.add( tmp );
				tmp.save();
			}
			c3.save();

			Component c4 = new Component();
			c4.name = "Artifacts";
			p1.addComponent( c4 );
			// c4.componentUsers = new ArrayList<User>();
			// c4.componentStories = new ArrayList<Story>();
			for( long i = 15; i <= 19; i++ )
			{
				User tmp = User.findById( i );
				tmp.components.add( c4 );
				c4.componentUsers.add( tmp );
				tmp.save();
			}
			c4.save();

			Component c5 = new Component();
			c5.name = "Virtual task Board";
			p1.addComponent( c5 );
			// c5.componentUsers = new ArrayList<User>();
			// c5.componentStories = new ArrayList<Story>();
			for( long i = 20; i <= 24; i++ )
			{

				User tmp = User.findById( i );
				tmp.components.add( c5 );
				c5.componentUsers.add( tmp );
				tmp.save();
			}
			c5.save();

			// Stories

			Story S1 = new Story( "As a System Admin I can view all System logs", "", "", 1, "", 1 );
			S1.componentID = c1;
			c1.componentStories.add( S1 );
			S1.save();

			Story S2 = new Story( "As a Project Admin I can create one or more new roles in one or more of my projects.", "", "", 1, "", 1 );
			S2.componentID = c1;
			c1.componentStories.add( S2 );
			S2.save();

			Story S3 = new Story( "As a System Admin I can edit details of users on the system", "", "", 1, "", 1 );
			S3.componentID = c1;
			c1.componentStories.add( S3 );
			S3.save();

			Story S4 = new Story( "As a Registered User I can Request to Create a Project", "", "", 1, "", 1 );
			S4.componentID = c2;
			c2.componentStories.add( S4 );
			S4.save();

			Story S5 = new Story( "As a Project Admin I Can Edit the default properties of the project", "", "", 1, "", 1 );
			S5.componentID = c2;
			c2.componentStories.add( S5 );
			S5.save();

			Story S6 = new Story( "As a Project Admin, I can create product roles in a project.", "", "", 1, "", 1 );
			S6.componentID = c3;
			c3.componentStories.add( S6 );
			S6.save();

			Story S7 = new Story( "As a Project Admin, I can assign specific stories to a specific sprint.", "", "", 1, "", 1 );
			S7.componentID = c3;
			c3.componentStories.add( S7 );
			S7.save();
            
			
			Story S8 = new Story( "As a Project Admin, I can drag and drop a task", "", "", 1, "", 1 );
			S8.componentID = c5;
			c5.componentStories.add( S8 );
			S8.save();
			
			
			Story S9 = new Story( "As a Project Admin, I can estimate effort points", "", "", 1, "", 1 );
			S9.componentID = c4;
			c4.componentStories.add( S9 );
			S9.save();
			
			
			
			
			
			
			// Sprints

			Sprint Sp1 = new Sprint( 2010, 5, 1, 2010, 5, 15, p1 ).save();
			Sprint Sp2 = new Sprint( 2010, 6, 1, 2010, 6, 15, p1 ).save();

			p1.sprints.add( Sp1 );
			p1.sprints.add( Sp2 );
			//
			// // Tasks
			//
			Task T1 = new Task();
			T1.description = "Coding";
			T1.taskStory = S1;
			T1.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T1.taskType = S1.componentID.project.taskTypes.get( 0 );
			T1.reporter = users.get( 0 );
			T1.assignee = users.get( 1 );
			T1.reviewer = users.get( 2 );
			T1.estimationPoints = 12.0;
			T1.taskSprint = Sp1;
			Sp1.tasks.add( T1 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T1 );
			S1.storiesTask.add( T1 );
			T1.save();

			Task T2 = new Task();
			T2.description = "Documentation";
			T2.taskStory = S1;
			T2.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T2.taskType = S1.componentID.project.taskTypes.get( 0 );

			T2.reporter = users.get( 0 );
			T2.assignee = users.get( 1 );
			T2.reviewer = users.get( 2 );
			T2.taskSprint = Sp1;
			T2.estimationPoints = 3.0;
			Sp1.tasks.add( T2 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T2 );
			S1.storiesTask.add( T2 );
			T2.save();

			Task T3 = new Task();
			T3.description = "Coding";
			T3.taskStory = S4;
			T3.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T3.taskType = S4.componentID.project.taskTypes.get( 0 );

			T3.reporter = users.get( 4 );
			T3.assignee = users.get( 5 );
			T3.reviewer = users.get( 6 );
			T3.estimationPoints = 12.0;
			T3.taskSprint = Sp1;
			Sp1.tasks.add( T3 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T3 );
			S4.storiesTask.add( T3 );
			T3.save();

			Task T4 = new Task();
			T4.description = "Scenario testing";
			T4.taskStory = S4;
			T4.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T4.taskType = S4.componentID.project.taskTypes.get( 0 );

			T4.reporter = users.get( 4 );
			T4.assignee = users.get( 5 );
			T4.reviewer = users.get( 6 );
			T4.estimationPoints = 2.0;
			T4.taskSprint = Sp1;
			Sp1.tasks.add( T4 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T4 );
			S4.storiesTask.add( T4 );
			T4.save();
			
		
		
			Task T5 = new Task();
			T5.description = "Coding";
			T5.taskStory = S6;
			T5.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T5.taskType = S6.componentID.project.taskTypes.get( 0 );

			T5.reporter = users.get( 11 );
			T5.assignee = users.get( 12 );
			T5.reviewer = users.get( 13 );
			T5.taskSprint = Sp1;
			T5.estimationPoints = 3.0;
			Sp1.tasks.add( T5 );
			S6.componentID.project.taskStatuses.get( 0 ).Tasks.add( T5 );
			S6.storiesTask.add( T5 );
			T5.save();

			Task T6 = new Task();
			T6.description = "Testing";
			T6.taskStory = S6;
			T6.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T6.taskType = S6.componentID.project.taskTypes.get( 0 );

			T6.reporter = users.get( 11 );
			T6.assignee = users.get( 12 );
			T6.reviewer = users.get( 13 );
			T6.estimationPoints = 1.0;
			T6.taskSprint = Sp1;
			Sp1.tasks.add( T6 );
			S6.componentID.project.taskStatuses.get( 0 ).Tasks.add( T6 );
			S6.storiesTask.add( T6 );
			T6.save();
			
			
			
			Task T7 = new Task();
			T7.description = "Reviewing";
			T7.taskStory = S6;
			T7.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T7.taskType = S6.componentID.project.taskTypes.get( 0 );

			T7.reporter = users.get( 11 );
			T7.assignee = users.get( 12 );
			T7.reviewer = users.get( 13 );
			T7.estimationPoints = 1.0;
			T7.taskSprint = Sp1;
			Sp1.tasks.add( T7 );
			S7.componentID.project.taskStatuses.get( 0 ).Tasks.add( T7 );
			S7.storiesTask.add( T7 );
			T7.save();
			
			
			
			Task T8 = new Task();
			T8.description = "Reviewing";
			T8.taskStory = S6;
			T8.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T8.taskType = S6.componentID.project.taskTypes.get( 0 );

			T8.reporter = users.get( 11 );
			T8.assignee = users.get( 12 );
			T8.reviewer = users.get( 13 );
			T8.estimationPoints = 1.0;
			T8.taskSprint = Sp1;
			Sp1.tasks.add( T8 );
			S7.componentID.project.taskStatuses.get( 0 ).Tasks.add( T8 );
			S7.storiesTask.add( T8 );
			T8.save();
			
			Task T9 = new Task();
			T9.description = "Reviewing";
			T9.taskStory = S4;
			T9.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T9.taskType = S4.componentID.project.taskTypes.get( 0 );

			T9.reporter = users.get( 4 );
			T9.assignee = users.get( 5 );
			T9.reviewer = users.get( 6 );
			T9.estimationPoints = 2.0;
			T9.taskSprint = Sp1;
			Sp1.tasks.add( T9 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T9 );
			S4.storiesTask.add( T9 );
			T9.save();
			
			
			Task T10 = new Task();
			T10.description = "Reviewing";
			T10.taskStory = S1;
			T10.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T10.taskType = S1.componentID.project.taskTypes.get( 0 );
			T10.reporter = users.get( 0 );
			T10.assignee = users.get( 1 );
			T10.reviewer = users.get( 2 );
			T10.estimationPoints = 12.0;
			T10.taskSprint = Sp1;
			Sp1.tasks.add( T10 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T10 );
			S1.storiesTask.add( T10 );
			T10.save();
			

			Task T11 = new Task();
			T11.description = "Reviewing4";
			T11.taskStory = S8;
			T11.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T11.taskType = S8.componentID.project.taskTypes.get( 0 );
			T11.reporter = users.get( 0 );
			T11.assignee = users.get( 1 );
			T11.reviewer = users.get( 2 );
			T11.estimationPoints = 12.0;
			T11.taskSprint = Sp1;
			Sp1.tasks.add( T11 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T11 );
			S8.storiesTask.add( T11 );
			T11.save();
			

			Task T12 = new Task();
			T12.description = "Reviewing5";
			T12.taskStory = S8;
			T12.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T12.taskType = S8.componentID.project.taskTypes.get( 0 );
			T12.reporter = users.get( 0 );
			T12.assignee = users.get( 1 );
			T12.reviewer = users.get( 2 );
			T12.estimationPoints = 12.0;
			T12.taskSprint = Sp1;
			Sp1.tasks.add( T12 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T12 );
			S8.storiesTask.add( T12 );
			T12.save();
			

			Task T13 = new Task();
			T13.description = "Coding";
			T13.taskStory = S9;
			T13.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T13.taskType = S9.componentID.project.taskTypes.get( 0 );
			T13.reporter = users.get( 0 );
			T13.assignee = users.get( 1 );
			T13.reviewer = users.get( 2 );
			T13.estimationPoints = 12.0;
			T13.taskSprint = Sp1;
			Sp1.tasks.add( T13 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T13 );
			S9.storiesTask.add( T13 );
			T13.save();
			

			Task T14 = new Task();
			T14.description = "Documentation3";
			T14.taskStory = S9;
			T14.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T14.taskType = S9.componentID.project.taskTypes.get( 0 );
			T14.reporter = users.get( 0 );
			T14.assignee = users.get( 1 );
			T14.reviewer = users.get( 2 );
			T14.estimationPoints = 12.0;
			T14.taskSprint = Sp1;
			Sp1.tasks.add( T14 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T14 );
			S9.storiesTask.add( T14 );
			T14.save();
			
			Task T15 = new Task();
			T15.description = "Reviewing2";
			T15.taskStory = S9;
			T15.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T15.taskType = S9.componentID.project.taskTypes.get( 0 );
			T15.reporter = users.get( 0 );
			T15.assignee = users.get( 1 );
			T15.reviewer = users.get( 2 );
			T15.estimationPoints = 12.0;
			T15.taskSprint = Sp1;
			Sp1.tasks.add( T15 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T15 );
			S9.storiesTask.add( T15 );
			T15.save();
			
			
			
			Task T16 = new Task();
			T16.description = "Reviewing1";
			T16.taskStory = S9;
			T16.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T16.taskType = S9.componentID.project.taskTypes.get( 0 );
			T16.reporter = users.get( 0 );
			T16.assignee = users.get( 1 );
			T16.reviewer = users.get( 2 );
			T16.estimationPoints = 12.0;
			T16.taskSprint = Sp1;
			Sp1.tasks.add( T16 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T16 );
			S9.storiesTask.add( T16 );
			T16.save();
			
			
			Task T17 = new Task();
			T17.description = "Reviewing3";
			T17.taskStory = S9;
			T17.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T17.taskType = S9.componentID.project.taskTypes.get( 0 );
			T17.reporter = users.get( 0 );
			T17.assignee = users.get( 1 );
			T17.reviewer = users.get( 2 );
			T17.estimationPoints = 12.0;
			T17.taskSprint = Sp1;
			Sp1.tasks.add( T17 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T17 );
			S9.storiesTask.add( T17 );
			T17.save();
		
			
			Task T18 = new Task();
			T18.description = "Coding ";
			T18.taskStory = S9;
			T18.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T18.taskType = S9.componentID.project.taskTypes.get( 0 );
			T18.reporter = users.get( 0 );
			T18.assignee = users.get( 1 );
			T18.reviewer = users.get( 2 );
			T18.estimationPoints = 12.0;
			T18.taskSprint = Sp1;
			Sp1.tasks.add( T18 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T18 );
			S9.storiesTask.add( T18 );
			T18.save();
			
			Task T19 = new Task();
			T19.description = "Senario review";
			T19.taskStory = S9;
			T19.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T19.taskType = S9.componentID.project.taskTypes.get( 0 );
			T19.reporter = users.get( 0 );
			T19.assignee = users.get( 1 );
			T19.reviewer = users.get( 2 );
			T19.estimationPoints = 12.0;
			T19.taskSprint = Sp1;
			Sp1.tasks.add( T19 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T19 );
			S9.storiesTask.add( T19 );
			T19.save();
		
			
			Task T20 = new Task();
			T20.description = "Documentation";
			T20.taskStory = S9;
			T20.taskStatus = S9.componentID.project.taskStatuses.get( 0 );
			T20.taskType = S9.componentID.project.taskTypes.get( 0 );
			T20.reporter = users.get( 0 );
			T20.assignee = users.get( 1 );
			T20.reviewer = users.get( 2 );
			T20.estimationPoints = 12.0;
			T20.taskSprint = Sp1;
			Sp1.tasks.add( T20 );
			S9.componentID.project.taskStatuses.get( 0 ).Tasks.add( T20 );
			S9.storiesTask.add( T20 );
			T20.save();
			
			Task T21 = new Task();
			T21.description = "Coding4";
			T21.taskStory = S1;
			T21.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T21.taskType = S1.componentID.project.taskTypes.get( 0 );
			T21.reporter = users.get( 0 );
			T21.assignee = users.get( 1 );
			T21.reviewer = users.get( 2 );
			T21.estimationPoints = 12.0;
			T21.taskSprint = Sp1;
			Sp1.tasks.add( T21 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T21 );
			S1.storiesTask.add( T21 );
			T21.save();
			
			
			Task T22 = new Task();
			T22.description = "Coding5";
			T22.taskStory = S1;
			T22.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T22.taskType = S1.componentID.project.taskTypes.get( 0 );
			T22.reporter = users.get( 0 );
			T22.assignee = users.get( 1 );
			T22.reviewer = users.get( 2 );
			T22.estimationPoints = 12.0;
			T22.taskSprint = Sp1;
			Sp1.tasks.add( T22 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T22 );
			S1.storiesTask.add( T22 );
			T22.save();
			
			
			
			Task T23 = new Task();
			T23.description = "Coding6";
			T23.taskStory = S1;
			T23.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T23.taskType = S1.componentID.project.taskTypes.get( 0 );
			T23.reporter = users.get( 0 );
			T23.assignee = users.get( 1 );
			T23.reviewer = users.get( 2 );
			T23.estimationPoints = 12.0;
			T23.taskSprint = Sp1;
			Sp1.tasks.add( T23 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T23 );
			S1.storiesTask.add( T23 );
			T23.save();
			
			Task T24 = new Task();
			T24.description = "Coding5";
			T24.taskStory = S1;
			T24.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T24.taskType = S1.componentID.project.taskTypes.get( 0 );
			T24.reporter = users.get( 0 );
			T24.assignee = users.get( 1 );
			T24.reviewer = users.get( 2 );
			T24.estimationPoints = 12.0;
			T24.taskSprint = Sp1;
			Sp1.tasks.add( T24 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T24 );
			S1.storiesTask.add( T24 );
			T24.save();
			
			Task T25 = new Task();
			T25.description = "Coding";
			T25.taskStory = S1;
			T25.taskStatus = S1.componentID.project.taskStatuses.get( 0 );
			T25.taskType = S1.componentID.project.taskTypes.get( 0 );
			T25.reporter = users.get( 0 );
			T25.assignee = users.get( 1 );
			T25.reviewer = users.get( 2 );
			T25.estimationPoints = 12.0;
			T25.taskSprint = Sp1;
			Sp1.tasks.add( T25 );
			S1.componentID.project.taskStatuses.get( 0 ).Tasks.add( T25 );
			S1.storiesTask.add( T25 );
			T25.save();
			

			Task T26 = new Task();
			T26.description = "Reviewing7";
			T26.taskStory = S8;
			T26.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T26.taskType = S8.componentID.project.taskTypes.get( 0 );
			T26.reporter = users.get( 0 );
			T26.assignee = users.get( 1 );
			T26.reviewer = users.get( 2 );
			T26.estimationPoints = 12.0;
			T26.taskSprint = Sp1;
			Sp1.tasks.add( T26 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T26 );
			S8.storiesTask.add( T26 );
			T26.save();
			
		
			Task T27 = new Task();
			T27.description = "Documentation";
			T27.taskStory = S8;
			T27.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T27.taskType = S8.componentID.project.taskTypes.get( 0 );
			T27.reporter = users.get( 0 );
			T27.assignee = users.get( 1 );
			T27.reviewer = users.get( 2 );
			T27.estimationPoints = 12.0;
			T27.taskSprint = Sp1;
			Sp1.tasks.add( T27 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T27 );
			S8.storiesTask.add( T27 );
			T27.save();
			
			Task T28 = new Task();
			T28.description = "Reviewing9";
			T28.taskStory = S8;
			T28.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T28.taskType = S8.componentID.project.taskTypes.get( 0 );
			T28.reporter = users.get( 0 );
			T28.assignee = users.get( 1 );
			T28.reviewer = users.get( 2 );
			T28.estimationPoints = 12.0;
			T28.taskSprint = Sp1;
			Sp1.tasks.add( T28 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T28 );
			S8.storiesTask.add( T28 );
			T28.save();
			

			Task T29 = new Task();
			T29.description = "senario reviewer";
			T29.taskStory = S8;
			T29.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T29.taskType = S8.componentID.project.taskTypes.get( 0 );
			T29.reporter = users.get( 0 );
			T29.assignee = users.get( 1 );
			T29.reviewer = users.get( 2 );
			T29.estimationPoints = 12.0;
			T29.taskSprint = Sp1;
			Sp1.tasks.add( T29 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T29 );
			S8.storiesTask.add( T29 );
			T29.save();
			
			
			Task T30 = new Task();
			T30.description = "Reviewing5";
			T30.taskStory = S8;
			T30.taskStatus = S8.componentID.project.taskStatuses.get( 0 );
			T30.taskType = S8.componentID.project.taskTypes.get( 0 );
			T30.reporter = users.get( 0 );
			T30.assignee = users.get( 1 );
			T30.reviewer = users.get( 2 );
			T30.estimationPoints = 12.0;
			T30.taskSprint = Sp1;
			Sp1.tasks.add( T30 );
			S8.componentID.project.taskStatuses.get( 0 ).Tasks.add( T30 );
			S8.storiesTask.add( T30 );
			T30.save();
			
			

			Task T31 = new Task();
			T31.description = "Coding";
			T31.taskStory = S4;
			T31.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T31.taskType = S4.componentID.project.taskTypes.get( 0 );

			T31.reporter = users.get( 4 );
			T31.assignee = users.get( 5 );
			T31.reviewer = users.get( 6 );
			T31.estimationPoints = 12.0;
			T31.taskSprint = Sp1;
			Sp1.tasks.add( T31 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T31 );
			S4.storiesTask.add( T31 );
			T31.save();
			
			

			Task T32 = new Task();
			T32.description = "Coding";
			T32.taskStory = S4;
			T32.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T32.taskType = S4.componentID.project.taskTypes.get( 0 );

			T32.reporter = users.get( 4 );
			T32.assignee = users.get( 5 );
			T32.reviewer = users.get( 6 );
			T32.estimationPoints = 12.0;
			T32.taskSprint = Sp1;
			Sp1.tasks.add( T32 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T32 );
			S4.storiesTask.add( T32 );
			T32.save();
			
			
			

			Task T33 = new Task();
			T33.description = "Coding";
			T33.taskStory = S4;
			T33.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T33.taskType = S4.componentID.project.taskTypes.get( 0 );

			T33.reporter = users.get( 4 );
			T33.assignee = users.get( 5 );
			T33.reviewer = users.get( 6 );
			T33.estimationPoints = 12.0;
			T33.taskSprint = Sp1;
			Sp1.tasks.add( T33 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T33 );
			S4.storiesTask.add( T33 );
			T33.save();
			
			

			Task T34 = new Task();
			T34.description = "Coding";
			T34.taskStory = S4;
			T34.taskStatus = S4.componentID.project.taskStatuses.get( 0 );
			T34.taskType = S4.componentID.project.taskTypes.get( 0 );

			T34.reporter = users.get( 4 );
			T34.assignee = users.get( 5 );
			T34.reviewer = users.get( 6 );
			T34.estimationPoints = 12.0;
			T34.taskSprint = Sp1;
			Sp1.tasks.add( T34 );
			S4.componentID.project.taskStatuses.get( 0 ).Tasks.add( T34 );
			S4.storiesTask.add( T34 );
			T34.save();
			
			
			Task T35 = new Task();
			T35.description = "Senario testing";
			T35.taskStory = S6;
			T35.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T35.taskType = S6.componentID.project.taskTypes.get( 0 );

			T35.reporter = users.get( 11 );
			T35.assignee = users.get( 12 );
			T35.reviewer = users.get( 13 );
			T35.estimationPoints = 1.0;
			T35.taskSprint = Sp1;
			Sp1.tasks.add( T35 );
			S7.componentID.project.taskStatuses.get( 0 ).Tasks.add( T35 );
			S7.storiesTask.add( T35 );
			T35.save();
			
			Task T36 = new Task();
			T36.description = "Documentation testing";
			T36.taskStory = S6;
			T36.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T36.taskType = S6.componentID.project.taskTypes.get( 0 );

			T36.reporter = users.get( 11 );
			T36.assignee = users.get( 12 );
			T36.reviewer = users.get( 13 );
			T36.estimationPoints = 1.0;
			T36.taskSprint = Sp1;
			Sp1.tasks.add( T36 );
			S7.componentID.project.taskStatuses.get( 0 ).Tasks.add( T36 );
			S7.storiesTask.add( T36 );
			T36.save();
			
			Task T37 = new Task();
			T37.description = "Senario testing";
			T37.taskStory = S6;
			T37.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T37.taskType = S6.componentID.project.taskTypes.get( 0 );

			T37.reporter = users.get( 11 );
			T37.assignee = users.get( 12 );
			T37.reviewer = users.get( 13 );
			T37.estimationPoints = 1.0;
			T37.taskSprint = Sp1;
			Sp1.tasks.add( T37 );
			S7.componentID.project.taskStatuses.get( 0 ).Tasks.add( T37 );
			S7.storiesTask.add( T37 );
			T37.save();
			
			
			Task T38 = new Task();
			T38.description = "Senario testing";
			T38.taskStory = S6;
			T38.taskStatus = S6.componentID.project.taskStatuses.get( 0 );
			T38.taskType = S6.componentID.project.taskTypes.get( 0 );

			T38.reporter = users.get( 11 );
			T38.assignee = users.get( 12 );
			T38.reviewer = users.get( 13 );
			T38.estimationPoints = 1.0;
			T38.taskSprint = Sp1;
			Sp1.tasks.add( T38 );
			S7.componentID.project.taskStatuses.get( 0 ).Tasks.add( T38 );
			S7.storiesTask.add( T38 );
			T38.save();
			
			
			
			// ************ LOGS BEGIN ************
			for( int l = 0; l < 30; l++ )
			{
				if( l % 2 == 0 )
					new Log( users.get( 0 ), "TEST", "TEST", 0, projects.get( 0 ), new Date() ).save();
				else
					new Log( users.get( 1 ), "TEST", "TEST", 0, projects.get( 1 ), new Date() ).save();
			}
			// ************ LOGS END ************

			// ************ USER NOTIFICATION PROFILES BEGIN ************
			for( int w = 0; w < users.size(); w++ )
			{
				new UserNotificationProfile( users.get( w ), projects.get( 0 ) ).save();
			}
			// ************ USER NOTIFICATION PROFILES END ************

			Artifact a1 = new Artifact( "Notes", "First note" );
			a1.save();
			Artifact a2 = new Artifact( "Notes", "Second note" );
			a2.save();
			Meeting M = new Meeting( "ERD", users.get( 0 ), "anything", new Date().getTime() + 1000000000, new Date().getTime(), "C1", "meeting", p1, null );
			M.artifacts.add( a1 );
			M.artifacts.add( a2 );
			a1.meetingsArtifacts.add( M );
			a2.meetingsArtifacts.add( M );
			M.tasks.add( T1 );
			M.tasks.add( T2 );
			T1.meeting.add( M );
			T2.meeting.add( M );
			M.save();

			Meeting m2 = new Meeting( "Scrum meeting", users.get( 0 ), "15 minute scrum meeting", new Date().getTime() + 1000000000, new Date().getTime(), "board", "scrum", projects.get( 0 ), Sp1 );
			m2.components.add( c1 );
			m2.infrontBoard = true;
			c1.componentMeetings.add( m2 );
			m2.save();

			Meeting m3 = new Meeting( "Scrum meeting", users.get( 0 ), "15 minute scrum meeting", new Date().getTime() + 1000000000, new Date().getTime(), "board", "scrum", projects.get( 0 ), Sp1 );
			m3.components.add( c2 );
			m3.infrontBoard = true;
			c2.componentMeetings.add( m3 );
			m3.save();

			for( int i = 0; i < 10; i++ )
			{
				MeetingAttendance ma;
				if( i < 4 )
				{
					ma = new MeetingAttendance( users.get( i ), m2 );
					ma.status = "declined";
					m2.users.add( ma );
					users.get( i ).attendantusers.add( ma );
					ma.save();
				}
				else if( i < 7 )
				{
					ma = new MeetingAttendance( users.get( i ), m3 );
					ma.status = "confirmed";
					m3.users.add( ma );
					users.get( i ).attendantusers.add( ma );
					ma.save();
				}
				else
				{
					ma = new MeetingAttendance( users.get( i ), M );
					ma.status = "waiting";
					M.users.add( ma );
					users.get( i ).attendantusers.add( ma );
					ma.save();
				}
			}

			ProductRole pr = new ProductRole( 1L, "scrum master", "can do anything" ).save();

			// Hadeer Younis Issue:
			T1.setEffortOfDay( 4, 0 );
			T1.setEffortOfDay( 3, 1 );
			T1.setEffortOfDay( 1, 2 );
			T1.setEffortOfDay( 3, 5 );
			T1.save();
			S1.failureSenario = " S1 :Failure Scenario 1/n S1 :Failure Scenario 2";
			S1.succussSenario = "S1: Siccess Scenario/nS1 :Success Scenario 2";
			S1.save();
			S3.failureSenario = "S3 :Failure Scenario 1/n S3 :Failure Scenario 2";
			S3.succussSenario = "S1: Siccess Scenario/nS1 :Success Scenario 2";
			S3.save();
			ProductRole pr1 = new ProductRole( 1L, "admin", "can do anything" ).save();
			S1.productRole = pr1;
			S1.save();
			S2.productRole = pr1;
			S2.save();

		}
	}
}

/*
 * import models.User; import play.jobs.Job; import
 * play.jobs.OnApplicationStart; import play.test.Fixtures;
 * @OnApplicationStart public class Bootstrap extends Job { public void doJob()
 * { if (User.count() == 0) { Fixtures.load("dummy-data.yml"); } } }
 */
