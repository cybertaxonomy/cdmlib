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
public enum TaxonType
    implements IEnumTerm<TaxonType>{
    CONCEPT(UUID.fromString("d6e65c50-729a-4c80-8ec0-d30627c8b2f0"), "Concept","CO"),
    NAME_USAGE(UUID.fromString("a3ffccb8-8408-4fb4-8e38-9ffe84ca804f"), "Name Usage","NU"),
    ;

    @SuppressWarnings("unchecked")
    private TaxonType(UUID uuid, String defaultString, String key){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<TaxonType> delegateVoc;
    private IEnumTerm<TaxonType> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(TaxonType.class);
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
    public TaxonType getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<TaxonType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(TaxonType ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<TaxonType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static TaxonType getByKey(String key){return delegateVoc.getByKey(key);}
    public static TaxonType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

//*************************** END DELEGATE *********************************************/

    protected static boolean includesType(EnumSet<TaxonType> set, TaxonType type) {
        EnumSet<TaxonType> all;
        if (type.getGeneralizationOf(true).isEmpty()){
            all = EnumSet.noneOf(TaxonType.class);
        }else{
            all = EnumSet.copyOf(type.getGeneralizationOf(true));
        }
        all.add(type);
        for (TaxonType st : all){
            if (set.contains(st)){
                return true;
            }
        }
        return false;
    }
}
