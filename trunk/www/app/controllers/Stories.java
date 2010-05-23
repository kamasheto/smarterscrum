package controllers;

// import java.awt.Component;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import models.Component;
import models.Task;
import models.Sprint;
import models.Project;
import models.Story;
import models.User;
import play.libs.Mail;
import play.mvc.Controller;
import play.mvc.With;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

@With (Secure.class)
public class Stories extends Controller {

	/**
	 * Views the add story form.
	 * 
	 * @author Galal Aly
	 * @parm void
	 * @return void
	 */
	@Check ("canAddStory")
	public static void add() {
		render();
	}

	/**
	 * Adds a story to the database.
	 * 
	 * @author Galal Aly
	 * @param des
	 *            is the story description to be added
	 * @param succ
	 *            is the success Scenarios of the story to be added
	 * @param fail
	 *            is the failure Scenarios of the story to be added
	 * @param priority
	 *            is the priority of the story to be added. 0 : low, 1: Medium
	 *            and 2:High
	 * @param notes
	 *            is the notes of the story to be added.
	 * @return void
	 */
	@Check ("canAddStory")
	public static void addStory(String des, String succ, String fail, int priority, String notes) {
		String message = "";
		if (des.length() < 5) {
			message = "Error, Description must be at least of length 5";
		} else {
			User u = User.find("byEmail", Security.connected()).first();
			Story add = new Story(des, succ, fail, priority, notes, u.id);
			add.save();
			message = "Story added successfully";
		}
		render(message);
	}

	/**
	 * Edits an existing story and saves the updates to the database.
	 * 
	 * @author Galal Aly
	 * @param id
	 *            is the existing story's id to be edited
	 * @param des
	 *            is the new story description to replace the existing one.
	 * @param succ
	 *            is the new success Scenarios of the story to replace the
	 *            existing one.
	 * @param fail
	 *            is the new failure Scenarios of the story to replace the
	 *            existing one.
	 * @param priority
	 *            is the new priority of the story to replace the existing one.
	 *            0 : low, 1: Medium and 2:High
	 * @param notes
	 *            is the new notes of the story to replace the existing one.
	 * @return void
	 */
	@Check ("canEditStory")
	public static void editStory(long id, String des, String succ, String fail, int priority, String notes) {
		String message = "";
		System.out.println("oba " + id);
		Story story = Story.findById(id);
		if (story == null) {
			message = "Error, Story not found";
		} else if (des.length() < 5) {
			message = "Error, Description must be at least of length 5";
		} else {
			story.description = des;
			story.succussSenario = succ;
			story.failureSenario = fail;
			story.priority = priority;
			story.notes = notes;
			story.save();
			message = "Story edited successfully " + story.description;
		}
		render(message);
	}

	/**
	 * Deletes an existing story by changing its deletion market to true.
	 * 
	 * @author Galal Aly
	 * @param id
	 *            is the existing story's id to be deleted
	 * @return void
	 */
	@Check ("canDeletetory")
	public static void deleteStory(long id) {
		Story story = Story.findById(id);
		/*
		 * Plan : - Loop on all stories that depends on this story and remove
		 * the dependency - Mark all the tasks under this story as deleted
		 */
		ArrayList<Story> projectStories = new ArrayList<Story>();
		Project project = story.componentID.project;
		for (Component component : project.components) {
			projectStories.addAll(component.componentStories);
		}
		for (Story x : projectStories) {
			int temp = x.dependsOn(story);
			if (temp != -1)
				x.dependentStories.remove(temp);
			x.save();
		}

		for (Task t : story.storiesTask) {
			// delete the tasks
		}

		// Now mark my story as deleted
		story.deleted = true;
		story.save();
		// Logs.addLog( story.addedBy, "Delete", "Story", story.id , project,
		// new GregorianCalendar().getTime());
		render();
	}

	/**
	 * Lists all of the stories ordered by their id in descending order. For
	 * testing purposes.
	 * 
	 * @author Galal Aly
	 * @param void
	 * @return void
	 */
	public static void list() {
		List<Story> stories = Story.find("order by id desc").fetch();
		render(stories);
	}

	/**
	 * Gets the story from the database and sends it to the view to be placed in
	 * a form so that a user can edit it.
	 * 
	 * @author Galal Aly
	 * @param id
	 *            is the existing story's id to be edited
	 */
	@Check ("canEditStory")
	public static void edit(long id) {
		Story story = Story.findById(id);
		render(story);
	}

	/**
	 * Returns story's status whether it is done or not.
	 * 
	 * @param id
	 *            the id of the story
	 * @return the story status whether it is done
	 */
	public static boolean storyIsDone(long id) {
		Story tmp = Story.findById(id);
		render(tmp.done);
		return tmp.done;
	}

	/**
	 * Views a form for the user to suggest changes.
	 * 
	 * @author Galal Aly
	 * @param id
	 *            the id of the story
	 * @return void
	 */
	@Check ("canRequest")
	public static void request(long id) {
		Story story = Story.findById(id);
		render(story);
	}

