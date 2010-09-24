package models;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Required;

@Entity
public class Sprint extends SmartModel
{
	public static Date Last = new Date( 2312312 );
	/**
	 * The sprint number in this project.
	 */

	public String sprintNumber;
	/**
	 * The sprint's start date.
	 */

	@Required
	public Date startDate;
	/**
	 * The sprint's end date.
	 */
	public Date endDate;
	/**
	 * If true then this sprint is deleted.
	 */
	public boolean deleted;
	/**
	 * If true this sprint has ended.
	 */
	public boolean ended;
	public Snapshot finalsnapshot;
	/**
	 * The project of this sprint.
	 */
	@Required
	@ManyToOne
	public Project project;
	/**
	 * The Meetings associated to this sprint.
	 */
	@OneToMany( mappedBy = "sprint", cascade = CascadeType.ALL )
	public List<Meeting> meetings;
	/**
	 * The tasks associated to this sprint.
	 */
	@OneToMany( mappedBy = "taskSprint", cascade = CascadeType.ALL )
	public List<Task> tasks;

	// @OneToMany (mappedBy = "daySprint", cascade = CascadeType.ALL)
	// public List<Day> days;

	public static class Object
	{

		long id;
		String sprintNumber;
		int startDay;
		int startMonth;
		long startYear;
		int endDay;
		int endMonth;
		long endYear;
		String project;
		long projectId;

		public Object( long id, String sprintNumber, Date startDate, Date endDate, String project, long projectId )
		{
			this.id = id;
			this.sprintNumber = sprintNumber;
			this.startDay = startDate.getDate();
			this.startMonth = startDate.getMonth() + 1;
			this.startYear = startDate.getYear() + 1900;
			this.endDay = endDate.getDate();
			this.endMonth = endDate.getMonth() + 1;
			this.endYear = endDate.getYear() + 1900;
			this.project = project;
			this.projectId = projectId;
		}
	}

	/**
	 * Class constructor initializing (Start date, end date ,project and sprint
	 * number).
	 * 
	 * @param year
	 *            : year of the end date.
	 * @param month
	 *            : month of the end date.
	 * @param day
	 *            : day of the end date.
	 * @param p
	 *            : project of this sprint
	 */

	public Sprint( int year, int month, int day, Project p )
	{
		this();
		startDate = new GregorianCalendar( year, month - 1, day ).getTime();
		endDate = new GregorianCalendar().getTime();
		int defaultDays = p.sprintDuration;
		endDate.setTime( startDate.getTime() + (86400000 * defaultDays) );
		project = p;
		this.sprintNumber = p.sprints.size() + 1 + "";
		p.sprints.add( this );
		deleted = false;
	}

	/**
	 * Class constructor initializing (Start date, end date ,project and sprint
	 * number).
	 * 
	 * @param startyear
	 *            : year of the start date.
	 * @param startmonth
	 *            : Month of the start date.
	 * @param startday
	 *            : day of the start date.
	 * @param endyear
	 *            : year of the end date.
	 * @param endmonth
	 *            : month of the end date.
	 * @param endday
	 *            : day of the end date.
	 * @param p
	 *            : project of this sprint
	 */
	public Sprint( int startyear, int startmonth, int startday, int endyear, int endmonth, int endday, Project p )
	{
		this();
		startDate = new GregorianCalendar( startyear, startmonth - 1, startday ).getTime();
		endDate = new GregorianCalendar( endyear, endmonth - 1, endday ).getTime();
		project = p;

		this.sprintNumber = p.sprints.size() + 1 + "";
		p.sprints.add( this );
		deleted = false;
	}

	/**
	 * Class constructor initializing (Start date, end date ,project and sprint
	 * number).
	 * 
	 * @param startDate
	 *            : start date of the sprint
	 * @param endDate
	 *            : end date of the sprint
	 * @param p
	 *            : project of the sprint
	 */
	public Sprint( Date startDate, Date endDate, Project p )
	{
		this();
		this.startDate = startDate;
		this.endDate = endDate;
		this.project = p;
		// this.sprintNumber = p.getSprintCounter();
		this.sprintNumber = p.sprints.size() + 1 + "";
		p.sprints.add( this );
		deleted = false;
	}

