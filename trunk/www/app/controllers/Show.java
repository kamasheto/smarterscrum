package controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import models.Board;
import models.ChatRoom;
import models.Project;
import models.Request;
import models.Role;
import models.Task;
import models.User;
import play.mvc.With;

@With( Secure.class )
public class Show extends SmartController
{

	/**
	 * renders the roles in a certain project to be used in the views.
	 * 
	 * @param id
	 *            the role id
	 */
	public static void roles( long id )
	{
		User user = Security.getConnected();
		Project project = Project.findById( id );
		List<Role> roles = null;
		if( project == null )
		{
			Security.check( user.isAdmin );
			roles = Role.find( "byProjectIsNull" ).fetch();
		}
		else
		{
			// Security.check(project, "manageRoles");
			roles = project.roles;
		}
		List<Request> requests = Request.find( "byUser", user ).fetch();
		ArrayList<Role> requestedRoles = new ArrayList<Role>();
		for( Request request : requests )
		{
			requestedRoles.add( request.role );
		}
		render( project, roles, user, requestedRoles );
	}

	public static void index()
	{
		users( 0 );
	}

	/**
	 * render the users in the system in a paginated way
	 * 
	 * @param page
	 *            the page of users
	 */
	public static void users( int page )
	{
		if( page < 0 )
		{
			page = 0;
		}
		List<User> users = User.find( "byDeleted", false ).from( page * 20 ).fetch( 20 );
		long total = User.count();
		render( users, page, total );
	}

	/**
	 * shows the projects in the system in a paginated way
	 * 
	 * @param page
	 *            the page of projects
	 */
	public static void projects( int page )
	{
		List<Project> projects = Project.find( "byDeleted", false ).from( page * 20 ).fetch( 20 );
		long total = Project.count();
		render( projects, page, total );
	}

	public static void boards()
	{
		List<Board> boards = Board.find( "byDeleted", false ).fetch();
		render( boards );
	}

	/**
	 * Show user profile
	 * 
	 * @param id
	 *            user id
	 */
	public static void user( long id )
	{
		User user = User.findById( id );
		if( user == null || user.deleted )
		{
			notFound();
		}
		User me = Security.getConnected();
		List<Project> myProjects = new LinkedList<Project>();
		for( Project project : Project.<Project> findAll() )
		{
			if( me.in( project ).can( "invite" ) )
				myProjects.add( project );
		}
		render( user, myProjects, me );
	}

	/**
	 * shows the workspace of a project
	 * 
	 * @param id
	 *            the project id
	 */
	public static void workspace( long id )
	{
		if (id < 0) {
			Security.check(Security.getConnected().isAdmin);
			render();
		}
		Project proj = Project.findById( id );
		ChatRoom room = proj.chatroom;
		int chatters = 0;
		User user = Security.getConnected();
		for( User U : proj.users )
		{
			if( U.openChats.contains( room ) )
			{
				chatters++;
			}

		}
		List<Task> tasks = new ArrayList<Task>();
		for( Task task1 : proj.projectTasks )
		{
			if( task1.assignee != null && task1.assignee.equals( user ) && task1.checkUnderImpl() && task1.taskStatus != null && !task1.taskStatus.closed )
			{
				tasks.add( task1 );
			}
		}
		for( Task task1 : proj.projectTasks )
		{
			if( task1.reviewer != null && task1.reviewer.equals( user ) && task1.checkUnderImpl() && task1.taskStatus != null && task1.taskStatus.pending )
			{
				tasks.add( task1 );
			}
		}
		for( Task task1 : proj.projectTasks )
		{
			if( task1.assignee != null && task1.assignee.equals( user ) && task1.taskStatus != null && !task1.taskStatus.closed && !tasks.contains( task1 ) )
			{
				tasks.add( task1 );
			}
		}
		for( Task task1 : proj.projectTasks )
		{
			if( task1.reviewer != null && task1.reviewer.equals( user ) && task1.taskStatus != null && task1.taskStatus.pending && !tasks.contains( task1 ) && (!task1.deleted) )
			{
				tasks.add( task1 );
			}
		}
		int tasknumber = 0;
		for(int i=0;i<tasks.size();i++){
			if(!(tasks.get(i).deleted)){
				tasknumber++;
			}
		}
		
		render( tasknumber, proj, chatters );
	}
}
