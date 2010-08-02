import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.*;

import controllers.Application;
import play.test.*;
import models.*;

public class c4s1 extends UnitTest
{

	@Before
	public void setUp()
	{
		Fixtures.deleteAll();
		Fixtures.load( "c4s1.yml" );
	}

	@Test
	public void setEffortOfDayTest()
	{
		new Task( "This is a Test Task C4S1", false, 27.0).save();
		Task testTask = Task.find( "byDescription", "This is a Test Task C4S1" ).first();
		Sprint mySprint = Sprint.find("bySprintNumber","1").first();
		testTask.taskSprint =  mySprint;
		assertNotNull( mySprint );
		testTask.estimationPointsPerDay = new ArrayList<Double>(mySprint.getDuration());
		testTask.setEffortOfDay( 13.0, 1 );
		testTask.setEffortOfDay( 3.0, 10 );
		assertNotNull( testTask );
		assertEquals( "This is a Test Task C4S1", testTask.description );
		assertEquals( 13.0, testTask.getEffortPerDay( 0 ),1 );
		assertEquals( 3.0, testTask.getEffortPerDay( 10 ),1 );

	}

}
