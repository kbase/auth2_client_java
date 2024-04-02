package us.kbase.test.auth;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import us.kbase.auth.AuthToken;

public class AuthTokenTest {
	
	// these tests are mostly copied from the old auth repo
	
	@Test
	public void equals() throws Exception {
		EqualsVerifier.forClass(AuthToken.class).usingGetClass().verify();
	}
	
	@Test
	public void construct() throws Exception {
		final AuthToken t = new AuthToken("foo", "bar");
		assertThat("incorrect token", t.getToken(), is("foo"));
		assertThat("incorrect user", t.getUserName(), is("bar"));
	}
	
	@Test
	public void constructFail() throws Exception {
		failMakeToken(null, "user", "token cannot be null or empty");
		failMakeToken("", "user", "token cannot be null or empty");
		failMakeToken("bar", null, "user cannot be null or empty");
		failMakeToken("bar", "", "user cannot be null or empty");
	}
	
	private void failMakeToken(String token, String user, String exp) {
		try {
			new AuthToken(token, user);
			fail("created bad token");
		} catch (IllegalArgumentException got) {
			assertThat("incorrect exception message", got.getMessage(),
					is(exp));
		}
	}

}
