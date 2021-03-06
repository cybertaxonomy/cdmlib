package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptiveDataSetDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

@Repository
@Qualifier("descriptiveDataSetDaoImpl")
public class DescriptiveDataSetDao
        extends IdentifiableDaoBase<DescriptiveDataSet>
        implements IDescriptiveDataSetDao {
	private static final Logger logger = Logger.getLogger(DescriptiveDataSetDao.class);

	public DescriptiveDataSetDao() {
		super(DescriptiveDataSet.class);
	}

	@Override
    public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,	List<String> propertyPaths) {
		checkNotInPriorView("DescriptiveDataSetDao.getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,Integer pageNumber, List<String> propertyPaths)");
		Query query = getSession().createQuery("SELECT description FROM DescriptiveDataSet descriptiveDataSet JOIN DescriptiveDataSet.descriptions description ORDER BY description.titleCache ASC");

		if(pageSize != null) {
			query.setMaxResults(pageSize);
	        if(pageNumber != null) {
	        	query.setFirstResult(pageNumber * pageSize);
	        } else {
	        	query.setFirstResult(0);
	        }
	    }
		@SuppressWarnings("unchecked")
        List<DescriptionBase> descriptions = query.list();
		Map<DescriptionBase, Set<DescriptionElementBase>> result = new HashMap<>();
		for(DescriptionBase description : descriptions) {
			Criteria criteria = getSession().createCriteria(DescriptionElementBase.class);
			criteria.add(Restrictions.eq("inDescription", description));
			if(features != null && !features.isEmpty()) {
				criteria.add(Restrictions.in("feature", features));
			}

			@SuppressWarnings("unchecked")
            List<DescriptionElementBase> r = criteria.list();
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
			Query queryTree = getSession().createQuery(strQueryTreeId);
			queryTree.setParameter("descriptiveDataSetUuid", descriptiveDataSetUuid);
			List<?> trees = queryTree.list();


			String ftSelect = "SELECT feature.id FROM TermNode node join node.feature as feature " +
					" WHERE node.featureTree.id in (:trees) ";
			Query ftQuery = getSession().createQuery(ftSelect);
			ftQuery.setParameterList("trees", trees);
			List<?> features = ftQuery.list();

			String strClass = (clazz == null )? "DescriptionElementBase" : clazz.getSimpleName();

			String fetch = "";
			if (clazz.equals(CategoricalData.class)){
				fetch = " left join fetch el.states stateList join fetch stateList.state ";
			}else if (clazz.equals(QuantitativeData.class)){
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
			Query query = getSession().createQuery(strQuery);

			query.setParameter("descriptiveDataSetUuid", descriptiveDataSetUuid);
			query.setParameter("clazz", clazz.getSimpleName());
			query.setParameterList("features", features);

			//NOTE: Paging does not work with fetch

			// fill result
			@SuppressWarnings("unchecked")
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UuidAndTitleCache<DescriptiveDataSet>> getDescriptiveDataSetUuidAndTitleCache(Integer limitOfInitialElements,
            String pattern) {
        Session session = getSession();

        String queryString = "SELECT uuid, id, label FROM DescriptiveDataSet ";

        if ( pattern != null){
            queryString += " WHERE ";
            queryString += " label LIKE :pattern";

        }

        Query query;
        query = session.createQuery(queryString);


        if (limitOfInitialElements != null){
            query.setMaxResults(limitOfInitialElements);
        }
        if (pattern != null){
              pattern = pattern.replace("*", "%");
              pattern = pattern.replace("?", "_");
              pattern = pattern + "%";
              query.setParameter("pattern", pattern);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        List<UuidAndTitleCache<DescriptiveDataSet>> list = new ArrayList<>();
        for(Object[] object : result){
            list.add(new UuidAndTitleCache<DescriptiveDataSet>(DescriptiveDataSet.class, (UUID) object[0],(Integer)object[1], (String)object[2]));
        }

        return list;
    }
}
