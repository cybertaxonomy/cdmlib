/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author a.mueller
 * @since 17.03.2021
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedSource", propOrder = {
        "nameUsedInSource"
    })
@XmlRootElement(name = "SourcedEntityBase")
@MappedSuperclass
@Audited
public abstract class NamedSourceBase extends OriginalSourceBase {

    private static final long serialVersionUID = 4262357256080305268L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DescriptionElementSource.class);

// ************************* FIELDS ********************************/

    @XmlElement(name = "nameUsedInSource")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private TaxonName nameUsedInSource;

//    public static DescriptionElementSource NewDataImportInstance(String id){
//        DescriptionElementSource result = new DescriptionElementSource(OriginalSourceType.Import);
//        result.setIdInSource(id);
//        return result;
//    }
//
//    public static DescriptionElementSource NewDataImportInstance(String id, String idNamespace){
//        DescriptionElementSource result = NewDataImportInstance(id);
//        result.setIdNamespace(idNamespace);
//        return result;
//    }
//
//    public static DescriptionElementSource NewDataImportInstance(String id, String idNamespace, Reference ref){
//        DescriptionElementSource result = NewDataImportInstance(id, idNamespace);
//        result.setCitation(ref);
//        return result;
//    }
//
//    public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation){
//        DescriptionElementSource result = NewInstance(type);
//        result.setIdInSource(id);
//        result.setIdNamespace(idNamespace);
//        result.setCitation(citation);
//        return result;
//    }
//
//    public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation){
//        DescriptionElementSource result = NewInstance(type, id, idNamespace, citation);
//        result.setCitationMicroReference(microCitation);
//        return result;
//    }
//
//    public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString){
//        DescriptionElementSource result = NewInstance(type, id, idNamespace, citation, microReference);
//        result.setNameUsedInSource(nameUsedInSource);
//        result.setOriginalNameString(originalNameString);
//        return result;
//    }
//
//    public static DescriptionElementSource NewInstance(OriginalSourceType type, String id, String idNamespace,
//            Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString, ICdmTarget target){
//        DescriptionElementSource result = NewInstance(type, id, idNamespace, citation, microReference, nameUsedInSource, originalNameString);
//        result.setCdmSource(target);
//        return result;
//    }
//
//    public static DescriptionElementSource NewAggregationInstance(ICdmTarget target){
//        DescriptionElementSource result = NewInstance(OriginalSourceType.Aggregation);
//        result.setCdmSource(target);
//        return result;
//    }

//
//    public static DescriptionElementSource NewPrimarySourceInstance(Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString){
//        DescriptionElementSource result = NewPrimarySourceInstance(citation, microReference);
//        result.setNameUsedInSource(nameUsedInSource);
//        result.setOriginalNameString(originalNameString);
//        return result;
//    }

//*********************** CONSTRUCTOR ******************************/

    //for hibernate use only
    /**
     * @deprecated for internal use only
     */
    @Deprecated
    protected NamedSourceBase(){}

    protected NamedSourceBase(OriginalSourceType type){
        super(type);
    }

// **************************  GETTER / SETTER ***************************/

    public TaxonName getNameUsedInSource() {
        return nameUsedInSource;
    }

    public void setNameUsedInSource(TaxonName nameUsedInSource) {
        this.nameUsedInSource = nameUsedInSource;
    }

// **************** EMPTY ************************/

    @Override
    public boolean checkEmpty(){
       return this.checkEmpty(false);
    }

    @Override
    public boolean checkEmpty(boolean excludeType){
        return super.checkEmpty(excludeType)
            && this.nameUsedInSource == null;
    }

//*********************************** CLONE *********************************************************/

    @Override
    public NamedSourceBase clone() throws CloneNotSupportedException{
        NamedSourceBase result = (NamedSourceBase)super.clone();

        //no changes
        return result;
    }

//*********************************** EQUALS *********************************************************/

    @Override
    public boolean equalsByShallowCompare(OriginalSourceBase other) {

        if(!super.equalsByShallowCompare(other)) {
            return false;
        }

        int a = -1;
        int b = -1;
        if(this.getNameUsedInSource() != null) {
            a = this.getNameUsedInSource().getId();
        }
        NamedSourceBase otherNamedSource = (NamedSourceBase)other;
        if(otherNamedSource.getNameUsedInSource() != null) {
            b = otherNamedSource.getNameUsedInSource().getId();
        }
        return a == b;
    }
}