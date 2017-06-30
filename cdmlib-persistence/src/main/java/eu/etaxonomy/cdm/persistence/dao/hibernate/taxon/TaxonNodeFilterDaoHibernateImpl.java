/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeFilterDao;

/**
 * DAO to retrived taxon node uuids according to a {@link TaxonNodeFilter}.
 *
 *
 * @author a.mueller
 */
@Repository
public class TaxonNodeFilterDaoHibernateImpl extends CdmEntityDaoBase<TaxonNode> implements ITaxonNodeFilterDao {

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    public TaxonNodeFilterDaoHibernateImpl() {
        super(TaxonNode.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long count(TaxonNodeFilter filter){
        String queryStr = query(filter, "count(*) as n ");
        Query query = getSession().createQuery(queryStr);
        long result = (Long)query.uniqueResult();

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UUID> listUuids(TaxonNodeFilter filter){
        String queryStr = query(filter, "tn.uuid");
        Query query = getSession().createQuery(queryStr);
        List<UUID> list = castToUuidList(query.list());

        list = deduplicate(list);
        return list;
    }

    //maybe we will later want to have ordering included
    private String query(TaxonNodeFilter filter, String selectPart){
        String select = " SELECT " + selectPart;
        String from = " FROM TaxonNode tn ";
        String subtreeFilter = getSubtreeFilter(filter);
        String taxonNodeFilter = getTaxonNodeFilter(filter);
        String classificationFilter = getClassificationFilter(filter);
        String taxonFilter = getTaxonFilter(filter);
        String rootNoteFilter = getRootNodeFilter(filter);
        String fullFilter = getFullFilter(subtreeFilter, taxonNodeFilter,
                classificationFilter, taxonFilter, rootNoteFilter);
//        String groupBy = " GROUP BY tn.uuid ";
        String groupBy = "";
        String fullQuery = select + from + " WHERE " + fullFilter + groupBy;
        return fullQuery;

    }




    /**
     * @param filter
     * @return
     */
    private String getRootNodeFilter(TaxonNodeFilter filter) {
        String result = "";
        if (!filter.isIncludeRootNodes()){
            result = " ( tn.parent IS NOT NULL ) ";
        }
        return result;
    }


    /**
     * @param subtreeFilter
     * @param taxonNodeFilter
     * @param classificationFilter
     * @param taxonFilter
     * @return
     */
    private String getFullFilter(String subtreeFilter, String taxonNodeFilter, String classificationFilter,
            String taxonFilter, String rootNodeFilter) {
        String result = " (1=1 ";
        result = CdmUtils.concat(") AND (", result, subtreeFilter, taxonNodeFilter,
                classificationFilter, taxonFilter, rootNodeFilter) + ") ";
        return result;
    }


    /**
     * @param list
     * @return
     */
    private List<UUID> deduplicate(List<UUID> list) {
        List<UUID> result = new ArrayList<>();
        for (UUID uuid : list){
            if (!result.contains(uuid)){
                result.add(uuid);
            }
        }
        return result;
    }


    private String getSubtreeFilter(TaxonNodeFilter filter) {
        String result = "";
        List<LogicFilter<TaxonNode>> subtreeFilter = filter.getSubtreeFilter();
        initializeSubtreeIndex(subtreeFilter);
        boolean isFirst = true;
        for (LogicFilter<TaxonNode> singleFilter : subtreeFilter){
            String treeIndex = singleFilter.getTreeIndex();
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            if (treeIndex != null){
                result = String.format("(%s%s(tn.treeIndex like '%s%%'))", result, op, treeIndex);
            }else{
                result = String.format("(%s%s(%s))", result, op, "(1=0)");
            }
            isFirst = false;
        }
        return result;
    }


    private String getTaxonNodeFilter(TaxonNodeFilter filter) {
        String result = "";
        List<LogicFilter<TaxonNode>> taxonNodeFilter = filter.getTaxonNodesFilter();
        boolean isFirst = true;
        for (LogicFilter<TaxonNode> singleFilter : taxonNodeFilter){
            String uuid = singleFilter.getUuid().toString();
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            result = String.format("(%s%s(tn.uuid = '%s'))", result, op, uuid);
            isFirst = false;
        }
        return result;
    }


    private String getClassificationFilter(TaxonNodeFilter filter) {
        String result = "";
        List<LogicFilter<Classification>> classificationFilter = filter.getClassificationFilter();
        boolean isFirst = true;
        for (LogicFilter<Classification> singleFilter : classificationFilter){
            String uuid = singleFilter.getUuid().toString();
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            result = String.format("(%s%s(tn.classification.uuid = '%s'))", result, op, uuid);
//            System.out.println(result);
            isFirst = false;
        }
        return result;
    }

    private String getTaxonFilter(TaxonNodeFilter filter) {
        String result = "";
        List<LogicFilter<Taxon>> taxonFilter = filter.getTaxonFilter();
        boolean isFirst = true;
        for (LogicFilter<Taxon> singleFilter : taxonFilter){
            String uuid = singleFilter.getUuid().toString();
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            result = String.format("(%s%s(tn.taxon.uuid = '%s'))", result, op, uuid);
//            System.out.println(result);
            isFirst = false;
        }
        return result;
    }


    /**
     * @param subtreeFilter
     */
    private void initializeSubtreeIndex(List<LogicFilter<TaxonNode>> subtreeFilter) {
        for (LogicFilter<TaxonNode> filter : subtreeFilter){
            if (filter.getTreeIndex() == null){
                //TODO finde without loading, best be sending full list and returning treeindexes
                TaxonNode node = taxonNodeDao.findByUuid(filter.getUuid());
                if (node != null){
                    filter.setTreeIndex(node.treeIndex());
                }
            }
        }

    }


    /**
     * Returns the HQL string for this operation
     *
     */
    private String op2Hql(Op op){
        return op == Op.NOT ? " AND NOT " : op.toString();
    }

    @SuppressWarnings("unchecked")
    private List<UUID> castToUuidList(List<?> queryList){
        return (List<UUID>) queryList;
    }

}
