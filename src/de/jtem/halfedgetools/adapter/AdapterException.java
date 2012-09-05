package de.jtem.halfedgetools.adapter;


public class AdapterException extends RuntimeException {

	private static final long 
		serialVersionUID = 1L;

	public AdapterException() {
		super();
	}

	public AdapterException(String message, Throwable cause) {
		super(message, cause);
	}

	public AdapterException(String message) {
		super(message);
	}

	public AdapterException(Throwable cause) {
		super(cause);
	}
	
}
