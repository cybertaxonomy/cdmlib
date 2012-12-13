package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao;

@Repository
public class StatisticsDaoHibernateImpl extends DaoBase implements
		IStatisticsDao {

	/**
	 * @return be aware that the returned long might be null
	 */
	@Override
	public Long countNomenclaturalReferences() {
		return countNomenclaturalReferences("");
	}

	private Long countNomenclaturalReferences(String where) {

		Query query = getSession().createQuery(
				"select count(distinct nomenclaturalReference) from TaxonNameBase"
						+ where);
		return (Long) query.uniqueResult();
	}
	
	@Override
	public Long countNomenclaturalReferences(Class clazz){
		return countNomenclaturalReferences("where" + clazz.getSimpleName());
	}

	/**
	 * @return be aware that the returned long might be null
	 */
	
	public Long countDescriptiveSourceReferences() {
		Query query;
		Long count = new Long(0);
		
		// count sources from Descriptions:
		query = getSession().createQuery("select count(distinct d.descriptionSources) from DescriptionBase as d join d.descriptionSources");
		
		
		query = getSession().createQuery("select count(distinct s.citation) from DescriptionElementBase as d join d.sources as s");
		return (Long)query.uniqueResult();
	}
	

}
