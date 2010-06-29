package controllers;

import java.util.List;

import models.Project;
import models.Role;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

/**
 * CRUD controller to handle all roles/permissions
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class Roles extends SmartCRUD {
	/**
	 * Action to view and manage default roles (those assigned to all projects
	 * by default)
	 */
	public static void defaultRoles() {
		Security.check(Security.getConnected().isAdmin);
		List<Role> roles = Role.find("select r from Role r where r.project = null").fetch();
		render(roles);
	}

	public static void getPermissions(long id) {
		if (id == 0)
			renderJSON(new Object());
		Role r = Role.findById(id);
		Security.check(r.project, "manageRoles");
		r.project = null;
		renderJSON(r);
	}

	public static void show(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Project project = ((Role) object).project;
		List<Role> roles = null;
		if (project == null) {
			Security.check(Security.getConnected().isAdmin);
			roles = Role.find("select r from Role r where r.project = null").fetch();
		} else {
			Security.check(project, "editRoles");
		}
		try {
			Role x = (Role) object;
			render(type, object, x, roles, project);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Role role = (Role) object;
		if (role.project == null) {
			Security.check(Security.getConnected().isAdmin);
		} else {
			Security.check(role.project, "editRoles");
		}
		validation.valid(object.edit("object", params));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			flash.error(Messages.get("crud.hasErrors"));
			redirect("/admin/roles/" + id);
		}
		object.save();
		flash.success(Messages.get("crud.saved", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			Project project = ((Role) object).project;
			redirect(project == null ? "/roles/defaultroles" : "/show/roles?id=" + ((Role) object).project.id);
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	public static void blank(long id) {
		Project project = Project.<Project> findById(id);
		List<Role> roles = null;
		if (id != 0)
			Security.check(project, "canCreateRole");
		else {
			Security.check(Security.getConnected().isAdmin);
			roles = Role.find("select r from Role r where r.project = null").fetch();
		}

		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		try {
			render(project, type, roles);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}
	}

	public static void create(long id) throws Exception {
		Project project = Project.<Project> findById(id);
		if (id == 0) {
			Security.check(Security.getConnected().isAdmin);
			params.remove("object.project@id");
		} else {
			Security.check(project, "createRole");
		}
		ObjectType type = ObjectType.get(getControllerClass());
		JPASupport object = type.entityClass.newInstance();
		validation.valid(object.edit("object", params));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/blank.html", type, id);
				// blank()
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}
		object.save();
		flash.success(Messages.get("crud.created", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			if (id != 0) {
				redirect("/show/roles?id=" + id);
			} else {
				redirect("/roles/defaultroles");
			}
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Security.check(((Role) object).project, "deleteRole");
		if (((Role) object).name.equalsIgnoreCase("project admin")) {
			flash.error(Messages.get("crud.delete.error", type.modelName, object.getEntityId()));
			redirect("/show/roles?id=" + ((Role) object).project.id);
		} else {
			try {
				object.delete();
			} catch (Exception e) {
				flash.error(Messages.get("crud.delete.error", type.modelName, object.getEntityId()));
				redirect(request.controller + ".show", object.getEntityId());
			}
			flash.success(Messages.get("crud.deleted", type.modelName, object.getEntityId()));
			Project project = ((Role) object).project;
			redirect(project == null ? "/roles/defaultroles" : "/show/roles?id=" + ((Role) object).project.id);
		}
	}

	public static void list() {
		forbidden();
	}
}
