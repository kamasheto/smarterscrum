package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Chat room Model ,done to make chatroom to each project
 * 
 * @author Amr Hany
 */
@Entity
public class ChatRoom extends SmartModel
{

	public boolean deleted;

	@OneToMany( mappedBy = "room" )
	public List<Message> chats;

	public Project project;

	public ChatRoom( Project p )
	{
		chats = new ArrayList<Message>();
		project = p;
	}

}
