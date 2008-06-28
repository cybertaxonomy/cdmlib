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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.StrategyBase;

public class BookSectionDefaultCacheStrategy <T extends BookSection> extends StrategyBase implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategy.class);
	
	private String beforeMicroReference = ": ";
	private String afterBookAuthor = " - ";
	private String inBook = "in ";
	private String blank = " ";
	
	final static UUID uuid = UUID.fromString("f9c53f20-addd-4d2f-9697-ef1fe727deba");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid; 
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
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTokenizedNomenclaturalTitel(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTokenizedNomenclaturalTitel(T bookSection) {
		if (bookSection.getInBook() == null){
			return null;
		}
		String result = "in " +  bookSection.getInBook().getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTitleCache(T bookSection) {
		if (bookSection.getInBook() == null){
			return null;
		}
		String result = bookSection.getInBook().getTitleCache();
		String bookAuthor = CdmUtils.Nz(bookSection.getInBook().getAuthorTeam().getTitleCache());
		result = bookAuthor + afterBookAuthor + result;
		result = inBook +  result;
		String title = CdmUtils.Nz(bookSection.getTitle());
		if (title.length() > 0){
			result = title + blank + result;
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getBeforeMicroReference()
	 */
	public String getBeforeMicroReference(){
		return beforeMicroReference;
	}

}
