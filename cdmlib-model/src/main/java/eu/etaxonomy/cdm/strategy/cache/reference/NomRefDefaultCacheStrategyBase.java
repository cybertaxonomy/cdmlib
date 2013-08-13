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
 * @version 1.0
 */
public abstract class NomRefDefaultCacheStrategyBase<T extends Reference> extends StrategyBase implements  INomenclaturalReferenceCacheStrategy<T>{
	private static final long serialVersionUID = -725290113353165022L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomRefDefaultCacheStrategyBase.class);
	
	protected String beforeYear = ". ";
	protected String beforeMicroReference = ": ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";	
	

	@Override
	public String getTokenizedNomenclaturalTitel(T ref) {
		String result =  getTitleWithoutYearAndAuthor(ref, true);
		result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
		result = addYear(result, ref, true);
		return result;
	}

	@Override
	public String getTitleCache(T nomenclaturalReference) {
		return getTitleCache(nomenclaturalReference, false);
	}

	@Override
	public String getAbbrevTitleCache(T nomenclaturalReference) {
		return getTitleCache(nomenclaturalReference, true);
	}
	
	private String getTitleCache(T ref, boolean isAbbrev) {
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
				result = team.getTitleCache() + afterAuthor + result;
			}
			
		}
		return result;
	}
	
	protected abstract String getTitleWithoutYearAndAuthor(T reference, boolean isAbbrev);

	
	public String getCitation(T referenceBase) {
		StringBuilder stringBuilder = new StringBuilder();
		
		TeamOrPersonBase<?> team = referenceBase.getAuthorTeam();
		if (team != null &&  ! (team.getTitleCache() == null) && ! team.getTitleCache().trim().equals("")){
			//String author = CdmUtils.Nz(team == null? "" : team.getTitleCache());
			stringBuilder.append(team.getTitleCache() + afterAuthor);
		}
		
		String year = CdmUtils.Nz(referenceBase.getYear());
		if (!"".equals(year)){
			stringBuilder.append(beforeYear + year);
		}
		
		return stringBuilder.toString();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getBeforeMicroReference()
	 */
	public String getBeforeMicroReference(){
		return beforeMicroReference;
	}
	
	protected String addYear(String string, T nomRef, boolean useFullDatePublished){
		String result;
		if (string == null){
			return null;
		}
		String year = useFullDatePublished ? nomRef.getDatePublishedString() : nomRef.getYear();
		if (StringUtils.isBlank(year)){
			result = string + afterYear;
		}else{
			result = string + beforeYear + year + afterYear;
		}
		return result;
	}
	
	public String getNomenclaturalCitation(T nomenclaturalReference, String microReference) {
		if (nomenclaturalReference.isProtectedTitleCache()){
			return nomenclaturalReference.getTitleCache();
		}
		String result = getTokenizedNomenclaturalTitel(nomenclaturalReference);
		microReference = CdmUtils.Nz(microReference);
		if (StringUtils.isNotBlank(microReference)){
			microReference = getBeforeMicroReference() + microReference;
		}
		result = result.replaceAll(INomenclaturalReference.MICRO_REFERENCE_TOKEN, microReference);
		return result;
	}

}
