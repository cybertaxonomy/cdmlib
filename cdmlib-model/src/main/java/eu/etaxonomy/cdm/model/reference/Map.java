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
 * This class represents published maps from which information can be derived.
 * A map is a visual representation of an area.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Map".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Map")
@XmlRootElement(name = "Map")
@Entity
//@Audited
public class Map extends PublicationBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Map.class);

	/** 
	 * Creates a new empty map instance.
	 */
	public static Map NewInstance(){
		Map result = new Map();
		return result;
	}
	
	
	/** 
	 * Clones <i>this</i> map instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * map instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Map clone(){
		Map result = (Map)super.clone();
		//no changes to: -
		return result;
	}

}