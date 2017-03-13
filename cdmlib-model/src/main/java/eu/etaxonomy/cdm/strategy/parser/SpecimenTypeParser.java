/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @date 25.07.2011
 */
public class SpecimenTypeParser {

	public static class TypeInfo{
		public SpecimenTypeDesignationStatus status;
		public String collectionString;
		public boolean notDesignated;
		public boolean notSeen;
	}

	/**
	 * see also CentralAfricaFernsTaxonParser#handleTypeLocationPart
	 */
	public static final String typeTypePattern = "(?i)(holo|lecto|iso|isolecto|syn|isosyn|neo|isoneo|type)\\.?";
	public static final String collectionPattern = "^[A-Z]+(\\-[A-Z]+)?";
	public static final String notSeen = "(n\\.v\\.|not\\s+seen)";



	public static SpecimenTypeDesignationStatus parseSpecimenTypeStatus(String type) throws UnknownCdmTypeException {
		//TODO also compare with NameTypeParser
		//TODO further types

		if (StringUtils.isBlank(type)){
			return null;
		}else if (type.endsWith("type") && ! type.equalsIgnoreCase("type")){
			type = type.substring(0, type.length() -4 );
		}else if (type.endsWith("types") && ! type.equalsIgnoreCase("types")){
			type = type.substring(0, type.length() -5 );
		}

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
		}else if (type.equalsIgnoreCase("type")){
			status = SpecimenTypeDesignationStatus.TYPE();
		}else{
			String message = "Type Status not supported: " + type;
			throw new UnknownCdmTypeException(message);
		}
		return status;
	}
}
