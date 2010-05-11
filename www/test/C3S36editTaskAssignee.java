import models.Component;
import models.Project;
import models.Board;
import models.Column;
import models.Component;
import models.Log;
import models.Project;
import models.Requestreviewer;
import models.Sprint;
import models.Story;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

import org.junit.Before;
import org.junit.Test;

import controllers.Tasks;

import play.test.Fixtures;
import play.test.UnitTest;

/**
 * editing task description
 * 
 * @author Moumen Mohamed
 */
public class C3S36editTaskAssignee extends UnitTest
{
	@Before
	public void setUp()
	{
		Fixtures.deleteAll();

	}

	@Test
	public void createComponent()
	{
		Project x = new Project( "amr", "Hany" );
		x.save();
		User assignee=	new User ("one", "one@gmail.com", "test", "avatar", true); 
		assignee.save();
		User reporter=	new User ("two", "two@gmail.com", "test", "avatar", true);
		reporter.save();
		User reviewer=	new User ("three", "three@gmail.com", "test", "avatar", true);
		reviewer.save();
		User user1=	new User ("dfgffd", "three111111@gmail.com", "tessst", "avatar", true);
		user1.save();
		Story story1=new Story ("story", "none", "none", 1, "fdsf", assignee.id);
		story1.save();
		
		Component comp=new Component();
		comp.componentStories.add(story1);
		comp.componentUsers.add(assignee);
		comp.componentUsers.add(reviewer);
		comp.componentUsers.add(reporter);
		comp.project=x;
		comp.save();

		
		Task task1=new  Task( "test", x);	
		task1.assignee=assignee;
		task1.reporter=reporter;
		task1.reviewer=reviewer;
		task1.taskStory=story1;
		TaskType old=new TaskType();
		old.name="old one";
		old.save();
		TaskStatus oldstatus= new TaskStatus();
		oldstatus.name="old status";
		oldstatus.save();
		task1.taskStatus=oldstatus;
		task1.taskType=old;
		task1.estimationPoints=1;
		Sprint s=new Sprint(2010,3,3,x);
		s.save();
		task1.taskSprint=s;
		task1.save();
		story1.componentID=comp;
		story1.storiesTask.add(task1);
		story1.save();
		
		boolean z= Tasks.editTaskAssignee(task1.id,user1.id);
		assertTrue(task1.assignee.id !=assignee.id);
		assertTrue(z);
		boolean m=Tasks.editTaskAssignee(task1.id, assignee.id, assignee.id);
		assertTrue(task1.assignee.id ==assignee.id);
		assertTrue(m);
	



	}
}
