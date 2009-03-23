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
import org.hibernate.envers.Audited;

/**
 * This (abstract) class represents isolated sections (parts, chapters or
 * papers) within a {@link PrintedUnitBase printed unit}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "SubReference".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:51
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SectionBase", propOrder = {
    "pages"
})
@XmlRootElement(name = "SectionBase")
@Entity
@Audited
public abstract class SectionBase extends StrictReferenceBase {
	
	static Logger logger = Logger.getLogger(SectionBase.class);
	
	@XmlElement(name = "Pages")
	private String pages;

	/**
	 * Returns the string representing the page(s) where the content of
	 * <i>this</i> section is located within the {@link PrintedUnitBase printed unit}.
	 * 
	 * @return  the string with the pages corresponding to <i>this</i> section
	 */
	public String getPages(){
		return this.pages;
	}

	/**
	 * @see #getPages()
	 */
	public void setPages(String pages){
		this.pages = pages;
	}

	/**
	 * Returns the {@link PrintedUnitBase printed unit} to which <i>this</i> section
	 * belongs.
	 * 
	 * @return  the printed unit containing <i>this</i> section
	 */
	@Transient
	public PrintedUnitBase getPrintedUnit(){
		logger.warn("Not yet implemented");
		return null;
	}

//*********** CLONE **********************************/	
	

	/** 
	 * Clones <i>this</i> section. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> section
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		SectionBase result = (SectionBase)super.clone();
		//no changes to: pages
		return result;
	}
}