package controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import models.Invite;
import models.Project;
import models.User;

import play.mvc.With;

@With(Secure.class)
public class Ajax extends SmartController
{
	/**
	 * renderJSON projects that match the query search term, and based on the
	 * boolean flag chosen to select from
	 * 
	 * @param query
	 *            ,search query
	 * @param invite
	 *            if true, selects projects this user canSendInvite to
	 * @param notMine
	 *            , if true, means that the project to invite to isn't mine
	 */
	public static void projects( String query, boolean invite, boolean notMine )
	{
		if( invite )
		{
			User me = Security.getConnected();
			List<Project> myProjects = new LinkedList<Project>();
			if( me.isAdmin )
				myProjects = Project.find( "byNameLikeAndDeleted", "%" + query + "%", false ).fetch();
			else
				for( Project project : me.projects )
				{
					if( project.name.contains( query ) && me.in( project ).can( "invite" ) )
						myProjects.add( project );
				}
			List<Project.Object> result = new LinkedList<Project.Object>();
			for( Project p : myProjects )
			{
				result.add( new Project.Object( p.id, p.name ) );
			}
			renderJSON( result );
		}
		else
		{
			List<Project.Object> result = new LinkedList<Project.Object>();
			for( Project u : Project.find( "byNameLikeAndDeleted", "%" + query + "%", false ).<Project> fetch() )
			{
				if( Security.isConnected() && Security.getConnected().projects.contains( u ) && notMine )
				{
					continue;
				}
				result.add( new Project.Object( u.id, u.name ) );
			}
			renderJSON( result );
		}
		forbidden();
	}

	/**
	 * renderJSON users that match the search query
	 * 
	 * @param query
	 *            , search query to search for
	 */
	public static void users( long projectId, String query )
	{		
		List<User.Object> result = new LinkedList<User.Object>();
		if (projectId == 0) {
			if (!query.isEmpty()) {
				for (User u : User.find("byNameLikeAndDeleted",
						"%" + query + "%", false).<User> fetch()) {
					result.add(new User.Object(u.id, u.name));
				}
			}
		}
		else
		{
			Project pro = Project.findById(projectId);
			User user = Security.getConnected();
			List<Invite> invs = Invite.findAll();
			ArrayList<User> invitedUsers = new ArrayList<User>();
			for(int i=0 ; i<invs.size(); i++)
			{
				if(invs.get(i).role.project.equals(pro))
					invitedUsers.add(invs.get(i).user);
			}
			for (User u : User.find("byNameLikeAndDeleted",
					"%" + query + "%", false).<User> fetch()) {
				if(!user.equals(u) && !u.projects.contains(pro) && !invitedUsers.contains(u))						
					result.add(new User.Object(u.id, u.name));
			}
		}
		renderJSON( result );
	}
	
	

	/**
	 * This action handles all drops caused by dragging and dropping items on
	 * workspaces.
	 * 
	 * @author mahmoudsakr
	 */
	public static void dynamicDrop(String from, String to) {
		Security.check(Security.isConnected());
		String[] arr = from.split("-"); // meeting-1
		String[] arr2 = to.split("-"); // user-3

		from = arr[0].toLowerCase();
		to = arr2[0].toLowerCase();
		
		// id = from's id
		// id2 = to's id
		long id = Long.parseLong(arr[1]), id2 = Long.parseLong(arr2[1]);

		if (from.equals("user") && to.equals("component")) {
			// inviting user id to component id2
			Users.chooseUsers(id2, id);
		} else if (from.equals("task") && to.equals("component")) {
			// associating task to component
			Tasks.associateToComponent(id, id2);
		} else if (from.equals("task") && to.equals("meeting")) {
			// associate task to meeting
			Meetings.addTask(id2, id);
		} else if (from.equals("user") && to.equals("meeting")) {
			// inviting user to a meeting (Amr Hany)
			Meetings.inviteUser(id2, id);
		} else if (from.equals("component") && to.equals("meeting")) {
			// inviting component to a meeting (Amr Hany)
			Meetings.inviteComponent(id2, id);
		}else if( from.equals("task") && to.equals( "user" ) )
		{
			Tasks.assignTaskAssignee(id,id2);
		}
		else if  (from.equals("user") && to.equals( "task" ) )
		{
			Tasks.assignTaskReviewer(id2, id);
		}
		else if  (from.equals("task") && to.equals( "sprint" ) )
		{
			Sprints.addTask(id, id2);
		}
		else if  (from.equals("task") && to.equals( "task" ) )
		{
			Tasks.setDependency(id, id2);
		} else if (from.equals("projectusers") && to.equals("meeting")) {
			Meetings.inviteAllMembers(id2);
		} else {
			renderText("Something went wrong. Please try again. " + from + id + ", " + to + id2);
		}
	}
}
