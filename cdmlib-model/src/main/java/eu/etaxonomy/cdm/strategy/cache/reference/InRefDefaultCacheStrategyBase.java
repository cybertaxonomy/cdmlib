/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 15.03.2012
 */
public abstract class InRefDefaultCacheStrategyBase<T extends Reference> extends NomRefDefaultCacheStrategyBase<T> {
	private static final long serialVersionUID = -8418443677312335864L;

	private String afterInRefAuthor = ", ";

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTokenizedNomenclaturalTitel(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	@Override
	public String getTokenizedNomenclaturalTitel(T generic) {
		if (generic == null /* || generic.getInReference() == null */){
			return null;
		}
		Reference<?> inRef = generic.getInReference();
		String result;
		//use generics's publication date if it exists
		if ( (generic.getDatePublished() != null && generic.getDatePublished().getStart() != null) || inRef == null){
			GenericDefaultCacheStrategy<Reference> inRefStrategy = GenericDefaultCacheStrategy.NewInstance();
			result =  inRef == null ? "" : inRefStrategy.getNomRefTitleWithoutYearAndAuthor(inRef);
			result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
			result = addYear(result, generic);
		}else{
			//else use inRefs's publication date
			result = inRef.getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
			result = result.replace(beforeMicroReference +  INomenclaturalReference.MICRO_REFERENCE_TOKEN, INomenclaturalReference.MICRO_REFERENCE_TOKEN);
		}
		result = getInRefAuthorPart(generic.getInReference(), afterInRefAuthor) + result;
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

}
