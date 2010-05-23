package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * Message Entity, Used to save messages in the DB in order to show them in the
 * chat System
 * 
 * @author Amr Hany
 */
@Entity
public class Message extends Model {

	public boolean deleted;

	public long stamp;

	public String author;

	@Lob
	public String message;

	@ManyToOne
	public ChatRoom room;

	public Message (String authr, String mssage, ChatRoom r) {
		this.room = r;
		this.author = authr;
		this.message = mssage;
		this.stamp = new Date().getTime();
	}
}