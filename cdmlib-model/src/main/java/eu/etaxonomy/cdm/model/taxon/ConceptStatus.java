/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.jpa.EnumSetConverterBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;
import jakarta.persistence.Converter;
import jakarta.xml.bind.annotation.XmlEnum;

/**
 * @author a.mueller
 * @since 10.05.2021
 */
@XmlEnum
public enum ConceptStatus
        implements IEnumTerm<ConceptStatus>{

    PERSISTENT(UUID.fromString("08ae05a9-2d61-4c6d-b005-5d47d0139eff"),"persistent","PER"),
    SUPPORTS_PROVENANCE(UUID.fromString("1f0417cb-72ae-4a0e-a2fa-cce0de2c462a"),"supports provenance","PRO"),
    CURRENT(UUID.fromString("fba936d3-e871-4993-b29c-20a846a112d7"),"current","CUR")

    ;
    @SuppressWarnings("unchecked")
    private ConceptStatus(UUID uuid, String defaultString, String key){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
    }

//********************** CONVERTER ****************************/

    @Converter
    public static class ConceptStatusEnumSetConverter extends EnumSetConverterBase<ConceptStatus> {
        public ConceptStatusEnumSetConverter() { super(ConceptStatus.class); }
    }

// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<ConceptStatus> delegateVoc;
    private IEnumTerm<ConceptStatus> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(ConceptStatus.class);
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
    public ConceptStatus getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<ConceptStatus> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(ConceptStatus ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<ConceptStatus> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


    public static ConceptStatus getByKey(String key){return delegateVoc.getByKey(key);}
    public static ConceptStatus getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

//*************************** END DELEGATE *********************************************/

    protected static boolean includesType(EnumSet<ConceptStatus> set, ConceptStatus status) {
        EnumSet<ConceptStatus> all;
        if (status.getGeneralizationOf(true).isEmpty()){
            all = EnumSet.noneOf(ConceptStatus.class);
        }else{
            all = EnumSet.copyOf(status.getGeneralizationOf(true));
        }
        all.add(status);
        for (ConceptStatus st : all){
            if (set.contains(st)){
                return true;
            }
        }
        return false;
    }
}
