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
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class BookSectionDefaultCacheStrategy <T extends ReferenceBase> extends NomRefDefaultCacheStrategyBase<T>  implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategy.class);
	
	public static final String UNDEFINED_BOOK = "- undefined book -";
	private String afterSectionAuthor = " - ";
	private String afterNomRefBookAuthor = ", ";
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
	@Override
	public String getTokenizedNomenclaturalTitel(T bookSection) {
		if (bookSection == null || bookSection.getInReference() == null){
			return null;
		}
		ReferenceBase inBook = bookSection.getInReference();
		String result;
		//use booksection's publication date if it exists
		if (bookSection.getDatePublished() != null && bookSection.getDatePublished().getStart() != null){
			BookDefaultCacheStrategy<ReferenceBase> bookStrategy = BookDefaultCacheStrategy.NewInstance();
			result =  bookStrategy.getNomRefTitleWithoutYearAndAuthor(inBook);
			result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
			result = addYear(result, bookSection);
		}else{
			//else use book's publication date
			result = inBook.getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
			result = result.replace(beforeMicroReference +  INomenclaturalReference.MICRO_REFERENCE_TOKEN, INomenclaturalReference.MICRO_REFERENCE_TOKEN);
		}
		result = getBookAuthorPart(bookSection.getInReference(), afterNomRefBookAuthor) + result;
		result = "in " +  result;
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	@Override
	public String getTitleCache(T bookSection) {
		boolean hasBook = (bookSection.getInBook() != null);
		String result;
		// get book part
		if (hasBook){
			result = bookSection.getInReference().getTitleCache();
		}else{
			result = "- undefined book -";
		}
		
		//in
		result = inBook +  result;
		
		//section title
		String title = CdmUtils.Nz(bookSection.getTitle());
		if (title.length() > 0){
			result = title + blank + result;
		}
		
		//section author
		TeamOrPersonBase<?> sectionTeam = bookSection.getAuthorTeam();
		String sectionAuthor = CdmUtils.Nz(sectionTeam == null ? "" : sectionTeam.getTitleCache());
		result = sectionAuthor + afterSectionAuthor + result;
		
		//date
		if (bookSection.getDatePublished() != null && ! bookSection.getDatePublished().isEmpty()){
			String bookSectionDate = bookSection.getDatePublished().toString();
			if (hasBook && bookSection.getInBook().getDatePublished() != null){
				TimePeriod bookDate = bookSection.getInBook().getDatePublished();
				String bookDateString = bookDate.getYear();
				int pos = StringUtils.lastIndexOf(result, bookDateString);
				if (pos > -1 ){
					result = result.substring(0, pos) + bookSectionDate + result.substring(pos + bookDateString.length());
				}else{
					logger.warn("BookDateString (" + bookDateString + ") could not be found in result (" + result +")");
				}
			}else{
				result = result + beforeYear + bookSectionDate;
			}
		}
		return result;
	}
	
	private String getBookAuthorPart(ReferenceBase book, String seperator){
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
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.NomRefDefaultCacheStrategyBase#getNomRefTitleWithoutYearAndAuthor(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	@Override
	protected String getNomRefTitleWithoutYearAndAuthor(T reference) {
		// not needed in BookSection
		logger.warn("Questionable procedure call. Procedure not implemented because not needed. ");
		return null;
	}
	

}
