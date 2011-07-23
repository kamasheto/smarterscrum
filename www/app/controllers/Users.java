package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.PersistenceException;

import org.apache.commons.io.IOUtils;

import notifiers.Notifications;
import models.Component;
import models.Project;
import models.Reviewer;
import models.Task;
import models.CollaborateUpdate;
import models.User;
import models.UserNotificationProfile;
import models.Log;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.libs.Mail;
import play.mvc.Router;
import play.mvc.With;

/**
 * Represents the User Entity in the Database and it's relations with other entities.
 * 
 * @author Moataz Mekki
 * @author Amr Tj.Wallas
 */
@With (Secure.class)
public class Users extends SmartCRUD {
	/**
	 * Assigns a user to a component
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            component id.
	 * @param user_id
	 *            user id.
	 * @return void
	 */
	public static void choose_users(long id, long user_id) {
		User myUser = User.findById(user_id);
		Component myComponent = Component.findById(id);
		Security.check(myComponent.project, "assignUserToComponent");
		if (myUser.components.contains(myComponent)) {
			renderText("Cannot assign user to a component he is already a member in");
		}
		myUser.components.add(myComponent);
		for(Component component : myComponent.project.components){
						if(component.number==0 && myUser.components.contains(component)){
							myUser.components.remove(component);
						component.componentUsers.remove(myUser);
						}
						}
		myComponent.componentUsers.add(myUser);
		// Logs.addLog(user, "assignUser", "User", user_id, myComponent.project,
		 // d);
		Log.addUserLog("Assign user to component", myComponent, myComponent.project, myUser);
		String url = Router.getFullUrl("Application.externalOpen")+"?id="+myComponent.project.id+"&isOverlay=false&url=/components/viewthecomponent?component_id="+myComponent.id;
		for(User u :myComponent.componentUsers)
		{
			if(!u.equals(myUser))
				Notifications.notifyUser(u, "assigned", url, myUser.name+" to the component", myComponent.name, (byte) 0, myComponent.project);
		}
		Notifications.notifyUser(myUser, "assigned", url, "you to the component", myComponent.name, (byte) 1, myComponent.project);
		myUser.save();
		myComponent.save();
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
	public static void manage_notification_profile(long id) throws ClassNotFoundException {
		Project currentProject = Project.findById(id);
		User currentUser = Security.getConnected();
		// Security.check(currentUser.in(currentProject).can("editUserNotificationProfile"));
		UserNotificationProfile currentNotificationProfile = UserNotificationProfile.find("user = ? and project = ?", currentUser, currentProject).first();
		ObjectType type = ObjectType.get(UserNotificationProfiles.class);
		notFoundIfNull(type);
		Security.check(currentUser.projects.contains(currentProject));
		// if (currentNotificationProfile == null) {
		// 			// create me a notification profile please
		// 			currentNotificationProfile = new UserNotificationProfile(currentUser, currentProject).save();
		// 		}
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
	public static void save_notification_profile(long id) throws Exception {
		ObjectType type = ObjectType.get(UserNotificationProfiles.class);
		notFoundIfNull(type);
		// NA3AM!! TYPE.FINDBYID EZAY YA3NI!
		JPASupport object = UserNotificationProfile.findById(id);
		Security.check(object != null);
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
		flash.success("Your Notificaton Profile modifications have been saved");
		Application.overlayKiller( "", "" );
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
	public static void list(int page, String search, String search_fields, String order_by, String order) {
		Security.check(Security.getConnected().isAdmin);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		List<JPASupport> objects = type.findPage(page, search, search_fields, order_by, order, (String) request.args.get("where"));
		Long count = type.count(search, search_fields, (String) request.args.get("where"));
		Long totalCount = type.count(null, null, (String) request.args.get("where"));
		try {
			render(type, objects, count, totalCount, page, order_by, order);
		} catch (TemplateNotFoundException e) {
			render("CRUD/list.html", type, objects, count, totalCount, page, order_by, order);
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
	 * @param component_id
	 *                  the component id.
	 * @param all
	 *          an int value that indicates whether the list of the users is per project or per component.
	 * @param userId
	 *             the user id.
	 * @author Monayri, Heba Elsherif
	 * @return void
	 */
	public static void find_users(long project_id, long component_id, int all, long user_id)
	{	
		String title;
		if(user_id!=0)
		{
			Project currentProject = Project.findById(project_id);
			User user = User.findById(user_id);
			title= user.name;
			render(user, title, currentProject);
		}
		else
		{
			Project currentProject = Project.findById(project_id);
			List<User> users = new ArrayList<User>();
			if(all == 1)
			{
				users= User.find("byDeleted", false).fetch();
				title = "All Users";
				render(users, title, currentProject);
			}
			else
			{
				if(component_id !=0)
				{
					Component component = Component.findById(component_id);
					currentProject = component.project;
					for(User user: component.componentUsers){
						System.out.println(user.name);
						if(!user.deleted){
							
							users.add(user);
						}
					}
					title = "C"+ component.number+ ": Users";
					render(users, title, currentProject);
					return;
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
	public static void list_user_projects(long userId, int boxId, long projectId, long currentProjectId)
	{
		String title = "";
		User user = User.findById(userId);
		Project currentProject = Project.findById(currentProjectId);		
		if(boxId==1)
		{
			title= user.name+"'s Projects";
			render(user, title, boxId, currentProject);
		}
		if(boxId==2)
		{
			Project project = Project.findById(projectId);
			List<Reviewer> revroles = Reviewer.find("byUserAndProjectAndAccepted", user, project, true).fetch();
			title= user.name+"'s Roles in Project: " + project.name;
			render(user, title, boxId, project, currentProject, revroles);
		}
		
	}
	
	/**
	 * Renders the user who's profile is being edited and the connected user to the html page that displays the edit form with the user mini profile data.
	 * 
	 * @param userProfileId
	 *                    the use id that his mini profile is being edited.
	 * @return void
	 */
	public static void edit_mini_profile ( long userProfileId)
	{
		User userProfile = User.findById(userProfileId);
		User connectedUser = Security.getConnected();
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
	public static void mini_profile_action ( @Required(message = "You must enter a name") String name,
			@Required(message = "You must enter an email") @Email(message = "You must enter a valid email") String email,String mobile,
			long userProfileId, File file) throws IOException, Throwable {
		User userProfile = User.findById(userProfileId);
		User connectedUser = Security.getConnected();
		boolean isNumber = false;
		long mob=0;
		if (connectedUser.deleted)
			notFound();
		if (userProfile.deleted)
			notFound();
		if (userProfile == connectedUser || connectedUser.isAdmin) {
			if (Validation.hasErrors()) {
				for (Error error : Validation.errors()) { 
					flash.error(error.message());
		        }
				edit_mini_profile(userProfileId);
			}
			try{
				mob= Integer.parseInt(mobile);
				isNumber= true;
			}catch(Exception e){
				flash.error("You must enter a valid mobile number");
				edit_mini_profile(userProfileId);

			}
			String oldEmail = userProfile.email;
			String oldName = userProfile.name;
			//long oldMobile = userProfile.mobileNumber;
			userProfile.name = name;
			userProfile.email = email;
			if(isNumber)
			userProfile.mobileNumber= mob;
			userProfile.save();
			String message = "";
			try {
				message = "You have successfully edited user personal information.";
				if (file != null) {
					FileInputStream avatar = new FileInputStream(file);
					String url = "/public/Avatars/" + userProfileId + "_" + file.getName();
					IOUtils.copy(avatar, new FileOutputStream(Play.getFile(url)));
					userProfile.avatar = url;
					userProfile.save();
				}
				if (!userProfile.email.equals(oldEmail)) {
					userProfile.activationHash = Application.randomHash(32);
					userProfile.isActivated = false;
					userProfile.save();
					Notifications.activate(userProfile.email, userProfile.name, Router.getFullUrl("Accounts.doActivation")+"?hash=" + userProfile.activationHash, true);
				}
				
				if (!oldName.equals(name)) {
					CollaborateUpdate.update(userProfile, "$('#username-in-topbar').html('"+name+"')");
				}
			
				for (Project project : userProfile.projects) {
					CollaborateUpdate.update(project, "reload('users', 'user-"+userProfileId+"')");
				}
				
				flash.success(message);
				Application.overlayKiller("", "");
			} 
			catch (PersistenceException e) 
			{
				List<User> usersWithSameName = User.find("byName", name).fetch();
				List<User> usersWithSameEmail = User.find("byEmail", email).fetch();
				message = "";
				if(!usersWithSameName.isEmpty())
				{
					message = "Sorry, The user name is already used. Please enter another name. ";
				}
				else if(!usersWithSameEmail.isEmpty())
				{
					message = message +"The email is already used. Please enter another email.";
				}
				flash.error(message);
				edit_mini_profile (userProfileId);
			}
		} 	else {	
			flash.error("You are not allowed to edit these personal information.");
			Application.overlayKiller("","");
		}	
	}
}