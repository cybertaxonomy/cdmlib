package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao;

@Repository
public class StatisticsDaoHibernateImpl extends DaoBase implements
		IStatisticsDao {

	@Override
	public Long countNomenclaturalReferences() {
//		Query query = createNomenclaturalReferencesQuery();
		Query query = getSession()
				.createQuery(
						"select count(distinct nomenclaturalreference_id) from TaxonNameBase");
		if (query.uniqueResult() != null) {

			return (Long) query.uniqueResult(); 
		} else {
			return null;
		}
	}



}
