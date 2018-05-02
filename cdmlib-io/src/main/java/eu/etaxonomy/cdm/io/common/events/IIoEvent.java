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
 * Interface for all IO events
 * @author a.mueller
 * @since 23.06.2011
 */
public interface IIoEvent {
	

	/**
	 * The class that threw this event.
	 * @return
	 */
	public Class getThrowingClass();
	public void setThrowingClass(Class throwingClass);


	/**
	 * A string explaining this event to humans.
	 * @return
	 */
	public String getMessage();
	public void setMessage(String message);
	
	/**
	 * The location in the data where this event was thrown
	 * @return
	 */
	public String getLocation();
	/**
	 * @param location
	 */
	public void setLocation(String location);
	
}
