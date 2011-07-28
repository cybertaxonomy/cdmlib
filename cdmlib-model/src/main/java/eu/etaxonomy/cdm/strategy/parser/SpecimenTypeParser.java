// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @date 25.07.2011
 *
 */
public class SpecimenTypeParser {


	public static class TypeInfo{
		public SpecimenTypeDesignationStatus status;
		public String collectionString;
	}
	
	/**
	 * see also CentralAfricaFernsTaxonParser#handleTypeLocationPart
	 */
	public static final String typeTypePattern = "(holo|lecto|iso|isolecto|syn|isosyn|neo|isoneo)\\.?";
	public static final String collectionPattern = "^[A-Z]+(\\-[A-Z]+)?";
	
	
	
	public static SpecimenTypeDesignationStatus makeSpecimentTypeStatus(String type) throws UnknownCdmTypeException {
		//TODO further types
		SpecimenTypeDesignationStatus status; 
		if (type.equalsIgnoreCase("iso")){
			status = SpecimenTypeDesignationStatus.ISOTYPE();
		}else if (type.equalsIgnoreCase("isolecto")){
			status = SpecimenTypeDesignationStatus.ISOLECTOTYPE();
		}else if (type.equalsIgnoreCase("syn")){
			status = SpecimenTypeDesignationStatus.SYNTYPE();
		}else if (type.equalsIgnoreCase("holo")){
			status = SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (type.equalsIgnoreCase("lecto")){
			status = SpecimenTypeDesignationStatus.LECTOTYPE();
		}else if (type.equalsIgnoreCase("isosyn")){
			status = SpecimenTypeDesignationStatus.ISOSYNTYPE();
		}else if (type.equalsIgnoreCase("neo")){
			status = SpecimenTypeDesignationStatus.NEOTYPE();
		}else if (type.equalsIgnoreCase("isoneo")){
			status = SpecimenTypeDesignationStatus.ISONEOTYPE();
		}else{
			String message = "Type Status not supported: " + type;
			throw new UnknownCdmTypeException(message);
		}
		return status;
	}
}
