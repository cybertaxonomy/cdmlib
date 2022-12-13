/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.metadata.TermSearchField;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since 29.05.2008
 */
@Repository
public class DefinedTermDaoImpl
        extends IdentifiableDaoBase<DefinedTermBase>
        implements IDefinedTermDao{

    private static final Logger logger = LogManager.getLogger(DefinedTermDaoImpl.class);

	@SuppressWarnings("unchecked")
    public DefinedTermDaoImpl() {
		super(DefinedTermBase.class);
		indexedClasses = new Class[25];
		indexedClasses[0] = Rank.class;
		indexedClasses[1] = AnnotationType.class;
		indexedClasses[2] = ExtensionType.class;
		indexedClasses[3] = Language.class;
		indexedClasses[4] = MarkerType.class;
		indexedClasses[5] = MeasurementUnit.class;
		indexedClasses[6] = DefinedTerm.class;
		indexedClasses[7] = PresenceAbsenceTerm.class;
		indexedClasses[8] = State.class;
		indexedClasses[9] = StatisticalMeasure.class;
		indexedClasses[10] = TextFormat.class;
		indexedClasses[11] = DerivationEventType.class;
		indexedClasses[12] = NamedArea.class;
		indexedClasses[13] = NamedAreaLevel.class;
		indexedClasses[14] = NamedAreaType.class;
		indexedClasses[15] = ReferenceSystem.class;
		indexedClasses[16] = Country.class;
		indexedClasses[17] = RightsType.class;
		indexedClasses[18] = HybridRelationshipType.class;
		indexedClasses[19] = NameRelationshipType.class;
		indexedClasses[20] = NameTypeDesignationStatus.class;
		indexedClasses[21] = NomenclaturalStatusType.class;
		indexedClasses[22] = SpecimenTypeDesignationStatus.class;
		indexedClasses[24] = TaxonRelationshipType.class;
	}

	/**
	 * Searches by Label
	 */
	@Override
    public List<DefinedTermBase> findByLabel(String queryString) {
		return findByLabel(queryString, null);
	}

	/**
	 * Searches by Label
	 */
	@Override
    public List<DefinedTermBase> findByLabel(String queryString, CdmBase sessionObject) {
		checkNotInPriorView("DefinedTermDaoImpl.findByTitle(String queryString, CdmBase sessionObject)");
		Session session = getSession();
		if ( sessionObject != null ) {//attache the object to the session, TODO needed?
			session.update(sessionObject);
		}
		Query<DefinedTermBase> query = session.createQuery("SELECT term "
		        + " FROM DefinedTermBase term JOIN FETCH term.representations representation "
		        + " WHERE representation.label = :label",
		        DefinedTermBase.class);
		query.setParameter("label", queryString);
		@SuppressWarnings("rawtypes")
		List<DefinedTermBase> result = deduplicateResult(query.list());
		return result;
	}

	@Override
    public List<DefinedTermBase> findByTitle(String queryString, MatchMode matchMode, int page, int pagesize, List<Criterion> criteria) {
		//FIXME is query parametrised?
		checkNotInPriorView("DefinedTermDaoImpl.findByTitle(String queryString, ITitledDao.MATCH_MODE matchMode, int page, int pagesize, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		crit.setMaxResults(pagesize);
		int firstItem = (page - 1) * pagesize + 1;
		crit.setFirstResult(firstItem);
		@SuppressWarnings("unchecked")
        List<DefinedTermBase> results = deduplicateResult(crit.list());
		return results;
	}

	@Override
    public Country getCountryByIso(String iso3166) {
		// If iso639 = "" query returns non-unique result. We prevent this here:
		if (StringUtils.isBlank(iso3166) || iso3166.length()<2 || iso3166.length()>3) { return null; }
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query<Country> query = getSession().createQuery("FROM Country WHERE iso3166_A2 = :isoCode OR idInVocabulary = :isoCode", Country.class);
		    query.setParameter("isoCode", iso3166);
		    return query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Country.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("iso3166_A2").eq(iso3166));
			query.add(AuditEntity.property("idInVocabulary").eq(iso3166));
			return (Country) query.getSingleResult();
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationText(String text, Class<T> clazz ) {
		return getDefinedTermByRepresentationText(text,clazz,null,null);
	}

	@Override
    public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationText(String text, Class<T> clazz, Integer pageSize,Integer  pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getDefinedTermByRepresentationText(String text, Class<T> clazz, Integer pageSize,Integer  pageNumber)");

		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.text", text));

		addPageSizeAndNumber(criteria, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
        List<T> result = deduplicateResult(criteria.list());
		return result;
	}

	@Override
    public long countDefinedTermByRepresentationText(String text, Class<? extends DefinedTermBase> clazz) {
	    checkNotInPriorView("DefinedTermDaoImpl.countDefinedTermByRepresentationText(String text, Class<? extends DefinedTermBase> clazz)");
		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.text", text));

		criteria.setProjection(Projections.rowCount());

		return (Long)criteria.uniqueResult();
	}

	@Override
	public <T extends DefinedTermBase> List<T> getDefinedTermByIdInVocabulary(String label, UUID vocUuid, Class<T> clazz, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getDefinedTermByIdInVocabulary(String label, UUID vocUuid, Class<T> clazz, Integer pageSize, Integer pageNumber)");

		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("vocabulary", "voc")
		    .add(Restrictions.like("voc.uuid", vocUuid))
			.add(Restrictions.like("idInVocabulary", label, org.hibernate.criterion.MatchMode.EXACT));

		addPageSizeAndNumber(criteria, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
        List<T> result = deduplicateResult(criteria.list());
		return result;
	}

    @Override
	public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationAbbrev(String text, Class<T> clazz, Integer pageSize,Integer  pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getDefinedTermByRepresentationAbbrev(String abbrev, Class<T> clazz, Integer pageSize,Integer  pageNumber)");

		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.abbreviatedLabel", text));

		addPageSizeAndNumber(criteria, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
		List<T> result = deduplicateResult(criteria.list());
		return result;
	}

	@Override
	public long countDefinedTermByRepresentationAbbrev(String text, Class<? extends DefinedTermBase> clazz) {
	    checkNotInPriorView("DefinedTermDaoImpl.countDefinedTermByRepresentationAbbrev(String abbrev, Class<? extends DefinedTermBase> clazz)");
		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.abbreviatedLabel", text));
		criteria.setProjection(Projections.rowCount());

        return (Long)criteria.uniqueResult();
	}

	@Override
    public Language getLanguageByIso(String iso639) {
		if (iso639.length() < 2 || iso639.length() > 3) {
			logger.warn("Invalid length " + iso639.length() + " of ISO code. Length must be 2 or 3.");
			return null;
		}
		boolean isIso639_1 = iso639.length() == 2;

		String queryStr;
		if (isIso639_1){
			queryStr = "FROM Language WHERE iso639_1 = :isoCode";
		}else{
			queryStr = "FROM Language WHERE idInVocabulary = :isoCode AND vocabulary.uuid = :vocUuid";
		}
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query<Language> query = getSession().createQuery(queryStr, Language.class);
		    query.setParameter("isoCode", iso639);
		    if (! isIso639_1){
		    	query.setParameter("vocUuid", Language.uuidLanguageVocabulary);
			}
		    return query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Language.class,auditEvent.getRevisionNumber());
			if (isIso639_1){
				query.add(AuditEntity.property("iso639_1").eq(iso639));
			}else{
				query.add(AuditEntity.property("iso639_2").eq(iso639));
				query.add(AuditEntity.property("vocabulary.uuid").eq(Language.uuidLanguageVocabulary));
			}

			return (Language)query.getSingleResult();
		}
	}

	/**
	 *  FIXME this will result in a query per language - could you, given that iso codes
	 *  are unique, use from Language where iso639_1 in (:isoCode) or iso639_2 in (:isoCode)
	 */
	@Override
    public List<Language> getLanguagesByIso(List<String> iso639List) {
		List<Language> languages = new ArrayList<>(iso639List.size());
		for (String iso639 : iso639List) {
			languages.add(getLanguageByIso(iso639));
		}
		return languages;
	}

	@Override
    public List<Language> getLanguagesByLocale(Enumeration<Locale> locales) {
		List<Language> languages = new ArrayList<>();
		while(locales.hasMoreElements()) {
			Locale locale = locales.nextElement();
			languages.add(getLanguageByIso(locale.getLanguage()));
		}
		return languages;
	}

	@Override
    public long count(NamedAreaLevel level, NamedAreaType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getCriteria(NamedArea.class);

		    if(level != null) {
			    criteria.add(Restrictions.eq("level",level));
		    }

		    if(type != null) {
			    criteria.add(Restrictions.eq("type", type));
		    }

		    criteria.setProjection(Projections.rowCount());

	        return (Long)criteria.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(NamedArea.class, auditEvent);

			if(level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
		    }

		    if(type != null) {
		    	query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
		    query.addProjection(AuditEntity.id().count());
		    return (Long)query.getSingleResult();
		}
	}

	@Override
    public long countMedia(DefinedTermBase definedTerm) {
		checkNotInPriorView("DefinedTermDaoImpl.countMedia(DefinedTermBase definedTerm)");
		Query<Long> query = getSession().createQuery("SELECT count(media) "
		        + " FROM DefinedTermBase definedTerm JOIN definedTerm.media media "
		        + " WHERE definedTerm = :definedTerm",
		        Long.class);
	    query.setParameter("definedTerm", definedTerm);

		return query.uniqueResult();
	}

	@Override
    public List<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber)");
		Query<Media> query = getSession().createQuery(
		           "SELECT media "
		        + " FROM DefinedTermBase definedTerm "
		        + "   JOIN definedTerm.media media "
		        + " WHERE definedTerm = :definedTerm",
		        Media.class);
		query.setParameter("definedTerm", definedTerm);

		addPageSizeAndNumber(query, pageSize, pageNumber);

        List<Media> result = query.list();
		return result;
	}

	@Override
    public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(NamedArea.class);

		    if(level != null) {
			    criteria.add(Restrictions.eq("level",level));
		    }

		    if(type != null) {
			    criteria.add(Restrictions.eq("type", type));
		    }

		    addPageSizeAndNumber(criteria, pageSize, pageNumber);

	        @SuppressWarnings("unchecked")
	        List<NamedArea> result = deduplicateResult(criteria.list());
	        return result;
		} else {
            AuditQuery query = makeAuditQuery(NamedArea.class, auditEvent);

			if(level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
		    }

		    if(type != null) {
		    	query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }

		    @SuppressWarnings("unchecked")
            List<NamedArea> result = deduplicateResult(query.getResultList());
		    return result;
		}
	}

	@Override
    public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {

	    List<NamedArea> result;

		AuditEvent auditEvent = getAuditEventFromContext();
		if (auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getCriteria(NamedArea.class);

			if (level != null) {
				criteria.add(Restrictions.eq("level", level));
			}
			if (type != null) {
				criteria.add(Restrictions.eq("type", type));
			}
			addOrder(criteria,orderHints);
			addPageSizeAndNumber(criteria, pageSize, pageNumber);

			result = deduplicateResult(criteria.list());

		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NamedArea.class,
				auditEvent.getRevisionNumber());
			if (level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
			}
			if (type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
			}
			result = deduplicateResult(query.getResultList());
		}

		defaultBeanInitializer.initializeAll(result, propertyPaths);

		return result;
	}


	@Override
    public <T extends DefinedTermBase> long countGeneralizationOf(T kindOf) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query<Long> query = getSession().createQuery(
		            "   SELECT count(term) "
		            + " FROM DefinedTermBase term "
		            + " WHERE term.kindOf = :kindOf",
		            Long.class);
		    query.setParameter("kindOf", kindOf);
		    return query.uniqueResult();
		} else {
            AuditQuery query = makeAuditQuery(DefinedTermBase.class,auditEvent);
			query.add(AuditEntity.relatedId("kindOf").eq(kindOf.getId()));
		    query.addProjection(AuditEntity.id().count());
		    return (Long)query.getSingleResult();
		}
	}

	@Override
    public <T extends DefinedTermBase> long countIncludes(Collection<T> partOf) {
		if (partOf == null || partOf.isEmpty()){
			return 0;
		}
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query<Long> query = getSession().createQuery(
    		        "   SELECT count(term) "
    		        + " FROM DefinedTermBase term "
    		        + " WHERE term.partOf in (:partOf)",
    		        Long.class);
	    	query.setParameterList("partOf", partOf);
		    return query.uniqueResult();
		} else {
			long count = 0;
			for(T t : partOf) {
				AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
				query.add(AuditEntity.relatedId("partOf").eq(t.getId()));
			    query.addProjection(AuditEntity.id().count());
			    count += (Long)query.getSingleResult();
			}
			return count;
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getGeneralizationOf(T kindOf, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query<DefinedTermBase> query = getSession().createQuery("SELECT term FROM DefinedTermBase term WHERE term.kindOf = :kindOf", DefinedTermBase.class);
		    query.setParameter("kindOf", kindOf);

		    addPageSizeAndNumber(query, pageSize, pageNumber);
            return (List)deduplicateResult(query.list());
		} else {
			 AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
			 query.add(AuditEntity.relatedId("kindOf").eq(kindOf.getId()));

			 addPageSizeAndNumber(query, pageSize, pageNumber);

             @SuppressWarnings("unchecked")
             List<T> result = deduplicateResult(query.getResultList());
             return result;
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getIncludes(Collection<T> partOf,	Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		if (partOf == null || partOf.isEmpty()){
			return new ArrayList<>();
		}
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query<DefinedTermBase> query = getSession().createQuery("SELECT term FROM DefinedTermBase term WHERE term.partOf in (:partOf)", DefinedTermBase.class);
    		query.setParameterList("partOf", partOf);

    		addPageSizeAndNumber(query, pageSize, pageNumber);

            List<T> results = (List)deduplicateResult(query.list());
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		    return results;
		} else {
			List<T> result = new ArrayList<>();
			for(T t : partOf) {
				AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
				query.add(AuditEntity.relatedId("partOf").eq(t.getId()));
				addPageSizeAndNumber(query, pageSize, pageNumber);

			    result.addAll(deduplicateResult(query.getResultList()));
			}
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			return result;
		}
	}

	@Override
    public <T extends DefinedTermBase> long countPartOf(Set<T> definedTerms) {
		checkNotInPriorView("DefinedTermDaoImpl.countPartOf(Set<T> definedTerms)");
		Query<Long> query = getSession().createQuery("SELECT count(DISTINCT definedTerm) FROM DefinedTermBase definedTerm JOIN definedTerm.includes included WHERE included in (:definedTerms)", Long.class);
		query.setParameterList("definedTerms", definedTerms);
		return query.uniqueResult();
	}

	@Override
    public <T extends DefinedTermBase> List<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("DefinedTermDaoImpl.getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber)");
		@SuppressWarnings("unchecked")
        Query<T> query = getSession().createQuery("select distinct definedTerm from DefinedTermBase definedTerm join definedTerm.includes included where included in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);

		addPageSizeAndNumber(query, pageSize, pageNumber);

        List<T> r = query.list();
		/**
		 * For some weird reason, hibernate returns proxies (extending the superclass), not the actual class on this,
		 * despite querying the damn database and returning the discriminator along with the rest of the object properties!
		 *
		 * Probably a bug in hibernate, but we'll manually deproxy for now since the objects are initialized anyway, the
		 * performance implications are small (we're swapping one array of references for another, not hitting the db or
		 * cache).
		 */
		List<T> results = new ArrayList<>();
		if(!definedTerms.isEmpty()) {
		    for(T t : r) {
		        T deproxied = CdmBase.deproxy(t);
                results.add(deproxied);
		    }
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		}
		return results;
	}

	@Override
    public DefinedTermBase findByUri(URI uri) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query<DefinedTermBase> query = getSession().createQuery("select term from DefinedTermBase term where term.uri = :uri", DefinedTermBase.class);
		    query.setParameter("uri", uri);
		    return query.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
			query.add(AuditEntity.property("uri").eq(uri));
		    return (DefinedTermBase<?>)query.getSingleResult();
		}
	}

	@Override
	public <T extends DefinedTermBase> List<T> listByTermType(TermType termType, Integer limit, Integer start,
	        List<OrderHint> orderHints, List<String> propertyPaths) {
	    @SuppressWarnings("unchecked")
        Query<T> query = getSession().createQuery("SELECT term FROM DefinedTermBase term WHERE term.termType = :termType");
	    query.setParameter("termType", termType);

        List<T> result = deduplicateResult(query.list());

	    defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
	}

	@Override
    public <TERM extends DefinedTermBase> List<TERM> listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
