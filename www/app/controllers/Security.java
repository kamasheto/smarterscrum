package controllers;

import models.Project;
import models.User;
import play.data.validation.Required;
import play.libs.Mail;

/**
 * Security class/controller.. handles all security checks/permissions
 * 
 * @author mahmoudsakr
 */
public class Security extends Secure.Security
{

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

	public static void forgotPassword()
	{
		render();
	}

	public static void checkUsername( @Required String username )
	{
		if( validation.hasErrors() )
		{
			flash.error( "Please enter a valid username/Email" );
			Security.forgotPassword();
		}
		User u;
		username=username.toLowerCase();
		if( username.contains( "@" ) )
		{
			u = User.find( "byEmail", username ).first();
		}
		else
		{
			u = User.find( "byName", username ).first();
		}
		if( u == null || u.deleted==true)
		{
			flash.error( "This username/Email does not exist" );
			Logs.addLog( "A guest tried to recover a password of the username " + username + " but the username was not found" );
			Security.forgotPassword();
		}
		else
		{
			u.recoveryHash = Application.randomHash( 10 );
			u.save();
			String subject = "Your SmarterScrum account password recovery";
			String body = "Dear " + u.name + ", Please click the following link to recover your password: " + "http://localhost:9000/security/passwordRecovery?h=" + u.recoveryHash;
			Mail.send( "se.smartsoft@gmail.com", u.email, subject, body );
			Logs.addLog( "A guest tried to recover a password of the username " + username );
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

	public static void passwordRecovery( String h )
	{
		User user = User.find( "byRecoveryHash", h ).first();
		if(user==null)
		{
			notFound();
		}
		render( user );
	}

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
			Logs.addLog( u.name + " forgot his/her password and recovered it successfully" );
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