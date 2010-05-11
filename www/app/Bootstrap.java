import java.util.ArrayList;
import java.util.Date;

import models.Artifact;
import models.Component;
import models.Log;
import models.Meeting;
import models.MeetingAttendance;
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
					ma.status = "confirmed";
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
					ma.status = "confirmed";
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
