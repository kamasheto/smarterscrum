package controllers;

import play.mvc.Before;
import play.mvc.Controller;

public class SmartController extends Controller {
	@Before
	public static void addDefaults() {
		renderArgs.put("connected", Security.getConnected());
	}
}
