// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;

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
	public static DescriptionElementSource NewInstance(OriginalSourceType type){
		return new DescriptionElementSource(type);
	}

	public static DescriptionElementSource NewDataImportInstance(String id){
		DescriptionElementSource result = new DescriptionElementSource(OriginalSourceType.Import);
		result.setIdInSource(id);
		return result;
	}

	public static DescriptionElementSource NewDataImportInstance(String id, String idNamespace){
		DescriptionElementSource result = NewDataImportInstance(id);
		result.setIdNamespace(idNamespace);
		return result;
	}

	public static DescriptionElementSource NewDataImportInstance(String id, String idNamespace, Reference ref){
		DescriptionElementSource result = NewDataImportInstance(id, idNamespace);
		result.setCitation(ref);
		return result;
	}

	public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation){
		DescriptionElementSource result = NewInstance(type);
		result.setIdInSource(id);
		result.setIdNamespace(idNamespace);
		result.setCitation(citation);
		return result;
	}

	public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation){
		DescriptionElementSource result = NewInstance(type, id, idNamespace, citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

	public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microReference, TaxonNameBase nameUsedInSource, String originalNameString){
		DescriptionElementSource result = NewInstance(type, id, idNamespace, citation, microReference);
		result.setNameUsedInSource(nameUsedInSource);
		result.setOriginalNameString(originalNameString);
		return result;
	}

	public static DescriptionElementSource NewPrimarySourceInstance(Reference citation, String microCitation){
		DescriptionElementSource result = NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

	public static DescriptionElementSource NewPrimarySourceInstance(Reference citation, String microReference, TaxonNameBase nameUsedInSource, String originalNameString){
		DescriptionElementSource result = NewPrimarySourceInstance(citation, microReference);
		result.setNameUsedInSource(nameUsedInSource);
		result.setOriginalNameString(originalNameString);
		return result;
	}


//TODO evtl. JoinTable http://www.programcreek.com/java-api-examples/index.php?api=org.hibernate.annotations.AnyMetaDef

//	@XmlElement(name = "SourcedObject")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//	@Any(metaDef = "CdmBase",
//	    	 metaColumn=@Column(name = "sourcedObj_type"),
//	    	 fetch = FetchType.LAZY,
//	    	 optional = false)
//	@JoinTable(
//	        name = "DescriptionElementBase_OriginalSourceBase",
//	        joinColumns = @JoinColumn( name = "DescriptionElementBase_id" ),
//	        inverseJoinColumns = @JoinColumn( name = "sources_id" ) )
////	@JoinColumn(name = "sourcedObj_id")
//	@ManyToOne(fetch = FetchType.LAZY)
//	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
//	@NotAudited
//	private DescriptionElementBase sourcedObj;

	@XmlElement(name = "nameUsedInSource")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TaxonNameBase<?,?> nameUsedInSource;

//*********************** CONSTRUCTOR ******************************/

	//for hibernate use only
	private DescriptionElementSource(){
		super();
	}

	private DescriptionElementSource(OriginalSourceType type){
		super(type);
	}


// **************************  GETTER / SETTER ***************************/


	@Override
    public DescriptionElementBase getSourcedObj() {
		return sourcedObj;
	}

	@Override
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

	@Override
	public Object clone() throws CloneNotSupportedException{
		DescriptionElementSource result = (DescriptionElementSource)super.clone();

		//no changes to: sourcedObj
		return result;
	}



}
