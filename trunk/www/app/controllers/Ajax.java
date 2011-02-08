package controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import models.Invite;
import models.Project;
import models.Task;
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
	 * @param not_mine
	 *            , if true, means that the project to invite to isn't mine
	 */
	public static void projects( String query, boolean invite, boolean not_mine )
	{
		if( invite )
		{
			User me = Security.getConnected();
			List<Project> my_projects = new LinkedList<Project>();
			if( me.isAdmin )
				my_projects = Project.find( "byNameLikeAndDeletedAndApprovalStatus", "%" + query + "%", false, true ).fetch();
			else
				for( Project project : me.projects )
				{
					if( project.name.contains( query ) && me.in( project ).can( "invite" ) )
						my_projects.add( project );
				}
			List<Project.Object> result = new LinkedList<Project.Object>();
			for( Project p : my_projects )
			{
				result.add( new Project.Object( p.id, p.name ) );
			}
			renderJSON( result );
		}
		else
		{
			List<Project.Object> result = new LinkedList<Project.Object>();
			for( Project u : Project.find( "byNameLikeAndDeletedAndApprovalStatus", "%" + query + "%", false, true ).<Project> fetch() )
			{
				if( Security.isConnected() && Security.getConnected().projects.contains( u ) && not_mine )
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
	public static void users( long project_id, String query )
	{		
		List<User.Object> result = new LinkedList<User.Object>();
		if (project_id == 0) {
			if (!query.isEmpty()) {
				for (User u : User.find("byNameLikeAndDeleted",
						"%" + query + "%", false).<User> fetch()) {
					result.add(new User.Object(u.id, u.name));
				}
			}
		}
		else
		{
			Project project = Project.findById(project_id);
			User user = Security.getConnected();
			List<Invite> invites = Invite.find("role.project = ?", project).fetch();
			ArrayList<User> invited_users = new ArrayList<User>();
			for (Invite invite : invites) {
				invited_users.add(invite.user);
			}
			for (User u : User.find("byNameLikeAndDeletedAndIsActivated",
					"%" + query + "%", false, true).<User> fetch()) {
				if(user != u && !u.projects.contains(project) && !invited_users.contains(u))
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
	public static void dynamic_drop(String from, String to) {
		Security.check(Security.isConnected());
		String[] from_arr = from.split("-"); // meeting-1
		String[] to_arr = to.split("-"); // user-3

		from = from_arr[0].toLowerCase();
		to = to_arr[0].toLowerCase();
		
		long from_id = Long.parseLong(from_arr[1]), to_id = Long.parseLong(to_arr[1]);

		if (from.equals("user") && to.equals("component")) {
			// inviting user id to component id2
			Users.choose_users(to_id, from_id);
		} else if (from.equals("task") && to.equals("component")) {
			// associating task to component
			Tasks.associate_to_component(from_id, to_id);
		} else if (from.equals("task") && to.equals("meeting")) {
			// associate task to meeting
			Meetings.addTask(to_id, from_id);
		} else if (from.equals("user") && to.equals("meeting")) {
			// inviting user to a meeting (Amr Hany)
			Meetings.inviteUser(to_id, from_id);
		} else if (from.equals("component") && to.equals("meeting")) {
			// inviting component to a meeting (Amr Hany)
			Meetings.inviteComponent(to_id, from_id);
		}else if( from.equals("task") && to.equals( "user" ) )
		{
			Tasks.assign_task_assignee(from_id,to_id);
		}
		else if  (from.equals("user") && to.equals( "task" ) )
		{
			Tasks.assign_task_reviewer(from_id, to_id);
		}
		else if  (from.equals("task") && to.equals( "sprint" ) )
		{
			Sprints.addTask(from_id, to_id);
		}
		else if  (from.equals("task") && to.equals( "task" ) )
		{
			Tasks.set_dependency(from_id, to_id);
		} else if (from.equals("projectusers") && to.equals("meeting")) {
			Meetings.inviteAllMembers(to_id);
		} else {
			renderText("Something went wrong. Please try again. " + from + from_id + ", " + to + to_id);
		}
	}
}
