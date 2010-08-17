package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Board;
import models.Column;
import models.Component;
import models.Component.ComponentRowh;
import models.Project;
import models.Snapshot;
import models.Sprint;
import models.Task;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;

public class SmartController extends Controller
{

	/**
	 * making actions before any action called in any controllers
	 * 
	 * @throws Throwable
	 */
	@Before
	public static void beforeActions() throws Throwable
	{
		renderArgs.put( "connected", Security.getConnected() );
		if( Security.isConnected() && Security.getConnected().deleted )
		{
			Secure.logout();
		}
if(!Sprint.Last.equals(new Date())){
	Sprint.Last=new Date();
		List<Sprint> sprints = Sprint.findAll();
		for( Sprint s : sprints )
		{
			Date now = Calendar.getInstance().getTime();
			if( s.endDate != null && s.endDate.before( now ) || s.ended == true )
			{
				s.ended = true;
				Project p = s.project;
				Board b = p.board;
				User user = Security.getConnected();
				List<Component> components = p.getComponents();

				ArrayList<ComponentRowh> data = new ArrayList<ComponentRowh>();
				List<Column> columns = b.columns;
				List<Column> CS = new ArrayList<Column>();
				ArrayList<String> Columnsofsnapshot = new ArrayList<String>();
				for( int i = 0; i < columns.size(); i++ )
				{
					if( columns.get( i ).onBoard == true )
					{
						CS.add( null );
						CS.set( i, columns.get( i ) );
					}
				}
				for( int i = 0; i < columns.size(); i++ )
				{
					if( columns.get( i ).onBoard == true )
					{
						CS.set( columns.get( i ).sequence, columns.get( i ) );
					}
				}
				for( int i = 0; i < CS.size(); i++ )
				{
					Columnsofsnapshot.add( null );
					Columnsofsnapshot.set( i, CS.get( i ).name );
				}
				for( int i = 0; i < components.size(); i++ )// for each
															// component
				// get
				// the tasks
				{
					data.add( null );
					if( components.get( i ).number != 0 )
					{
						data.add( null );
						data.set( i, new ComponentRowh( components.get( i ).id, components.get( i ).name ) );
						List<Task> tasks = components.get( i ).returnComponentTasks( s );

						for( int j = 0; j < CS.size(); j++ )
						{
							data.get( i ).add( null );
							data.get( i ).set( j, new ArrayList<String>() );
						}

						for( Task task : tasks )
						{
							Column pcol = new Column();
							for( int k = 0; k < task.taskStatus.columns.size(); k++ )
							{
								pcol = task.taskStatus.columns.get( k );
								if( pcol.board.id == b.id )
								{
									break;
								}
							}

							if( pcol.onBoard && !pcol.deleted && task.assignee!=null)
							{
								data.get( i ).get( CS.indexOf( pcol ) ).add( "T" + task.id + "-" + task.description + "-" + task.assignee.name );
							}
						}
					}
				}
				String type = "sprint " + s.id;
				Snapshot snap = new Snapshot();
				snap.user = user;
				snap.type = type;
				snap.board = b;
				snap.sprint = s;
				snap.data = data;
				snap.Columnsofsnapshot = Columnsofsnapshot;
				snap.save();
				s.finalsnapshot = snap;
				s.save();
				List<Component> Cs = p.components;
				for( int index = 0; index < Cs.size(); index++ )
				{

					Board b1 = Cs.get( index ).componentBoard;

					List<User> users = Cs.get( index ).getUsers();
					ArrayList<ComponentRowh> data1 = new ArrayList<ComponentRowh>();
					List<Column> columns1 = b.columns;
					ArrayList<String> Columnsofsnapshot1 = new ArrayList<String>();
					List<Column> CS1 = new ArrayList<Column>();
					for( int i = 0; i < columns1.size(); i++ )
					{
						if( columns1.get( i ).onBoard == true )
						{
							CS1.add( null );
							CS1.set( i, columns1.get( i ) );
						}
					}
					for( int i = 0; i < columns1.size(); i++ )
					{
						if( columns1.get( i ).onBoard == true )
						{
							CS1.set( columns1.get( i ).sequence, columns1.get( i ) );
						}
					}
					for( int i = 0; i < CS1.size(); i++ )
					{
						Columnsofsnapshot1.add( null );
						Columnsofsnapshot1.set( i, CS1.get( i ).name );
					}
					for( int i = 0; i < users.size(); i++ )// for each component
					// get
					// the tasks
					{
						data1.add( null );
						data1.set( i, new ComponentRowh( users.get( i ).id, users.get( i ).name ) );
						List<Task> tasks1 = users.get( i ).returnUserTasks( s, Cs.get( index ).id );

						for( int j = 0; j < CS1.size(); j++ )
						{
							data1.get( i ).add( null );
							data1.get( i ).set( j, new ArrayList<String>() );
						}

						for( Task task : tasks1 )
						{
							Column pcol = new Column();
							for( int k = 0; k < task.taskStatus.columns.size(); k++ )
							{
								pcol = task.taskStatus.columns.get( k );
								if( pcol.board.id == b.id )
								{
									break;
								}
							}
							if( pcol.onBoard == true && !pcol.deleted )
							{
								data.get( i ).get( CS.indexOf( pcol ) ).add( "T" + task.id + "-" + task.description + "-" + task.assignee.name );
							}
						}
					}
					user = Security.getConnected();

					Snapshot snap1 = new Snapshot();
					snap1.user = user;
					snap1.type = "sprint " + s.sprintNumber + " " + Cs.get( index ).name;
					snap1.board = b1;
					snap1.component = Cs.get( index );
					snap1.sprint = s;
					snap1.data = data1;
					snap1.Columnsofsnapshot = Columnsofsnapshot1;
					snap1.save();

				}

			}

		}}
	}

}
