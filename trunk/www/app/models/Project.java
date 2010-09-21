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
@Entity
public class Project extends SmartModel
{
	@Column( unique = true )
	@Required
	public String name;

	@Lob
	/**
	 * project's description
	 */
	public String description;

	/**
	 * whether the project is deleted
	 */
	public boolean deleted;

	/**
	 * whether the project is private
	 */

	public boolean isPrivate;

	/**
	 * whether it's a scrum project
	 */
	public boolean isScrum;

	/**
	 * whether the project was approved
	 */
	public boolean approvalStatus = false;

	/**
	 * Project creator
	 */
	public User user;

	/* One To Many Relations */
	/**
	 * project's product roles
	 */
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<ProductRole> productRoles;

	/**
	 * project's meetings
	 */
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<Meeting> meetings;

	/**
	 * project's roles
	 */
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<Role> roles;

	/**
	 * project's components
	 */
	@OneToMany( mappedBy = "project" )
	public List<Component> components;
	/**
	 * project's sprints
	 */
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<Sprint> sprints;

	// Added in Sprint 2 by Galal Aly
	/**
	 * project's stories' priorities
	 */
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<Priority> priorities;

	/**
	 * project's tasks statuses
	 */
	// Added in Sprint 2 by Monayri
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<TaskStatus> taskStatuses;

	/**
	 * project's tasks' types
	 */
	// Added in Sprint 2 by Monayri
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<TaskType> taskTypes;

	/**
	 * userNotificationProfiles associated with the project.
	 * 
	 * @author Amr Tj.Wallas
	 * @see models.UserNotificationProfile
	 * @since Sprint2.
	 * @Task C1S33
	 */
	@OneToMany( mappedBy = "project" )
	public List<UserNotificationProfile> userNotificationProfiles;

	/**
	 * requests associated with the project.
	 * 
	 * @author Amr Tj.Wallas
	 * @see models.Request
	 * @since Sprint2.
	 * @Task C1S14
	 */
	@OneToMany( mappedBy = "project" )
	public List<Request> requests;

	@OneToMany( mappedBy = "project" )
	public List<Reviewer> reviewers;

	/* One To One Relations */

	/**
	 * the project's board
	 */
	@OneToOne( mappedBy = "project" )
	public Board board;

	/**
	 * project's notification profile
	 */
	@OneToOne( mappedBy = "project", cascade = CascadeType.ALL )
	public ProjectNotificationProfile notificationProfile;

	/**
	 * project's chatroom
	 */
	@OneToOne
	public ChatRoom chatroom;

