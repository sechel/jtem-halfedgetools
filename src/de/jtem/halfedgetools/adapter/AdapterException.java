package de.jtem.halfedgetools.adapter;


public class AdapterException extends UnsupportedOperationException {

	private static final long 
		serialVersionUID = 1L;

	public AdapterException() {
		super();
		printStackTrace();
	}

	public AdapterException(String message, Throwable cause) {
		super(message, cause);
		printStackTrace();
	}

	public AdapterException(String message) {
		super(message);
		printStackTrace();
	}

	public AdapterException(Throwable cause) {
		super(cause);
		printStackTrace();
	}
	
}
