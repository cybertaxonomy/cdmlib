/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeFilterDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.compare.ISortableTaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.compare.TaxonNodeDtoComparatorFactory;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * DAO to retrieve taxon node uuids according to a {@link TaxonNodeFilter}.
 *
 * @author a.mueller
 */
@Repository
public class TaxonNodeFilterDaoHibernateImpl
        extends CdmEntityDaoBase<TaxonNode>
        implements ITaxonNodeFilterDao {

    private static final String DESCRIPTION_ELEMENTS = "descriptionElements";

    private static final String DISTRIBUTION_FEATURE_UUID = Feature.uuidDistribution.toString();

    private static final String HQL_TRUE = " 1 "; //maybe we can/should use 'true' instead, needs to be checked for all DB types
    private static final String HQL_FALSE = " 0 "; //maybe we can/should use 'false' instead, needs to be checked for all DB types

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    @Autowired
    private IDefinedTermDao termDao;

    public TaxonNodeFilterDaoHibernateImpl() {
        super(TaxonNode.class);
    }

    @Override
    public long count(TaxonNodeFilter filter){
        String selectPart = "count(*) as n ";
        String queryStr = query(filter, selectPart);
        Query<Long> query = getSession().createQuery(queryStr, Long.class);
        long result = query.uniqueResult();

        return result;
    }

    @Override
    public List<UUID> listUuids(TaxonNodeFilter filter){

        if (filter.hasTaxonomicSortMode()) {
            return listTaxonomicallySorted(filter, true);
        }else {
            String queryStr = query(filter, "tn.uuid");
            Query<UUID> query = getSession().createQuery(queryStr, UUID.class);
            List<UUID> list = query.list();

            list = deduplicate(list);
            return list;
        }
    }

    @Override
    public List<Integer> listIds(TaxonNodeFilter filter){

        if (filter.hasTaxonomicSortMode()) {
            return listTaxonomicallySorted(filter, false);
        }else {
            String queryStr = query(filter, "tn.id");
            Query<Integer> query = getSession().createQuery(queryStr, Integer.class);
            List<Integer> list = query.list();
            list = deduplicate(list);
            return list;
        }
    }

    private <S extends Object> List<S> listTaxonomicallySorted(TaxonNodeFilter filter, boolean isUuid) {

        List<SortableTaxonNodeDto> idTreeIndexList = listIdWithTreeIndex(filter, isUuid);
        List<S> result = new ArrayList<>();
        if (idTreeIndexList.isEmpty()) {
            return result;
        }

        //create tree
        String treeIndexOfClassification = idTreeIndexList.get(0).treeIndex.indexOfTree().toString();
        SortableTaxonNodeDto tempRoot = new SortableTaxonNodeDto(null, null, treeIndexOfClassification,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        SortableTaxonNodeDto last = tempRoot;
        for (SortableTaxonNodeDto node : idTreeIndexList) {
            while (last != null && last.treeIndex != null
                    && !last.treeIndex.hasChild(node.treeIndex)) {
                last = last.parent;
            }
            if (last != null) {
                last.children.add(node);
                node.parent = last;
                last = node;
            }
        }

        //sort
        Comparator<ISortableTaxonNodeDto> comparator = TaxonNodeDtoComparatorFactory.getDtoComparator(filter.getBaseSortMode());
        sortRecursively(tempRoot, filter, comparator);

        //tree->list
        tempRoot.children.forEach(child->addToResultList(child, result, isUuid));
        return result;
    }

    @SuppressWarnings("unchecked")
    private <S extends Object> void addToResultList(SortableTaxonNodeDto dto, List<S> result, boolean isUuid) {
        if (isUuid) {
            result.add((S) dto.uuid);
        }else {
            result.add((S) dto.id);
        }
        dto.children.forEach(child->addToResultList(child, result, isUuid));
        return;
    }

    private <S extends Object>  void sortRecursively(SortableTaxonNodeDto root, TaxonNodeFilter filter,
            Comparator<ISortableTaxonNodeDto> comparator) {

        root.children.sort(comparator);
        root.children.forEach(child -> sortRecursively(child, filter, comparator));
    }

    //TODO maybe we can merge with SortableTaxonNodeQueryResultDto
    //note: should not be a record as not all field can be final (e.g. parent)
    private class SortableTaxonNodeDto implements ISortableTaxonNodeDto{
        private Integer id;
        private UUID uuid;
        private TreeIndex treeIndex;
        private UUID parentUUID;
        private TaxonNodeStatus status;
        private Integer rankOrderIndex;
        private List<TaggedText> taggedTitle;
        private UUID taxonUuid;
        private String nameTitleCache;
        private Integer sortIndex;

        private SortableTaxonNodeDto parent;

        List<SortableTaxonNodeDto> children = new ArrayList<>();

        public SortableTaxonNodeDto(Integer id, UUID uuid, String treeIndex, UUID parentUuid, Integer sortIndex,
                TaxonNodeStatus status, UUID taxonUuid, String nameTitleCache, Integer rankOrderIndex,
                String genusOrUninomial, String infraGenericEpithet, String specificEpithet,
                String infraSpecificEpithet, String cultivarEpithet, String appendedPhrase, String authorshipCache) {

            this.id = id;
            this.treeIndex = treeIndex == null ? null : TreeIndex.NewInstance(treeIndex);
            this.uuid = uuid;
            this.parentUUID = parentUuid;
            this.sortIndex = sortIndex;
            this.status = status;
            this.taxonUuid = taxonUuid;
            this.nameTitleCache = nameTitleCache;
            this.rankOrderIndex = rankOrderIndex;
            this.taggedTitle = createTaggedTitle(genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
                    cultivarEpithet, appendedPhrase, authorshipCache);
        }

        private List<TaggedText> createTaggedTitle(String genusOrUninomial, String infraGenericEpithet, String specificEpithet,
                String infraSpecificEpithet, String cultivarEpithet, String appendedPhrase, String authorshipCache) {

            TaggedTextBuilder builder = new TaggedTextBuilder();
            if (StringUtils.isNotBlank(genusOrUninomial)) {
                builder.add(TagEnum.name, genusOrUninomial);
            }
            if (StringUtils.isNotBlank(infraGenericEpithet)) {
                builder.add(TagEnum.name, infraGenericEpithet);
            }
            if (StringUtils.isNotBlank(specificEpithet)) {
                builder.add(TagEnum.name, specificEpithet);
            }
            if (StringUtils.isNotBlank(infraSpecificEpithet)) {
                builder.add(TagEnum.name, infraSpecificEpithet);
            }
            if (StringUtils.isNotBlank(cultivarEpithet)) {
                builder.add(TagEnum.name, cultivarEpithet);
            }
            if (StringUtils.isNotBlank(appendedPhrase)) {
                builder.add(TagEnum.name, appendedPhrase);
            }
            if (StringUtils.isNotBlank(authorshipCache)) {
                builder.add(TagEnum.authors, authorshipCache);
            }
            return builder.getTaggedText();
        }

        @Override
        public TaxonNodeStatus getStatus() {
            return status;
        }

        @Override
        public Integer getRankOrderIndex() {
            return rankOrderIndex;
        }

        @Override
        public List<TaggedText> getTaggedTitle() {
            return taggedTitle;
        }

        @Override
        public UUID getTaxonUuid() {
            return taxonUuid;
        }

        @Override
        public String getNameTitleCache() {
            return nameTitleCache;
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public UUID getParentUUID() {
            return parentUUID;
        }

        @Override
        public Integer getSortIndex() {
            return sortIndex;
        }

        @Override
        public String getTreeIndex() {
            return treeIndex.toString();
        }
    }

    private <S extends Object> List<SortableTaxonNodeDto> listIdWithTreeIndex(TaxonNodeFilter filter, boolean isUuid){

//        String selectPart = isUuid ? "tn.id, tn.uuid, tn.treeIndex" : "tn.id, tn.treeIndex";
        //TODO sortIndex of taxon node not yet supported (for natural order)
        String selectPart = "tn.id, tn.uuid, tn.treeIndex, parent.uuid, cast(null as integer), tn.status, "
                + "taxon.uuid, "
                + "name.titleCache, rank.orderIndex, "
                + "name.genusOrUninomial, name.infraGenericEpithet, name.specificEpithet, "
                +   "name.infraSpecificEpithet, name.cultivarEpithet, name.appendedPhrase, name.authorshipCache";
        String queryStr = query(filter, selectPart);
        List<SortableTaxonNodeDto> list = getSession().createQuery(queryStr, Object[].class)
                .getResultList()
                .stream()
                .map(row -> new SortableTaxonNodeDto(
                       (Integer)row[0]
                       ,(UUID)row[1]
                     ,   (String) row[2]
                     ,   (UUID) row[3]
                     ,   (Integer) row[4]
                     ,   (TaxonNodeStatus) row[5]
                     ,   (UUID) row[6]
                     ,   (String) row[7]
                     ,   (Integer) row[8]
                     ,   (String) row[9] //genus
                     ,   (String) row[10]
                     ,   (String) row[11]
                     ,   (String) row[12]
                     ,   (String) row[13]
                     ,   (String) row[14]
                     ,   (String) row[15]
                        ))
                .toList();
        list = deduplicate(list);
        return list;
    }

    //maybe we will later want to have ordering included
    private String query(TaxonNodeFilter filter, String selectPart){

        String select = " SELECT " + selectPart;
        boolean isSorted = filter.hasTaxonomicSortMode();  //sorted taxonomically, not by IDs or treeindex
        String from = getFrom(filter, isSorted);
        String subtreeFilter = getSubtreeFilter(filter);
        String taxonNodeFilter = getTaxonNodeFilter(filter);
        String classificationFilter = getClassificationFilter(filter);
        String taxonFilter = getTaxonFilter(filter);
        String rootNodeFilter = getRootNodeFilter(filter);
        String rankMaxFilter = getRankMaxFilter(filter);
        String rankMinFilter = getRankMinFilter(filter);
        String areaFilter = getAreaFilter(filter);
        String unpublishFilter = getUnpublishFilter(filter);

        String fullFilter = getFullFilter(subtreeFilter, taxonNodeFilter,
                classificationFilter, taxonFilter,
                rankMaxFilter, rankMinFilter, areaFilter, rootNodeFilter,
                unpublishFilter);
//        String groupBy = " GROUP BY tn.uuid ";
        String groupBy = "";
        String orderBy = getOrderBy(filter, selectPart);
        String fullQuery = select + from + " WHERE " + fullFilter + groupBy + orderBy;

        return fullQuery;
    }

    private String getOrderBy(TaxonNodeFilter filter, String selectPart) {
        String orderBy = "";
        if (filter.getSortMode()!= null && !selectPart.contains("count")){
            orderBy = "ORDER BY " + filter.getSortMode().getHql();
        }
        return orderBy;
    }

    private String getFrom(TaxonNodeFilter filter, boolean isSorted){
        String from = " FROM TaxonNode tn ";
        if (hasTaxonFilter(filter) || isSorted){
            from += " LEFT JOIN tn.taxon taxon ";  //LEFT to allow includeRootNode
        }
        if(!filter.getAreaFilter().isEmpty()){
            from += " INNER JOIN taxon.descriptions descriptions "
                  + " INNER JOIN descriptions.descriptionElements " + DESCRIPTION_ELEMENTS + " ";
        }
        if (isSorted) {
            from += " LEFT JOIN taxon.name name "
                  + " LEFT JOIN name.rank rank "
                  + " LEFT JOIN tn.parent parent "
//                  + " LEFT JOIN parent.childNodes ch"
                  ;
        }
        return from;
    }

    private boolean hasTaxonFilter(TaxonNodeFilter filter) {
        boolean result = !filter.getAreaFilter().isEmpty()
                || !filter.isIncludeUnpublished();
        return result;
    }

    private String getAreaFilter(TaxonNodeFilter filter) {
        String result = "";
        List<LogicFilter<NamedArea>> areaFilter = filter.getAreaFilter();
        boolean isFirst = true;
        List<Integer> areaIds = new ArrayList<>();
        for (LogicFilter<NamedArea> singleFilter : areaFilter){
            areaIds = getChildAreasRecursively(singleFilter.getUuid());
            String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
            result = String.format("(%s%s(" + DESCRIPTION_ELEMENTS + ".feature.uuid='" + DISTRIBUTION_FEATURE_UUID + "' "
                    + " AND " + DESCRIPTION_ELEMENTS + ".area.id in (%s))",
                    result, op, org.springframework.util.StringUtils.collectionToCommaDelimitedString(areaIds)
                    );
            if (!filter.isIncludeAbsentDistributions()) {
                result +=  " AND " + DESCRIPTION_ELEMENTS + ".status.absenceTerm = " + HQL_FALSE;
            }
            result += ")";
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
        Query<UUID> query = getSession().createQuery(queryStr, UUID.class);
        List<UUID> childAreas = query.list();
        for (UUID childArea : childAreas) {
            areaIds.addAll(getChildAreasRecursively(childArea));
        }
        return areaIds;
    }




    private String getRootNodeFilter(TaxonNodeFilter filter) {
        String result = "";
        if (!filter.isIncludeRootNodes()){
            result = " ( tn.parent IS NOT NULL ) ";
        }
        return result;
    }

    private String getUnpublishFilter(TaxonNodeFilter filter) {
        String result = "";
        if (!filter.isIncludeUnpublished()){
            result = " ( taxon.publish = "+HQL_TRUE+" OR tn.parent IS NULL ) ";
        }
        return result;
    }

    private String getFullFilter(String subtreeFilter, String taxonNodeFilter, String classificationFilter,
            String taxonFilter, String rankMaxFilter, String rankMinFilter, String areaFilter, String rootNodeFilter,
            String unpublishFilter) {
        String result = " (1=1 ";
        result = CdmUtils.concat(") AND (", result, subtreeFilter, taxonNodeFilter,
                classificationFilter, taxonFilter, rankMaxFilter, rankMinFilter, areaFilter, rootNodeFilter,
                unpublishFilter) + ") ";
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

    private void initializeSubtreeIndex(List<LogicFilter<TaxonNode>> subtreeFilter) {
        for (LogicFilter<TaxonNode> filter : subtreeFilter){
            if (filter.getTreeIndex() == null){
                //TODO find without loading, best be sending full list and returning tree indexes
                TaxonNode node = taxonNodeDao.findByUuid(filter.getUuid());
                if (node != null){
                    filter.setTreeIndex(node.treeIndex());
                }
            }
        }
    }

    /**
     * Returns the HQL string for this operation
     */
    private String op2Hql(Op op){
        return op == Op.NOT ? " AND NOT " : op.toString();
    }
}
