/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.events;

/**
 * @author a.mueller
 \* @since 24.06.2011
 *
 */
public class IoProblemEvent extends IoEventBase {
	

	public static IoProblemEvent NewInstance(Class throwingClass, String message, String location, int lineNumber, int severity, String methodName){
		IoProblemEvent result = new IoProblemEvent();
		result.setThrowingClass(throwingClass);
		result.setMessage(message);
		result.setLocation(location);
		result.setLineNumber(lineNumber);
		result.setSeverity(severity);
		result.setMethodName(methodName);
		return result;
	}
	
	protected IoProblemEvent(){
		super();
	}
	
	private int severity;
	private int lineNumber;
	private String methodName;
	
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public int getSeverity() {
		return severity;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String className = getThrowingClass() == null ? "-" : getThrowingClass().getSimpleName();
		return String.format("%s [location=%s; severity=%d; %s/%s:%d]",	getMessage(), getLocation(), getSeverity(), className, getMethodName(), getLineNumber());
	}

	
	
}
