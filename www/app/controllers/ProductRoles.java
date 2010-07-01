package controllers;

import java.util.Date;
import java.util.List;

import models.ProductRole;
import models.Project;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With (Secure.class)
public class ProductRoles extends SmartCRUD {
	/**
	 * Renders a list of the product roles that exists in a certain project, the
	 * considered project id and it's name to the project's product roles view.
	 * 
	 * @author Heba Elsherif
	 * @parm id the id of the considered project.
	 * @return void
	 * @task C3 S1 & S2 & S3
	 * @sprint 2
	 **/
	public static void viewProductRoles(long id) {
		Project project = Project.findById(id);
		String projectName = project.name;
		List<ProductRole> productRoles = ProductRole.find("byProject.idAndDeleted", id, false).fetch();
		boolean noProductRoles = productRoles.isEmpty();
		render(productRoles, id, projectName, noProductRoles);
	}

	/**
	 * Renders a product role and a boolean variable indicating if this product
	 * role is included in a task that belongs to a current sprint to the
	 * product role view.
	 * 
	 * @author Heba Elsherif
	 * @parm id the id of the considered project.
	 * @return void
	 * @task C3 S2 & S3
	 * @sprint 2
	 **/
	public static void viewProductRole(long id) {
		ProductRole productRole = ProductRole.findById(id);
		boolean noStories = productRole.stories.isEmpty();
		boolean editable = !(productRole.inSprint());
		boolean deletable = productRole.stories.isEmpty();
		render(productRole, editable, deletable, noStories);
	}

	/**
	 * Overrides the CRUD blank method that renders the create form, in order to
	 * take the project id to create a product role into it.
	 * 
	 * @author Heba Elsherif
	 * @param id
	 *            the project id of the created product role.
	 * @return void
	 * @task C3 S1
	 * @sprint 2
	 **/
	//@Check ("canAddProductRole")
	public static void blank(long id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Project project = Project.findById(id);
		User user = Security.getConnected();
		Security.check(user.in(project).can("addProductRole"));
		try {
			render(type, project);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}
	}

