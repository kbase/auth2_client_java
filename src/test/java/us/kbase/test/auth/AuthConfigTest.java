package us.kbase.test.auth;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import us.kbase.auth.AuthConfig;
import us.kbase.test.common.TestCommon;

@SuppressWarnings("deprecation")
public class AuthConfigTest {
	
	@Test
	public void defaultURL() throws Exception {
		assertThat("incorrect default URL", AuthConfig.getDefaultAuthURL(),
				is(new URL("https://ci.kbase.us/services/auth/")));
	}
	
	@Test
	public void buildMinimal() throws Exception {
		final AuthConfig c = new AuthConfig();
		
		assertThat("incorrect auth URL", c.getAuthServerURL(),
				is(new URL("https://ci.kbase.us/services/auth/")));
		assertThat("incorrect allow insecure", c.isInsecureURLsAllowed(), is(false));
	}
	
	@Test
	public void buildMaximal() throws Exception {
		final AuthConfig c = new AuthConfig()
				.withKBaseAuthServerURL(new URL("https://vegtableexcitement.com"))
				.withAllowInsecureURLs(true);
		
		assertThat("incorrect auth URL", c.getAuthServerURL(),
				is(new URL("https://vegtableexcitement.com/")));
		assertThat("incorrect allow insecure", c.isInsecureURLsAllowed(), is(true));
	}
	
	@Test
	public void buildAndStripURLs() throws Exception {
		// this one seems unlikely to be seen in the wild
		buildAndStripURLs("https://ci.kbase.us/services/auth/Sessions/Login");
		buildAndStripURLs("https://ci.kbase.us/services/auth/api/legacy/KBase/");
		buildAndStripURLs("https://ci.kbase.us/services/auth/api/legacy/KBase/Sessions/Login");
	}
	
	private void buildAndStripURLs(final String url) throws Exception {
		final AuthConfig c = new AuthConfig().withKBaseAuthServerURL(new URL(url));
		
		assertThat("incorrect auth URL", c.getAuthServerURL(),
				is(new URL("https://ci.kbase.us/services/auth/")));
	}
	
	@Test
	public void withKBaseAuthServerURLFail() throws Exception {
		try {
			new AuthConfig().withKBaseAuthServerURL(null);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(
					got, new NullPointerException("authServer cannot be null"));
		}
	}

}
