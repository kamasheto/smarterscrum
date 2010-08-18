package controllers;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import others.Event;
import others.LogSearchResult;
import others.NotificationSearchResult;

import models.Component;
import models.Log;
import models.Meeting;
import models.MeetingAttendance;
import models.Notification;
import models.Project;
import models.Sprint;
import models.User;
import notifiers.Notifications;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.Router;
import play.mvc.With;

/**
 * Class for static methods
 * 
 * @author mahmoudsakr
 */
@With( Secure.class )
public class Application extends SmartController
{
	/**
	 * Generates the hash value of the given String
	 * 
	 * @param str
	 * @return hash String
	 */
	public static String hash( String str )
	{
		String res = "";
		try
		{
			MessageDigest algorithm = MessageDigest.getInstance( "MD5" );
			algorithm.reset();
			algorithm.update( str.getBytes() );
			byte[] md5 = algorithm.digest();
			String tmp = "";
			for( int i = 0; i < md5.length; i++ )
			{
				tmp = (Integer.toHexString( 0xFF & md5[i] ));
				if( tmp.length() == 1 )
				{
					res += "0" + tmp;
				}
				else
				{
					res += tmp;
				}
			}
		}
		catch( NoSuchAlgorithmException ex )
		{
		}
		return res;
	}

	/**
	 * Renders the loading page
	 */
	public static void loading()
	{
		render();
	}

	/**
	 * Returns a 32 character long randomly generated hash string
	 * 
	 * @return hash String
	 */
	public static String randomHash()
	{
		return randomHash( 32 );
	}

	/**
	 * Generates a random hash String with a specified length
	 * 
	 * @param length
	 *            , The length of the hash string
	 * @return hash String
	 */
	public static String randomHash( int length )
	{
		return hash( System.currentTimeMillis() * Math.random() + "" ).substring( 0, length );
	}

	/**
	 * Retrieves the home page
	 */
	public static void index()
	{		
		render();
	}

	/**
	 * Renders all the components in a certain project and those currently in a
	 * sprint
	 * 
	 * @author Amr Hany
	 * @param id
	 *            ,Project ID
	 */
	public static void viewComponents( long id )
	{
		Project currentProject = Project.findById( id );
		if( currentProject.deleted )
			notFound();
		boolean inSprint = (currentProject.inSprint( new Date() ));
		String projectName = currentProject.name;
		List<Component> components = Component.find( "byProject.idAndDeleted", id, false ).fetch();

		render( components, id, projectName, inSprint, currentProject );
	}

	/**
	 * Deletes a component
	 * 
	 * @author Amr Hany
	 * @param componentID
	 *            , Component ID
	 */
	public static void deleteComponent( long id )
	{
		Component c = Component.findById( id );
		if( c.deleted )
			notFound();
		Security.check( Security.getConnected().in( c.project ).can( "deleteComponent" ) );
		c.deleteComponent();
		Log.addUserLog("Deleted " + c.getFullName(), c, c.project);
//		Logs.addLog( Security.getConnected(), "Delete", "Component", c.id, c.project, new Date( System.currentTimeMillis() ) );
	}

	/**
	 * Renders the hash of any String
	 * 
	 * @param str
	 *            , The inputed String
	 */
	public static void md5( String str )
	{
		renderText( hash( str ) );
	}

	/**
	 * Renders the adminIndexPage page
	 */
	public static void adminIndexPage()
	{
		Security.check( Security.getConnected().isAdmin );
		redirect( "/projects/manageProjectRequests" );
	}

	/**
	 * Renders the adminIndex page
	 */
	public static void adminIndex()
	{
		Security.check( Security.getConnected().isAdmin );
		render();
	}

	/**
	 * Renders an editable profile
	 * 
	 * @param id
	 *            user id
	 */

	public static void profile( long id )
	{
		if( id == 0 )
		{
			id = Security.getConnected().id;
		}
		User user = User.findById( id );
		if( user.deleted )
			notFound();
		Security.check( Security.getConnected().equals( user ) );
		render( user );
	}

