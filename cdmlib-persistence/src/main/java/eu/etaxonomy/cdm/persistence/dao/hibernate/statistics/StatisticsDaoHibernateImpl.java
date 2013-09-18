package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
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

	private static final int REFERENCE_LINK_RECURSION_DEPTH = 1;

	private static final Logger logger = Logger
			.getLogger(StatisticsDaoHibernateImpl.class);

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

		return Long.valueOf(processQueriesWithIdDistinctListResult(
				queryStrings, null).size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countDescriptiveSourceReferences
	 * (eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	@Override
	public Long countDescriptive(Boolean sourceRef, Classification classification) {
		return Long.valueOf(listDescriptiveIds(sourceRef,classification).size());
	}

	// private Set<Integer> listDescriptiveSourceReferenceIds(
	// Classification classification) {
	//
	// if (classification == null)
	// return null; // or MAYDO: throw some Exception???
	//
	// Map<String, Object> parameters = new HashMap<String, Object>();
	//
	// List<String> queryStrings = new ArrayList<String>();
	//
	// // // Taxon description elements:
	// queryStrings
	// .add("select distinct des.citation.id from TaxonNode as tn "
	// + "join tn.taxon.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and des.citation is not null ");
	//
	// parameters.put("classification", classification);
	//
	// // TaxonNameBase description elements for taxa:
	// queryStrings.add("select distinct des.citation.id from TaxonNode tn "
	// + "join tn.taxon.name.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and tn.taxon is not null "
	// + "and tn.taxon.name is not null "
	// + "and des.citation is not null ");
	//
	// // TaxonNameBase description elements for synonyms:
	// queryStrings.add("select distinct des.citation.id from TaxonNode tn "
	// + "join tn.taxon.synonymRelations as syr "
	// + "join syr.relatedFrom as sy "
	// + "join sy.name.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and des.citation is not null " + "and sy is not null " // TODO:
	// // is
	// // this
	// // case
	// // actually
	// // possible???
	// + "and sy.name is not null ");
	//
	// // SpecimenOrObservationBase description elements:
	// // 1. via determinations
	// queryStrings
	// .add("select distinct des.citation.id from DescriptionBase db, TaxonNode tn "
	// + "join db.describedSpecimenOrObservation as so "
	// + "join so.determinations as det "
	// + "join db.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and tn.taxon=det.taxon ");
	//
	// // 2. via derived units in taxon description
	// // already done with the taxon/synonym descriptions
	//
	// // 3. via SpecimenTypeDesignation in TaxonName:
	// // a. taxon names:
	// queryStrings.add("select distinct des.citation.id from TaxonNode tn "
	// + " join tn.taxon.name.typeDesignations as tdes "
	// + "join tdes.typeSpecimen.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and tdes.class=:type " + "and tn.taxon is not null "
	// + "and tn.taxon.name is not null "
	// + "and des.citation is not null ");
	//
	// parameters.put("type", "SpecimenTypeDesignation");
	//
	// // b. synonym names:
	// queryStrings.add("select distinct des.citation.id from TaxonNode tn "
	//
	// + "join tn.taxon.synonymRelations as syr "
	// + "join syr.relatedFrom as sy "
	// + " join sy.name.typeDesignations as tdes "
	// + "join tdes.typeSpecimen.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and tdes.class=:type " + "and tn.taxon is not null "
	// + "and sy.name is not null " + "and des.citation is not null ");
	//
	// // 4. via HomotypicalGroup in TaxonBase
	// // we get this automatically with the names
	//
	// return processQueriesWithIdDistinctListResult(queryStrings, parameters);
	//
	// }

	private Set<Integer> listDescriptiveIds(Boolean sourceReferences,
			Classification classification) {

//		Boolean sourceReferences = true;
		String sourceRefJoins = "";
		String sourceRefWhere = "";
		String selection = "d.id ";

		if (sourceReferences) {
			sourceRefJoins = "join d.descriptionElements as de "
					+ "join de.sources as des ";
			sourceRefWhere = "and des.citation is not null ";
			selection = "des.citation.id ";
		}

		if (classification == null)
			return null; // or MAYDO: throw some Exception???

		Map<String, Object> parameters = new HashMap<String, Object>();

		List<String> queryStrings = new ArrayList<String>();

		// // Taxon description elements:
		queryStrings.add("select distinct " + selection
				+ "from TaxonNode as tn " + "join tn.taxon.descriptions as d "
				+ sourceRefJoins + "where tn.classification=:classification "
				+ sourceRefWhere);

		parameters.put("classification", classification);

		// TaxonNameBase description elements for taxa:
		queryStrings.add("select distinct " + selection + "from TaxonNode tn "
				+ "join tn.taxon.name.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null " + sourceRefWhere);

		// TaxonNameBase description elements for synonyms:
		queryStrings.add("select distinct " + selection + "from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification " + sourceRefWhere
				+ "and sy is not null " // TODO:
										// is
										// this
										// case
										// actually
										// possible???
				+ "and sy.name is not null ");

		// SpecimenOrObservationBase description elements:
		// 1. via determinations
		queryStrings.add("select distinct " + selection
				+ "from DescriptionBase d, TaxonNode tn "
				+ "join d.describedSpecimenOrObservation as so "
				+ "join so.determinations as det " + sourceRefJoins
				+ "where tn.classification=:classification "
				+ "and tn.taxon=det.taxon " + sourceRefWhere);

		// 2. via derived units in taxon description
		// already done with the taxon/synonym descriptions

		// 3. via SpecimenTypeDesignation in TaxonName:
		// a. taxon names:
		queryStrings.add("select distinct " + selection + "from TaxonNode tn "
				+ " join tn.taxon.name.typeDesignations as tdes "
				+ "join tdes.typeSpecimen.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification "
				+ "and tdes.class=:type " + "and tn.taxon is not null "
				+ "and tn.taxon.name is not null " + sourceRefWhere);

		parameters.put("type", "SpecimenTypeDesignation");

		// b. synonym names:
		queryStrings.add("select distinct " + selection + "from TaxonNode tn "

		+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ " join sy.name.typeDesignations as tdes "
				+ "join tdes.typeSpecimen.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification "
				+ "and tdes.class=:type " + "and tn.taxon is not null "
				+ "and sy.name is not null " + sourceRefWhere);

		// 4. via HomotypicalGroup in TaxonBase
		// we get this automatically with the names

		return processQueriesWithIdDistinctListResult(queryStrings, parameters);

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

			criteria.add(Restrictions.eq("classification", classification));
			criteria.setProjection(Projections.rowCount());
			return Long.valueOf((Long) criteria.uniqueResult());
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

		return Long.valueOf(processQueriesWithIdDistinctListResult(
				queryStrings, parameters).size());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao#
	 * countNomenclaturalReferences()
	 */
//	@Override
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

		return Long.valueOf(processQueriesWithIdDistinctListResult(
				queryStrings, parameters).size());
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

		if (classification == null)
			return null; // or MAYDO: throw some Exception???

		// get all the descriptive source reference ids
		// ---------------------------------------------

		// preparation
		List<String> queryStrings = new ArrayList<String>();
		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("classification", classification);

		// get the ids from the Descriptive source references to add them to the
		// count
		Set<Integer> ids = listDescriptiveIds(true, classification);

		// get classification reference
		queryStrings.add("select c.reference.id from Classification as c "
				+ "where c.id=:classificationId ");
		// TODO ???
		// +"join c.souces as s "
		// +"join s.citation "

		parameters.put("classificationId", classification.getId());

		// get node relations references:
		queryStrings
				.add("select distinct tn.referenceForParentChildRelation.id as c from TaxonNode tn "
						+ "where tn.classification=:classification "
						+ "and tn.referenceForParentChildRelation is not null ");

		// get sec references
		// -------------------------------------------------------------------
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

		// get relationship citations
		// ---------------------------------------------------------------

		// taxon relations
		queryStrings.add("select distinct tr.citation.id from TaxonNode tn "
				+ "join tn.taxon.relationsFromThisTaxon as tr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null " + "and tr.citation is not null ");

		// synonym relations

		queryStrings.add("select distinct sr.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as sr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null " + "and sr.citation is not null ");

		// get hybrid relation citations
		// Taxa:
		queryStrings.add("select distinct hr.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.hybridParentRelations as hr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon.name.class=:nonViralName "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null ");

		parameters.put("nonViralName", "NonViralName");

		// synonyms:
		queryStrings.add("select distinct hr.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.hybridParentRelations as hr "
				+ "where tn.classification=:classification "
				+ "and sy.name.class=:nonViralName " + "and sy is not null " // TODO:
																				// is
																				// this
																				// case
																				// actually
																				// possible???
				+ "and sy.name is not null ");

		// get name relations references:
		// -------------------------------------------------------
		// Taxa:
		queryStrings.add("select distinct nr.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.relationsFromThisName as nr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null ");

		// synonyms:
		queryStrings.add("select distinct nr.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy "
				+ "join sy.name.relationsFromThisName as nr "
				+ "where tn.classification=:classification "
				+ "and sy is not null " // TODO: is this case actually
										// possible???
				+ "and sy.name is not null ");

		// get Nomenclatural status citation

		// Taxa:
		queryStrings.add("select distinct s.citation.id from TaxonNode tn "
				+ "join tn.taxon.name.status as s "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null ");

		// synonyms:
		queryStrings.add("select distinct s.citation.id from TaxonNode tn "
				+ "join tn.taxon.synonymRelations as syr "
				+ "join syr.relatedFrom as sy " + "join sy.name.status as s "
				+ "where tn.classification=:classification "
				+ "and sy is not null " // TODO: is this case actually
										// possible???
				+ "and sy.name is not null ");

		// get sequences which contain citations and publishedIn ------
		// and contain "Media" which could be of the subtype
		// "ReferencedMediaBase"
		// which has a citation

		queryStrings
				.add("select distinct cit.id " +
						" from TaxonNode tn "
						+ "join tn.taxon.descriptions as db "

						+ "join db.describedSpecimenOrObservation as so "
						+ "join so.sequences as seq "
						+ "join seq.citations as cit "

						+ "where so.class=:dnaSample "
						+ "and tn.classification=:classification "
						+ "and cit is not null ");

		// traverse to specimenOrObservation via individualsAssociation

		queryStrings
				.add("select distinct cit.id from TaxonNode tn "
						+ "join tn.taxon.descriptions as db "
						+ "join db.descriptionElements as ia "
						+ "join ia.associatedSpecimenOrObservation as so "
						+ "join so.sequences as seq "
						+ "join seq.citations as cit "

						+ "where so.class=:dnaSample "
						+ "and ia.class=:individualsAssociation "
						+ "and tn.classification=:classification "
						+ "and cit is not null ");

		// we do assume, that a name description would not have a
		// SpecimenOrObservation element

		//
		parameters.put("dnaSample", "DnaSample");
		parameters.put("individualsAssociation", "IndividualsAssociation");

// TODO v3.3, preliminary removed for adaptation to model v3.3		
//		// media
//		queryStrings.add("select distinct me.citation.id from TaxonNode tn "
//				+ "join tn.taxon.descriptions as db "
//				+ "join db.describedSpecimenOrObservation as so "
//				+ "join so.sequences as seq "
//				+ "join seq.chromatograms as me "
//
//				+ "where so.class=:dnaSample "
//				+ "and me.class=:referencedMediaBase "
//				+ "and tn.classification=:classification "
//
//				+ "and me.citation is not null ");
//
//		// traverse to specimenOrObservation via individualsAssociation
//
//		queryStrings.add("select distinct me.citation.id from TaxonNode tn "
//				+ "join tn.taxon.descriptions as db "
//				+ "join db.descriptionElements as ia "
//				+ "join ia.associatedSpecimenOrObservation as so "
//				+ "join so.sequences as seq " 
//				+ "join seq.chromatograms as me "
//				+ "where so.class=:dnaSample "
//				+ "and ia.class=:individualsAssociation "
//				+ "and me.class=:referencedMediaBase "
//				+ "and tn.classification=:classification "
//
//				+ "and me.citation is not null ");

// TODO v3.3, preliminary  removed for adaptation to model v3.3, Media.citation does not exist anymore, use OriginalSource instead	
//		// via media via name description
//		// Taxa:
//		queryStrings.add("select distinct me.citation.id from TaxonNode tn "
//				+ "join tn.taxon.name.descriptions as d "
//				+ "join d.descriptionElements as de "
//				+ "join de.media as me "
//				+ "where tn.classification=:classification "
//				+ "and tn.taxon.name is not null "
//				+ "and me.class=:referencedMediaBase "
//				+ "and me.citation is not null " + "and tn.taxon is not null "
//				+ "and tn.taxon.name is not null ");
//
//		// synonyms:
//		queryStrings.add("select distinct me.citation.id from TaxonNode tn "
//				+ "join tn.taxon.synonymRelations as syr "
//				+ "join syr.relatedFrom as sy "
//				+ "join sy.name.descriptions as d "
//				+ "join d.descriptionElements as de "
//				+ "join de.media as me "
//				+ "where tn.classification=:classification "
//				+ "and sy.name is not null "
//				+ "and me.class=:referencedMediaBase "
//				+ "and me.citation is not null " + "and tn.taxon is not null "
//				+ "and tn.taxon.name is not null ");
//
//		// get all "Media" from everywhere because it could be
//		// of the subtype "ReferencedMediaBase"
//		// which has a citation
//
//		// TODO do we need the media from DefinedTermBase???
//		// what can be a Feature!
//
//		// from description element
//		queryStrings.add("select distinct me.citation.id from TaxonNode as tn "
//				+ "join tn.taxon.descriptions as d "
//				+ "join d.descriptionElements as de " 
//				+ "join de.media as me "
//				+ "where tn.classification=:classification "
//				+ "and me.class=:referencedMediaBase "
//				+ "and me.citation is not null ");
//
//		// via NamedArea that has 2 media parameter
//		// and a waterbodyOrContinet that has media parameter and has continent
//		// parameter
//		// which also has media parameter:
//
//		// from CommonTaxonName or Distribution
//		queryStrings
//				.add("select distinct de.area.shape.citation.id, me1.citation.id, "
//						+ "me2.citation.id, me3.citation.id from TaxonNode as tn "
//						+ "join tn.taxon.descriptions as d "
//						+ "join d.descriptionElements as de "
//						+ "join de.area.media as me1 "
//						+ "join de.area.waterbodiesOrCountries as wboc "
//						+ "join wboc.media as me2 "
//						+ "join wboc.continents as co "
//						+ "join co.media as me3 "
//						+ "where tn.classification=:classification "
//						+ "and (de.class=:commonTaxonName or de.class=:distribution) "
//						+ "and me1.class=:referencedMediaBase "
//						+ "and me1.citation is not null "
//						+ "and me2.class=:referencedMediaBase "
//						+ "and me2.citation is not null "
//						+ "and me3.class=:referencedMediaBase "
//						+ "and me3.citation is not null "
//						+ "and de.area.shape.class=:referencedMediaBase "
//						+ "and de.area is not null "
//						+ "and de.area.shape is not null ");
//
//		parameters.put("commonTaxonName", "CommonTaxonName");
//		parameters.put("distribution", "Distribution");
//
//		// from TaxonDescription:
//		queryStrings
//				.add("select distinct na.shape.citation.id, me1.citation.id, "
//						+ "me2.citation.id, me3.citation.id from TaxonNode as tn "
//						+ "join tn.taxon.descriptions as d "
//						+ "join d.geoScopes as na " + "join na.media as me1 "
//						+ "join na.waterbodiesOrCountries as wboc "
//						+ "join wboc.media as me2 "
//						+ "join wboc.continents as co "
//						+ "join co.media as me3 "
//						+ "where tn.classification=:classification "
//						+ "and me1.class=:referencedMediaBase "
//						+ "and me1.citation is not null "
//						+ "and me2.class=:referencedMediaBase "
//						+ "and me2.citation is not null "
//						+ "and me3.class=:referencedMediaBase "
//						+ "and me3.citation is not null "
//						+ "and na.shape.class=:referencedMediaBase "
//						+ "and na.shape is not null ");
//
//		// from gathering event
//		queryStrings
//				.add("select fo.gatheringEvent.country.shape.citation.id, ca.shape.citation.id " +
//						" from TaxonNode tn "
//						+ "join tn.taxon.descriptions as db "
//						+ "join db.describedSpecimenOrObservation as fo "
//						+ "join fo.gatheringEvent.collectingAreas as ca "
//						+ "where fo.class=:fieldObservation "
//						+ "and fo.gatheringEvent is not null "
//						+ "and fo.gatheringEvent.country is not null "
//						+ "and fo.gatheringEvent.country.shape is not null "
//						+ "and ca.shape is not null "
//						+ "and ca.shape.class=:referencedMediaBase "
//						+ "and ca.shape.citation is not null "
//						+ "and fo.gatheringEvent.country.shape.class=:referencedMediaBase "
//						+ " and fo.gatheringEvent.country.shape.citation is not null "
//						+ "and tn.classification=:classification ");
//
//		// traverse to specimenOrObservation via individualsAssociation
//
//		queryStrings
//				.add("select fo.gatheringEvent.country.shape.citation.id, ca.shape.citation.id "
//						+ "from TaxonNode tn "
//						+ "join tn.taxon.descriptions as db "
//						+ "join db.descriptionElements as ia "
//						+ "join ia.associatedSpecimenOrObservation as fo "
//						+ "join fo.gatheringEvent.collectingAreas as ca "
//						+ "where fo.class=:fieldObservation "
//						+ "and fo.gatheringEvent is not null "
//						+ "and fo.gatheringEvent.country is not null "
//						+ "and fo.gatheringEvent.country.shape is not null "
//						+ "and ca.shape is not null "
//						+ "and ca.shape.class=:referencedMediaBase "
//						+ "and ca.shape.citation is not null "
//						+ "and fo.gatheringEvent.country.shape.class=:referencedMediaBase "
//						+ " and fo.gatheringEvent.country.shape.citation is not null "
//						+ "and ia.class=:individualsAssociation "
//						+ "and tn.classification=:classification ");

		parameters.put("fieldObservation", "FieldObservation");
		parameters.put("referencedMediaBase", "ReferencedMediaBase");

		parameters.put("classification", classification);

		// via events
		// ----------------------------------------
		// determination event:
		// taxa
		queryStrings
				.add("select distinct sor.id from DeterminationEvent dtev, TaxonNode tn "
						+ "join dtev.setOfReferences as sor "

						+ "where tn.classification=:classification "
						+ "and tn.taxon=dtev.taxon ");

		// synonyms

		queryStrings
				.add("select distinct sor.id from DeterminationEvent dtev, TaxonNode tn "
						+ "join dtev.setOfReferences as sor "
						+ "join tn.taxon.synonymRelations as syr "
						+ "join syr.relatedTo as sy "
						+ "where tn.classification=:classification "
						+ "and sy=dtev.taxon ");

		// TODO get all objects that inherit IdentifiableEntity because it has
		// an
		// IdentifiableSource which inherits from OriginalSourceBase
		// which inherits from ReferencedEntityBase
		// which has a citation

		ids.addAll(processQueriesWithIdDistinctListResult(queryStrings,	parameters));

		// TODO get sources of all references from ids and add the references of
		// the sources...
		// iterate in a certain depth REFERENCE_LINK_RECURSION_DEPTH

		// for (int i = 0; i < REFERENCE_LINK_RECURSION_DEPTH; i++) {
		//
		// ids.remove(null);
		// List<Integer> preIds = new ArrayList(ids);
		//
		// if (!preIds.isEmpty()) {
		//
		// queryStrings.clear();
		// Set<Integer> postIds = new HashSet<Integer>();
		//
		// queryStrings
		// .add("select distinct s.citation.id from Reference as R "
		// + "join R.sources as s "
		// + "where s.citation is not null "
		// // + "and s.citation.id is not null "
		// + " and s.citation.id in (:preIds) ");
		//
		// parameters.clear();
		// parameters.put("preIds", preIds);
		// Set<Integer> result = processQueriesWithIdDistinctListResult(
		// queryStrings, parameters);
		// postIds.addAll(result);
		//
		// ids.addAll(postIds);
		// }
		//
		// }

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

		// MAYDO catch error if queries deliver wrong type
		Query query;
		Set<Integer> ids = new HashSet<Integer>();

		for (String queryString : queryStrings) {

			query = getSession().createQuery(queryString);

			List<String> queryParameters = new ArrayList<String>(
					Arrays.asList(query.getNamedParameters()));

			if (parameters != null) {
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {

					if (queryParameters.contains(entry.getKey())) {
						query.setParameter(entry.getKey(), entry.getValue());
					}
				}
			}
			// if ((classification != null) &&
			// (queryParameters.contains("classification"))) {
			// query.setParameter("classification", classification);
			// }
			//
			// if ((type != null) && (queryParameters.contains("type"))) {
			// query.setParameter("type", type);
			// }
			List queryList = query.list();
			// System.out.println(queryList.toString());
			ids.addAll((ArrayList<Integer>) queryList);

			// System.out.println(parameters.toString());
			// System.out.println("");
		}
		// System.out.println("end");
		return ids;
		// return Long.valueOf(ids.size());
	}

	@Override
	public List<UUID> getTaxonTree(IdentifiableEntity filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UUID> getAllChildNodeIds(UUID rootUuid) {

		Set<UUID> uuids = new HashSet<UUID>();
		List<UUID> children = new ArrayList<UUID>();
		List<UUID> parents = new ArrayList<UUID>();
		String queryString;
		String parameter;

		// it should be this one!
		// queryString="select distinct chn.uuid from TaxonNode tn " +
		// "join tn.childNodes as chn " +
		// "where tn.uuid in (:parents) ";

		// just for testing, but does not work anyway
		queryString = "select distinct chn.uuid from TaxonNode tn "
				+ "join tn.childNodes as chn " + "where tn.uuid = :parent ";

		Query query = getSession().createQuery(queryString);

		parents.add(rootUuid);
		uuids.add(rootUuid);

		// while(!(parents.isEmpty())){
		// query.setParameterList("parents",parents);
		query.setParameter("parent", parents.get(0));
		children = query.list();
		uuids.addAll(children);
		parents = children;
		// }
		List<UUID> uuidList = new ArrayList<UUID>();
		uuidList.addAll(uuids);
		return uuidList;

	}

	// @Override
	// public List<UUID> getAllTaxonIds(UUID rootUuid){
	//
	// Set<UUID> uuids= new HashSet<UUID>();
	// List<UUID> children= new ArrayList<UUID>();
	// List<UUID> parents= new ArrayList<UUID>();
	// String queryString;
	// String parameter;
	//
	// queryString="select distinct chn.taxon.uuid from TaxonNode tn " +
	// "join tn.childNodes as chn " +
	// "where tn.taxon.uuid in :parents ";
	//
	// Query query= getSession().createQuery(queryString);
	//
	// parents.add(rootUuid);
	//
	// //while(!(parents.isEmpty())){
	// parents.add(UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9"));
	// parameter=parents.toString();
	// System.out.println("parameter: "+parameter);
	//
	// //children = query.list();
	// // parents=children
	// //}
	//
	// return parents;
	//
	// }

	@Override
	public void getAllTaxonIds() {

		Set<UUID> uuids = new HashSet<UUID>();

		// return (List<UUID>) uuids;

	}

}
