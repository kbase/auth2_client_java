package us.kbase.auth;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import us.kbase.auth.client.AuthClient;

/**
 * This is a shim around the {@link AuthClient} for backwards compatibility purposes.
 * 
 * Only one instance of a client should be created per application if at all possible.
 * 
 * @author wjriehl
 * @author gaprice@lbl.gov
 */
public class ConfigurableAuthService {
	
	private final AuthClient client;

	/** Create an authorization service client with the default configuration.
	 * @throws IOException if an IO error occurs.
	 */
	public ConfigurableAuthService() throws IOException {
		this(new AuthConfig());
	}
	
	/** Create an authorization service client with a custom configuration.
	 * @param config the configuration for the auth client.
	 * @throws IOException if an IO error occurs.
	 */
	public ConfigurableAuthService(final AuthConfig config) throws IOException {
		if (config == null) {
			throw new NullPointerException("config cannot be null");
		}
		if (!config.isInsecureURLsAllowed() &&
				!"https".equals(config.getAuthServerURL().getProtocol())) {
			throw new IllegalArgumentException(String.format(
					"The URL %s is insecure and insecure URLs are not allowed",
					config.getAuthServerURL()));
		}
		try {
			client = AuthClient.from(config.getAuthServerURL().toURI());
		} catch (AuthException e) {
			// can't break backwards compatibility by throwing AuthException
			throw new IOException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("this should be impossible - checked in AuthConfig", e);
		}
	}
	
	/** Get the auth client underlying this client.
	 * @return the client.
	 */
	public AuthClient getClient() {
		return client;
	}
	
	/**
	 * Checks whether strings are a valid user names.
	 * @param usernames the usernames
	 * @param token a valid token
	 * @return a mapping of username to validity.
	 * @throws AuthException if the credentials are invalid
	 * @throws IOException if there is a problem communicating with the server.
	 * @throws IllegalArgumentException if a username is invalid.
	 */
	public Map<String, Boolean> isValidUserName(
			final List<String> usernames,
			final AuthToken token)
			throws IOException, AuthException {
		return client.isValidUserName(usernames, token.getToken());
	}
	
	/**
	 * Validates a token and returns a validated token.
	 * 
	 * @param tokenStr the token string to validate.
	 * @return a validated token
	 * @throws IOException if there is a problem communicating with the server.
	 * @throws AuthException if the token is invalid.
	 */
	public AuthToken validateToken(final String tokenStr)
			throws IOException, AuthException {
		return client.validateToken(tokenStr);
	}
	
}
