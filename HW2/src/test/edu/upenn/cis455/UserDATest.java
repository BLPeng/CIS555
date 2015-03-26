package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import junit.framework.TestCase;

import org.junit.Test;

import edu.upenn.cis455.storage.PasswordHash;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.storage.UserDA;

public class UserDATest extends TestCase {

	
	@Test
	public void testPut() {
		String username = "xb";
		String password;
		try {
			password = PasswordHash.hashPassword("cis555");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			fail("password hasing failed");
			return;
		}
	    User user = new User(username, password);
	    UserDA.putUser(user);
	    User user1 = UserDA.getUser("xb");
	    assertEquals(user.toString(), user1.toString());
	}
	
	@Test
	public void testGet() {
		String username = "xb";
		String password;
		try {
			password = PasswordHash.hashPassword("cis555");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			fail("password hasing failed");
			return;
		}
	    User user = new User(username, password);
	    UserDA.putUser(user);
	    User user1 = UserDA.getUser("xb");
	    assertEquals(user.toString(), user1.toString());
	}

	@Test
	public void testDelete() {
		String username = "aa";
		String password;
		try {
			password = PasswordHash.hashPassword("cis555");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			fail("password hasing failed");
			return;
		}
	    User user = new User(username, password);
	    UserDA.putUser(user);
	    User user1 = UserDA.getUser("aa");
	    assertEquals(user.toString(), user1.toString());
	    UserDA.deleteUser("aa");
	    user1 = UserDA.getUser("aa");
	    assertNull(user1);
	}
}
