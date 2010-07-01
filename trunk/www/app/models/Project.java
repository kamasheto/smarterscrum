package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.Required;

/**
 * @author Kash
 */
/**
 * @author Kash
 */
@Entity
public class Project extends SmartModel {
	@Column (unique = true)
	@Required
	public String name;

	@Lob
	@Required
	public String description;

	public boolean deleted;

	public boolean approvalStatus = false;

	public User user;

	/* One To Many Relations */
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<ProductRole> productRoles;

	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<Meeting> meetings;

	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<Role> roles;

	@OneToMany (mappedBy = "project")
	public List<Component> components;

	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<Sprint> sprints;

	// Added in Sprint 2 by Galal Aly
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<Priority> priorities;

	// Added in Sprint 2 by Monayri
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<TaskStatus> taskStatuses;

	// Added in Sprint 2 by Monayri
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<TaskType> taskTypes;

	/**
	 * userNotificationProfiles associated with the project.
	 * 
	 * @author Amr Tj.Wallas
	 * @see models.UserNotificationProfile
	 * @since Sprint2.
	 * @Task C1S33
	 */
	@OneToMany (mappedBy = "project")
	public List<UserNotificationProfile> userNotificationProfiles;

	/**
	 * requests associated with the project.
	 * 
	 * @author Amr Tj.Wallas
	 * @see models.Request
	 * @since Sprint2.
	 * @Task C1S14
	 */
	@OneToMany (mappedBy = "project")
	public List<Request> requests;

	/* One To One Relations */
	@OneToOne (mappedBy = "project")
	public Board board;

	@OneToOne (mappedBy = "project", cascade = CascadeType.ALL)
	public ProjectNotificationProfile notificationProfile;

	public ChatRoom chatroom;

	/*                      */

