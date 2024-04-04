package us.kbase.test.auth.client.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import us.kbase.auth.AuthToken;
import us.kbase.auth.client.internal.TokenCache;
import us.kbase.test.common.TestCommon;

public class TokenCacheTest {
	
	// These tests are mostly copied from the old auth repo
	
	private static final List<AuthToken> TEST_TOKENS = IntStream.range(1, 6).
			mapToObj(i -> new AuthToken("token" + i, "user" + i)).collect(Collectors.toList());

	@Test
	public void constructFail() throws Exception {
		failConstruct(0, 1, "size and maxsize must be > 0");
		failConstruct(1, 0, "size and maxsize must be > 0");
		failConstruct(2, 1, "size must be < maxsize");
		failConstruct(3, 3, "size must be < maxsize");
	}
	
	private void failConstruct(final int size, final int maxsize, final String expected) {
		try {
			new TokenCache(size, maxsize);
			fail("expected exception");
		} catch (IllegalArgumentException got) {
			TestCommon.assertExceptionCorrect(got, new IllegalArgumentException(expected));
		}
	}
	
	@Test
	public void dropsOldTokensOnResize() throws Exception {
		final TokenCache tc = new TokenCache(2, 4);
		tc.putValidToken(TEST_TOKENS.get(0));
		Thread.sleep(2);
		tc.putValidToken(TEST_TOKENS.get(1));
		Thread.sleep(2);
		tc.putValidToken(TEST_TOKENS.get(2));
		Thread.sleep(2);
		tc.putValidToken(TEST_TOKENS.get(3));
		Thread.sleep(2);
		tc.putValidToken(TEST_TOKENS.get(0)); // reset the timer
		assertThat("failure - cache missing tokens",
				tc.getToken("token1"), is(new AuthToken("token1", "user1")));
		//make sure oldest token still there
		assertThat("failure - cache missing tokens",
				tc.getToken("token2"), is(new AuthToken("token2", "user2")));
		Thread.sleep(2);
		tc.putValidToken(TEST_TOKENS.get(4));
		final boolean[] hasToken = {true, false, false, false, true};
		for (int i = 0; i < hasToken.length; i++) {
			if (hasToken[i]) {
				assertNotNull("cache missing token " + i,
						tc.getToken(TEST_TOKENS.get(i).getToken()));
			} else {
				assertNull("cache contains token " + i,
						tc.getToken(TEST_TOKENS.get(i).getToken()));
			}
		}
	}
	
	@Test
	public void putValidTokenBadArgs() throws Exception {
		final TokenCache tc = new TokenCache(2, 3);
		try {
			tc.putValidToken(null);
			fail("expected npe");
		} catch (NullPointerException npe) {
			assertThat("incorrect exception", npe.getMessage(),
					is("token cannot be null"));
		}
	}
	
	@Test
	public void getTokenBadArgs() throws Exception {
		failGetToken("");
		failGetToken(null);
	}
	
	private void failGetToken(final String token) {
		final TokenCache tc = new TokenCache(2, 3);
		try {
			tc.getToken(token);
			fail("expected exception");
		} catch (IllegalArgumentException iae) {
			assertThat("incorrect exception", iae.getMessage(),
					is("token cannot be null or empty"));
		}
	}

	@Test
	public void dropsExpiredTokens() throws Exception {
		final TokenCache tc = new TokenCache(2, 3);
		final Field f = tc.getClass().getDeclaredField("MAX_AGE_MS");
		f.setAccessible(true);
		f.set(tc, 70);
		for (int i = 0; i <= 2; i++) {
			tc.putValidToken(TEST_TOKENS.get(i));
			Thread.sleep(50);
		}
		final boolean[] hasToken = {false, false, true};
		for (int i = 0; i < hasToken.length; i++) {
			if (hasToken[i]) {
				assertNotNull("cache missing token " + i,
						tc.getToken(TEST_TOKENS.get(i).getToken()));
			} else {
				assertNull("cache contains token " + i,
						tc.getToken(TEST_TOKENS.get(i).getToken()));
			}
		}
		f.set(tc, 5 * 60 * 1000); //reset to default
	}
}
