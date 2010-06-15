package controllers;

import play.mvc.Before;

public class SmartCRUD extends CRUD {
	@Before
	public static void addDefaults() {
		renderArgs.put("connected", Security.getConnected());
	}
}
