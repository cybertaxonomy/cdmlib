/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author a.mueller
 * @created 29.06.2008
 */
public abstract class NomRefDefaultCacheStrategyBase extends StrategyBase implements INomenclaturalReferenceCacheStrategy{
	private static final long serialVersionUID = -725290113353165022L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomRefDefaultCacheStrategyBase.class);
	
	protected String beforeYear = ". ";
	protected String beforeMicroReference = ": ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";	
	

	@Override
	public String getTokenizedNomenclaturalTitel(Reference ref) {
		String result =  getTitleWithoutYearAndAuthor(ref, true);
		result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
		result = addYear(result, ref, true);
		return result;
	}

	@Override
	public String getTitleCache(Reference nomenclaturalReference) {
		return getTitleCache(nomenclaturalReference, false);
	}

	@Override
	public String getAbbrevTitleCache(Reference nomenclaturalReference) {
		return getTitleCache(nomenclaturalReference, true);
	}
	
	private String getTitleCache(Reference ref, boolean isAbbrev) {
		//TODO needed?
		if (isAbbrev && ref.isProtectedAbbrevTitleCache()){
			return ref.getAbbrevTitleCache();
		}else if (!isAbbrev && ref.isProtectedTitleCache()){
			return ref.getTitleCache();
		}
		String result =  getTitleWithoutYearAndAuthor(ref, isAbbrev);
		result = addYear(result, ref, false);
		TeamOrPersonBase<?> team = ref.getAuthorTeam();
		
		if (team != null){
			String teamTitle = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(), team.getNomenclaturalTitle(), isAbbrev, true);
			if (teamTitle.length() > 0 ){
				String concat = isNotBlank(result) ? afterAuthor : "";
				result = team.getTitleCache() + concat + result;
			}
			
		}
		return result;
	}
	
	protected abstract String getTitleWithoutYearAndAuthor(Reference reference, boolean isAbbrev);

	
	@Override
	public String getCitation(Reference referenceBase) {
		StringBuilder stringBuilder = new StringBuilder();
		
		String nextConcat = "";
		
		TeamOrPersonBase<?> team = referenceBase.getAuthorTeam();
		if (team != null &&  ! (team.getTitleCache() == null) && ! team.getTitleCache().trim().equals("")){
			stringBuilder.append(team.getTitleCache() );
			nextConcat = afterAuthor;
		}
		
		String year = referenceBase.getYear();
		if (StringUtils.isNotBlank(year)){
			stringBuilder.append(nextConcat + year);
		}
		
		return stringBuilder.toString();
	}
	

	@Override
	public String getBeforeMicroReference(){
		return beforeMicroReference;
	}
	
	protected String addYear(String string, Reference nomRef, boolean useFullDatePublished){
		String result;
		if (string == null){
			return null;
		}
		String year = useFullDatePublished ? nomRef.getDatePublishedString() : nomRef.getYear();
		if (StringUtils.isBlank(year)){
			result = string + afterYear;
		}else{
			String concat = isBlank(string)  ? "" : string.endsWith(".")? " " : beforeYear;
			result = string + concat + year + afterYear;
		}
		return result;
	}
	
	@Override
	public String getNomenclaturalCitation(Reference nomenclaturalReference, String microReference) {
		if (nomenclaturalReference.isProtectedTitleCache()){
			return nomenclaturalReference.getTitleCache();
		}
		String result = getTokenizedNomenclaturalTitel(nomenclaturalReference);
		microReference = CdmUtils.Nz(microReference);
		if (StringUtils.isNotBlank(microReference)){
			microReference = getBeforeMicroReference() + microReference;
			if (microReference.endsWith(".")  && result.contains(INomenclaturalReference.MICRO_REFERENCE_TOKEN + ".") ){
				microReference = microReference.substring(0, microReference.length() - 1);
			}
		}
		result = result.replaceAll(INomenclaturalReference.MICRO_REFERENCE_TOKEN, microReference);
		if (result.startsWith(". ")){  //only year available, remove '. '
			result = result.substring(2);
		}
		return result;
	}

}
