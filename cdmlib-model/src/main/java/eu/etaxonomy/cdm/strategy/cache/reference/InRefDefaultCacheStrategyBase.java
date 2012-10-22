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
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 15.03.2012
 */
public abstract class InRefDefaultCacheStrategyBase<T extends Reference> extends NomRefDefaultCacheStrategyBase<T> {
	private static final long serialVersionUID = -8418443677312335864L;
	private static final Logger logger = Logger.getLogger(InRefDefaultCacheStrategyBase.class);
	
	private String inSeperator = "in ";
	private String afterSectionAuthor = " - ";
	private String blank = " ";
	
	
	protected abstract String getInRefType();
	
	private String afterInRefAuthor = ", ";

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTokenizedNomenclaturalTitel(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	@Override
	public String getTokenizedNomenclaturalTitel(T generic) {
		return getTokenizedNomenclaturalTitel(generic, false);
	}
	
	protected String getTokenizedNomenclaturalTitel(T thisRef, boolean inRefIsObligatory) {
		//generic == null
		if (thisRef == null /* || generic.getInReference() == null */){
			return null;
		}

		Reference<?> inRef = thisRef.getInReference();
		
		//inRef == null
		if (! inRefIsObligatory && inRef == null){
			return super.getTokenizedNomenclaturalTitel(thisRef);
		}
		
		String result;
		//use generics's publication date if it exists
		if (inRef == null ||  (thisRef.hasDatePublished() ) ){
			GenericDefaultCacheStrategy<Reference> inRefStrategy = GenericDefaultCacheStrategy.NewInstance();
			result =  inRef == null ? "" : inRefStrategy.getNomRefTitleWithoutYearAndAuthor(inRef);
			result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
			result = addYear(result, thisRef, true);
		}else{
			//else use inRefs's publication date
			result = inRef.getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
			if (result != null){
				result = result.replace(beforeMicroReference +  INomenclaturalReference.MICRO_REFERENCE_TOKEN, INomenclaturalReference.MICRO_REFERENCE_TOKEN);
			}
		}
		//FIXME: vol. etc., http://dev.e-taxonomy.eu/trac/ticket/2862
		
		result = getInRefAuthorPart(thisRef.getInReference(), afterInRefAuthor) + result;
		result = "in " +  result;
		return result;
	}
	
	protected String getInRefAuthorPart(Reference book, String seperator){
		if (book == null){
			return "";
		}
		TeamOrPersonBase<?> team = book.getAuthorTeam();
		String result = CdmUtils.Nz( team == null ? "" : team.getTitleCache());
		if (! result.trim().equals("")){
			result = result + seperator;	
		}
		return result;
	}
	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	@Override
	public String getTitleCache(T thisRef) {
		return getTitleCache(thisRef, false);
	}	
	
	protected String getTitleCache(T thisRef, boolean inRefIsObligatory) {
		boolean hasInRef = (thisRef.getInReference() != null);
		String result;
		// get inRef part
		if (hasInRef){
			result = thisRef.getInReference().getTitleCache();
		}else{
			if ( ! inRefIsObligatory){
				return super.getTitleCache(thisRef);
			}
			result = String.format("- undefined %s -", getInRefType());
		}
		
		//in
		result = inSeperator +  result;
		
		//section title
		String title = CdmUtils.Nz(thisRef.getTitle());
		if (title.length() > 0){
			result = title + blank + result;
		}
		
		//section author
		TeamOrPersonBase<?> thisRefTeam = thisRef.getAuthorTeam();
		String thisRefAuthor = CdmUtils.Nz(thisRefTeam == null ? "" : thisRefTeam.getTitleCache());
		result = CdmUtils.concat(afterSectionAuthor, thisRefAuthor, result);
		
		//date
		if (thisRef.getDatePublished() != null && ! thisRef.getDatePublished().isEmpty()){
			String thisRefDate = thisRef.getDatePublished().toString();
			if (hasInRef && thisRef.getInBook().getDatePublished() != null){
				TimePeriod inRefDate = thisRef.getInReference().getDatePublished();
				String inRefDateString = inRefDate.getYear();
				if (CdmUtils.isNotEmpty(inRefDateString)){
					int pos = StringUtils.lastIndexOf(result, inRefDateString);
					if (pos > -1 ){
						result = result.substring(0, pos) + thisRefDate + result.substring(pos + inRefDateString.length());
					}else{
						logger.warn("InRefDateString (" + inRefDateString + ") could not be found in result (" + result +")");
					}
				}else{
					result = result + beforeYear + thisRefDate + afterYear;
				}
			}else{
				result = result + beforeYear + thisRefDate + afterYear;
			}
		}
		return result;
	}
	


}
