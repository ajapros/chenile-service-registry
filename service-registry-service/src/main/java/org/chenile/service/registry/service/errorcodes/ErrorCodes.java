package org.chenile.service.registry.service.errorcodes;

public enum ErrorCodes {
    // replace the 50000 below with your own sub error range.
	// Each service must have its own sub error range so that it is easy
	// to know the service from the sub error range
	// Make sure that the sub error number matches the one in the resources
	SOME_ERROR(50000);

	private final int subError;
	private ErrorCodes(int subError) {
		this.subError = subError;
	}
	
	public int getSubError() {
		return this.subError;
	}
}
