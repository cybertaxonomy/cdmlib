package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;

@Repository
public class IdentificationKeyDaoImpl extends DaoBase implements IIdentificationKeyDao {

	@Autowired
	@Qualifier("defaultBeanInitializer")
	protected IBeanInitializer defaultBeanInitializer;

	@Override
    public int count() {
		Query query = getSession().createQuery("select count(key) from eu.etaxonomy.cdm.model.description.IIdentificationKey key");

		List<Long> result = query.list();
		Integer total = 0;
		for(Long l : result) {
			total += l.intValue();
		}
		return total;
	}

	@Override
    public List<IIdentificationKey> list(Integer limit,Integer start, List<String> propertyPaths) {
		Query query = getSession().createQuery("select key from eu.etaxonomy.cdm.model.description.IIdentificationKey key order by created desc");

		if(limit != null) {
			if(start != null) {
				query.setFirstResult(start);
			} else {
				query.setFirstResult(0);
			}
			query.setMaxResults(limit);
		}

		List<IIdentificationKey> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao#findKeysConvering(eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
	@Override
	public <T extends IIdentificationKey> List<T> findByTaxonomicScope(
			TaxonBase taxon, Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {

		Query query = getSession().createQuery("select key from " + type.getCanonicalName() +" key join key.taxonomicScope ts where ts = (:taxon)");
		query.setParameter("taxon", taxon);
		List<T> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao#findKeysConvering(eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
	@Override
	public <T extends IIdentificationKey> Long countByTaxonomicScope(TaxonBase taxon, Class<T> type) {

		Query query = getSession().createQuery("select count(key) from " + type.getCanonicalName() +" key join key.taxonomicScope ts where ts = (:taxon)");
		query.setParameter("taxon", taxon);
		List<Long> list = query.list();
		Long count = 0l;
		for(Long perTypeCount : list){
			count += perTypeCount;
		}
		return count;
	}

}
