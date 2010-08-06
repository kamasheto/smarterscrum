package controllers;

import java.util.List;

import notifiers.Notifications;
import models.Invite;
import models.MeetingAttendance;
import models.Role;
import models.User;
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
	 * @param id
	 *            Role id
	 * @param userId
	 *            user id
	 */
	// @Check ("canInvite")
	public static void sendInvite(long id, long userId) {
		User user = User.findById(userId);
		Role role = Role.findById(id);
		Security.check(role.project, "invite");
		Invite invite = new Invite(user, role).save();
		//String url = "@{Application.externalOpen("+temp.project.id+", '/components/viewthecomponent?componentId="+temp.id+"', false)}";
		//Notifications.notifyProjectUsers(temp.project, "onCreateComponent", url, "Component", temp.name, (byte) 0);		
		//Notifications.notifyUsers(user, "Invitation to " + role.name, String.format("Dear " + user.name + ", you have been invited to " + role.name + " in the project " + role.project.name + ".\n\nTo accept this invitation <a href='%s'>click here</a>. To decline this invitation <a href='%s'>click here</a>.", Router.getFullUrl("Invites.respondInvite") + "?what=1&hash=" + invite.hash + "&id=" + invite.id, Router.getFullUrl("Invites.respondInvite") + "?what=0&hash=" + invite.hash + "&id=" + invite.id), (byte) 0);
		Logs.addLog(role.project, "invited " + user.name, "Invite", invite.id);
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
		Logs.addLog(invite.role.project, "accepted invite to " + invite.role.name, "Invite", invite.id);
		invite.delete();
		//flash.success("Invitation successfully accepted and role " + invite.role.name + " added");
		//Application.index();
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
	
}