//		checkNotInPriorView("DefinedTermDaoImpl.listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths)");

        Query<TERM> query = getSession().createQuery("FROM " + clazz.getSimpleName(), clazz);
        List<TERM> result = deduplicateResult(query.list());

	    defaultBeanInitializer.initializeAll(result, propertyPaths);
	    return result;
	}

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> type, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPath) {

        return deduplicateResult(super.list(type, limit, start, orderHints, propertyPath));
    }

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> clazz, List<TermVocabulary> vocs, Integer limit, String pattern) {
        return list(clazz, vocs, 0, limit, pattern, MatchMode.BEGINNING);
    }

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> clazz, List<TermVocabulary> vocs, Integer pageNumber, Integer limit, String pattern, MatchMode matchmode){
        if (clazz == null){
            clazz = (Class)type;
        }
        Criteria crit = getSession().createCriteria(clazz, "term");
        if (!StringUtils.isBlank(pattern)){
            crit.createAlias("term.representations", "reps");
            Disjunction or = Restrictions.disjunction();
            if (matchmode == MatchMode.EXACT) {
                or.add(Restrictions.eq("titleCache", matchmode.queryStringFrom(pattern)));
                or.add(Restrictions.eq("reps.label", matchmode.queryStringFrom(pattern)));
            } else {
                or.add(Restrictions.like("titleCache", matchmode.queryStringFrom(pattern)));
                or.add(Restrictions.like("reps.label", matchmode.queryStringFrom(pattern)));
            }
            crit.add(or);
        }

        if (limit != null && limit >= 0) {
            crit.setMaxResults(limit);
        }

        if (vocs != null &&!vocs.isEmpty()){
            crit.createAlias("term.vocabulary", "voc");
            Disjunction or = Restrictions.disjunction();
            for (TermVocabulary<?> voc: vocs){
                Criterion criterion = Restrictions.eq("voc.id", voc.getId());
                or.add(criterion);
            }
            crit.add(or);
        }

        crit.addOrder(Order.asc("titleCache"));
        if (limit == null){
            limit = 1;
        }
        crit.setFirstResult(0);
        @SuppressWarnings("unchecked")
        List<S> results = deduplicateResult(crit.list());
        return results;
    }

    @Override
    public <S extends DefinedTermBase> List<S> listByAbbrev(Class<S> clazz, List<TermVocabulary> vocs, Integer limit, String pattern, TermSearchField type) {
        return listByAbbrev(clazz, vocs, 0, limit, pattern, MatchMode.BEGINNING, type);
    }

    @Override
    public <S extends DefinedTermBase> List<S> listByAbbrev(Class<S> clazz, List<TermVocabulary> vocs, Integer pageNumber, Integer limit, String pattern, MatchMode matchmode, TermSearchField abbrevType){

        if (clazz == null){
            clazz = (Class)type;
        }
        Criteria crit = getSession().createCriteria(clazz, "type");
        if (!StringUtils.isBlank(pattern)){
            if (matchmode == MatchMode.EXACT) {
                crit.add(Restrictions.eq(abbrevType.getKey(), matchmode.queryStringFrom(pattern)));
            } else {
                crit.add(Restrictions.like(abbrevType.getKey(), matchmode.queryStringFrom(pattern)));
            }
        }
        if (limit != null && limit >= 0) {
            crit.setMaxResults(limit);
        }

        if (vocs != null &&!vocs.isEmpty()){
            crit.createAlias("type.vocabulary", "voc");
            Disjunction or = Restrictions.disjunction();
            for (TermVocabulary<?> voc: vocs){
                Criterion criterion = Restrictions.eq("voc.id", voc.getId());
                or.add(criterion);
            }
            crit.add(or);
        }

        crit.addOrder(Order.asc(abbrevType.getKey()));
        if (limit == null){
            limit = 1;
        }
//        int firstItem = (pageNumber - 1) * limit;

        crit.setFirstResult(0);
        @SuppressWarnings("unchecked")
        List<S> results = deduplicateResult(crit.list());
        return results;
    }


    @Override
    public Collection<TermDto> getIncludesAsDto(
            TermDto parentTerm) {
        String queryString;
        if (parentTerm.getTermType().equals(TermType.NamedArea)){
            queryString = TermDto.getTermDtoSelectNamedArea();
        }else{
            queryString = TermDto.getTermDtoSelect();
        }
        queryString = queryString
                + "where a.partOf.uuid = :parentUuid";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("parentUuid", parentTerm.getUuid());

        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> getKindOfsAsDto(TermDto parentTerm) {

        String queryString;
        if (parentTerm.getTermType().equals(TermType.NamedArea)){
            queryString = TermDto.getTermDtoSelectNamedArea();
        }else{
            queryString = TermDto.getTermDtoSelect();
        }
        queryString = queryString + "where a.kindOf.uuid = :parentUuid";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("parentUuid", parentTerm.getUuid());

        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> findByTitleAsDtoWithVocDto(String title, TermType termType) {

        //terms
        String termQueryString = TermDto.getTermDtoSelect()
                + " where a.titleCache like :title "
                + (termType!=null?" and a.termType = :termType ":"");

        title = title.replace("*", "%");
        Query<Object[]> termQuery = getSession().createQuery(termQueryString, Object[].class);
        termQuery.setParameter("title", "%"+title+"%");
        if(termType!=null){
            termQuery.setParameter("termType", termType);
        }

        List<Object[]> termArrayResult = termQuery.list();
        List<TermDto> list = TermDto.termDtoListFrom(termArrayResult);

        //vocabularies
        String vocQueryString = TermCollectionDto.getTermCollectionDtoSelect() + " WHERE a.uuid = :uuid";
        Query<Object[]> vocQuery = getSession().createQuery(vocQueryString, Object[].class);
        Map<UUID,TermVocabularyDto> vocMap = new HashMap<>();
        for (TermDto dto: list){
            UUID vocUuid = dto.getVocabularyUuid();
            TermVocabularyDto vocDto = vocMap.get(vocUuid);
            if (vocDto == null){
                vocQuery.setParameter("uuid", dto.getVocabularyUuid());
                List<Object[]> vocArrayResult = vocQuery.list();
                List<TermVocabularyDto> vocs = TermVocabularyDto.termVocabularyDtoListFrom(vocArrayResult);
                if (!vocs.isEmpty()){
                    vocDto = vocs.get(0);
                    vocMap.put(vocUuid, vocs.get(0));
                }
            }
            dto.setVocabularyDto(vocDto);
        }
        return list;
    }

    @Override
    public TermDto findByUUIDAsDto(UUID uuid) {

        String queryString = TermDto.getTermDtoSelect()
                + " where a.uuid like :uuid ";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("uuid", uuid);

        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        if (list.size()== 1){
            return list.get(0);
        }else{
            return null;
        }
    }

    @Override
    public Collection<TermDto> findByTypeAsDto(TermType termType) {
        if (termType == null){
            return null;
        }
        String queryString = TermDto.getTermDtoSelect()
                + " WHERE a.termType = :termType ";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);

        query.setParameter("termType", termType);

        List<Object[]> result = query.list();
        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> findByUriAsDto(URI uri, String termLabel, TermType termType) {
        String queryString = TermDto.getTermDtoSelect()
                + " where a.uri like :uri "
                + (termType!=null?" and a.termType = :termType ":"")
                + (termLabel!=null?" and a.titleCache = :termLabel ":"")
                ;
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("uri", uri.toString());
        if(termLabel!=null){
            query.setParameter("termLabel", "%"+termLabel+"%");
        }
        if(termType!=null){
            query.setParameter("termType", termType);
        }

        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public  Map<UUID, List<TermDto>> getSupportedStatesForFeature(Set<UUID> featureUuids){
        Map<UUID, List<TermDto>> map = new HashMap<>();
        for (UUID featureUuid: featureUuids){
            List<TermDto> list = new ArrayList<>();
            String supportedCategoriesQueryString = "SELECT cat.uuid "
                    + "from DefinedTermBase t "
                    + "join t.supportedCategoricalEnumerations as cat "
                    + "where t.uuid = :featureUuid";
            Query<UUID> supportedCategoriesQuery =  getSession().createQuery(supportedCategoriesQueryString, UUID.class);
            supportedCategoriesQuery.setParameter("featureUuid", featureUuid);
    		List<UUID> supportedCategories = supportedCategoriesQuery.list();
            if(supportedCategories.isEmpty()){
                map.put(featureUuid, list);
                continue;
            }

            String queryString = TermDto.getTermDtoSelect()
                    + "where v.uuid in (:supportedCategories) "
                    + "order by a.titleCache";
            Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
            query.setParameterList("supportedCategories", supportedCategories);

            List<Object[]> result = query.list();
            list = TermDto.termDtoListFrom(result);
            map.put(featureUuid, list);
        }
        return map;
    }

    @Override
    public Collection<TermDto> findByUUIDsAsDto(List<UUID> uuidList) {
        List<TermDto> list = new ArrayList<>();
        if (uuidList == null || uuidList.isEmpty()){
            return null;
        }

        String queryString = TermDto.getTermDtoSelect()
                + " WHERE a.uuid in :uuidList "
                + " ORDER by a.titleCache";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameterList("uuidList", uuidList);

        List<Object[]> result = query.list();
        list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> findFeatureByUUIDsAsDto(List<UUID> uuidList) {
        List<TermDto> list = new ArrayList<>();
        if (uuidList == null || uuidList.isEmpty()){
            return null;
        }

        String queryString = FeatureDto.getTermDtoSelect()
                + "where a.uuid in :uuidList "
                + "order by a.titleCache";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameterList("uuidList", uuidList);

        List<Object[]> result = query.list();

        list = FeatureDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> findFeatureByTitleAsDto(String pattern) {
        String queryString = FeatureDto.getTermDtoSelect()
                + " where a.titleCache like :title "
                +  " and a.termType = :termType ";

        pattern = pattern.replace("*", "%");
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("title", "%"+pattern+"%");
        query.setParameter("termType", TermType.Feature);

        List<Object[]> result = query.list();
        List<TermDto> list = FeatureDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public TermDto getTermDto(UUID uuid) {
        String queryString = TermDto.getTermDtoSelect()
                + " where a.uuid = :uuid ";

        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("uuid", uuid);

        List<Object[]> result = query.list();
        TermDto dto = null;
        List<TermDto> dtoList = TermDto.termDtoListFrom(result);
        if (dtoList != null && !dtoList.isEmpty()){
            dto = dtoList.get(0);
        }
        return dto;
    }

  //***************** Overrides for deduplication *******************************/

    @Override
    public List<DefinedTermBase> loadList(Collection<Integer> ids, List<OrderHint> orderHints,
            List<String> propertyPaths) throws DataAccessException {
        return DefinedTermDaoImpl.deduplicateResult(super.loadList(ids, orderHints, propertyPaths));
    }

    @Override
    public List<DefinedTermBase> list(Collection<UUID> uuids, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) throws DataAccessException {
        return DefinedTermDaoImpl.deduplicateResult(super.list(uuids, pageSize, pageNumber, orderHints, propertyPaths));
    }

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> clazz, Collection<UUID> uuids, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws DataAccessException {
        return DefinedTermDaoImpl.deduplicateResult(super.list(clazz, uuids, pageSize, pageNumber, orderHints, propertyPaths));
    }

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return DefinedTermDaoImpl.deduplicateResult(super.list(type, restrictions, limit, start, orderHints, propertyPaths));
    }

    @Override
    public List<DefinedTermBase> list(Integer limit, Integer start, List<OrderHint> orderHints) {
        return DefinedTermDaoImpl.deduplicateResult(super.list(limit, start, orderHints));
    }

    @Override
    public List<DefinedTermBase> list(Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        return DefinedTermDaoImpl.deduplicateResult(super.list(limit, start, orderHints, propertyPaths));
    }

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> type, Integer limit, Integer start,
            List<OrderHint> orderHints) {
        return DefinedTermDaoImpl.deduplicateResult(super.list(type, limit, start, orderHints));
    }
}