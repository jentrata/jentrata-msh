package hk.hku.cecid.ebms.spa.handler;

public class InvalidAttachmentException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3293677712235124039L;
	/**
	 * 
	 * @serial The embedded exception if tunnelling, or null.
	 */
	private Exception exception;

	/**
	 * Create a new InvalidAttachmentException wrapping an existing
	 * exception.
	 * 
	 * <p>
	 * The existing exception will be embedded in the new one, and its message
	 * will become the default message for the
	 * InvalidAttachmentException.
	 * </p>
	 * 
	 * @param e
	 *            The exception to be wrapped in a
	 *            InvalidAttachmentException.
	 */

	public InvalidAttachmentException(Exception e) {
		super();
		this.exception = e;
	}

	/**
	 * Create a new InvalidAttachmentException from an existing
	 * exception.
	 * 
	 * <p>
	 * The existing exception will be embedded in the new one, but the new
	 * exception will have its own message.
	 * </p>
	 * 
	 * @param message
	 *            The detail message.
	 * @param e
	 *            The exception to be wrapped in a
	 *            InvalidAttachmentException.
	 */

	public InvalidAttachmentException(String message, Exception e) {
		super(message);
		this.exception = e;
	}

	/**
	 * Return the embedded exception, if any.
	 * 
	 * @return The embedded exception, or null if there is none.
	 */

	public Exception getException() {
		return exception;
	}

	/**
	 * Return a detail message for this exception.
	 * 
	 * <p>
	 * If there is an embedded exception, and if the
	 * InvalidAttachmentException has no detail message of its own, this
	 * method will return the detail message from the embedded exception.
	 * </p>
	 * 
	 * @return The error or warning message.
	 */

	@Override
	public String getMessage() {
		String message = super.getMessage();

		if (message == null && exception != null) {
			return exception.getMessage();
		} else {
			return message;
		}
	}

	/**
	 * Create a new InvalidAttachmentException.
	 * 
	 * @param message
	 *            The error or warning message.
	 */
	public InvalidAttachmentException(String message) {
		super(message);
		this.exception = null;
	}

	/**
	 * Override toString to pick up any embedded exception.
	 * 
	 * @return A string representation of this exception.
	 */

	@Override
	public String toString() {
		if (exception != null) {
			return exception.toString();
		} else {
			return super.toString();
		}
	}
}
