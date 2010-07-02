package controllers;

import models.Invite;
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
		Notifications.notifyUsers(user, "Invitation to " + role.name, String.format("Dear " + user.name + ", you have been invited to " + role.name + " in the project " + role.project.name + ".\n\nTo accept this invitation <a href='%s'>click here</a>. To decline this invitation <a href='%s'>click here</a>.", Router.getFullUrl("Invites.respondInvite") + "?what=1&hash=" + invite.hash + "&id=" + invite.id, Router.getFullUrl("Invites.respondInvite") + "?what=0&hash=" + invite.hash + "&id=" + invite.id), (byte) 0);
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
			invite.user.roles.add(invite.role);
			if (!invite.user.projects.contains(invite.role.project)) {
				invite.user.projects.add(invite.role.project);
				if (!invite.role.baseRole) {
					Role baseRole = Role.find("byProjectAndBaseRole", invite.role.project, true).first();
					invite.user.roles.add(baseRole);	
				}
			}
			invite.user.save();
			invite.role.save();
		}
		Logs.addLog(invite.role.project, "accepted invite to " + invite.role.name, "Invite", invite.id);
		invite.delete();
		flash.success("Invitation successfully accepted and role " + invite.role.name + " added");
		Application.index();
	}
}
