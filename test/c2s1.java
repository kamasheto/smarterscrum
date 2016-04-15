import java.util.ArrayList;
import java.util.List;

import models.Project;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class c2s1 extends UnitTest
{
	@Before
	public void setUp()
	{
		Fixtures.deleteAll();
		
	}
	@Test
	public void createProject(){
		Long countBeforeCreation=(long)Project.count();
		Project x=new Project("Project1","Desc");
		x.save();
		Long newCount=(long)Project.count();
		assertEquals(newCount-countBeforeCreation ,1  );

		
		
	}
	
}
