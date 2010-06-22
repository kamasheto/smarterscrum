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
	public static void show(String id) {
		forbidden();
	}

	public static void delete(String id) {
		forbidden();
	}

	public static void blank() {
		forbidden();
	}

	public static void create() {
		forbidden();
	}

	public static void save(String id) {
		forbidden();
	}

	public static void list(int page, String search, String searchFields, String orderBy, String order) {
		forbidden();
	}
}
