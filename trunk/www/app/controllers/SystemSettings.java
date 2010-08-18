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
	public static void save(String key, String value) {
		System.out.println(key + " -> " + value);
	}
}

