package models;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Sprint extends Model {

	public String sprintNumber;

	@Required
	public Date startDate;

	public Date endDate;

	public boolean deleted;

	@Required
	@ManyToOne
	public Project project;

	@OneToMany (mappedBy = "sprint", cascade = CascadeType.ALL)
	public List<Meeting> meetings;

	@OneToMany (mappedBy = "taskSprint", cascade = CascadeType.ALL)
	public List<Task> tasks;

	// @OneToMany (mappedBy = "daySprint", cascade = CascadeType.ALL)
	// public List<Day> days;

	public Sprint (int year, int month, int day, Project p) {
		this();
		startDate = new GregorianCalendar(year, month - 1, day).getTime();
		endDate = new GregorianCalendar().getTime();
		int defaultDays = p.sprintDuration;
		endDate.setTime(startDate.getTime() + (86400000 * defaultDays));
		project = p;
		this.sprintNumber = p.sprints.size() + 1 + "";
		p.sprints.add(this);
		deleted = false;
	}

	// public Sprint (int year, int month, int day, Project p) {
	// startDate = new GregorianCalendar(year, month - 1, day).getTime();
	// endDate = new GregorianCalendar().getTime();
	// endDate.setTime(startDate.getTime() + (86400000 * 14));
	// project = p;
	// deleted = false;
	// // days = d;
	// // this.sprintNumber = p.sprints.size() + 1 + "";
	// }

	public Sprint (int startyear, int startmonth, int startday, int endyear, int endmonth, int endday, Project p) {
		this();
		startDate = new GregorianCalendar(startyear, startmonth - 1, startday).getTime();
		endDate = new GregorianCalendar(endyear, endmonth - 1, endday).getTime();
		project = p;
		// this.sprintNumber = p.getSprintCounter();
		this.sprintNumber = p.sprints.size() + 1 + "";
		p.sprints.add(this);
		deleted = false;
	}

	public Sprint () {
		meetings = new ArrayList<Meeting>();
		tasks = new ArrayList<Task>();
	}

	public boolean setStartDate(int year, int month, int day) {
		startDate = new GregorianCalendar(year, month--, day).getTime();
		return true;
	}

	public boolean setEndDate(int year, int month, int day) {
		endDate = new GregorianCalendar(year, month, day).getTime();
		return true;
	}

	public int getDuration() {
		int duration = (int) ((endDate.getTime() - startDate.getTime()) / 86400000);
		return duration;

	}

	/**
	 * this method is to format the date and remove the time from it
	 * 
	 * @author minazaki
	 * @param date
	 * @return dateformatted
	 */
	public String format(Date date) {

		return DateFormat.getDateInstance().format(date);
	}

	/**
	 * returns the list of sprints related to the project id
	 * 
	 * @param projId
	 * @return List<Sprint>
	 */
	public List<Sprint> getSprints(long projId) {
		return Sprint.find("project.id=" + projId).fetch();
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
	public String getCoordinatesOfData(long cid) {
		List<Task> taskss = new ArrayList<Task>();
		if (cid == -1) {
			taskss = tasks;
		} else {
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).taskStory.componentID.id == cid)
					taskss.add(tasks.get(i));

			}
		}
		int numberOfDays = getDuration();
		double[] estimationPointsPerDay = new double[numberOfDays];
		System.out.println(cid + " " + taskss.size() + " " + tasks.size());
		if (taskss.size() == 0)
			return "[[]]";
		for (int i = 0; i < estimationPointsPerDay.length; i++) {
			for (int j = 0; j < taskss.size(); j++) {
				if (taskss.get(j).estimationPointsPerDay.size() > i) {
					estimationPointsPerDay[i] = estimationPointsPerDay[i] + taskss.get(j).getEffortPerDay(i);
				}
			}
		}

		String m = "[";

		for (int i = 0; i < estimationPointsPerDay.length; i++) {
			if (i == estimationPointsPerDay.length - 1) {
				m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "]";
			} else {
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
	public static LinkedList<Task> getImpedimentTasks(long Sprint_id) {
		Sprint temp = Sprint.findById(Sprint_id);

		List<Task> STasks = temp.tasks;
		LinkedList<Task> Impediment = new LinkedList<Task>();
		int j = 0;
		for (int i = 0; i < STasks.size(); i++) {
			Task Current = STasks.get(i);
			if (Current.taskType != null && Current.taskType.name == "Impediment") {
				Impediment.add(j, Current);
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
	public String fetchData(long cid) {
		List<Task> taskss = new ArrayList<Task>();
		if (cid == -1) {
			taskss = tasks;
		} else {
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).taskStory.componentID.id == cid)
					taskss.add(tasks.get(i));

			}
		}
		int numberOfDays = getDuration();
		System.out.println(numberOfDays);
		double[] estimationPointsPerDay = new double[numberOfDays];
		int yMax = 0;
		if (taskss.size() == 0)
			return "'NONE',[],[],'','','',1,1";
		for (int i = 0; i < estimationPointsPerDay.length; i++) {
			for (int j = 0; j < taskss.size(); j++) {
				if (i == 0)
					yMax = yMax + (int) (taskss.get(j).estimationPoints);
				if (taskss.get(j).estimationPointsPerDay.size() > i) {
					estimationPointsPerDay[i] = estimationPointsPerDay[i] + taskss.get(j).getEffortPerDay(i);
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

		for (int i = 0; i <= estimationPointsPerDay.length; i++) {
			if (i != estimationPointsPerDay.length) {
				if (i == estimationPointsPerDay.length - 1) {
					m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "]";

				} else {
					m = m + "[" + (i) + "," + estimationPointsPerDay[i] + "],";
				}
				xTicks = xTicks + (i) + ",";
			} else
				xTicks = xTicks + (i);

		}
		xTicks = xTicks + "]";
		m = m + "]";
		String k = ",";
		return m + k + xTicks + k + yTicks + k + xLabel + k + yLabel + k + title + k + xMax + k + yMax;
	}
}
