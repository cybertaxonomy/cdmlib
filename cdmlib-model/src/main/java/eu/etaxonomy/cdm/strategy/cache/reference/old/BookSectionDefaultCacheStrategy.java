/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference.old;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;

public class BookSectionDefaultCacheStrategy extends InRefDefaultCacheStrategyBase implements INomenclaturalReferenceCacheStrategy {
	private static final long serialVersionUID = 7293886681984614996L;

	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategy.class);
	final static UUID uuid = UUID.fromString("f9c53f20-addd-4d2f-9697-ef1fe727deba");
	
	private static final String inRefTypeStr = "book";

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
	public static BookSectionDefaultCacheStrategy NewInstance(){
		return new BookSectionDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private BookSectionDefaultCacheStrategy(){
		super();
	}

	@Override
	protected String getTitleWithoutYearAndAuthor(Reference reference, boolean isAbbrev) {
		// not needed in BookSection
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
	public String getFullAbbrevTitleString(Reference thisRef) {
		return super.getTitleCache(thisRef, inRefIsObligatory, true);
	}
	

}
