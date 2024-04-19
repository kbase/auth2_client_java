package us.kbase.auth;

//TODO CODE remove if JsonClientCaller in java comment gets rewritten to remove this.

/** This class is here only to trick extremely crufty code and serves no useful purpose. */
@Deprecated
public class AuthUser {
	
	public AuthUser() {}
	
	/** Throws {@link UnsupportedOperationException}.
	 * @return nothing useful.
	 */
	public AuthToken getToken() {
		throw new UnsupportedOperationException();
	}

}
