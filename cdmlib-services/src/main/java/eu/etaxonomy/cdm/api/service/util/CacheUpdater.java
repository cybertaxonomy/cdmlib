package eu.etaxonomy.cdm.api.service.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;


@Component
public class CacheUpdater implements Serializable {

    private static final long serialVersionUID = -1410600568024821771L;

    private static final Logger logger = Logger.getLogger(CacheUpdater.class);

    @Autowired
    protected INameService nameService;

    @Autowired
    protected ITaxonService taxonService;

    @Autowired
    protected  IClassificationService classificationService;

    @Autowired
    protected IReferenceService referenceService;

    @Autowired
    protected IAgentService agentService;


    @Autowired
    protected IOccurrenceService occurrenceService;

    @Autowired
    protected ITermService termService;
    @Autowired
    protected IDescriptionService descriptionService;

    @Autowired
    protected ICollectionService collectionService;
    @Autowired
    protected IFeatureTreeService featureTreeService;

    @Autowired
    protected IVocabularyService vocabularyService;

    @Autowired
    protected IPolytomousKeyService polytomousKeyService;
    @Autowired
    protected IMediaService mediaService;
    @Autowired
    protected IProgressMonitorService progressMonitorService;

	public UpdateResult doInvoke(CacheUpdaterConfigurator config) {
	    UpdateResult result = new UpdateResult();
		if (config.getClassList() == null || config.getClassList().isEmpty()){
			//!! not yet implemented
			logger.warn("Create class list from boolean values is not yet implemented for cache updater");
			createClassListFromBoolean();
		}
		config.getMonitor().beginTask("Update Caches", 100);
		//handle class list
		result = handleClassList(config.getClassList(), config.getMonitor());
		config.getMonitor().done();
		return result;
	}


	private UpdateResult handleClassList(List<Class<? extends IdentifiableEntity>> classList, IProgressMonitor monitor) {
	    UpdateResult result = new UpdateResult();
	    int ticksForSubTasks = 100/classList.size();
		for (Class<? extends IdentifiableEntity> clazz : classList){
			//WE need to separate classes , because hibernate
			//returns multiple values for service.count() for e.g. IdentifableEntity.class
			//which leads to an exception
		    UpdateResult multipleResult = handleMultiTableClasses(clazz, monitor);

			if (multipleResult == null){
			    SubProgressMonitor subMonitor= new SubProgressMonitor(monitor, ticksForSubTasks);
			    subMonitor.setTaskName("Update " + clazz.getSimpleName());
				result.includeResult(this.handleSingleTableClass(clazz, subMonitor));
			}else{
			    result.includeResult(multipleResult);
			}
		}
		return result;
	}

	private UpdateResult handleMultiTableClasses(Class<? extends IdentifiableEntity> clazz, IProgressMonitor monitor) {
		if (clazz.isAssignableFrom(IdentifiableEntity.class)){
            @SuppressWarnings("rawtypes")
            List list = Arrays.asList(new Class[]{
					DescriptionBase.class, IdentifiableMediaEntity.class,
					Media.class, Sequence.class,
					TaxonBase.class, TaxonName.class,
					Classification.class, TermBase.class
					});
			return handleClassList(list, monitor);
		}else if (clazz.isAssignableFrom(IdentifiableMediaEntity.class)){
			@SuppressWarnings("rawtypes")
            List list = Arrays.asList(new Class[]{AgentBase.class, Collection.class, Reference.class, SpecimenOrObservationBase.class});
			return handleClassList(list, monitor);
		}else if (clazz.isAssignableFrom(TermBase.class)){
			@SuppressWarnings("rawtypes")
            List list = Arrays.asList(new Class[]{DefinedTermBase.class, TermTree.class, TermVocabulary.class });
			return handleClassList(list, monitor);
		}else{
		   return null;
		}

	}

	private UpdateResult handleSingleTableClass(Class<? extends IdentifiableEntity> clazz, IProgressMonitor subMonitor) {

		UpdateResult result = new UpdateResult();
		if (clazz == null){
            return result;
        }
	    logger.info("Updating class " + clazz.getSimpleName() + " ...");

		try {
			//TermBase
			if (DefinedTermBase.class.isAssignableFrom(clazz)){
			    result.includeResult(termService.updateCaches((Class) clazz, null, null, subMonitor));
			}else if (TermTree.class.isAssignableFrom(clazz)){
			    result.includeResult(featureTreeService.updateCaches((Class) clazz, null, null, subMonitor));
			}else if (TermVocabulary.class.isAssignableFrom(clazz)){
			    result.includeResult(vocabularyService.updateCaches((Class) clazz, null, null, subMonitor));
			}
			//DescriptionBase
			else if (DescriptionBase.class.isAssignableFrom(clazz)){
			    result.includeResult(descriptionService.updateCaches((Class) clazz, null, null, subMonitor));
			}
			//Media
			else if (Media.class.isAssignableFrom(clazz)){
			    result.includeResult(mediaService.updateCaches((Class) clazz, null, null, subMonitor));
			}//TaxonBase
			else if (TaxonBase.class.isAssignableFrom(clazz)){
			    result.includeResult(taxonService.updateCaches((Class) clazz, null, null, subMonitor));
			}
			//IdentifiableMediaEntity
			else if (AgentBase.class.isAssignableFrom(clazz)){
			    result.includeResult(agentService.updateCaches((Class) clazz, null, null, subMonitor));
			}else if (Collection.class.isAssignableFrom(clazz)){
			    result.includeResult(collectionService.updateCaches((Class) clazz, null, null, subMonitor));
			}else if (Reference.class.isAssignableFrom(clazz)){
			    result.includeResult(referenceService.updateCaches((Class) clazz, null, null, subMonitor));
			}else if (SpecimenOrObservationBase.class.isAssignableFrom(clazz)){
			    result.includeResult(occurrenceService.updateCaches((Class) clazz, null, null, subMonitor));

			}
//			//Sequence  //currently not identifiable and therefore has not caches
//			else if (Sequence.class.isAssignableFrom(clazz)){
//				//TODO misuse TaxonService for sequence update, use sequence service when it exists
//				getTaxonService().updateTitleCache((Class) clazz, null, null, null);
//			}
			//TaxonName
			else if (TaxonName.class.isAssignableFrom(clazz)){

			    result.includeResult(nameService.updateCaches((Class) clazz, null, null, subMonitor));
			}
			//Classification
			else if (Classification.class.isAssignableFrom(clazz)){
			    result.includeResult(classificationService.updateCaches((Class) clazz, null, null, subMonitor));
			}
			//Polytomous Key
            else if (PolytomousKey.class.isAssignableFrom(clazz)){
                result.includeResult(polytomousKeyService.updateCaches((Class) clazz, null, null, subMonitor));

            }

			//unknown class
			else {
				String warning = "Unknown identifable entity subclass + " + clazz.getName();
				logger.error(warning);
				result.setAbort();
				result.addException(new Exception(warning));

			}
			return result;
		} catch (Exception e) {
			String warning = "Exception occurred when trying to update class + " + clazz.getName();
			warning += " Exception was: " + e.getMessage();
			logger.error(warning);
			e.printStackTrace();
			result.setAbort();
			result.addException(e);
			return result;
		}
	}

	private void createClassListFromBoolean() {
		logger.warn("Create class list from boolean not yet implemented. Can't run cache updater");
	}





}
