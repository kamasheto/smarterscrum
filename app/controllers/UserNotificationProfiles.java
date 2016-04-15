package controllers;

import play.mvc.With;

/**
 * This Controller class doesn't do anything and is just needed by CRUD for a
 * custom made view.
 * 
 * @author Amr Tj.Wallas
 * @see controllers.Users
 */
@With (Secure.class)
public class UserNotificationProfiles extends SmartCRUD {
	
	/**
	 * Overrides the CRUD show method, in order to make the crud view not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void show() {
		forbidden();
	}

	/**
	 * Overrides the CRUD delete method, in order to make the crud delete action not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void delete() {
		forbidden();
	}

	/**
	 * Overrides the CRUD blank method, in order to make the crud view not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void blank() {
		forbidden();
	}

	/**
	 * Overrides the CRUD create method, in order to make the crud create action not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void create() {
		forbidden();
	}

	/**
	 * Overrides the CRUD save method, in order to make the crud edit action not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void save() {
		forbidden();
	}

	/**
	 * Overrides the CRUD list method, in order to make the crud view not accessable.
	 * 
	 * @param void
	 * @return void
	 */
	public static void list() {
		forbidden();
	}
}
