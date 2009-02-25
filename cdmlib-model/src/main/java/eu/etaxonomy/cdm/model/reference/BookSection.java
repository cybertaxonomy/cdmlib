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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;

/**
 * This class represents isolated sections (parts or chapters) within a {@link Book book}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "BookSection".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:14
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BookSection", propOrder = {
    "inBook"
})
@XmlRootElement(name = "BookSection")
@Entity
@Audited
public class BookSection extends SectionBase implements INomenclaturalReference, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1066199749700092670L;

	private static final Logger logger = Logger.getLogger(BookSection.class);
	
    @XmlElement(name = "BookSection")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private Book inBook;
	
    @XmlTransient
    @Transient
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);

	
	/** 
	 * Class constructor: creates a new empty book section instance only containing the
	 * {@link eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy
	 */
	protected BookSection(){
		super();
		this.cacheStrategy = BookSectionDefaultCacheStrategy.NewInstance();
	}
	

	/** 
	 * Creates a new empty book section instance only containing the
	 * {@link eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #NewInstance(Book, TeamOrPersonBase, String, String)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy
	 */
	public static BookSection NewInstance(){
		BookSection result = new BookSection();
		return result;
	}
	
	/** 
	 * Creates a new book section instance with its given book, title, pages and
	 * author (team) and its {@link eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	inBook			the book <i>this</i> book section belongs to
	 * @param	author			the team or person who wrote <i>this</i> book section
	 * @param	sectionTitle	the string representing the title of <i>this</i>
	 * 							book section
	 * @param	pages			the string representing the pages in the book
	 * 							where <i>this</i> book section can be found  
	 * @see 					#NewInstance()
	 * @see 					eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy
	 */
	public static BookSection NewInstance(Book inBook, TeamOrPersonBase author, String sectionTitle, String pages ){
		BookSection result = new BookSection();
		result.setInBook(inBook);
		result.setTitle(sectionTitle);
		result.setPages(pages);
		result.setAuthorTeam(author);
		return result;
	}
	
	
	/**
	 * Returns the {@link Book book} <i>this</i> book section belongs to.
	 * 
	 * @return  the book containing <i>this</i> book section
	 * @see 	Book
	 */
	public Book getInBook(){
		return this.inBook;
	}

	/**
	 * @see #getInBook()
	 */
	public void setInBook(Book inBook){
		this.inBook = inBook;
	}


	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, title, book authors, book title, pages, corresponding to <i>this</i>
	 * book section.<BR>
	 * This method overrides the generic and inherited getCitation method
	 * from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see  #getNomenclaturalCitation(String)
	 * @see  StrictReferenceBase#getCitation()
	 */
	@Override
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> book section - including
	 * (abbreviated) book title, book authors, book section title but not its
	 * authors - and on the given details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within t<i>this</i> book section
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					#getCitation()
	 */
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}
	
	 /** 
	  * If the publication date of a book section and it's inBook do differ this is usually 
	 * caused by the fact that a book has been published during a period, because originally 
	 * it consisted of several parts that only later where put together to one book.
	 * If so, the book section's publication date may be a point in time (year or month of year)
	 * whereas the books publication date may be a period of several years.
	 * Therefore a valid nomenclatural reference string should use the book sections 
	 * publication date rather then the book's publication date.
	 * 
	 * @see 	StrictReferenceBase#getDatePublished()
	 **/
	 @Override
	 // This method overrides StrictReferenceBase.getDatePublished() only to have 
	 // a specific Javadoc for BookSection.getDatePublished().
	public TimePeriod getDatePublished(){
		return super.getDatePublished();
	}


	/**
	 * Generates, according to the {@link strategy.cache.reference.BookSectionDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> book section, a string that identifies <i>this</i>
	 * book section and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited generateTitle method
	 * from {@link ReferenceBase ReferenceBase}.
	 *
	 * @return  the string identifying <i>this</i> book section
	 * @see  	#getCitation()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
	
//*********** CLONE **********************************/	

	/** 
	 * Clones <i>this</i> book section. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> book
	 * section by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		BookSection result = (BookSection)super.clone();
		result.nomRefBase = NomenclaturalReferenceHelper.NewInstance(result);
		//no changes to: inBook
		return result;
	}
}