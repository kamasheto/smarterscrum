package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.MaxSize;
import play.data.validation.Required;

@Entity
public class Component extends SmartModel
{
	/**
	 * Component Entity : consists of name,description up till now
	 */

	/***
	 * component name
	 */
	@Required
	public String name;

	/***
	 * component description
	 */
	@Lob
	@MaxSize( 10000 )
	public String description;

	/***
	 * a flag that determines whether the component is deleted or not
	 */
	public boolean deleted;

	/***
	 * each project can have many components & each component belongs only to
	 * one project
	 */
	@ManyToOne
	@Required
	public Project project;

	/***
	 * a user can be in many components & a component can have many users
	 */
	@ManyToMany( mappedBy = "components", cascade = CascadeType.ALL )
	public List<User> users;

	/***
	 * a list of component snapshots
	 */
	@OneToMany( mappedBy = "component", cascade = CascadeType.ALL )
	public List<Snapshot> snapshots;

	/***
	 * a component can have many meetings & a meeting can include many
	 * components
	 */
	@ManyToMany( mappedBy = "components", cascade = CascadeType.ALL )
	public List<Meeting> meetings;

	/***
	 * a component has only one board
	 */
	@OneToOne( mappedBy = "component" )
	public Board board;

	/***
	 * component number
	 */
	public int number;

	/***
	 * a component can have many tasks & a task can belong to only one component
	 */
	@OneToMany( mappedBy = "component", cascade = CascadeType.ALL )
	public List<Task> tasks;

	/***
	 * Component constructor that initializes a list of users, meetings & tasks
	 * for the component
	 */
	public Component()
	{
		users = new ArrayList<User>();
		meetings = new ArrayList<Meeting>();
		tasks = new ArrayList<Task>();
	}

	/***
	 * Overrides toString() method
	 */
	@Override
	public String toString()
	{
		return name;
	}

	/**
	 * Returns a list of the users in this component API for C3 & it's S28
	 * 
	 * @return List of component's users
	 */
	public List<User> get_users()
	{
		return users;
	}

	/**
	 * Returns a list of tasks associated to a certain sprint for this component
	 * Story: 4,5,6.
	 * 
	 * @author menna_ghoneim
	 * @param s
	 *            given a sprint
	 * @return List of tasks in this sprint of this component
	 */
	@SuppressWarnings( "null" )
	public List<Task> component_sprint_tasks( Sprint s )
	{

		List<Task> tasks = this.tasks;

		int j = 0;

		for( int i = 0; i < tasks.size(); i++ )
		{

			Task task = tasks.get( i - j );
			if( task.sprint != s || task.deleted )
			{
				tasks.remove( task );
				j++;
			}
		}

		return tasks;
	}

	/***
	 * Returns list of tasks in sprint s in this component
	 * 
	 * @param s
	 *            sprint
	 */
	public List<Task> comp_sprint_not_parent_tasks( Sprint s )
	{
		List<Task> task = new ArrayList<Task>();
		for( int i = 0; i < tasks.size(); i++ )
		{
			if( tasks.get( i ).sprint != null )
			{
				if( tasks.get( i ).sprint.id == s.id && (tasks.get(i).subTasks==null || tasks.get(i).parent!=null) )
				{
					task.add( tasks.get( i ) );
				}
			}
		}
		return task;
	}

	/**
	 * Deletes the component by setting its deletion marker to true
	 * 
	 * @author Amr Hany
	 * @return boolean varaiable the shows if the component is deleted
	 *         successfully or not
	 */
	public boolean delete_component()
	{
		if( this.deleted == false )
		{
			this.deleted = true;
			this.save();
			for(Task task: this.tasks)
			{
				task.deleted = true;
				task.save();
			}
			return true;
		}
		return false;

	}

	/**
	 * Meeting status method returns the status of the component in attending
	 * the meeting either 'all invited' or 'confirmed' or 'waiting' or
	 * 'declined' or 'not invited'
	 * 
	 * @author Amr Hany
	 * @param meeting_id
	 *            meeting ID
	 * @return the status of the meeting
	 */
	public String meeting_status( long meeting_id )
	{
		boolean confirmed = true;
		for( User user : this.users )
		{
			if( !user.deleted )
				if( !user.meetingStatus( meeting_id ).equals( "confirmed" ) )
				{
					confirmed = false;
					break;
				}
		}
		if( confirmed )
			return "confirmed";

		boolean waiting = true;
		for( User user : this.users )
		{
			if( !user.deleted )
				if( !user.meetingStatus( meeting_id ).equals( "waiting" ) )
				{
					waiting = false;
					break;
				}
		}
		if( waiting )
			return "waiting";

		boolean declined = true;
		for( User user : this.users )
		{
			if( !user.deleted )
				if( !user.meetingStatus( meeting_id ).equals( "declined" ) )
				{
					declined = false;
					break;
				}
		}
		if( declined )
			return "declined";

		for( User user : this.users )
		{
			if( !user.deleted )
				if( user.meetingStatus( meeting_id ).equals( "notInvited" ) )
					return "notInvited";
		}
		return "allInivited";

	}

	/***
	 * Creates component board & sets the component number
	 */
	public void init()
	{
		board = new Board( this ).save();
		for( int i = 0; i < project.board.columns.size(); i++ )
		{
			BoardColumn c = new BoardColumn( project.board.columns.get( i ).name, board, project.board.columns.get( i ).task_status );
			c.save();
		}
		this.number = this.project.components.size() - 1;
		for( Component component : this.project.components )
		{
			if( component.number >= this.number && !component.equals( this ) )
			{
				this.number = component.number + 1;
			}
		}

		this.save();

	}

	/***
	 * Class used to store 2 dimensional ArrayList of tasks to be rendered to
	 * the project board
	 */
	public static class ComponentRowh extends ArrayList<ArrayList<String>>
	{
		long id;
		public String title;

		public ComponentRowh( long id, String title )
		{
			this.id = id;
			this.title = title;
		}
	}

	/***
	 * Class used to store 2 dimensional ArrayList of tasks to be rendered to
	 * the component board
	 */
	public static class ComponentRow extends ArrayList<ArrayList<Task>>
	{
		long id;
		String title;

		public ComponentRow( long id, String title )
		{
			this.id = id;
			this.title = title;
		}
	}

	/**
	 * Returns the name of this component in the format: C1: User and Roles
	 */
	public String get_full_name()
	{
		return "C" + number + ": " + name;
	}

	public boolean has_running_games()
	{
		List<Game> games = Game.find( "byComponent", this ).fetch();
		for( Game game : games )
		{
			if( !game.getRound().isDone() )
			{
				return true;
			}
		}
		return false;
	}
	public int has_users()
	{
		int count = 0;
		for(User user:this.users)
		{
			if(!user.deleted)
				count++;
		}
		return count;
	}
}
