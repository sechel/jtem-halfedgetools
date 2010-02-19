package de.jtem.halfedgetools.adapter;

public class CalculatorException extends RuntimeException {

	private static final long 
		serialVersionUID = 1L;

	public CalculatorException() {
	}

	public CalculatorException(String message) {
		super(message);
	}

	public CalculatorException(Throwable cause) {
		super(cause);
	}

	public CalculatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
