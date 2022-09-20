/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * Filter used e.g. for additivity ontology import. Should work similar to TaxonNodeFilter.
 *
 * @author a.mueller
 * @date 02.09.2022
 */
public class VocabularyFilter implements Serializable {

    private static final long serialVersionUID = -8054720226785479796L;

    private List<TermType> termTypes = new ArrayList<>();
    private List<LogicFilter<TermVocabulary>> termVocabularies = new ArrayList<>();

    private ORDER orderBy = null;

    public enum ORDER{
        ID("voc.id"),
//        TREEINDEX("tn.treeIndex"),
//        TREEINDEX_DESC("tn.treeIndex DESC")
        ;
        String hql;
        private ORDER(String hql){
            this.hql = hql;
        }
        public String getHql(){
            return hql;
        }
    }

// *************************** FACTORY ********************************/

    public static VocabularyFilter NewInstance(){
        return new VocabularyFilter();
    }

    public static VocabularyFilter NewTermTypeInstance(TermType termType){
        return new VocabularyFilter().orTermType(termType);
    }

// ******************** CONSTRUCTOR ************************/

    private VocabularyFilter() {
        reset();
    }

// ******************** reset *****************************/

    public void reset(){
        resetTermTypes();
        resetTermVocabularies();
    }

    private void resetTermTypes() {
        termTypes = new ArrayList<>();
    }

    private void resetTermVocabularies() {
        termVocabularies = new ArrayList<>();
    }

// ******************** OR, XXX ****************************************/

    public VocabularyFilter orTermType(TermType termType){
//        termTypes.add( new LogicFilter<>(termType, Op.OR));
        termTypes.add(termType);
        return this;
    }

    public VocabularyFilter orVocabulary(UUID uuid){
        termVocabularies.add( new LogicFilter<>(TermVocabulary.class, uuid, Op.OR));
        return this;
    }


    public List<LogicFilter<TermVocabulary>>getTermVocabulariesFilter(){
        return Collections.unmodifiableList(termVocabularies);
    }

    public List<TermType>getTermTypesFilter(){
        return Collections.unmodifiableList(termTypes);
    }

    public ORDER getOrderBy() {
        return orderBy;
    }
    public void setOrder(ORDER orderBy) {
        this.orderBy = orderBy;
    }
}
