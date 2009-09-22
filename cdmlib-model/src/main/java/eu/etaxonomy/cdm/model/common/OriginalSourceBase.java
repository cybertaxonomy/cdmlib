/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;

/**
 * Abstract base class for classes implementing {@link eu.etaxonomy.cdm.model.common.IOriginalSource IOriginalSource}.
 * @see eu.etaxonomy.cdm.model.common.IOriginalSource
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:22
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginalSource", propOrder = {
    "idInSource",
    "idNamespace"
})
@XmlRootElement(name = "OriginalSource")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(appliesTo="OriginalSourceBase")
public abstract class OriginalSourceBase<T extends ISourceable> extends ReferencedEntityBase implements Cloneable, IOriginalSource<T> {
	private static final long serialVersionUID = -1972959999261181462L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OriginalSourceBase.class);
	
	//The object's ID in the source, where the alternative string comes from
	@XmlElement(name = "IdInSource")
	private String idInSource;
	
	@XmlElement(name = "IdNamespace")
	private String idNamespace;


	/**
	 * Constructor
	 */
	protected OriginalSourceBase(){
		super();
	}

/* (non-Javadoc)
 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#getIdInSource()
 */
	
	public String getIdInSource(){
		return this.idInSource;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#setIdInSource(java.lang.String)
	 */
	public void setIdInSource(String idInSource){
		this.idInSource = idInSource;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#getIdNamespace()
	 */
	public String getIdNamespace() {
		return idNamespace;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#setIdNamespace(java.lang.String)
	 */
	public void setIdNamespace(String idNamespace) {
		this.idNamespace = idNamespace;
	}

	
//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		OriginalSourceBase result = (OriginalSourceBase)super.clone();
		
		//no changes to: idInSource, sourcedObj
		return result;
	}
	
	/**
	 * Clones this original source and sets the clones sourced object to 'sourceObj'
	 * @see java.lang.Object#clone()
	 */
	public OriginalSourceBase clone(IdentifiableEntity sourcedObj) throws CloneNotSupportedException{
		OriginalSourceBase result = (OriginalSourceBase)clone();
		result.setSourcedObj(sourcedObj);
		return result;
	}

}