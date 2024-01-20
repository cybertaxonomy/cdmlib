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
 * @author muellera
 * @since 20.01.2024
 */
public enum TaxonOccurrenceRelationType {

    IndividualsAssociation,
    Determination,
    TypeDesignation;

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
    public static EnumSet<TaxonOccurrenceRelationType> TypeDesignations(){
        return EnumSet.of(TypeDesignation);
    }

}
