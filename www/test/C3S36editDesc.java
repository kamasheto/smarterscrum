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
public class C3S36editDesc extends UnitTest
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
		Sprint s=new Sprint(2010,3,3,x);
		task1.taskSprint=s;
		s.save();
		task1.save();
		story1.componentID=comp;
		story1.storiesTask.add(task1);
	
		story1.save();

		
		String oldDesc=task1.description;
		String newdesc="the new one";
		
		boolean z=Tasks.editTaskDesc(task1.id, newdesc);
		String  result=task1.description;
		assertFalse(oldDesc.equals(result));

	}
}
