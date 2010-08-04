package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import notifiers.Notifications;

import models.Component;
import models.Project;
import models.User;
import models.UserNotificationProfile;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

/**
 * @author Moataz Mekki
 * @author Amr Tj.Wallas
 * @version 685
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
	 * belongs to s16 this method renders the users in the project to assign
	 * them to components
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            this method takes the component id as an input this method
	 *            renders to the html page a list of users & the component id it
	 *            renders the component id just to redirect back to the
	 *            component id when the actions are done
	 */

	// @Check ("canEditComponent")
	public static void assignUsers(long id) {
		Component comp = Component.findById(id);
		Project pro = comp.project;
		Security.check(pro, "editComponent");
		List<User> users = getFreeUsers(pro);
		render(users, comp, pro);
	}

	/**
	 * it's a helper method
	 * 
	 * @author Moataz_Mekki
	 * @param p
	 *            the project that we need to get the developers in it
	 * @return it returns a list of the developers that are not assigned in any
	 *         component yet.
	 */
	public static List<User> getFreeUsers(Project p) {
		List<User> users = p.users;
		List<Component> comp = p.components;
		ArrayList<User> res = new ArrayList<User>();
		for (int i = 0; i < users.size(); i++) {
			User tmp = users.get(i);
			for (int j = 0; j < comp.size(); j++) {
				Component com = comp.get(j);
				if (com.componentUsers.contains(tmp))
					break;
				else if (j == comp.size() - 1)
					res.add(tmp);
			}

		}
		return res;
	}

	/**
	 * belongs to s16
	 * 
	 * @author Moataz_Mekki
	 * @param id
	 *            component id
	 * @param UId
	 *            user id that was assigned to the component this method adds
	 *            the relation between the user & the component to make sure
	 *            that this user is assigned to that component
	 */
	// @Check ("canAssignUserToComponent")
	public static void chooseUsers(long id, long UId) {
		User myUser = User.findById(UId);
		Component myComponent = Component.findById(id);
		Security.check(myComponent.project, "assignUserToComponent");
		if (myUser.components.contains(myComponent)) {
			renderText("Cannot assign user to a component he is already a member in");
		}
		myUser.components.add(myComponent);
		myComponent.componentUsers.add(myUser);
		Date d = new Date();
		// User user = User.find("byEmail", Security.connected()).first();
		User user = Security.getConnected();
		Logs.addLog(user, "assignUser", "User", UId, myComponent.project, d);
		//Notifications.notifyUsers(myUser, "Assigned to a component", "You were assigned to the component " + myComponent.name + " in the project " + myComponent.project.name, (byte) 0);
		myUser.save();
		renderText("User assigned to component successfully|reload('component-" + id + "')");
	}

	/**
	 * Deletes a user from the system, and redirects to the manage admin page
	 * 
	 * @author mahmoudsakr
	 * @param id
	 *            user id
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
	 * This method fetches and renders the corresponding UserNotificationProfile
	 * when a user clicks the manage notifications link corresponding to a
	 * certain project.
	 * 
	 * @author Amr Tj.Wallas
	 * @param id
	 *            The id of that project the user wants to manage his
	 *            notifications in.
	 * @throws ClassNotFoundException
	 * @see {@link models.UserNotificationProfile}
	 * @see {@link views/Users/manageNotificationProfile.html}
	 * @since Sprint2.
	 * @Task C1S33
	 */
	// @Check ("canEditUserNotificationProfile")
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
	 * This method saves any modifications made by the user in a given
	 * UserNotificationProfile in the UI Side to the database. And renders a
	 * success message.
	 * 
	 * @author Amr Tj.Wallas
	 * @param id
	 *            The id of that project the user is managing his notifications
	 *            in.
	 * @throws Exception
	 * @see {@link models.UserNotificationProfile}
	 * @see {@link views/Users/manageNotificationProfile.html}
	 * @since Sprint2.
	 * @Task C1S33
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
	
	public static void delete() {
		forbidden();
	}
	
	/**
	 * @author Monayri
	 * Issue : 228
	 * Sprint : 4
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
	
	public static void listUserProjects(long userId, int x, long projectId, long currentProjectId)
	{
		String title;
		if(x==1)
		{
			Project currentProject = Project.findById(currentProjectId);
			User user = User.findById(userId);
			title= user.name+"'s Projects";
			render(user, title, x, currentProject);
		}
		if(x==2)
		{
			Project currentProject = Project.findById(currentProjectId);
			User user = User.findById(userId);
			Project project = Project.findById(projectId);
			title= user.name+"'s Roles in Project: " + project.name;
			render(user, title, x, project, currentProject);
		}
		
	}
}
