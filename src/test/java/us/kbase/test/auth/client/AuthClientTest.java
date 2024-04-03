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
}
