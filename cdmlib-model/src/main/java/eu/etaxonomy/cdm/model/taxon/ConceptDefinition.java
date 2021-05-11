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

import javax.xml.bind.annotation.XmlEnum;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.mueller
 * @since 10.05.2021
 */
@XmlEnum
public enum ConceptDefinition
        implements IEnumTerm<ConceptDefinition>{

    HOMOTYPIC_GROUP(UUID.fromString("951d781d-f30d-4749-a79a-858a4e6a6eee"),"homotypic groups","HG"),
    HOMOTYPIC_GROUP_WITH_EDITOR(UUID.fromString("ee316a72-fd0c-48ac-9104-0fccff091b01"),"homotypic groups with editor decisions","HGWE"),
    DESCRIPTION(UUID.fromString("eaec1b7e-a9cd-432f-a684-9504f0e85337"),"descriptions","DES"),
    DESCRIPTION_WITH_EDITOR(UUID.fromString("5ab7fd6e-b7c3-42b7-9a75-d3cccb547a96"),"descriptions with editor decisions","DESWE"),

    ;
    @SuppressWarnings("unchecked")
    private ConceptDefinition(UUID uuid, String defaultString, String key){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<ConceptDefinition> delegateVoc;
    private IEnumTerm<ConceptDefinition> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(ConceptDefinition.class);
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
    public ConceptDefinition getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<ConceptDefinition> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(ConceptDefinition ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<ConceptDefinition> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


    public static ConceptDefinition getByKey(String key){return delegateVoc.getByKey(key);}
    public static ConceptDefinition getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

//*************************** END DELEGATE *********************************************/

    protected static boolean includesType(EnumSet<ConceptDefinition> set, ConceptDefinition definition) {
        EnumSet<ConceptDefinition> all;
        if (definition.getGeneralizationOf(true).isEmpty()){
            all = EnumSet.noneOf(ConceptDefinition.class);
        }else{
            all = EnumSet.copyOf(definition.getGeneralizationOf(true));
        }
        all.add(definition);
        for (ConceptDefinition def : all){
            if (set.contains(def)){
                return true;
            }
        }
        return false;
    }
}
