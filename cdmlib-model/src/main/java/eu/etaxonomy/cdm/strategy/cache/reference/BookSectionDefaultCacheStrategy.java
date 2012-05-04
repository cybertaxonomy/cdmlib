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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;

public class BookSectionDefaultCacheStrategy <T extends Reference> extends InRefDefaultCacheStrategyBase<T>  implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final long serialVersionUID = 7293886681984614996L;

	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategy.class);
	final static UUID uuid = UUID.fromString("f9c53f20-addd-4d2f-9697-ef1fe727deba");
	
	private static final String inRefTypeStr = "book";

	private static final boolean inRefIsObligatory = true;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
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

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.NomRefDefaultCacheStrategyBase#getNomRefTitleWithoutYearAndAuthor(eu.etaxonomy.cdm.model.reference.Reference)
	 */
	@Override
	protected String getNomRefTitleWithoutYearAndAuthor(T reference) {
		// not needed in BookSection
		logger.warn("Questionable procedure call. Procedure not implemented because not needed. ");
		return null;
	}


	@Override
	public String getTokenizedNomenclaturalTitel(T generic) {
		return super.getTokenizedNomenclaturalTitel(generic, inRefIsObligatory);
	}


	@Override
	public String getTitleCache(T thisRef) {
		return super.getTitleCache(thisRef, inRefIsObligatory);
	}
	
	
	

}
