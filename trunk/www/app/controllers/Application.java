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

	public static void loading()
	{
		render();
	}
	
	public static String randomHash()
	{
		return randomHash( 32 );
	}

	public static String randomHash( int length )
	{
		return hash( System.currentTimeMillis() * Math.random() + "" ).substring( 0, length );
	}

	/**
	 *@author Moataz this method retrieves the latest 30 notifications to the
	 *         home page
	 */
	public static void index()
	{
		/*User user = Security.getConnected();
		List<Notification> notis = Notification.find( "user = " + user.id + "order by id desc" ).fetch();
		List<Notification> noti;
		if( notis.size() < 30 )
			noti = notis;
		else
			noti = notis.subList( 0, 29 );
		// Notification nn = new Notification(Security.getConnected(),
		// "Smarter Scrum","Smarter Scrum v.01 the very first product made by SmartSoft has been released!!!",
		// (byte) 1);
		// Notification n = new Notification(Security.getConnected(),
		// "Hadeer Younis (Design)","Plum v.03 is up and running!! Note that it's still in beta phase, So please report any bugs straight away!!",
		// (byte) 1);
		// noti.add(n);noti.add(nn);*/
		render();
	}

	public static void notificationsHistory()
	{
		User user = Security.getConnected();
		List<Notification> notis = Notification.find( "user = " + user.id + "order by id desc" ).fetch();
		render( notis );
	}

	/**
	 * View components controller which takes a projectID as an ID and returns
	 * the list of components to use it in the model view and it checks also if
	 * this component is in sprint or not
	 * 
	 * @author Amr Hany
	 * @param ProjectID
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
	 * View component controller is the method called to render the component
	 * and the sprint status to the view in order to use them
	 * 
	 * @author Amr Hany
	 * @param componentID
	 */
	// public static void viewComponent(long id) {
	// Component component = Component.findById(id);
	// if(component == null)
	// System.out.println("NULL YA AMR YA HANY");
	// boolean inSprint = component.project.inSprint(new Date());
	// render(component);
	// }

	/**
	 * This method is used to Delete a component by calling the model's method.
	 * 
	 * @author Amr Hany
	 * @param componentID
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

	public static void md5( String str )
	{
		renderText( hash( str ) );
	}

	// @Check ("systemAdmin")
	public static void adminIndexPage()
	{
		Security.check( Security.getConnected().isAdmin );
		redirect( "/projects/manageProjectRequests" );
	}

	// @Check ("systemAdmin")
	public static void adminIndex()
	{
		Security.check( Security.getConnected().isAdmin );
		render();
	}

	/**
	 * View editable profile
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
	 * @param pwd1
	 * @param pwd2
	 * @param email
	 * @param id
	 *            user id
	 */
	public static void editProfile( @Required( message = "You must enter a name" ) String name, String pwd1, String pwd2, @Required( message = "You must enter an email" ) @Email( message = "You must enter a valid email" ) String email, long id )
	{
		User usr = User.findById( id );
		if( usr.deleted )
			notFound();
		Security.check( Security.getConnected().equals( usr ) );
		if( Validation.hasErrors() || (pwd1.length() > 0 && !pwd1.equals( pwd2 )) )
		{
			flash.error( "An error has occured" );
			profile( id );
		}
		String oldEmail = usr.email;
		usr.name = name;
		if( pwd1.length() > 0 )
			usr.pwdHash = Application.hash( pwd1 );
		usr.email = email;
		usr.save();
		// Added By Wallas in Sprint 2.
		if( !usr.email.equals( oldEmail ) )
		{
			usr.activationHash = Application.randomHash( 32 );
			usr.isActivated = false;
			usr.save();
			session.put( "username", email ); // Update the session cookie by
			// setting the new Email.
			String subject = "Your SmartSoft new Email activation requires your attention";
			String body = "Dear " + usr.name + ", You have requested to change the Email Address associated with your account. Please click the following link to activate your account: " + "http://localhost:9000/accounts/doActivation?hash=" + usr.activationHash;
			Mail.send( "se.smartsoft@gmail.com", usr.email, subject, body );
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
	 * @author Hadeer younis
	 */
	public static void overlayKiller(String js)
	{
		render(js);
	}

	/**
	 * Renders a page that loads the workspace corresponding to the project id
	 * given, and the specified url loaded in a magic box.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , project id
	 * @param url
	 *            , url to be loaded
	 * @param isOverlay
	 *            , wether or not the url will open in an overlay
	 */
	public static void externalOpen( long id, String url, boolean isOverlay )
	{
		render( id, url, isOverlay );
	}
}