package controllers;

import javax.persistence.PersistenceException;

import models.User;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Mail;

/**
 * @author amr_osman
 * @version 684
 * @Task C1S23
 * @Task C1S24
 */
public class Accounts extends SmartController {
	/**
	 * This method simply takes the required parameters of a User record ,
	 * creates that user object and saves it into the database after checking
	 * the persistence constraints such as required and unique parameters. On
	 * Detection of a persistence constraint violation, a PersistenceException
	 * is fired by java and then caught in this method. Which in return, creates
	 * a play validation error and flashes it to be displayed in the
	 * corresponding view.
	 * 
	 * @param name
	 *            user name of that new user.
	 * @param email
	 *            email of that new user.
	 * @param password
	 *            password of that new user.
	 * @param confirmPass
	 *            password confirmation of that new user.
	 * @exception PersistenceException
	 *                fired on database constraints violations.
	 * @see models.User
	 * @see views.Accounts.addUser
	 * @see views.Accounts.register
	 * @Task C1S24
	 */
	public static void addUser(@Required String name, @Required @Email String email, @Required String password, @Required String confirmPass) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			register();
		} else if (!password.equals(confirmPass)) {
			flash.error("Your passwords do not match");
			validation.keep();
			register();
		} else {
			try {
				User existingUser = User.find("name like '" + name + "' or " + "email like '" + email + "'").first();
				if (existingUser != null) {
					flash.error("Oops, that user already exists!" + "\t" + "Please choose another user name and/or email.");
					register();
				}
				User user = new User(name, email, password);
				// System.out.println( user );
				user.save();
				// render( user.name, user.email, password );
				String subject = "Your SmartSoft account activation";
				String body = "Dear " + name + ", We are glad to have you as a registered user. Please click the following link to activate your account: " + "http://localhost:9000/accounts/doActivation?hash=" + user.activationHash;
				Mail.send("se.smartsoft@gmail.com", user.email, subject, body);
				flash.success("You have been registered. An Activation link has been sent to your Email Address");
				Secure.login();
			} /*
			 * catch (PersistenceException e) {
			 * flash.error("Oops, that user already exists!" + "\t" +
			 * "Please choose another user name and/or email."); register(); }
			 */catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * This method is just an empty controller response to the register view and
	 * it does nothing but rendering the elements in the register view.
	 * 
	 * @see views.Accounts.register
	 * @Task C1S24
	 */
	public static void register() {
		render();
	}

	/**
	 * This method is just an empty controller response to the requestDeletion
	 * view and it does nothing but rendering the elements in that view.
	 * 
	 * @see views.Accounts.requestDeletion
	 * @Task C1S23
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
	 * This method simply takes a user name, Finds that user name in the
	 * database and marks him as he requested to be deleted from the system by
	 * setting his pendingDeletion Attribute to true. In that case, his deletion
	 * request is pending approval form the system admin and an appropriate
	 * notification message is sent to the corresponding view to inform the user
	 * of his action.
	 * 
	 * @param userName
	 *            Name of That user
	 * @see models.User
	 * @see views.Accounts.requestDeletion
	 * @Task C1S23
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
	 * This method activates a user with an activation hash "hash" when that
	 * user clicks the activation link sent to him.
	 * 
	 * @param hash
	 *            The Activation hash value of that user.
	 * @throws Throwable
	 *             Any exception that might happen during the login process is
	 *             thrown here as well.
	 * @see models.User
	 * @since Sprint2.
	 * @Task C1S30
	 */
	public static void doActivation(String hash) throws Throwable {
		User currentUser = User.find("activationHash", hash).first();
		if (currentUser != null && !currentUser.isActivated) {
			currentUser.isActivated = true;
			currentUser.save();
			flash.success("Thank you , your Account has been Activated! . Login Below");
		} else
			flash.error("This activation link is not valid or has expired. Activation Failed!");
		Secure.login();
	}

	public static void undoRequest() {
		Security.check(Security.isConnected());
		User user = Security.getConnected();
		user.pendingDeletion = false;
		user.save();
		flash.success("Your deletion request has been successfully undone !");
		redirect("/");
	}

}
