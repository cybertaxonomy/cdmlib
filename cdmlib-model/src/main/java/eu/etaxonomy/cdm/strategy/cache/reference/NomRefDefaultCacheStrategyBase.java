/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author a.mueller
 * @created 29.06.2008
 * @version 1.0
 */
public abstract class NomRefDefaultCacheStrategyBase<T extends ReferenceBase> extends StrategyBase implements  INomenclaturalReferenceCacheStrategy<T>{
	private static final long serialVersionUID = -725290113353165022L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomRefDefaultCacheStrategyBase.class);
	
	protected String beforeYear = ". ";
	protected String beforeMicroReference = ": ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTokenizedNomenclaturalTitel(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTokenizedNomenclaturalTitel(T nomenclaturalReference) {
		String result =  getNomRefTitleWithoutYearAndAuthor(nomenclaturalReference);
		result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
		result = addYear(result, nomenclaturalReference);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTitleCache(T nomenclaturalReference) {
		//TODO neede?
		if (nomenclaturalReference.isProtectedTitleCache()){
			return nomenclaturalReference.getTitleCache();
		}
		String result =  getNomRefTitleWithoutYearAndAuthor(nomenclaturalReference);
		result = addYear(result, nomenclaturalReference);
		TeamOrPersonBase<?> team = nomenclaturalReference.getAuthorTeam();
		if (team != null &&  ! (team.getTitleCache() == null) && ! team.getTitleCache().trim().equals("")){
			//String author = CdmUtils.Nz(team == null? "" : team.getTitleCache());
			result = team.getTitleCache() + afterAuthor + result;
		}
		return result;
	}
	
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
	
	protected String addYear(String string, T nomRef){
		String result;
		if (string == null){
			return null;
		}
		String year = CdmUtils.Nz(nomRef.getYear());
		if ("".equals(year)){
			result = string + afterYear;
		}else{
			result = string + beforeYear + year + afterYear;
		}
		return result;
	}
	
	protected abstract String getNomRefTitleWithoutYearAndAuthor(T reference);
	
	public String getNomenclaturalCitation(T nomenclaturalReference, String microReference) {
		if (nomenclaturalReference.isProtectedTitleCache()){
			return nomenclaturalReference.getTitleCache();
		}
		String result = getTokenizedNomenclaturalTitel(nomenclaturalReference);
		microReference = CdmUtils.Nz(microReference);
		if (! "".equals(microReference)){
			microReference = getBeforeMicroReference() + microReference;
		}
		result = result.replaceAll(INomenclaturalReference.MICRO_REFERENCE_TOKEN, microReference);
		return result;
	}
}
