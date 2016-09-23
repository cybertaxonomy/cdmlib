// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

public enum TaxonStatus{
    Accepted,
    SynonymObjective,
    Synonym; //All others including undefined and explicitly subjective /heterotypic synonyms

    public boolean isSynonym(){
       return this == SynonymObjective || this == Synonym;
    }
}