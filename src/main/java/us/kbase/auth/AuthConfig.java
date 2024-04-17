package us.kbase.auth;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/** The configuration class for {@link ConfigurableAuthService}.
 * 
 * Changes to the config have no effect after {@link ConfigurableAuthService} is instantiated.
 * 
 * @author gaprice@lbl.gov
 *
 */
public class AuthConfig {
	
	private static final String DEFAULT_KBASE_AUTH_SERVER_URL =
			"https://ci.kbase.us/services/auth/";
	
	private static final String LEGACY_PATH = "api/legacy/KBase/";
	private static final String LOGIN_LOC = "Sessions/Login/";
			
	private URI authServerURL;
	private boolean allowInsecureURLs = false;
	
	/** Get the default authorization URL.
	 * @return the default authorization URL.
	 */
	public static URL getDefaultAuthURL() {
		try {
			return new URL(DEFAULT_KBASE_AUTH_SERVER_URL);
		} catch (MalformedURLException e) {
			throw new RuntimeException("The impossible just happened");
		}
	}
	
	/**
	 * Create a configuration object with default settings.
	 */
	public AuthConfig() {
		try {
			authServerURL = new URI(DEFAULT_KBASE_AUTH_SERVER_URL);
		} catch (URISyntaxException use) {
			throw new RuntimeException(
					"This cannot occur. Please check with your local deity for an explanation.");
		}
	}
	
	/** Set the URL of the KBase authorization server. Note that to maintain
	 * compatibility with previous versions of this client, URLs ending in
	 * Sessions/Login, api/legacy/KBase/, or api/legacy/KBase/Sessions/Login
	 * with or without trailing slashes will have that portion of the URL removed.
	 * @param authServer the URL of the KBase authorization server.
	 * @return this
	 * @throws URISyntaxException if the URL is not a valid URI. In general
	 * this should never happen.
	 */
	public AuthConfig withKBaseAuthServerURL(URL authServer)
			throws URISyntaxException {
		if (authServer == null) {
			throw new NullPointerException("authServer cannot be null");
		}
		if (!authServer.toString().endsWith("/")) {
			try {
				authServer = new URL(authServer.toString() + "/");
			} catch (MalformedURLException e) {
				throw new RuntimeException("This can't happen", e);
			}
		}
		authServer = stripURLSuffix(authServer, LOGIN_LOC);
		authServer = stripURLSuffix(authServer, LEGACY_PATH);
		authServerURL = authServer.toURI();
		return this;
	}

	private URL stripURLSuffix(URL rui, final String suffix) {
		if (rui.getPath().endsWith(suffix)) {
			final int index = rui.toString().lastIndexOf(suffix);
			try {
				rui = new URL(rui.toString().substring(0, index));
			} catch (MalformedURLException e) {
				throw new RuntimeException(
						"The impossible just occured. Congratulations.", e);
			}
		}
		return rui;
	}
	
	/** Allow insecure http URLs rather than https URLs. Only use this setting
	 * for tests, never in production.
	 * 
	 * When using insecure URLs, you must call this method *before*
	 * initializing the auth client.
	 * @param insecure true to allow insecure URLs.
	 * @return this
	 */
	public AuthConfig withAllowInsecureURLs(final boolean insecure) {
		this.allowInsecureURLs = insecure;
		return this;
	}
	
	/** Returns the configured KBase authorization service URL.
	 * @return the authorization service URL.
	 */
	public URL getAuthServerURL() {
		try {
			return authServerURL.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should never happen");
		}
	}

	/** Returns true if insecure URLs are allowed, false otherwise.
	 * @return whether insecure URLs are allowed.
	 */
	public boolean isInsecureURLsAllowed() {
		return allowInsecureURLs;
	}

}
