package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.persistence.dao.description.IWorkingSetDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;

@Repository 
@Qualifier("workingSetDaoImpl")
public class WorkingSetDao extends AnnotatableDaoImpl<WorkingSet> implements IWorkingSetDao {
	private static final Logger logger = Logger.getLogger(WorkingSetDao.class);
	
	public WorkingSetDao() {
		super(WorkingSet.class);
	}

	public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,	List<String> propertyPaths) {
		checkNotInPriorView("WorkingSetDao.getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths)");
		Query query = getSession().createQuery("select description from WorkingSet workingSet join workingSet.descriptions description order by description.titleCache asc");
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
	        if(pageNumber != null) {
	        	query.setFirstResult(pageNumber * pageSize);
	        } else {
	        	query.setFirstResult(0);
	        }
	    }
		List<DescriptionBase> descriptions = (List<DescriptionBase>)query.list();
		Map<DescriptionBase, Set<DescriptionElementBase>> result = new HashMap<DescriptionBase, Set<DescriptionElementBase>>();
		for(DescriptionBase description : descriptions) {
			Criteria criteria = getSession().createCriteria(DescriptionElementBase.class);
			criteria.add(Restrictions.eq("inDescription", description));
			if(features != null && !features.isEmpty()) {
				criteria.add(Restrictions.in("feature", features));
			}
			
			List<DescriptionElementBase> r = (List<DescriptionElementBase>)criteria.list();
			defaultBeanInitializer.initializeAll(r, propertyPaths);
			result.put(description, new HashSet<DescriptionElementBase>(r));
		}
		return result;
	}

	@Override
	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(Class<T> clazz, UUID workingSetUuid, DescriptiveSystemRole role) {
		checkNotInPriorView("WorkingSetDao.getTaxonFeatureDescriptionElementMap(WorkingSet workingSet, Set<Feature> features, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths)");
		Map<UuidAndTitleCache, Map<UUID, Set<T>>> result = new HashMap<UuidAndTitleCache, Map<UUID, Set<T>>>();
		try {
			
			//for maps see
//			http://docs.jboss.org/hibernate/core/3.3/reference/en/html/queryhql.html
//			Elements of indexed collections (arrays, lists, and maps) can be referred to by index in a where clause only:
//
//			Example: from Order order where order.items[0].id = 1234

			
			//feature
			String strQueryTreeId = "SELECT ws.descriptiveSystem.id FROM WorkingSet ws join ws.descriptiveSystem tree WHERE ws.uuid = :workingSetUuid ";
			Query queryTree = getSession().createQuery(strQueryTreeId);
			queryTree.setParameter("workingSetUuid", workingSetUuid);
			List<?> trees = queryTree.list();

			
			String ftSelect = "SELECT feature.id FROM FeatureNode node join node.feature as feature " +
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
			
			String strQuery = " select taxon.uuid, taxon.titleCache, feature.uuid, el "
				+ " from " + strClass + " el " 
				+ " join el.feature feature "
				+ " join el.inDescription d " 
				+ " join d.taxon taxon "
				+ " join d.workingSets ws "
				+ " join ws.descriptiveSystem ftree "
				+ fetch
				+ " where ws.uuid = :workingSetUuid "
					+ " and el.class = :clazz "
					+ " and feature.id in (:features)  "
				+ " order by taxon.uuid asc, feature.uuid asc"
				;
			Query query = getSession().createQuery(strQuery);
					
			query.setParameter("workingSetUuid", workingSetUuid);
			query.setParameter("clazz", clazz.getSimpleName());
			query.setParameterList("features", features);

			//NOTE: Paging does not work with fetch

			// fill result
			List<Object[]> list = query.list();
			for (Object[] listEntry : list){
				UUID taxonUuid = (UUID)listEntry[0];
				String titleCache = (String)listEntry[1];
				UuidAndTitleCache taxon = new UuidAndTitleCache(taxonUuid, titleCache);
				UUID featureUuid = (UUID)listEntry[2];
				T data = (T)listEntry[3];
				Map<UUID, Set<T>> taxonMap = result.get(taxon);
				if (taxonMap == null){
					taxonMap = new HashMap<UUID, Set<T>>();
					result.put(taxon, taxonMap);
				}
				Set<T> featureSet = taxonMap.get(featureUuid);
				if (featureSet == null){
					featureSet = new HashSet<T>();
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
}
