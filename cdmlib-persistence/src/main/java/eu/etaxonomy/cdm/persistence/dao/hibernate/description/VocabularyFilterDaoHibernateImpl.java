/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.filter.VocabularyFilter;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.IVocabularyFilterDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;

/**
 * @author a.mueller
 * @date 02.09.2022
 */
@Repository
public class VocabularyFilterDaoHibernateImpl
        extends CdmEntityDaoBase<TermVocabulary>
        implements IVocabularyFilterDao {

    public VocabularyFilterDaoHibernateImpl() {
        super(TermVocabulary.class);
    }

    @Override
    public List<Integer> idList(VocabularyFilter filter){
        String queryStr = query(filter, "tn.id");
        Query<Integer> query = getSession().createQuery(queryStr, Integer.class);
        List<Integer> list = query.list();
        list = deduplicate(list);
        return list;
    }

    @Override
    public long count(VocabularyFilter filter){
        String queryStr = query(filter, "count(*) as n ");
        Query<Long> query = getSession().createQuery(queryStr, Long.class);
        long result = query.uniqueResult();

        return result;
    }

    @Override
    public List<UUID> listUuids(VocabularyFilter filter){
        String queryStr = query(filter, "tn.uuid");
        Query<UUID> query = getSession().createQuery(queryStr, UUID.class);
        List<UUID> list = query.list();

        list = deduplicate(list);
        return list;
    }

    private <T> List<T> deduplicate(List<T> list) {
        List<T> result = new ArrayList<>();
        for (T uuid : list){
            if (!result.contains(uuid)){
                result.add(uuid);
            }
        }
        return result;
    }

    //maybe we will later want to have ordering included
    private String query(VocabularyFilter filter, String selectPart){
        String select = " SELECT " + selectPart;
        String from = getFrom(filter);
        String vocabularyFilter = getVocabularyFilter(filter);
        String termTypeFilter = getTermTypeFilter(filter);

        String fullFilter = getFullFilter(vocabularyFilter, termTypeFilter);
//        String groupBy = " GROUP BY tn.uuid ";
        String groupBy = "";
        String orderBy = getOrderBy(filter, selectPart);
        String fullQuery = select + from + " WHERE " + fullFilter + groupBy + orderBy;

        return fullQuery;
    }

    private String getFullFilter(String vocabularyFilter, String termTypeFilter) {
        String result = " (1=1 ";
        result = CdmUtils.concat(") AND (", result, vocabularyFilter, termTypeFilter) + ") ";
        return result;
    }

    private String getFrom(@SuppressWarnings("unused") VocabularyFilter filter){
        String from = " FROM TermVocabulary voc ";
        return from;
    }

    private String getOrderBy(VocabularyFilter filter, String selectPart) {
        String orderBy = "";
        if (filter.getOrderBy()!= null && !selectPart.contains("count")){
            orderBy = "ORDER BY " + filter.getOrderBy().getHql();
        }
        return orderBy;
    }

    private String getVocabularyFilter(VocabularyFilter filter) {
        String result = "";
        List<LogicFilter<TermVocabulary>> termVocabularyFilter = filter.getTermVocabulariesFilter();
        boolean isFirst = true;
        for (LogicFilter<TermVocabulary> singleFilter : termVocabularyFilter){
            String uuid = singleFilter.getUuid().toString();
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            result = String.format("(%s%s(tn.uuid = '%s'))", result, op, uuid);
            isFirst = false;
        }
        return result;
    }

    private String getTermTypeFilter(VocabularyFilter filter) {
        String result = "";
        List<TermType> termTypeFilter = filter.getTermTypesFilter();
        boolean isFirst = true;
        for (TermType termType : termTypeFilter){
            String strTermType = termType.getKey();
            String op = isFirst ? "" : op2Hql(Op.OR);
            result = String.format("(%s%s(voc.termType = '%s'))", result, op, strTermType);
            isFirst = false;
        }
        return result;
    }

    /**
     * Returns the HQL string for this operation
     */
    private String op2Hql(Op op){
        return op == Op.NOT ? " AND NOT " : op.toString();
    }

}
