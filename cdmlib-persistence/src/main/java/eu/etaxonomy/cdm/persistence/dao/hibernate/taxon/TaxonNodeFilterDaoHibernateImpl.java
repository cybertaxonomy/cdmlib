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
import org.springframework.util.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeFilterDao;

/**
 * DAO to retrieve taxon node uuids according to a {@link TaxonNodeFilter}.
 *
 * @author a.mueller
 */
@Repository
public class TaxonNodeFilterDaoHibernateImpl extends CdmEntityDaoBase<TaxonNode> implements ITaxonNodeFilterDao {

    private static final String DESCRIPTION_ELEMENTS = "descriptionElements";

    private static final String FEATURE_UUID = "9fc9d10c-ba50-49ee-b174-ce83fc3f80c6";

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    @Autowired
    private IDefinedTermDao termDao;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> idList(TaxonNodeFilter filter){
        String queryStr = query(filter, "tn.id");
        Query query = getSession().createQuery(queryStr);
        List<Integer> list = castToIntegerList(query.list());

        list = deduplicate(list);
        return list;
    }

    //maybe we will later want to have ordering included
    private String query(TaxonNodeFilter filter, String selectPart){
        String select = " SELECT " + selectPart;
        String from = getFrom(filter);
        String subtreeFilter = getSubtreeFilter(filter);
        String taxonNodeFilter = getTaxonNodeFilter(filter);
        String classificationFilter = getClassificationFilter(filter);
        String taxonFilter = getTaxonFilter(filter);
        String rootNodeFilter = getRootNodeFilter(filter);
        String rankMaxFilter = getRankMaxFilter(filter);
        String rankMinFilter = getRankMinFilter(filter);
        String areaFilter = getAreaFilter(filter);
        String fullFilter = getFullFilter(subtreeFilter, taxonNodeFilter,
                classificationFilter, taxonFilter,
                rankMaxFilter, rankMinFilter, areaFilter, rootNodeFilter);
//        String groupBy = " GROUP BY tn.uuid ";
        String groupBy = "";
        String fullQuery = select + from + " WHERE " + fullFilter + groupBy;
        return fullQuery;

    }

    private String getFrom(TaxonNodeFilter filter){
        String from = " FROM TaxonNode tn ";
        if(!filter.getAreaFilter().isEmpty()){
            from += "inner join tn.taxon.descriptions descriptions inner join descriptions.descriptionElements "+DESCRIPTION_ELEMENTS+" ";
        }
        return from;
    }

    private String getAreaFilter(TaxonNodeFilter filter) {
        String result = "";
        List<LogicFilter<NamedArea>> areaFilter = filter.getAreaFilter();
        boolean isFirst = true;
        List<Integer> areaIds = new ArrayList<>();
        for (LogicFilter<NamedArea> singleFilter : areaFilter){
            areaIds = getChildAreasRecursively(singleFilter.getUuid());
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            result = String.format("(%s%s("+DESCRIPTION_ELEMENTS+".feature.uuid='"+FEATURE_UUID+"' AND "+DESCRIPTION_ELEMENTS+".area.id in (%s)))",
                    result, op, StringUtils.collectionToCommaDelimitedString(areaIds));
            isFirst = false;
        }
        return result;
    }

    private List<Integer> getChildAreasRecursively(UUID uuid){
        List<Integer> areaIds = new ArrayList<>();
        NamedArea area = HibernateProxyHelper.deproxy(termDao.load(uuid), NamedArea.class);
        areaIds.add(area.getId());
        String queryStr = String.format("SELECT includes.uuid FROM DefinedTermBase t inner join t.includes includes WHERE t.uuid = '%s'",
                area.getUuid().toString());
        Query query = getSession().createQuery(queryStr);
        List<UUID> childAreas = castToUuidList(query.list());
        for (UUID childArea : childAreas) {
            areaIds.addAll(getChildAreasRecursively(childArea));
        }
        return areaIds;
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
     * @param rankMinFilter
     * @param rankMaxFilter
     * @param rootNodeFilter
     * @return
     */
    private String getFullFilter(String subtreeFilter, String taxonNodeFilter, String classificationFilter,
            String taxonFilter, String rankMaxFilter, String rankMinFilter, String areaFilter, String rootNodeFilter) {
        String result = " (1=1 ";
        result = CdmUtils.concat(") AND (", result, subtreeFilter, taxonNodeFilter,
                classificationFilter, taxonFilter, rankMaxFilter, rankMinFilter, areaFilter, rootNodeFilter) + ") ";
        return result;
    }


    /**
     * @param list
     * @return
     */
    private <T> List<T> deduplicate(List<T> list) {
        List<T> result = new ArrayList<>();
        for (T uuid : list){
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

    private String getRankMaxFilter(TaxonNodeFilter filter) {
        String result = "";
        LogicFilter<Rank> rankFilter = filter.getRankMax();
        if(rankFilter!=null){
            UUID rankUuid = rankFilter.getUuid();
            Rank rank = (Rank) termDao.load(rankUuid);
            result = String.format("(tn.taxon.name.rank.orderIndex >= %s)", rank.getOrderIndex());
        }
        return result;
    }

    private String getRankMinFilter(TaxonNodeFilter filter) {
        String result = "";
        LogicFilter<Rank> rankFilter = filter.getRankMin();
        if(rankFilter!=null){
            UUID rankUuid = rankFilter.getUuid();
            Rank rank = (Rank) termDao.load(rankUuid);
            result = String.format("(tn.taxon.name.rank.orderIndex <= %s)", rank.getOrderIndex());
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
                //TODO finde without loading, best be sending full list and returning tree indexes
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
    @SuppressWarnings("unchecked")
    private List<Integer> castToIntegerList(List<?> queryList){
        return (List<Integer>) queryList;
    }
}
