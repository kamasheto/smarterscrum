package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Component;
import models.Log;
import models.Project;
import models.Update;
import notifiers.Notifications;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.Router;
import play.mvc.With;

@With (Secure.class)
public class Components extends SmartCRUD {
	/**
	 * This method Overrides the CRUD.blank() method that is executed on adding
	 * a new component, Because the project ID is needed in order to allow the
	 * user to create component only in the project that he was redirected from
	 * 
	 * @author Amr Hany
	 * @param projectID
	 */

	// @Check ("canAddComponent")
	public static void blank(long id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Project currentProject = Project.findById(id);
		if (currentProject.deleted)
			notFound();
		Security.check(Security.getConnected().in(currentProject).can("addComponent"));

		try {
			render(type, currentProject);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type);
		}
	}

	/**
	 * This method Overrides CRUD.create() method that is executed on posting
	 * the data of the new Component. CRUD.create() had an error on validating
	 * the data it redirects into an error because the project id was not sent
	 * again to the page so it gives an error so I just added the currentProject
	 * to the render method
	 * 
	 * @author Amr Hany
	 * @throws Exception
	 */
	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.entityClass.newInstance();
		validation.valid(object.edit("object", params));

		// Here is the only difference,, in order to make validation
		// redirect to the same page without giving error and with the
		// same project
		Component temp = (Component) object;
		Security.check(Security.getConnected().in(temp.project).can("addComponent"));

		Project currentProject = temp.project;

		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {

				render("Components/blank.html", type, currentProject);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type);
			}
		}
		object.save();
		temp.init();
		Log.addUserLog("Created component", temp, currentProject);
		// Logs.addLog(Security.getConnected(), "Create", "Component", temp.id, currentProject, new Date(System.currentTimeMillis()));
		String url = Router.getFullUrl("Application.externalOpen")+"?id="+temp.project.id+"&isOverlay=false&url=/components/viewthecomponent?componentId="+temp.id;		
		Notifications.notifyProjectUsers(temp.project, "onCreateComponent", url, "Component", temp.name, (byte) 0);
		flash.success(Messages.get("crud.created", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			Application.overlayKiller("reload('components')", "");
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect("/admin/projects/" + currentProject.id + "/components/new");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * This method Overrides CRUD.save() method that is executed on posting the
	 * data of the edited Component. CRUD.save() redirects to crud.list() method
	 * which view list of all components, So that was overridden to redirect to
	 * this component page.
	 * 
	 * @author Amr Hany
	 * @throws Exception
	 */
	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Component temp = (Component) object;
		Security.check(Security.getConnected().in(temp.project).can("editComponent"));
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

		String url = Router.getFullUrl("Application.externalOpen")+"?id="+temp.project.id+"&isOverlay=false&url=/components/viewthecomponent?componentId="+temp.id;
		Notifications.notifyProjectUsers(temp.project, "onEditComponent", url, "Component", temp.name, (byte) 0);
		Log.addUserLog("Edited component", temp, temp.project);
		// Logs.addLog(Security.getConnected(), "Edit", "Component", temp.id, temp.project, new Date(System.currentTimeMillis()));
		flash.success(Messages.get("crud.saved", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			Application.overlayKiller("reload('component-" + id + "')", "");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	/**
	 * This method overrides CRUD.delete method because CRUD.delete method
	 * performs deletion of the instance of the model and all its relation but
	 * in This project, the deletion marker is used therefore it just change the
	 * marker to be true and it handles the redirection too.
	 * 
	 * @author Amr Hany
	 * @param id
	 */

	// @Check( "canDeleteComponent" )
	public static void delete(long id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		Component component = (Component) object;
		if (component.deleted)
			notFound();
		Security.check(Security.getConnected().in(component.project).can("deleteComponent"));
		try {
			component.deleteComponent();
			Log.addUserLog("Delete component", component, component.project);
			// Logs.addLog(Security.getConnected(), "Delete", "Component", component.id, component.project, new Date(System.currentTimeMillis()));
			String url = Router.getFullUrl("Application.externalOpen")+"?id="+component.project.id+"&isOverlay=false&url=/components/listcomponentsinproject?projectId="+component.project.id;			
			Notifications.notifyProjectUsers(component.project, "onDeleteComponent", url, "Component", component.name, (byte) -1);
		} catch (Exception e) {
			// flash.error(Messages.get("crud.delete.error", type.modelName,
			// object.getEntityId()));
			// redirect(request.controller + ".show", object.getEntityId());
		}
		// flash.success(Messages.get("crud.deleted", type.modelName,
		// object.getEntityId()));
		// redirect("/projects/" + component.project.id + "/components");
		Update.update(component.project, "reload('components', 'component-"+component.id+"')");
		renderText("Component deleted successfully.");
	}

	/**
	 * This method overrides CRUD.Show, The only change is in the check in order
	 * to check that this user can edit component
	 * 
	 * @author Amr Hany
	 * @param id
	 */
	public static void show(long id) {
		Component c = Component.findById(id);
		if (c.deleted)
			notFound();
		Security.check(Security.getConnected().in(c.project).can("editComponent"));
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
	 * this method renders a list of components in a project
	 * @param projectId the id of the project the page renders its components
	 */
	public static void listComponentsInProject(long projectId) {
		// Project project = Project.findById(projectId);
		Project project = Project.find("byIdAndDeleted", projectId, false).first();
		notFoundIfNull(project);
		List<Component> components = new ArrayList<Component>();
		for (Component c : project.components) {
			if (c.deleted == false)
				components.add(c);
		}
		render(components, projectId);
	}

	/**
	 * this method renders the component that should be shown in the html page
	 * @param componentId the id of the component shown by the html page
	 */
	public static void viewTheComponent(long componentId) {
		Component component = Component.findById(componentId);
		if (component.deleted)
			notFound();
		render(component);
	}

	public static void list() {
		forbidden();
	}

}