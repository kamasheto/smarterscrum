package controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;

import models.Project;
import models.TaskType;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;

public class TaskTypes extends SmartCRUD {
	public static void blank(long id) {
		ObjectType type = ObjectType.get(getControllerClass());
		Project project = Project.findById(id);
		notFoundIfNull(type);
		try {
			render(type, project);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}
	}

	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.entityClass.newInstance();
		validation.valid(object.edit("object", params));
		TaskType tmp = (TaskType) object;
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/blank.html", type, tmp.project);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}
		object.save();
		tmp = (TaskType) object;
		Calendar cal = new GregorianCalendar();
		User myUser = User.find("byEmail", Security.connected()).first();
		Logs.addLog(myUser, "add", "TaskType", tmp.id, tmp.project, cal.getTime());
		String message2 = myUser.name + " has created a task Type " + tmp.name;
		Notifications.notifyUsers(tmp.project.users, "Task Type added", message2, (byte) 1);
		flash.success(Messages.get("crud.created", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

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
		TaskType tmp = (TaskType) object;
		Calendar cal = new GregorianCalendar();
		User myUser = User.find("byEmail", Security.connected()).first();
		Logs.addLog(myUser, "edit", "TaskType", tmp.id, tmp.project, cal.getTime());
		String message2 = myUser.name + " has editted a task type to " + tmp.name;
		Notifications.notifyUsers(tmp.project.users, "Task Type editted", message2, (byte) 1);
		flash.success(Messages.get("crud.saved", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		TaskType tmp = (TaskType) object;
		try {
			tmp.deleted = true;
			object.save();
			Calendar cal = new GregorianCalendar();
			User myUser = User.find("byEmail", Security.connected()).first();
			Logs.addLog(myUser, "delete", "TaskType", tmp.id, tmp.project, cal.getTime());
			String message2 = myUser.name + " has deleted the task type " + tmp.name;
			Notifications.notifyUsers(tmp.project.users, "Task Type deleted", message2, (byte) 1);
		} catch (Exception e) {
			flash.error(Messages.get("crud.delete.error", type.modelName, object.getEntityId()));
			redirect(request.controller + ".show", object.getEntityId());
		}
		flash.success(Messages.get("crud.deleted", type.modelName, object.getEntityId()));
		redirect(request.controller + ".list");
	}

}