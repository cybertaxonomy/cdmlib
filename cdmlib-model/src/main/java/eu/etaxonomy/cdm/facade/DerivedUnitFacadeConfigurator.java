/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.facade;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 */
public class DerivedUnitFacadeConfigurator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	public static DerivedUnitFacadeConfigurator NewInstance(){
		return new DerivedUnitFacadeConfigurator();
	}

	private boolean moveFieldObjectMediaToGallery = false;

	private boolean moveDerivedUnitMediaToGallery = false;

	private boolean throwExceptionForNonSpecimenPreservationMethodRequest = true;

	private boolean firePropertyChangeEvents = true;

	//for object initialization
	private List<String> propertyPaths;

	private DerivedUnitFacadeConfigurator(){}

// ************************ GETTER / SETTER **********************************

	public void setMoveFieldObjectMediaToGallery(
			boolean moveFieldObjectMediaToGallery) {
		this.moveFieldObjectMediaToGallery = moveFieldObjectMediaToGallery;
	}

	public boolean isMoveFieldObjectMediaToGallery() {
		return moveFieldObjectMediaToGallery;
	}

	public void setMoveDerivedUnitMediaToGallery(
			boolean moveDerivedUnitMediaToGallery) {
		this.moveDerivedUnitMediaToGallery = moveDerivedUnitMediaToGallery;
	}

	public boolean isMoveDerivedUnitMediaToGallery() {
		return moveDerivedUnitMediaToGallery;
	}
//
//	public void setOccurrenceService(IOccurrenceService occurrenceService) {
//		this.occurrenceService = occurrenceService;
//	}
//
//	/**
//	 * Needed for object initialization.
//	 * @see #getPropertyPaths()
//	 */
//	public IOccurrenceService getOccurrenceService() {
//		return occurrenceService;
//	}

	public void setPropertyPaths(List<String> propertyPaths) {
		this.propertyPaths = propertyPaths;
	}

	/**
	 * Needed for object initialization.
	 * Also requires to set occurrence service
	 * @see #getOccurrenceService()
	 */
	public List<String> getPropertyPaths() {
		return propertyPaths;
	}

	/**
	 * @param throwExceptionForNonSpecimenPreservationMethodRequest the throwExceptionForNonSpecimenPreservationMethodRequest to set
	 */
	public void setThrowExceptionForNonSpecimenPreservationMethodRequest(
			boolean throwExceptionForNonSpecimenPreservationMethodRequest) {
		this.throwExceptionForNonSpecimenPreservationMethodRequest = throwExceptionForNonSpecimenPreservationMethodRequest;
	}

	/**
	 * @return the throwExceptionForNonSpecimenPreservationMethodRequest
	 */
	public boolean isThrowExceptionForNonSpecimenPreservationMethodRequest() {
		return throwExceptionForNonSpecimenPreservationMethodRequest;
	}

	public void setFirePropertyChangeEvents(boolean firePropertyChangeEvents) {
		this.firePropertyChangeEvents = firePropertyChangeEvents;
	}

	public boolean isFirePropertyChangeEvents() {
		return firePropertyChangeEvents;
	}
}