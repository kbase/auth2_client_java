package us.kbase.test.auth.client.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import us.kbase.auth.client.cache.StringCache;
import us.kbase.test.common.TestCommon;

public class StringCacheTest {
	
	// mostly copied from the old auth repo
	
	private static final List<String> TEST_STRINGS = IntStream.range(1, 6).
			mapToObj(i -> "string" + i).collect(Collectors.toList());
	
	@Test
	public void dropsOldStrings() throws InterruptedException {
		StringCache sc = new StringCache(2, 4);
		sc.putString(TEST_STRINGS.get(0));
		Thread.sleep(50);
		sc.putString(TEST_STRINGS.get(1));
		Thread.sleep(50);
		assertTrue("failure - cache missing strings", sc.hasString(TEST_STRINGS.get(0)));
		sc.putString(TEST_STRINGS.get(2));
		Thread.sleep(50);
		sc.putString(TEST_STRINGS.get(3));
		Thread.sleep(50);
		assertTrue("failure - cache missing strings", sc.hasString(TEST_STRINGS.get(0)));
		sc.putString(TEST_STRINGS.get(4));
		boolean[] expected = {true, false, false, false, true};
		for (int i = 0; i < expected.length; i++) {
			assertEquals("failure - cache retained wrong strings",
					expected[i], sc.hasString(TEST_STRINGS.get(i)));
			
		}
	}
	
	@Test
	public void dropsExpiredStrings() throws InterruptedException {
		StringCache sc = new StringCache(2, 3);
		sc.setExpiry(2);
		assertThat("failure - expiry time not set correctly", sc.getExpiry(), is(2L));
		sc.putString(TEST_STRINGS.get(0));
		Thread.sleep(1500);
		//touch to reset touched time of string, but not added time
		assertThat("failure - missing non-expired String",
				sc.hasString(TEST_STRINGS.get(0)), is(true));
		Thread.sleep(1000); //now should be expired but touched within 1 sec
		assertThat("expected token to be expired",
				sc.hasString(TEST_STRINGS.get(0)), is(false));
		sc.putString(TEST_STRINGS.get(1));
		Thread.sleep(50);
		sc.putString(TEST_STRINGS.get(2));
		Thread.sleep(50);
		sc.putString(TEST_STRINGS.get(3));
		assertThat("failure expired string is still in cache"
				, sc.hasString(TEST_STRINGS.get(0)), is(false));
		boolean[] expected = {false, false, true, true};
		for (int i = 0; i < expected.length; i++) {
			assertEquals("failure - cache retained wrong strings",
					expected[i], sc.hasString(TEST_STRINGS.get(i)));
		}
	}
	
	@Test
	public void constructFail() throws Exception {
		failConstruct(0, 1, "size and maxsize must be > 0");
		failConstruct(1, 0, "size and maxsize must be > 0");
		failConstruct(2, 1, "size must be < maxsize");
		failConstruct(3, 3, "size must be < maxsize");
	}
	
	private void failConstruct(final int size, final int maxsize, final String expected) {
		try {
			new StringCache(size, maxsize);
			fail("expected exception");
		} catch (IllegalArgumentException got) {
			TestCommon.assertExceptionCorrect(got, new IllegalArgumentException(expected));
		}
	}
	
	@Test
	public void setExpiryFail() throws Exception {
		try {
			new StringCache(1, 2).setExpiry(0);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(got, new IllegalArgumentException(
					"seconds must be > 0"));
		}
	}
	
	@Test
	public void hasStringFail() throws Exception {
		try {
			new StringCache(1, 2).hasString(null);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(
					got, new NullPointerException("string cannot be null"));
		}
	}
	
	@Test
	public void putStringFail() throws Exception {
		try {
			new StringCache(1, 2).putString(null);
			fail("expected exception");
		} catch (Exception got) {
			TestCommon.assertExceptionCorrect(
					got, new NullPointerException("string cannot be null"));
		}
	}

}
