package us.kbase.auth;

import java.io.IOException;

// TODO CODE remove if JsonClientCaller in java comment gets rewritten to remove this.

/** This class is here only to trick extremely crufty code and serves no useful purpose. */
@Deprecated
public class AuthService {
	
	/** Throws {@link UnsupportedOperationException}.
	 * @param userName ignore me
	 * @param password ignore me
	 * @return nothing useful
	 * @throws AuthException never
	 * @throws IOException never
	 */
	public static AuthUser login(
			final String userName,
			final String password)
			throws AuthException, IOException {
		throw new UnsupportedOperationException();
	}

}
