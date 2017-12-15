/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.search.spatial.impl.Rectangle;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.PreservedSpecimenDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
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
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;

/**
 *
 * @author a.kohlbecker
 * @since Jun 23, 2017
 *
 */
public class DerivedUnitConverterTest extends Assert {


    @Test
    public void toMediaSpecimen() throws DerivedUnitConversionException {
        DerivedUnit du = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        du.setTitleCache("test derived unit", true);
        DerivedUnitConverter<MediaSpecimen> duc = new DerivedUnitConverter<MediaSpecimen>(du, new OccurrenceServiceMock());
        MediaSpecimen target = duc.convertTo(MediaSpecimen.class, SpecimenOrObservationType.StillImage);
        assertNotNull(target);
        assertEquals(SpecimenOrObservationType.StillImage, target.getRecordBasis());
        assertEquals("test derived unit", target.getTitleCache());
    }

    @Test
    public void toDerivedUnit() throws DerivedUnitConversionException {

        MediaSpecimen du = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
        du.setTitleCache("test media specimen", true);
        DerivedUnitConverter<DerivedUnit> duc = new DerivedUnitConverter<DerivedUnit>(du, new OccurrenceServiceMock());
        DerivedUnit target = duc.convertTo(DerivedUnit.class, SpecimenOrObservationType.PreservedSpecimen);
        assertNotNull(target);
        assertEquals(SpecimenOrObservationType.PreservedSpecimen, target.getRecordBasis());
        assertEquals("test media specimen", target.getTitleCache());
    }

    class OccurrenceServiceMock implements IOccurrenceService {

