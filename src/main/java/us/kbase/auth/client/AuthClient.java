package us.kbase.auth.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.auth.AuthException;

/** A client for the KBase Auth2 authentication server (https://github.com/kbase/auth2).
 * 
 *  Only one instance of a client should be created per application if at all possible.
 */
public class AuthClient {
	
	private static final int MAX_RESPONSE_LEN = 100;
	
	// TODO CODE use the built in client in Java 11 when we drop java 8
	
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final URI rootURI;
	
	/** Create the client.
	 * @param auth2RootURI the root URI of the auth service - for example,
	 * https://appdev.kbase.us/services/auth
	 * @throws IOException if an IOException occurs communicating with the auth service.
	 * @throws AuthException if an auth exception occurs communicating with the auth service.
	 */
	public static AuthClient from(final URI auth2RootURI) throws IOException, AuthException {
		return new AuthClient(auth2RootURI);
	}
	
	private AuthClient(final URI auth2RootURI) throws IOException, AuthException {
		if (auth2RootURI == null) {
			throw new NullPointerException("auth2RootURI");
		}
		if (!"https".equals(auth2RootURI.getScheme())) {
			LoggerFactory.getLogger(getClass()).warn("auth root URI is insecure");
		}
		rootURI = auth2RootURI;
		final Map<String, Object> doc = request(rootURI);
		if (!"Authentication Service".equals(doc.get("servicename"))) {
			throw new AuthException(String.format(
					"Service at %s is not the authentication service", rootURI));
		}
	}
	
	private Map<String, Object> request(final URI target) throws IOException, AuthException {
		// tried to use the Jersey client here but kept getting ssl handshake errors if I used
		// it more than once
		final HttpURLConnection conn = (HttpURLConnection) target.toURL().openConnection();
		conn.addRequestProperty("Accept", "application/json");
		try {
			final int code = conn.getResponseCode();
			final String res = readResponse(conn, code != 200);
			final Map<String, Object> obj; 
			try {
				@SuppressWarnings("unchecked")
				final Map<String, Object> foo = MAPPER.readValue(res, Map.class);
				obj = foo;
			} catch (IOException e) {
				throw new AuthException(String.format(
						"Failed reading from auth url %s with response code %s - "
						+ "response is not JSON: %s",
						target,
						code,
						truncate(res)),
						e);
			}
			if (code == 200) {
				return obj;
			}
			if (!obj.containsKey("error")) {
				// not sure how to test this
				throw new AuthException(String.format(
						"Unexpected error response from auth url %s with response code %s: %s",
						target,
						code,
						truncate(res)));
			}
			// ok, we assume things are from the auth server now
			@SuppressWarnings("unchecked")
			final Map<String, Object> errobj = (Map<String, Object>) obj.get("error");
			throw new AuthException(
					"Auth service returned an error: " + errobj.get("message"));
		} finally {
			conn.disconnect();
		}
	}

	private String truncate(String res) {
		// Testing this exactly would require a mockserver. Don't worry about it for now.
		if (res.codePointCount(0, res.length()) > MAX_RESPONSE_LEN) {
			final int index = res.offsetByCodePoints(0, MAX_RESPONSE_LEN - 3);
			res = res.substring(0, index) + "...";
		}
		return res;
	}

	private String readResponse(
			final HttpURLConnection conn,
			final boolean error)
			throws IOException {
		final StringBuilder restext = new StringBuilder();
		try (final InputStream is = error ? conn.getErrorStream() : conn.getInputStream()) {
			if (is == null) {
				return "";
			}
			final Reader reader = new BufferedReader(
					new InputStreamReader(is, StandardCharsets.UTF_8));
			int c = 0;
			while ((c = reader.read()) != -1) {
				restext.append((char) c);
			}
		}
		return restext.toString();
	}

	/** Get the version of the auth server with which this client communicates.
	 * @return the server version.
	 * @throws IOException if an IOException occurs communicating with the auth service.
	 * @throws AuthException if an auth exception occurs communicating with the auth service.
	 */
	public String getServerVersion() throws IOException, AuthException {
		final Map<String, Object> doc = request(rootURI);
		return (String) doc.get("version");
	}

}
