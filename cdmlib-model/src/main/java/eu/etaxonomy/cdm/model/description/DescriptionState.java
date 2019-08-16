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
 *
 */
public enum DescriptionState implements IEnumTerm<DescriptionState>{

    @XmlEnumValue("COM")
    COMPUTED(UUID.fromString("7048c64e-9e61-41ed-b561-5765bc8e4ba2"), "Computed", "COM", null),

    @XmlEnumValue("AGG")
    AGGREGATED(UUID.fromString("d1c02cbf-e27c-49ee-919a-7393d953ef36"), "Aggregated", "AGG", COMPUTED),

    @XmlEnumValue("CLO")
    CLONE_FOR_SOURCE(UUID.fromString("2d58416f-506b-40c5-bdb6-60b6735c92d3"), "Clone", "CLO", null),

    @XmlEnumValue("SEC")
    SECONDARY_DATA(UUID.fromString("382e6b00-9725-4877-bd50-18ee263fe90e"), "Secondary Data", "SEC", null),

    /**
     * If Descriptions are aggregated, these values can be taken as default if explicit values do not exist.
     */
    @XmlEnumValue("DVA")
    DEFAULT_VALUES_FOR_AGGREGATION(UUID.fromString("e4a51ab3-7040-4f60-9d08-51782c2255a1"), "Default Values for Aggregation", "DVA", null),

    ;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DescriptionState.class);


    private DescriptionState(UUID uuid, String defaultString, String key, DescriptionState parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }

// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<DescriptionState> delegateVoc;
    private IEnumTerm<DescriptionState> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(DescriptionState.class);
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
    public DescriptionState getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<DescriptionState> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(DescriptionState ancestor) {return delegateVocTerm.isKindOf(ancestor);   }

    @Override
    public Set<DescriptionState> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static DescriptionState getByKey(String key){return delegateVoc.getByKey(key);}
    public static DescriptionState getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


    /**
     * Returns true if the given set contains the type {@link #COMPUTED} computed or one of
     * it's sub-types.<BR><BR>
     * Note: Computed is a base type. It has children like {@link #AGGREGATED}.
     * Also for them this method returns <code>true</code>.
     */
    public static boolean isComputed(EnumSet<DescriptionState> set) {
        return includesType(set, COMPUTED);
    }

    public static boolean isAggregated(EnumSet<DescriptionState> set) {
        return includesType(set, AGGREGATED);
    }

    public static boolean isCloneForSource(EnumSet<DescriptionState> set) {
        return includesType(set, DescriptionState.CLONE_FOR_SOURCE);
    }

    public static boolean isSecondaryData(EnumSet<DescriptionState> set) {
        return includesType(set, DescriptionState.SECONDARY_DATA);
    }

    public static boolean isDefaultForAggregation(EnumSet<DescriptionState> set) {
        return includesType(set, DescriptionState.DEFAULT_VALUES_FOR_AGGREGATION);
    }

    /**
     * @param set
     * @return
     */
    protected static boolean includesType(EnumSet<DescriptionState> set, DescriptionState state) {
        EnumSet<DescriptionState> all = EnumSet.copyOf(state.getGeneralizationOf(true));
        all.add(state);
        for (DescriptionState st : all){
            if (set.contains(st)){
                return true;
            }
        }
        return false;
    }



}
