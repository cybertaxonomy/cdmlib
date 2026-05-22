/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.api.filter.CdmBaseFilters;
import eu.etaxonomy.cdm.api.filter.IdentifiableEntityFilters;
import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 01.09.2008
 */
@Repository
public class OccurrenceDaoHibernateImpl
          extends IdentifiableDaoBase<SpecimenOrObservationBase>
          implements IOccurrenceDao {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IDescriptionDao descriptionDao;

    @Autowired
    private ITaxonNameDao taxonNameDao;

    @Autowired
    private IHomotypicalGroupDao homotypicalGroupDao;

    @SuppressWarnings({ "unchecked"})
    public OccurrenceDaoHibernateImpl() {
        super(SpecimenOrObservationBase.class);
        indexedClasses = new Class[7];
        indexedClasses[0] = FieldUnit.class;
        indexedClasses[1] = DerivedUnit.class;
        indexedClasses[5] = DnaSample.class;
    }

    @Override
    public long countDerivationEvents(@SuppressWarnings("rawtypes")SpecimenOrObservationBase occurence) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.countDerivationEvents(SpecimenOrObservationBase occurence)");
        Query<Long> query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence", Long.class);
        query.setParameter("occurence", occurence);

        return query.uniqueResult();
    }

    @Override
    public long countMedia(@SuppressWarnings("rawtypes")SpecimenOrObservationBase occurence) {
        return this.getMediaIds(occurence).size();
    }

    @Override
    public List<Media> getMedia(@SuppressWarnings("rawtypes")SpecimenOrObservationBase occurence,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
        List<Integer> ids = this.getMediaIds(occurence);
        Query<Media> query = getSession().createQuery(
                "   SELECT m "
                + " FROM Media m "
                + " WHERE m.id in (:mediaIds)", Media.class);
        query.setParameterList("mediaIds", ids);

        addPageSizeAndNumber(query, pageSize, pageNumber);

        List<Media> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private List<Integer> getMediaIds(@SuppressWarnings("rawtypes")SpecimenOrObservationBase occurence) {
        Query query = getSession().createQuery(
                "   SELECT DISTINCT m.id "
                + " FROM SpecimenOrObservationBase occ JOIN occ.descriptions d "
                + " JOIN d.descriptionElements el JOIN el.media m "
                + " WHERE occ = :occurence AND d.imageGallery = true "
                + " ORDER BY m.id ");
        query.setParameter("occurence", occurence);
        @SuppressWarnings("unchecked")
        List<Integer> list = query.list();

        if (occurence.isInstanceOf(MediaSpecimen.class)){
            String q2Str = " SELECT DISTINCT m.id "
                    + " FROM MediaSpecimen spec "
                    + " JOIN spec.mediaSpecimen m "
                    + " WHERE spec = :occurence ";
            Query<Integer> q2 = getSession().createQuery(q2Str, Integer.class);
            q2.setParameter("occurence", occurence);
            List<Integer> list2 = q2.list();
            list.addAll(list2);
            Set<Integer> dedupSet = new HashSet<>(list);
            list = new ArrayList<>(dedupSet);
            Collections.sort(list);
        }

        return list;
    }

    @Override
    public List<DerivationEvent> getDerivationEvents(@SuppressWarnings("rawtypes")SpecimenOrObservationBase occurence,
            Integer pageSize,Integer pageNumber, List<String> propertyPaths) {

        checkNotInPriorView("OccurrenceDaoHibernateImpl.getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber)");
        Query<DerivationEvent> query = getSession().createQuery("SELECT DISTINCT derivationEvent FROM DerivationEvent derivationEvent JOIN derivationEvent.originals occurence WHERE occurence = :occurence", DerivationEvent.class);
        query.setParameter("occurence", occurence);

        addPageSizeAndNumber(query, pageSize, pageNumber);

        List<DerivationEvent> result = query.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(SpecimenOrObservationBase<?> occurrence : list(null,null)) { // re-index all taxon base

            for(DeterminationEvent determination : occurrence.getDeterminations()) {
                Hibernate.initialize(determination.getActor());
                Hibernate.initialize(determination.getTaxon());
            }
            Hibernate.initialize(occurrence.getDefinition());
            if(occurrence instanceof DerivedUnit) {
                DerivedUnit derivedUnit = (DerivedUnit) occurrence;
                Hibernate.initialize(derivedUnit.getCollection());
                if(derivedUnit.getCollection() != null) {
                    Hibernate.initialize(derivedUnit.getCollection().getSuperCollection());
                    Hibernate.initialize(derivedUnit.getCollection().getInstitute());
                }
                SpecimenOrObservationBase<?> original = derivedUnit.getOriginalUnit();
                if(original != null && original.isInstanceOf(FieldUnit.class)) {
                    FieldUnit fieldUnit = CdmBase.deproxy(original, FieldUnit.class);
                    Hibernate.initialize(fieldUnit.getGatheringEvent());
                    if(fieldUnit.getGatheringEvent() != null) {
                        Hibernate.initialize(fieldUnit.getGatheringEvent().getActor());
                    }
                }
            }
            fullTextSession.index(occurrence);
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public long count(@SuppressWarnings("rawtypes") Class<? extends SpecimenOrObservationBase> clazz, TaxonName determinedAs) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        @SuppressWarnings("rawtypes")
        Root<SpecimenOrObservationBase> root = cq.from(SpecimenOrObservationBase.class);

        Predicate predicate = determinedAsTaxonPredicate(determinedAs, cb, root);

        cq.select(cb.countDistinct(root))
          .where(predicate);

        return getSession().createQuery(cq).getSingleResult();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> clazz, TaxonName determinedAs,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<SpecimenOrObservationBase> cq = cb.createQuery(SpecimenOrObservationBase.class);
        Root<SpecimenOrObservationBase> root = cq.from(SpecimenOrObservationBase.class);

        Predicate predicate = determinedAsTaxonPredicate(determinedAs, cb, root);

        cq.select(root)
          .distinct(true)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<SpecimenOrObservationBase> results = addPageSizeAndNumber(
                getSession().createQuery(cq), pageSize, pageNumber)
               .getResultList();
       defaultBeanInitializer.initializeAll(results, propertyPaths);
       return deduplicateResult(results);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public long count(Class<? extends SpecimenOrObservationBase> clazz,	TaxonBase determinedAs) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<SpecimenOrObservationBase> root = cq.from(SpecimenOrObservationBase.class);

        Predicate predicate = determinedAsTaxonPredicate(determinedAs, cb, root);

        cq.select(cb.countDistinct(root))
          .where(predicate);

        return getSession().createQuery(cq).getSingleResult();
    }


    @SuppressWarnings("rawtypes")
    @Override
    public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> clazz, TaxonBase determinedAs,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<SpecimenOrObservationBase> cq = cb.createQuery(SpecimenOrObservationBase.class);
        Root<SpecimenOrObservationBase> root = cq.from(SpecimenOrObservationBase.class);

        Predicate predicate = determinedAsTaxonPredicate(determinedAs, cb, root);

        cq.select(root)
          .distinct(true)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<SpecimenOrObservationBase> results = addPageSizeAndNumber(
                getSession().createQuery(cq), pageSize, pageNumber)
               .getResultList();
       defaultBeanInitializer.initializeAll(results, propertyPaths);
       return deduplicateResult(results);
    }

    @SuppressWarnings("rawtypes")
    private Predicate determinedAsTaxonPredicate(TaxonBase determinedAs, CriteriaBuilder cb,
            Root<SpecimenOrObservationBase> root) {
        return cb.equal(root.join("determinations").get("taxon"), determinedAs);
    }

    @SuppressWarnings("rawtypes")
    private Predicate determinedAsTaxonPredicate(TaxonName determinedAs, CriteriaBuilder cb,
            Root<SpecimenOrObservationBase> root) {
        return cb.equal(root.join("determinations").get("taxonName"), determinedAs);
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<UuidAndTitleCache<SpecimenOrObservationBase>>
        findOccurrencesUuidAndTitleCache(
            Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis,
            Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer pageSize, Integer start, List<OrderHint> orderHints) {

        clazz = nullSafeClass(clazz);
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<T> root = cq.from(clazz);

        List<String> propertyPaths = null;
        Predicate predicate = createFindOccurrencesPredicate(cb, root, clazz, queryString, matchmode,
                significantIdentifier, recordBasis, associatedTaxon, associatedTaxonName, includeUnpublished,
                taxonOccurrenceRelTypes, pageSize, start, orderHints, propertyPaths );

        final String UUID = CdmBaseFilters.UUID;
        final String ID = CdmBaseFilters.ID;
        final String TITLE_CACHE = IdentifiableEntityFilters.TITLE_CACHE;

        cq.multiselect(root.get(UUID), root.get(ID), root.get(TITLE_CACHE))
          .distinct(true)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<Tuple> tuples = addPageSizeAndStart(
                getSession().createQuery(cq), pageSize, start)
               .getResultList();

        @SuppressWarnings("rawtypes")
        List<UuidAndTitleCache<SpecimenOrObservationBase>> result = tuples.stream()
                .map(tuple -> new UuidAndTitleCache<SpecimenOrObservationBase>(tuple.get(UUID,
                        UUID.class), tuple.get(ID, Integer.class), tuple.get(TITLE_CACHE, String.class)))
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> findOccurrences(
            Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, boolean includeUnpublished, EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer pageSize, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        clazz = nullSafeClass(clazz);
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        Predicate predicate = createFindOccurrencesPredicate(cb, root, clazz, queryString, matchmode,
                significantIdentifier, recordBasis, associatedTaxon, associatedTaxonName, includeUnpublished,
                taxonOccurrenceRelTypes, pageSize, start, orderHints, propertyPaths);

        cq.select(root)
          .distinct(true)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<T> results = addPageSizeAndStart(
                getSession().createQuery(cq), pageSize, start)
               .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return deduplicateResult(results);
    }

    private <T extends SpecimenOrObservationBase> Predicate createFindOccurrencesPredicate(
            CriteriaBuilder cb, Root<T> root,
            Class<T> clazz, String queryString, MatchMode matchmode,
            String significantIdentifier, SpecimenOrObservationType recordBasis,
            Taxon associatedTaxon, TaxonName associatedTaxonName,
            boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        List<Predicate> predicates = new ArrayList<>();

        //specimen titleCache
        boolean ignoreCase = true;
        predicates.add(IdentifiableEntityFilters.titleCacheFilter(clazz,
                queryString, matchmode, ignoreCase).toPredicate(root, cb));

        //significant identifier
        if (significantIdentifier != null && !FieldUnit.class.isAssignableFrom(clazz)) {
            //only if clazz is derived unit or specimenOrObservation
            //if clazz is derived unit or subclass we could neglect the isNotDerivedUnit predicate, but if clazz is SpecimenOrObservation we need to exclude derived units (and subclass), because they do not have the fields we want to search for

            //FIXME after upgrading to Hibernate 6 use cb.isInstanceOf(rootClass)
            //also .treat() does not work correctly in hibernate 5 (it is handled as a filter on the exact class, not as cast only,
            //as a fast workaround we implemented the below for each subclass
            //An alternative would be to use a subquery
            //Something like
         // 1. Die Unterabfrage erstellen (sucht direkt auf der Klasse Book)
//            Subquery<Long> subquery = query.subquery(Long.class);
//            Root<Book> subRoot = subquery.from(Book.class);
//
//            // Wir wählen die IDs aller Bücher aus, die NICHT der gesuchten ISBN entsprechen
//            subquery.select(subRoot.get("id"))
//                    .where(cb.notEqual(subRoot.get("isbn"), "123-456"));
//
//            // 2. Die Hauptabfrage filtern:
//            // Lass alle Objekte durch, deren ID NICHT in der Liste der "falschen Bücher" auftaucht
//            Predicate excludeWrongBooks = cb.not(root.get("id").in(subquery));

            //the case is covered by AbcdGgbnImportTest so it is save to test different solutions

//            Predicate isDerivedUnit = cb.not(root.type().in(DerivedUnit.class, MediaSpecimen.class, DnaSample.class));
            Predicate isDerivedUnit = cb.equal(root.type(), DerivedUnit.class);
            Predicate isMediaSpecimen = cb.equal(root.type(), MediaSpecimen.class);
            Predicate isDnaSample = cb.equal(root.type(), DnaSample.class);

            Root<DerivedUnit> duRoot = cb.treat((Root)root, DerivedUnit.class);
            Root<MediaSpecimen> msRoot = cb.treat((Root)root, MediaSpecimen.class);
            Root<DnaSample> dnaRoot = cb.treat((Root)root, DnaSample.class);
            predicates.add(
                cb.or(
                    cb.and(
                        isDerivedUnit, cb.or(
                            predicateILike(cb, duRoot, "accessionNumber", significantIdentifier),
                            predicateILike(cb, duRoot, "catalogNumber", significantIdentifier),
                            predicateILike(cb, duRoot, "barcode", significantIdentifier)
                        )
                    ),
                    cb.and(
                            isMediaSpecimen, cb.or(
                                predicateILike(cb, msRoot, "accessionNumber", significantIdentifier),
                                predicateILike(cb, msRoot, "catalogNumber", significantIdentifier),
                                predicateILike(cb, msRoot, "barcode", significantIdentifier)
                            )
                        ),
                    cb.and(
                            isDnaSample, cb.or(
                                predicateILike(cb, dnaRoot, "accessionNumber", significantIdentifier),
                                predicateILike(cb, dnaRoot, "catalogNumber", significantIdentifier),
                                predicateILike(cb, dnaRoot, "barcode", significantIdentifier)
                            )
                        )
                    )
            );
        }

        //recordBasis/SpecimenOrObservationType
        if(recordBasis != null){
            Set<SpecimenOrObservationType> typeAndSubtypes = recordBasis.getGeneralizationOf(true);
            typeAndSubtypes.add(recordBasis);
            predicates.add(root.get("recordBasis").in(typeAndSubtypes));
        }

        Set<UUID> associationUuids = new HashSet<>();
        //taxon associations
        if(associatedTaxon != null || associatedTaxonName != null){
            if(associatedTaxon != null){
                @SuppressWarnings("rawtypes")
                List<UuidAndTitleCache<SpecimenOrObservationBase>> associatedTaxaList =
                    listUuidAndTitleCacheByAssociatedTaxon(
                        clazz, associatedTaxon, includeUnpublished, taxonOccurrenceRelTypes,
                        limit, start, orderHints);
                if(associatedTaxaList != null){
                    for (UuidAndTitleCache<SpecimenOrObservationBase> uuidAndTitleCache : associatedTaxaList) {
                        associationUuids.add(uuidAndTitleCache.getUuid());
                    }
                }
            }
            //taxon name associations
            else {
                @SuppressWarnings("rawtypes")
                List<? extends SpecimenOrObservationBase> associatedTaxaList = listByAssociatedTaxonName(clazz, associatedTaxonName, limit, start, orderHints, propertyPaths);
                if(associatedTaxaList != null){
                    for (SpecimenOrObservationBase<?> specimenOrObservationBase : associatedTaxaList) {
                        associationUuids.add(specimenOrObservationBase.getUuid());
                    }
                }
            }
            if(!associationUuids.isEmpty()){
                predicates.add(predicateIn(root, "uuid", associationUuids));
            }
            else{
                return cb.disjunction();  //always false predicate
            }
        }
        return predicateAnd(cb, predicates);
    }

    @Override
    public <T extends SpecimenOrObservationBase> long countOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, boolean includeUnpublished, EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        clazz = nullSafeClass(clazz);
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(clazz);

        Predicate predicate = createFindOccurrencesPredicate(
                cb, root,
                clazz, queryString, matchmode,
                significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName,
                includeUnpublished, taxonOccurrenceRelTypes,
                limit, start, orderHints, propertyPaths);

        cq.select(cb.countDistinct(root))
          .where(predicate);

        return getSession().createQuery(cq).getSingleResult();
    }

    @Override
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache(Integer limit, String pattern) {
        List<UuidAndTitleCache<DerivedUnit>> list = new ArrayList<>();
        Session session = getSession();
        String hql = "SELECT uuid, id, titleCache "
                + " FROM " + type.getSimpleName()
                + " WHERE NOT dtype = " + FieldUnit.class.getSimpleName();
        Query<Object[]> query;
        if (pattern != null){
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query = session.createQuery(hql +" AND titleCache like :pattern", Object[].class);
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(hql, Object[].class);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<DerivedUnit>(DerivedUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        List<UuidAndTitleCache<FieldUnit>> list = new ArrayList<>();

        Query<Object[]> query = getSession().createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = " + FieldUnit.class.getSimpleName(), Object[].class);

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<FieldUnit>(FieldUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxonName(Class<T> type,
            TaxonName associatedTaxonName, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        @SuppressWarnings("rawtypes")
        Set<SpecimenOrObservationBase> setOfAll = new HashSet<>();

        // A Taxon name may be referenced by the DeterminationEvent of the SpecimenOrObservationBase
        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> byDetermination = list(type, associatedTaxonName, null, 0, null, null);
        setOfAll.addAll(byDetermination);

        if(setOfAll.size() == 0){
            // no need querying the data base
            return new ArrayList<T>();
        }

        String queryString =
            "SELECT sob " +
            " FROM SpecimenOrObservationBase sob" +
            " WHERE sob in (:setOfAll)";

        if(type != null && !type.equals(SpecimenOrObservationBase.class)){
            queryString += " AND sob.class = :type";
        }
        queryString += orderByClause("sob", orderHints);

        @SuppressWarnings("unchecked")
        Query<T> query = getSession().createQuery(queryString);
        query.setParameterList("setOfAll", setOfAll);

        if(type != null && !type.equals(SpecimenOrObservationBase.class)){
            query.setParameter("type", type.getSimpleName());
        }

        addLimitAndStart(query, limit, start);

        List<T> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private List<SpecimenNodeWrapper> querySpecimen(Query<Object[]> query, List<UUID> taxonNodeUuids,
            Integer limit, Integer start){

        query.setParameterList("taxonNodeUuids", taxonNodeUuids);
        addLimitAndStart(query, limit, start);

        List<SpecimenNodeWrapper> list = new ArrayList<>();
        List<Object[]> result = query.list();
        for(Object[] object : result){
            //no specimen!
            if (object[0] == null) {
                continue;
            }
            UuidAndTitleCache<SpecimenOrObservationBase> temp = new UuidAndTitleCache<>(
                    (UUID) object[0],
                    (Integer) object[1],
                    (String) object[7]);

            String shortCache = "";
            String countryString = (String)object[10];
            String fieldNumber = (String)object[2];
            String collectorString = (String)object[9];
            String collectionCode = (String)object[6];
            String identifier = "";

            if (object[3] != null) {
                identifier = (String)object[3];
            }else if (object[4] != null) {
                identifier = (String)object[4];
            }else if (object[5] != null) {
                identifier = (String)object[5];
            }


            shortCache = CdmUtils.concat(" - ", countryString, collectorString, fieldNumber, collectionCode, identifier);
            temp.setAbbrevTitleCache(shortCache);
            TaxonNode node= null;
            if (object[11] == null && object.length>12 && object[12] instanceof TaxonNode) {
                node = (TaxonNode)object[12];
            }else {
                node = (TaxonNode)object[11];
            }
            SpecimenNodeWrapper wrapper = new SpecimenNodeWrapper(temp,
                    (SpecimenOrObservationType)object[8],
                    new TaxonNodeDto(node));

            if(object.length>12 && object[12] instanceof UUID) {
                wrapper.setTaxonDescriptionUuid((UUID)object[12]);
            }
            list.add(wrapper);
        }
        return list;
    }

    private List<SpecimenNodeWrapper> queryIndividualAssociatedSpecimen(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "specimen.uuid, "
                + "specimen.id, "
                + "original.fieldNumber, "
                + "specimen.barcode, "
                + "specimen.accessionNumber, "
                + "specimen.catalogNumber, "
                + "collection.code, "
                + "specimen.titleCache, "
                + "specimen.recordBasis, "
                + "collector.collectorTitleCache, "
                + "country.titleCache, "
                + "tn, "
                + "d.uuid "
                + "FROM DescriptionElementBase AS de "
                + "LEFT JOIN de.inDescription AS d "
                + "JOIN de.associatedSpecimenOrObservation as specimen "
                + "LEFT JOIN specimen.collection AS collection "
                + "LEFT JOIN d.taxon AS t "
                + "LEFT JOIN specimen.derivedFrom AS derivedFrom "
                + "LEFT JOIN derivedFrom.originals AS original "
                + "LEFT JOIN original.gatheringEvent AS gathering "
                + "LEFT JOIN gathering.actor AS collector "
                + "LEFT JOIN gathering.country AS country "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE d.class = 'TaxonDescription' "
                + "AND tn.uuid in (:taxonNodeUuids) "
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTypeSpecimen(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){

        String queryString =  "SELECT "
                + "td.typeSpecimen.uuid, "
                + "td.typeSpecimen.id, "
                + "original.fieldNumber, "
                + "td.typeSpecimen.barcode, "
                + "td.typeSpecimen.accessionNumber, "
                + "td.typeSpecimen.catalogNumber, "
                + "collection.code, "
                + "td.typeSpecimen.titleCache, "
                + "td.typeSpecimen.recordBasis, "
                + "collector.collectorTitleCache, "
                + "country.titleCache, "
                + "tnode, "
                + "tnode2 "
                + "FROM SpecimenTypeDesignation AS td "
                + "LEFT JOIN td.typifiedNames AS tn "
                + "LEFT JOIN td.typeSpecimen.collection as collection "
                + "LEFT JOIN tn.taxonBases AS t "
                + "LEFT JOIN t.acceptedTaxon AS taxon "
                + "LEFT JOIN td.typeSpecimen.derivedFrom AS derivedFrom "
                + "LEFT JOIN derivedFrom.originals AS original "
                + "LEFT JOIN original.gatheringEvent AS gathering "
                + "LEFT JOIN gathering.actor AS collector "
                + "LEFT JOIN gathering.country AS country "
                + "LEFT JOIN t.taxonNodes AS tnode "
                + "LEFT JOIN taxon.taxonNodes as tnode2 "
                + "WHERE tnode.uuid in (:taxonNodeUuids) OR tnode2.uuid in (:taxonNodeUuids)"
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTaxonDeterminations(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "det.identifiedUnit.uuid, "
                + "det.identifiedUnit.id, "
                + "original.fieldNumber, "
                + "det.identifiedUnit.barcode, "
                + "det.identifiedUnit.accessionNumber, "
                + "det.identifiedUnit.catalogNumber, "
                + "collection.code, "
                + "det.identifiedUnit.titleCache, "
                + "det.identifiedUnit.recordBasis, "
                + "collector.collectorTitleCache, "
                + "country.titleCache, "
                + "tn "
                + "FROM DeterminationEvent AS det "
                + "LEFT JOIN det.taxon AS t "
                + "LEFT JOIN det.identifiedUnit.collection as collection "
                + "LEFT JOIN det.identifiedUnit.derivedFrom AS derivedFrom "
                + "LEFT JOIN derivedFrom.originals AS original "
                + "LEFT JOIN original.gatheringEvent AS gathering "
                + "LEFT JOIN gathering.actor AS collector "
                + "LEFT JOIN gathering.country AS country "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTaxonNameDeterminations(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "det.identifiedUnit.uuid, "
                + "det.identifiedUnit.id, "
                + "original.fieldNumber, "
                + "det.identifiedUnit.barcode, "
                + "det.identifiedUnit.accessionNumber, "
                + "det.identifiedUnit.catalogNumber, "
                + "collection.code, "
                + "det.identifiedUnit.titleCache, "
                + "det.identifiedUnit.recordBasis, "
                + "collector.collectorTitleCache, "
                + "country.titleCache, "
                + "tnode, "
                + "tnode2 "
                + "FROM DeterminationEvent AS det "
                + "LEFT JOIN det.identifiedUnit.collection as collection "
                + "LEFT JOIN det.taxonName AS n "
                + "LEFT JOIN n.taxonBases AS t "
                + "LEFT JOIN t.acceptedTaxon AS taxon "
                + "LEFT JOIN det.identifiedUnit.derivedFrom AS derivedFrom "
                + "LEFT JOIN derivedFrom.originals AS original "
                + "LEFT JOIN original.gatheringEvent AS gathering "
                + "LEFT JOIN gathering.actor AS collector "
                + "LEFT JOIN gathering.country AS country "
                + "LEFT JOIN t.taxonNodes AS tnode "
                + "LEFT JOIN taxon.taxonNodes as tnode2 "
                + "WHERE tnode.uuid in (:taxonNodeUuids) OR tnode2.uuid in (:taxonNodeUuids)"
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    @Override
    public Collection<SpecimenNodeWrapper> listUuidAndTitleCacheByAssociatedTaxon(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){

        Set<SpecimenNodeWrapper> testSet = new HashSet<>();

        testSet.addAll(queryIndividualAssociatedSpecimen(taxonNodeUuids, limit, start));
        testSet.addAll(queryTaxonDeterminations(taxonNodeUuids, limit, start));
        testSet.addAll(queryTaxonNameDeterminations(taxonNodeUuids, limit, start));
        testSet.addAll(queryTypeSpecimen(taxonNodeUuids, limit, start));

        Collection<SpecimenNodeWrapper> wrappers = new HashSet<>();
        wrappers.addAll(testSet);
        return wrappers;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<UuidAndTitleCache<SpecimenOrObservationBase>> listUuidAndTitleCacheByAssociatedTaxon(
            Class<T> clazz, Taxon associatedTaxon, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints){

        Query<Object[]> query = createSpecimenQuery("sob.uuid, sob.id, sob.titleCache", clazz,
                associatedTaxon, includeUnpublished, taxonOccurrenceRelTypes,
                limit, start, orderHints, Object[].class);
        if(query==null){
            return Collections.emptyList();
        }
        List<UuidAndTitleCache<SpecimenOrObservationBase>> list = new ArrayList<>();
        List<Object[]> result = query.list();
        for(Object[] object : result){
            list.add(new UuidAndTitleCache<>((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> clazz,
            Taxon associatedTaxon, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        @SuppressWarnings("rawtypes")
        Query<SpecimenOrObservationBase> query = createSpecimenQuery(
                "sob", clazz, associatedTaxon, includeUnpublished, taxonOccurrenceRelTypes,
                limit, start, orderHints, SpecimenOrObservationBase.class);
        if(query==null){
            return Collections.emptyList();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<T> results = (List)query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private <T extends SpecimenOrObservationBase, R extends Object> Query<R> createSpecimenQuery(
            String select, Class<T> clazz, Taxon associatedTaxon,
            boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start,
            List<OrderHint> orderHints, Class<R> returnClass){

        Set<Integer> setOfAllIds = new HashSet<>();
        boolean classFilterExists = clazz != null && !clazz.equals(SpecimenOrObservationBase.class);
        boolean requiresClassFilter = false;

        //Note: we don't pass limits and order to individual results query as the data is merged with other results

        //add determinations
        if (taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.Determination)
                || taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.CurrentDetermination)) {
            boolean currentOnly = !taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.Determination);
            List<Integer> detResults = addAssociatedDeterminations(clazz, associatedTaxon, currentOnly);
            setOfAllIds.addAll(detResults);
        }

        //add specimen associated via IndividualsAssociation
        if (taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.IndividualsAssociation)) {
            List<Integer> iaResults = descriptionDao.getIndividualAssociationSpecimenIDs(
                    associatedTaxon.getUuid(), null, includeUnpublished, null, null, null);
            //NOTE: iaResults are not yet filtered by clazz
            requiresClassFilter |= classFilterExists && !iaResults.isEmpty();
            setOfAllIds.addAll(iaResults);
        }

        // add specimen associated via type designation
        if (taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.TypeDesignation)) {
            //... of accepted taxon name
            List<Integer> accTdResults = taxonNameDao.getTypeSpecimenIdsForTaxonName(
                    associatedTaxon.getName(), null, null, null);
            //NOTE: accTdResults are not yet filtered by clazz
            requiresClassFilter |= classFilterExists && !accTdResults.isEmpty();
            setOfAllIds.addAll(accTdResults);

            //... and of synonym names (via homotypic groups)
            Set<Integer> synTdResults = getTypeSpecimenIdsForSynonyms(associatedTaxon);
            //NOTE: synTdResults are not yet filtered by clazz
            requiresClassFilter |= classFilterExists && !synTdResults.isEmpty();
            setOfAllIds.addAll(synTdResults);
        }

        if(setOfAllIds.isEmpty()){
            // no need querying the data base
            return null;
        }

        //query
        String queryString =
            " SELECT "+select+
            " FROM SpecimenOrObservationBase sob" +
            " WHERE sob.id in (:setOfAllIds)";

        if (!includeUnpublished) {
            queryString += " AND sob.publish = TRUE ";
        }

        if(requiresClassFilter){
            queryString += " AND sob.class = :type ";
        }

        if(orderHints != null && orderHints.size() > 0){
            queryString += " ORDER BY ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "sob." + orderHint.toHql();
            }
            queryString += orderStr;
        }

        Query<R> query = getSession().createQuery(queryString, returnClass);
        query.setParameterList("setOfAllIds", setOfAllIds);

        if(requiresClassFilter){
            //note: null warning is incorrect here
            query.setParameter("type", clazz.getSimpleName());
        }

        addLimitAndStart(query, limit, start);

        return query;
    }

    private Set<Integer> getTypeSpecimenIdsForSynonyms(Taxon associatedTaxon) {
        //TODO check if there is a real synonym relationship between accepted taxon and name of homotypic group
        Set<Integer> synTdResults = new HashSet<>();
        for(HomotypicalGroup homotypicalGroup :  associatedTaxon.getHomotypicSynonymyGroups()) {
            //TODO fetch specimen IDs only instead of loading the type designation
            List<SpecimenTypeDesignation> byHomotypicalGroup = homotypicalGroupDao.getTypeDesignations(
                    homotypicalGroup, SpecimenTypeDesignation.class, null, null, 0, null);
            for (SpecimenTypeDesignation specimenTypeDesignation : byHomotypicalGroup) {
                if (specimenTypeDesignation.getTypeSpecimen() != null){
                    synTdResults.add(specimenTypeDesignation.getTypeSpecimen().getId());
                }
            }
        }
        return synTdResults;
    }

    /**
     * Computes the IDs of the specimen associated with a taxon via determinations
     */
    private <S extends SpecimenOrObservationBase> List<Integer> addAssociatedDeterminations(
            Class<S> clazz,
            Taxon associatedTaxon, boolean currentOnly) {

        clazz = nullSafeClass(clazz);
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<S> root = cq.from(clazz);

        List<Predicate> predicates = new ArrayList<>();
        Join<S, DeterminationEvent> determinations = root.join("determinations");
        if (currentOnly) {
            predicates.add(predicateBoolean(cb, determinations, "preferredFlag", Boolean.TRUE));
        }

        //determined as taxon or taxon name/ synonym or synonym name
        List<Predicate> orPredicates = new ArrayList<>();
        orPredicates.add(cb.equal(determinations.get("taxon"), associatedTaxon)); //taxon
        orPredicates.add(cb.and(cb.isNull(determinations.get("taxon")),          //taxon name
                                cb.equal(determinations.get("taxonName"), associatedTaxon.getName())));
        associatedTaxon.getSynonyms().forEach(
                synonym -> orPredicates.add(cb.equal(determinations.get("taxon"), synonym))); //synonyms
        associatedTaxon.getSynonymNames().forEach(
                synonymName -> orPredicates.add(
                        cb.and(cb.isNull(determinations.get("taxon")), // synonym names
                               cb.equal(determinations.get("taxonName"), synonymName))));

        predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));

        cq.select(root.get("id"))
          .distinct(true)
          .where(predicateAnd(cb, predicates));

        List<Integer> results = getSession().createQuery(cq)
               .getResultList();
        return results;
    }

    @Override
    public Collection<SpecimenOrObservationBase> listBySpecimenOrObservationType(SpecimenOrObservationType type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenOrObservationBase specimens "
                + " WHERE specimens.recordBasis = :type ";

        queryString += orderByClause("specimens", orderHints);

        Query<SpecimenOrObservationBase> query = getSession().createQuery(queryString, SpecimenOrObservationBase.class);
        query.setParameter("type", type);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }


    @Override
    public Collection<DeterminationEvent> listDeterminationEvents(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM DeterminationEvent determination "
                + " WHERE determination.identifiedUnit = :specimen";

        queryString += orderByClause("determination", orderHints);

        Query<DeterminationEvent> query = getSession().createQuery(queryString, DeterminationEvent.class);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<DeterminationEvent> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenTypeDesignation designations "
                + " WHERE designations.typeSpecimen = :specimen";

        queryString += orderByClause("designations", orderHints);

        Query<SpecimenTypeDesignation> query = getSession().createQuery(queryString, SpecimenTypeDesignation.class);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<SpecimenTypeDesignation> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM IndividualsAssociation associations WHERE associations.associatedSpecimenOrObservation = :specimen";

        queryString += orderByClause("associations", orderHints);

        Query<IndividualsAssociation> query = getSession().createQuery(queryString, IndividualsAssociation.class);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<IndividualsAssociation> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM DescriptionBase descriptions "
                + " WHERE descriptions.describedSpecimenOrObservation = :specimen";

        queryString += orderByClause("descriptions", orderHints);

        Query<DescriptionBase<?>> query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<DescriptionBase<?>> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<FieldUnit> findFieldUnitsForGatheringEvent(UUID gatheringEventUuid, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM FieldUnit fu "
                + "WHERE fu.gatheringEvent.uuid = :gatheringEventUuid";

        queryString += orderByClause("sob", orderHints);

        Query<FieldUnit> query = getSession().createQuery(queryString, FieldUnit.class);
        query.setParameter("gatheringEventUuid", gatheringEventUuid);

        addLimitAndStart(query, limit, start);

        List<FieldUnit> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public DnaSample findByGeneticAccessionNumber(String accessionNumberString, List<String> propertyPaths) {
        String queryString = "SELECT dnaSample FROM DnaSample as dnaSample join dnaSample.sequences as sequences WITH sequences.geneticAccessionNumber LIKE :accessionNumberString";
        Query query = getSession().createQuery(queryString);
        query.setParameter("accessionNumberString", accessionNumberString);
        @SuppressWarnings("unchecked")
        List<DnaSample> dnaSamples = query.list();
        defaultBeanInitializer.initializeAll(dnaSamples, propertyPaths);

        if (dnaSamples.isEmpty()){
            logger.debug("there is no dnaSample for genetic accession number " + accessionNumberString + " this should not happen.");
            return null;
        }else if (dnaSamples.size() == 1){
            return dnaSamples.get(0);
        } else{
            logger.debug("there are more than one dnaSample for genetic accession number " + accessionNumberString + " this should not happen.");
            return null;
        }
    }

   @Override
   public long countByGeneticAccessionNumber(String accessionNumberString) {
       String queryString = "SELECT count(dnaSample) FROM DnaSample dnaSample JOIN dnaSample.sequences sequence WHERE sequence.geneticAccessionNumber LIKE :accessionNumberString";
       Query<Long> query = getSession().createQuery(queryString, Long.class);
       query.setParameter("accessionNumberString", accessionNumberString);
       long result = query.uniqueResult();
       return result;
   }

    private void extractDeterminedOriginals(List<DerivedUnit> samples, List<DerivedUnit> results) {
        for (DerivedUnit sample:samples){
            if (sample.getDeterminations() != null && !sample.getDeterminations().isEmpty()){
                results.add(sample);
            }else{
                if (sample instanceof DerivedUnit){
                    Set<SpecimenOrObservationBase> originals = sample.getDerivedFrom().getOriginals();
                    List<DerivedUnit> originalDerivedUnits = new ArrayList<>();
                    for (SpecimenOrObservationBase original: originals){
                        if (original instanceof DerivedUnit){
                            originalDerivedUnits.add((DerivedUnit)original);
                        }
                    }
                    if(!originalDerivedUnits.isEmpty()){
                        extractDeterminedOriginals(originalDerivedUnits, results);
                    }
                }
            }
        }
    }

    @Override
    public List<SpecimenOrObservationBase> findOriginalsForDerivedUnit(UUID derivedUnitUuid, List<String> propertyPaths) {
        String queryString = "SELECT DISTINCT o FROM DerivedUnit du"
                + " JOIN du.derivedFrom.originals o WHERE du.uuid LIKE :derivedUnitUuid";
        Query<SpecimenOrObservationBase> query = getSession().createQuery(queryString, SpecimenOrObservationBase.class);
        query.setParameter("derivedUnitUuid", derivedUnitUuid);
        List<SpecimenOrObservationBase> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<Point> findPointsForFieldUnitList(List<UUID> fieldUnitUuids) {
        String queryString = "SELECT DISTINCT fu.gatheringEvent.exactLocation FROM FieldUnit fu"
                + "  WHERE fu.uuid IN (:fieldUnitUuids)";
        Query<Point> query = getSession().createQuery(queryString, Point.class);
        query.setParameterList("fieldUnitUuids", fieldUnitUuids);
        List<Point> results = query.list();

        return results;
    }

    @Override
    public String findMostSignificantIdentifier(UUID derivedUnitUuid) {

        String queryString = "SELECT du.catalogNumber as catalogNumber, du.accessionNumber as accessionNumber, du.barcode as barcode FROM DerivedUnit du"
                + " WHERE du.uuid LIKE :derivedUnitUuid";
        Query<Tuple> query = getSession().createQuery(queryString, Tuple.class);
        query.setParameter("derivedUnitUuid", derivedUnitUuid);
        List<Tuple> results = query.list();
        if (results.isEmpty()){
            return null;
        }
        Tuple stringResult = results.get(0);
        if (stringResult.get("accessionNumber") != null && stringResult.get("accessionNumber") instanceof String){
            return (String)stringResult.get("accessionNumber");
        }
        if (stringResult.get("barcode") != null && stringResult.get("barcode") instanceof String){
            return (String)stringResult.get("barcode");
        }
        if (stringResult.get("catalogNumber") != null && stringResult.get("catalogNumber") instanceof String){
            return (String)stringResult.get("catalogNumber");
        }

        return null;
    }

}
