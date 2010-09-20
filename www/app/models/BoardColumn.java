package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class BoardColumn extends SmartModel {

	/***
	 * each column belong only to one board while a board can have many columns
	 */
	@ManyToOne
	public Board board;
	
	/***
	 * flag that determines whether this column should be displayed n the board or not
	 */
	public boolean onBoard;
	
	/***
	 * column name
	 */
	public String name;
	
	/***
	 * column position on the board
	 */
	public int sequence;
	
	/***
	 * each column represents only one task status while a task status
	 * can be represented in many columns (project board & component board columns)
	 */
	@ManyToOne
	public TaskStatus taskStatus;

	/***
	 * a flag that determines whether the column is deleted or not
	 */
	public boolean deleted;

	/***
	 * Column constructor that sets the name of the column (initially task status name),
	 * the board the column belongs to & the task status that this column represents
	 * 
	 * @param name 
	 * 			column name
	 * @param board 
	 * 			column board
	 * @param taskstatus 
	 * 			status that the column represents
	 */
	public BoardColumn (String name, Board board, TaskStatus taskstatus) {
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
	public BoardColumn(){
		
	}
	
}
