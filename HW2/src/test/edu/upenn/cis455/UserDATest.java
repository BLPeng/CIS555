package test.edu.upenn.cis455;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.PasswordHash;
import edu.upenn.cis455.storage.RobotInfoDA;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.storage.UserDA;

public class UserDATest extends TestCase {
	@BeforeClass
	public void setUp() {
		String basedir = System.getProperty("user.dir");
		File file = new File(basedir, "testDatabase");
		boolean noExist = file.mkdirs();
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		Environment env = new Environment(file, envConfig);
		UserDA.init(env);
	}
	
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
	    UserDA.putEntry(user);
	    User user1 = UserDA.getEntry("xb");
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
	    UserDA.putEntry(user);
	    User user1 = UserDA.getEntry("xb");
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
	    UserDA.putEntry(user);
	    User user1 = UserDA.getEntry("aa");
	    assertEquals(user.toString(), user1.toString());
	    UserDA.deleteEntry("aa");
	    user1 = UserDA.getEntry("aa");
	    assertNull(user1);
	}
	
	@AfterClass
    public void tearDown() {
		UserDA.close();
    }
}
