package models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.*;

import javax.persistence.Entity;

import models.User;
import models.Project;

import controllers.Security;

import play.db.jpa.*;

@Entity
public class Log extends SmartModel {
	/**
	 * log message
	 */
	public String message;

	/**
	 * timestamp of this log
	 */
	public long timestamp;
	
	/**
	 * Adds a log with the models attached
	 * @param message log message
	 * @param models all models attached to this log
	 */
	public static void addLog(String message, SmartModel... models) {
		Log log = new Log();
		log.message = message;
		log.timestamp = new Date().getTime();
		log.save();

		List<Class> clazzes = new ArrayList<Class>();
		for (SmartModel model : models) {
			model.logs.add(log);
			if (clazzes.contains(model.getClass())) {
				model.refresh();
			} else {
				clazzes.add(model.getClass());
			}
			model.save();
		}
	}

	/**
	 * Adds a log with the connected user attached to the models
	 * @param message log message
	 * @param models all  models attached to this log
	 */
	public static void addUserLog(String message, SmartModel... models) {
		SmartModel[] newModels = Arrays.copyOf(models, models.length + 1);
		newModels[models.length] = Security.getConnected();
		addLog(message, newModels);
	}
	
	/**
	 * Returns the first user associated with this log (usually the log performer)
	 * @return User that performed this action/log
	 */
	public User getUser() {
		return get(User.class);
	}
	
	/**
	 * Gets all models of class clazz that are associated with this log (usually one)
	 * @param clazz class to check for, example: Meeting.class, User.class, etc
	 * @return List of this class that have this log
	 */
	public <T extends SmartModel> List<T> getAll(Class<T> clazz) {
		try {
			// find the method with the following signature (remember Object... maps to an array)
			Method method = clazz.getMethod("find", String.class, Object[].class);
			
			// invoke this method on a null object (hence a static method), remember there's a difference between sending the class name as a ?, and hardcoding it in the query string
			Object result = method.invoke(null, "select m from " + clazz.getName() + " m join m.logs as l where l = ?", new Object[] {this});
			
			// cast the result, and fetch the list
			return ((JPASupport.JPAQuery) result).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the first class of type clazz associated with this log
	 * @param clazz class to check for, example: Meeting.class, User.class, etc
	 * @return First model that mathces
	 */
	public <T extends SmartModel> T get(Class<T> clazz) {
		List<T> all = (List<T>) getAll(clazz);
		return all.isEmpty() ? null : all.get(0);
	}
}
