import java.util.Date;

import play.test.*;
import org.junit.*;
import models.*;
 
public class C1S32 extends UnitTest {
 
    @Test
    public void testUsers() {
        User u = new User("ay7aga", "ay7aga@gmail.com", "testing").save();
    	Date d = new Date();
        new Notification(u, u, "ay7aga", "/show/users", "User", "ay7aga",(byte) 1).save();
        Notification n = Notification.find("byHeader", "Testing").first();
        assertNotNull(n);
    	assertEquals(u, n.receiver);
    }
 
}