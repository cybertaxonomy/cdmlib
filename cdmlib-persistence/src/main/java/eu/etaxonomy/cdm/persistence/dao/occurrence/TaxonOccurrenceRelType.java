/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.occurrence;

import java.util.EnumSet;

/**
 * @author muellera
 * @since 20.01.2024
 */
public enum TaxonOccurrenceRelType {

    IndividualsAssociation,
    Determination,
    TypeDesignation;

    public static EnumSet<TaxonOccurrenceRelType> All(){
        return EnumSet.allOf(TaxonOccurrenceRelType.class);
    }

    //** Enum Sets

    public static EnumSet<TaxonOccurrenceRelType> IndividualsAssociations(){
        return EnumSet.of(IndividualsAssociation);
    }
    public static EnumSet<TaxonOccurrenceRelType> Determinations(){
        return EnumSet.of(Determination);
    }
    public static EnumSet<TaxonOccurrenceRelType> TypeDesignations(){
        return EnumSet.of(TypeDesignation);
    }

}
