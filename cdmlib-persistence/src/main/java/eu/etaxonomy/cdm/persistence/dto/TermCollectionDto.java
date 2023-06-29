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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @date 05.11.2018
 */
public abstract class TermCollectionDto extends AbstractTermDto {

    private static final long serialVersionUID = 6053392236860675874L;

    private List<TermDto> terms;

    private boolean isAllowDuplicate;
    private boolean containsDuplicates = false;
    private boolean isOrderRelevant;
    private boolean isFlat;

    //subclasses should override this method
    public static TermCollectionDto fromCdmBase(TermCollection<?,?> termCollection) {
        if (termCollection.isInstanceOf(TermVocabulary.class)) {
            return TermVocabularyDto.fromVocabulary(CdmBase.deproxy(termCollection, TermVocabulary.class));
        }else if (termCollection.isInstanceOf(TermTree.class)) {
            return TermTreeDto.fromTree(CdmBase.deproxy(termCollection, TermTree.class));
        }else {
            throw new RuntimeException("TermCollection type not supported: " + termCollection.getClass());
        }
    }

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

    public void setTerms(List<TermDto> terms){
        this.terms.clear();
        this.terms.addAll(terms);
    }

    public void removeTerm(TermDto term){
        terms.remove(term);
    }

    public boolean isAllowDuplicate() {
        return isAllowDuplicate;
    }
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

    public boolean isOrderRelevant() {
        return isOrderRelevant;
    }
    public void setOrderRelevant(boolean isOrderRelevant) {
        this.isOrderRelevant = isOrderRelevant;
    }

    public boolean isFlat() {
        return isFlat;
    }
    public void setFlat(boolean isFlat) {
        this.isFlat = isFlat;
    }

    public static String getTermCollectionDtoSelect(){
        return getTermCollectionDtoSelect("TermCollection");
    }

    public static String getTermCollectionDtoSelect(String fromTable){
//        return ""
//                + "select "
//                + "a.uuid, "
//                + "r, "
//                + "a.termType,"
//                + "a.titleCache,"
//                + "a.allowDuplicates,"
//                + "a.orderRelevant,"
//                + "a.isFlat, "
//                + "a.externallyManaged, "
//                + "root "
//
//
//                + "FROM "+fromTable+" as a "
//                + "LEFT JOIN a.representations AS r "
//                + "LEFT JOIN a.root as root";

        String sqlSelectString = ""
                + "select "
                + "a.uuid, "
                + "r, "
                + "a.termType,  "
                + "a.titleCache, "
                + "a.allowDuplicates, "
                + "a.orderRelevant, "
                + "a.isFlat, "
                + "a.externallyManaged, "
                + "a.uri ";
        if (!fromTable.equals("TermVocabulary")) {
            sqlSelectString+=  ", root ";
        }

        String sqlFromString =   "from " +fromTable+" as a ";
        String sqlJoinString = "";
        if (!fromTable.equals("TermVocabulary")) {
            sqlJoinString +="LEFT JOIN a.root as root ";
        }
        sqlJoinString +=" LEFT JOIN a.representations AS r ";

        String result = sqlSelectString + sqlFromString + sqlJoinString;

        return result;
    }





    /**
     * @param result
     * @return
     */
    public static List<TermCollectionDto> termCollectionDtoListFrom(List<Object[]> result) {
        List<TermCollectionDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations because of LEFT JOIN
        Map<UUID, TermCollectionDto> dtoMap = new HashMap<>(result.size());
        for (Object[] elements : result) {
            extracted(dtos, dtoMap, elements);
        }
        return dtos;
    }

    /**
     * @param dtos
     * @param dtoMap
     * @param elements
     * @param uuid
     */
    protected static void extracted(List<TermCollectionDto> dtos, Map<UUID, TermCollectionDto> dtoMap, Object[] elements) {
        UUID uuid = (UUID)elements[0];
        if(dtoMap.containsKey(uuid)){
            // multiple results for one voc -> multiple (voc) representation
            if(elements[1]!=null){
                dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
            }

        } else {
            if (elements[9]== null) {
                TermVocabularyDto.extractedVocabularies(dtos, dtoMap, elements);
            }else {
                TermTreeDto.extracted(dtos, dtoMap, elements);
            }

        }
    }
}