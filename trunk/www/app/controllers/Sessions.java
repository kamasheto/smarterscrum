package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Project;
import models.Session;
import models.User;
import play.mvc.With;

/**
 * Sessions controller
 * 
 * @author mahmoudsakr
 */
@With( Secure.class )
public class Sessions extends SmartController
{
	/**
	 * pings the server, updates the session of the current user
	 */
	public static void ping()
	{
		Session.update();

		List<Session> sessions = Session.find( "order by lastClick desc" ).fetch();

		List<User.Object> users = new ArrayList<User.Object>();
		for( Session session : sessions )
		{
			users.add( new User.Object( session.user.id, session.user.name, session.lastClick, session.user.isAdmin ) );
		}
		renderJSON( users );
	}

	/**
	 * pings the server, updates the online users of a certian project
	 * 
	 * @param id
	 *            (project id)
	 * @author Amr Hany
	 */

	public static void getOnline( long id )
	{
		Session.update();

		List<Session> sessions = Session.find( "order by lastClick desc" ).fetch();

		List<User.Object> onlineUsers = new ArrayList<User.Object>();
		for( Session session : sessions )
		{
			if( session.user.projects.contains( Project.findById( id ) ) )
				onlineUsers.add( new User.Object( session.user.id, session.user.name, session.lastClick, session.user.isAdmin ) );
		}
		renderJSON( onlineUsers );
	}

	/**
	 * Custom logout method to make sure session is removed from database before
	 * logging out
	 * 
	 * @throws Throwable
	 */
	public static void logout() throws Throwable
	{
		Session.delete( "user = ?", Security.getConnected() );
		Security.getConnected().openChats=null;
		Security.getConnected().save();
		Secure.logout();
	}
}
