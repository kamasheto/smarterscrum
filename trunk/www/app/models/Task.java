package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import notifiers.Notifications;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.mvc.Router;

@Entity
public class Task extends SmartModel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Description
	 */
	@Required
	@Lob
	@MaxSize( 300 )
	public String description;
	/**
	 * if true its deleted
	 */
	public boolean deleted;

	/**
	 * this task's estimation points.
	 */
	public double estimationPoints;

	/**
	 * Estimation points per day used in the backlog.
	 */
	public ArrayList<Double> estimationPointsPerDay;

	/**
	 * List of meetings that have many tasks associated to it.
	 */
	@ManyToMany( mappedBy = "tasks" )
	public List<Meeting> meeting;
	/**
	 * The user assigned to this task
	 */
	@OneToOne
	public User assignee;
	/**
	 * The user reported this task
	 */
	@OneToOne
	public User reporter;
	/**
	 * The user reviewing this task
	 */
	@OneToOne
	public User reviewer;
	/**
	 * The list of tasks depending on this task
	 */
	@ManyToMany
	public List<Task> dependentTasks;
	/**
	 * The task status
	 */
	@ManyToOne
	public TaskStatus status;
	/**
	 * The task type
	 */
	@ManyToOne
	public TaskType type;
	/**
	 * The sprint that have this task associated to it.
	 */
	@ManyToOne
	public Sprint sprint;

	/**
	 * The task number relative to this project.
	 */
	public int number;

	/**
	 * The List of comments on the task.
	 */
	@OneToMany( mappedBy = "task" )
	public List<Comment> comments;
	/**
	 * The comment added
	 */
	public String comment;
	/**
	 * The parent of the task
	 */
	@ManyToOne
	public Task parent;
	/**
	 * The list of tasks That has this task as a parent
	 */
	@OneToMany( mappedBy = "parent" )
	public List<Task> subTasks;
	/**
	 * The project of the task.
	 */
	@ManyToOne
	public Project project;
	/**
	 * the component if it belongs to one.
	 */
	@ManyToOne
	public Component component;
	/**
	 * The Success scenario of the task
	 */
	@Lob
	public String successScenario;
	/**
	 * The failure scenario of the task
	 */
	@Lob
	public String failureScenario;
	/**
	 * The priority of the task
	 */
	public int priority;
	/**
	 * The Product role thats used in the task.
	 */
	@ManyToOne
	public ProductRole productRole;

	/**
	 * Task deadline time stamp that is set by the assignee.
	 */
	public long deadline;

	// @ManyToOne
	// public Column Status_on_Board;

	// @ManyToMany
	// public List<Day> taskDays;

	/**
	 * Class constructor just initializing the lists for the task.
	 */

	public void init()
	{
		this.subTasks = new ArrayList<Task>();
		this.estimationPointsPerDay = new ArrayList<Double>();
		if( this.parent == null )
		{
			List<Task> tasks = Task.find( "byProjectAndParentIsNull", this.project ).fetch();
			this.number = tasks.size() + 1;
		}
		else
		{
			this.project = this.parent.project;
			this.component = this.parent.component;
			this.number = this.parent.subTasks.size() +1;
			for( Task task : this.parent.subTasks )
			{
				if( task.number >= this.number && !this.equals( task ) )
				{
					this.number = task.number + 1;
				}
			}
		}

		this.save();
	}

	/**
	 * Class constructor initializing (description, success scenarios, failure
	 * scenarios,priority , notes and reporter)
	 * 
	 * @param des
	 *            : description
	 * @param succ
	 *            : Success Scenario string
	 * @param fail
	 *            : Failure Scenario string
	 * @param priority
	 *            : Integer value for the priority
	 * @param notes
	 *            : The string of notes on this task
	 * @param userId
	 *            : The ID of the reporter of this task
	 */
	public Task( String des, String succ, String fail, int priority, String notes, long userId )
	{

		this.reporter = User.findById( userId );
		this.description = des;
		this.successScenario = succ;
		this.failureScenario = fail;
		this.priority = priority;
		this.comment = notes;
		this.dependentTasks = null;
		this.productRole = null;
		this.component = null;
		this.subTasks = new ArrayList<Task>();

		this.estimationPointsPerDay = new ArrayList<Double>();
	}

	/**
	 * Returns the effort points of a specific task in a specific day.
	 * 
	 * @author Hadeer Younis
	 * @category C4S8 and C4S9
	 * @param dayId
	 *            An Integer used to find a specific day by it's ID.
	 * @return The number of effort points for this task in a specific day.
	 */
	public double getEffortPerDay( int dayId )
	{
		if( estimationPointsPerDay.size() == 0 )
			return estimationPoints;
		if( dayId >= estimationPointsPerDay.size() )
			return estimationPointsPerDay.get( estimationPointsPerDay.size() - 1 );
		return estimationPointsPerDay.get( dayId );
	}

	/**
	 * Sets the effort points of a specific task in a specific day.
	 * 
	 * @author Hadeer Younis
	 * @category C4S1
	 * @param day
	 *            An Integer used to find a specific day by it's ID.
	 * @param effort
	 *            It is the number of effort point for the corresponding day.
	 */
	public void setEffortOfDay( double effort, int day )
	{
		if( estimationPointsPerDay.size() == 0 && day == 0 )
		{
			estimationPointsPerDay.add( effort );
		}
		else if( estimationPointsPerDay.size() == 0 && day > 0 )
		{
			while( estimationPointsPerDay.size() < day )
				estimationPointsPerDay.add( effort );
		}
		if( estimationPointsPerDay.size() <= day )
		{
			double temp = estimationPointsPerDay.get( estimationPointsPerDay.size() - 1 );
			for( int i = estimationPointsPerDay.size() - 1; i < day; i++ )
			{
				estimationPointsPerDay.add( temp );
			}
		}
		estimationPointsPerDay.set( day, effort );
	}

	/**
	 * Class constructor initializing the list of meetings ,dependent tasks and
	 * estimation points per day.
	 */
	public Task()
	{
		meeting = new ArrayList<Meeting>();
		dependentTasks = new ArrayList<Task>();
		this.estimationPointsPerDay = new ArrayList<Double>();
	}

	/**
	 * Class constructor initializing (description,estimation points and
	 * estimation points per day).
	 * 
	 * @param des
	 *            : description
	 * @param deleted
	 * @param estimationPoints
	 *            : estimation points of this task.
	 */
	public Task( String des, boolean deleted, double estimationPoints )
	{
		this();
		this.description = des;
		this.deleted = false;
		this.estimationPoints = estimationPoints;
		this.estimationPointsPerDay = new ArrayList<Double>();
		this.save();
	}

	/**
	 * Class constructor initializing (description, Task type to impediment,
	 * deleted to false dependent tasks estimation points per day lists, and the
	 * task status to new the project and the estimation points)
	 * 
	 * @param des
	 * @param project
	 */

	public Task( String des, Project project )
	{
		this();
		this.description = des;
		this.deleted = false;

		this.type = new TaskType();
		this.type.name = "Impediment";
		this.type.save();
		this.type.project = project;

		this.dependentTasks = new ArrayList<Task>();
		this.status = new TaskStatus();

		this.estimationPointsPerDay = new ArrayList<Double>( 1 );
		this.status = new TaskStatus().save();

		this.status.name = "New";
		this.status.save();
		this.status.project = project;

		this.estimationPoints = 0.0;

	}

	/**
	 * This method checks if a Task is Under Implementation Story 37 Component 3
	 * 
	 * @author Monayri
	 * @param void
	 * @return boolean
	 */
	public boolean checkUnderImpl()
	{
		Sprint taskSprint = this.sprint;
		if( taskSprint != null )
		{
			Date Start = taskSprint.startDate;
			Date End = taskSprint.endDate;
			Calendar cal = new GregorianCalendar();
			if( Start.before( cal.getTime() ) && End.after( cal.getTime() ) )
				return true;
		}

		return false;
	}

	/**
	 * A Method that deletes a Task
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return its a void method.
	 */
	public void DeleteTask()
	{
		Project project = this.project;
		for( Task task : project.projectTasks )
		{
			if( task.dependentTasks.contains( this ) )
				task.dependentTasks.remove( this );
			for( Task task2 : task.subTasks )
			{
				if( task2.dependentTasks.contains( this ) )
					task2.dependentTasks.remove( this );
				task2.save();
			}
			task.save();
		}
		Log.addUserLog( "Deleted task", this, this.project );
		ArrayList<User> users = new ArrayList<User>();
		if(this.assignee!=null)
			users.add(this.assignee);
		if(this.reviewer!=null)
			users.add(this.reviewer);
		if(this.reporter!=null)
			users.add(this.reporter);
		String url = Router.getFullUrl("Application.externalOpen")+"?id="+this.project.id+"&isOverlay=false&url=/tasks/view_task?project_id="+this.project.id;
		Notifications.notifyUsers( users, "deleted", url, "task", "task "+this.number, (byte)-1, this.project);
		this.deleted = true;
		this.save();
	}

	/**
	 * A Method that checks whether the Task has dependencies hence deletable.
	 * 
	 * @author Monayri
	 * @category C3 17.1
	 * @return it return a boolean variable that represents whether the task is
	 *         deletable.
	 */
	public boolean isDeletable()
	{
		Project project = this.project;
		List<Component> components = project.components;

		for( Component component : components )
		{
			for( Task task : component.componentTasks )
			{
				if( task.dependentTasks.contains( this ) )
				{
					for( Task task2 : this.subTasks )
					{
						if( task2.dependentTasks.contains( this ) )
						{
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public static class Object
	{

		long id;

		String description;

		public Object( long id, String description )
		{
			this.id = id;
			this.description = description;
		}
	}

	@Override
	public String toString()
	{
		return this.description;
	}

	/***
	 * Returns a string containing the product role (if available) & the
	 * description of a task
	 */
	public String getSummary()
	{
		String summary = "";
		if( this.productRole != null )
		{
			if( this.productRole.name.charAt( 0 ) == 'a' || this.productRole.name.charAt( 0 ) == 'e' || this.productRole.name.charAt( 0 ) == 'i' || this.productRole.name.charAt( 0 ) == 'o' || this.productRole.name.charAt( 0 ) == 'u' || this.productRole.name.charAt( 0 ) == 'A' || this.productRole.name.charAt( 0 ) == 'E' || this.productRole.name.charAt( 0 ) == 'I' || this.productRole.name.charAt( 0 ) == 'O' || this.productRole.name.charAt( 0 ) == 'U' )
			{
				summary = "As an " + this.productRole.name + "," + this.description;
			}
			else
			{
				summary = "As a " + this.productRole.name + "," + this.description;
			}
		}
		else
			summary = this.description;
		return summary;
	}

	/**
	 * A method that returns the number of the Task.
	 * 
	 * @return String
	 */
	public String getTaskNumber()
	{
		String number = "";
		if( this.parent != null )
		{
			number = this.parent.number + ".";
		}
		number += this.number;
		return number;
	}

	public List<User> getAssigneeOrReviewer( boolean ar )
	{

		List<User> u = this.component.componentUsers;
		if( ar )
		{
			u.remove( this.reviewer );
		}
		else
		{
			if(type!=null)
				u = Reviewer.find("byProjectAndAcceptedAndtaskType", project, true, type).fetch();
			u.remove( this.assignee );
		}
		return u;
	}
	
	/***
	 * Extracts the product role from the task description & saves it as 
	 * a new product role if its new, saves the task description & product role
	 * 
	 * @param newdesc: 
	 * 				task description
	 */
	public void getProductRole(String newdesc)
	{
		String[] desc = newdesc.split( "," );
		if( desc.length == 1 )
		{
			this.description = desc[0];
		}
		else
		{
			String[] desc2 = desc[0].split( " " );
			if( desc2.length >= 3 )
			{
				if( desc2[0].equalsIgnoreCase( "as" ) && (desc2[1].equalsIgnoreCase( "a" ) || desc2[1].equalsIgnoreCase( "an" )) )
				{
					boolean flag = false;
					String productrole = "";
					for( int k = 2; k < desc2.length; k++ )
					{
						if( k == desc2.length - 1 )
							productrole = productrole + desc2[k];
						else
							productrole = productrole + desc2[k] + " ";

					}
					for( int j = 0; j < this.project.productRoles.size(); j++ )
					{
						if( this.project.productRoles.get( j ).name.equalsIgnoreCase( productrole ) )
							flag = true;
					}
					if( !flag )
					{
						ProductRole pr = new ProductRole( this.project.id, productrole, "" );
						pr.save();						
						//Notifications.notifyProjectUsers(pr.project, "addProductRole", "", "product role", pr.name, (byte) 0);
						this.productRole = pr;
					}
					else
					{
						for( int j = 0; j < this.project.productRoles.size(); j++ )
						{
							if( this.project.productRoles.get( j ).name.equalsIgnoreCase( productrole ) )
							{
								this.productRole = this.project.productRoles.get( j );
							}
						}
					}
					for( int i = 1; i < desc.length; i++ )
					{
						this.description = desc[i] + " ";
					}
				}
			}
			else
			{
				this.description = newdesc;
				this.productRole = null;
			}
		}
	}
}
