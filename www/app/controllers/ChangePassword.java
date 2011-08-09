package controllers;

import models.User;

import com.google.gson.JsonObject;

import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class ChangePassword extends SmartCRUD {

	public static void ChangePassword(long userProfileId) {
		System.out.println("da7'al fel page ely betrender");
		User userProfile = User.findById(userProfileId);
		User connectedUser = Security.getConnected();
		if (connectedUser.deleted)
			notFound();
		if (userProfile.deleted)
			notFound();
		if ((userProfile.id == connectedUser.id) || (connectedUser.isAdmin)) {
			render(userProfile, connectedUser);
		} else {
			flash.error("Sorry, You cannot edit these personal informations.");
		}
	}

	public static void changePass(
			@Required(message = "You must enter your old password") String oldPassword,
			@Required(message = "You must enter your new password") String newPassword,
			@Required(message = "You must confirm your new password") String confirmPassword,
			long userProfileId) {
		System.out.println("da7'al fel page ely bet3'ayar");
		User userProfile = User.findById(userProfileId);
		User connectedUser = Security.getConnected();
		if (connectedUser.deleted)
			notFound();
		if (userProfile.deleted)
			notFound();
		if (userProfile == connectedUser || connectedUser.isAdmin) {
			if (validation.hasErrors()) {
				params.flash();
				validation.keep();
				ChangePassword(userProfileId);
			} else {
				if((userProfile.password.compareTo(oldPassword)) != 0){
					flash.error("You entered an incorrect password");
					validation.keep();
					ChangePassword(userProfileId);
				}
				if (!newPassword.equals(confirmPassword)) {
					flash.error("Your passwords do not match");
					validation.keep();
					ChangePassword(userProfileId);
				} else {
					if (newPassword.length() < 5) {
						flash.error("Your password is too short");
						validation.keep();
						ChangePassword(userProfileId);
					}
				}
			}
			String	oldPasswordCheck = userProfile.password;
			userProfile.password = newPassword;
			userProfile.pwdHash = Application.hash( newPassword );
			userProfile.save();
			System.out.println("elpassword el gededa " + userProfile.password);
			String message="";
			try{
				message = "You have successfully changed your password.";
				if(!oldPasswordCheck.equals(userProfile.password)){
					userProfile.save();
				}

				flash.success(message);
				Application.overlayKiller("", "");
			}catch(Exception e){
				flash.error("Your password hasnt been changed please try again");
			}
		}	else {	
			flash.error("You are not allowed to edit these personal information.");
			Application.overlayKiller("","");
		}
		

	}
}
