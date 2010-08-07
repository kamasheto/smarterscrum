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
	/*
	 * Component Entity : consists of name,description up till now
	 */
	@Required
	public String name;

	@Lob
	@Required
	@MaxSize( 10000 )
	public String description;

	// deletion marker
	public boolean deleted;

	// Relation with Project Entity
	@ManyToOne
	@Required
	public Project project;

	// Relation with User Entity
	@ManyToMany( mappedBy = "components", cascade = CascadeType.ALL )
	public List<User> componentUsers;

	@OneToMany( mappedBy = "component", cascade = CascadeType.ALL )
	public List<Snapshot> snapshots;

	// Relation with Meeting Entity
	@ManyToMany( mappedBy = "components", cascade = CascadeType.ALL )
	public List<Meeting> componentMeetings;

	// Relation with Board Entity
	@OneToOne( mappedBy = "component" )
	public Board componentBoard;

	public int number;

	@OneToMany( mappedBy = "component", cascade = CascadeType.ALL )
	public List<Task> componentTasks;

	/***
	 * Component constructor
	 */
	public Component()
	{
		componentUsers = new ArrayList<User>();
		componentMeetings = new ArrayList<Meeting>();
		componentTasks = new ArrayList<Task>();
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
	 * Returns a list of the users in this component
	 * API for C3 & it's S28
	 * 
	 * @return List of component's users
	 */
	public List<User> getUsers()
	{
		return componentUsers;
	}

	/**
	 * Returns a list of tasks associated to a certain sprint for this component 
	 * Story: 4,5,6.
	 * 
	 * @author menna_ghoneim 
	 * @param s
	 * 		given a sprint
	 * @return
	 * 		List of tasks in this sprint of this component
	 */
	@SuppressWarnings( "null" )
	public List<Task> returnComponentSprintTasks( Sprint s )
	{

		List<Task> tasks = componentTasks;

		int tasksNo = tasks.size();

		int j = 0;

		for( int i = 0; i < tasksNo; i++ )
		{

			Task task = tasks.get( i - j );
			if( task.taskSprint != s || task.deleted )
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
	 * 		 sprint
	 * 
	 */
	public List<Task> componentSprintTasks( Sprint s )
	{
		List<Task> t = new ArrayList<Task>();
		for( int i = 0; i < componentTasks.size(); i++ )
		{
			if( componentTasks.get( i ).taskSprint != null )
			{
				if( componentTasks.get( i ).taskSprint.id == s.id )
				{
					t.add( componentTasks.get( i ) );
				}
			}
		}
		return t;
	}

	/**
	 * Returns a list of tasks associated to a certain sprint for a certain component
	 * 
	 * @author Hadeer Diwan 
	 * @param s
	 * 		given a sprint
	 * @return 
	 * 		List of tasks in this sprint of this component
	 */
	public List<Task> returnComponentTasks( Sprint s )
	{

		List<Task> t = new ArrayList<Task>();
		for( int i = 0; i < componentTasks.size(); i++ )
		{
			if( componentTasks.get( i ).taskSprint != null )
			{
				if( componentTasks.get( i ).taskSprint.id == s.id )
				{
					t.add( componentTasks.get( i ) );
				}
			}
		}
		return t;
	}

	/**
	 * Deletes the Component
	 * 
	 * @author Amr Hany
	 * @return boolean varaiable the shows if the component is deleted
	 *         successfully or not
	 */
	public boolean deleteComponent()
	{
		if( this.deleted == false )
		{
			this.deleted = true;
			this.save();
			return true;
		}
		return false;

	}

	/**
	 * Meeting status method returns the status of the component in attending the meeting 
	 * either 'all invited' or 'confirmed' or 'waiting' or 'declined' or 'not invited'
	 * 
	 * @author 
	 * 		Amr Hany
	 * @param meetingID
	 * 			meeting ID
	 * @return 
	 * 			the status of the meeting
	 */
	public String meetingStatus( long meetingID )
	{
		boolean confirmed = true;
		for( User user : this.componentUsers )
		{
			if( !user.deleted )
				if( !user.meetingStatus( meetingID ).equals( "confirmed" ) )
				{
					confirmed = false;
					break;
				}
		}
		if( confirmed )
			return "confirmed";

		boolean waiting = true;
		for( User user : this.componentUsers )
		{
			if( !user.deleted )
				if( !user.meetingStatus( meetingID ).equals( "waiting" ) )
				{
					waiting = false;
					break;
				}
		}
		if( waiting )
			return "waiting";

		boolean declined = true;
		for( User user : this.componentUsers )
		{
			if( !user.deleted )
				if( !user.meetingStatus( meetingID ).equals( "declined" ) )
				{
					declined = false;
					break;
				}
		}
		if( declined )
			return "declined";

		for( User user : this.componentUsers )
		{
			if( !user.deleted )
				if( user.meetingStatus( meetingID ).equals( "notInvited" ) )
					return "notInvited";
		}
		return "allInivited";

	}

	
	/***
	 * Creates component board & sets the component number
	 */
	public void init()
	{
		componentBoard = new Board( this ).save();
		for( int i = 0; i < project.board.columns.size(); i++ )
		{
			Column c = new Column( project.board.columns.get( i ).name, componentBoard, project.board.columns.get( i ).taskStatus );
			c.save();
		}
		if( this.project == null )
		{
			this.number = 1;
		}
		else
		{
			this.number = this.project.components.size();
			for( Component component : this.project.components )
			{
				if( component.number >= this.number && !component.equals( this ) )
				{
					this.number = component.number + 1;
				}
			}
		}

		this.save();

	}

	/***
	 * Class used to store 2 dimensional ArrayList of tasks to be rendered to the project board
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
	 * Class used to store 2 dimensional ArrayList of tasks to be rendered to the component board
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

}
