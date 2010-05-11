import models.Project;
import models.Task;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class C4S12 extends UnitTest {
	@Before
	public void setUp() {
		Fixtures.deleteAll();

	}

	@Test
	public void save() {

		Long countBeforeCreation = (long) Task.count();
		Project p=new Project("smart soft", "best company");
		p.save();
		Task x = new Task("a new Impediment",p );
		x.save();
		Long newCount = (long) Task.count();
		assertEquals(newCount - countBeforeCreation, 1);

	}

}
