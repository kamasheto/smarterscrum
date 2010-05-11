import org.junit.Test;

import play.mvc.Before;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;


public class C1S30Func extends FunctionalTest
{
	 @Before
	 public void loadFixtures()
	 {
	  	Fixtures.deleteAll();
	    Fixtures.load("data.yml");
	    //Session.current().put("username", "me@sakr.me");
	 }
	    
		@Test
	    public void testdoActivation() 
		{
			Response response = GET("/accounts/doActivation?hash=11111111111111111111111111111111");
	        assertStatus(302, response); //Got a 302 redirect http message ? Success : Fail
	              
	    }
}
