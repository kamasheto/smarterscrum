package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import models.Component;
import models.Notification;
import models.Project;
import models.User;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.Mail;
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
	 * Retrieves the latest 30 notifications to the home page
	 * 
	 * @author Moataz
	 */
	public static void index()
	{
		/*
		 * User user = Security.getConnected(); List<Notification> notis =
		 * Notification.find( "user = " + user.id + "order by id desc"
		 * ).fetch(); List<Notification> noti; if( notis.size() < 30 ) noti =
		 * notis; else noti = notis.subList( 0, 29 ); // Notification nn = new
		 * Notification(Security.getConnected(), //"Smarter Scrum",
		 * "Smarter Scrum v.01 the very first product made by SmartSoft has been released!!!"
		 * , // (byte) 1); // Notification n = new
		 * Notification(Security.getConnected(), //"Hadeer Younis (Design)",
		 * "Plum v.03 is up and running!! Note that it's still in beta phase, So please report any bugs straight away!!"
		 * , // (byte) 1); // noti.add(n);noti.add(nn);
		 */
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
		Logs.addLog( Security.getConnected(), "Delete", "Component", c.id, c.project, new Date( System.currentTimeMillis() ) );
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
			String subject = "Your SmartSoft new Email activation requires your attention";
			String body = "Dear " + user.name + ", You have requested to change the Email Address associated with your account. Please click the following link to activate your account: " + "http://localhost:9000/accounts/doActivation?hash=" + user.activationHash;
			Mail.send( "se.smartsoft@gmail.com", user.email, subject, body );
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
		List<Notification> notifications = Notification.find( "receiver =" + user.id + " order by id desc" ).fetch();
		render( notifications );
	}

}