package us.kbase.test.auth;

import static org.junit.Assert.fail;

import org.junit.Test;

import us.kbase.auth.AuthUser;

@SuppressWarnings("deprecation")
public class AuthUserTest {

	@Test
	public void getToken() throws Exception {
		try {
			new AuthUser().getToken();
			fail("expected exception");
		} catch (UnsupportedOperationException got) {
			// test passed
		}
		
	}
}
