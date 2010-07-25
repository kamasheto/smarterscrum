package controllers;

import models.Project;
import models.User;

/**
 * Security class/controller.. handles all security checks/permissions
 * 
 * @author mahmoudsakr
 */
public class Security extends Secure.Security {

	public static User getConnected() {
		String usr = (isConnected() ? connected() : "").toLowerCase();
		return User.find("select u from User u where u.email=? or u.name=?", usr, usr).first();
	}

	/**
	 * returns whether or not this email/pwd form a valid combination
	 * 
	 * @param email
	 *            user email
	 * @param password
	 *            user password
	 * @return true if such a user exist, false otherwise
	 */
	public static boolean authentify(String email, String password) {
		User user = User.find("select u from User u where (u.email=? or u.name=?) and u.pwdHash = ?", email.toLowerCase(), email.toLowerCase(), Application.hash(password)).first();
		/* By Tj.Wallas_ in Sprint2 */
		if (user != null && !user.isActivated) {
			flash.error("Your account is not activated, please follow the instructions in the Email we sent you to activate your account");
			try {
				Secure.login();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if ( user != null && user.deleted) {
			flash.error("Your account has been deleted. Please contact a website administrator for further information.");
			try {
				Secure.login();
			} catch(Throwable e) {
				e.printStackTrace();
			}
		}

		return user != null;
	}

	/**
	 * checks whether the boolean variable gives the user access to view the
	 * page or not
	 * 
	 * @param can
	 *            if false, user gets an access denied page
	 * @return false
	 */
	public static boolean check(boolean can) {
		if (!can) {
			forbidden();
		}
		return false;
	}

	/**
	 * checks whether the connected user could perform the action specified by
	 * permission in the given project
	 * 
	 * @param project
	 * @param permission
	 * @return false
	 */
	public static boolean check(Project project, String permission) {
		return check(getConnected().in(project).can(permission));
	}
}