	/**
	 * Overrides the CRUD create method that is invoked to submit the creation
	 * of the product role, in order to associate the product role to the
	 * project and checks if it can be added with that data and adds it to the
	 * database.
	 * 
	 * @author Heba Elsherif
	 * @param void
	 * @return void
	 * @task C3 S1
	 * @sprint 2
	 **/
	//@Check ("canAddProductRole")
	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.entityClass.newInstance();
		validation.valid(object.edit("object", params));
		ProductRole productRoleObject = (ProductRole) object;
		Project project = productRoleObject.project;
		String message = "";
		User user = Security.getConnected();
		Security.check(user.in(project).can("addProductRole"));
		if (validation.hasErrors()) {
			message = "Please Fill in All The Required Fields.";
			if (productRoleObject.name.equals("")) {
				message = message + " Product role name must be added.";
			}
			flash.error(Messages.get(message));
			try {
				render("ProductRoles/blank.html", type, project);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else if (!ProductRole.hasUniqueName(productRoleObject.name, project.id)) {
			flash.error("Product Role name " + productRoleObject.name + " is already taken.");
			try {
				render("ProductRoles/blank.html", type, project);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		} else {
			object.save();
			String header = "Product Role: " + "\'" + productRoleObject.name + "\'" + " has been added.";
			String body = "Added to Project: " + "\'" + project.name + "\'" + "." + '\n'
			+ " Added by: " + Security.getConnected().name + "." + '\n' 
			+ " Added at: " + new Date(System.currentTimeMillis()) + ".";
		  /*////Long Informative Notification message. Not suitable for online notification.
			String header = "Product Role: " + "\'" + productRoleObject.name + "\'" + " has been added to Project: " + "\'" + project.name + "\'" + ".";
			String body = "New Product Role has been added to Project " + "\'" + project.name + "\'" + "." + '\n' + '\n' 
				+ "Product Role Name: " + productRoleObject.name +"."+ '\n' 
				+ " Description: " + productRoleObject.description + "." + '\n' 
				+ " Added by: " + Security.getConnected().name + "." + '\n' 
				+ " Added at: " + new Date(System.currentTimeMillis()) + "."; */
			Logs.addLog(Security.getConnected(), "Create", "ProductRole", productRoleObject.id, project, new Date(System.currentTimeMillis()));
			Notifications.notifyUsers(project, header, body, "addProductRole", new Byte((byte) 1));
			flash.success("Product Role " + productRoleObject.name + " has been created successfully.");
			if (params.get("_save") != null) {
				redirect("/projects/" + project.id + "/productroles");
			}
			if (params.get("_saveAndAddAnother") != null) {
				redirect("/admin/projects/" + project.id + "/productroles/new");
			}
			redirect(request.controller + ".show", object.getEntityId());
		}
	}

	/**
	 * Overrides the CRUD show method that renders the edit form, in order to
	 * take the project id and checks if it's editable.
	 * 
	 * @author Heba Elsherif
	 * @param id
	 *            the project id of the created product role.
	 * @return void
	 * @task C3 S2 & S3
	 * @sprint 2
	 **/
	//@Check ("canEditProductRole")
	public static void show(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		ProductRole productRoleObject = (ProductRole) object;
		Project project = productRoleObject.project;
		boolean editable = !(productRoleObject.inSprint());
		boolean deletable = (productRoleObject.stories.isEmpty());
		User user = Security.getConnected();
		Security.check(user.in(project).can("editProductRole"));
		try {
			render(type, object, project, editable, deletable);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	/**
	 * Overrides the CRUD save method that is invoked to submit the edit, in
	 * order to check if the edits are acceptable.
	 * 
	 * @author Heba Elsherif
	 * @param id
	 *            the editable product role id.
	 * @return void
	 * @task C3 S2
	 * @sprint 2
	 **/
	//@Check ("canEditProductRole")
	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		ProductRole productRoleObject = (ProductRole) object;
		String oldName = productRoleObject.name;
		validation.valid(object.edit("object", params));
		Project project = productRoleObject.project;
		String message = "";
		boolean editable = !(productRoleObject.inSprint());
		boolean deletable = (productRoleObject.stories.isEmpty());
		User user = Security.getConnected();
		Security.check(user.in(project).can("editProductRole"));
		if (validation.hasErrors()) {
			message = "Please Fill in All The Required Fields.";
			if (productRoleObject.name.equals("")) {
				message = message + " Product Role name must be added.";
			}
			flash.error(Messages.get(message));
			try {
				render("ProductRoles/show.html", type, object, editable, deletable);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		} else {
			String header = "Product Role: " + "\'" + oldName + "\'" + " in Project " + "\'" + project.name + "\'" + " has been edited.";
			String body = "The Product Role: " + "\'" + oldName + "\'" + " in Project " + "\'" + project.name + "\'" + " has been edited." + '\n' + '\n'
				+ "Product Role Name: " + productRoleObject.name + "." + '\n' 
				+ " Description: " + productRoleObject.description + "." + '\n' 
				+ " Edited by: " + Security.getConnected().name + "." + '\n' 
				+ " Edited at: " + new Date(System.currentTimeMillis()) + ".";
			object.save();
			Logs.addLog(Security.getConnected(), "Edit", "ProductRole", productRoleObject.id, project, new Date(System.currentTimeMillis()));
			Notifications.notifyUsers(project, header, body, "editProductRole", new Byte((byte) 0));
			flash.success("Product Role " + productRoleObject.name + " has been edited.");
			if (params.get("_save") != null) {
				redirect("/productroles/" + productRoleObject.id);
			}
			redirect(request.controller + ".show", object.getEntityId());
		}
	}

	/**
	 * Overrides the CRUD delete method that is invoked to delete a product
	 * role, in order to delete the product role by setting the deleted boolean
	 * variable to true instade of deleting it.
	 * 
	 * @author Heba Elsherif
	 * @param id
	 *            the id of the product role that's going to be deleted.
	 * @return void
	 * @task C3 S3
	 * @sprint 2
	 **/
	//@Check ("canDeleteProductRole")
	public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		ProductRole productRoleObject = (ProductRole) object;
		Project project = productRoleObject.project;
		productRoleObject.deleted = true;
		boolean editable = !(productRoleObject.inSprint());
		boolean deletable = (productRoleObject.stories.isEmpty());
		User user = Security.getConnected();
		Security.check(user.in(project).can("deleteProductRole"));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render("ProductRoles/show.html", type, object, editable, deletable);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		} else if (!(productRoleObject.stories.isEmpty())) {
			flash.error("This Product Role cannot be deleted because it is included in a story.");
			try {
				render("ProductRoles/show.html", type, object, editable, deletable);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		} else {
			String header = "Product Role: " + "\'" + productRoleObject.name + "\'" + " in Project " + "\'" + productRoleObject.project.name + "\'" + " has been deleted.";
			String body = "The Product Role: " + "\'" + productRoleObject.name + "\'" + " in Project " + "\'" + productRoleObject.project.name + "\'" + " has been deleted."
				+ '\n' + '\n' 
				+ " Deleted by: " + Security.getConnected().name + "." + '\n' 
				+ " Deleted at: " + new Date(System.currentTimeMillis()) + ".";
			object.save();
			Logs.addLog(Security.getConnected(), "Delete", "ProductRole", productRoleObject.id, productRoleObject.project, new Date(System.currentTimeMillis()));
			Notifications.notifyUsers(productRoleObject.project, header, body, "deleteProductRole", new Byte((byte) -1));
			flash.success("Product Role " + productRoleObject.name + " has been deleted.");
			redirect("/projects/" + project.id + "/productroles");
		}
	}

	/**
	 * Deletes a product role.
	 * 
	 * @author Heba Elsherif
	 * @parm id the id of the considered product role.
	 * @return void
	 * @task C3 S3
	 * @sprint 2
	 **/
	//@Check ("canDeleteProductRole")
	public static void deleteProductRole(long id) {
		ProductRole productRoleObject = ProductRole.findById(id);
		Project project = productRoleObject.project;
		User user = Security.getConnected();
		Security.check(user.in(project).can("deleteProductRole"));
		productRoleObject.deleted = true;
		productRoleObject.save();
		
		String header = "Product Role: " + "\'" + productRoleObject.name + "\'" + " in Project " + "\'" + productRoleObject.project.name + "\'" + " has been deleted.";
		String body = "The Product Role: " + "\'" + productRoleObject.name + "\'" + " in Project " + "\'" + productRoleObject.project.name + "\'" + " has been deleted."
			+ '\n' + '\n' 
			+ " Deleted by: " + Security.getConnected().name + "." + '\n' 
			+ " Deleted at: " + new Date(System.currentTimeMillis()) + ".";
		Logs.addLog(Security.getConnected(), "Delete", "ProductRole", productRoleObject.id, productRoleObject.project, new Date(System.currentTimeMillis()));
		Notifications.notifyUsers(productRoleObject.project, header, body, "deleteProductRole", new Byte((byte) -1));
		flash.success("Product Role " + productRoleObject.name + " has been deleted.");
	}
	
	/**
	 * Overrides the CRUD list method that is invoked to list product roles
	 * role, in order to in order not to allow users to view the crud list for the productroles.
	 * 
	 * @author Heba Elsherif
	 * @sprint 3
	 **/
	public static void list( int page, String search, String searchFields, String orderBy, String order )
	{
		forbidden();
	}
}
