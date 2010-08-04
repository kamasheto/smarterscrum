package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Loading extends SmartController {
	public static void loading() {
		render("Application/loading.html");
	}
	
	/**
	 * This action handles all drops caused by dragging and dropping items on workspaces.
	 */
	public static void dynamicDrop(String from, String to) {
		String[] arr = from.split("-"); // meeting-1
		String[] arr2 = to.split("-"); // user-3
		
		from = arr[0].toLowerCase();
		to = arr2[0].toLowerCase();
		long id = Long.parseLong(arr[1]), id2 = Long.parseLong(arr2[1]);
		
		if (from.equals("user") && to.equals("component")) {
			// inviting user id to component id2
			Users.chooseUsers(id2, id);
		}
	}
}