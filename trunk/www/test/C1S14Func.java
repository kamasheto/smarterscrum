import play.test.*;
import play.mvc.*;
import play.mvc.Before;
import play.mvc.Http.*;
import play.mvc.Scope.Session;
import org.junit.*;
import controllers.Secure;
import controllers.Security;
 
public class C1S14Func extends FunctionalTest {
     
    @Before
    public void loadFixtures()
    {
    	Fixtures.deleteAll();
        Fixtures.load("dummy-data.yml");
        //Session.current().put("username", "me@sakr.me");
    }
    
	@Test
    public void testdeletionRequestRespond() 
	{
		Response response = GET("/projects/deletionRequestRespond?id=1");
        assertStatus(302, response); //Got a 302 redirect http message ? Success : Fail
              
    }
     
}
