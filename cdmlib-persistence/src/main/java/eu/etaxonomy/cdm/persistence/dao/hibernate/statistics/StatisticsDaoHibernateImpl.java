package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Query query = getSession()
				.createQuery(
						"select count(distinct nomenclaturalReference) from TaxonNameBase");
		return (Long) query.uniqueResult();
	}

	/**
	 * @return be aware that the returned long might be null
	 */

	public Long countDescriptiveSourceReferences() {
		Query query;
		Long count = new Long(0);

		// count sources from Descriptions:
		query = getSession().createQuery(
				"select count(distinct r.uuid) from DescriptionBase as d "
						+ "join d.descriptionSources as r");
		count += (Long) query.uniqueResult();

		// count sources from DescriptionElements:
		query = getSession().createQuery(
				"select count(distinct s.citation) from DescriptionElementBase as d "
						+ "join d.sources as s where s.citation is not null");
		count += (Long) query.uniqueResult();

		return count;
	}

	@Override
	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification) {

		if (clazz.equals(TaxonBase.class)) {

			return countTaxaInClassification(Taxon.class, classification)
					+ countTaxaInClassification(Synonym.class, classification);
		}

		if (clazz.equals(Taxon.class)) {
			Criteria criteria = getSession().createCriteria(TaxonNode.class);
			criteria = getSession().createCriteria(TaxonNode.class);
			criteria.add(Restrictions.eq("classification", classification));
			criteria.setProjection(Projections.rowCount());
			return Long.valueOf((Integer) criteria.uniqueResult());
		}

		else if (clazz.equals(Synonym.class)) {
			// criteria= getSession().createCriteria(TaxonNode.class);

			Query query = getSession().createQuery(
					"select count(distinct sr.relatedFrom.uuid) from TaxonNode tn "
							+ "join tn.taxon.synonymRelations as sr "
							+ "where tn.classification=:classification");
			query.setParameter("classification", classification);

			return (Long) query.uniqueResult();
		}
		// this should never happen:
		return null;

	}

	@Override
	public Long countTaxonNames(Classification classification) {

		// the query would be:
		// "select count (distinct n) from (
		// + "select distinct tn.taxon.name as c from TaxonNode tn "
		// + "where tn.classification=:classification "
		// + "UNION "
		// + "select distinct sr.relatedFrom.name as c from TaxonNode tn "
		// + "join tn.taxon.synonymRelations sr "
		// + "where tn.classification=:classification "
		// ") as n "

		// as hibernate does not accept brackets in from and no unions
		// we have to do it otherwise:

		Query query;
		Set<Integer> nameIds = new HashSet<Integer>();

		// so instead of "UNION" we use 2 queries
		// and count the names manually
		List<String> queries = new ArrayList<String>();
		queries.add("select distinct tn.taxon.name.id as c from TaxonNode tn "
				+ "where tn.classification=:classification ");
		queries.add("select distinct sr.relatedFrom.name.id as c from TaxonNode tn "
				+ "join tn.taxon.synonymRelations sr "
				+ "where tn.classification=:classification");

		for (String queryString : queries) {

			query = getSession().createQuery(queryString);
			query.setParameter("classification", classification);
			nameIds.addAll((ArrayList<Integer>) query.list());
		}

		return Long.valueOf(nameIds.size());
	}

	// @Override
	// public Long countNomenclaturalReferences() {
	// return countNomenclaturalReferences("");
	// }
	//
	// private Long countNomenclaturalReferences(String where) {
	//
	// Query query = getSession().createQuery(
	// "select count(distinct nomenclaturalReference) from TaxonNameBase"
	// + where);
	// return (Long) query.uniqueResult();
	// }
	//
	// @Override
	// public Long countNomenclaturalReferences(Class clazz) {
	// return countNomenclaturalReferences("where" + clazz.getSimpleName());
	// }

}
