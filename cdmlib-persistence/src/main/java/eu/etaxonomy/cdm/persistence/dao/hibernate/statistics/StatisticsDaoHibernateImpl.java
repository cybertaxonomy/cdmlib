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

/**
 * this dao provides counting methods for elements in a database in general or
 * in a specific class (or tree - TODO) in the database.
 * 
 * only functionality, that is not covered by other daos is implemented
 * 
 * MAYDO: restructure and using {@link Criteria} and methods like prepareQuery
 * 
 * @author s.buers
 * 
 */

@Repository
public class StatisticsDaoHibernateImpl extends DaoBase implements
		IStatisticsDao {
	
	// TODO remove every commented query related to DescriptionBase.descriptionSources
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countDescriptiveSourceReferences()
	 */
	@Override
	public Long countDescriptiveSourceReferences() {

		List<String> queryStrings = new ArrayList<String>();

		// this query does not work...

		// query = getSession().createQuery(
		// "select count(distinct(r.id, desc.id)) from DescriptionBase as d "
		// + "join d.descriptionElements as de "
		// + "join de.sources as des "
		// + "join des.citation as desc "
		// + "join d.descriptionSources as r "
		// + "where "
		// + "desc is not null"
		// + " and "
		// + "r is not null ");

		// ... here is the manual version:

		// count sources from Descriptions:
		// as the descriptionSources of DescriptionBase are depricated:
//		queryStrings.add("select distinct r.id from DescriptionBase as d "
//				+ "join d.descriptionSources as r ");

		// count sources from DescriptionElements:
		queryStrings
				.add("select distinct s.citation.id from DescriptionElementBase as d "
						+ "join d.sources as s where s.citation is not null ");

		return processQueriesWithIdListResultAndCountDistinct(queryStrings,
				null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countDescriptiveSourceReferences
	 * (eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	@Override
	public Long countDescriptiveSourceReferences(Classification classification) {
		if (classification == null)

			// TODO add count of Name descriptions:
			// what about SpecimenOrObservations???

			return null; // or MAYDO: throw some Exception???

		List<String> queryStrings = new ArrayList<String>();

		// count Taxon descriptions
		// as the descriptionSources of DescriptionBase are depricated:
//		queryStrings.add("select distinct r.id from TaxonNode as tn "
//				+ "join tn.taxon.descriptions as d "
//				+ "join d.descriptionSources as r "
//				+ "where tn.classification=:classification "

//				);
		//

		// Taxon description elements:
		queryStrings
				.add("select distinct des.citation.id from TaxonNode as tn "
						+ "join tn.taxon.descriptions as d "
						+ "join d.descriptionElements as de "
						+ "join de.sources as des "
						+ "where tn.classification=:classification "
						+ "and des.citation is not null ");

		// TaxonNameBase descriptions for taxa:
		// as the descriptionSources of DescriptionBase are depricated:
//		queryStrings.add("select distinct r.id from TaxonNode tn "
//				+ "join tn.taxon.name.descriptions as d "
//				+ "join d.descriptionSources as r "
//				+ "where tn.classification=:classification ");

		// TaxonNameBase description elements for taxa:
		queryStrings.add("select distinct des.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.descriptions as d "
				+ "join d.descriptionElements as de "
				+ "join de.sources as des "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null "				
				+ "and des.citation is not null ");
		
		// TaxonNameBase description elements for synonyms:
		queryStrings.add("select distinct des.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.descriptions as d "
				+ "join d.descriptionElements as de "
				+ "join de.sources as des "
				+ "where tn.classification=:classification "
				+ "and des.citation is not null "
				+ "and sy is not null " // TODO: is this case actually possible???
				+ "and sy.name is not null ");


		// SpecimenOrObservationBase descriptions:
		// as the descriptionSources of DescriptionBase are depricated:
//		queryStrings.add("select r.id from DescriptionBase db, TaxonNode tn "
//				+ "join db.describedSpecimenOrObservations as soo "
//				+ "join soo.determinations as det "
//				+ "join db.descriptionSources as r "
//				+ "where tn.classification=:classification "
//				+ "and tn.taxon=det.taxon");

		// SpecimenOrObservationBase description elements:
		queryStrings
				.add("select des.citation.id from DescriptionBase db, TaxonNode tn "
						+ "join db.describedSpecimenOrObservations as so "
						+ "join so.determinations as det "
						+ "join db.descriptionElements as de "
						+ "join de.sources as des "
						+ "where tn.classification=:classification "
						+ "and tn.taxon=det.taxon ");

		// TODO deal with WorkingSet!!!
		
		return processQueriesWithIdListResultAndCountDistinct(queryStrings,
				classification);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countTaxaInClassification(java.lang.Class,
	 * eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	@Override
	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification) {
		if (classification == null)
			return null; // or MAYDO: throw some Exception???

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
							+ "where tn.classification=:classification ");
			query.setParameter("classification", classification);

			return (Long) query.uniqueResult();
		}
		// this should never happen:
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#countTaxonNames
	 * (eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	@Override
	public Long countTaxonNames(Classification classification) {

		if (classification == null)
			return null; // or MAYDO: throw some Exception???
		
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

		// so instead of "UNION" we use 2 queries
		// and count the names manually
		List<String> queryStrings = new ArrayList<String>();
		queryStrings.add("select distinct tn.taxon.name.id as c from TaxonNode tn "
				+ "where tn.classification=:classification "
				+ "and tn.taxon.name is not null ");
		queryStrings.add("select distinct sr.relatedFrom.name.id as c from TaxonNode tn "
				+ "join tn.taxon.synonymRelations sr "
				+ "where tn.classification=:classification "
				+ "and sr.relatedFrom.name is not null ");

		return processQueriesWithIdListResultAndCountDistinct(queryStrings,
				classification);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countNomenclaturalReferences()
	 */
	@Override
	public Long countNomenclaturalReferences() {
		Query query = getSession()
				.createQuery(
						"select count(distinct nomenclaturalReference) from TaxonNameBase ");
		return (Long) query.uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countNomenclaturalReferences(eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	@Override
	public Long countNomenclaturalReferences(Classification classification) {

		if (classification == null)
			return null; // or MAYDO: throw some Exception???

		// so instead of "UNION" we use 2 queries
		// and count the names manually
		List<String> queryStrings = new ArrayList<String>();
		queryStrings
				.add("select distinct tn.taxon.name.nomenclaturalReference.id from TaxonNode tn "
						+ "where tn.classification=:classification "
						+ "and tn.taxon.name.nomenclaturalReference is not null ");
		queryStrings
				.add("select distinct sr.relatedFrom.name.nomenclaturalReference.id as c from TaxonNode tn "
						+ "join tn.taxon.synonymRelations as sr "
						+ "where tn.classification=:classification "
						+ "and sr.relatedFrom.name.nomenclaturalReference is not null ");

		return processQueriesWithIdListResultAndCountDistinct(queryStrings,
				classification);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countReferencesInClassification
	 * (eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	// TODO!!!
	@Override
	public Long countReferencesInClassification(Classification classification) {
		if (classification == null)
			return null; // or MAYDO: throw some Exception???
		// TODO implement this count
		return null;
	}

	/**
	 * @param queryStrings
	 *            - should be strings that represent a hibernate query, that
	 *            result in a list of ids (Integer)
	 * @param classification
	 *            - to which the elements with the listed ids are attached to
	 * @return - the distinct amount of ids, the queries result
	 */
	private Long processQueriesWithIdListResultAndCountDistinct(
			List<String> queryStrings, Classification classification) {

		// MAYDO catch error if qeries deliver wrong type
		Query query;
		Set<Integer> ids = new HashSet<Integer>();

		for (String queryString : queryStrings) {

			query = getSession().createQuery(queryString);
			if (classification != null) {
				query.setParameter("classification", classification);
			}
			ids.addAll((ArrayList<Integer>) query.list());
		}

		return Long.valueOf(ids.size());
	}

}
