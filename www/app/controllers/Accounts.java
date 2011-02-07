package controllers;

import javax.persistence.PersistenceException;

import notifiers.Notifications;

import models.User;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.Router;

/**
 * Handles all of the actions related to Accounts
 * 
 * @author Amr Tj.Wallas
 */
public class Accounts extends SmartController
{
	/**
	 * Creates a new user
	 * 
	 * @param name
	 *            , user name of that new user.
	 * @param email
	 *            , email of that new user.
	 * @param password
	 *            , password of that new user.
	 * @param confirm_password
	 *            , password confirmation of that new user.
	 * @exception PersistenceException
	 *                , fired on database constraints violations.
	 */
	public static void add_user( @Required String name, @Required @Email String email, @Required String password, @Required String confirm_password )
	{
		if( validation.hasErrors() )
		{
			params.flash();
			validation.keep();
			register();
		}
		else if( !password.equals( confirm_password ) )
		{
			flash.error( "Your passwords do not match" );
			validation.keep();
			register();
		}
		else
		{
			try
			{
				User existing_user = User.find( "name like '" + name + "' or " + "email like '" + email + "'" ).first();
				if( existing_user != null )
				{
					flash.error( "Oops, that user already exists!" + "\t" + "Please choose another user name and/or email." );
					register();
				}
				User user = new User( name, email, password );
				user.save();
				String url = Router.getFullUrl("Accounts.doActivation")+"?hash=" + user.activationHash+"&firstTime=true";				
				Notifications.activate(user.email, user.name, url, false);
				flash.success( "You have been registered. An Activation link has been sent to your Email Address" );
				Secure.login();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Renders the register page
	 */
	public static void register()
	{
		render();
	}

	/**
	 * Renders the deletion request view
	 */
	public static void render_deletion_request()
	{
		if( !Security.isConnected() )
		{
			Security.error( "You are not registered, Please login if you haven't done so" );

		}
		else if( Security.getConnected().pendingDeletion )
		{
			User user = Security.getConnected();
			render( user );
		}
		render();
	}

	/**
	 * Requests the deletion of the currently connected user.
	 * 
	 * @param pwd
	 *            , confirmation password
	 */

	public static void deletionRequest( @Required String pwd )
	{
		Security.check( Security.isConnected() );
		if( validation.hasErrors() )
		{
			params.flash();
			validation.keep();
			render_deletion_request();
		}
		else
		{
			User user_found = Security.getConnected();
			String pwd_hash = Application.hash( pwd );
			if( !user_found.pwdHash.equals( pwd_hash ) )
			{
				flash.error( "You have entered a wrong password!" );
				render_deletion_request();
			}
			else
			{
				user_found.pendingDeletion = true;
				user_found.save();
				flash.success( "your deletion request has been successfully sent!" );
				redirect( "/" );
			}
		}

	}

	/**
	 * Activates a user with an activation hash "hash"
	 * 
	 * @param hash
	 *            ,The Activation hash value of that user.
	 * @throws Throwable
	 *             ,Any exception that might happen during the login process is
	 *             thrown here as well.
	 * @since Sprint2.
	 */
	public static void doActivation( String hash, boolean firstTime ) throws Throwable
	{
		User currentUser = User.find( "activationHash", hash ).first();
		if( currentUser != null && !currentUser.isActivated )
		{
			currentUser.isActivated = true;
			currentUser.save();
			Notifications.welcome(currentUser, firstTime);
			flash.success( "Thank you , your Account has been Activated! . Login Below" );
		}
		else
			flash.error( "This activation link is not valid or has expired. Activation Failed!" );
		Secure.login();
	}

	/**
	 * Undoes the deletion request of current connected user.
	 * 
	 * @since Sprint3
	 */
	public static void undoRequest()
	{
		Security.check( Security.isConnected() );
		User user = Security.getConnected();
		user.pendingDeletion = false;
		user.save();
		flash.success( "Your deletion request has been successfully undone !" );
		redirect( "/" );
	}

}
