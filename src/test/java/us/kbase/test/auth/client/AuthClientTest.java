package us.kbase.test.auth.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import us.kbase.auth.AuthException;
import us.kbase.auth.AuthToken;
import us.kbase.auth.client.AuthClient;
import us.kbase.test.common.TestCommon;

public class AuthClientTest {
	
	private static List<ILoggingEvent> LOGS;
	private static ListAppender<ILoggingEvent> APPENDER;

	@BeforeClass
	public static void setup() {
		final Logger fac = (Logger) LoggerFactory.getLogger(AuthClient.class);
		APPENDER = new ListAppender<>();
		APPENDER.start();
		fac.addAppender(APPENDER);
		LOGS = APPENDER.list;
	}
	
	@AfterClass
	public static void tearDown() {
		final Logger fac = (Logger) LoggerFactory.getLogger(AuthClient.class);
		fac.detachAppender(APPENDER);
	}
	
	@Before
	public void clearLogs() {
		LOGS.clear();
	}
	
	@Test
	public void constructFailNullURI() throws Exception {
		failConstruct(null, new NullPointerException("auth2RootURI"));
	}
	
	@Test
	public void constructFailBadUrl200() throws Exception {
		// this test is fragile, the exception message might need tweaking
		final String text = "<!doctype html><html lang=\"en\"><head>"
				+ "<script async src=\"https://www.googletagmanager.com/gtag/js?...";
		final String err = "Failed reading from auth url https://ci.kbase.us/services/authx/ "
				+ "with response code 200 - response is not JSON: " + text;
		assertThat("text is too long", text.length(), is(100));
		failConstruct(new URI("https://ci.kbase.us/services/authx/"), new AuthException(err));
		assertThat("no logs", LOGS.isEmpty(), is(true));
	}
	
	@Test
	public void constructFailBadCodeNoText() throws Exception {
		final String err = "Failed reading from auth url https://httpbin.org/status/400 "
				+ "with response code 400 - response is not JSON: ";
		failConstruct(new URI("https://httpbin.org/status/400"), new AuthException(err));
		assertThat("no logs", LOGS.isEmpty(), is(true));
	}
	
	@Test
	public void constructFailBadCodeWithText() throws Exception {
		final String text = "<!DOCTYPE html>\n<!--[if IE 8]>         <html class="
				+ "\"no-js lt-ie9\" lang=\"en\" > <![endif]-->\n<!--[i...";
		final String err = "Failed reading from auth url http://the-internet."
				+ "herokuapp.com/status_codes/500 with response code 500 - response is not JSON: "
				+ text;
		assertThat("text is too long", text.length(), is(100));
		failConstruct(
				new URI("http://the-internet.herokuapp.com/status_codes/500"),
				new AuthException(err));
		assertThat("incorrect logs " + LOGS, LOGS.size(), is(1));
		final ILoggingEvent event = LOGS.get(0);
		assertThat("incorrect level ", event.getLevel(), is(Level.WARN));
		assertThat("incorrect message", event.getMessage(), is("auth root URI is insecure"));
	}
	
	@Test
	public void constructFailBadPath() throws Exception {
		final String err = "Auth service returned an error: HTTP 404 Not Found";
		failConstruct(
				new URI("https://ci.kbase.us/services/auth/fakeapi"), new AuthException(err));
		assertThat("no logs", LOGS.isEmpty(), is(true));
	}
	
	@Test
	public void constructFailNotService() throws Exception {
		final String err = "Service at https://httpbin.org/json is not the authentication service";
		failConstruct(new URI("https://httpbin.org/json"), new AuthException(err));
		assertThat("no logs", LOGS.isEmpty(), is(true));
	}

	private void failConstruct(final URI uri, final Exception err) {
		try {
			AuthClient.from(uri);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(got, err);
		}
	}

	@Test
	public void getServerVersion() throws Exception {
		final AuthClient c = AuthClient.from(new URI("https://ci.kbase.us/services/auth"));
		final String ver = c.getServerVersion();
		// TODO TEST use regex matcher in hamcrest 2 when we update
		// don't anchor right side to allow for pre-release strings
		assertThat("not a semver", ver.matches("^\\d+\\.\\d+\\.\\d+"), is(true));
	}
	
	@Test
	public void validateToken() throws Exception {
		final String token1 = TestCommon.getAuthToken1();
		final String user1 = TestCommon.getAuthUser1();
		final String token2 = TestCommon.getAuthToken2();
		final String user2 = TestCommon.getAuthUser2();
		assertThat("both tokens are the same", token1.equals(token2), is(false));
		assertThat("both users are the same", user1.equals(user2), is(false));

		final AuthClient c = AuthClient.from(new URI(TestCommon.getAuthURI()));
		
		// First time from service
		final AuthToken t = c.validateToken(token1);
		assertThat("incorrect user", t.getUserName(), is(user1));  // for easier debugging
		assertThat("incorrect auth token", t, is(new AuthToken(token1, user1)));
		
		// Second time from cache. No way to actually verify that's what's happening though.
		// If we refactor to use the same client for every request, can inject the client
		// and mock it
		final AuthToken t2 = c.validateToken(token1);
		assertThat("incorrect auth token", t2, is(new AuthToken(token1, user1)));
		
		// First time from service
		final AuthToken t3 = c.validateToken(token2);
		assertThat("incorrect user", t3.getUserName(), is(user2));  // for easier debugging
		assertThat("incorrect auth token", t3, is(new AuthToken(token2, user2)));
		
		// Second time from cache.
		final AuthToken t4 = c.validateToken(token2);
		assertThat("incorrect auth token", t4, is(new AuthToken(token2, user2)));
	}
	
	@Test
	public void validateTokenFailEmptyToken() throws Exception {
		final URI uri = new URI("https://ci.kbase.us/services/auth");
		validateTokenFail(uri, null, new IllegalArgumentException(
				"token must be a non-whitespace string"));
		validateTokenFail(uri, "   \t   ", new IllegalArgumentException(
				"token must be a non-whitespace string"));
	}
	
	@Test
	public void validateTokenFailInvalidToken() throws Exception {
		final URI uri = new URI("https://ci.kbase.us/services/auth");
		validateTokenFail(uri, "faketoken", new AuthException(
				"Auth service returned an error: 10020 Invalid token"));
	}
	
	private void validateTokenFail(final URI uri, final String token, final Exception expected) {
		try {
			AuthClient.from(uri).validateToken(token);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(got, expected);
		}
	}
}
