
import java.util.ArrayList;
import java.util.List;

import models.Project;
import models.User;
import models.Meeting;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class C2S12 extends UnitTest
{
	@Before
	public void setUp()
	{
		Fixtures.deleteAll();
		
	}
	@Test
	public void createMeeting(){
		Long countBeforeCreation=(long)Meeting.count();
		User user = new User("username1","username@gmail.com","test");
		long startTime =(long) 127300 ;
		long endTime = 127310 ;
		Meeting x=new Meeting("JUnit test",user,"to test the Unit Testing",startTime,endTime,"C3101",null,null,null);
		x.save();
		Long newCount=(long)Meeting.count();
		assertEquals(newCount-countBeforeCreation ,1  );

		
		
	}
	
}
