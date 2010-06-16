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
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * This class represents an {@link eu.etaxonomy.cdm.model.common.IOriginalSource IOriginalSource}
 * that can be used with {@link eu.etaxonomy.cdm.model.description.DescriptionElementBase description elements}.
 * Additionally to the core functionally of IOriginalSource a {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}
 * can be stored that points to the name used in the source. This is needed because description always belong
 * to accepted taxa while the referenced citations may use synonym names.
 * </BR>
 * The use of "originalNameString" within a DescriptionElementSource has to be discussed.
 * In general this string is to be used for different representations of the sourced object. In this classes
 * context it could also stand for the string representation of the taxon name used in the source. This
 * may make sense if the taxon name is not available in the CDM and the user for some reason does not want 
 * to create a new ful {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}.
 *  
 * @author a.mueller
 * @created 18.09.2009
 * @version 1.0
 */
@XmlType(name = "DescriptionElementSource", propOrder = {
	    "sourcedObj",
	    "nameUsedInSource"
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
	
	public static DescriptionElementSource NewInstance(String id, String idNamespace, ReferenceBase citation){
		DescriptionElementSource result = NewInstance(id, idNamespace);
		result.setCitation(citation);		
		return result;
	}

	public static DescriptionElementSource NewInstance(String id, String idNamespace, ReferenceBase citation, String microCitation){
		DescriptionElementSource result = NewInstance(id, idNamespace);
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}
	
	public static DescriptionElementSource NewInstance(String id, String idNamespace, ReferenceBase citation, String microReference, TaxonNameBase nameUsedInSource, String originalNameString){
		DescriptionElementSource result = NewInstance(id, idNamespace, citation, microReference);
		result.setNameUsedInSource(nameUsedInSource);
		result.setOriginalNameString(originalNameString);
		return result;
	}
	
	public static DescriptionElementSource NewInstance(ReferenceBase citation, String microCitation){
		DescriptionElementSource result = NewInstance();
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

	public static DescriptionElementSource NewInstance(ReferenceBase citation, String microReference, TaxonNameBase nameUsedInSource, String originalNameString){
		DescriptionElementSource result = NewInstance(citation, microReference);
		result.setNameUsedInSource(nameUsedInSource);
		result.setOriginalNameString(originalNameString);
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
	
	@XmlElement(name = "nameUsedInSource")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private TaxonNameBase nameUsedInSource;
	
	private DescriptionElementSource(){
		
	}
	
	
// **************************  GETTER / SETTER ****************************************************/
	
	
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
	
	
	/**
	 * @return the taxonNameUsedInSource
	 */
	public TaxonNameBase getNameUsedInSource() {
		return nameUsedInSource;
	}

	/**
	 * @param nameUsedInReference the nameUsedInReference to set
	 */
	public void setNameUsedInSource(TaxonNameBase nameUsedInSource) {
		this.nameUsedInSource = nameUsedInSource;
	}


//*********************************** CLONE *********************************************************/
	
	
	/**
	 * Clones this original source and sets the clones sourced object to 'sourceObj'
	 * @see java.lang.Object#clone()
	 */
	public DescriptionElementSource clone(DescriptionElementBase sourcedObj) throws CloneNotSupportedException{
		DescriptionElementSource result = (DescriptionElementSource)clone();
		result.setSourcedObj(sourcedObj);
		return result;
	}
	
	
	
}
