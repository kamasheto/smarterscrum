package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Column extends SmartModel {

	@ManyToOne
	public Board board;
	public boolean onBoard;
	public String name;
	public int sequence;
	@ManyToOne
	public TaskStatus taskStatus;

	public boolean deleted;

	/***
	 * Column constructor
	 * 
	 * @param name 
	 * 			column name
	 * @param board 
	 * 			column board
	 * @param taskstatus 
	 * 			status that the column represents
	 */
	public Column (String name, Board board, TaskStatus taskstatus) {
		this.name = name;
		this.board = board;
		if(board!=null)
		this.sequence = board.columns.size();
		this.onBoard=true;
		this.taskStatus=taskstatus;
		/*if(this.name.equalsIgnoreCase( "new" )||this.name.equalsIgnoreCase( "verified" )||this.name.equalsIgnoreCase( "closed" ))
		{
			
			this.onBoard=true;
			int count=0;
			for(int i=0;i<board.columns.size();i++)
			{
				if(board.columns.get(i).onBoard==true)
					count++;
			}
			this.sequence=count;
		}
		else
		{
			this.onBoard=false;
			this.sequence=-1;
		}	*/
		this.save();
		if(board!=null)
		board.columns.add(this);
	}

	/***
	 * Default column constructor
	 */
	public Column(){}
	
}
