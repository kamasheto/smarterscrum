package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class Column extends Model
{

	@ManyToOne
	public Board board;

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

	@OneToOne( mappedBy = "column" )
	public TaskStatus taskStatus;

	public boolean deleted;

	public Column( String name, Board board )
	{
		this.name = name;
		this.board = board;
		this.sequence = board.columns.size();
		board.columns.add( this );

	}
	// public String toString()
	// {
	// return name + "  " + "board" + board.id;
	// }
}
