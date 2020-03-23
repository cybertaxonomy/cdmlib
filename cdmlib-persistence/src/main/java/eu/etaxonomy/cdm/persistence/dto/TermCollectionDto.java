// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author pplitzner
 * @date 05.11.2018
 *
 */
public class TermCollectionDto extends AbstractTermDto {

    private static final long serialVersionUID = 6053392236860675874L;

    private Set<TermDto> terms;

    public TermCollectionDto(UUID uuid, Set<Representation> representations, TermType termType) {
        super(uuid, representations);
        terms = new HashSet<>();
        setTermType(termType);
    }

    public Set<TermDto> getTerms() {
        if (terms.isEmpty()){

        }
        return terms;
    }

    public void addTerm(TermDto term){
        terms.add(term);
    }

    public static String getTermDtoSelect(){
        return getTermDtoSelect("TermVocabulary");
    }

    public static String getTermDtoSelect(String fromTable){
        return ""
                + "select a.uuid, "
                + "r, "
                + "a.termType "

                + "FROM "+fromTable+" as a "
                + "LEFT JOIN a.representations AS r ";
    }




}
