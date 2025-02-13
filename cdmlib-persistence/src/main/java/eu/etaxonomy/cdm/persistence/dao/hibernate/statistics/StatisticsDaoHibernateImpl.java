/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
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
public class StatisticsDaoHibernateImpl
        extends DaoBase
        implements IStatisticsDao {

	// TODO remove every commented query related to
	// DescriptionBase.descriptionSources

	private static final int REFERENCE_LINK_RECURSION_DEPTH = 1;

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Override
	public Long countDescriptiveSourceReferences() {

		List<String> queryStrings = new ArrayList<>();

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
				.add("SELECT DISTINCT s.citation.uuid "
				        + "FROM DescriptionElementBase as d "
						+ "  JOIN d.sources as s "
						+ "WHERE s.citation is not null ");

		return Long.valueOf(processQueriesWithIdDistinctListResult(
				queryStrings, null).size());
	}

	@Override
	public Long countDescriptive(Boolean sourceRef,
			Classification classification) {
		return Long.valueOf(listDescriptiveIds(sourceRef, classification)
				.size());
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
	// // TaxonName description elements for taxa:
	// queryStrings.add("select distinct des.citation.id from TaxonNode tn "
	// + "join tn.taxon.name.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and tn.taxon is not null "
	// + "and tn.taxon.name is not null "
	// + "and des.citation is not null ");
	//
	// // TaxonName description elements for synonyms:
	// queryStrings.add("select distinct des.citation.id from TaxonNode tn "
	// + "join tn.taxon.synonyms as sy "
	// + "join sy.name.descriptions as d "
	// + "join d.descriptionElements as de "
	// + "join de.sources as des "
	// + "where tn.classification=:classification "
	// + "and des.citation is not null " + "and sy is not null " // TODO:
	// // is this case actually possible???
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
	// + "join tn.taxon.synonyms as sy "
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

	private Set<UUID> listDescriptiveIds(Boolean sourceReferences,
			Classification classification) {

		// Boolean sourceReferences = true;
		String sourceRefJoins = "";
		String sourceRefWhere = "";
//		String selection = "d.id ";
		String selection = "d.uuid ";

		if (sourceReferences) {
			sourceRefJoins = "JOIN d.descriptionElements as de "
					+ "JOIN de.sources as des ";
			sourceRefWhere = "AND des.citation is not null ";
//			selection = "des.citation.id ";
			selection = "des.citation.uuid ";
		}

		if (classification == null)
         {
            return null; // or MAYDO: throw some Exception???
        }

		Map<String, Object> parameters = new HashMap<String, Object>();

		List<String> queryStrings = new ArrayList<String>();

		// // Taxon description elements:
		queryStrings.add("SELECT DISTINCT " + selection
				+ "FROM TaxonNode as tn "
		        + "  JOIN tn.taxon.descriptions as d "
				+ sourceRefJoins
				+ "WHERE tn.classification=:classification "
				+ sourceRefWhere);

		parameters.put("classification", classification);

		// TaxonName description elements for taxa:
		queryStrings.add("SELECT DISTINCT " + selection +
		          "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.name.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null " + sourceRefWhere);

		// TaxonName description elements for synonyms:
		queryStrings.add("select distinct " + selection + "from TaxonNode tn "
				+ "join tn.taxon.synonyms as sy "
				+ "join sy.name.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification " + sourceRefWhere
				+ "and sy is not null " // TODO:
										// is
										// this
										// case
										// actually
										// possible???
				+ "and sy.name is not null ");

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

		+ "join tn.taxon.synonyms as sy "
				+ " join sy.name.typeDesignations as tdes "
				+ "join tdes.typeSpecimen.descriptions as d " + sourceRefJoins
				+ "where tn.classification=:classification "
				+ "and tdes.class=:type " + "and tn.taxon is not null "
				+ "and sy.name is not null " + sourceRefWhere);

		// 4. via HomotypicalGroup in TaxonBase
		// we get this automatically with the names

		//###TODO
		return processQueriesWithIdDistinctListResult(queryStrings, parameters);
//		return null;
	}

	@Override
	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification) {
		if (classification == null)
         {
            return null; // or MAYDO: throw some Exception???
        }

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

			Query<Long> query = getSession().createQuery(
					"SELECT COUNT(DISTINCT s.uuid) FROM TaxonNode tn "
							+ " JOIN tn.taxon.synonyms as s "
							+ " WHERE tn.classification=:classification ",
							Long.class);
			query.setParameter("classification", classification);
			return query.uniqueResult();
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
         {
            return null; // or MAYDO: throw some Exception???
        }

		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("classification", classification);
		// the query would be:
		// "select count (distinct n) from (
		// + "select distinct tn.taxon.name as c from TaxonNode tn "
		// + "where tn.classification=:classification "
		// + "UNION "
		// + "select distinct s.name as c from TaxonNode tn "
		// + "join tn.taxon.synonyms s "
		// + "where tn.classification=:classification "
		// ") as n "

		// as hibernate does not accept brackets in from and no unions
		// we have to do it otherwise:

		// so instead of "UNION" we use 2 queries
		// and count the names manually
		List<String> queryStrings = new ArrayList<String>();
		queryStrings
				.add("select distinct tn.taxon.name.uuid as c from TaxonNode tn "
						+ "where tn.classification=:classification "
						+ "and tn.taxon.name is not null ");
		queryStrings
				.add("select distinct s.name.uuid as c from TaxonNode tn "
						+ "join tn.taxon.synonyms s "
						+ "where tn.classification=:classification "
						+ "and s.name is not null ");

		return Long.valueOf(processQueriesWithIdDistinctListResult(
				queryStrings, parameters).size());

	}

	@Override
    public Long countNomenclaturalReferences() {
		Query<Long> query = getSession()
				.createQuery(
						" SELECT COUNT(DISTINCT ns.citation) "
						+ " FROM TaxonName n"
						+ " JOIN n.nomenclaturalSource ns ",
						Long.class);
		return query.uniqueResult();
	}

	@Override
	public Long countNomenclaturalReferences(
			Classification classification) {

		if (classification == null)
         {
            return null; // or MAYDO: throw some Exception???
        }

		Map<String, Object> parameters = new HashMap<>();

		parameters.put("classification", classification);
		// so instead of "UNION" we use 2 queries
		// and count the names manually
		List<String> queryStrings = new ArrayList<String>();
		queryStrings
				.add("SELECT DISTINCT tn.taxon.name.nomenclaturalSource.citation.uuid "
				        + " FROM TaxonNode tn "
						+ " WHERE tn.classification=:classification "
						+ " AND tn.taxon.name.nomenclaturalSource.citation IS NOT NULL ");
		queryStrings
				.add("SELECT DISTINCT s.name.nomenclaturalSource.citation.uuid as c "
				        + " FROM TaxonNode tn "
						+ " JOIN tn.taxon.synonyms as s "
						+ " WHERE tn.classification=:classification "
						+ "    AND s.name.nomenclaturalSource.citation is NOT NULL ");

		return Long.valueOf(processQueriesWithIdDistinctListResult(
				queryStrings, parameters).size());
	}

	@Override
	public Long countReferencesInClassificationWithUuids(Classification classification) {
		if (classification == null){
            return null; // or MAYDO: throw some Exception???
        }

		// get all the descriptive source reference ids
		// ---------------------------------------------

		// preparation
		List<String> queryStrings = new ArrayList<>();
		Map<String, Object> parameters = new HashMap<>();

		parameters.put("classification", classification);

		// get the ids from the Descriptive source references to add them to the
		// count

		//TODO
		//Set<Integer> ids = listDescriptiveIds(true, classification);
		Set<UUID> ids = new HashSet<>();

		// get classification reference
		queryStrings.add("SELECT c.source.citation.uuid FROM Classification as c "
				+ "WHERE c.uuid=:classificationId ");
		// TODO ???
		// +"join c.souces as s "
		// +"join s.citation "

		parameters.put("classificationId", classification.getUuid());

		// get node relations references:
		queryStrings
				.add(" SELECT DISTINCT ts.citation.uuid AS c "
				   + " FROM TaxonNode tn "
				   + " LEFT JOIN tn.source ts "
                   + " WHERE tn.classification = :classification "
				   + "     AND ts.citation IS NOT NULL ");

		// get sec references
		// -------------------------------------------------------------------
		// taxa
		queryStrings
				.add("SELECT DISTINCT tn.taxon.secSource.citation.uuid AS c FROM TaxonNode tn "
						+ "WHERE tn.classification=:classification "
						+ "  AND tn.taxon.secSource.citation IS NOT NULL ");

		// synonyms
		queryStrings
				.add("SELECT DISTINCT s.secSource.citation.uuid AS c FROM TaxonNode tn "
						+ "JOIN tn.taxon.synonyms s "
						+ "WHERE tn.classification=:classification "
						+ "AND s.secSource.citation IS NOT NULL ");

		// get relationship citations
		// ---------------------------------------------------------------

		// taxon relations
		queryStrings.add("SELECT DISTINCT tr.source.citation.uuid "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.relationsFromThisTaxon as tr "
				+ "WHERE tn.classification=:classification "
				+ "  AND tn.taxon IS NOT NULL "
				+ "  AND tr.source.citation IS NOT NULL ");


		// get hybrid relation citations
		// Taxa:
		queryStrings.add("SELECT DISTINCT hr.source.citation.uuid "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.name.hybridParentRelations as hr "
				+ "WHERE tn.classification=:classification "
				+ "  AND tn.taxon IS NOT NULL "
				+ "  AND tn.taxon.name IS NOT NULL ");

		// synonyms:
		queryStrings.add("SELECT distinct hr.source.citation.uuid "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.synonyms as sy "
				+ "  JOIN sy.name.hybridParentRelations as hr "
				+ "WHERE tn.classification=:classification "
				+ "  AND sy IS NOT NULL "
				// TODO: is this case actually possible???
				+ "  AND sy.name IS NOT NULL ");

		// get name relations references:
		// -------------------------------------------------------
		// Taxa:
		queryStrings.add("SELECT distinct nr.source.citation.uuid from TaxonNode tn "
				+ "JOIN tn.taxon.name.relationsFromThisName as nr "
				+ "WHERE tn.classification=:classification "
				+ "  AND tn.taxon IS NOT NULL "
				+ "  AND tn.taxon.name IS NOT NULL ");

		// synonyms:
		queryStrings.add("SELECT distinct nr.source.citation.uuid "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.synonyms as sy "
				+ "  JOIN sy.name.relationsFromThisName as nr "
				+ "WHERE tn.classification=:classification "
				+ "  AND sy IS NOT NULL " // TODO: is this case actually possible???
				+ "  AND sy.name IS NOT NULL ");

		// get Nomenclatural status citation

		// Taxa:
		queryStrings.add("SELECT DISTINCT s.source.citation.uuid "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.name.status as s "
				+ "WHERE tn.classification=:classification "
				+ "  AND tn.taxon IS NOT NULL "
				+ "  AND tn.taxon.name IS NOT NULL ");

		// traverse to specimenOrObservation via individualsAssociation

		queryStrings.add("SELECT DISTINCT cit.uuid "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.descriptions as db "
				+ "  JOIN db.descriptionElements as ia "
				+ "  JOIN ia.associatedSpecimenOrObservation as so "
				+ "  JOIN so.sequences as seq "
				+ "  JOIN seq.citations as cit "
				+ "WHERE so.class=:dnaSample "
				+ "  AND ia.class=:individualsAssociation "
				+ "  AND tn.classification=:classification "
				+ "  AND cit IS NOT NULL ");

		// we do assume, that a name description would not have a
		// SpecimenOrObservation element

		parameters.put("dnaSample", "DnaSample");
		parameters.put("individualsAssociation", "IndividualsAssociation");

		//
		//
		// //### TODO v3.3, preliminary removed for adaptation to model v3.3
		// this was all about ReferencedMedia
		{
			// // media
			// queryStrings.add("select distinct me.citation.id from TaxonNode tn "
			// + "join tn.taxon.descriptions as db "
			// + "join db.describedSpecimenOrObservation as so "
			// + "join so.sequences as seq "
			// + "join seq.chromatograms as me "
			//
			// + "where so.class=:dnaSample "
			// + "and me.class=:referencedMediaBase "
			// + "and tn.classification=:classification "
			//
			// + "and me.citation is not null ");
			//
			// // traverse to specimenOrObservation via individualsAssociation
			//
			// queryStrings.add("select distinct me.citation.id from TaxonNode tn "
			// + "join tn.taxon.descriptions as db "
			// + "join db.descriptionElements as ia "
			// + "join ia.associatedSpecimenOrObservation as so "
			// + "join so.sequences as seq "
			// + "join seq.chromatograms as me "
			// + "where so.class=:dnaSample "
			// + "and ia.class=:individualsAssociation "
			// + "and me.class=:referencedMediaBase "
			// + "and tn.classification=:classification "
			//
			// + "and me.citation is not null ");
			//
			// // TODO v3.3, preliminary removed for adaptation to model v3.3,
			// Media.citation does not exist anymore, use OriginalSource instead
			// // via media via name description
			// // Taxa:
			// queryStrings.add("select distinct me.citation.id from TaxonNode tn "
			// + "join tn.taxon.name.descriptions as d "
			// + "join d.descriptionElements as de "
			// + "join de.media as me "
			// + "where tn.classification=:classification "
			// + "and tn.taxon.name is not null "
			// + "and me.class=:referencedMediaBase "
			// + "and me.citation is not null " + "and tn.taxon is not null "
			// + "and tn.taxon.name is not null ");
			//
			// // synonyms:
			// queryStrings.add("select distinct me.citation.id from TaxonNode tn "
			// + "join tn.taxon.synonyms as sy "
			// + "join sy.name.descriptions as d "
			// + "join d.descriptionElements as de "
			// + "join de.media as me "
			// + "where tn.classification=:classification "
			// + "and sy.name is not null "
			// + "and me.class=:referencedMediaBase "
			// + "and me.citation is not null " + "and tn.taxon is not null "
			// + "and tn.taxon.name is not null ");
			//
			// // get all "Media" from everywhere because it could be
			// // of the subtype "ReferencedMediaBase"
			// // which has a citation
			//
			// // TODO do we need the media from DefinedTermBase???
			// // what can be a Feature!
			//
			// // from description element
			// queryStrings.add("select distinct me.citation.id from TaxonNode as tn "
			// + "join tn.taxon.descriptions as d "
			// + "join d.descriptionElements as de "
			// + "join de.media as me "
			// + "where tn.classification=:classification "
			// + "and me.class=:referencedMediaBase "
			// + "and me.citation is not null ");
			//
			// // via NamedArea that has 2 media parameter
			// // and a waterbodyOrContinet that has media parameter and has
			// continent
			// // parameter
			// // which also has media parameter:
			//
			//
			//
			// // from CommonTaxonName or Distribution
			// queryStrings
			// .add("select distinct de.area.shape.citation.id, me1.citation.id, "
			// + "me2.citation.id, me3.citation.id from TaxonNode as tn "
			// + "join tn.taxon.descriptions as d "
			// + "join d.descriptionElements as de "
			// + "join de.area.media as me1 "
			// + "join de.area.waterbodiesOrCountries as wboc "
			// + "join wboc.media as me2 "
			// + "join wboc.continents as co "
			// + "join co.media as me3 "
			// + "where tn.classification=:classification "
			// + "and (de.class=:commonTaxonName or de.class=:distribution) "
			// + "and me1.class=:referencedMediaBase "
			// + "and me1.citation is not null "
			// + "and me2.class=:referencedMediaBase "
			// + "and me2.citation is not null "
			// + "and me3.class=:referencedMediaBase "
			// + "and me3.citation is not null "
			// + "and de.area.shape.class=:referencedMediaBase "
			// + "and de.area is not null "
			// + "and de.area.shape is not null ");
			//
			// parameters.put("commonTaxonName", "CommonTaxonName");
			// parameters.put("distribution", "Distribution");
			// //***
			// // from TaxonDescription:
			// queryStrings
			// .add("select distinct na.shape.citation.id, me1.citation.id, "
			// + "me2.citation.id, me3.citation.id from TaxonNode as tn "
			// + "join tn.taxon.descriptions as d "
			// + "join d.geoScopes as na " + "join na.media as me1 "
			// + "join na.waterbodiesOrCountries as wboc "
			// + "join wboc.media as me2 "
			// + "join wboc.continents as co "
			// + "join co.media as me3 "
			// + "where tn.classification=:classification "
			// + "and me1.class=:referencedMediaBase "
			// + "and me1.citation is not null "
			// + "and me2.class=:referencedMediaBase "
			// + "and me2.citation is not null "
			// + "and me3.class=:referencedMediaBase "
			// + "and me3.citation is not null "
			// + "and na.shape.class=:referencedMediaBase "
			// + "and na.shape is not null ");
			//
			// // from gathering event
			// queryStrings
			// .add("select fo.gatheringEvent.country.shape.citation.id, ca.shape.citation.id "
			// +
			// " from TaxonNode tn "
			// + "join tn.taxon.descriptions as db "
			// + "join db.describedSpecimenOrObservation as fo "
			// + "join fo.gatheringEvent.collectingAreas as ca "
			// + "where fo.class=:fieldObservation "
			// + "and fo.gatheringEvent is not null "
			// + "and fo.gatheringEvent.country is not null "
			// + "and fo.gatheringEvent.country.shape is not null "
			// + "and ca.shape is not null "
			// + "and ca.shape.class=:referencedMediaBase "
			// + "and ca.shape.citation is not null "
			// +
			// "and fo.gatheringEvent.country.shape.class=:referencedMediaBase "
			// + " and fo.gatheringEvent.country.shape.citation is not null "
			// + "and tn.classification=:classification ");
			//
			// // traverse to specimenOrObservation via individualsAssociation
			//
			// queryStrings
			// .add("select fo.gatheringEvent.country.shape.citation.id, ca.shape.citation.id "
			// + "from TaxonNode tn "
			// + "join tn.taxon.descriptions as db "
			// + "join db.descriptionElements as ia "
			// + "join ia.associatedSpecimenOrObservation as fo "
			// + "join fo.gatheringEvent.collectingAreas as ca "
			// + "where fo.class=:fieldObservation "
			// + "and fo.gatheringEvent is not null "
			// + "and fo.gatheringEvent.country is not null "
			// + "and fo.gatheringEvent.country.shape is not null "
			// + "and ca.shape is not null "
			// + "and ca.shape.class=:referencedMediaBase "
			// + "and ca.shape.citation is not null "
			// +
			// "and fo.gatheringEvent.country.shape.class=:referencedMediaBase "
			// + " and fo.gatheringEvent.country.shape.citation is not null "
			// + "and ia.class=:individualsAssociation "
			// + "and tn.classification=:classification ");
			//
			//
			//
			// parameters.put("fieldObservation", "FieldObservation");
			// parameters.put("referencedMediaBase", "ReferencedMediaBase");
			//
			// parameters.put("classification", classification);
			//
			// // via events
			// // ----------------------------------------
			// // determination event:
			// // taxa
			// queryStrings
			// .add("select distinct sor.id from DeterminationEvent dtev, TaxonNode tn "
			// + "join dtev.setOfReferences as sor "
			//
			// + "where tn.classification=:classification "
			// + "and tn.taxon=dtev.taxon ");
			//
			// // synonyms
			//
			// queryStrings
			// .add("select distinct sor.id from DeterminationEvent dtev, TaxonNode tn "
			// + "join dtev.setOfReferences as sor "
			// + "join tn.taxon.synonyms as sy "
			// + "where tn.classification=:classification "
			// + "and sy=dtev.taxon ");
			//
		}

		// ------------------------------------------------------------------
		// TODO get all objects that inherit IdentifiableEntity because it has
		// an
		// IdentifiableSource which inherits from OriginalSourceBase
		// which has a citation
		// furthermore recources can recursivly link to recources:
		// get sources of all references from ids and add the references of
		// the sources...
		// iterate in a certain depth REFERENCE_LINK_RECURSION_DEPTH

		// ----------------------------------------------------------

		ids.addAll(processQueriesWithIdDistinctListResult(queryStrings,
				parameters));

		return Long.valueOf(ids.size());
	}


	// TODO!!!
	// TODO this is the old reference counter where i counted the referenced
	// media as well and fetched ids from the database to erase dublettes
	@Override
	public Long countReferencesInClassification(Classification classification) {
		if (classification == null)
         {
            return null; // or MAYDO: throw some Exception???
        }

		// get all the descriptive source reference ids
		// ---------------------------------------------

		// preparation
		List<String> queryStrings = new ArrayList<>();
		Map<String, Object> parameters = new HashMap<>();

		parameters.put("classification", classification);

		// get the ids from the Descriptive source references to add them to the
		// count
		//###TODO
//		Set<Integer> ids = listDescriptiveIds(true, classification);

		// get classification reference
		queryStrings.add("SELECT COUNT(c.source.citation.id) "
		        + "FROM Classification as c "
				+ "WHERE c.id=:classificationId ");
		// TODO ???
		// +"join c.souces as s "
		// +"join s.citation "

		parameters.put("classificationId", classification.getId());

		// get node relations references:
		queryStrings
				.add("SELECT COUNT(DISTINCT ts.citation.id) as c "
				    + "FROM TaxonNode tn "
				    + "   LEFT JOIN tn.source ts "
                    + "WHERE tn.classification = :classification "
					+ "  AND tn.source.citation is not null ");

		// get sec references
		// -------------------------------------------------------------------
		// taxa
		queryStrings
				.add("SELECT COUNT(DISTINCT tn.taxon.secSource.citation.id) as c "
				        + "from TaxonNode tn "
						+ "where tn.classification = :classification "
						+ " and tn.taxon.secSource.citation is not null ");

		// synonyms
		queryStrings
				.add("SELECT COUNT(DISTINCT s.secSource.citation.id) AS c "
				        + "FROM TaxonNode tn "
						+ "JOIN tn.taxon.synonyms s "
						+ "WHERE tn.classification=:classification "
						+ " AND sr.relatedFrom.secSource.citation IS NOT NULL ");

		// get relationship citations
		// ---------------------------------------------------------------

		// taxon relations
		queryStrings.add("SELECT COUNT(DISTINCT tr.citation.id) "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.relationsFromThisTaxon as tr "
				+ "WHERE tn.classification=:classification "
				+ "  AND tn.taxon IS NOT NULL "
				+ "  AND tr.source.citation is not null ");

		// get hybrid relation citations
		// Taxa:
		queryStrings.add("SELECT COUNT(DISTINCT hr.source.citation.id) "
		        + "FROM TaxonNode tn "
				+ "  JOIN tn.taxon.name.hybridParentRelations as hr "
				+ "WHERE tn.classification=:classification "
				+ "  AND tn.taxon is not null "
				+ "  AND tn.taxon.name is not null ");

		// synonyms:
		queryStrings.add("SELECT COUNT(DISTINCT hr.source.citation.id) from TaxonNode tn "
				+ "join tn.taxon.synonyms as sy "
				+ "join sy.name.hybridParentRelations as hr "
				+ "where tn.classification=:classification "
				+ " and sy is not null "
				// TODO: is this case actually possible???
				+ " and sy.name is not null ");

		// get name relations references:
		// -------------------------------------------------------
		// Taxa:
		queryStrings.add("SELECT COUNT(DISTINCT nr.source.citation.id) from TaxonNode tn "
				+ "join tn.taxon.name.relationsFromThisName as nr "
				+ "where tn.classification=:classification "
				+ "and tn.taxon is not null "
				+ "and tn.taxon.name is not null ");

		// synonyms:
		queryStrings.add("SELECT COUNT(DISTINCT nr.source.citation.id) from TaxonNode tn "
				+ "join tn.taxon.synonyms as sy "
				+ "join sy.name.relationsFromThisName as nr "
				+ "where tn.classification=:classification "
				+ " and sy is not null " // TODO: is this case actually
										// possible???
				+ " and sy.name is not null ");

		// get Nomenclatural status citation

		// Taxa:
		queryStrings.add("SELECT COUNT(DISTINCT s.citation.id) "
		        + "from TaxonNode tn "
				+ "join tn.taxon.name.status as s "
				+ "where tn.classification=:classification "
				+ " and tn.taxon is not null "
				+ " and tn.taxon.name is not null ");

		// synonyms:
		queryStrings.add("SELECT COUNT(DISTINCT s.source.citation.id) "
		        + "from TaxonNode tn "
				+ "join tn.taxon.synonyms as sy "
				+ "join sy.name.status as s "
				+ "where tn.classification=:classification "
				+ " and sy is not null " // TODO: is this case actually
										// possible???
				+ " and sy.name is not null ");

		// traverse to specimenOrObservation via individualsAssociation

		queryStrings.add("SELECT COUNT(DISTINCT cit.id) "
		        + "from TaxonNode tn "
				+ "join tn.taxon.descriptions as db "
				+ "join db.descriptionElements as ia "
				+ "join ia.associatedSpecimenOrObservation as so "
				+ "join so.sequences as seq "
				+ "join seq.citations as cit "
				+ "where so.class=:dnaSample "
				+ " and ia.class=:individualsAssociation "
				+ " and tn.classification=:classification "
				+ " and cit is not null ");

		// we do assume, that a name description would not have a
		// SpecimenOrObservation element

		parameters.put("dnaSample", "DnaSample");
		parameters.put("individualsAssociation", "IndividualsAssociation");

		//###TODO???
		//		ids.addAll(processQueriesWithIdDistinctListResult(queryStrings,
//				parameters));

		return processQueries(queryStrings, parameters);
	}

