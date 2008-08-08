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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;

/**
 * This class represents books. A book is a  {@link PrintedUnitBase printed unit} usually
 * published by a publishing company.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "Book"
 * <li> "EditedBook"
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:13
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Book", propOrder = {
    "edition",
    "isbn",
    "nomRefBase"
})
@XmlRootElement(name = "Book")
@Entity
public class Book extends PrintedUnitBase implements INomenclaturalReference, Cloneable {
	
	private static final Logger logger = Logger.getLogger(Book.class);
	
    @XmlElement(name = "Edition")
	private String edition;

    @XmlElement(name = "ISBN")
	private String isbn;
	
    @XmlElement(name = "NomenclaturalReferenceBase")
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);


	
	/** 
	 * Class constructor: creates a new empty book instance
	 * only containing the {@link strategy.cache.reference.BookDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see strategy.cache.reference.BookDefaultCacheStrategy
	 */
	protected Book(){
		super();
		this.cacheStrategy = BookDefaultCacheStrategy.NewInstance();
	}
	
	/** 
	 * Creates a new empty book instance
	 * only containing the {@link strategy.cache.reference.BookDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #Book()
	 * @see strategy.cache.reference.BookDefaultCacheStrategy
	 */
	public static Book NewInstance(){
		return new Book();
	}

	
	/**
	 * Returns the string representing the edition of <i>this</i> book. A book which
	 * is published once more after having been editorially revised or updated
	 * corresponds to another superior edition as the book the content of which
	 * had to be revised or updated (previous edition). Different book editions
	 * have almost the same content but may differ in layout. Editions are
	 * therefore essential for accurate citations. 
	 * 
	 * @return  the string identifying the edition
	 */
	public String getEdition(){
		return this.edition;
	}
	/**
	 * @see #getEdition()
	 */
	public void setEdition(String edition){
		this.edition = edition;
	}

	/**
	 * Returns the string representing the ISBN (International Standard Book
	 * Number, a unique numerical commercial book identifier, based upon the
	 * 9-digit Standard Book Numbering code) of <i>this</i> book.
	 * 
	 * @return  the string representing the ISBN
	 */
	public String getIsbn(){
		return this.isbn;
	}
	/**
	 * @see #getIsbn()
	 */
	public void setIsbn(String isbn){
		this.isbn = isbn;
	}


	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, title, edition, volume, series, corresponding to
	 * <i>this</i> book.<BR>
	 * This method overrides the generic and inherited
	 * StrictReferenceBase#getCitation() method.
	 * 
	 * @see  NomenclaturalReferenceHelper#getCitation()
	 * @see  StrictReferenceBase#getCitation()
	 */
	@Override
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> book - including
	 * (abbreviated) title  but not authors - and on the given
	 * details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within <i>this</i> book
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					NomenclaturalReferenceHelper#getNomenclaturalCitation(String)
	 * @see  					INomenclaturalReference#getNomenclaturalCitation(String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/**
	 * Generates, according to the {@link strategy.cache.reference.BookDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> book, a string that identifies <i>this</i>
	 * book and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> book
	 * @see  	#getCitation()
	 * @see  	NomenclaturalReferenceHelper#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
	
	
//*********** CLONE **********************************/	
		
	/** 
	 * Clones <i>this</i> book. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> book
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the {@link StrictReferenceBase#clone() method} from StrictReferenceBase.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Book clone(){
		Book result = (Book)super.clone();
		//no changes to: edition, isbn
		return result;
	}


}