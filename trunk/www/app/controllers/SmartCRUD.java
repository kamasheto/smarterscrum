package controllers;

import play.mvc.Before;

public class SmartCRUD extends CRUD {
	@Before
	public static void beforeCRUDActions() throws Throwable {
		SmartController.beforeActions();
	}
}
