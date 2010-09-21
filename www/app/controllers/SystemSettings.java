package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(Secure.class)
public class SystemSettings extends SmartController {
	/**
	 * Renders the system settings
	 */
	public static void index() {	
		Security.check(Security.getConnected().isAdmin);
		Setting settings = Setting.findById(1L);
		render(settings);
	}
	
	/**
	 * Saves the key with the new value
	 */
	public static void save(String key, String new_value) {
		Security.check(Security.getConnected().isAdmin);
		
		Setting settings = Setting.findById(1L);
		String javascript = "";
		if (key.equalsIgnoreCase("sitename")) {
			settings.siteName = new_value;
			javascript = "update_title('"+new_value+"')";
		} else if (key.equalsIgnoreCase("twitterhash")) {
			settings.twitterHash = new_value;
			javascript = "update_twitter_hash('"+new_value+"')";
		} else if (key.equalsIgnoreCase("entriesperbox")) {
			try {
				settings.defaultEntriesPerBox = Integer.parseInt(new_value);
				javascript = "itemsPerPage = " + new_value + ";";
			} catch (NumberFormatException n) {
				// n.printStackTrace();
			}
		} else if (key.equalsIgnoreCase("systemmail")) {
			settings.systemMail = new_value;
		}
		settings.save();
		if (javascript.length() > 0) 
			CollaborateUpdate.update(javascript);
	}
}

