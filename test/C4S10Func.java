import org.junit.Test;

import play.mvc.Before;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class C4S10Func extends FunctionalTest
{

	@Before
	public void loadFixtures()
	{
		Fixtures.deleteAll();
		Fixtures.load( "dummy-data.yml" );
	}

	@Test
	public void enterAnEffort()
	{
		Response response = GET( "/reviewlog/showMeetings?projectID=1" );
		assertStatus( 302, response );
	}

}
