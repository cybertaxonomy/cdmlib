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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

/**
 * This class represents personal communications. A personal communication is a
 * non published document originally written for information exchange between
 * private persons. 
 * <P>
 * This class corresponds to: <ul>
 * <li> the term "Communication" from PublicationTypeTerm according to the TDWG ontology
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:43
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersonalCommunication")
@XmlRootElement(name = "PersonalCommunication")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class PersonalCommunication extends ReferenceBase<IReferenceBaseCacheStrategy<PersonalCommunication>> implements Cloneable {
	private static final long serialVersionUID = -2527724602502055339L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PersonalCommunication.class);
	
	/**
	 * Creates a new empty personal communication instance.
	 */
	public static PersonalCommunication NewInstance(){
		PersonalCommunication result = new PersonalCommunication();
		return result;
	}
	
	protected PersonalCommunication() {
		this.type = ReferenceType.PersonalCommunication;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<PersonalCommunication>();
	}

	

	/** 
	 * Clones <i>this</i> personal communication instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * personal communication instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PersonalCommunication clone(){
		PersonalCommunication result = (PersonalCommunication)super.clone();
		//no changes to: -
		return result;
	}
}