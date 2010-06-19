package controllers;

import models.Role;
import play.db.jpa.FileAttachment;
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
	public static void getPermissions(long id) {
		if (id == 0)
			renderJSON(new Object());
		Role r = Role.findById(id);
		Security.check(Security.getConnected().in(r.project).can("manageRoles"));
		r.project = null;
		renderJSON(r);
	}

	// @Check ("canEditRoles")
	public static void show(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		try {
			render(type, object);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	@Check ("canEditRoles")
	public static void attachment(String id, String field) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		FileAttachment attachment = (FileAttachment) object.getClass().getField(field).get(object);
		if (attachment == null) {
			notFound();
		}
		renderBinary(attachment.get(), attachment.filename);
	}

	@Check ("canEditRoles")
	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
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
		flash.success(Messages.get("crud.saved", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			redirect("/show/roles?id=" + ((Role) object).project.id);
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	@Check ("canCreateRole")
	public static void blank(long id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		try {
			render(type, id);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}
	}

	@Check ("canCreateRole")
	public static void create(long id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
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
			redirect("/show/roles?id=" + id);
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	@Check ("canDeleteRole")
	public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		try {
			object.delete();
		} catch (Exception e) {
			flash.error(Messages.get("crud.delete.error", type.modelName, object.getEntityId()));
			redirect(request.controller + ".show", object.getEntityId());
		}
		flash.success(Messages.get("crud.deleted", type.modelName, object.getEntityId()));
		redirect(request.controller + ".list");
	}

}
