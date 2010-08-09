package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;

import notifiers.Notifications;

import models.Component;
import models.Project;
import models.User;
import models.UserNotificationProfile;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.libs.Mail;
import play.mvc.With;

/**
 * Represents the User Entity in the Database and it's relations with other entities.
 * 
 * @author Moataz Mekki
 * @author Amr Tj.Wallas
 */
@With (Secure.class)
public class Users extends SmartCRUD {
	/*
	 * public static boolean[] userExists(User user) { boolean [] toBeReturned =
	 * new boolean [] {false,false}; for(User currentUser :
	 * User.<User>findAll()) { if(currentUser.name == user.name) toBeReturned[0]
	 * = true; if(currentUser.email == user.email) toBeReturned[1] = true; }
	 * return toBeReturned; }
	 */// Commented out by Wallas

	/**
	 * Takes the component id as an input and renders to the html page a list of users in the project
	 * & the component id it to redirect back to the component when the actions are done.
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *         The component id.
	 * @return void
	 */
	public static void assignUsers(long id) {
		Component comp = Component.findById(id);
		Project pro = comp.project;
		Security.check(pro, "editComponent");
		List<User> users = getFreeUsers(pro);
		render(users, comp, pro);
	}

	/**
	 * Takes a project and returns a list of the developers in that project who are not assigned in any component yet
	 * 
	 * @author Moataz_Mekki
	 * @param p
	 *         The project that we need to get the developers in it.
	 * @return List<User> 
	 *             list of the developers that are not assigned in any component yet.
	 */
	public static List<User> getFreeUsers(Project project) {
		List<User> users = project.users;
		List<Component> projectComponents = project.components;
		ArrayList<User> res = new ArrayList<User>();
		for (int i = 0; i < users.size(); i++) {
			User tmp = users.get(i);
			for (int j = 0; j < projectComponents.size(); j++) {
				Component com = projectComponents.get(j);
				if (com.componentUsers.contains(tmp))
					break;
				else if (j == projectComponents.size() - 1)
					res.add(tmp);
			}

		}
		return res;
	}

	/**
	 * Adds the relation between the user & the component 
	 * to make sure that this user is assigned to that component
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            component id.
	 * @param UId
	 *            user id.
	 * @return void
	 */
	public static void chooseUsers(long id, long UId) {
		User myUser = User.findById(UId);
		Component myComponent = Component.findById(id);
		Security.check(myComponent.project, "assignUserToComponent");
		if (myUser.components.contains(myComponent)) {
			renderText("Cannot assign user to a component he is already a member in");
		}
		myUser.components.add(myComponent);
		myComponent.componentUsers.add(myUser);
		for(Component component : myComponent.project.components){
			if(component.number==0 && myUser.components.contains(component)){
				myUser.components.remove(component);
				component.componentUsers.remove(myUser);
				System.out.println("here");
			}
		}
		Date d = new Date();
		// User user = User.find("byEmail", Security.connected()).first();
		User user = Security.getConnected();
		Logs.addLog(user, "assignUser", "User", UId, myComponent.project, d);
		//Notifications.notifyUsers(myUser, "Assigned to a component", "You were assigned to the component " + myComponent.name + " in the project " + myComponent.project.name, (byte) 0);
		myUser.save();
		renderText("User assigned to component successfully|reload('component-" + id + "')");
	}

	/**
	 * Deletes a user from the system, and redirects to the manage admin page.
	 * 
	 * @author mahmoudsakr
	 * @param id
	 *          user id.
	 * @param fromACP
	 *                 
	 * @return void
	 */
	public static void del(long id, boolean fromACP) {
		Security.check(Security.getConnected().isAdmin);
		User user = User.findById(id);
		user.deleted = true;
		user.save();
		redirect(fromACP ? "/admin/users" : "/show/users");
		// redirect( flash.get( "url" ) );
	}