        /**
         * {@inheritDoc}
         */
        @Override
        public void updateTitleCache() {


        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void updateTitleCache(Class<? extends SpecimenOrObservationBase> clazz, Integer stepSize,
                IIdentifiableEntityCacheStrategy<SpecimenOrObservationBase> cacheStrategy, IProgressMonitor monitor) {


        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase find(LSID lsid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase replace(SpecimenOrObservationBase x, SpecimenOrObservationBase y) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<IdentifiableSource> getSources(SpecimenOrObservationBase t, Integer pageSize, Integer pageNumber,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<Rights> getRights(SpecimenOrObservationBase t, Integer pageSize, Integer pageNumber,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getTitleCache(UUID uuid, boolean refresh) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> findByTitle(Class<? extends SpecimenOrObservationBase> clazz,
                String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> findByTitle(
                IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> configurator) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer countByTitle(Class<? extends SpecimenOrObservationBase> clazz, String queryString,
                MatchMode matchmode, List<Criterion> criteria) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer countByTitle(IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> configurator) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> listByTitle(Class<? extends SpecimenOrObservationBase> clazz,
                String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> listByReferenceTitle(Class<? extends SpecimenOrObservationBase> clazz,
                String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int deduplicate(Class<? extends SpecimenOrObservationBase> clazz, IMatchStrategy matchStrategy,
                IMergeStrategy mergeStrategy) {

            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> findTitleCache(Class<? extends SpecimenOrObservationBase> clazz,
                String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
                MatchMode matchMode) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S extends SpecimenOrObservationBase> Pager<IdentifiedEntityDTO<S>> findByIdentifier(Class<S> clazz,
                String identifier, DefinedTerm identifierType, MatchMode matchmode, boolean includeCdmEntity,
                Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S extends SpecimenOrObservationBase> Pager<MarkedEntityDTO<S>> findByMarker(Class<S> clazz,
                MarkerType markerType, Boolean markerValue, boolean includeEntity, Integer pageSize, Integer pageNumber,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<Annotation> getAnnotations(SpecimenOrObservationBase annotatedObj, MarkerType status,
                Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<Marker> getMarkers(SpecimenOrObservationBase annotatableEntity, Boolean technical,
                Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Object[]> groupMarkers(Class<? extends SpecimenOrObservationBase> clazz, Boolean technical,
                Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int countMarkers(Class<? extends SpecimenOrObservationBase> clazz, Boolean technical) {

            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S extends SpecimenOrObservationBase> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz,
                Integer limit, String pattern) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<UuidAndTitleCache<SpecimenOrObservationBase>> getUuidAndTitleCache(Integer limit, String pattern) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<AuditEventRecord<SpecimenOrObservationBase>> pageAuditEvents(SpecimenOrObservationBase t,
                Integer pageSize, Integer pageNumber, AuditEventSort sort, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AuditEventRecord<SpecimenOrObservationBase> getNextAuditEvent(SpecimenOrObservationBase t) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AuditEventRecord<SpecimenOrObservationBase> getPreviousAuditEvent(SpecimenOrObservationBase t) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<AuditEventRecord<SpecimenOrObservationBase>> pageAuditEvents(
                Class<? extends SpecimenOrObservationBase> clazz, AuditEvent from, AuditEvent to,
                List<AuditCriterion> criteria, Integer pageSize, Integer pageValue, AuditEventSort sort,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteResult isDeletable(UUID object, DeleteConfiguratorBase config) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {


        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void lock(SpecimenOrObservationBase t, LockOptions lockOptions) {


        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refresh(SpecimenOrObservationBase t, LockOptions lockOptions, List<String> propertyPaths) {


        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int count(Class<? extends SpecimenOrObservationBase> clazz) {

            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteResult delete(UUID persistentObjectUUID) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean exists(UUID uuid) {

            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> find(Set<UUID> uuidSet) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase find(UUID uuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase findWithoutFlush(UUID uuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase find(int id) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> findById(Set<Integer> idSet) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Session getSession() {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Object[]> group(Class<? extends SpecimenOrObservationBase> clazz, Integer limit, Integer start,
                List<Grouping> groups, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S extends SpecimenOrObservationBase> List<S> list(Class<S> type, Integer limit, Integer start,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase load(UUID uuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase load(int id, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase load(UUID uuid, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> load(List<UUID> uuids, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase merge(SpecimenOrObservationBase transientObject) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S extends SpecimenOrObservationBase> Pager<S> page(Class<S> type, Integer pageSize, Integer pageNumber,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UUID refresh(SpecimenOrObservationBase persistentObject) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> rows(String tableName, int limit, int start) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<UUID, SpecimenOrObservationBase> save(Collection<SpecimenOrObservationBase> newInstances) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase save(SpecimenOrObservationBase newInstance) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UUID saveOrUpdate(SpecimenOrObservationBase transientObject) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<UUID, SpecimenOrObservationBase> saveOrUpdate(
                Collection<SpecimenOrObservationBase> transientObjects) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UUID update(SpecimenOrObservationBase transientObject) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SpecimenOrObservationBase loadWithUpdate(UUID uuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> list(SpecimenOrObservationBase example, Set<String> includeProperties,
                Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteResult delete(SpecimenOrObservationBase persistentObject) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteResult delete(Collection<UUID> persistentObjectUUIDs) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> merge(List<SpecimenOrObservationBase> detachedObjects) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase> loadByIds(List<Integer> idSet, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<MergeResult<SpecimenOrObservationBase>> merge(List<SpecimenOrObservationBase> detachedObjects,
                boolean returnTransientEntity) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MergeResult<SpecimenOrObservationBase> merge(SpecimenOrObservationBase newInstance,
                boolean returnTransientEntity) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Country getCountryByIso(String iso639) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Country> getCountryByName(String name) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type,
                TaxonBase determinedAs, Integer limit, Integer start, List<OrderHint> orderHints,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type,
                TaxonName determinedAs, Integer limit, Integer start, List<OrderHint> orderHints,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<Media> getMediainHierarchy(SpecimenOrObservationBase rootOccurence, Integer pageSize,
                Integer pageNumber, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int countDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonbase) {

            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonBase,
                Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,
                Integer pageNumber, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String query,
                Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache(Integer limit, String pattern) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths)
                throws DerivedUnitFacadeNotSupportedException {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<DerivedUnitFacade> listDerivedUnitFacades(DescriptionBase description, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type,
                Set<TaxonRelationshipEdge> includeRelationships, Taxon associatedTaxon, Integer maxDepth,
                Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<SpecimenOrObservationBase> listFieldUnitsByAssociatedTaxon(Taxon associatedTaxon,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SpecimenOrObservationBase> pageFieldUnitsByAssociatedTaxon(
                Set<TaxonRelationshipEdge> includeRelationships, Taxon associatedTaxon, Integer maxDepth,
                Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type,
                Set<TaxonRelationshipEdge> includeRelationships, Taxon associatedTaxon, Integer maxDepth,
                Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<FieldUnit> getFieldUnits(UUID specimenUuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pager<SearchResult<SpecimenOrObservationBase>> findByFullText(
                Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle boundingBox,
                List<Language> languages, boolean highlightFragments, Integer pageSize, Integer pageNumber,
                List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type,
                Set<TaxonRelationshipEdge> includeRelationships, String taxonUUID, Integer maxDepth, Integer pageSize,
                Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UpdateResult moveSequence(DnaSample from, DnaSample to, Sequence sequence) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UpdateResult moveSequence(UUID fromUuid, UUID toUuid, UUID sequenceUuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UpdateResult moveDerivate(UUID specimenFromUuid, UUID specimenToUuid, UUID derivateUuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean moveDerivate(SpecimenOrObservationBase<?> from, SpecimenOrObservationBase<?> to,
                DerivedUnit derivate) {

            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FieldUnitDTO assembleFieldUnitDTO(FieldUnit fieldUnit, UUID associatedTaxonUuid) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PreservedSpecimenDTO assemblePreservedSpecimenDTO(DerivedUnit derivedUnit) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<ICdmBase> getNonCascadedAssociatedElements(SpecimenOrObservationBase<?> specimen) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteResult delete(SpecimenOrObservationBase<?> specimen, SpecimenDeleteConfigurator config) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteResult delete(UUID specimenUuid, SpecimenDeleteConfigurator config) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen,
                Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<TaxonBase<?>> listIndividualsAssociationTaxa(SpecimenOrObservationBase<?> specimen,
                Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<TaxonBase<?>> listAssociatedTaxa(SpecimenOrObservationBase<?> specimen, Integer limit,
                Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<TaxonBase<?>> listDeterminedTaxa(SpecimenOrObservationBase<?> specimen, Integer limit,
                Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<DeterminationEvent> listDeterminationEvents(SpecimenOrObservationBase<?> specimen,
                Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<TaxonBase<?>> listTypeDesignationTaxa(DerivedUnit specimen, Integer limit, Integer start,
                List<OrderHint> orderHints, List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<DerivedUnit, Collection<SpecimenTypeDesignation>> listTypeDesignations(
                Collection<DerivedUnit> specimens, Integer limit, Integer start, List<OrderHint> orderHints,
                List<String> propertyPaths) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<SpecimenTypeDesignation> listTypeDesignations(DerivedUnit specimen, Integer limit,
                Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(
                SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints,
                List<String> propertyPaths) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<DescriptionElementBase> getCharacterDataForSpecimen(SpecimenOrObservationBase<?> specimen) {

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<DescriptionElementBase> getCharacterDataForSpecimen(UUID specimenUuid) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMostSignificantIdentifier(DerivedUnit derivedUnit) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int countOccurrences(IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> config) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<SpecimenOrObservationBase<?>> getAllHierarchyDerivatives(SpecimenOrObservationBase<?> specimen) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<DerivedUnit> getAllChildDerivatives(SpecimenOrObservationBase<?> specimen) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<DerivedUnit> getAllChildDerivatives(UUID specimenUuid) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S extends SpecimenOrObservationBase> List<IdentifiedEntityDTO<S>> listByIdentifier(Class<S> clazz,
                String identifier, DefinedTerm identifierType, MatchMode matchmode, boolean includeEntity,
                List<String> propertyPaths, Integer limit) {
            // TODO Auto-generated method stub
            return null;
        }

    }


}
