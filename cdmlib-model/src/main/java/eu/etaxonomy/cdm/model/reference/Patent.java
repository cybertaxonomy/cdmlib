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

/**
 * This class represents patents. A patent is a document containing the set of
 * exclusive rights granted by a state to an inventor or his assignee for a
 * fixed period of time in exchange for a disclosure of an invention.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Patent".
 *  
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Patent")
@XmlRootElement(name = "Patent")
@Entity
public class Patent extends StrictReferenceBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Patent.class);
	
	/** 
	 * Creates a new empty patent instance
	 */
	public static Patent NewInstance(){
		Patent result = new Patent();
		return result;
	}
	
	/** 
	 * Clones <i>this</i> patent instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * patent instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Patent clone(){
		Patent result = (Patent)super.clone();
		//no changes to: -
		return result;
	}

}