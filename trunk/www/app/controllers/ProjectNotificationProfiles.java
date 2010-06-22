package controllers;

import play.mvc.With;

@With (Secure.class)
public class ProjectNotificationProfiles extends SmartCRUD {
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
