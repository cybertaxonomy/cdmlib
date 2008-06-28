/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * @author a.mueller
 * @created 28.06.2008
 * @version 1.0
 */
class NomenclaturalReferenceHelper {
	private static final Logger logger = Logger.getLogger(NomenclaturalReferenceHelper.class);
	
	private ReferenceBase nomenclaturalReference; 
	private IReferenceBaseCacheStrategy<ReferenceBase> cacheStrategy; 
	
	protected static NomenclaturalReferenceHelper NewInstance(INomenclaturalReference<ReferenceBase> nomRef){
		NomenclaturalReferenceHelper result = new NomenclaturalReferenceHelper();
		result.nomenclaturalReference = (ReferenceBase)nomRef;
		result.cacheStrategy = result.nomenclaturalReference.cacheStrategy;
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.StrictReferenceBase#getCitation()
	 */
	@Transient
	public String getCitation(){
		//TODO
		logger.warn("getCitation not yet fully implemented");
		return cacheStrategy.getTitleCache(nomenclaturalReference);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation(java.lang.String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		String result = getTokenizedFullNomenclaturalTitel();
		result = result.replaceAll(INomenclaturalReference.MICRO_REFERENCE_TOKEN, microReference);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 */
	@Transient
	public String generateTitle(){
		return cacheStrategy.getTitleCache(nomenclaturalReference);
	}
	
	//
	private String getTokenizedFullNomenclaturalTitel() {
		if (cacheStrategy == null || ! (cacheStrategy instanceof INomenclaturalReferenceCacheStrategy) ){
			logger.warn("cacheStrategy == null of not instanceOf INomenclaturalReferenceCacheStrategy");
			return null;
		}
		return ((INomenclaturalReferenceCacheStrategy)cacheStrategy).getTokenizedNomenclaturalTitel(nomenclaturalReference);
	}
	
//	//
//	private String getTokenizedFullNomenclaturalTitel() {
//		if (tokenizedFullNomenclaturalTitel == null 
//				// FIXME || isDirty 
//				){ 
//			return ((INomenclaturalReferenceCacheStrategy)cacheStrategy).getTokenizedNomenclaturalTitel(this);
//			
//		}else{
//			return tokenizedFullNomenclaturalTitel;
//		}
//	}
	
//	
//	//for persistence
//	private void setTokenizedFullNomenclaturalTitel(String tokenizedFullNomenclaturalTitel) {
//		this.tokenizedFullNomenclaturalTitel = tokenizedFullNomenclaturalTitel;
//	}
}
