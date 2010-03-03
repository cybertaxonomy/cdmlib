// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.IDbImportTransformer;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class ErmsTransformer implements IDbImportTransformer{
	private static final Logger logger = Logger.getLogger(ErmsTransformer.class);
	
	public static final int SOURCE_USE_ORIGINAL_DESCRIPTION = 1;
	public static final int SOURCE_USE_BASIS_OF_RECORD = 2;
	public static final int SOURCE_USE_ADDITIONAL_SOURCE = 3;
	public static final int SOURCE_USE_SOURCE_OF_SYNONYMY = 4;
	public static final int SOURCE_USE_REDESCRIPTION = 5;
	public static final int SOURCE_USE_NEW_COMBINATION_REFERENCE = 6;
	public static final int SOURCE_USE_STATUS_SOURCE = 7;
	public static final int SOURCE_USE_EMENDATION = 8;
	
	
	public static NomenclaturalCode kingdomId2NomCode(Integer kingdomId){
		switch (kingdomId){
			case 1: return null;
			case 2: return NomenclaturalCode.ICZN;  //Animalia
			case 3: return NomenclaturalCode.ICBN;  //Plantae
			case 4: return NomenclaturalCode.ICBN;  //Fungi
			case 5: return NomenclaturalCode.ICZN ;  //Protozoa
			case 6: return NomenclaturalCode.ICNB ;  //Bacteria
			case 7: return NomenclaturalCode.ICBN;  //Chromista
			case 147415: return NomenclaturalCode.ICNB;  //Monera
			default: return null;
	
		}
	
	}
	
	public NameTypeDesignationStatus transformNameTypeDesignationStatus(Object statusId){
		if (statusId == null){
			return null;
		}
		Integer intDesignationId = (Integer)statusId;
		switch (intDesignationId){
			case 1: return NameTypeDesignationStatus.ORIGINAL_DESIGNATION();
			case 2: return NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION();
			case 3: return NameTypeDesignationStatus.MONOTYPY();
			default: 
				String warning = "Unknown name type designation status id " + statusId;
				logger.warn(warning);
				throw new IllegalArgumentException(warning);
		}
	}

//	public static Rank rankStr2Rank(String rankName){
//		Rank result = Rank.getRankByName(rankName);
//		return result;
//	}
}