	/**
	 * Saves new user information
	 * 
	 * @param name
	 *            , the users name
	 * @param pwd1
	 *            , the password
	 * @param pwd2
	 *            , the confirmation password
	 * @param email
	 *            , the users email address
	 * @param id
	 *            , user id
	 */
	public static void editProfile( @Required( message = "You must enter a name" ) String name, String pwd1, String pwd2, @Required( message = "You must enter an email" ) @Email( message = "You must enter a valid email" ) String email, long id )
	{
		User user = User.findById( id );
		if( user.deleted )
			notFound();
		Security.check( Security.getConnected().equals( user ) );
		if( Validation.hasErrors() || (pwd1.length() > 0 && !pwd1.equals( pwd2 )) )
		{
			flash.error( "An error has occured" );
			profile( id );
		}
		String oldEmail = user.email;
		user.name = name;
		if( pwd1.length() > 0 )
			user.pwdHash = Application.hash( pwd1 );
		user.email = email;
		user.save();
		if( !user.email.equals( oldEmail ) )
		{
			user.activationHash = Application.randomHash( 32 );
			user.isActivated = false;
			user.save();
			session.put( "username", email );			
			String url = Router.getFullUrl("Accounts.doActivation")+"?hash=" + user.activationHash+"|firstTime=false";
			Notifications.activate(user.email, user.name, url, true);			
			flash.success( "Successfully saved your data! , please check your new Email and follow the instructions sent by us to confirm your new Email." );
			profile( id );
		}
		else
		{
			flash.success( "Successfully saved your data!" );
			profile( id );
		}

	}

	/**
	 * Renders a web page that contains a script that closes the overlay iframe.
	 * 
	 * @param js
	 *            , The script that runs in the parent frame
	 * @param nativeJS
	 *            , The script that runs in the current frame
	 * @author Hadeer younis
	 */
	public static void overlayKiller( String js, String nativeJS )
	{
		render( js, nativeJS );
	}

	/**
	 * Renders a page that loads the workspace corresponding to the project id
	 * given, and the specified url loaded in a magic box. This will be used in
	 * links found in emails.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , project id
	 * @param url
	 *            , url to be loaded
	 * @param isOverlay
	 *            , whether or not the url will open in an overlay
	 */
	public static void externalOpen( long id, String url, boolean isOverlay )
	{
		render( id, url, isOverlay );
	}

	/**
	 * Renders all the notifications for the currently connected user
	 */
	public static void showNotifications()
	{
		User user = Security.getConnected();
		boolean emailing = user.enableEmails;
			
		render(  emailing);

	}public static void list( int page, int perPage) {
		if (perPage == 0) {
			perPage = 10;
		}
		List<Notification> notifications = Notification.find("byReciever",Security.getConnected()).from(perPage * page).fetch(perPage);
		for(Notification noti:notifications){
			if(noti.unread){
				Security.getConnected().ReadNotifications++;
				Security.getConnected().save();
			}
			
			
		}
		NotificationSearchResult result = new NotificationSearchResult();
		result.notifications = notifications;
		result.currentPage = page;
		result.totalPages = (int) Notification.find("byReciever",Security.getConnected().id+ " order by id desc").fetch().size() / perPage;
		renderJSON(result);
	}
	
