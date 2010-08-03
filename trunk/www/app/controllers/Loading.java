package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Loading extends SmartController {
	public static void loading() {
		render("Application/loading.html");
	}
}

