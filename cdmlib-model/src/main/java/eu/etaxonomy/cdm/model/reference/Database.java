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
 * This class represents a database used as an information source. A database is
 * a structured collection of records or data.
 * <P>
 * This class corresponds, according to the TDWG ontology, partially to the
 * publication type term (from PublicationTypeTerm): "ComputerProgram".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:19
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Database")
@XmlRootElement(name = "Database")
@Entity
public class Database extends PublicationBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Database.class);

	/** 
	 * Creates a new empty database instance.
	 */
	public static Database NewInstance(){
		return new Database();
	}
	
	
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> database instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * database instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Database clone(){
		Database result = (Database)super.clone();
		//no changes to: -
		return result;
	}

}