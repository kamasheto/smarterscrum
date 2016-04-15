import org.junit.Test;

import play.mvc.Before;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class C4S1Func extends FunctionalTest
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
		Response response = GET( "/tasks/entereffort?id=2&effort=12&day=12" );
		assertStatus(302, response );
	}

}
