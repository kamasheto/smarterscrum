package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@CRUD.For(User.class)
public class Dummy extends SmartCRUD {
	/**
	 * Dummy action that promotes the give user to admin
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

