/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author a.mueller
 * @since 17.03.2021
 */
@XmlType(name = "NamedSource", propOrder = {
})
@Entity
@Audited
public class NamedSource extends NamedSourceBase {

    private static final long serialVersionUID = 6778434032127847851L;

    /**
     * Factory method
     * @return
     */
    public static NamedSource NewInstance(OriginalSourceType type){
        return new NamedSource(type);
    }

    public static NamedSource NewPrimarySourceInstance(Reference citation, String microCitation){
        NamedSource result = NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
        result.setCitation(citation);
        result.setCitationMicroReference(microCitation);
        return result;
    }

    public static NamedSource NewDataImportInstance(String id){
        NamedSource result = new NamedSource(OriginalSourceType.Import);
        result.setIdInSource(id);
        return result;
    }

    public static NamedSource NewDataImportInstance(String id, String idNamespace){
        NamedSource result = NewDataImportInstance(id);
        result.setIdNamespace(idNamespace);
        return result;
    }

    public static NamedSource NewDataImportInstance(String id, String idNamespace, Reference ref){
        NamedSource result = NewDataImportInstance(id, idNamespace);
        result.setCitation(ref);
        return result;
    }

    public static NamedSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation){
        NamedSource result = NewInstance(type);
        result.setIdInSource(id);
        result.setIdNamespace(idNamespace);
        result.setCitation(citation);
        return result;
    }

    public static NamedSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation){
        NamedSource result = NewInstance(type, id, idNamespace, citation);
        result.setCitationMicroReference(microCitation);
        return result;
    }

    public static NamedSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString){
        NamedSource result = NewInstance(type, id, idNamespace, citation, microReference);
        result.setNameUsedInSource(nameUsedInSource);
        result.setOriginalNameString(originalNameString);
        return result;
    }

    public static NamedSource NewInstance(OriginalSourceType type, String id, String idNamespace,
        Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString, ICdmTarget target){
        NamedSource result = NewInstance(type, id, idNamespace, citation, microReference, nameUsedInSource, originalNameString);
        result.setCdmSource(target);
        return result;
    }

    public static NamedSource NewAggregationInstance(ICdmTarget target){
        NamedSource result = NewInstance(OriginalSourceType.Aggregation);
        result.setCdmSource(target);
        return result;
    }


    public static NamedSource NewPrimarySourceInstance(Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString){
        NamedSource result = NewPrimarySourceInstance(citation, microReference);
        result.setNameUsedInSource(nameUsedInSource);
        result.setOriginalNameString(originalNameString);
        return result;
    }

  //*********************** CONSTRUCTOR ******************************/

    //for hibernate use only
    /**
     * @deprecated for internal use only
     */
    @Deprecated
    protected NamedSource(){}

    protected NamedSource(OriginalSourceType type){
        super(type);
    }

  //*********************************** CLONE *********************************************************/

    @Override
    public NamedSource clone() throws CloneNotSupportedException{
        NamedSource result = (NamedSource)super.clone();

        //no changes
        return result;
    }
}
