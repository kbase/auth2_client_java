package us.kbase.auth;

import java.lang.Exception;

/** An exception thrown when an auth error occurs. */
public class AuthException extends Exception {
	
	// TODO CODE could add the various fields from the auth server so that services can
	//           react differently to different error codes

	private static final long serialVersionUID = 1L;
	
	/** Create the exception.
	 * @param message the exception message.
	 */
	public AuthException(final String message) {
		super(message);
	}

	/** Create the exception.
	 * @param message the exception message.
	 * @param cause the cause of the exception.
	 */
	public AuthException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
