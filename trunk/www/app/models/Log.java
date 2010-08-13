package models;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Entity;

import models.User;

import controllers.Security;

@Entity
public class Log extends SmartModel {

	public String message;

	public long timestamp;

	public static void addLog(String message, SmartModel... models) {
		Log log = new Log();
		log.message = message;
		log.timestamp = new Date().getTime();
		log.save();

		for (SmartModel model : models) {
			if (!model.logs.contains(log)) {
				model.logs.add(log);
				model.refresh();
				model.save();
			}
		}
	}

	public static void addUserLog(String message, SmartModel... models) {
		SmartModel[] newModels = Arrays.copyOf(models, models.length + 1);
		newModels[models.length] = Security.getConnected();
		System.out.println("START PRINTING MODELS");
		for (SmartModel m : newModels) {
			System.out.println(m);
		}
		addLog(message, newModels);
	}
	
	public User getUser() {
		User user = User.find("select u from User u join u.logs as l where l = ?", this).first();
		return user;
	}
}
