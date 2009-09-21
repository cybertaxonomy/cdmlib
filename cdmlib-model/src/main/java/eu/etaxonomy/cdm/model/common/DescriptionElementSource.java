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

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 18.09.2009
 * @version 1.0
 */
@XmlType(name = "DescriptionElementSource", propOrder = {
	    "sourcedObj"
	})
@Entity
@Audited
public class DescriptionElementSource extends OriginalSourceBase<DescriptionElementBase>{
	private static final long serialVersionUID = -8487673428764273806L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DescriptionElementSource.class);
	

	/**
	 * Factory method
	 * @return
	 */
	public static DescriptionElementSource NewInstance(){
		return new DescriptionElementSource();
	}
	
	public static DescriptionElementSource NewInstance(String id){
		DescriptionElementSource result = new DescriptionElementSource();
		result.setIdInSource(id);
		return result;
	}

	public static DescriptionElementSource NewInstance(String id, String idNamespace){
		DescriptionElementSource result = NewInstance(id);
		result.setIdNamespace(idNamespace);
		return result;
	}
	
	public static DescriptionElementSource NewInstance(String id, String idNamespace, ReferenceBase citation, String microReference){
		DescriptionElementSource result = NewInstance(id, idNamespace);
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
	private DescriptionElementBase sourcedObj;
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#getSourcedObj()
	 */
	public DescriptionElementBase getSourcedObj() {
		return sourcedObj;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IOriginalSource#setSourcedObj(eu.etaxonomy.cdm.model.common.ISourceable)
	 */
	public void setSourcedObj(DescriptionElementBase sourcedObj) {
		this.sourcedObj = sourcedObj;
	}
	
}
