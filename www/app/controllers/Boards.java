package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import others.MeetingUsers;

import models.Board;
import models.Column;
import models.Component;
import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.Sprint;
import models.Task;
import models.User;
import models.Component.ComponentRow;
import play.mvc.With;

@With( Secure.class )
public class Boards extends CRUD
{
	/**
	 * Renders the data and the columnsOfBoard to be used by the views. this method is
	 * used to fill the 2D array attribute in the board with the data that
	 * should be represented in each column of the project board
	 * (components and their tasks and stories for a specific sprint)
	 * 
	 * @author Hadeer_Diwan
	 * @param SprintID
	 *            the sprint id
	 */

	public static void loadBoard( long sprintID )
	{
		Sprint s = Sprint.findById( sprintID );
		Project p = s.project;
		Board b = p.board;
		List<Component> components = p.getComponents();
		ArrayList<ComponentRow> data = new ArrayList<ComponentRow>();
		List<Column> columnsOfBoard = b.columns;
		columnsOfBoard = orderColumns( columnsOfBoard );
		for( int i = 0; i < components.size(); i++ )
		{
			data.add( null );
			data.set( i, new ComponentRow( components.get( i ).id, components.get( i ).name ) );
			List<Task> tasks = components.get( i ).returnComponentTasks( s );

			for( int j = 0; j < columnsOfBoard.size(); j++ )
			{
				data.get( i ).add( null );
				data.get( i ).set( j, new ArrayList<Task>() );
			}
			for( Task task : tasks )
			{
				data.get( i ).get( columnsOfBoard.indexOf( task.taskStatus.column ) ).add( task );
			}
		}

		/**
		 * * @author asmaak89
		 * 
		 * @param meetings
		 *            list of project p is used code:- linkedList to save the
		 *            meetings that it's time is in current time and then add to
		 *            another Linkedlist users of all meetings running it render
		 *            this list of Users in views-Boards- loadBoard.html
		 */

		
		
 

		LinkedList<Meeting> total = new LinkedList<Meeting>();

	//	ArrayList<MeetingUsers> u = new ArrayList<MeetingUsers>();
		for( Meeting m : p.meetings )
		{
			long now = new Date().getTime();
			if( m.startTime < now && m.endTime > now )
			{
				total.add( m );
			}
    	}
		
		ArrayList<ArrayList<User>> u = new ArrayList<ArrayList<User>>();
		for( int i = 0; i < total.size(); i++ )
		{
			Meeting m = Meeting.findById( total.get( i ).id );

			u.add(new MeetingUsers(m));
			for (MeetingAttendance k : m.users) 
			{
				if (k.status.equals("confirmed")) 
				{
					u.get(i).add(k.user);
				}

			}
			
		}
		render( data, columnsOfBoard,u,b,s,p,total);

			}

	/**
	 * order any list of columns and return the ordered list.
	 * 
	 * @author Josephhajj
	 * @param cols
	 */


	public static List<Column> orderColumns( List<Column> cols )
	{
		int smallest;
		Column temp;
		for( int i = 0; i < cols.size(); i++ )
		{
			smallest = i;
			for( int j = i + 1; j < cols.size(); j++ )
			{
				if( cols.get( smallest ).sequence > cols.get( j ).sequence )
				{
					smallest = j;

				}

			}
			temp = cols.get( smallest );
			cols.set( smallest, cols.get( i ) );
			cols.set( i, temp );
		}
		return cols;
	}
	/**
	 * Renders the data and the cols to be used by the views. this method is
	 * used to fill the 2D array attribute in the board with the data that
	 * should be represented in each column of the component board
	 * (users and their tasks and stories for a specific sprint)
	 * 
	 * @author Hadeer_Diwan
	 * @param sprintID
	 *            the sprint id
	 * @param componentID
	 *             the component id
	 */
	public static void loadComponentBoard( long sprintID, long componentID )
	{
		Sprint s = Sprint.findById( sprintID );
		Project p = s.project;
		Board b = p.board;

		Component c = Component.findById( componentID );
		List<User> users = c.getUsers();
        ArrayList<User>u=new ArrayList<User>(); 
		ArrayList<ComponentRow> data = new ArrayList<ComponentRow>();
		List<Column> columnsOfBoard = b.columns;
		columnsOfBoard = orderColumns( columnsOfBoard );
		for( int i = 0; i < users.size(); i++ )
		{
			data.add( null );
			data.set( i, new ComponentRow( users.get( i ).id, users.get( i ).name ) );
			List<Task> tasks = users.get( i ).returnUserTasks( s, componentID );

			for( int j = 0; j < columnsOfBoard.size(); j++ )
			{
				data.get( i ).add( null );
				data.get( i ).set( j, new ArrayList<Task>() );
			}

			for( Task task : tasks )
			{
				data.get( i ).get( columnsOfBoard.indexOf( task.taskStatus.column ) ).add( task );
			}
		
		}

		
		
		
		/**
		 * next lines indicates how current Component meeting is held infront of each component 
		 * board . as it user Component c and it's list of Users to search for which meeting 
		 * running now and which User have status Confirmed.
		 * @param    
		 *    Component c (Used thought code lines), List of componentMeetings,
		 *    List of Users for this Component . 
		 * @author asmaak89
		 */
		
		
		
		 LinkedList<Meeting> total = new LinkedList<Meeting>();
		 
			
             for (Meeting m : c.componentMeetings) {
				long now = new Date().getTime();
				if( m.startTime < now && m.endTime > now ) {
					
					total.add(m);
				
				}
			}
			for (int i = 0; i < total.size(); i++) {
				Meeting m = Meeting.findById(total.get(i).id);
				
				for (MeetingAttendance k : m.users) {
						if (k.status.equals("confirmed")) {
							u.add(k.user);
						}
					}
		
		}


		render( data,columnsOfBoard,u,s,c,p,total);
	}
}