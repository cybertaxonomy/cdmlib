/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * Editorial / taxonomic acceptance status of a {@link TaxonBase}.
 * Replaces the former boolean {@code doubtful} flag.
 *
 * Note: not to be confused with {@code eu.etaxonomy.cdm.persistence.dto.TaxonStatus}.
 *
 * See #11039
 *
 * @author muellera
 * @since 07.10.2026
 */
@XmlEnum
public enum TaxonStatus
        implements IEnumTerm<TaxonStatus>{

    /**
     * Taxon or synonym assignment without doubt (default).
     */
    @XmlEnumValue("Accepted")
    ACCEPTED(UUID.fromString("ee74d076-b89c-4491-9d6e-04e5359b2b79"), "Accepted", "ACC", true, false),

    /**
     * The assignment to accepted taxon or synonym is doubtful.
     */
    @XmlEnumValue("Doubtful")
    DOUBTFUL(UUID.fromString("e073d533-dd5a-4aad-95e7-335a0d94391f"), "Doubtful", "DOU", true, true),

    /**
     * Provisionally accepted taxon.
     */
    @XmlEnumValue("Provisionally Accepted")
    PROVISIONALLY_ACCEPTED(UUID.fromString("ad6418fc-267d-419e-bf9f-336e077911b7"), "Provisionally Accepted", "PAC", true, false),

    /**
     * Synonym.
     */
    @XmlEnumValue("Synonym")
    SYNONYM(UUID.fromString("ddfb069c-5d98-40bd-b229-9a2fa871e700"), "Synonym", "SYN", false, true),

    ;

// **************** END ENUM **********************/

    private boolean supportsAccepted;
    private boolean supportsSynonym;

    private TaxonStatus(UUID uuid, String defaultString, String key, boolean supportsAccepted, boolean supportsSynonym){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
        this.supportsAccepted = supportsAccepted;
        this.supportsSynonym = supportsSynonym;
    }

    public static final EnumSet<TaxonStatus> forAccepted(){
        Set<TaxonStatus> result = new HashSet<>();
        for (TaxonStatus st : values()) {
            if (st.supportsAccepted) {
                result.add(st);
            }
        }
        return EnumSet.copyOf(result);
    }

    public static final EnumSet<TaxonStatus> forSynonyms(){
        Set<TaxonStatus> result = new HashSet<>();
        for (TaxonStatus st : values()) {
            if (st.supportsSynonym) {
                result.add(st);
            }
        }
        return EnumSet.copyOf(result);

    }

// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<TaxonStatus> delegateVoc;
    private IEnumTerm<TaxonStatus> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(TaxonStatus.class);
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
    public TaxonStatus getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<TaxonStatus> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(TaxonStatus ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<TaxonStatus> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static TaxonStatus getByKey(String key){return delegateVoc.getByKey(key);}
    public static TaxonStatus getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}
}
