// $Id$
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
 * @date 24.06.2011
 *
 */
public class IoProblemEvent extends IoEventBase {
	
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
		return String.format("%s [location=%s; severity=%d; %s/%s:%d]",	getMessage(), getLocation(), getSeverity(), getThrowingClass().getSimpleName(), getMethodName(), getLineNumber());
	}

	
	
}
