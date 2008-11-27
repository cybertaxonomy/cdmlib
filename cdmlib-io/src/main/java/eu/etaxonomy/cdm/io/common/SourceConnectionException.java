package eu.etaxonomy.cdm.io.common;

public class SourceConnectionException extends Exception {
	private static final long serialVersionUID = -3846939002083939654L;

	public SourceConnectionException() {
		super();
	}

	public SourceConnectionException(String message) {
		super(message);
	}

	public SourceConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SourceConnectionException(Throwable cause) {
		super(cause);
	}

}
