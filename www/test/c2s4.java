import models.Component;
import models.Project;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Checks Creating new component C2S4
 * 
 * @author Amr Hany
 */
public class c2s4 extends UnitTest
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
		int componentsBefore = Component.findAll().size();
		Component component = new Component();
		component.name = "C2";
		component.description = "the best !!";
		component.project = x;
		component.save();
		int componentsAfter = Component.findAll().size();
		assertEquals( componentsAfter - componentsBefore, 1 );

	}
}
