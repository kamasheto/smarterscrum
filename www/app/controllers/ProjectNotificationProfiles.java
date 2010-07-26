package controllers;

import play.mvc.With;

@With (Secure.class)
public class ProjectNotificationProfiles extends SmartCRUD {
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
