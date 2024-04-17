package us.kbase.test.auth;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import us.kbase.auth.AuthConfig;
import us.kbase.auth.AuthToken;
import us.kbase.auth.ConfigurableAuthService;
import us.kbase.test.common.TestCommon;

public class ConfigurableAuthServiceTest {

	
	@Test
	public void buildFailBadArgs() throws Exception {
		buildFail(null, new NullPointerException("config cannot be null"));
		
		final AuthConfig c = new AuthConfig()
				.withKBaseAuthServerURL(new URL("http://ci.kbase.us"));
		
		buildFail(c, new IllegalArgumentException(
				"The URL http://ci.kbase.us/ is insecure and insecure URLs are not allowed"));
	}
	
	@Test
	public void buildFailBadURL() throws Exception {
		final AuthConfig c = new AuthConfig()
				.withKBaseAuthServerURL(new URL("https://ci.kbase.us/services/auth/fakeapi"));
		
		buildFail(c, new IOException(
				"Auth service returned an error: HTTP 404 Not Found"));
	}
	
	private void buildFail(final AuthConfig cfg, final Exception expected) {
		try {
			new ConfigurableAuthService(cfg);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(got, expected);
		}
	}
	
	@Test
	public void buildNoArgs() throws Exception {
		// not sure what else to test here
		assertThat("incorrect build", new ConfigurableAuthService().getClient().getURI(),
				is(new URI("https://ci.kbase.us/services/auth/")));
	}
	
	@Test
	public void buildLenient() throws Exception {
		final ConfigurableAuthService c = new ConfigurableAuthService(new AuthConfig()
				.withAllowInsecureURLs(true)
				.withKBaseAuthServerURL(new URL("https://ci.kbase.us/services/auth/")));
		// not sure what else to test here
		assertThat("incorrect build", c.getClient().getURI(),
				is(new URI("https://ci.kbase.us/services/auth/")));
	}
	
	@Test
	public void validateToken() throws Exception {
		// most of the logic is tested in the auth client tests, just do a minimal test here
		final String token1 = TestCommon.getAuthToken1();
		final String user1 = TestCommon.getAuthUser1();

		final AuthToken t = new ConfigurableAuthService(
				new AuthConfig().withKBaseAuthServerURL(new URL(TestCommon.getAuthURI()))
				).validateToken(token1);
		
		assertThat("incorrect user", t.getUserName(), is(user1));  // for easier debugging
		assertThat("incorrect auth token", t, is(new AuthToken(token1, user1)));
	}
	
	@Test
	public void isValidUserName() throws Exception {
		// most of the logic is tested in the auth client tests, just do a minimal test her
		final String token1 = TestCommon.getAuthToken1();
		final List<String> goodUsers = TestCommon.getGoodUsers();

		final ConfigurableAuthService c = new ConfigurableAuthService(
				new AuthConfig().withKBaseAuthServerURL(new URL(TestCommon.getAuthURI())));
		
		final List<String> badUsers = Arrays.asList(
				"superfakeuserthatdoesntexistihope",
				"anothersuperfakeuserrighthereimfake");
		
		final List<String> allUsers = new LinkedList<>(badUsers);
		allUsers.addAll(goodUsers);
		
		final Map<String, Boolean> expected = new HashMap<>();
		goodUsers.stream().forEach(u -> expected.put(u.trim(), true));
		badUsers.stream().forEach(u -> expected.put(u, false));
				
		final Map<String, Boolean> res = c.isValidUserName(
				allUsers, new AuthToken(token1, "fakeuser"));
		
		assertThat("incorrect users", res, is(expected));
	}
}