	/**
	 *Class constructor initializing (Sprint meetings, and sprint associated
	 * tasks).
	 */
	public Sprint()
	{
		meetings = new ArrayList<Meeting>();
		tasks = new ArrayList<Task>();
	}

	/**
	 * Setter for the start date of the sprint
	 * 
	 * @param year
	 *            : year of the sprint start date
	 * @param month
	 *            : month of the sprint start date
	 * @param day
	 *            : day of the sprint start date
	 */
	public boolean setStartDate( int year, int month, int day )
	{
		startDate = new GregorianCalendar( year, month--, day ).getTime();
		return true;
	}

	/**
	 * Setter for the end date of the sprint
	 * 
	 * @param year
	 *            : year of the sprint end date
	 * @param month
	 *            : month of the sprint end date
	 * @param day
	 *            : day of the sprint end date
	 */
	public boolean setEndDate( int year, int month, int day )
	{
		endDate = new GregorianCalendar( year, month, day ).getTime();
		return true;
	}

	/**
	 * @return the duration of the sprint
	 */
	public int getDuration()
	{
		int duration = (int) ((endDate.getTime() - startDate.getTime()) / 86400000);
		if( duration <= 0 )
			return 1;
		return duration;
	}

	/**
	 * formats the date and remove the time from it
	 * 
	 * @author minazaki
	 * @param date
	 * @return dateformatted
	 */
	public String format( Date date )
	{

		return DateFormat.getDateInstance().format( date );
	}

	/**
	 * returns the list of sprints related to the project id
	 * 
	 * @param projId
	 * @return List<Sprint>
	 */
	public List<Sprint> getSprints( long projId )
	{
		return Sprint.find( "project.id=" + projId ).fetch();
	}

	/**
	 * Returns a String which contains the coordinates of the effortPoints of
	 * all the Days in the Sprint. Which will be used by the javascript
	 * function: generateGraph() to generate the sprint burndown chart.
	 * 
	 * @param cid
	 *            this is the component id
	 * @author Hadeer Younis
	 * @category C4S9
	 * @return String of the sprint's data
	 */
	public String getCoordinatesOfData( long cid )
	{
		List<Task> taskss = new ArrayList<Task>();
		if( cid == 0 )
		{
			taskss = tasks;
		}
		else
		{
			for( int i = 0; i < tasks.size(); i++ )
			{
				if( tasks.get( i ).component.id == cid )
					taskss.add( tasks.get( i ) );

			}
		}
		int numberOfDays = getDuration();
		double[] estimationPointsPerDay = new double[numberOfDays];
		if( taskss.size() == 0 )
			return "[[]]";
		for( int i = 0; i < estimationPointsPerDay.length; i++ )
		{
			for( int j = 0; j < taskss.size(); j++ )
			{
				if( taskss.get( j ).estimationPointsPerDay.size() > i )
				{
					estimationPointsPerDay[i] = estimationPointsPerDay[i] + taskss.get( j ).getEffortPerDay( i );
				}
			}
		}

		String m = "[";

		for( int i = 0; i < estimationPointsPerDay.length; i++ )
		{
			if( i == estimationPointsPerDay.length - 1 )
			{
				m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "]";
			}
			else
			{
				m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "],";
			}
		}
		m = m + "]";
		return (m);
	}

	/**
	 * @author emadabdelrahman.
	 * @param Sprint
	 *            id.
	 * @return Impediment Tasks.
	 * @story C4S13.
	 * @description Gets all the Impediment task in a given sprint.
	 */
	public static LinkedList<Task> getImpedimentTasks( long Sprint_id )
	{
		Sprint temp = Sprint.findById( Sprint_id );

		List<Task> STasks = temp.tasks;
		LinkedList<Task> Impediment = new LinkedList<Task>();
		int j = 0;
		for( int i = 0; i < STasks.size(); i++ )
		{
			Task Current = STasks.get( i );
			if( Current.taskType != null && Current.taskType.name == "Impediment" )
			{
				Impediment.add( j, Current );
				j++;
			}

		}
		return Impediment;
	}

