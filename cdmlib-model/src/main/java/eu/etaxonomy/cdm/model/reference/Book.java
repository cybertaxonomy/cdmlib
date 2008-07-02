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
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:13
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Book", propOrder = {
    "edition",
    "isbn"
})
@Entity
public class Book extends PrintedUnitBase implements INomenclaturalReference, Cloneable {
	
	private static final Logger logger = Logger.getLogger(Book.class);
	
    @XmlElement(name = "Edition")
	private String edition;

    @XmlElement(name = "ISBN")
	private String isbn;
	
    @XmlElement(name = "NomRefBase")
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);


	public static Book NewInstance(){
		return new Book();
	}
	
	protected Book(){
		super();
		this.cacheStrategy = BookDefaultCacheStrategy.NewInstance();
	}
	
	
	public String getEdition(){
		return this.edition;
	}
	public void setEdition(String edition){
		this.edition = edition;
	}

	public String getIsbn(){
		return this.isbn;
	}
	public void setIsbn(String isbn){
		this.isbn = isbn;
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
	
	
//*********** CLONE **********************************/	
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PrintedUnitBase#clone()
	 */
	public Book clone(){
		Book result = (Book)super.clone();
		//no changes to: edition, isbn
		return result;
	}


}