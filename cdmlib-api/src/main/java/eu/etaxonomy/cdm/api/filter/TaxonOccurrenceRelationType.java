/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.util.EnumSet;

/**
 * Enumeration to define a filter for taxon-occurrence associations.
 *
 * See also https://dev.e-taxonomy.eu/redmine/issues/10400
 *
 * @author muellera
 * @since 20.01.2024
 */
public enum TaxonOccurrenceRelationType {

    IndividualsAssociation("IA"),
    Determination("DET"),
    CurrentDetermination("CDET"),  //only relevant if DET is not used as CurrentDetermination is a subset of Determination
    TypeDesignation("TD");

    private String key;

    //TODO more fine grained rel types could be:
//  TypeDesignationAcceptedOnly
  //TypeDesignationsNoNamesMissingInSynonymy //currently all names in a homotypical group are taken, even the names do not appear in the synonymy, however in most projects this is not of relevance
  //DeterminationAcceptedOnly

    TaxonOccurrenceRelationType(String key) {
        this.key = key;
    }

    public static EnumSet<TaxonOccurrenceRelationType> All(){
        return EnumSet.allOf(TaxonOccurrenceRelationType.class);
    }

    //** Enum Sets

    public static EnumSet<TaxonOccurrenceRelationType> IndividualsAssociations(){
        return EnumSet.of(IndividualsAssociation);
    }
    public static EnumSet<TaxonOccurrenceRelationType> Determinations(){
        return EnumSet.of(Determination);
    }
    public static EnumSet<TaxonOccurrenceRelationType> CurrentDeterminations(){
        return EnumSet.of(CurrentDetermination);
    }
    public static EnumSet<TaxonOccurrenceRelationType> TypeDesignations(){
        return EnumSet.of(TypeDesignation);
    }

    public static TaxonOccurrenceRelationType of(String key) {
        for (TaxonOccurrenceRelationType t : values()) {
            if (t.key.equalsIgnoreCase(key)) {
                return t;
            }
        }
        return null;
    }
}