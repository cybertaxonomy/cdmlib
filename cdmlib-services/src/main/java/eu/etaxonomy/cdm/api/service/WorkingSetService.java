package eu.etaxonomy.cdm.api.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.description.IWorkingSetDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

@Service
@Transactional(readOnly = false)
public class WorkingSetService extends
		AnnotatableServiceBase<WorkingSet, IWorkingSetDao> implements IWorkingSetService {

    private static Logger logger = Logger.getLogger(WorkingSetService.class);

    @Autowired
    private IOccurrenceService occurrenceService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    @Autowired
    private IProgressMonitorService progressMonitorService;

	@Override
	@Autowired
	protected void setDao(IWorkingSetDao dao) {
		this.dao = dao;
	}

	@Override
    public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,
			List<String> propertyPaths) {
		return dao.getDescriptionElements(workingSet, features, pageSize, pageNumber, propertyPaths);
	}

	@Override
	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(
			Class<T> clazz, UUID workingSetUuid, DescriptiveSystemRole role) {
		return dao.getTaxonFeatureDescriptionElementMap(clazz, workingSetUuid, role);
	}

	@Override
    public List<UuidAndTitleCache<WorkingSet>> getWorkingSetUuidAndTitleCache(Integer limitOfInitialElements, String pattern) {
        return dao.getWorkingSetUuidAndTitleCache( limitOfInitialElements, pattern);
    }


    @Override
    @Transactional
    public UUID monitGetRowWrapper(WorkingSet workingSet) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                return getRowWrapper(workingSet, monitor);
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }

	@Override
	public ArrayList<RowWrapperDTO> getRowWrapper(WorkingSet workingSet, IProgressMonitor monitor) {
	    monitor.beginTask("Load row wrapper", workingSet.getDescriptions().size());
	    ArrayList<RowWrapperDTO> wrappers = new ArrayList<>();
	    Set<DescriptionBase> descriptions = workingSet.getDescriptions();
	    for (DescriptionBase description : descriptions) {
            if(monitor.isCanceled()){
                return new ArrayList<>();
            }
            wrappers.add(createRowWrapper(null, description, null, workingSet));
            monitor.worked(1);
        }
	    return wrappers;
	}

    @Override
    public Collection<SpecimenNodeWrapper> loadSpecimens(WorkingSet workingSet){

        List<SpecimenNodeWrapper> specimenCache = new ArrayList<>();
        //set filter parameters
        TaxonNodeFilter filter = TaxonNodeFilter.NewRankInstance(workingSet.getMinRank(), workingSet.getMaxRank());
        workingSet.getGeoFilter().forEach(area -> filter.orArea(area.getUuid()));
        workingSet.getTaxonSubtreeFilter().forEach(node -> filter.orSubtree(node));
        filter.setIncludeUnpublished(true);

        List<UUID> filteredNodes = taxonNodeService.uuidList(filter);
        return occurrenceService.listUuidAndTitleCacheByAssociatedTaxon(filteredNodes, null, null, null);
//        for (UUID uuid : filteredNodes) {
//            //TODO implement occurrence service for taxon nodes
//            // let it return UuidAndTitleCache
//            TaxonNode taxonNode = taxonNodeService.load(uuid);
//            Taxon taxon = taxonNode.getTaxon();
//            if(taxon!=null){
//                FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
//                config.setAssociatedTaxonUuid(taxon.getUuid());
////                List<UuidAndTitleCache<SpecimenOrObservationBase>> list = occurrenceService.findByTitleUuidAndTitleCache(config).getRecords();
//                List<SpecimenOrObservationBase> list =
//                        occurrenceService.pageByAssociatedTaxon(SpecimenOrObservationBase.class,
//                                null, taxon, null, null, null, null, null).getRecords();
//                list.forEach(specimen ->{
//                    specimenCache.add(new SpecimenNodeWrapper(new UuidAndTitleCache<>(specimen.getUuid(), specimen.getId(), specimen.getTitleCache()), taxonNode));
//                });
//            }
//        }
//        return specimenCache;
    }

    @Override
    public RowWrapperDTO createRowWrapper(DescriptionBase description, WorkingSet workingSet){
        return createRowWrapper(null, description, null, workingSet);
    }

    @Override
    public RowWrapperDTO createRowWrapper(SpecimenOrObservationBase specimen, WorkingSet workingSet){
        return createRowWrapper(specimen, null, null, workingSet);
    }

	private RowWrapperDTO createRowWrapper(SpecimenOrObservationBase specimen, DescriptionBase description, TaxonNode taxonNode, WorkingSet workingSet){
	    if(description!=null){
	        specimen = description.getDescribedSpecimenOrObservation();
	    }
        FieldUnit fieldUnit = null;
        String identifier = null;
        NamedArea country = null;
        //supplemental information
        if(specimen!=null){
            if(taxonNode==null){
                Collection<TaxonBase<?>> associatedTaxa = occurrenceService.listAssociatedTaxa(specimen, null, null, null,
                        Arrays.asList(new String[]{
                                "taxonNodes",
                                "taxonNodes.classification",
                        }));
                if(associatedTaxa!=null){
                    //FIXME: what about multiple associated taxa
                    Set<TaxonNode> taxonSubtreeFilter = workingSet.getTaxonSubtreeFilter();
                    if(taxonSubtreeFilter!=null && !taxonSubtreeFilter.isEmpty()){
                        Taxon taxon = HibernateProxyHelper.deproxy(associatedTaxa.iterator().next(), Taxon.class);
                        taxonNode = taxon.getTaxonNode(taxonSubtreeFilter.iterator().next().getClassification());
                    }
                }
            }
            Collection<FieldUnit> fieldUnits = occurrenceService.getFieldUnits(specimen.getUuid(),
                    Arrays.asList(new String[]{
                            "gatheringEvent",
                            "gatheringEvent.country"
                            }));
            if(fieldUnits.size()!=1){
                logger.error("More than one or no field unit found for specimen"); //$NON-NLS-1$
            }
            else{
                fieldUnit = fieldUnits.iterator().next();
            }
            if(specimen instanceof DerivedUnit){
                identifier = occurrenceService.getMostSignificantIdentifier(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class));
            }
            if(fieldUnit!=null && fieldUnit.getGatheringEvent()!=null){
                country = fieldUnit.getGatheringEvent().getCountry();
            }
        }
        return new RowWrapperDTO(description, specimen, taxonNode, fieldUnit, identifier, country);
	}
}