// TODO: this is used by countReferencesInClassificationWithIds()

	private Set<UUID> processQueriesWithIdDistinctListResult(
			List<String> queryStrings, Map<String, Object> parameters) {

		// MAYDO catch error if queries deliver wrong type
		Set<UUID> ids = new HashSet<>();
		for (String queryString : queryStrings) {
//		    System.out.println(queryString);
		    Query<UUID> query = getSession().createQuery(queryString, UUID.class);

		    Set<String> parameterNames = query.getParameterMetadata().getNamedParameterNames();
			List<String> queryParameters = new ArrayList<>(parameterNames);

			if (parameters != null) {
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {

					if (queryParameters.contains(entry.getKey())) {
						query.setParameter(entry.getKey(), entry.getValue());
					}
				}
			}

            List<UUID> queryList = query.list();
			ids.addAll(queryList);
		}
		return ids;
	}

	/**
	 * @param queryStrings
	 *            - should be a list of strings that each represent a count hibernate query
	 * @param parameters parameters for all the queries
	 *
	 * @return sum of the values all queries result in
	 */
	private Long processQueries(
			List<String> queryStrings, Map<String, Object> parameters) {

		// MAYDO catch error if queries deliver wrong type
		Query<Long> query;
		Long all = new Long(0);
		Long result;


		for (String queryString : queryStrings) {

			query = getSession().createQuery(queryString, Long.class);

			//add matching parameters to query
			Set<String> parameterNames = query.getParameterMetadata().getNamedParameterNames();
            List<String> queryParameters = new ArrayList<>(parameterNames);

			if (parameters != null) {
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {

					if (queryParameters.contains(entry.getKey())) {
						query.setParameter(entry.getKey(), entry.getValue());
					}
				}
			}
			result=query.uniqueResult();
			all += result;
		}
		return all;

	}

	@Override
	public List<UUID> getTaxonTree(IdentifiableEntity filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UUID> getAllChildNodeIds(UUID rootUuid) {

		Set<UUID> uuids = new HashSet<>();
		List<UUID> children = new ArrayList<>();
		List<UUID> parents = new ArrayList<>();

		// it should be this one!
		// queryString="select distinct chn.uuid from TaxonNode tn " +
		// "join tn.childNodes as chn " +
		// "where tn.uuid in (:parents) ";

		// just for testing, but does not work anyway
		String queryString = "SELECT DISTINCT chn.uuid FROM TaxonNode tn "
				+ " JOIN tn.childNodes as chn "
		        + " WHERE tn.uuid = :parent ";

		Query<UUID> query = getSession().createQuery(queryString, UUID.class);

		parents.add(rootUuid);
		uuids.add(rootUuid);

		// while(!(parents.isEmpty())){
		// query.setParameterList("parents",parents);
		query.setParameter("parent", parents.get(0));
		children = query.list();
		uuids.addAll(children);
		parents = children;
		// }
		List<UUID> uuidList = new ArrayList<>();
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

		Set<UUID> uuids = new HashSet<>();

		// return (List<UUID>) uuids;

	}

}
