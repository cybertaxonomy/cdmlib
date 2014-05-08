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

import eu.etaxonomy.cdm.model.reference.Reference;

public class SectionDefaultCacheStrategy extends InRefDefaultCacheStrategyBase implements INomenclaturalReferenceCacheStrategy {
	private static final long serialVersionUID = -4534389083526558368L;
	private static final Logger logger = Logger.getLogger(SectionDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("a7150cd3-107b-4169-ac84-71ffe3219152");
	
	private static final String inRefTypeStr = "section";

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
	public String getTokenizedNomenclaturalTitel(Reference generic) {
		return super.getTokenizedNomenclaturalTitel(generic, inRefIsObligatory);
	}

	@Override
	public String getTitleCache(Reference thisRef) {
		return super.getTitleCache(thisRef, inRefIsObligatory, false);
	}
	
	@Override
	public String getAbbrevTitleCache(Reference thisRef) {
		return super.getTitleCache(thisRef, inRefIsObligatory, true);
	}
	

}
