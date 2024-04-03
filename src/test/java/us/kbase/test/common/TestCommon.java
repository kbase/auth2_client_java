package us.kbase.test.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.Ini;


public class TestCommon {
	
	public static final String AUTH_URL = "auth_url";
	public static final String TOKEN1 = "auth_token1";
	public static final String TOKEN2 = "auth_token2";
	public static final String USER1 = "auth_user1";
	public static final String USER2 = "auth_user2";
	public static final String GOOD_USERS = "good_users";
	
	public static final String TEST_CONFIG_FILE_PROP_NAME = "test.cfg";
	public static final String TEST_CONFIG_FILE_SECTION = "auth_client_test";
	
	private static Map<String, String> testConfig;

	public static String getAuthURI() {
		return getTestProperty(AUTH_URL);
	}
	
	public static String getAuthToken1() {
		return getTestProperty(TOKEN1);
	}
	
	public static String getAuthToken2() {
		return getTestProperty(TOKEN2);
	}
	
	public static String getAuthUser1() {
		return getTestProperty(USER1);
	}
	
	public static String getAuthUser2() {
		return getTestProperty(USER2);
	}
	
	public static List<String> getGoodUsers() {
		return Arrays.asList(getTestProperty(GOOD_USERS).split(","));
	}
	
	public static String getTestProperty(final String propertyKey, final boolean allowNull) {
		getTestConfig();
		final String prop = testConfig.get(propertyKey);
		if (!allowNull && (prop == null || prop.trim().isEmpty())) {
			throw new TestException(String.format(
					"Property %s in section %s of test file %s is missing",
					propertyKey, TEST_CONFIG_FILE_SECTION, getConfigFilePath()));
		}
		return prop;
	}

	public static String getTestProperty(final String propertyKey) {
		return getTestProperty(propertyKey, false);
	}

	private static void getTestConfig() {
		if (testConfig != null) {
			return;
		}
		final Path testCfgFilePath = getConfigFilePath();
		final Ini ini;
		try {
			ini = new Ini(testCfgFilePath.toFile());
		} catch (IOException ioe) {
			throw new TestException(String.format(
					"IO Error reading the test configuration file %s: %s",
					testCfgFilePath, ioe.getMessage()), ioe);
		}
		testConfig = ini.get(TEST_CONFIG_FILE_SECTION);
		if (testConfig == null) {
			throw new TestException(String.format("No section %s found in test config file %s",
					TEST_CONFIG_FILE_SECTION, testCfgFilePath));
		}
	}

	private static Path getConfigFilePath() {
		final String testCfgFilePathStr = System.getProperty(TEST_CONFIG_FILE_PROP_NAME);
		if (testCfgFilePathStr == null || testCfgFilePathStr.trim().isEmpty()) {
			throw new TestException(String.format("Cannot get the test config file path." +
					" Ensure the java system property %s is set to the test config file location.",
					TEST_CONFIG_FILE_PROP_NAME));
		}
		return Paths.get(testCfgFilePathStr).toAbsolutePath().normalize();
	}
	
	public static void assertExceptionCorrect(
			final Throwable got,
			final Throwable expected) {
		assertThat("incorrect exception. trace:\n" +
				ExceptionUtils.getStackTrace(got),
				got.getLocalizedMessage(),
				is(expected.getLocalizedMessage()));
		assertThat("incorrect exception type", got, instanceOf(expected.getClass()));
	}
	
	public static class TestException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		
		public TestException(final String message) {
			super(message);
		}
		
		public TestException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

}
