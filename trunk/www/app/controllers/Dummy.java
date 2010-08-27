package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Dummy extends SmartController {
	/**
	 * Dummy action that promotes the give user to admin
	 */
	public static void promote(long id) {
		if (id == 0) {
			id = 1;
		}
		User user = User.findById(id);
		user.isAdmin = true;
		user.isActivated = true;
		user.save();
	}
}

