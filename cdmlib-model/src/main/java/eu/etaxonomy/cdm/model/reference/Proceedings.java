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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

/**
 * This class represents conference proceedings. Proceedings are a
 * collection of academic papers that are published in the context of an
 * academic conference. Each paper typically is quite isolated from the other
 * papers in the proceedings. Proceedings are published in-house, by the
 * organizing institution of the conference, or via an academic publisher. 
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "ConferenceProceedings".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Proceedings", propOrder = {
//    "organization"
})
@XmlRootElement(name = "Proceedings")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Proceedings extends PrintedUnitBase<IReferenceBaseCacheStrategy<Proceedings>> implements Cloneable {
	private static final long serialVersionUID = -2096638066963321309L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Proceedings.class);
	
//	//The conference sponsor
//	@XmlElement(name = "Organization")
//	@Field(index=Index.TOKENIZED)
//	private String organization;
	
	
	/** 
	 * Creates a new empty proceedings instance.
	 * 
	 * @see #NewInstance(String)
	 */
	public static Proceedings NewInstance(){
		Proceedings result = new Proceedings();
		return result;
	}
	
	/** 
	 * Creates a new proceedings instance with the given organization
	 * responsible for the conference.
	 * 
	 * @see #NewInstance(String)
	 */
	public static Proceedings NewInstance(String organization){
		Proceedings result = NewInstance();
		result.setOrganization(organization);
		return result;
	}
	
	protected Proceedings(){
		super();
		this.type = ReferenceType.Proceedings;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<Proceedings>();
	}
	

	/**
	 * Returns the string representing the organization responsible for the
	 * conference in the context of which <i>this</i> conference proceedings
	 * has been printed.
	 * 
	 * @return  the string with the responsible organization
	 */
	public String getOrganization(){
		return this.organization;
	}
	/**
	 * @see #getOrganization()
	 */
	public void setOrganization(String organization){
		this.organization = organization;
	}

	
			
			
	/** 
	 * Clones <i>this</i> conference proceedings. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> conference
	 * proceedings by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
		@Override
		public Proceedings clone(){
			Proceedings result = (Proceedings)super.clone();
			//no changes to: organization
			return result;
		}

}