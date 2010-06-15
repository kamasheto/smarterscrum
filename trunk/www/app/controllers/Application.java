package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import models.Component;
import models.Project;
import models.User;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.Mail;
import play.mvc.With;

/**
 * Class for static methods
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class Application extends SmartController {
	public static String hash(String str) {
		String res = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(str.getBytes());
			byte[] md5 = algorithm.digest();
			String tmp = "";
			for (int i = 0; i < md5.length; i++) {
				tmp = (Integer.toHexString(0xFF & md5[i]));
				if (tmp.length() == 1) {
					res += "0" + tmp;
				} else {
					res += tmp;
				}
			}
		} catch (NoSuchAlgorithmException ex) {
		}
		return res;
	}

	public static String randomHash() {
		return randomHash(32);
	}

	public static String randomHash(int length) {
		return hash(System.currentTimeMillis() * Math.random() + "").substring(0, length);
	}

	public static void index() {
		render();
	}

	/**
	 * View components controller which takes a projectID as an ID and returns
	 * the list of components to use it in the model view and it checks also if
	 * this component is in sprint or not
	 * 
	 * @author Amr Hany
	 * @param ProjectID
	 */
	public static void viewComponents(long id) {

		Project currentProject = Project.findById(id);
		boolean inSprint = (currentProject.inSprint(new Date()));
		String projectName = currentProject.name;
		List<Component> components = Component.find("byProject.idAnddeleted", id, false).fetch();

		render(components, id, projectName, inSprint);

	}

	/**
	 * View component controller is the method called to render the component
	 * and the sprint status to the view in order to use them
	 * 
	 * @author Amr Hany
	 * @param componentID
	 */
	public static void viewComponent(long id) {

		Component component = Component.findById(id);
		boolean inSprint = component.project.inSprint(new Date());
		render(component, inSprint);
	}

	/**
	 * This method is used to Delete a component by calling the model's method.
	 * 
	 * @author Amr Hany
	 * @param componentID
	 */

	@Check ("canDeleteComponent")
	public static void deleteComponent(long id) {
		Component c = Component.findById(id);
		c.deleteComponent();
		Logs.addLog(Security.getConnected(), "Delete", "Component", c.id, c.project, new Date(System.currentTimeMillis()));

	}

	public static void md5(String str) {
		renderText(hash(str));
	}

	@Check ("systemAdmin")
	public static void adminIndexPage() {
		render();
	}

	@Check ("systemAdmin")
	public static void adminIndex() {
		render();
	}

	/**
	 * View editable profile
	 * 
	 * @param id
	 *            user id
	 */
	@Check ("canEditProfile")
	public static void profile(long id) {
		if (id == 0) {
			id = Security.getConnected().id;
		}
		User user = User.findById(id);
		render(user);
	}

	/**
	 * Saves new user information
	 * 
	 * @param name
	 * @param pwd1
	 * @param pwd2
	 * @param email
	 * @param id
	 *            user id
	 */
	@Check ("canEditProfile")
	public static void editProfile(@Required (message = "You must enter a name") String name, String pwd1, String pwd2, @Required (message = "You must enter an email") @Email (message = "You must enter a valid email") String email, long id) {
		if (Validation.hasErrors() || (pwd1.length() > 0 && !pwd1.equals(pwd2))) {
			flash.error("An error has occured");
			profile(id);
		}

		User usr = User.findById(id);
		String oldEmail = usr.email;
		usr.name = name;
		if (pwd1.length() > 0)
			usr.pwdHash = Application.hash(pwd1);
		usr.email = email;
		usr.save();
		// Added By Wallas in Sprint 2.
		if (!usr.email.equals(oldEmail)) {
			usr.activationHash = Application.randomHash(32);
			usr.isActivated = false;
			usr.save();
			session.put("username", email); // Update the session cookie by
			// setting the new Email.
			String subject = "Your SmartSoft new Email activation requires your attention";
			String body = "Dear " + usr.name + ", You have requested to change the Email Address associated with your account. Please click the following link to activate your account: " + "http://localhost:9000/accounts/doActivation?hash=" + usr.activationHash;
			Mail.send("se.smartsoft@gmail.com", usr.email, subject, body);
			flash.success("Successfully saved your data! , please check your new Email and follow the instructions sent by us to confirm your new Email.");
			profile(id);
		} else {
			flash.success("Successfully saved your data!");
			profile(id);
		}

	}

}