package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
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
	public Long countNomenclaturalReferences(Class clazz) {
		return countNomenclaturalReferences("where" + clazz.getSimpleName());
	}

	/**
	 * @return be aware that the returned long might be null
	 */

	public Long countDescriptiveSourceReferences() {
		Query query;
		Long count = new Long(0);

		// count sources from Descriptions:
		query = getSession()
				.createQuery(
						"select count(distinct d.descriptionSources) from DescriptionBase as d join d.descriptionSources");

		query = getSession()
				.createQuery(
						"select count(distinct s.citation) from DescriptionElementBase as d join d.sources as s");
		return (Long) query.uniqueResult();
	}

	// TODO remove this method:
	@Override
	public void tryArround() {
		Criteria criteria = getSession().createCriteria(Synonym.class);
		// Query query =
		// System.out.println("query: "+((Query) criteria).getQueryString());
		// System.out.println(getSession().createCriteria(Synonym.class);
		criteria.list();
		// criteria.
		// System.out.println(criteria.toString());
		System.out.println("");

	}

	@Override
	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification) {

		if (clazz.equals(TaxonBase.class)) {

			return countTaxaInClassification(Taxon.class, classification)
					+ countTaxaInClassification(Synonym.class, classification);
		}
		Criteria criteria = getSession().createCriteria(TaxonNode.class);

		if (clazz.equals(Taxon.class)) {
			criteria = getSession().createCriteria(TaxonNode.class);
			criteria.add(Restrictions.eq("classification", classification));
			criteria.setProjection(Projections.rowCount());
			// criteria.
			return Long.valueOf((Integer) criteria.uniqueResult());
		}

		else if (clazz.equals(Synonym.class)) {
			// criteria= getSession().createCriteria(TaxonNode.class);
			Query query = getSession()
					.createQuery(
							"create table t as select taxon from TaxonNode where classification = :classification; "
									+ "select (distinct sr.uuid) from t join t.synonymRelations as sr"
					// "select distinct sr.uuid from"
					// +
					// " (select taxon from TaxonNode tn where tn.classification = :classification) "
					// +
					// "as t "
					// + "join t.synonymRelations as sr "
					);
			query.setParameter("classification", classification);
			System.out.println(query.getQueryString());
			List<Taxon> tList = query.list();
			return new Long(0);
		}

		return new Long(0);

	}
}
