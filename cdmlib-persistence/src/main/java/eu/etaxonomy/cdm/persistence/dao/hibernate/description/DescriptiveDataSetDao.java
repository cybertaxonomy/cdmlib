package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptiveDataSetDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermTreeDao;
import eu.etaxonomy.cdm.persistence.dto.DescriptiveDataSetBaseDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

@Repository
@Qualifier("descriptiveDataSetDaoImpl")
public class DescriptiveDataSetDao
        extends IdentifiableDaoBase<DescriptiveDataSet>
        implements IDescriptiveDataSetDao {

    private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ITermTreeDao termTreeDao;

	@Autowired
    private IDefinedTermDao termDao;

	@Autowired
    private ITaxonNodeDao nodeDao;

	public DescriptiveDataSetDao() {
		super(DescriptiveDataSet.class);
	}

	@Override
    public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,	List<String> propertyPaths) {
		checkNotInPriorView("DescriptiveDataSetDao.getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,Integer pageNumber, List<String> propertyPaths)");
		Query<DescriptionBase> query = getSession().createQuery("SELECT description FROM DescriptiveDataSet descriptiveDataSet JOIN DescriptiveDataSet.descriptions description ORDER BY description.titleCache ASC", DescriptionBase.class);

		addPageSizeAndNumber(query, pageSize, pageNumber);
        List<DescriptionBase> descriptions = query.list();

        Map<DescriptionBase, Set<DescriptionElementBase>> result = new HashMap<>();
		for(DescriptionBase<?> description : descriptions) {

		    CriteriaBuilder cb = getCriteriaBuilder();
	        CriteriaQuery<DescriptionElementBase> cq = cb.createQuery(DescriptionElementBase.class);
	        Root<DescriptionElementBase> root = cq.from(DescriptionElementBase.class);

	        List<Predicate> predicates = new ArrayList<>();

	        predicates.add(cb.equal(root.get("inDescription"), description));
	        if(!CdmUtils.isNullSafeEmpty(features)) {
	            root.get("feature").in(features);
	            predicates.add(root.get("feature").in(features));
            }
	        cq.select(root);
	        cq.where(cb.and(predicates.toArray(new Predicate[0])));
	        List<DescriptionElementBase> r = getSession().createQuery(cq).getResultList();

	        defaultBeanInitializer.initializeAll(r, propertyPaths);
            result.put(description, new HashSet<>(r));
		}
		return result;
	}

	@Override
	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(
	        Class<T> clazz, UUID descriptiveDataSetUuid, DescriptiveSystemRole role) {
		checkNotInPriorView("DescriptiveDataSetDao.getTaxonFeatureDescriptionElementMap(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths)");

		Map<UuidAndTitleCache, Map<UUID, Set<T>>> result = new HashMap<>();
		try {

			//for maps see
//			http://docs.jboss.org/hibernate/core/3.3/reference/en/html/queryhql.html
//			Elements of indexed collections (arrays, lists, and maps) can be referred to by index in a where clause only:
//
//			Example: from Order order where order.items[0].id = 1234

			//feature
			String strQueryTreeId = "SELECT ws.descriptiveSystem.id FROM DescriptiveDataSet dds join dds.descriptiveSystem tree WHERE dds.uuid = :descriptiveDataSetUuid ";
			Query<Integer> queryTree = getSession().createQuery(strQueryTreeId, Integer.class);
			queryTree.setParameter("descriptiveDataSetUuid", descriptiveDataSetUuid);
			List<Integer> trees = queryTree.list();

			String ftSelect = "SELECT feature.id FROM TermNode node join node.feature as feature " +
					" WHERE node.featureTree.id in (:trees) ";
			Query<Integer> ftQuery = getSession().createQuery(ftSelect, Integer.class);
			ftQuery.setParameterList("trees", trees);
			List<Integer> features = ftQuery.list();

			String strClass = (clazz == null )? "DescriptionElementBase" : clazz.getSimpleName();

			String fetch = "";
			if (clazz!= null && clazz.equals(CategoricalData.class)){
				fetch = " left join fetch el.states stateList join fetch stateList.state ";
			}else if (clazz!=null && clazz.equals(QuantitativeData.class)){
				fetch = " left join fetch el.statisticalValues valueList join fetch valueList.type ";
			}

			String strQuery = " select taxon.uuid, taxon.id, taxon.titleCache, feature.uuid, el "
				+ " from " + strClass + " el "
				+ " join el.feature feature "
				+ " join el.inDescription d "
				+ " join d.taxon taxon "
				+ " join d.descriptiveDataSets ws "
				+ " join ws.descriptiveSystem ftree "
				+ fetch
				+ " where ws.uuid = :descriptiveDataSetUuid "
					+ " and el.class = :clazz "
					+ " and feature.id in (:features)  "
				+ " order by taxon.uuid asc, feature.uuid asc"
				;
			Query<Object[]> query = getSession().createQuery(strQuery, Object[].class);

			query.setParameter("descriptiveDataSetUuid", descriptiveDataSetUuid);
			query.setParameter("clazz", clazz.getSimpleName());
			query.setParameterList("features", features);

			//NOTE: Paging does not work with fetch

			// fill result
            List<Object[]> list = query.list();
			for (Object[] listEntry : list){
				UUID taxonUuid = (UUID)listEntry[0];
				Integer id = (Integer)listEntry[1];
				String titleCache = (String)listEntry[2];
				UuidAndTitleCache taxon = new UuidAndTitleCache(taxonUuid, id, titleCache);
				UUID featureUuid = (UUID)listEntry[3];
                T data = (T)listEntry[4];
				Map<UUID, Set<T>> taxonMap = result.get(taxon);
				if (taxonMap == null){
					taxonMap = new HashMap<>();
					result.put(taxon, taxonMap);
				}
				Set<T> featureSet = taxonMap.get(featureUuid);
				if (featureSet == null){
					featureSet = new HashSet<>();
					taxonMap.put(featureUuid, featureSet);
				}else{
					if (logger.isDebugEnabled()){logger.debug("feature set already exists");}
				}
				featureSet.add(data);
			}
//			defaultBeanInitializer.initialize(
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

    @Override
    public List<UuidAndTitleCache<DescriptiveDataSet>> getDescriptiveDataSetUuidAndTitleCache(Integer limitOfInitialElements,
            String pattern) {
        Session session = getSession();

        String queryString = "SELECT uuid, id, label FROM DescriptiveDataSet ";

        if ( pattern != null){
            queryString += " WHERE ";
            queryString += " label LIKE :pattern";

        }

        Query<Object[]> query = session.createQuery(queryString, Object[].class);

        if (limitOfInitialElements != null){
            query.setMaxResults(limitOfInitialElements);
        }
        if (pattern != null){
              pattern = pattern.replace("*", "%");
              pattern = pattern.replace("?", "_");
              pattern = pattern + "%";
              query.setParameter("pattern", pattern);
        }

        List<Object[]> result = query.list();
        List<UuidAndTitleCache<DescriptiveDataSet>> list = new ArrayList<>();
        for(Object[] object : result){
            list.add(new UuidAndTitleCache<DescriptiveDataSet>(DescriptiveDataSet.class, (UUID) object[0],(Integer)object[1], (String)object[2]));
        }

        return list;
    }

    private List<UUID> getNodeUuidsForDescriptiveDataSet(UUID uuid) {
        Session session = getSession();

        String queryString = "SELECT t.uuid  FROM DescriptiveDataSet a JOIN a.taxonSubtreeFilter as t WHERE a.uuid = :uuid";
        Query<UUID> query = session.createQuery(queryString, UUID.class);
        query.setParameter("uuid", uuid);

        List<UUID> result = query.list();
        List<UUID> list = new ArrayList<>();
        for(UUID object : result){
            list.add(object);
        }
        return list;
    }

    private List<UUID> getDescriptionUuidsForDescriptiveDataSet(UUID uuid) {
        Session session = getSession();
        String queryString = "SELECT t.uuid  FROM DescriptiveDataSet a JOIN a.descriptions as t WHERE a.uuid = :uuid";

        Query<UUID> query = session.createQuery(queryString, UUID.class);
        query.setParameter("uuid", uuid);

        List<UUID> result = query.list();
        List<UUID> list = new ArrayList<>();
        for(UUID object : result){
            list.add(object);
        }
        return list;
    }

    @Override
    public DescriptiveDataSetBaseDto getDescriptiveDataSetDtoByUuid(UUID uuid) {
        String queryString = DescriptiveDataSetBaseDto.getDescriptiveDataSetDtoSelect()
                + " WHERE a.uuid = :uuid"
                + " ORDER BY a.titleCache";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("uuid", uuid);

        List<Object[]> result = query.list();

        List<DescriptiveDataSetBaseDto> list = DescriptiveDataSetBaseDto.descriptiveDataSetBaseDtoListFrom(result);
        UUID descriptiveSystemUuid = null;
        UUID minRankUuid = null;
        UUID maxRankUuid = null;
        if (result != null && !result.isEmpty()){
            Object[] descriptiveDataSetResult = result.get(0);
            descriptiveSystemUuid = (UUID)descriptiveDataSetResult[4];
            minRankUuid = (UUID)descriptiveDataSetResult[5];
            maxRankUuid = (UUID)descriptiveDataSetResult[6];
        }else{
            return null;
        }
        //get descriptiveSystem
        DescriptiveDataSetBaseDto dto = list.get(0);
        if (descriptiveSystemUuid != null){
            TermCollectionDto treeDto = termTreeDao.getTermTreeDtosByUuid(descriptiveSystemUuid);
            dto.setDescriptiveSystem(treeDto);
        }
        //get taxon nodes
        List<UUID> nodeUuids = getNodeUuidsForDescriptiveDataSet(uuid);
        List<TaxonNodeDto> nodeDtos = nodeDao.getTaxonNodeDtosWithoutParent(nodeUuids);
        Set<TaxonNodeDto> nodeSet = new HashSet<>(nodeDtos);
        dto.setSubTreeFilter(nodeSet);

        List<UUID> descriptionUuidList = getDescriptionUuidsForDescriptiveDataSet(uuid);
        Set<UUID> descriptionUuids = new HashSet<>(descriptionUuidList);
        dto.setDescriptionUuids(descriptionUuids);

        TermDto minRank = termDao.getTermDto(minRankUuid);
        TermDto maxRank = termDao.getTermDto(maxRankUuid);
        dto.setMaxRank(maxRank);
        dto.setMinRank(minRank);
        return dto;
    }
}