	/* Many To Many Relations */
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL)
	public List<Log> logs;

	@ManyToMany (mappedBy = "projects", cascade = CascadeType.ALL)
	public List<User> users;

	/*                       */

	public ArrayList<String> meetingsTypes;
	public ArrayList<Boolean> meetingsTypesInSprint;
	public boolean autoReschedule;
	public boolean autoNotify;
	public int sprintDuration;
	public int effortEstimationUnit;

	public Project () {
		this.productRoles = new ArrayList<ProductRole>();
		this.meetings = new ArrayList<Meeting>();
		this.roles = new ArrayList<Role>();
		this.components = new ArrayList<Component>();
		this.sprints = new ArrayList<Sprint>();
		this.priorities = new ArrayList<Priority>();

		this.taskStatuses = new ArrayList<TaskStatus>();
		this.taskTypes = new ArrayList<TaskType>();
		this.userNotificationProfiles = new ArrayList<UserNotificationProfile>();
		this.requests = new ArrayList<Request>();
		this.logs = new ArrayList<Log>();
		this.users = new ArrayList<User>();
	}

	/**
	 * Project Class Constructor.
	 * 
	 * @author behairy
	 * @param projectName
	 *            String The name of the project to be created.
	 * @param projectDescription
	 *            String The description of the project to be created.
	 */
	public Project (String projectName, String projectDescription, User projectAdmin, int numberOfComponents, int numberOfSprints) {
		this();
		this.deleted = false;
		this.approvalStatus = false;
		this.name = projectName;
		this.description = projectDescription;
		// this.numberOfComponents = numberOfComponents;
		// this.numberOfSprints = numberOfSprints;
		// init();

	}

	public Project (String name, String desc) {
		this();
		this.name = name;
		this.description = desc;
	}

	/**
	 * This method gets the tasks of a given project as a list of Tasks Story 34
	 * Component 3
	 * 
	 * @author Monayri
	 * @param void
	 * @return List<Task>:ProjectTasks
	 */
	public List<Task> getTasks() {

		List<Component> Components = this.components;
		List<Task> ProjectTasks = new ArrayList<Task>();
		for (int i = 0; i < Components.size(); i++) {
			Component X = Components.get(i);
			List<Story> ComponentsTasks = X.componentStories;
			for (int j = 0; j < ComponentsTasks.size(); j++) {
				List<Task> Tasks = ComponentsTasks.get(j).storiesTask;
				for (int z = 0; z < Tasks.size(); z++) {
					ProjectTasks.add(Tasks.get(z));
				}
			}
		}
		return ProjectTasks;
	}

	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * this method returns a list of the components in this project requested by
	 * C1S24, C3S9, C3S11, C3S18, C5S1
	 * 
	 * @author Ghada Fakhry
	 * @return : List of component's users
	 */
	public List<Component> getComponents() {
		return components;
	}

	/**
	 * Returns a String that calls the GenerateFullGarph and GenerateGraph
	 * functions to show the appropriate graphs.
	 * 
	 * @param cid
	 *            this is the component id
	 * @author Hadeer Younis
	 * @category C4S9
	 * @return String containing the functions to be called on page load
	 */
	public String fetchData(long cid) {
		String data = "";
		String FULL = "GenerateFullGraph([";
		String names = "[";
		if (sprints.size() == 0)
			return "''";
		for (int i = 0; i < sprints.size(); i++) {
			if (sprints.get(i).tasks.size() > 0) {
				if (i == 2) {
					FULL = FULL + sprints.get(i).getCoordinatesOfData(cid);
					names = names + "{label:'Sprint: " + sprints.get(i).sprintNumber + "'}";
				} else {
					FULL = FULL + sprints.get(i).getCoordinatesOfData(cid) + ",";
					names = names + "{label:'Sprint: " + Long.parseLong(sprints.get(i).sprintNumber) + "'},";
				}
				data = data + ("GenerateGraph(" + sprints.get(i).fetchData(cid) + ",'c_" + sprints.get(i).sprintNumber + "');" + '\n');

			}
		}
		// FULL = FULL+"],";
		FULL = FULL + "]," + names + "]);";
		return FULL + data;

	}

	public List<User> getUsers() {

		return users;
	}

	/**
	 * Overriding toString() method.
	 * 
	 * @author Behairy
	 * @param void
	 * @return String:projectName
	 */
	@Override
	public String toString() {
		return name;

	}

	/*
	 * @author minazaki i just modified the insprint to my need it checks if the
	 * start date of a sprint overlaps with any sprints OR the end date overlaps
	 * with any sprints
	 * @param start the start date of the sprint
	 * @param endate the end date of the sprint
	 * @return boolean
	 */
	public boolean inSprint(Date start, Date end) {
		for (Sprint sprint : sprints) {
			if ((start.after(sprint.startDate) && start.before(sprint.endDate)) || (end.after(sprint.startDate) && end.before(sprint.endDate)))
				return true;
		}
		return false;
	}

	/**
	 * @author minazaki
	 * @param
	 * @return long id of the sprint currently running or -1 otherwise (no
	 *         sprints running)
	 */
	public long runningSprint() {
		Date now = Calendar.getInstance().getTime();
		for (int i = 0; i < sprints.size(); i++) {
			if ((sprints.get(i).startDate.before(now)) && (sprints.get(i).endDate.after(now))) {
				return sprints.get(i).id;
			}
		}
		return -1;
	}

	/**
	 * This method takes a date and returns if this date occur in a sprint of
	 * the project's sprints
	 * 
	 * @author Amr Hany
	 * @param givenDate
	 * @return Project is inSprint
	 */
	public boolean inSprint(Date givenDate) {
		int i = 0;
		for (Sprint sprint : sprints) {
			if ((givenDate.after(sprint.startDate) && givenDate.before(sprint.endDate)))
				return true;

			i++;
		}
		return false;
	}

	/**
	 * Creates new roles and assigns them to this project!
	 * 
	 * @author mahmoudsakr
	 */
	public void init() {
		// this.save();
		board = new Board(this).save();

		// chat room added by amr hany
		chatroom = new ChatRoom(this).save();

		List<Role> roles = Role.find("select r from Role r where r.project = null").fetch();
		for (Role role : roles) {
			Role r = new Role();
			r.name = role.name;
			r.baseRole = role.baseRole;
			r.project = this;
			r.permissions.addAll(role.permissions);
			r.save();
		}

		// automatically create a project notification profile once the project
		// is created
		this.notificationProfile = new ProjectNotificationProfile(this).save();

		// username = Security.getConnected() != null ?
		// Security.getConnected().email : "";

		meetingsTypes = new ArrayList<String>();
		meetingsTypesInSprint = new ArrayList<Boolean>();

		TaskType t1 = new TaskType();
		t1.project = this;
		t1.name = "Documentation";
		t1.save();
		taskTypes.add(t1);

		TaskType t2 = new TaskType();
		t2.project = this;
		t2.name = "Impediment";
		t2.save();
		taskTypes.add(t2);

		TaskType t3 = new TaskType();
		t3.project = this;
		t3.name = "Implementation";
		t3.save();
		taskTypes.add(t3);

		TaskType t4 = new TaskType();
		t4.project = this;
		t4.name = "Test";
		t4.save();
		taskTypes.add(t4);

		TaskType t5 = new TaskType();
		t5.project = this;
		t5.name = "Other";
		t5.save();
		taskTypes.add(t5);

		TaskType t6 = new TaskType();
		t6.project = this;
		t6.name = "Impediment";
		t6.save();
		taskTypes.add(t6);

		TaskStatus s1 = new TaskStatus();
		s1.project = this;
		s1.name = "New";
		s1.save();
		s1.init();
		taskStatuses.add(s1);

		TaskStatus s2 = new TaskStatus();
		s2.project = this;
		s2.name = "Started";
		s2.save();
		s2.init();
		taskStatuses.add(s2);

		TaskStatus s3 = new TaskStatus();
		s3.project = this;
		s3.name = "Fixed";
		s3.save();
		s3.init();
		taskStatuses.add(s3);

		TaskStatus s4 = new TaskStatus();
		s4.project = this;
		s4.name = "Reopened";
		s4.save();
		s4.init();
		taskStatuses.add(s4);

		TaskStatus s5 = new TaskStatus();
		s5.project = this;
		s5.name = "Resolved";
		s5.save();
		s5.init();
		taskStatuses.add(s5);

		TaskStatus s6 = new TaskStatus();
		s6.project = this;
		s6.name = "Verified";
		s6.save();
		s6.init();
		taskStatuses.add(s6);

		TaskStatus s7 = new TaskStatus();
		s7.project = this;
		s7.name = "Closed";
		s7.save();
		s7.init();
		taskStatuses.add(s7);

		meetingsTypes.add("Scrum Meeting");
		meetingsTypes.add("Plan Meeting");

		meetingsTypesInSprint.add(true);
		meetingsTypesInSprint.add(true);

		sprintDuration = 14;

		this.save();
	}

	/**
	 * @author mahmoudsakr
	 */
	public static class Object {
		long id;

		String name;

		public Object (long id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	/**
	 * This method Checks whether the user has requested the project given
	 * before.
	 * 
	 * @param name
	 * @return boolean True if name is Unique
	 * @author behairy
	 */

	public static boolean isUnique(String name) {
		List<Project> p = Project.find("name='" + name + "'").fetch();

		if (p.isEmpty())
			return false;
		else
			return true;

	}

	/**
	 * This method Checks whether the user has requested the project given
	 * before.
	 * 
	 * @param username
	 * @param projectname
	 * @return boolean
	 * @author behairy
	 */
	public static boolean userRequstedProjectBefore(Long userId, String projectname) {
		List<Project> p = Project.find("name='" + projectname + "' and " + " user.id='" + userId + "' and approvalStatus=false and deleted=false").fetch();

		if (p.isEmpty())
			return false;
		else
			return true;

	}

	/**
	 * Checks if a sprint is running
	 * 
	 * @author hossam sharaf
	 * @return boolean
	 */
	public boolean hasRunningSprints() {
		long now = new Date().getTime();
		List<Sprint> Sprints = Sprint.find("byProject", this).fetch();
		// System.out.println("Size "+Sprints.size());
		for (int i = 0; i < Sprints.size(); i++) {

			// System.out.println("sta "+Sprints.get(i).startDate.getTime());
			// System.out.println("now "+now);
			// System.out.println("end "+Sprints.get(i).endDate.getTime());
			if (now > Sprints.get(i).startDate.getTime() && now < Sprints.get(i).endDate.getTime()) {
				return true;
			}
		}
		return false;

	}

	/**
	 * This method simply removes a user from a project.
	 * 
	 * @author Amr Tj.Wallas
	 * @param user
	 *            The user to be deleted from the project.
	 * @see controllers.Requests
	 * @since Sprint2.
	 * @Task C1S14
	 */
	public void removeUser(User user) {
		this.users.remove(user);
		this.save();
	}

	/**
	 * method to return all the upcoming and current sprints
	 * 
	 * @author minazaki
	 * @return list of current and upcoming sprints
	 */
	public ArrayList<Sprint> upcomingSprints() {
		Date today = new Date();
		ArrayList<Sprint> list = new ArrayList<Sprint>();
		for (int i = 0; i < sprints.size(); i++) {
			if (sprints.get(i).startDate.after(today))
				list.add(sprints.get(i));
		}
		if (runningSprint() != -1) {
			Sprint running = Sprint.findById(runningSprint());
			list.add(running);
		}
		return list;
	}

	/**
	 * to return meetings that are associated to the end of sprint
	 * 
	 * @author minazaki
	 * @return list of meetings associated to end of sprint
	 */
	public List<Meeting> meetingsAssoccToEndOfSprint(Sprint sprint) {
		ArrayList<Meeting> temp = new ArrayList<Meeting>();
		for (int i = 0; i < meetings.size(); i++) {

			if (meetings.get(i).sprint != null && meetings.get(i).sprint.equals(sprint)) {
				int index = meetingsTypes.indexOf(meetings.get(i).type);
				if (meetingsTypesInSprint.get(index)) {
					temp.add(meetings.get(i));
				}
			}
		}
		return temp;
	}

	/**
	 * return list of meeting associated to end of sprint
	 * 
	 * @return list of meetings that are associated to end of sprint
	 */
	public List<String> meetingTypes() {
		ArrayList<String> temp = new ArrayList<String>();
		for (int i = 0; i < meetingsTypes.size(); i++) {
			if (meetingsTypesInSprint.get(i))
				temp.add(meetingsTypes.get(i));
		}
		return temp;
	}

	public void addComponent(Component c) {
		this.components.add(c);
		c.project = this;
		c.save();
	}

	public int getNumberOfRequests() {
		List<Request> requests = Request.find("byIsDeletionAndProject", false, this).fetch();
		return requests.size();
	}

	public int getNumberOfDeletionRequests() {
		List<Request> requests = Request.find("byIsDeletionAndProject", true, this).fetch();
		return requests.size();
	}
	public int getNumberOfTotalRequests()
	{
		List<Request> requests = Request.find("byIsDeletionAndProject", false, this).fetch();
		List<Request> drequests = Request.find("byIsDeletionAndProject", true, this).fetch();
		return requests.size()+drequests.size();
	}
}
