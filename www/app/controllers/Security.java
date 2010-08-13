package controllers;

import notifiers.Notifications;
import models.Project;
import models.User;
import models.Log;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.Router;

/**
 * Security class/controller.. handles all security checks/permissions
 * 
 * @author mahmoudsakr
 */
public class Security extends Secure.Security
{

	/**
	 * gets the connected user in the session.
	 * 
	 * @return the currently connected user
	 */
	public static User getConnected()
	{
		String usr = (isConnected() ? connected() : "").toLowerCase();
		return User.find( "select u from User u where u.email=? or u.name=?", usr, usr ).first();
	}

	/**
	 * returns whether or not this email/pwd form a valid combination
	 * 
	 * @param email
	 *            user email
	 * @param password
	 *            user password
	 * @return true if such a user exist, false otherwise
	 */
	public static boolean authentify( String email, String password )
	{
		User user = User.find( "select u from User u where (u.email=? or u.name=?) and u.pwdHash = ?", email.toLowerCase(), email.toLowerCase(), Application.hash( password ) ).first();
		/* By Tj.Wallas_ in Sprint2 */
		if( user != null && !user.isActivated )
		{
			flash.error( "Your account is not activated, please follow the instructions in the Email we sent you to activate your account" );
			try
			{
				Secure.login();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}
		else if( user != null && user.deleted )
		{
			flash.error( "Your account has been deleted. Please contact a website administrator for further information." );
			try
			{
				Secure.login();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}

		return user != null;
	}

	/**
	 * checks whether the boolean variable gives the user access to view the
	 * page or not
	 * 
	 * @param can
	 *            if false, user gets an access denied page
	 * @return false
	 */
	public static boolean check( boolean can )
	{
		if( !can )
		{
			forbidden();
		}
		return false;
	}

	/**
	 * checks whether the connected user could perform the action specified by
	 * permission in the given project
	 * 
	 * @param project
	 * @param permission
	 * @return false
	 */
	public static boolean check( Project project, String permission )
	{
		return check( getConnected().in( project ).can( permission ) );
	}

	/**
	 * renders the forgot password view
	 */
	public static void forgotPassword()
	{
		render();
	}

	/**
	 * called from the forgot password view on order to check if the user
	 * name,email are existing in the database or not
	 * 
	 * @param username
	 */
	public static void checkUsername( @Required String username )
	{
		if( validation.hasErrors() )
		{
			flash.error( "Please enter a valid username/Email" );
			Security.forgotPassword();
		}
		User u;
		username = username.toLowerCase();
		u = User.find( "select u from User u where u.email=? or u.name=?", username, username ).first();
		if( u == null || u.deleted == true )
		{
			flash.error( "This username/Email does not exist" );
			// Logs.addLog( "A guest tried to recover a password of the username " + username + " but the username was not found" );
			Log.addLog("Guest submitted a recovery password for " + username + " but was not found");
			Security.forgotPassword();
		}
		else
		{
			u.recoveryHash = Application.randomHash( 10 );
			u.save();			
			String url = Router.getFullUrl("Security.passwordRecovery")+"?h=" + u.recoveryHash;			
			Notifications.lostPass(u, url);
			// Logs.addLog( "A guest tried to recover a password of the username " + username );
			Log.addLog("Guest tried to recover password for username: " + username);
			flash.success( "An email was sent to your email address." );
			try
			{
				Secure.login();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}

	}

	/**
	 * renders the view of the user to enter his new password on password
	 * recovery
	 * 
	 * @param h
	 *            which is the recovery hash
	 */
	public static void passwordRecovery( String h )
	{
		User user = User.find( "byRecoveryHash", h ).first();
		if( user == null )
		{
			notFound();
		}
		render( user );
	}

	/**
	 * recover the user's password and change it successfully
	 * 
	 * @param username
	 *            the username
	 * @param pass1
	 *            password field one
	 * @param pass2
	 *            password filed two (have to be the same like password field
	 *            one)
	 */
	public static void recoverPassword( @Required String username, @Required String pass1, @Required String pass2 )
	{
		User u = User.find( "byName", username ).first();
		if( validation.hasErrors() )
		{
			flash.error( "Please enter both fields" );
			passwordRecovery( u.recoveryHash );
		}
		else if( !pass1.equals( pass2 ) )
		{
			flash.error( "Your passwords do not match" );
			passwordRecovery( u.recoveryHash );
		}
		else
		{
			u.pwdHash = Application.hash( pass1 );
			u.recoveryHash = "";
			u.save();
			flash.success( "Password changed successfully" );
			// Logs.addLog( u.name + " forgot his/her password and recovered it successfully" );
			Log.addLog(u.name + " recovered his password successfully");
			try
			{
				Secure.login();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}

	}
}