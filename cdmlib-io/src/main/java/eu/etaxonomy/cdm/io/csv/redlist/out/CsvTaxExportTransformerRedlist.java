/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.dwca.out.TermMapping;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 \* @since 02.05.2011
 *
 */
public class CsvTaxExportTransformerRedlist extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(CsvTaxExportTransformerRedlist.class);
	
//	private static Map<UUID, String> nomStatusMap = new HashMap<UUID, String>();
	private static TermMapping nomStatusMapping;
	private static TermMapping rankMapping;
	private static TermMapping specimenTypeMapping;
	private static TermMapping nameTypeMapping;
	private static TermMapping sexMapping;
	private static TermMapping lifeStageMapping;
	private static TermMapping occStatusMapping;
	private static TermMapping establishmentMeansMapping;
	

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
					nomStatusMapping = new TermMapping("nomStatusToGbif.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = nomStatusMapping.getTerm(nomStatus.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("Nom. Status (" + nomStatus.getLabel() + ") could not be mapped. Use CDM status label.");
			}
			return result;
		}
	}

//	private static void initNomStatusMap() {
//		nomStatusMap.put(NomenclaturalStatusType.uuidAlternative, "alternativum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidAmbiguous, "ambigua");
////		nomStatusMap.put(NomenclaturalStatusType.uuidCombinationIllegitimate, "");
////		nomStatusMap.put(NomenclaturalStatusType.uuidCombinationInvalid, "");
//		nomStatusMap.put(NomenclaturalStatusType.uuidConfusum, "confusum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidConserved, "conservandum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidConservedProp, "conservandumProp");
//		//TODO Wrong at GBIF !!!: dubimum
//		nomStatusMap.put(NomenclaturalStatusType.uuidDoubtful, "dubium");
//		nomStatusMap.put(NomenclaturalStatusType.uuidIllegitimate, "illegitimum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidInvalid, "invalidum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidLegitimate, "legitimate");  //why english not latin ??
//		nomStatusMap.put(NomenclaturalStatusType.uuidNovum, "novum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidNudum, "nudum");
//		//TODO 
//		nomStatusMap.put(NomenclaturalStatusType.uuidOpusUtiqueOppr, "opressa");
//		//TODO
//		nomStatusMap.put(NomenclaturalStatusType.uuidOrthographyConserved, "orthographia");
//		//TODO
//		nomStatusMap.put(NomenclaturalStatusType.uuidOrthographyConservedProp, "orthographia");
//		nomStatusMap.put(NomenclaturalStatusType.uuidProvisional, "provisorium");
//		nomStatusMap.put(NomenclaturalStatusType.uuidRejected, "rejiciendum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidRejectedProp, "rejiciendumProp");
////		nomStatusMap.put(NomenclaturalStatusType.uuidSanctioned, "");
//		
//		nomStatusMap.put(NomenclaturalStatusType.uuidSubnudum, "rejiciendum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidSuperfluous, "superfluum");
//		nomStatusMap.put(NomenclaturalStatusType.uuidValid, "valid");
//		
//		//CDM is missing abortivum, available, combinatio, negatum, oblitum,
//		//               protectum, rejiciendumUtique, rejiciendumUtiqueProp
//	}
	
	public static String transformToGbifRank(Rank term){
		if ( term == null){
			return null;
		}else{
			if (rankMapping == null){
				try {
					rankMapping = new TermMapping("rankToGbif.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = rankMapping.getTerm(term.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("Rank (" + term.getLabel() + ") could not be mapped. Use CDM abbreviated label instead.");
			}
			return result;
		}
	}
	
	public static String transformSpecimenTypeStatusToGbif(SpecimenTypeDesignationStatus status){
		if ( status == null){
			return null;
		}else{
			if (specimenTypeMapping == null){
				try {
					specimenTypeMapping = new TermMapping("specimenTypeStatusToGbif.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = specimenTypeMapping.getTerm(status.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("Specimen type status (" + status.getLabel() + ") could not be mapped. Use CDM status label.");
			}
			return result;
		}
	}
	
	
	public static String transformNameTypeStatusToGbif(NameTypeDesignationStatus status){
		if ( status == null){
			return null;
		}else{
			if (nameTypeMapping == null){
				try {
					nameTypeMapping = new TermMapping("nameTypeStatusToGbif.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = nameTypeMapping.getTerm(status.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("Name type status (" + status.getLabel() + ") could not be mapped. Use CDM status label.");
			}
			return result;
		}
	}

	public static String transformToGbifSex(DefinedTerm sex) {
		if ( sex == null){
			return null;
		}else{
			if (sexMapping == null){
				try {
					sexMapping = new TermMapping("sexToGbif.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = sexMapping.getTerm(sex.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("Sex (" + sex.getLabel() + ") could not be mapped. Use CDM status label.");
			}
			return result;
		}
	}

	public static String transformToGbifLifeStage(DefinedTerm stage) {
		if ( stage == null){
			return null;
		}else{
			if (lifeStageMapping == null){
				try {
					lifeStageMapping = new TermMapping("lifeStageToGbif.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = lifeStageMapping.getTerm(stage.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("Life stage (" + stage.getLabel() + ") could not be mapped. Use CDM status label.");
			}
			return result;
		}
	}

	public static String transformToGbifOccStatus(PresenceAbsenceTerm status) {
		if ( status == null){
			return null;
		}else{
			if (occStatusMapping == null){
				try {
					occStatusMapping = new TermMapping("presenceTermToGbifOccurrenceStatus.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = occStatusMapping.getTerm(status.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("PresenceAbsence term (" + status.getLabel() + ") could not be mapped to GBIF occurrence status. Use CDM status label.");
			}
			return result;
		}
	}

	public static String transformToGbifEstablishmentMeans(PresenceAbsenceTerm status) {
		if ( status == null){
			return null;
		}else{
			if (establishmentMeansMapping == null){
				try {
					establishmentMeansMapping = new TermMapping("presenceTermToGbifEstablishmentMeans.tsv");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			String result = establishmentMeansMapping.getTerm(status.getUuid());
			if (StringUtils.isBlank(result)){
				logger.info("PresenceAbsence term (" + status.getLabel() + ") could not be mapped to GBIF establishment means. Use CDM status label.");
			}
			return result;
		}
	}
	
}
