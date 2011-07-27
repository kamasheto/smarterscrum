package controllers;

import java.util.List;

import notifiers.Notifications;
import models.Invite;
import models.MeetingAttendance;
import models.Project;
import models.Role;
import models.User;
import models.Log;
import play.mvc.Router;
import play.mvc.With;

/**
 * Invites controller, handles all actions by and to invites
 * <ul>
 * <li>Invite user to role
 * <li>Accept/decline invite (by hash and id)
 * </ul>
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class Invites extends SmartController {
	/**
	 * Creates invite and sends email to user with links to accept/decline
	 * 
	 * @param pid
	 *            Project id
	 * @param userId
	 *            user id
	 */	
	public static void sendInvite(long rId, long userId) {		
		User user = User.findById(userId);
		Role role = Role.findById(rId);		
		Security.check(Security.getConnected().in(role.project).can("invite"));
		Invite invite = new Invite(user, role).save();
		flash.success("Invitation(s) sent successfully!");
		String url = Router.getFullUrl("Application.externalOpen")+"?id="+role.project.id;
		String confirm = Router.getFullUrl("Invites.respondInvite") + "?what=1&hash=" + invite.hash + "&id=" + invite.id;
		String decline = Router.getFullUrl("Invites.respondInvite") + "?what=0&hash=" + invite.hash + "&id=" + invite.id;
		Notifications.invite(user, url, "", confirm, decline, role.project, false);
		// Logs.addLog(pro, "invited " + user.name, "Invite", invite.id);
		Log.addUserLog("Invited " + user.name + " with role: " + role.name, invite, role.project, role);
	}

	/**
	 * either accepts or declines invitation, based on what
	 * 
	 * @param what
	 *            true - accept, false - decline
	 * @param hash
	 *            invite hash
	 * @param id
	 *            invite id
	 */
	public static void respondInvite(boolean what, String hash, long id) {
		Invite invite = Invite.find("byHashAndId", hash, id).first();
		notFoundIfNull(invite);
		if (what && !invite.user.roles.contains(invite.role)) {
			invite.user.addRole(invite.role);
		}
		// Logs.addLog(invite.role.project, "accepted invite to " + invite.role.name, "Invite", invite.id);
		Log.addUserLog("Accepted invitation to " + invite.role.name, invite.role.project, invite.role);
		invite.delete();		
	}
	
	/**
	 * renders the page where the admin can invite users to his project
	 * @param id the id of the project
	 */
	public static void InviteUsers(long id)
	{
		Project project = Project.findById(id);
		render(project);
	}
	
	/**
	 * this method renders to the html page 2 lists of project invitations
	 * and meeting invitations to be shown in the sidebar
	 */
	public static void showInvitations()
	{
		User usr = Security.getConnected();
		List<Invite> invitations = Invite.find("byUser", usr).fetch();
		List<MeetingAttendance> meetings = MeetingAttendance.find("byUserAndDeletedAndStatus", usr, false, "waiting").fetch();
		render(invitations, meetings);
	}
	
	public static void getProjectRoles(long id)
	{		
		Project pro = Project.findById(id);
		List<Role> roles = pro.roles;
		for(int i=0; i<roles.size(); i++)
		{
			roles.get(i).permissions=null;
			roles.get(i).project=null;
			roles.get(i).users=null;			
		}
		renderJSON(roles);
	}
	
}
