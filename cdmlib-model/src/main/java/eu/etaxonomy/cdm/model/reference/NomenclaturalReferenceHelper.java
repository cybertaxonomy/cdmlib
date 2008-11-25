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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;

/**
 * This class provides several functionalities for {@link ReferenceBase references} that
 * are used for {@link name.TaxonNameBase#getNomenclaturalReference() nomenclatural} purposes.
 * 
 * @author a.mueller
 * @created 28.06.2008
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NomenclaturalReferenceHelper", propOrder = {
    "nomenclaturalReference"
})
@XmlRootElement(name = "NomenclaturalReferenceHelper")
class NomenclaturalReferenceHelper {
	private static final Logger logger = Logger.getLogger(NomenclaturalReferenceHelper.class);

	@XmlElement(name = "NomenclaturalReference")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private ReferenceBase nomenclaturalReference; 
	//private IReferenceBaseCacheStrategy<ReferenceBase> cacheStrategy; 
	
	/** 
	 * Creates a new nomenclatural reference helper instance with the given
	 * {@link INomenclaturalReference nomenclatural} {@link ReferenceBase reference}.
	 * 
	 * @param	nomRef	the reference used for nomenclatural purposes
	 * @see 			ReferenceBase
	 * @see 			INomenclaturalReference
	 * @see 			name.TaxonNameBase#getNomenclaturalReference()
	 * @see 			strategy.cache.reference.INomenclaturalReferenceCacheStrategy
	 */
	protected static NomenclaturalReferenceHelper NewInstance(INomenclaturalReference<? extends ReferenceBase> nomRef){
		NomenclaturalReferenceHelper result = new NomenclaturalReferenceHelper();
		result.nomenclaturalReference = (ReferenceBase)nomRef;
		return result;
	}
	
	
	private INomenclaturalReferenceCacheStrategy<ReferenceBase> getCacheStrategy(){	
		return (INomenclaturalReferenceCacheStrategy)nomenclaturalReference.cacheStrategy;
	}
	
	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors and other elements corresponding to the  {@link ReferenceBase reference}
	 * assigned to <i>this</i> nomenclatural reference helper.<BR>
	 * The returned string is build according to the corresponding
	 * {@link INomenclaturalReferenceCacheStrategy cache strategy}.
	 * 
	 * @see  strategy.cache.reference.INomenclaturalReferenceCacheStrategy
	 */
	@Transient
	public String getCitation(){
		//TODO
		logger.warn("getCitation not yet fully implemented");
		return getCacheStrategy().getTitleCache(nomenclaturalReference);
	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on the {@link ReferenceBase reference} assigned to
	 * <i>this</i> nomenclatural reference helper - including
	 * (abbreviated) title  but not authors - and on the given details.<BR>
	 * The returned string is build according to the corresponding
	 * {@link INomenclaturalReferenceCacheStrategy cache strategy}.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							corresponding to the nomenclatural reference of
	 * 							<i>this</i> nomenclatural reference helper
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					INomenclaturalReference#getNomenclaturalCitation(String)
	 * @see 					name.TaxonNameBase#getNomenclaturalReference()
	 * @see 					strategy.cache.reference.INomenclaturalReferenceCacheStrategy
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		if (nomenclaturalReference.isProtectedTitleCache()){
			return nomenclaturalReference.getTitleCache();
		}
		String result = getTokenizedFullNomenclaturalTitel();
		microReference = CdmUtils.Nz(microReference);
		if (! "".equals(microReference)){
			microReference = getCacheStrategy().getBeforeMicroReference() + microReference;
		}
		result = result.replaceAll(INomenclaturalReference.MICRO_REFERENCE_TOKEN, microReference);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 */
	@Transient
	/**
	 * Generates, according to the {@link INomenclaturalReferenceCacheStrategy cache strategy}
	 * assigned to the {@link ReferenceBase reference} of <i>this</i> nomenclatural reference helper,
	 * a string that identifies this reference and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute of the reference.
	 *
	 * @return  the string identifying the reference of <i>this</i> nomenclatural reference helper
	 * @see  	#getCitation()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 */
	public String generateTitle(){
		return getCacheStrategy().getTitleCache(nomenclaturalReference);
	}
	
	//
	@Transient
	private String getTokenizedFullNomenclaturalTitel() {
		if (getCacheStrategy() == null || ! (getCacheStrategy() instanceof INomenclaturalReferenceCacheStrategy) ){
			logger.warn("cacheStrategy == null of not instanceOf INomenclaturalReferenceCacheStrategy");
			return null;
		}
		return getCacheStrategy().getTokenizedNomenclaturalTitel(nomenclaturalReference);
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
