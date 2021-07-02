/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * see also https://dev.e-taxonomy.eu/redmine/issues/9692
 *
 * @author a.mueller
 * @since 02.07.2021
 */
public enum TaxonomicOperationType implements IEnumTerm<TaxonomicOperationType> {
    SPLIT(UUID.fromString("e2b37637-9fe6-4e91-afb9-4c2eb49b3332"), "Split", "SPLIT"),
    MERGE(UUID.fromString("ef89d825-0eb4-4826-88d5-ca54a9adbae7"), "Merge", "MERGE"),
    ;
    @SuppressWarnings("unchecked")
    private TaxonomicOperationType(UUID uuid, String defaultString, String key){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<TaxonomicOperationType> delegateVoc;
    private IEnumTerm<TaxonomicOperationType> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(TaxonomicOperationType.class);
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
    public TaxonomicOperationType getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<TaxonomicOperationType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(TaxonomicOperationType ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<TaxonomicOperationType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


    public static TaxonomicOperationType getByKey(String key){return delegateVoc.getByKey(key);}
    public static TaxonomicOperationType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

//*************************** END DELEGATE *********************************************/


}
