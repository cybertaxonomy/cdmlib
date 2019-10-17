/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.mueller
 * @since 14.08.2019
 */
public enum DescriptionType implements IEnumTerm<DescriptionType>{

    /**
     * The description has been computed by a machine, e.g. by aggregation or any
     * other algorithm. Usually such descriptions should not be edited by users manually.
     * {@link DescriptionType#COMPUTED} is a base type for more specific types such as
     * {@link DescriptionType#AGGREGATED}
     */
    @XmlEnumValue("COM")
    COMPUTED(UUID.fromString("7048c64e-9e61-41ed-b561-5765bc8e4ba2"), "Computed", "COM", null),

    /**
     * The description has been computed by a machine by aggregation of data.
     * Usually such descriptions should not be edited by users manually.
     */
    @XmlEnumValue("AGG")
    AGGREGATED(UUID.fromString("d1c02cbf-e27c-49ee-919a-7393d953ef36"), "Aggregated", "AGG", COMPUTED),

    /**
     * Description is a clone which was used to fix a certain state of data to define
     * it as a source for e.g. computed data.
     * E.g. when computing a taxon description the underlying specimen descriptions might
     * be cloned and locked for editing to keep them as exact source for the computed
     * taxon description.
     */
    @XmlEnumValue("CLO")
    CLONE_FOR_SOURCE(UUID.fromString("2d58416f-506b-40c5-bdb6-60b6735c92d3"), "Clone", "CLO", null),

    /**
     * Kind of a marker to define that data comes from a secondary source. E.g. if
     * taxon descriptions are computed mostly on primary data (e.g. specimen descriptions)
     * some data might come from literature though to complete the description.
     * This literature data then should be marked as "Secondary data"
     */
    @XmlEnumValue("SEC")
    SECONDARY_DATA(UUID.fromString("382e6b00-9725-4877-bd50-18ee263fe90e"), "Secondary Data", "SEC", null),

    /**
     * If Descriptions are aggregated for e.g. taxon descriptions, often explicit data for some parameters
     * are missing as all underlying data have the same value on this parameter.
     * A description can be defined to hold default values for these parameters.
     */
    @XmlEnumValue("DVA")
    DEFAULT_VALUES_FOR_AGGREGATION(UUID.fromString("e4a51ab3-7040-4f60-9d08-51782c2255a1"), "Default Values for Aggregation", "DVA", null),

    /**
     * Designated descriptions for {@link IndividualsAssociation}s of specimens to taxa.
     */
    @XmlEnumValue("IAS")
    INDIVIDUALS_ASSOCIATION(UUID.fromString("b8a1346d-9521-4ea2-ada8-c6774cf9175a"), "Specimens", "IAS", null),

    ;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DescriptionType.class);


    private DescriptionType(UUID uuid, String defaultString, String key, DescriptionType parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }

// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<DescriptionType> delegateVoc;
    private IEnumTerm<DescriptionType> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(DescriptionType.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

    @Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public DescriptionType getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<DescriptionType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(DescriptionType ancestor) {return delegateVocTerm.isKindOf(ancestor);   }

    @Override
    public Set<DescriptionType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static DescriptionType getByKey(String key){return delegateVoc.getByKey(key);}
    public static DescriptionType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


    /**
     * Returns true if the given set contains the type {@link #COMPUTED} computed or one of
     * it's sub-types.<BR><BR>
     * Note: Computed is a base type. It has children like {@link #AGGREGATED}.
     * Also for them this method returns <code>true</code>.
     */
    public static boolean isComputed(EnumSet<DescriptionType> set) {
        return includesType(set, COMPUTED);
    }

    public static boolean isAggregated(EnumSet<DescriptionType> set) {
        return includesType(set, AGGREGATED);
    }

    public static boolean isCloneForSource(EnumSet<DescriptionType> set) {
        return includesType(set, DescriptionType.CLONE_FOR_SOURCE);
    }

    public static boolean isSecondaryData(EnumSet<DescriptionType> set) {
        return includesType(set, DescriptionType.SECONDARY_DATA);
    }

    public static boolean isDefaultForAggregation(EnumSet<DescriptionType> set) {
        return includesType(set, DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION);
    }

    public static boolean isSpecimenDescription(EnumSet<DescriptionType> set) {
        return includesType(set, DescriptionType.INDIVIDUALS_ASSOCIATION);
    }

    /**
     * @param set
     * @return
     */
    protected static boolean includesType(EnumSet<DescriptionType> set, DescriptionType state) {
        EnumSet<DescriptionType> all;
        if (state.getGeneralizationOf(true).isEmpty()){
            all = EnumSet.noneOf(DescriptionType.class);
        }else{
            all = EnumSet.copyOf(state.getGeneralizationOf(true));
        }
        all.add(state);
        for (DescriptionType st : all){
            if (set.contains(st)){
                return true;
            }
        }
        return false;
    }



}
