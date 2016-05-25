/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

public class SectionDefaultCacheStrategy extends InRefDefaultCacheStrategyBase implements INomenclaturalReferenceCacheStrategy {
	private static final long serialVersionUID = -4534389083526558368L;
	private static final Logger logger = Logger.getLogger(SectionDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("a7150cd3-107b-4169-ac84-71ffe3219152");

	private static final String inRefTypeStr = "in reference";

	private static final boolean inRefIsObligatory = true;

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
	protected String getInRefType() {
		return inRefTypeStr;
	}

	/**
	 * Factory method
	 * @return
	 */
	public static SectionDefaultCacheStrategy NewInstance(){
		return new SectionDefaultCacheStrategy();
	}

	/**
	 * Constructor
	 */
	private SectionDefaultCacheStrategy(){
		super();
	}

	@Override
	protected String getTitleWithoutYearAndAuthor(Reference reference, boolean isAbbrev) {
		// not needed in Section
		logger.warn("Questionable procedure call. Procedure not implemented because not needed. ");
		return null;
	}

	@Override
	public String getTokenizedNomenclaturalTitel(Reference section) {
		//generic == null
		if (section == null /* || generic.getInReference() == null */){
			return null;
		}
		Reference inRef = CdmBase.deproxy(section.getInReference(), Reference.class);
		if (inRef != null && inRef.getInReference() == null){
			//this is a simple inRef (not an in-in-Ref) which can be handled by InRefDefaultCacheStrategy
			return super.getTokenizedNomenclaturalTitel(section, inRefIsObligatory);
		}

		Reference inInRef = CdmBase.deproxy(inRef.getInReference(), Reference.class);
		String result;

		IReferenceBaseCacheStrategy inRefStrategy = inInRef.getCacheStrategy();
		if (! (inRefStrategy instanceof NomRefDefaultCacheStrategyBase)){
			inRefStrategy= inRef.getCacheStrategy();
			if (! (inRefStrategy instanceof NomRefDefaultCacheStrategyBase)){
				logger.warn("Neither inReference nor inInReference is a nomenclatural reference. This is not correct or not handled yet. Generic Cache Strategy used instead");
				inRefStrategy = ReferenceType.Generic.getCacheStrategy();
				result = ((NomRefDefaultCacheStrategyBase)inRefStrategy).getTitleWithoutYearAndAuthor(inInRef, true);
				//FIXME: vol. etc., http://dev.e-taxonomy.eu/trac/ticket/2862  (comment taken from super.getTokenizedNomenclaturalTitel())
			}else{
				result = ((NomRefDefaultCacheStrategyBase)inRefStrategy).getTitleWithoutYearAndAuthor(inRef, true);
			}
		}else{
			result = ((NomRefDefaultCacheStrategyBase)inRefStrategy).getTitleWithoutYearAndAuthor(inInRef, true);
		}
		result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;

		Reference dataReference = (section.hasDatePublished() ? section : inRef.hasDatePublished() ? inRef : inInRef);

		result = addYear(result, dataReference, true);

		result = getInRefAuthorPart(inInRef, afterInRefAuthor) + result;
		if (! result.startsWith("in ")){
			result = "in " +  result;
		}
		return result;
	}

	@Override
	public String getTitleCache(Reference thisRef) {
		return super.getTitleCache(thisRef, inRefIsObligatory, false);
	}

	@Override
	public String getAbbrevTitleCache(Reference thisRef) {
		return super.getTitleCache(thisRef, inRefIsObligatory, true);
	}

	@Override  //only for testing
	public String getNomenclaturalCitation(Reference nomenclaturalReference, String microReference){
		return super.getNomenclaturalCitation(nomenclaturalReference, microReference);
	}

}
