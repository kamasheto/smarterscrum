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
	 * Custom logout method to make sure session is removed from database before
	 * logging out
	 */
	public static void logout() {
		try {			
			Session.delete( "user = ?", Security.getConnected() );
			Security.getConnected().openChats = null;
			Security.getConnected().save();
			Secure.logout();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
