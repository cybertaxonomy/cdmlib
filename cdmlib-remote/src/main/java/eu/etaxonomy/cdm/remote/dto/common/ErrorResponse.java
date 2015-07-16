package eu.etaxonomy.cdm.remote.dto.common;


public class ErrorResponse implements RemoteResponse {
	private String errorMessage;
	
	public ErrorResponse() {
		errorMessage = "";
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;		
	}
}
