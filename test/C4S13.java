import play.test.*;
import play.mvc.*;
import play.mvc.Before;
import play.mvc.Http.*;
import org.junit.*;

public class C4S13 extends FunctionalTest
{

	@Before
	public void loadFixtures()
	{
		Fixtures.deleteAll();
		Fixtures.load( "dummy-data.yml" );
	}

	@Test
	public void viewimpediment()
	{
		Response response = GET( "/tasks/viewimpediment?Proj_id=1" );
		assertStatus(302, response );
	}

}


