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
import models.Setting;
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
		String result = "";
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
					result += "0" + tmp;
				}
				else
				{
					result += tmp;
				}
			}
		}
		catch( NoSuchAlgorithmException ex )
		{
		}
		return result;
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
	public static String random_hash()
	{
		return random_hash( 32 );
	}

	/**
	 * Generates a random hash String with a specified length
	 * 
	 * @param length
	 *            , The length of the hash string
	 * @return hash String
	 */
	public static String random_hash( int length )
	{
		return hash( System.currentTimeMillis() * Math.random() + "" ).substring( 0, length );
	}

	/**
	 * Retrieves the home page
	 */
	public static void index()
	{
		Setting settings = Setting.findById( 1L );
		render( settings );
	}

	/**
	 * Renders all the components in a certain project and those currently in a
	 * sprint
	 * 
	 * @author Amr Hany
	 * @param id
	 *            ,Project ID
	 */
	public static void view_components( long id )
	{
		Project current_project = Project.findById( id );
		if( current_project.deleted )
			notFound();
		boolean in_sprint = (current_project.inSprint( new Date() ));
		String project_name = current_project.name;
		List<Component> components = Component.find( "byProject.idAndDeleted", id, false ).fetch();

		render( components, id, project_name, in_sprint, current_project );
	}

	/**
	 * Deletes a component
	 * 
	 * @author Amr Hany
	 * @param componentID
	 *            , Component ID
	 */
	public static void delete_component( long id )
	{
		Component component = Component.findById( id );
		if( component.deleted )
			notFound();
		Security.check( Security.getConnected().in( component.project ).can( "deleteComponent" ) );
		component.delete_component();
		Log.addUserLog( "Deleted " + component.get_full_name(), component, component.project );
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
	public static void admin_index_page()
	{
		Security.check( Security.getConnected().isAdmin );
		redirect( "/projects/manageProjectRequests" );
	}

	/**
	 * Renders the adminIndex page
	 */
	public static void admin_index()
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
			user.activationHash = Application.random_hash( 32 );
			user.isActivated = false;
			user.save();
			session.put( "username", email );
			String url = Router.getFullUrl( "Accounts.doActivation" ) + "?hash=" + user.activationHash + "&firstTime=false";
			Notifications.activate( user.email, user.name, url, true );
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
	 * @param native_js
	 *            , The script that runs in the current frame
	 * @author Hadeer younis
	 */
	public static void overlay_killer( String js, String native_js )
	{
		render( js, native_js );
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
	 * @param is_overlay
	 *            , whether or not the url will open in an overlay
	 */
	public static void external_open( long id, String url, boolean is_overlay )
	{
		render( id, url, is_overlay );
	}

	/**
	 * Renders all the notifications for the currently connected user
	 */
	public static void show_notifications( int page )
	{
		User user = Security.getConnected();
		boolean first = false;
		boolean last = false;
		List<Notification> all_notifications = Notification.find( "byReceiver", user ).fetch();
		int total_pages = (int) all_notifications.size() / 10;
		if( all_notifications.size() % 10 != 0 )
		{
			total_pages++;
		}
		if( page == total_pages )
		{
			last = true;
		}
		else
		{
			if( page == 1 )
			{
				first = true;
			}
		}

		List<Notification> notifications_page;
		if( first )
		{
			notifications_page = Notification.find( "byReceiver", user ).from( 1 ).fetch( 10 );
		}
		else
		{
			int temp = (page - 1) * 10;
			if(temp<0)
				temp = 0;
			notifications_page = Notification.find( "byReceiver", user ).from( temp ).fetch( 10 );
		}
		for( Notification noti : notifications_page )
		{
			if( noti.unread )
			{
				user.ReadNotifications++;
				noti.unread = false;
				noti.save();
				user.save();
			}
		}
		boolean emailing = user.enableEmails;

		render( page, notifications_page, emailing, last, first );

	}
	/**
	 * renders a list of notifications in a certain page
	 * 
	 * @param page
	 * @param per_page
	 */
	public static void notifications_list( int page, int per_page )
	{
		if( per_page == 0 )
		{
			per_page = 10;
		}
		NotificationSearchResult result = new NotificationSearchResult();
		List<Notification> all_notifications = Notification.find( "byReceiver", Security.getConnected().id ).fetch();
		List<Notification> notifications_page = all_notifications.subList( page * per_page, page * per_page + per_page <= all_notifications.size() ? page * per_page + per_page : all_notifications.size() );
		for( Notification notification : all_notifications )
		{
			if( notification.unread )
			{
				result.newNotifications++;
			}
		}
		result.notifications = notifications_page;
		result.currentPage = page + 1;
		result.totalPages = (int) all_notifications.size() / per_page;
		renderJSON( result );
	}

	/**
	 * perform the action of choosing the option whether to receive emails or
	 * not
	 * 
	 * @param enable
	 *            : 0 if to stop 1 for enabling
	 */
	public static void optional_notifiations( int enable )
	{
		User user = Security.getConnected();
		if( enable == 1 )
			user.enableEmails = true;
		else
			user.enableEmails = false;
		user.save();
	}

	/**
	 * A method that renders the calendar page with the day's events in the side
	 * bar.
	 */
	public static void show_events_calender()
	{
		Date date = new Date();
		int year = date.getYear() + 1900;
		List<Integer> years = new ArrayList<Integer>();
		for( int i = -5; i < 5; i++ )
		{
			years.add( year + i );
		}
		Date today = new Date();
		List<Project> projects = Security.getConnected().projects;
		List<Sprint> sprints = new ArrayList<Sprint>();
		List<Sprint> sprints2 = new ArrayList<Sprint>();
		for( Project project : projects )
		{
			for( Sprint sprint : project.sprints )
			{
				if( !sprint.deleted && sprint.startDate == today)
				{
					sprints.add( sprint );
				}
				if( !sprint.deleted && sprint.endDate == today)
				{
					sprints2.add( sprint );
				}
			}
		}
		List<MeetingAttendance> meetings1 = MeetingAttendance.find( "byUserAndDeleted", Security.getConnected(), false ).fetch();
		List<Meeting> meetings = new ArrayList<Meeting>();
		for( MeetingAttendance meeting : meetings1 )
		{
			if( !meeting.meeting.deleted )
			{
				Date start = new Date( meeting.meeting.startTime );
				if( start == today)
					meetings.add( meeting.meeting );
			}
		}
		render( years, sprints, sprints2, meetings );
	}

	/**
	 * renders the sprints of the connected user's projects.
	 */
	public static void sprints()
	{
		List<Project> projects = Security.getConnected().projects;
		List<Sprint.Object> sprints = new ArrayList<Sprint.Object>();
		for( Project project : projects )
		{
			for( Sprint sprint : project.sprints )
			{
				if( !sprint.deleted )
				{
					sprints.add( new Sprint.Object( sprint.id, sprint.number, sprint.startDate, sprint.endDate, sprint.project.name, sprint.project.id ) );
				}
			}
		}
		renderJSON( sprints );
	}

	/**
	 * renders the meetings of the connected user's projects.
	 */
	public static void meetings()
	{
		List<MeetingAttendance> meeting_attendance = MeetingAttendance.find( "byUserAndDeleted", Security.getConnected(), false ).fetch();
		List<Meeting.Object> meetings = new ArrayList<Meeting.Object>();
		for( MeetingAttendance meeting : meeting_attendance )
		{
			if( !meeting.meeting.deleted )
			{
				meetings.add( new Meeting.Object( meeting.meeting.id, meeting.meeting.startTime, meeting.meeting.project.name, meeting.meeting.name, meeting.meeting.project.id ) );
			}
		}
		renderJSON( meetings );
	}

	/**
	 * renders a list of events that occur in a certian date (day, month, year)
	 * 
	 * @param day
	 *            : the day of the month from 1-31.
	 * @param month
	 *            : the month of the year from 1-12.
	 * @param year
	 *            : the year.
	 */
	public static void events_in_date( int day, int month, int year )
	{
		List<Project> projects = Security.getConnected().projects;
		Event events = new Event();
		Date today = new Date();
		if( !(day == today.getDate() && month == today.getMonth() + 1 && year == today.getYear() + 1900) )
		{
			for( Project project : projects )
			{
				for( Sprint sprint : project.sprints )
				{
					if( !sprint.deleted && sprint.startDate.getDate() == day && sprint.startDate.getMonth() + 1 == month && sprint.startDate.getYear() + 1900 == year )
					{
						events.sprints.add( new Sprint.Object( sprint.id, sprint.number, sprint.startDate, sprint.endDate, sprint.project.name, sprint.project.id ) );
					}
					if( !sprint.deleted && sprint.endDate.getDate() == day && sprint.endDate.getMonth() + 1 == month && sprint.endDate.getYear() + 1900 == year )
					{
						events.sprints.add( new Sprint.Object( sprint.id, sprint.number, sprint.startDate, sprint.endDate, sprint.project.name, sprint.project.id ) );
					}
				}
			}
			List<MeetingAttendance> meeting_attendance = MeetingAttendance.find( "byUserAndDeleted", Security.getConnected(), false ).fetch();
			for( MeetingAttendance meeting : meeting_attendance )
			{
				if( !meeting.meeting.deleted )
				{
					Date start = new Date( meeting.meeting.startTime );
					if( start.getDate() == day && start.getMonth() + 1 == month && start.getYear() + 1900 == year )
						events.meetings.add( new Meeting.Object( meeting.meeting.id, meeting.meeting.startTime, meeting.meeting.project.name, meeting.meeting.name, meeting.meeting.project.id ) );
				}
			}
		}
		renderJSON( events );
	}
}