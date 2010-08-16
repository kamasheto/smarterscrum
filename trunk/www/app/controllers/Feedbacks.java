package controllers;

import java.util.Random;

import notifiers.Notifications;

import play.cache.Cache;
import play.data.validation.Error;
import play.libs.Images;
import play.mvc.With;

@With( Secure.class )
public class Feedbacks extends SmartController{
	
	public static void index(){
		render();
	}
	
	public static void sendFeedback(String code, String title, String description){
		validation.required(title);
		validation.required(description);
		if(code.equalsIgnoreCase((String) Cache.get("smart_captcha"))){
			if(!(validation.hasErrors())){
				Notifications.feedbacks(Security.getConnected(), title, description, 0);
				renderText("Thank you for contacting us.");
			}
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
	
	public static void captcha(){
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#000000");
	    Cache.set("smart_captcha", code, "30mn");
	    renderBinary(captcha);
	}
	
}
