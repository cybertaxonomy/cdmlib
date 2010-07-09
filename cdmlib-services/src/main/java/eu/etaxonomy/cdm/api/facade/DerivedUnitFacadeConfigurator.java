/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.facade;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;

/**
 * @author a.mueller
 *
 */
public class DerivedUnitFacadeConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeConfigurator.class);
	
	
	public static DerivedUnitFacadeConfigurator NewInstance(){
		return new DerivedUnitFacadeConfigurator();
	}
	
	
	private boolean moveFieldObjectMediaToGallery = false;
	
	private boolean moveDerivedUnitMediaToGallery = false;
	
	private boolean throwExceptionForNonSpecimenPreservationMethodRequest = true;

	
	//needed if inititialization via property paths is required
	private IOccurrenceService occurrenceService;
	
	//for object initialization 
	private List<String> propertyPaths;

	
	private DerivedUnitFacadeConfigurator(){
		//
	}
	
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

	public void setOccurrenceService(IOccurrenceService occurrenceService) {
		this.occurrenceService = occurrenceService;
	}

	
	/**
	 * Needed for object initialization. 
	 * @see #getPropertyPaths()
	 * @return
	 */
	public IOccurrenceService getOccurrenceService() {
		return occurrenceService;
	}

	public void setPropertyPaths(List<String> propertyPaths) {
		this.propertyPaths = propertyPaths;
	}

	/**
	 * Needed for object initialization.
	 * Also requires to set occurrence service
	 * @see #getOccurrenceService()
	 * @return
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
	
	
}
