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
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
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

	private static final Logger logger = Logger.getLogger(NomRefDefaultCacheStrategyBase.class);

	protected String beforeYear = ". ";
	protected String beforeMicroReference = ": ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";


	/**
	 * Returns the nomenclatural title with micro reference represented as token
	 * which can later be replaced by the real data.
	 *
	 * @see INomenclaturalReference#MICRO_REFERENCE_TOKEN
	 *
	 * @param ref The reference
	 * @return
	 */
	protected String getTokenizedNomenclaturalTitel(Reference ref) {
		String result = getTitleWithoutYearAndAuthor(ref, true);
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
		TeamOrPersonBase<?> team = ref.getAuthorship();

		if (team != null){
			String teamTitle = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(), team.getNomenclaturalTitle(), isAbbrev, true);
			if (teamTitle.length() > 0 ){
				String concat = isNotBlank(result) ? afterAuthor : "";
				result = teamTitle + concat + result;
			}

		}
		return result;
	}

	protected abstract String getTitleWithoutYearAndAuthor(Reference<?> reference, boolean isAbbrev);


	@Override
	public String getCitation(Reference referenceBase) {
		StringBuilder stringBuilder = new StringBuilder();

		String nextConcat = "";

		TeamOrPersonBase<?> team = referenceBase.getAuthorship();
		if (team != null &&  isNotBlank(team.getTitleCache())){
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
		if (isBlank(year)){
			result = string + afterYear;
		}else{
			String concat = isBlank(string)  ? "" : string.endsWith(".")  ? " " : beforeYear;
			result = string + concat + year + afterYear;
		}
		return result;
	}

	@Override
	public String getNomenclaturalCitation(Reference nomenclaturalReference, String microReference) {
		if (nomenclaturalReference.isProtectedAbbrevTitleCache()){
		    nomenclaturalReference = HibernateProxyHelper.deproxy(nomenclaturalReference, Reference.class);
			String cache = nomenclaturalReference.getAbbrevTitleCache();
			return handleDetailAndYearForPreliminary(nomenclaturalReference, cache, microReference);

		}
		String result = getTokenizedNomenclaturalTitel(nomenclaturalReference);
		//if no data is available and only titleCache is protected take the protected title
		//this is to avoid empty cache if someone forgets to set also the abbrevTitleCache
		//we need to think about handling protected not separate for abbrevTitleCache  and titleCache
		if (result.equals(INomenclaturalReference.MICRO_REFERENCE_TOKEN) && nomenclaturalReference.isProtectedTitleCache() ){
			String cache = nomenclaturalReference.getTitleCache();
			return handleDetailAndYearForPreliminary(nomenclaturalReference, cache, microReference);
		}

		microReference = Nz(microReference);
		if (StringUtils.isNotBlank(microReference)){
			microReference = getBeforeMicroReference() + microReference;
			if (microReference.endsWith(".")  && result.contains(INomenclaturalReference.MICRO_REFERENCE_TOKEN + ".") ){
				microReference = microReference.substring(0, microReference.length() - 1);
			}
		}
		result = replaceMicroRefToken(microReference, result);
		if (result.startsWith(". ")){  //only year available, remove '. '
			result = result.substring(2);
		}
		return result;
	}

	/**
	 * @param microReference
	 * @param result
	 * @return
	 */
	private String replaceMicroRefToken(String microReference, String string) {
		int index = string.indexOf(INomenclaturalReference.MICRO_REFERENCE_TOKEN);

		if (index > -1){
			String before = string.substring(0, index);
			String after = string.substring(index + INomenclaturalReference.MICRO_REFERENCE_TOKEN.length() );
			String localMicroReference = microReference.trim();   //needed ?
			if (after.length() > 0){
				if (  ("".equals(localMicroReference) && before.endsWith(after.substring(0,1)) || localMicroReference.endsWith(after.substring(0,1)))){
					after = after.substring(1);
				}
			}
			String result = before + localMicroReference + after;
			return result;
		}else{
			return string;
		}
	}

	/**
	 * @param nomenclaturalReference
	 * @param microRef
	 * @return
	 */
	private String handleDetailAndYearForPreliminary(Reference<?> nomenclaturalReference, String cache, String microReference) {
		String microRef = isNotBlank(microReference) ? getBeforeMicroReference() + microReference : "";
		if (cache == null){
			logger.warn("Cache is null. This should never be the case.");
			cache = "";
		}
		String	result = cache + (cache.contains(microRef) ? "" : microRef);

		String date = nomenclaturalReference.getDatePublishedString();
		if (isNotBlank(date) && ! result.contains(date)){
			result = result + beforeYear + date;
		}
		return result;
	}

}
