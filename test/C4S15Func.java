import play.test.*;
import play.mvc.*;
import play.mvc.Before;
import play.mvc.Http.*;
import org.junit.*;

public class C4S15Func extends FunctionalTest
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
		Response response = GET( "/tasks/getreport?id=1" );
		assertStatus(302, response );
	}

}
