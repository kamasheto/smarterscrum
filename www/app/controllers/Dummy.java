package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

/**
 * Dummy class for dummy actions
 * MUST BE REMOVED ON A PRODUCTION ENVIRONMENT
 * To access CRUD forms: http://url/admin/dummy
 * To access normal actions: http://url/dummy/action
 */
@CRUD.For(User.class)
public class Dummy extends SmartCRUD {
	/**
	 * Dummy action that promotes the give user to admin (or not)
	 * @param id user id to promote
	 * @param isAdmin whether to give this user admin permissions or not
	 * @deprecated use CRUD form instead
	 */
	public static void promote(long id, boolean isAdmin) {
		if (id == 0) {
			id = 1;
		}
		User user = User.findById(id);
		user.isAdmin = isAdmin;
		user.isActivated = true;
		user.save();
		Application.index();
	}
}

