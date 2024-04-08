package us.kbase.test.auth;

import static org.junit.Assert.fail;

import org.junit.Test;

import us.kbase.auth.AuthService;

@SuppressWarnings("deprecation")
public class AuthServiceTest {
	
	@Test
	public void login() throws Exception {
		try {
			AuthService.login(null, null);
			fail("expected exception");
		} catch (UnsupportedOperationException got) {
			// test passes
		}
	}

}
