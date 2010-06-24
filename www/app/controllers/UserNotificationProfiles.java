package controllers;

import play.mvc.With;

/**
 * This Controller class doesn't do anything and is just needed by CRUD for a
 * custom made view.
 * 
 * @author Amr Tj.Wallas
 * @see controllers.Users
 * @since Sprint2.
 * @Task C1S33
 */
@With (Secure.class)
public class UserNotificationProfiles extends SmartCRUD {
	public static void show() {
		forbidden();
	}

	public static void delete() {
		forbidden();
	}

	public static void blank() {
		forbidden();
	}

	public static void create() {
		forbidden();
	}

	public static void save() {
		forbidden();
	}

	public static void list() {
		forbidden();
	}
}
