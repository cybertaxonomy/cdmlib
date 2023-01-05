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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.annotation.ReferenceOrSpecimen;

/**
 * This class represents an {@link eu.etaxonomy.cdm.model.reference.IOriginalSource IOriginalSource}
 * that can be used with {@link eu.etaxonomy.cdm.model.description.DescriptionElementBase description elements}.
 * Additionally to the core functionally of IOriginalSource a {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name}
 * can be stored that points to the name used in the source. This is needed because description always belong
 * to accepted taxa while the referenced citations may use synonym names.
 * </BR>
 * For discussion on originalInfo see #10097.
*
 * @author a.mueller
 * @since 18.09.2009
 */
@XmlType(name = "DescriptionElementSource", propOrder = {
	    "sourcedElement",
	    "specimen"
	})
@Entity
@Audited
@ReferenceOrSpecimen
public class DescriptionElementSource extends NamedSourceBase{

    private static final long serialVersionUID = -8487673428764273806L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

// ************************* FIELDS ********************************/

    @XmlElement(name = "sourcedElement")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private DescriptionElementBase sourcedElement;

    //#10194
    @XmlElement(name = "specimen")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private SpecimenOrObservationBase specimen;

//************************* FACTORY ******************************/

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

	public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microReference, TaxonName nameUsedInSource, String originalInfo){
		DescriptionElementSource result = NewInstance(type, id, idNamespace, citation, microReference);
		result.setNameUsedInSource(nameUsedInSource);
		result.setOriginalInfo(originalInfo);
		return result;
	}

    public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace,
            Reference citation, String microReference, TaxonName nameUsedInSource, String originalInfo, ICdmTarget target){
        DescriptionElementSource result = NewInstance(type, id, idNamespace, citation, microReference, nameUsedInSource, originalInfo);
        result.setCdmSource(target);
        return result;
    }

    public static DescriptionElementSource NewAggregationInstance(ICdmTarget target){
        DescriptionElementSource result = NewInstance(OriginalSourceType.Aggregation);
        result.setCdmSource(target);
        return result;
    }

	public static DescriptionElementSource NewPrimarySourceInstance(Reference citation, String microCitation){
		DescriptionElementSource result = NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

	public static DescriptionElementSource NewPrimarySourceInstance(Reference citation, String microReference, TaxonName nameUsedInSource, String originalInfo){
		DescriptionElementSource result = NewPrimarySourceInstance(citation, microReference);
		result.setNameUsedInSource(nameUsedInSource);
		result.setOriginalInfo(originalInfo);
		return result;
	}

//*********************** CONSTRUCTOR ******************************/

	//for hibernate use only
	/**
	 * @deprecated for internal use only
	 */
	@Deprecated
	protected DescriptionElementSource(){
	}

	private DescriptionElementSource(OriginalSourceType type){
		super(type);
	}

//***************** GETTER / SETTER ****************************/

    public DescriptionElementBase getSourcedElement() {
        return sourcedElement;
    }

    public void setSourcedElement(DescriptionElementBase sourcedElement) {
        if (this.sourcedElement != sourcedElement){
            this.sourcedElement = sourcedElement;
            if (sourcedElement != null){
                sourcedElement.addSource(this);
            }
        }
    }

    public SpecimenOrObservationBase getSpecimen() {
        return specimen;
    }
    public void setSpecimen(SpecimenOrObservationBase specimen) {
        this.specimen = specimen;
    }

//*********************************** CLONE *********************************************************/

	@Override
	public DescriptionElementSource clone() throws CloneNotSupportedException{
		DescriptionElementSource result = (DescriptionElementSource)super.clone();
		//we don't expect the source to belong to the same description element
        result.sourcedElement = null;

		//no changes
		return result;
	}
}