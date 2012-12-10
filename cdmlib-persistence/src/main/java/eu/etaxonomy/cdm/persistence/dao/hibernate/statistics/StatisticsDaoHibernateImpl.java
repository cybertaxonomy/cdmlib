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
	@Override
	public Long countDescriptiveSourceReferences() {

		// count sources from Descriptions:
		Query query;
		Long count = new Long(0);
		query = getSession().createQuery(
				"select distinct descriptionSources from DescriptionBase");
		// org.hibernate.type.Type[] types= query.getReturnTypes();
		List<DescriptionElementSource> queryList = query.list();
		Set<DescriptionElementSource> sourcesSet = new HashSet<DescriptionElementSource>();
		sourcesSet.addAll(query.list());
		if (queryList == null/* || queryList.isEmpty() */) {
			return null;

		} else {

			count += sourcesSet.size();
		}

		// count sources from Descriptive Elements:
		// TODO this part does count to many items maybe we have to join some
		// result tables or compare the uuids from query result set, to
		// eliminate doubles

		query = getSession().createQuery(
		// "select count(distinct citation) from (select distinct sources from DescriptionElementBase)");
				"select distinct sources from DescriptionElementBase");

		// count=(Long)query.uniqueResult();
		org.hibernate.type.Type[] types = query.getReturnTypes();
		// Object object =query.uniqueResult();

		queryList = query.list();
		if (queryList == null/* || queryList.isEmpty() */) {
			return null;

		} else {

			// count += queryList.size();
			// ////
		}
		// Set<DescriptionElementSource> sourceSet = new
		// HashSet<DescriptionElementSource>();
		Set<Reference> referenceSet = new HashSet<Reference>();
		for (DescriptionElementSource descriptionElementSource : queryList) {
			if (descriptionElementSource.getCitation() != null) {
				referenceSet.add(descriptionElementSource.getCitation());
			}
		}

		// sourceSet.addAll(queryList);
		// count += sourceSet.size();
		count += referenceSet.size();

		return count;
	}
}
