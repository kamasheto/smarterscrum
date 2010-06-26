package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Column extends SmartModel {

	@ManyToOne
	public Board board;
	public boolean onBoard;
	// @OneToMany( mappedBy = "Status_on_Board" )
	// public List<Task> task;

	// @Column( unique = true )
	// public long colRowNumber;

	public String name;

	// if 0 then column if 1 then row
	// public int type;

	public int sequence;

	// public double startDimension;
	// public double endDimension;

	@OneToOne (mappedBy = "column")
	public TaskStatus taskStatus;

	public boolean deleted;

	public Column (String name, Board board) {
		this.name = name;
		this.board = board;
		this.sequence = board.columns.size();
		if(this.name.equalsIgnoreCase( "new" )||this.name.equalsIgnoreCase( "verified" )||this.name.equalsIgnoreCase( "closed" ))
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
		}	
		board.columns.add(this);
		
	}
	// public String toString()
	// {
	// return name + "  " + "board" + board.id;
	// }
}
