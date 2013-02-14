package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
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

	// TODO remove every commented query related to
	// DescriptionBase.descriptionSources

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countDescriptiveSourceReferences()
	 */
	
	private static final int REFERENCE_LINK_RECURSION_DEPTH=1;
	
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
		// queryStrings.add("select distinct r.id from DescriptionBase as d "
		// + "join d.descriptionSources as r ");

		// count sources from DescriptionElements:
		queryStrings
				.add("select distinct s.citation.id from DescriptionElementBase as d "
						+ "join d.sources as s where s.citation is not null ");


		return Long.valueOf(processQueriesWithIdDistinctListResult(queryStrings,
				null).size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countDescriptiveSourceReferences
	 * (eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	@Override
	public Long countDescriptiveSourceReferences(Classification classification){
		return Long.valueOf(listDescriptiveSourceReferenceIds(classification).size());
	}

	private Set<Integer> listDescriptiveSourceReferenceIds(Classification classification) {

		if (classification == null) return null; // or MAYDO: throw some Exception???

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		List<String> queryStrings = new ArrayList<String>();

//		// Taxon description elements:
		queryStrings
				.add("select distinct des.citation.id from TaxonNode as tn "
						+ "join tn.taxon.descriptions as d "
						+ "join d.descriptionElements as de "
						+ "join de.sources as des "
						+ "where tn.classification=:classification "
						+ "and des.citation is not null " 
						);

		parameters.put("classification", classification);

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
				+ "and des.citation is not null " + "and sy is not null " // TODO:
																			// is
																			// this
																			// case
																			// actually
																			// possible???
				+ "and sy.name is not null ");



		// SpecimenOrObservationBase description elements:
		// 1. via determinations
		queryStrings
				.add("select distinct des.citation.id from DescriptionBase db, TaxonNode tn "
						+ "join db.describedSpecimenOrObservations as so "
						+ "join so.determinations as det "
						+ "join db.descriptionElements as de "
						+ "join de.sources as des "
						+ "where tn.classification=:classification "
						+ "and tn.taxon=det.taxon ");

		// 2. via derived units in taxon description 
		// already done with the taxon/synonym descriptions 

		
		// 3. via SpecimenTypeDesignation in TaxonName:
		// a. taxon names:
		queryStrings.add("select distinct des.citation.id from TaxonNode tn "
				+ " join tn.taxon.name.typeDesignations as tdes "
				+ "join tdes.typeSpecimen.descriptions as d "
				+ "join d.descriptionElements as de "
				+ "join de.sources as des "
				+ "where tn.classification=:classification "
				+ "and tdes.class=:type " + "and tn.taxon is not null "
				+ "and tn.taxon.name is not null "
				+ "and des.citation is not null ");
		
		parameters.put("type", "SpecimenTypeDesignation");
		
		// b. synonym names:
		queryStrings.add("select distinct des.citation.id from TaxonNode tn "

				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ " join sy.name.typeDesignations as tdes "
				+ "join tdes.typeSpecimen.descriptions as d "
				+ "join d.descriptionElements as de "
				+ "join de.sources as des "
				+ "where tn.classification=:classification "
				+ "and tdes.class=:type " + "and tn.taxon is not null "
				+ "and sy.name is not null "
				+ "and des.citation is not null ");

		// 4. via HomotypicalGroup in TaxonBase
		// we get this automatically with the names

		return processQueriesWithIdDistinctListResult(queryStrings,
				parameters );

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

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("classification", classification);
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
		queryStrings
				.add("select distinct tn.taxon.name.id as c from TaxonNode tn "
						+ "where tn.classification=:classification "
						+ "and tn.taxon.name is not null ");
		queryStrings
				.add("select distinct sr.relatedFrom.name.id as c from TaxonNode tn "
						+ "join tn.taxon.synonymRelations sr "
						+ "where tn.classification=:classification "
						+ "and sr.relatedFrom.name is not null ");


		return Long.valueOf(processQueriesWithIdDistinctListResult(queryStrings,
				parameters).size());
		
		
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

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("classification", classification);
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

		return Long.valueOf(processQueriesWithIdDistinctListResult(queryStrings,
				parameters).size());
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
		
		if (classification == null) return null; // or MAYDO: throw some Exception???

		List<String> queryStrings = new ArrayList<String>();
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("classification", classification);

		
		// get descriptive source reference ids ---------------------------------------------
		Set<Integer> ids = listDescriptiveSourceReferenceIds(classification);
		
		
		// get classification reference 
		queryStrings.add("select c.id from Classification as c "
				+"where c.id=:classificationId "
//				+"join c.souces as s "
//				+"join s.citation "
				);
		
		int i = classification.getId();
		parameters.put("classificationId", classification.getId());
		// get sec references -------------------------------------------------------------------
		
		// taxa
		queryStrings
		.add("select distinct tn.taxon.sec.id as c from TaxonNode tn "
				+ "where tn.classification=:classification "
				+ "and tn.taxon.sec is not null ");
		
		// synonyms
		queryStrings
		.add("select distinct sr.relatedFrom.sec.id as c from TaxonNode tn "
				+ "join tn.taxon.synonymRelations sr "
				+ "where tn.classification=:classification "
				+ "and sr.relatedFrom.sec is not null ");

		// get relationship citations ---------------------------------------------------------------

		// taxon relations
		queryStrings.add("select distinct tr.citation.id from TaxonNode tn "
				+ "join tn.taxon.relationsFromThisTaxon as tr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tr.citation is not null "
				);
		
		// synonym relations
		
		queryStrings.add("select distinct sr.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as sr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and sr.citation is not null "
				);
		
		//get hybrid relation citations
		// Taxa:
		queryStrings.add("select distinct hr.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.hybridParentRelations as hr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon.name.class=:nonViralName "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null "
				);

		parameters.put("nonViralName", "NonViralName");
		
		// synonyms:
		queryStrings.add("select distinct hr.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.hybridParentRelations as hr "
				+ "where tn.classification=:classification "
				+ "and sy.name.class=:nonViralName "
				+ "and sy is not null " // TODO: is this case actually possible???
				+ "and sy.name is not null ");

		// get name relations references:
		
		// Taxa:
		queryStrings.add("select distinct nr.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.relationsFromThisName as nr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null "
				);

		// synonyms:
		queryStrings.add("select distinct nr.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.relationsFromThisName as nr "
				+ "where tn.classification=:classification "
				+ "and sy is not null " // TODO: is this case actually possible???
				+ "and sy.name is not null ");
		
		// get Nomenclatural status citation
		
		// Taxa:
		queryStrings.add("select distinct s.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.status as s "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null "
				);

		// synonyms:
		queryStrings.add("select distinct s.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.status as s "
				+ "where tn.classification=:classification "
				+ "and sy is not null " // TODO: is this case actually possible???
				+ "and sy.name is not null ");
		
		
		// TODO get sequences which contain citations and publishedIn ------
		// and contain "Media" which could be of the subtype "ReferencedMediaBase"
		//	 which has a citation
		
		// taxa
		queryStrings
		.add("select distinct cit.id, seq.publishedIn.id from TaxonNode tn "
				+ "join tn.taxon.descriptions as db "

				+ "join db.describedSpecimenOrObservations as so "
				+ "join so.sequences as seq "
				+ "join seq.citations as cit "
				
				+ "where so.class=:dnaSample "
				+ "and tn.classification=:classification "
				+ "and cit is not null "
				+ "and seq.publishedIn is not null "
				);
		parameters.put("dnaSample", "DnaSample");
		
		// synonyms
		
		// media
		// taxa
		// synonyms
		
		
		// TODO get all "Media" from everywhere because it could be 
		// of the subtype "ReferencedMediaBase"
		//	 which has a citation
		
		// TODO get all objects that inherit IdentifiableEntity because it has an
		// IdentifiableSource which inherits from OriginalSourceBase 
		// which inherits from ReferencedEntityBase 
		// which has a citation
		
		ids.addAll(processQueriesWithIdDistinctListResult(queryStrings,
				parameters));
		
		//TODO get sources of all references from ids and add the references of the sources...
		// iterate in a certain depth REFERENCE_LINK_RECURSION_DEPTH
		
		return Long.valueOf(ids.size());
	}


	/**
	 * @param queryStrings
	 *            - should be strings that represent a hibernate query, that
	 *            result in a list of ids (Integer)
	 * @param classification
	 *            - to which the elements with the listed ids are attached to
	 * @param type
	 * @return - the distinct amount of ids, the queries result
	 */
	private Set<Integer> processQueriesWithIdDistinctListResult(
			List<String> queryStrings, Map<String, Object> parameters) {

		// MAYDO catch error if qeries deliver wrong type
		Query query;
		Set<Integer> ids = new HashSet<Integer>();

		for (String queryString : queryStrings) {
			
			query = getSession().createQuery(queryString);
			
			List<String> queryParameters=new ArrayList<String>(Arrays.asList(query.getNamedParameters()));
				
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				
				if(parameters!=null && queryParameters.contains(entry.getKey())){
					query.setParameter(entry.getKey(), entry.getValue());
				}
			}
//			if ((classification != null) && (queryParameters.contains("classification"))) {
//				query.setParameter("classification", classification);
//			}
//
//			if ((type != null) && (queryParameters.contains("type"))) {
//				query.setParameter("type", type);
//			}
			ids.addAll((ArrayList<Integer>) query.list());
		}

		return ids;
//		return Long.valueOf(ids.size());
	}

}
