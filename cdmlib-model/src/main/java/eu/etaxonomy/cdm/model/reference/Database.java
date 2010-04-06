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
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Database extends PublicationBase<IReferenceBaseCacheStrategy<Database>> implements Cloneable {
	private static final long serialVersionUID = -7077612779393752878L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Database.class);

	/** 
	 * Creates a new empty database instance.
	 */
	public static Database NewInstance(){
		return new Database();
	}
	
	protected Database() {
		super();
		this.type = ReferenceType.Database;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<Database>();
	}

	
	
	
	/** 
	 * Clones <i>this</i> database instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * database instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Database clone(){
		Database result = (Database)super.clone();
		//no changes to: -
		return result;
	}

}