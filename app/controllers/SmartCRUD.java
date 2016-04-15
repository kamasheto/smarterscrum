package controllers;

import play.mvc.Before;

public class SmartCRUD extends CRUD
{

	/**
	 * making actions before any action called in any controllers
	 * 
	 * @throws Throwable
	 */
	@Before
	public static void beforeCRUDActions() throws Throwable
	{
		SmartController.beforeActions();
	}
}
