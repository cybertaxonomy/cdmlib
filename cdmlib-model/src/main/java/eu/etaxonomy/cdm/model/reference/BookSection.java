/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:14
 */
@Entity
public class BookSection extends SectionBase implements INomenclaturalReference {
	private static final Logger logger = Logger.getLogger(BookSection.class);
	private Book inBook;
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);


	public static BookSection NewInstance(){
		BookSection result = new BookSection();
		return result;
	}
	
	public static BookSection NewInstance(Book inBook, String pages, String sectionTitle ){
		BookSection result = new BookSection();
		result.setInBook(inBook);
		result.setTitle(sectionTitle);
		result.setPages(pages);
		return result;
	}
	
	protected BookSection(){
		super();
		this.cacheStrategy = BookSectionDefaultCacheStrategy.NewInstance();
	}
	
	
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Book getInBook(){
		return this.inBook;
	}

	/**
	 * 
	 * @param inBook    inBook
	 */
	public void setInBook(Book inBook){
		this.inBook = inBook;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.StrictReferenceBase#getCitation()
	 */
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation(java.lang.String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
}