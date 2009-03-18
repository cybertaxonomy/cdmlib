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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.CdDvdDefaultCacheStrategy;

/**
 * This class represents electronic publications the support of which are Cds 
 * (Compact Discs) or Dvds (Digital Versatile Discs). This class applies for Cds
 * or Dvds as a whole but not for parts of it.
 * CdDvd implements INomenclaturalReference as this seems to be allowed by the ICZN
 * (see http://www.iczn.org/electronic_publication.html)
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdDvd")
@XmlRootElement(name = "CdDvd")
@Entity
@Audited
public class CdDvd extends PublicationBase implements INomenclaturalReference, Cloneable{
	static Logger logger = Logger.getLogger(CdDvd.class);

	public static CdDvd NewInstance() {
		return new CdDvd();
	}
	
	
    @XmlTransient
    @Transient
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);

	
	protected CdDvd(){
		super();
		this.cacheStrategy = CdDvdDefaultCacheStrategy.NewInstance();
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation(java.lang.String)
	 */
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}
	

//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> Cd or Dvd instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * Cd or Dvd instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		CdDvd result = (CdDvd)super.clone();
		//no changes to: -
		return result;
	}

}