	/**
	 * project's notifications
	 */
	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL )
	public List<Notification> notifications;

	/**
	 * users in the project
	 */
	@ManyToMany( mappedBy = "projects", cascade = CascadeType.ALL )
	public List<User> users;

	/**
	 * tasks that belong to the project
	 */
	@OneToMany( mappedBy = "project" )
	public List<Task> projectTasks;
	/*                       */

	/**
	 * project's meetings' types
	 */
	public ArrayList<String> meetingsTypes;

	/**
	 * project's meetings' types in a sprint
	 */
	public ArrayList<Boolean> meetingsTypesInSprint;
	/**
	 * 
	 */
	public boolean autoReschedule;
	/**
	 * 
	 */
	public boolean autoNotify;
	/**
	 * default sprint duration
	 */
	public int sprintDuration;
	/**
	 * project's effort estimation unit
	 */
	public int effortEstimationUnit;

	/**
	 * initializes the projects' instance variables
	 */
	public Project()
	{
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
		this.projectTasks = new ArrayList<Task>();
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
	public Project( String projectName, String projectDescription, User projectAdmin, int numberOfComponents, int numberOfSprints )
	{
		this();
		this.deleted = false;
		this.approvalStatus = false;
		this.name = projectName;
		this.description = projectDescription;
		// this.numberOfComponents = numberOfComponents;
		// this.numberOfSprints = numberOfSprints;
		// init();

	}

	/**
	 * Creates a project with name and description
	 * 
	 * @param name
	 *            the name or the title of the project
	 * @param desc
	 *            the description of the project
	 */
	public Project( String name, String desc )
	{
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
	public List<Task> getTasks()
	{

		List<Component> Components = this.components;
		List<Task> ProjectTasks = new ArrayList<Task>();
		for( int i = 0; i < Components.size(); i++ )
		{
			Component X = Components.get( i );
			List<Task> componentsTasks = X.componentTasks;
			projectTasks.addAll( componentsTasks );
		}
		return ProjectTasks;
	}

	/**
	 * gets the roles of the project
	 * 
	 * @return list of roles of a project
	 */
	public List<Role> getRoles()
	{
		return roles;
	}

	/**
	 * this method returns a list of the components in this project requested by
	 * C1S24, C3S9, C3S11, C3S18, C5S1
	 * 
	 * @author Ghada Fakhry
	 * @return : List of component's users
	 */
	public List<Component> getComponents()
	{
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
	public String fetchData( long cid )
	{
		String data = "";
		String FULL = "GenerateFullGraph([";
		String names = "[";
		if( sprints.size() == 0 )
			return "''";
		for( int i = 0; i < sprints.size(); i++ )
		{
			if( sprints.get( i ).tasks.size() > 0 )
			{
				if( i == 2 )
				{
					FULL = FULL + sprints.get( i ).getCoordinatesOfData( cid );
					names = names + "{label:'Sprint: " + sprints.get( i ).sprintNumber + "'}";
				}
				else
				{
					FULL = FULL + sprints.get( i ).getCoordinatesOfData( cid ) + ",";
					names = names + "{label:'Sprint: " + Long.parseLong( sprints.get( i ).sprintNumber ) + "'},";
				}
				data = data + ("GenerateGraph(" + sprints.get( i ).fetchData( cid ) + ",'c_" + sprints.get( i ).sprintNumber + "');" + '\n');

			}
		}
		// FULL = FULL+"],";
		FULL = FULL + "]," + names + "]);";
		return FULL + data;

	}

	/**
	 * Get all the users in this project
	 * 
	 * @return all users in this project
	 */
	public List<User> getUsers()
	{

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
	public String toString()
	{
		return name;

	}

	/**
	 * @author minazaki i just modified the insprint to my need it checks if the
	 *         start date of a sprint overlaps with any sprints OR the end date
	 *         overlaps with any sprints
	 * @param start
	 *            the start date of the sprint
	 * @param endate
	 *            the end date of the sprint
	 * @return boolean
	 */
	public boolean inSprint( Date start, Date end )
	{
		for( Sprint sprint : sprints )
		{
			if( (start.after( sprint.startDate ) && end.before( sprint.endDate )) || (start.before( sprint.startDate ) && end.after( sprint.endDate )) || (start.before( sprint.startDate ) && end.after( sprint.startDate )) || (start.before( sprint.endDate ) && end.after( sprint.endDate )) )
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
	public long runningSprint()
	{
		Date now = Calendar.getInstance().getTime();
		for( int i = 0; i < sprints.size(); i++ )
		{
			if( (sprints.get( i ).startDate.before( now )) && (sprints.get( i ).endDate.after( now )) )
			{
				return sprints.get( i ).id;
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
	public boolean inSprint( Date givenDate )
	{
		int i = 0;
		for( Sprint sprint : sprints )
		{
			if( (givenDate.after( sprint.startDate ) && givenDate.before( sprint.endDate )) )
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
	public Component init()
	{
		// this.save();
		board = new Board( this ).save();

		// chat room added by amr hany
		chatroom = new ChatRoom().save();

		List<Role> roles = Role.find( "select r from Role r where r.project = null" ).fetch();
		for( Role role : roles )
		{
			Role r = new Role();
			this.roles.add( r );
			r.name = role.name;
			r.baseRole = role.baseRole;
			r.project = this;
			r.permissions.addAll( role.permissions );
			r.save();
		}

		// automatically create a project notification profile once the project
		// is created
		this.notificationProfile = new ProjectNotificationProfile( this ).save();

		// username = Security.getConnected() != null ?
		// Security.getConnected().email : "";

		meetingsTypes = new ArrayList<String>();
		meetingsTypesInSprint = new ArrayList<Boolean>();

		TaskType t1 = new TaskType();
		t1.project = this;
		t1.name = "Implementation";
		t1.save();
		taskTypes.add( t1 );

		TaskType t2 = new TaskType();
		t2.project = this;
		t2.name = "Test";
		t2.save();
		taskTypes.add( t2 );

		TaskType t3 = new TaskType();
		t3.project = this;
		t3.name = "Documentation";
		t3.save();
		taskTypes.add( t3 );

		TaskType t4 = new TaskType();
		t4.project = this;
		t4.name = "Impediment";
		t4.save();
		taskTypes.add( t4 );

		TaskType t5 = new TaskType();
		t5.project = this;
		t5.name = "Other";
		t5.save();
		taskTypes.add( t5 );

		TaskStatus s1 = new TaskStatus();
		s1.project = this;
		s1.name = "New";
		s1.isNew = true;
		s1.save();
		s1.init();
		taskStatuses.add( s1 );

		TaskStatus s2 = new TaskStatus();
		s2.project = this;
		s2.name = "Started";
		s2.isNew = false;
		s2.save();
		s2.init();
		taskStatuses.add( s2 );

		TaskStatus s3 = new TaskStatus();
		s3.project = this;
		s3.name = "Pending";
		s3.isNew = false;
		s3.pending = true;
		s3.closed = false;
		s3.save();
		s3.init();
		taskStatuses.add( s3 );

		TaskStatus s4 = new TaskStatus();
		s4.project = this;
		s4.name = "Reopened";
		s4.isNew = false;
		s4.save();
		s4.init();
		taskStatuses.add( s4 );

		TaskStatus s5 = new TaskStatus();
		s5.project = this;
		s5.isNew = false;
		s5.name = "Verified";
		s5.closed = true;
		s5.pending = false;
		s5.save();
		s5.init();
		taskStatuses.add( s5 );

		meetingsTypes.add( "Scrum Meeting" );
		meetingsTypes.add( "Plan Meeting" );

		meetingsTypesInSprint.add( true );
		meetingsTypesInSprint.add( true );

		sprintDuration = 14;

		Component defaultComponent = new Component().save();
		defaultComponent.name = "default component";
		this.addComponent( defaultComponent );
		defaultComponent.save();
		defaultComponent.init();

		this.save();
		return defaultComponent;
	}

	public void init( boolean isScrum )
	{
		init();
		// Component defaultComponent = init();
		// if( !isScrum )
		// {
		// 	// Default creations in case not a scrum project
		// 
		// 	// Sprint defaultSprint = new Sprint().save();
		// 	// defaultSprint
		// 	// defaultSprint.project = this;
		// 	// defaultSprint.save();
		// }
		// 
		// this.save();
	}

	/**
	 * @author mahmoudsakr
	 */
	public static class Object
	{
		long id;

		String name;

		public Object( long id, String name )
		{
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

	public static boolean isUnique( String name )
	{
		List<Project> projects = Project.find( "name='" + name + "'" ).fetch();
		for( Project p : projects )
		{
			if( p.name.equalsIgnoreCase( name ) )
			{
				return false;
			}
		}
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
	public static boolean userRequstedProjectBefore( Long userId, String projectname )
	{
		List<Project> p = Project.find( "name='" + projectname + "' and " + " user.id='" + userId + "' and approvalStatus=false and deleted=false" ).fetch();

		if( p.isEmpty() )
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
	public boolean hasRunningSprints()
	{
		long now = new Date().getTime();
		List<Sprint> Sprints = Sprint.find( "byProject", this ).fetch();
		for( Sprint sprint : Sprints )
		{
			if( sprint.startDate != null && sprint.endDate != null && now > sprint.startDate.getTime() && now < sprint.endDate.getTime() )
			{
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
	public void removeUser( User user )
	{
		this.users.remove( user );
		this.save();
	}

	/**
	 * method to return all the upcoming and current sprints
	 * 
	 * @author minazaki
	 * @return list of current and upcoming sprints
	 */
	public ArrayList<Sprint> upcomingSprints()
	{
		Date today = new Date();
		ArrayList<Sprint> list = new ArrayList<Sprint>();
		for( int i = 0; i < sprints.size(); i++ )
		{
			if( sprints.get( i ).startDate.after( today ) )
				list.add( sprints.get( i ) );
		}
		if( runningSprint() != -1 )
		{
			Sprint running = Sprint.findById( runningSprint() );
			list.add( running );
		}
		return list;
	}

	/**
	 * to return meetings that are associated to the end of sprint
	 * 
	 * @author minazaki
	 * @return list of meetings associated to end of sprint
	 */
	public List<Meeting> meetingsAssoccToEndOfSprint( Sprint sprint )
	{
		ArrayList<Meeting> temp = new ArrayList<Meeting>();

		for( Meeting meeting : meetings )
		{
			if( meeting.sprint != null && meeting.sprint == sprint )
			{
				int index = meetingsTypes.indexOf( meeting.type );
				if( index >= 0 && meetingsTypesInSprint.get( index ) )
				{
					temp.add( meeting );
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
	public List<String> meetingTypes()
	{
		ArrayList<String> temp = new ArrayList<String>();
		for( int i = 0; i < meetingsTypes.size(); i++ )
		{
			if( meetingsTypesInSprint.get( i ) )
				temp.add( meetingsTypes.get( i ) );
		}
		return temp;
	}

	/**
	 * Adds a component to this project
	 * 
	 * @param c
	 *            the component to be added
	 */
	public void addComponent( Component c )
	{
		this.components.add( c );
		c.project = this;
		c.save();
	}

	/**
	 * Gets the number of non-deletion requests in this project
	 * 
	 * @return the number of non-deletion requests in this project
	 */
	public int getNumberOfRequests()
	{
		List<Request> requests = Request.find( "byIsDeletionAndProject", false, this ).fetch();
		return requests.size();
	}

	/**
	 * Gets the number of deletion requests in this project
	 * 
	 * @return the number of deletion requests in this project
	 */
	public int getNumberOfDeletionRequests()
	{
		List<Request> requests = Request.find( "byIsDeletionAndProject", true, this ).fetch();
		return requests.size();
	}

	/**
	 * Gets the number of all requests in a project
	 * 
	 * @return the number of all requests in a project
	 */
	public int getNumberOfTotalRequests()
	{
		List<Request> requests = Request.find( "byIsDeletionAndProject", false, this ).fetch();
		List<Request> drequests = Request.find( "byIsDeletionAndProject", true, this ).fetch();
		return requests.size() + drequests.size();
	}

	/**
	 * This method returns the recent activity of this project on a scale of
	 * 0-10, 0 being least active and 10 being most active, based on the number
	 * of recent actions taken place (extracted information from logs per
	 * project, in the last 10 days)
	 * 
	 * @author mahmoudsakr
	 */
	public int activity()
	{
		// System.out.println(logs);
		int s = logs.size() / 10;
		return s > 10 ? 10 : s < 0 ? 0 : s;
	}
	
	/***
	 * Returns number of undeleted components in a project
	 */
	public int hasComponents()
	{
		int count = 0;
		for(Component component:this.components)
		{
			if(!component.deleted)
				count++;
		}
		return count;
	}
	
	/***
	 * Returns number of undeleted task types in a project
	 */
	public int hasTypes()
	{
		int count = 0;
		for(TaskType type:this.taskTypes)
		{
			if(!type.deleted)
				count++;
		}
		return count;
	}
	
	/***
	 * Returns number of undeleted users in a project
	 */
	public int hasUsers()
	{
		int count = 0;
		for(User user:this.users)
		{
			if(!user.deleted)
				count++;
		}
		return count;
	}
	
	/***
	 * Returns number of undeleted task priorities in a project
	 */
	public int hasPriorities()
	{
		int count = 0;
		for(Priority priority : this.priorities)
		{
			if(!priority.deleted)
				count++;
		}
		return count;
	}

	/***
	 * Returns number of undeleted tasks in a project
	 */
	public int hasTasks()
	{
		int count = 0;
		for(Task task : this.projectTasks)
		{
			if(!task.deleted)
				count++;
		}
		return count;
	}
}
