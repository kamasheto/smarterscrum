package controllers;

import javax.persistence.PersistenceException;

import notifiers.Notifications;

import models.User;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.Router;

/**
 * Handles all of the actions related to Accounts
 * 
 * @author Amr Tj.Wallas
 */
public class Accounts extends SmartController {
	/**
	 * Creates a new user
	 * 
	 * @param name
	 *            , user name of that new user.
	 * @param email
	 *            , email of that new user.
	 * @param password
	 *            , password of that new user.
	 * @param confirmPass
	 *            , password confirmation of that new user.
	 * @exception PersistenceException
	 *                , fired on database constraints violations.
	 */
	public static void addUser(@Required String name,
			@Required @Email String email, @Required String password,
			@Required String confirmPass, String mobile) {
		boolean there = false;
		long mob = 0;
		System.out.println("at first " + mobile);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			register();
		} else if (!password.equals(confirmPass)) {
			flash.error("Your passwords do not match");
			validation.keep();
			register();
		} else {
			if (name.length() < 5) {
				flash.error("Your username is too short");
				validation.keep();
				register();
			} else {
				if (password.length() < 5) {
					flash.error("Your password is too short");
					validation.keep();
					register();
				} else {

					
						try {
							User existingUser = User.find(
									"name like '" + name + "' or "
											+ "email like '" + email + "'")
									.first();
							if (existingUser != null) {
								flash.error("Oops, that user already exists!"
										+ "\t"
										+ "Please choose another user name and/or email.");
								register();
							}
							if (mobile.length() != 0) {
								there = true;
								try {
									mob = Integer.parseInt(mobile);
									String temp0 =""+ mobile.charAt(0);
									String temp1 =""+ mobile.charAt(1);
									if (Integer.parseInt(temp0) != 0 || Integer.parseInt(temp1) != 1 || mobile.length()<10 || mobile.length()>11) {
										flash.error("Please enter a valid mobile number ");
										validation.keep();
										register();
									}
								} catch (Exception e) {
									flash.error("Please enter a valid mobile number");
									validation.keep();
									register();
								}
							}
							User user = new User(name, email, password);
							if (there) {
								user.mobileNumber = mob;
							}
							user.save();
							System.out.println("mob num " + user.mobileNumber);
							String url = Router
									.getFullUrl("Accounts.doActivation")
									+ "?hash="
									+ user.activationHash
									+ "&firstTime=true";
							Notifications.activate(user.email, user.name, url,
									false);
							flash.success("You have been registered. An Activation link has been sent to your Email Address");
							Secure.login();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					
				}
			}
		}
	}

	/**
	 * Renders the register page
	 */
	public static void register() {
		render();
	}

	/**
	 * Renders the deletion request view
	 */
	public static void requestDeletion() {
		if (!Security.isConnected()) {
			Security.error("You are not registered, Please login if you haven't done so");

		} else if (Security.getConnected().pendingDeletion) {
			User user = Security.getConnected();
			render(user);
		}
		render();
	}

	/**
	 * Requests the deletion of the currently connected user.
	 * 
	 * @param pwd
	 *            , confirmation password
	 */

	public static void deletionRequest(@Required String pwd) {
		Security.check(Security.isConnected());
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			requestDeletion();
		} else {
			User userFound = Security.getConnected();
			String pwdHash = Application.hash(pwd);
			if (!userFound.pwdHash.equals(pwdHash)) {
				flash.error("You have entered a wrong password!");
				requestDeletion();
			} else {
				userFound.pendingDeletion = true;
				userFound.save();
				flash.success("your deletion request has been successfully sent!");
				redirect("/");
			}
		}

	}

	/**
	 * Activates a user with an activation hash "hash"
	 * 
	 * @param hash
	 *            ,The Activation hash value of that user.
	 * @throws Throwable
	 *             ,Any exception that might happen during the login process is
	 *             thrown here as well.
	 * @since Sprint2.
	 */
	public static void doActivation(String hash, boolean firstTime)
			throws Throwable {
		User currentUser = User.find("activationHash", hash).first();
		if (currentUser != null && !currentUser.isActivated) {
			currentUser.isActivated = true;
			currentUser.save();
			Notifications.welcome(currentUser, firstTime);
			flash.success("Thank you , your Account has been Activated! . Login Below");
		} else
			flash.error("This activation link is not valid or has expired. Activation Failed!");
		Secure.login();
	}

	/**
	 * Undoes the deletion request of current connected user.
	 * 
	 * @since Sprint3
	 */
	public static void undoRequest() {
		Security.check(Security.isConnected());
		User user = Security.getConnected();
		user.pendingDeletion = false;
		user.save();
		flash.success("Your deletion request has been successfully undone !");
		redirect("/");
	}

}
