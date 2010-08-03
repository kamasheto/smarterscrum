package models;

import javax.persistence.*;

@Entity
public class Notification extends SmartModel{
	
	@ManyToOne
	public User receiver;
	
	public User actionPerformer;
	
	@ManyToOne
	public Project project;
	
	public String actionType;
	
	public String resourceURL;
	
	public String resourceType;
	
	public String resourceName;
	
	public byte importance;
	
	public boolean unread;
	
	public Notification(User receiver, User actionPerformer, String actionType, String resourceURL, String resourceType, String resourceName, byte importance)
	{
		this.receiver = receiver;
		this.actionPerformer = actionPerformer;
		this.actionType = actionType;
		this.resourceURL = resourceURL;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		this.importance = importance;
		this.unread = true;
	}

}
