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

import eu.etaxonomy.cdm.model.reference.Reference;

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

//********************************* FACTORY ********************************************************/

	public static IdentifiableSource NewInstance(OriginalSourceType type){
		return new IdentifiableSource(type);
	}

	public static IdentifiableSource NewDataImportInstance(String id){
		IdentifiableSource result = new IdentifiableSource(OriginalSourceType.Import);
		result.setIdInSource(id);
		return result;
	}

	public static IdentifiableSource NewDataImportInstance(String id, String idNamespace){
		IdentifiableSource result = NewDataImportInstance(id);
		result.setIdNamespace(idNamespace);
		return result;
	}

	public static IdentifiableSource NewDataImportInstance(String id, String idNamespace, Reference ref){
		IdentifiableSource result = NewDataImportInstance(id, idNamespace);
		result.setCitation(ref);
		return result;
	}

	public static IdentifiableSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation){
		IdentifiableSource result = NewInstance(type);
		result.setIdInSource(id);
		result.setIdNamespace(idNamespace);
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

	public static IdentifiableSource NewPrimarySourceInstance(Reference citation, String microCitation){
		IdentifiableSource result = NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

// ******************************** FIELDS ************************************/

	@XmlElement(name = "SourcedObject")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@Any(metaDef = "CdmBase",
	    	 metaColumn=@Column(name = "sourcedObj_type"),
	    	 fetch = FetchType.LAZY,
	    	 optional = false)
	@JoinColumn(name = "sourcedObj_id")
	@NotAudited

//	@org.hibernate.annotations.AnyMetaDef(name = "OriginalSource" ,
//        metaType="string",
//        idType="integer",
//        metaValues={
//        @MetaValue(value = "eu.etaxonomy.cdm.model.common.IdentifiableEntity", targetEntity = IdentifiableEntity.class),
//        @MetaValue(value = "eu.etaxonomy.cdm.model.description.DescriptionElementBase", targetEntity = DescriptionElementBase.class),
//    })
	private IdentifiableEntity<?> sourcedObj;

// ****************** CONSTRUCTOR ********************************/

	//for hibernate only
	private IdentifiableSource() {
	}

	private IdentifiableSource(OriginalSourceType type) {
		super(type);
	}

// ********************** GETTER /SETTER *****************************/

	@Override
	public IdentifiableEntity getSourcedObj() {
		return sourcedObj;
	}

	@Override
	public void setSourcedObj(IdentifiableEntity sourcedObj) {
		this.sourcedObj = sourcedObj;
	}

//*********************************** CLONE *********************************************************/

	/**
	 * Clones this original source and sets the clones sourced object to 'sourceObj'
	 * @see java.lang.Object#clone()
	 */
	public IdentifiableSource clone(IdentifiableEntity sourcedObj) throws CloneNotSupportedException{
		IdentifiableSource result = (IdentifiableSource)clone();
		result.setSourcedObj(sourcedObj);
		return result;
	}

	@Override
	public Object clone() throws CloneNotSupportedException{
		IdentifiableSource result = (IdentifiableSource)super.clone();

		//no changes to: sourcedObj
		return result;
	}


}