	/**
	 * Returns a String which contains the effortPoints of all the Days in the
	 * Sprint along with some more data needed to generate a graph. Which will
	 * be used by the javascript function: generateGraph() to generate the
	 * sprint burndown chart.
	 * 
	 * @param cid
	 *            this is the component id
	 * @author Hadeer Younis
	 * @category C4S8 and C4S9
	 * @return String of the sprint's data
	 */
	public String fetchData( long cid )
	{
		List<Task> taskss = new ArrayList<Task>();
		if( cid == 0 )
		{
			taskss = tasks;
		}
		else
		{
			for( int i = 0; i < tasks.size(); i++ )
			{
				if( tasks.get( i ).component.id == cid )
					taskss.add( tasks.get( i ) );

			}
		}
		int numberOfDays = getDuration();
		double[] estimationPointsPerDay = new double[numberOfDays];
		int yMax = 0;
		if( taskss.size() == 0 )
			return "'NONE',[],[],'','','',1,1";
		for( int i = 0; i < estimationPointsPerDay.length; i++ )
		{
			for( int j = 0; j < taskss.size(); j++ )
			{
				if( i == 0 )
					yMax = yMax + (int) (taskss.get( j ).estimationPoints);
				if( taskss.get( j ).estimationPointsPerDay.size() > i )
				{
					estimationPointsPerDay[i] = estimationPointsPerDay[i] + taskss.get( j ).getEffortPerDay( i );
				}
			}
		}
		int xMax = numberOfDays;
		String xLabel = "'Days'";
		String yLabel = "'Points'";
		String title = "'Sprint " + sprintNumber + ": BurnDown Chart'";
		String m = "[";
		String xTicks = "[";
		String yTicks = "[]";

		for( int i = 0; i <= estimationPointsPerDay.length; i++ )
		{
			if( i != estimationPointsPerDay.length )
			{
				if( i == estimationPointsPerDay.length - 1 )
				{
					m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "]";

				}
				else
				{
					m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "],";
				}
				xTicks = xTicks + (i) + ",";
			}
			else
				xTicks = xTicks + (i);

		}
		xTicks = xTicks + "]";
		m = m + "]";
		String k = ",";
		return m + k + xTicks + k + yTicks + k + xLabel + k + yLabel + k + title + k + xMax + k + yMax;
	}

	public boolean running()
	{
		Date now = Calendar.getInstance().getTime();
		if( startDate.before( now ) && endDate.after( now ) )
		{
			return true;
		}
		return false;
	}
	public boolean ended()
	{
		Date now = Calendar.getInstance().getTime();
		if( endDate.before( now ) )
		{
			return true;
		}
		return false;
	}

	/**
	 * @author minazaki i just modified the insprint to my need it checks if the
	 *         start date of a sprint overlaps with any sprints OR the end date
	 *         overlaps with any sprints
	 * @param start
	 *            the start date of the sprint
	 * @param endate
	 *            the end date of the sprint
	 * @return boolean
	 */
	public boolean overlapSprint( Date start, Date end )
	{
		List <Sprint> sprints = this.project.sprints;
		sprints.remove(this);
		for( Sprint sprint : sprints )
		{
			if( start.equals( sprint.startDate ) ||end.equals( sprint.endDate ) ||start.equals( sprint.endDate ) ||end.equals( sprint.startDate ) || (start.after( sprint.startDate ) && end.before( sprint.endDate )) || (start.before( sprint.startDate ) && end.after( sprint.endDate )) || (start.before( sprint.startDate ) && end.after( sprint.startDate )) || (start.before( sprint.endDate ) && end.after( sprint.endDate )) )
				return true;
		}
		return false;
	}
}