	/**
	 * Sends an email to the project owner to notify him that there's a request
	 * to edit a user story
	 * 
	 * @author Galal Aly
	 * @param id
	 *            the id of the story
	 * @param des
	 *            is the new story description to replace the existing one if
	 *            request was approved.
	 * @param succ
	 *            is the new success Scenarios of the story to replace the
	 *            existing one if request was approved.
	 * @param fail
	 *            is the new failure Scenarios of the story to replace the
	 *            existing one if request was approved.
	 * @param priority
	 *            is the new priority of the story to replace the existing one
	 *            if request was approved. 0 : low, 1: Medium and 2:High
	 * @param notes
	 *            is the new notes of the story to replace the existing one if
	 *            request was approved.
	 * @return void
	 */
	@Check ("canRequest")
	public static void sendRequest(long id, String des, String succ, String fail, int priority, String notes) {
		// Get the priority
		String prio = "";
		switch (priority) {
			case 0:
				prio = "Low";
				break;
			case 1:
				prio = "Medium";
				break;
			case 2:
				prio = "High";
				break;
			default:
				prio = "Low";
				break;
		}

		// Get the connected user
		String sender = Security.connected();
		User user = User.find("ByEmail", sender).first();
		String myuser = user.name;

		// Get the story's project owner (the addedBy column)
		Story t = Story.findById(id);
		String projectOwnerEmail = t.addedBy.email;
		String username = t.addedBy.name;

		// Writing a nice email message
		String message = "Dear " + username + ",<br>";
		message += "The user " + myuser + " requested a change to a user story and here are the details :<br>";
		message += "Description<br>" + des + "<br>";
		message += "Success Scenarios<br>" + succ + "<br>";
		message += "Failure Scenarios<br>" + fail + "<br>";
		message += "The Priority<br>" + prio + "<br>";
		message += "Notes<br>" + notes + "<br>";
		message += "<br>";
		message += "To approve the updates please edit the story using this link";
		message += "http://localhost:9000/stories/edit?id=" + id;

		// Send the email to the scrum master
		Mail.send(sender, projectOwnerEmail, "Editing a User Story Request", message);
		String result = "Request sent successfully";
		render(result);

	}

	/**
	 * Sends an email to the project owner to notify him that there's a request
	 * to delete a user story
	 * 
	 * @author Galal Aly
	 * @param id
	 *            the id of the story to be deleted
	 * @return void
	 */
	@Check ("canRequest")
	public static void sendDeleteRequest(long id, String s) {
		// Get the connected user
		String sender = Security.connected();
		User user = User.find("ByEmail", sender).first();
		String myuser = user.name;

		// Get the story's project owner (the addedBy column)
		Story t = Story.findById(id);
		String projectOwnerEmail = t.addedBy.email;
		String username = t.addedBy.name;

		// Writing a nice email message
		String message = "Dear " + username + ",<br>";
		message += "The user " + myuser + " requested to delete a user story<br>";
		message += s;
		message += "<br>";
		message += "To delete the story please click on this link";
		message += "http://localhost:9000/stories/deleteStory?id=" + id;

		// Send the email to the scrum master
		Mail.send(sender, projectOwnerEmail, "Editing a User Story Request", message);
		String result = "Request sent successfully";
		render(result);

	}
	/*
	 * /** Gets all the stories in the same project of a story
	 * @author Heba Elsherif
	 * @param storyId the id of the given story
	 * @return void
	 * @task C3 S10 public static void getAllStoriesProject( long storyId ) {
	 * System.out.println("get storiessss"); String message=""; Story story =
	 * Story.findById( storyId ); if( story == null ) { message =
	 * "Invaid Story ID."; renderText( message ); } //Long id =
	 * story.componentID.id; Component component =
	 * Component.findById(story.componentID.id); if( component == null ) {
	 * message = "Invaid Component ID."; renderText( message ); } Project
	 * project = component.project; if( project == null ) { message =
	 * "Project doesn't exist."; renderText( message ); } List<Component>
	 * components = project.components; if( components == null ) { message =
	 * "Project doesn't contain Components."; renderText( message ); }
	 * List<Story> stories = new ArrayList<Story>(); for(int i =
	 * components.size(); i>0; i--) { for(int j =
	 * components.get(i).componentStories.size(); j>0; j--) {
	 * stories.add(components.get(i).componentStories.get(j)); } } if(stories ==
	 * null) { message = "No list of stories."; render(message); } else
	 * render(stories); } /** Views the add Set Dependent Stories form.
	 * @author Heba Elsherif
	 * @parm void
	 * @return void
	 * @task C3 S10
	 * @Check( "canSetDependentStories" ) public static void
	 * setDependentStories() { render(); } /** Sets a story dependent on another
	 * story.
	 * @author Heba Elsherif
	 * @param storyID id of the story
	 * @param dependetStoryID id of the dependent story
	 * @return void
	 * @task C3 S10
	 * @Check( "canSetDependentStories" ) public static void
	 * setDependentStories2( long storyId, long dependetStoryId ) {
	 * System.out.println("ya rabbbbbbbbbbbbbb"); String message = ""; boolean
	 * actionNotAllowed = false; Story story = Story.findById(storyId); Story
	 * dependentStory = Story.findById(dependetStoryId); for(int i =
	 * story.storiesTask.size(); i>0; i--) { for(int j =
	 * dependentStory.storiesTask.size(); j>0; j--) {
	 * if(Long.parseLong((story.storiesTask.get(i-1).taskSprint.sprintNumber)) <
	 * Long
	 * .parseLong((dependentStory.storiesTask.get(j-1).taskSprint.sprintNumber
	 * ))) { message =
	 * "Action Failed. The dependent story has task(s)in a following sprit to the sprint of one of the tasks of this story."
	 * ; actionNotAllowed = true; break; } if
	 * (story.storiesTask.get(i-1).status==1) { message =
	 * "Action Failed. Some of the story tasks are in progress.";
	 * actionNotAllowed = true; break; } } } if(actionNotAllowed == false) {
	 * story.dependentStories.add(dependentStory); story.save(); message =
	 * "Dependency on a story has been added."; } render( message ); }
	 */

}
