import org.junit.Test;

import play.mvc.Before;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class C4S14 extends FunctionalTest
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
		Response response = GET( "/tasks/updatestatus?Proj_id=1" );
		assertStatus( 302, response );
	}
}