	/**
	 * perform the action of choosing the option whether to receive emails or not
	 * @param enable : 0 if to stop 1 for enabling 
	 */
	public static void modifyEmailing(int enable)
	{
		User user = Security.getConnected();		
		if(enable==1)
			user.enableEmails=true;
		else
			user.enableEmails=false;
		user.save();		
	}
	/**
	 * A method that renders the calendar page with the day's events in the side bar.
	 */
	public static void showEvents(){
		Date x = new Date();
		int year = x.getYear()+1900;
		List<Integer> years = new ArrayList<Integer>();
		for(int i =-5 ; i<5;i++ ){
			years.add(year+i);
		}
		Date today = new Date();
		List<Project> projects  = Security.getConnected().projects;
		List<Sprint> sprints = new ArrayList<Sprint>();
		List<Sprint> sprints2 = new ArrayList<Sprint>();
		for(Project project : projects){
			for(Sprint sprint : project.sprints){
				if(!sprint.deleted && sprint.startDate.getDate()== today.getDate() && sprint.startDate.getMonth() == today.getMonth() && sprint.startDate.getYear() == today.getYear()){
					sprints.add(sprint);
				}
				if(!sprint.deleted && sprint.endDate.getDate()== today.getDate() && sprint.endDate.getMonth() == today.getMonth() && sprint.endDate.getYear() == today.getYear()){
					sprints2.add(sprint);
				}
			}
		}
		List<MeetingAttendance> meetings1 = MeetingAttendance.find("byUserAndDeleted", Security.getConnected(),false).fetch();
		List<Meeting> meetings = new ArrayList<Meeting>();
		for(MeetingAttendance meeting : meetings1){
			if(!meeting.meeting.deleted){
				Date start = new Date ( meeting.meeting.startTime);
				if(start.getDate()== today.getDate() && start.getMonth() == today.getMonth() && start.getYear() == today.getYear())
					meetings.add(meeting.meeting);
			}
		}
		render(years, sprints, sprints2, meetings);
	}
	/**
	 * A method that returns the sprints of the connected user's projects.
	 */
	public static void sprints(){
		List<Project> projects  = Security.getConnected().projects;
		List<Sprint.Object> sprints = new ArrayList<Sprint.Object>();
		for(Project project : projects){
			for(Sprint sprint : project.sprints){
				if(!sprint.deleted){
					sprints.add(new Sprint.Object(sprint.id, sprint.sprintNumber, sprint.startDate, sprint.endDate, sprint.project.name, sprint.project.id));
				}
			}
		}
		renderJSON(sprints);
	}
	/**
	 * A method that returns the meetings of the connected user's projects.
	 */
	public static void meetings(){
		List<MeetingAttendance> meetings1 = MeetingAttendance.find("byUserAndDeleted", Security.getConnected(),false).fetch();
		List<Meeting.Object> meetings = new ArrayList<Meeting.Object>();
		for(MeetingAttendance meeting : meetings1){
			if(!meeting.meeting.deleted){
				meetings.add(new Meeting.Object(meeting.meeting.id, meeting.meeting.startTime, meeting.meeting.project.name, meeting.meeting.name, meeting.meeting.project.id));
			}
		}
		renderJSON(meetings);
	}
	
	/**
	 * A method that returns a selected day Events (Sprints and meetings)
	 * @param day : the day of the month from 1-31.
	 * @param month : the month of the year from 1-12.
	 * @param year : the year.
	 */
	public static void dayEvents(int day, int month, int year){
		List<Project> projects  = Security.getConnected().projects;
		Event events = new Event();
		Date today = new Date();
		if(!(day == today.getDate() && month==today.getMonth()+1 && year == today.getYear()+1900)){
		for(Project project : projects){
			for(Sprint sprint : project.sprints){
				if(!sprint.deleted && sprint.startDate.getDate()== day && sprint.startDate.getMonth()+1 == month && sprint.startDate.getYear()+1900 == year){
					events.sprints.add(new Sprint.Object(sprint.id, sprint.sprintNumber, sprint.startDate, sprint.endDate, sprint.project.name, sprint.project.id));
				}
				if(!sprint.deleted && sprint.endDate.getDate()== day && sprint.endDate.getMonth()+1 == month && sprint.endDate.getYear()+1900 == year){
					events.sprints.add(new Sprint.Object(sprint.id, sprint.sprintNumber, sprint.startDate, sprint.endDate, sprint.project.name, sprint.project.id));
				}
			}
		}
		List<MeetingAttendance> meetings1 = MeetingAttendance.find("byUserAndDeleted", Security.getConnected(),false).fetch();
		for(MeetingAttendance meeting : meetings1){
			if(!meeting.meeting.deleted){
				Date start = new Date ( meeting.meeting.startTime);
				if(start.getDate()== day && start.getMonth()+1 == month && start.getYear()+1900 == year)
					events.meetings.add(new Meeting.Object(meeting.meeting.id, meeting.meeting.startTime, meeting.meeting.project.name, meeting.meeting.name, meeting.meeting.project.id));
			}
		}
		}
		renderJSON(events);
	}
}