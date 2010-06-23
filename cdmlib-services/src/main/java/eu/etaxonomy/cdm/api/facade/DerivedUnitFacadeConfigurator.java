/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.facade;

import org.apache.log4j.Logger;

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
	
	
}
