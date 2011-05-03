// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @date 02.05.2011
 *
 */
public class DwcaTaxExportTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(DwcaTaxExportTransformer.class);
	
	private static Map<UUID, String> nomStatusMap = new HashMap<UUID, String>();
	private static TermMapping nomStatusMapping;
	private static TermMapping rankMapping;
	

//	public static String transformToGbifTaxonomicStatus(){
//		//TODO
//		return null;
//	}
	
	public static String transformToGbifNomStatus(NomenclaturalStatusType nomStatus){
		if ( nomStatus == null){
			return null;
		}else{
			if (nomStatusMapping == null){
				try {
					nomStatusMapping = new TermMapping("nomStatusToGbif.csv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = nomStatusMapping.getTerm(nomStatus.getUuid());
			if (StringUtils.isBlank(result)){
				logger.warn("Nom. Status (" + nomStatus.getLabel() + ") could not be mapped. Use CDM status label.");
			}
			return result;
		}
	}

	private static void initNomStatusMap() {
		nomStatusMap.put(NomenclaturalStatusType.uuidAlternative, "alternativum");
		nomStatusMap.put(NomenclaturalStatusType.uuidAmbiguous, "ambigua");
//		nomStatusMap.put(NomenclaturalStatusType.uuidCombinationIllegitimate, "");
//		nomStatusMap.put(NomenclaturalStatusType.uuidCombinationInvalid, "");
		nomStatusMap.put(NomenclaturalStatusType.uuidConfusum, "confusum");
		nomStatusMap.put(NomenclaturalStatusType.uuidConserved, "conservandum");
		nomStatusMap.put(NomenclaturalStatusType.uuidConservedProp, "conservandumProp");
		//TODO Wrong at GBIF !!!: dubimum
		nomStatusMap.put(NomenclaturalStatusType.uuidDoubtful, "dubium");
		nomStatusMap.put(NomenclaturalStatusType.uuidIllegitimate, "illegitimum");
		nomStatusMap.put(NomenclaturalStatusType.uuidInvalid, "invalidum");
		nomStatusMap.put(NomenclaturalStatusType.uuidLegitimate, "legitimate");  //why english not latin ??
		nomStatusMap.put(NomenclaturalStatusType.uuidNovum, "novum");
		nomStatusMap.put(NomenclaturalStatusType.uuidNudum, "nudum");
		//TODO 
		nomStatusMap.put(NomenclaturalStatusType.uuidOpusUtiqueOppr, "opressa");
		//TODO
		nomStatusMap.put(NomenclaturalStatusType.uuidOrthographyConserved, "orthographia");
		//TODO
		nomStatusMap.put(NomenclaturalStatusType.uuidOrthographyConservedProp, "orthographia");
		nomStatusMap.put(NomenclaturalStatusType.uuidProvisional, "provisorium");
		nomStatusMap.put(NomenclaturalStatusType.uuidRejected, "rejiciendum");
		nomStatusMap.put(NomenclaturalStatusType.uuidRejectedProp, "rejiciendumProp");
//		nomStatusMap.put(NomenclaturalStatusType.uuidSanctioned, "");
		
		nomStatusMap.put(NomenclaturalStatusType.uuidSubnudum, "rejiciendum");
		nomStatusMap.put(NomenclaturalStatusType.uuidSuperfluous, "superfluum");
		nomStatusMap.put(NomenclaturalStatusType.uuidValid, "valid");
		
		//CDM is missing abortivum, available, combinatio, negatum, oblitum,
		//               protectum, rejiciendumUtique, rejiciendumUtiqueProp
	}
	
	public static String transformToTdwgRank(Rank term){
		if ( term == null){
			return null;
		}else{
			if (rankMapping == null){
				try {
					rankMapping = new TermMapping("rankToTdwg.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = rankMapping.getTerm(term.getUuid());
			if (StringUtils.isBlank(result)){
				logger.warn("Rank (" + term.getLabel() + ") could not be mapped. Use CDM abbreviated label instead.");
			}
			return result;
		}
	}
	
}
