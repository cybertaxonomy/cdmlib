/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.events;

/**
 * Base class for all IO events. 
 * @author a.mueller
 * @since 24.06.2011
 */
public abstract class IoEventBase implements IIoEvent {

	private Class throwingClass;
	
	private String message;
	
	private String location;

// ************************* GETTER / SETTER *****************************	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.events.IIoEvent#setThrowingClass(java.lang.Class)
	 */
	public void setThrowingClass(Class throwingClass) {
		this.throwingClass = throwingClass;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.events.IIoEvent#getThrowingClass()
	 */
	public Class getThrowingClass() {
		return throwingClass;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.events.IIoEvent#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.events.IIoEvent#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IoEventBase [throwingClass=" + throwingClass + ", message="
				+ message + ", location=" + location + "]";
	}
	
	

}
