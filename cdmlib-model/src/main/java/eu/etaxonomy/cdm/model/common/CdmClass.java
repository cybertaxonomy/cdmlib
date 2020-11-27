/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.mueller
 * @since 30.04.2020
 */
public enum CdmClass implements IEnumTerm<CdmClass>{

    @XmlEnumValue("TAX")
    TAXON(Taxon.class, "TAX", true, false, UUID.fromString("e5cdf200-585a-4fb4-bf41-62d51ada85dc"), "Taxon"),
    @XmlEnumValue("OCC")
    OCCURRENCE(SpecimenOrObservationBase.class, "OCC", true, false, UUID.fromString("2a584229-15e6-4efa-ab73-0b3e8fbd6a1e"), "Occurrence"),
    @XmlEnumValue("TNA")
    TAXON_NAME(TaxonName.class, "TNA", true, false, UUID.fromString("c292d1ac-d8cf-4a76-b63b-74f29976597a"), "Taxon name"),

    @XmlEnumValue("TDA")
    TEXT_DATA(TextData.class, "TDA", false, true, UUID.fromString("3b15b116-bfbb-493d-8747-bd9741ebcac8"), "Text Data"),
    @XmlEnumValue("CTN")
    COMMON_TAXON_NAME(CommonTaxonName.class, "CTN", false, true, UUID.fromString("ebbf34dc-2e91-459f-8e60-fb94a23dd80d"), "Common name"),
    @XmlEnumValue("DIS")
    DISTRIBUTION(Distribution.class, "DIS", false, true, UUID.fromString("cc10421b-2376-4a98-83fd-048a2558ab8a"), "Distribution"),
    @XmlEnumValue("IAS")
    INDIVIDUALS_ASSOCIATION(IndividualsAssociation.class, "IAS", false, true, UUID.fromString("0686ca0a-9db6-4f54-bc43-9fd910f120d9"), "Individuals Association"),
    @XmlEnumValue("TIN")
    TAXON_INTERACTION(TaxonInteraction.class, "TIN", false, true, UUID.fromString("bcdcbeb7-d313-4ddc-be46-a656a82b29dc"), "Taxon interaction"),
    @XmlEnumValue("CDA")
    CATEGORICAL_DATA(CategoricalData.class, "CDA", false, true, UUID.fromString("d82d4113-c872-49ee-a7b6-73b940cfc929"), "Categorical data"),
    @XmlEnumValue("QDA")
    QUANTITATIVE_DATA(QuantitativeData.class, "QDA", false, true, UUID.fromString("2017d607-998e-4f24-974e-266953c55fa5"), "Quantitative data"),
    @XmlEnumValue("TED")
    TEMPORAL_DATA(TemporalData.class, "TED", false, true, UUID.fromString("3d6ee167-6136-47b3-b724-793548c2427f"), "Termporal data")
    ;

    private Class<? extends CdmBase> clazz;
    boolean isDescribed;
    boolean isDescriptionElement;


    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CdmClass.class);

    @SuppressWarnings("unchecked")
    private CdmClass(Class<? extends CdmBase> clazz, String key, boolean isDescribed,
            boolean isDescriptionElement, UUID uuid, String defaultString){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
        this.clazz = clazz;
        this.isDescribed = isDescribed;
        this.isDescriptionElement = isDescriptionElement;
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<CdmClass> delegateVoc;
    private IEnumTerm<CdmClass> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(CdmClass.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getLabel(){return delegateVocTerm.getLabel();}

    @Override
    public String getLabel(Language language){return delegateVocTerm.getLabel(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public CdmClass getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<CdmClass> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(CdmClass ancestor) {return delegateVocTerm.isKindOf(ancestor);   }

    @Override
    public Set<CdmClass> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static CdmClass getByKey(String key){return delegateVoc.getByKey(key);}
    public static CdmClass getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

//************************ GETTER **************************/

    public static final EnumSet<CdmClass> DESCRIBED(){
        EnumSet<CdmClass> result = EnumSet.allOf(CdmClass.class);
        result.removeIf(e->!e.isDescribed);
        return result;
    }

    public static final EnumSet<CdmClass> DESCRIPTION_ELEMENT_SUB(){
        EnumSet<CdmClass> result = EnumSet.allOf(CdmClass.class);
        result.removeIf(e->!e.isDescriptionElement);
        return result;
    }

    public Class<? extends CdmBase> getClazz() {
        return clazz;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Class<? extends IDescribable> getDescribedClazz() {
        if (!isDescribed){
            throw new java.lang.UnsupportedOperationException("getDescribedClazz() not available enum item " + this);
        }else{
            return (Class<? extends IDescribable>)clazz;
        }
    }

    @SuppressWarnings({ "unchecked" })
    public Class<? extends DescriptionElementBase> getDescriptionElementClazz() {
        if (!isDescriptionElement){
            throw new java.lang.UnsupportedOperationException("getDescriptionElementClazz() not available enum item " + this);
        }else{
            return (Class<? extends DescriptionElementBase>)clazz;
        }
    }

    public boolean supports(CdmBase cdmBase){
        if (cdmBase == null){
            return false;
        }else {
            return this.clazz.isAssignableFrom(cdmBase.getClass());
        }
    }

    public boolean supports(Class<? extends CdmBase> clazz){
        if (clazz == null){
            return false;
        }else {
            return this.clazz.isAssignableFrom(clazz);
        }
    }
}
