package controllers;

import java.util.Random;

import notifiers.Notifications;

import play.cache.Cache;
import play.data.validation.Error;
import play.libs.Images;
import play.mvc.With;

@With( Secure.class )
public class Feedbacks extends SmartController{
	/**
	 * renders the form
	 */
	public static void index(){
		render();
	}
	
	/**
	 * sends the feedback to the email of the support people.
	 * @param code the user input for the captcha image (for validation)
	 * @param title title/subject of the email to be sent (usually a bug summary)
	 * @param description detailed description of the bug/suggestion
	 */
	public static void sendFeedback(String code, String title, String description){
		validation.required(title);
		validation.required(description);
		if(code.equalsIgnoreCase((String) Cache.get("smart_captcha")) && !(validation.hasErrors())){
				Notifications.feedbacks(Security.getConnected(), title, description, 0);
				renderText("Thank you for contacting us.");
		}
		else{
			String errorMsg = "";
			for(Error error : validation.errors()) {
	             errorMsg+=error.message()+"\n";
	         }
			if(!code.equalsIgnoreCase((String) Cache.get("smart_captcha")))
				errorMsg += "Please re-enter the validation text as seen in the image. No Zeroes!";
			renderText(errorMsg);
		}
	}
	
	/**
	 * Generates the captcha image to validate the user input 
	 * (called from the view of the index)
	 */
	public static void captcha(){
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#000000");
	    Cache.set("smart_captcha", code, "30mn");
	    renderBinary(captcha);
	}
	
}
