package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.mvc.Http.Request;

@Entity
public class Story extends Model {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@Required
	public Component componentID;

	@Lob
	@Required
	@MaxSize (1000)
	public String description;

	@Lob
	public String succussSenario;

	@Lob
	public String failureSenario;
	
	@Required
	public int priority;

	@Lob
	@MaxSize (1000)
	public String notes;

	@ManyToMany
	public List<Story> dependentStories;

	public boolean deleted;

	public boolean done;

	@ManyToOne
	public ProductRole productRole;

	@OneToMany (mappedBy = "taskStory")
	public List<Task> storiesTask;

	@ManyToOne
	public User addedBy;

	public double estimate;

	public static List<Task> GetTasks(long StoryId) {

		Story Needed_Story = Story.findById(StoryId);
		return Needed_Story.storiesTask;
	}

	/**
	 * A method that gets a Story's List of tasks Story 32 Component 3
	 * 
	 * @param vord
	 * @return List of Tasks
	 * @author Monayri
	 */
	public List<Task> getTasks() {
		return storiesTask;
	}

	/**
	 * Class Constructor Creates a new story object with the basic story
	 * requirements
	 * 
	 * @author Galal Aly
	 * @param des
	 *            Description of the story
	 * @param succ
	 *            Success scenario of the story
	 * @param fail
	 *            Failure scenario of the story
	 * @param priority
	 *            priority of the story
	 * @param notes
	 *            Story notes
	 * @param userId
	 *            The id of the user who added the story
	 */

	public Story (String des, String succ, String fail, int priority, String notes, long userId) {
		String dependentStoriesIds = "";
		System.out.println("oba");
		addedBy = User.findById(userId);
		this.description = des;
		this.succussSenario = succ;
		this.failureSenario = fail;
		this.priority = priority;
		this.notes = notes;
		this.dependentStories = null;
		this.productRole = null;
		this.componentID = null;
		this.storiesTask = new ArrayList<Task>();
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

		return tmp.done;
	}

	/**
	 * Checks whether a story is in a sprint
	 * 
	 * @author Gallal Aly
	 * @return 
	 * 		 whether the story is in a running sprint
	 */
	public boolean inSprint() {
		Date now = Calendar.getInstance().getTime();
		for (Task task : storiesTask) {
			if (task == null)
				continue;
			if (task.taskSprint.startDate.before(now) && task.taskSprint.endDate.after(now)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether this story depends on another
	 * @author Galal Aly
	 * @param s
	 *            The story to check.
	 *   
	 * @return Whether this one depends on s
	 */
	public int dependsOn(Story s) {
		if (this.dependentStories.contains(s)) {
			return this.dependentStories.indexOf(s);
		}
		return -1;
	}
	
	/**
	 * Whether any story depends on this one
	 * 
	 * @author Galal Aly
	 * @return whether any story depends on this one
	 */
	public boolean hasDependency() {
		ArrayList<Story> projectStories = new ArrayList<Story>();
		Project project = this.componentID.project;
		for (Component component : project.components) {
			projectStories.addAll(component.componentStories);
		}
		for (Story x : projectStories) {
			int temp = x.dependsOn(this);
			if (temp != -1) {
				return true;
			}
		}
		return false;
	}

}
