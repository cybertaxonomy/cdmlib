// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Any;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * This class represents an {@link eu.etaxonomy.cdm.model.common.IOriginalSource IOriginalSource}
 * that can be used with {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity identifiable entity}.
 * 
 * @see eu.etaxonomy.cdm.model.common.IOriginalSource
 * 
 * @author a.mueller
 * @created 18.09.2009
 * @version 1.0
 */
@XmlType(name = "IdentifiableSource", propOrder = {
	    "sourcedObj"
	})
@Entity
@Audited
public class IdentifiableSource extends OriginalSourceBase<IdentifiableEntity>{
	private static final long serialVersionUID = -8487673428764273806L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IdentifiableSource.class);
	

	/**
	 * Factory method
	 * @return
	 */
	public static IdentifiableSource NewInstance(){
		return new IdentifiableSource();
	}
	
	public static IdentifiableSource NewInstance(String id){
		IdentifiableSource result = new IdentifiableSource();
		result.setIdInSource(id);
		return result;
	}

	public static IdentifiableSource NewInstance(String id, String idNamespace){
		IdentifiableSource result = NewInstance(id);
		result.setIdNamespace(idNamespace);
		return result;
	}
	
	public static IdentifiableSource NewInstance(String id, String idNamespace, ReferenceBase citation, String microReference){
		IdentifiableSource result = NewInstance(id, idNamespace);
		result.setCitation(citation);
		result.setCitationMicroReference(microReference);
		return result;
	}
	
	@XmlElement(name = "SourcedObject")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@Any(metaDef = "CdmBase",
	    	 metaColumn=@Column(name = "sourcedObj_type"),
	    	 fetch = FetchType.LAZY,
	    	 optional = false)
	@JoinColumn(name = "sourcedObj_id")
	@NotAudited
	private IdentifiableEntity sourcedObj;


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#getSourcedObj()
	 */
	public IdentifiableEntity getSourcedObj() {
		return sourcedObj;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#setSourcedObj(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
	 */
	public void setSourcedObj(IdentifiableEntity sourcedObj) {
		this.sourcedObj = sourcedObj;
	}
	
	/**
	 * Clones this original source and sets the clones sourced object to 'sourceObj'
	 * @see java.lang.Object#clone()
	 */
	public IdentifiableSource clone(IdentifiableEntity sourcedObj) throws CloneNotSupportedException{
		IdentifiableSource result = (IdentifiableSource)clone();
		result.setSourcedObj(sourcedObj);
		return result;
}

	
}
