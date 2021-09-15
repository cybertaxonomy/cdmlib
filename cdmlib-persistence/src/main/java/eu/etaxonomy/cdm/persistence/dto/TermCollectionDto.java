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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private List<TermDto> terms;

    private boolean isAllowDuplicate;
    private boolean containsDuplicates = false;
    private boolean isOrderRelevant;
    private boolean isFlat;
    private boolean isAllowModifications = true;

    public TermCollectionDto(UUID uuid, Set<Representation> representations, TermType termType, String titleCache, boolean isAllowDuplicate, boolean isOrderRelevant, boolean isFlat) {
        super(uuid, representations, titleCache);
        terms = new ArrayList<>();
        setTermType(termType);
        this.isAllowDuplicate = isAllowDuplicate;
        this.isOrderRelevant = isOrderRelevant;
        this.isFlat = isFlat;
    }

    public List<TermDto> getTerms() {
        if (terms.isEmpty()){

        }
        return terms;
    }

    public void addTerm(TermDto term){
        if (term == null){
            return;
        }
        if (terms == null){
            terms = new ArrayList<>();
        }
        if (terms.contains(term)){
            containsDuplicates = true;
        }
        terms.add(term);
    }

    public void removeTerm(TermDto term){
        terms.remove(term);
    }



    /**
     * @return the isAllowDuplicate
     */
    public boolean isAllowDuplicate() {
        return isAllowDuplicate;
    }

    /**
     * @param isAllowDuplicate the isAllowDuplicate to set
     */
    public void setAllowDuplicate(boolean isAllowDuplicate) {
        this.isAllowDuplicate = isAllowDuplicate;
    }

    public boolean isContainsDuplicates() {
        Set<TermDto> dtoSet = new HashSet<>(terms);
        if (dtoSet.size() == terms.size()){
            containsDuplicates = false;
        }

        return containsDuplicates;
    }

    public void setContainsDuplicates(boolean containsDuplicates) {
        this.containsDuplicates = containsDuplicates;
    }

    /**
     * @return the isOrderRelevant
     */
    public boolean isOrderRelevant() {
        return isOrderRelevant;
    }

    /**
     * @param isOrderRelevant the isOrderRelevant to set
     */
    public void setOrderRelevant(boolean isOrderRelevant) {
        this.isOrderRelevant = isOrderRelevant;
    }

    /**
     * @return the isFlat
     */
    public boolean isFlat() {
        return isFlat;
    }

    /**
     * @param isFlat the isFlat to set
     */
    public void setFlat(boolean isFlat) {
        this.isFlat = isFlat;
    }

    public static String getTermCollectionDtoSelect(){
        return getTermCollectionDtoSelect("TermVocabulary");
    }

    public static String getTermCollectionDtoSelect(String fromTable){
        return ""
                + "select a.uuid, "
                + "r, "
                + "a.termType,"
                + "a.titleCache,"
                + "a.allowDuplicates,"
                + "a.orderRelevant,"
                + "a.isFlat "


                + "FROM "+fromTable+" as a "
                + "LEFT JOIN a.representations AS r ";

    }




}
