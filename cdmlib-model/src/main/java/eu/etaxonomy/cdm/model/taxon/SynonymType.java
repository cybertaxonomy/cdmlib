/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IRelationshipType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.mueller
 * @date 11.12.2022
 */
public enum SynonymType implements IEnumTerm<SynonymType>, IRelationshipType {

    /**
     * Indicates that the reference asserting the synonym relationship
     * does not know whether both {@link name.TaxonName taxon names}
     * involved are typified by the same type or not.
     */
    SYNONYM_OF("1afa5429-095a-48da-8877-836fa4fe709e", "SYN", "synonym of", "has synonym"),

    /**
     * Synonym relationship type "is homotypic synonym of"
     * (Zoology: "is objective synonym of" or "is nomenclatural synonym of").
     * Indicates that the reference asserting the synonym relationship holds that
     * the {@link name.TaxonName taxon name} used as a {@link Synonym synonym}
     * and the taxon name used as the ("accepted/correct") {@link Taxon taxon}
     * are typified by the same type.
     * In this case they should belong to the same {@link name.HomotypicalGroup homotypical group}.
     */
    HOMOTYPIC_SYNONYM_OF("294313a9-5617-4ed5-ae2d-c57599907cb2", "HOM", "homotypic synonym of", "has homotypic synonym"),

    /**
     * Synonym relationship type "is heterotypic synonym of"
     * (Zoology: "is subjective synonym of" or "is taxonomic synonym of").
     * Indicates that the reference asserting the synonym relationship holds that
     * the {@link name.TaxonName taxon name} used as a {@link Synonym synonym} and the taxon name used as the
     * ("accepted/correct") {@link Taxon taxon} are not typified by the same type.
     * In this case they should not belong to the same {@link name.HomotypicalGroup homotypical group}.
     */
    HETEROTYPIC_SYNONYM_OF("4c1e2c59-ca55-41ac-9a82-676894976084", "HET", "heterotypic synonym of", "has heterotypic synonym"),

    /**
     * Synonym relationship type "is inferred synonym of".
     * This synonym relationship type is used in zoology whenever a synonym relationship
     * on species or infraspecific level is derived from a genus synonymy.
     */
    INFERRED_SYNONYM_OF("cb5bad12-9dbc-4b38-9977-162e45089c11", "INS", "inferred synonym of", "has inferred synonym"),

    /**
     * Synonym relationship type "is inferred genus of".
     * This synonym relationship type is used in zoology whenever a synonym relationship
     * on species or infraspecific
     * level is derived from an epithet synonymy.
     */
    INFERRED_GENUS_OF("f55a574b-c1de-45cc-9ade-1aa2e098c3b5", "ING", "inferred genus of", "has inferred genus"),

    /**
     * TODO this javadoc seems to be incorrect due to copy&paste
     * Synonym relationship type "is inferred synonym of".
     * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
     * level is derived from a genus synonymy.
     */
    INFERRED_EPITHET_OF("089c1926-eb36-47e7-a2d1-fd5f3918713d", "INE", "inferred epithet of", "has inferred epithet"),

    POTENTIAL_COMBINATION_OF("7c45871f-6dc5-40e7-9f26-228318d0f63a", "POT", "potential combination of", "has potential combination"),
    ;

    private final String inverseLabel;

    private SynonymType(String uuid, String key, String label, String inverseLabel){
        this.inverseLabel = inverseLabel;
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, UUID.fromString(uuid), label, key, null);
    }

    public String getInverseRepresentation(@SuppressWarnings("unused") Language language) {
        //for now we do not support i18n here
        //may be implemented when needed
        return inverseLabel;
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<SynonymType> delegateVoc;
    private IEnumTerm<SynonymType> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(SynonymType.class);
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
    public SynonymType getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<SynonymType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(SynonymType ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<SynonymType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


    public static SynonymType getByKey(String key){return delegateVoc.getByKey(key);}
    public static SynonymType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

// **************************************

    public boolean isInferredSynonym() {
        return this == INFERRED_EPITHET_OF || this == INFERRED_GENUS_OF || this == POTENTIAL_COMBINATION_OF;
    }

}