	/**
	 * Fetches and renders the corresponding UserNotificationProfile.
	 * 
	 * @author Amr Tj.Wallas
	 * @param id
	 *            The id of that project the user wants to manage his
	 *            notifications in.
	 * @throws ClassNotFoundException
	 * @see {@link models.UserNotificationProfile}
	 * @see {@link views/Users/manageNotificationProfile.html}
	 * @return void
	 */
	public static void manageNotificationProfile(long id) throws ClassNotFoundException {
		Project currentProject = Project.findById(id);
		User currentUser = Security.getConnected();
		// Security.check(currentUser.in(currentProject).can("editUserNotificationProfile"));
		UserNotificationProfile currentNotificationProfile = UserNotificationProfile.find("user = " + currentUser.id + " and project = " + currentProject.id).first();
		ObjectType type = ObjectType.get(UserNotificationProfiles.class);
		notFoundIfNull(type);
		Security.check(currentUser.projects.contains(currentProject));
		if (currentNotificationProfile == null) {
			// create me a notification profile please
			currentNotificationProfile = new UserNotificationProfile(currentUser, currentProject).save();
		}
		JPASupport object = type.findById(currentNotificationProfile.id);
		try {

			render(currentNotificationProfile, type, object);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	/**
	 * Saves any modifications made by the user in a given UserNotificationProfile in the 
	 * UI Side to the database. And renders a success message.
	 * 
	 * @author Amr Tj.Wallas
	 * @param id
	 *            The id of that project the user is managing his notifications
	 *            in.
	 * @throws Exception
	 * @see {@link models.UserNotificationProfile}
	 * @see {@link views/Users/manageNotificationProfile.html}
	 * @return void
	 */
	public static void saveNotificationProfile(String id) throws Exception {
		ObjectType type = ObjectType.get(UserNotificationProfiles.class);
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Security.check(((UserNotificationProfile) object).user == Security.getConnected());
		validation.valid(object.edit("object", params));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/show.html", type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
		object.save();
		flash.success("You Notificaton Profile modifications have been saved");
		if (params.get("_save") != null) {
			redirect("/users/managenotificationprofile?projectId=" + id);
		}
		redirect(request.controller + ".show", object.getEntityId());
	}
	
	/**
	 * Overrides the CRUD show method that renders the edit form..
	 * 
	 * @param id
	 *          The user id.
	 * @return void
	 **/
	public static void show(String id) {
		Security.check(Security.getConnected().isAdmin);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		try {
			render(type, object);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	/**
	 * Overrides the CRUD save method that is invoked to submit the edit, in
	 * order to check if the edits are acceptable.
	 * 
	 * @param id
	 *         The user id.
	 * @throws Exception
	 * @return void
	 **/
	public static void save(String id) throws Exception {
		Security.check(Security.getConnected().isAdmin);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		object = object.edit("object", params);
		// Look if we need to deserialize
		for (ObjectType.ObjectField field : type.getFields()) {
			if (field.type.equals("serializedText") && params.get("object." + field.name) != null) {
				Field f = object.getClass().getDeclaredField(field.name);
				//f.set(object, CRUD.collectionDeserializer(params.get("object." + field.name), (Class) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]));
			}
		}

		validation.valid(object);
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/show.html", type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
		object.save();
		flash.success(Messages.get("crud.saved", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * Overrides the CRUD blank method that renders the create form to create a user.
	 * 
	 * @param void
	 * @return void
	 */
	public static void blank() {
		Security.check(Security.getConnected().isAdmin);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		try {
			render(type);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}
	}

	/**
	 * Overrides the CRUD create method that is invoked to submit the creation
	 * of the user on the database.
	 * 
	 * @param void
	 * @throws Exception
	 * @return void
	 */
	public static void create() throws Exception {
		Security.check(Security.getConnected().isAdmin);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.entityClass.newInstance();
		validation.valid(object.edit("object", params));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/blank.html", type);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}
		((User) object).isActivated = true;
		object.save();
		flash.success(Messages.get("crud.created", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * Overrides the CRUD list method that lists the users on a project.
	 * 
	 * @param page
	 *           
	 * @param search
	 *             
	 * @param searchFields
	 *                   
	 * @param orderBy
	 *              
	 * @param order
	 *            
	 * @return void
	 */
	public static void list(int page, String search, String searchFields, String orderBy, String order) {
		Security.check(Security.getConnected().isAdmin);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		List<JPASupport> objects = type.findPage(page, search, searchFields, orderBy, order, (String) request.args.get("where"));
		Long count = type.count(search, searchFields, (String) request.args.get("where"));
		Long totalCount = type.count(null, null, (String) request.args.get("where"));
		try {
			render(type, objects, count, totalCount, page, orderBy, order);
		} catch (TemplateNotFoundException e) {
			render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
		}
	}
	
	/**
	 * Redirects to a page that indicates that the user dosen't have permission.
	 * 
	 * @param void
	 * @return void
	 */
	public static void delete() {
		forbidden();
	}
	
	/**
	 * Renders to the html page user(s) and the title of the list and the current project
	 * (indicating which project work space the user is using), in oder to list the users per project or per component or list the user's mini profile details.
	 * 
	 * @param projectId
	 *                the current project id.
	 * @param componentId
	 *                  the component id.
	 * @param all
	 *          an int value that indicates whether the list of the users is per project or per component.
	 * @param userId
	 *             the user id.
	 * @author Monayri, Heba Elsherif
	 * @return void
	 */
	public static void findUsers(long projectId, long componentId, int all, long userId)
	{
		String title;
		if(userId!=0)
		{
			Project currentProject = Project.findById(projectId);
			User user = User.findById(userId);
			title= user.name;
			render(user, title, currentProject);
		}
		else
		{
			Project currentProject = Project.findById(projectId);
			List<User> users = new ArrayList<User>();
			if(all == 1)
			{
				users= User.find("byDeleted", false).fetch();
				title = "All Users";
				render(users, title, currentProject);
			}
			else
			{
				if(componentId !=0)
				{
					Component component = Component.findById(componentId);
					currentProject = component.project;
					for(User user: component.componentUsers){
						if(!user.deleted){
							users.add(user);
						}
					}
					title = "C"+ component.number+ ": Users";
					render(users, title, currentProject);
				}
				else
				{
					for(User user: currentProject.users){
						if(!user.deleted){
							users.add(user);
						}
					}
					title= "Project Users";
					render(users, title, currentProject);
				}
			}
		}
	}
	

	/**
	 * Renders to the html page user(s) and the user id and title of the list and the box id and the current project,
	 *  in oder to list the user's mini profile details.
	 * 
	 * @param userId
	 *             the user id.
	 * @param boxId
	 *            the box id (indicates which box or list these data is rendered to).
	 * @param projectId
	 *                a project id (one of the projects that the user is a member of).
	 * @param currentProjectId
	 *          the project work space that the user is using.
	 
	 * @author Heba Elsherif
	 * @return void
	 */
	public static void listUserProjects(long userId, int boxId, long projectId, long currentProjectId)
	{
		String title;
		if(boxId==1)
		{
			Project currentProject = Project.findById(currentProjectId);
			User user = User.findById(userId);
			title= user.name+"'s Projects";
			render(user, title, boxId, currentProject);
		}
		if(boxId==2)
		{
			Project currentProject = Project.findById(currentProjectId);
			User user = User.findById(userId);
			Project project = Project.findById(projectId);
			title= user.name+"'s Roles in Project: " + project.name;
			render(user, title, boxId, project, currentProject);
		}
		
	}
	
	/**
	 * Renders the user who's profile is being edited and the connected user to the html page that displays the edit form with the user mini profile data.
	 * 
	 * @param userProfileId
	 *                    the use id that his mini profile is being edited.
	 * @return void
	 */
	public static void editMiniProfile ( long userProfileId)
	{
		User userProfile = User.findById(userProfileId);
		User connectedUser = User.findById(Security.getConnected().id);
		if (connectedUser.deleted)
			notFound();
		if (userProfile.deleted)
			notFound();
		if ((userProfile.id == connectedUser.id)||(connectedUser.isAdmin))
		{
			render(userProfile, connectedUser);
		}
		else
		{
			flash.error( "Sorry, You cannot edit these personal informations." );
		}
	}
	
	/**
	 * checks if the edits done to the user mini profile is aceptable and submits it 
	 * and save it to the data base and returns a sucess message.
	 * 
	 * @param name
	 *           the new user name.
	 * @param email
	 *            the new user email.
	 * 
	 * @param userProfileId
	 *                    the use id that his profile is being edited.
	 * @return void
	 */
	public static void miniProfileAction ( @Required(message = "You must enter a name") String name,
			@Required(message = "You must enter an email") @Email(message = "You must enter a valid email") String email,
			long userProfileId)
	{
		User userProfile = User.findById(userProfileId);
		User connectedUser = Security.getConnected();
		if (connectedUser.deleted)
			notFound();
		if (userProfile.deleted)
			notFound();
		if ((userProfile.id == connectedUser.id)||(connectedUser.isAdmin))
		{
			if (Validation.hasErrors()) 
			{
				editMiniProfile(userProfileId);
			}
			String oldEmail = userProfile.email;
			String oldname = userProfile.name;
			userProfile.name = name;
			userProfile.email = email;
			boolean hasErrors = false;
			String message = "";
			if (!userProfile.email.equals(oldEmail)) 
			{
				userProfile.activationHash = Application.randomHash(32);
				userProfile.isActivated = false;
				session.put("username", email);
				String emailSubject = "Your SmartSoft new Email activation requires your attention";
				String emailBody = "Dear "
						+ userProfile.name
						+ ", The Email Address associated with your account has been requested to be changed. Please click the following link to activate your account: "
						+ "http://localhost:9000/accounts/doActivation?hash="
						+ userProfile.activationHash;
				Mail.send("se.smartsoft@gmail.com", userProfile.email, emailSubject, emailBody);
				message = "You have successfully edited user personal information, A confirmation email has been sent to the new Email.";
			} 
			else 
			{
				message = "You have successfully edited user personal information.";
			}
			try 
			{
				userProfile.save();
				flash.success(message);
				if (!oldname.equals(name))
				{
					Application.overlayKiller("reload('users')", "window.parent.$('#username-in-topbar').html('"+name+"')");
				}
				else
				{
					Application.overlayKiller("reload('user-'+userProfileId)", "");
				}
				
			} 
			catch (PersistenceException e) 
			{
				List<User> usersWithSameName = User.find("byName", name).fetch();
				List<User> usersWithSameEmail = User.find("byEmail", email).fetch();
				message = "";
				if(usersWithSameName.isEmpty())
				{
					message = "Sorry, The user name is already used. Please enter another name. ";
					
				}
				else if(usersWithSameEmail.isEmpty())
				{
					message = message +"The email is already used. Please enter another email.";
				}
				flash.success(message);
				editMiniProfile (userProfileId);
			}
		}
		else
		{
			Application.overlayKiller("","");
			flash.error("You are not allowed to edit these personal information.");
		}	
	}
}