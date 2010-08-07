package controllers;

import play.mvc.With;

@With (Secure.class)
public class ProjectNotificationProfiles extends SmartCRUD {
	
	/**
	 * Overriding the CRUD method show and making it forbidden
	 */
	public static void show() {
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method delete and making it forbidden
	 */
	public static void delete() {
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method blank and making it forbidden
	 */
	public static void blank() {
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method create and making it forbidden
	 */
	public static void create() {
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method save and making it forbidden
	 */
	public static void save() {
		forbidden();
	}
	
	/**
	 * Overriding the CRUD method list and making it forbidden
	 */
	public static void list() {
		forbidden();
	}
}
