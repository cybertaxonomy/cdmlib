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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.StrategyBase;

public class BookSectionDefaultCacheStrategy <T extends BookSection> extends NomRefDefaultCacheStrategyBase<T>  implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategy.class);
	
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
		String result = bookSection.getInBook().getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
		//TODO beforeMicroReference should be the bookstrategy one's
		result = result.replace(beforeMicroReference +  INomenclaturalReference.MICRO_REFERENCE_TOKEN, INomenclaturalReference.MICRO_REFERENCE_TOKEN);
		result = "in " +  result;
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
		TeamOrPersonBase team = bookSection.getAuthorTeam();
		String bookAuthor = CdmUtils.Nz(team == null? "" : team.getTitleCache());
		result = bookAuthor + afterBookAuthor + result;
		result = inBook +  result;
		String title = CdmUtils.Nz(bookSection.getTitle());
		if (title.length() > 0){
			result = title + blank + result;
		}
		
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.NomRefDefaultCacheStrategyBase#getNomRefTitleWithoutYearAndAuthor(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	@Override
	protected String getNomRefTitleWithoutYearAndAuthor(T reference) {
		// not needed in BookSection
		logger.warn("Questionable procedure call. Procedure not implemented because not needed. ");
		return null;
	}
	

}
