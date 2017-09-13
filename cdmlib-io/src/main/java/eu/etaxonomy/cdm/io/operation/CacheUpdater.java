package eu.etaxonomy.cdm.io.operation;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.operation.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


@Component
public class CacheUpdater extends CdmImportBase<CacheUpdaterConfigurator, DefaultImportState<CacheUpdaterConfigurator>> {

    private static final long serialVersionUID = -1410600568024821771L;

    private static final Logger logger = Logger.getLogger(CacheUpdater.class);

	@Override
	protected void doInvoke(DefaultImportState<CacheUpdaterConfigurator> state) {
		CacheUpdaterConfigurator config = state.getConfig();
		if (config.getClassList() == null || config.getClassList().isEmpty()){
			//!! not yet implemented
			logger.warn("Create class list from boolean values is not yet implemented for cache updater");
			createClassListFromBoolean();
		}

		//handle class list
		handleClassList(config.getClassList());

		return;
	}


	private boolean handleClassList(List<Class<? extends IdentifiableEntity>> classList) {
		boolean result = true;
		for (Class<? extends IdentifiableEntity> clazz : classList){
			//WE need to separate classes , because hibernate
			//returns multiple values for service.count() for e.g. IdentifableEntity.class
			//which leads to an exception
			if (! handleMultiTableClasses(clazz)){
				result &= this.handleSingleTableClass(clazz);
			}
		}
		return result;
	}

	private boolean handleMultiTableClasses(Class<? extends IdentifiableEntity> clazz) {
		if (clazz.isAssignableFrom(IdentifiableEntity.class)){
			List list = Arrays.asList(new Class[]{
					DescriptionBase.class, IdentifiableMediaEntity.class,
					Media.class, Sequence.class,
					TaxonBase.class, TaxonName.class,
					Classification.class, TermBase.class
					});
			handleClassList(list);
		}else if (clazz.isAssignableFrom(IdentifiableMediaEntity.class)){
			List list = Arrays.asList(new Class[]{AgentBase.class, Collection.class, Reference.class, SpecimenOrObservationBase.class});
			handleClassList(list);
		}else if (clazz.isAssignableFrom(TermBase.class)){
			List list = Arrays.asList(new Class[]{DefinedTermBase.class, FeatureTree.class, TermVocabulary.class });
			handleClassList(list);
		}else{
			return false;
		}
		return true;
	}

	private boolean handleSingleTableClass(Class<? extends IdentifiableEntity> clazz) {
		logger.warn("Updating class " + clazz.getSimpleName() + " ...");
		try {
			//TermBase
			if (DefinedTermBase.class.isAssignableFrom(clazz)){
				getTermService().updateTitleCache((Class) clazz, null, null, null);
			}else if (FeatureTree.class.isAssignableFrom(clazz)){
				getFeatureTreeService().updateTitleCache((Class) clazz, null, null, null);
			}else if (TermVocabulary.class.isAssignableFrom(clazz)){
				getVocabularyService().updateTitleCache((Class) clazz, null, null, null);
			}
			//DescriptionBase
			else if (DescriptionBase.class.isAssignableFrom(clazz)){
				getDescriptionService().updateTitleCache((Class) clazz, null, null, null);
			}
			//Media
			else if (Media.class.isAssignableFrom(clazz)){
				getMediaService().updateTitleCache((Class) clazz, null, null, null);
			}//TaxonBase
			else if (TaxonBase.class.isAssignableFrom(clazz)){
				getTaxonService().updateTitleCache((Class) clazz, null, null, null);
			}
			//IdentifiableMediaEntity
			else if (AgentBase.class.isAssignableFrom(clazz)){
				getAgentService().updateTitleCache((Class) clazz, null, null, null);
			}else if (Collection.class.isAssignableFrom(clazz)){
				getCollectionService().updateTitleCache((Class) clazz, null, null, null);
			}else if (Reference.class.isAssignableFrom(clazz)){
				getReferenceService().updateTitleCache((Class) clazz, null, null, null);
			}else if (SpecimenOrObservationBase.class.isAssignableFrom(clazz)){
				getOccurrenceService().updateTitleCache((Class) clazz, null, null, null);
			}
			//Sequence
			else if (Sequence.class.isAssignableFrom(clazz)){
				//TODO misuse TaxonServic for sequence update, use sequence service when it exists
				getTaxonService().updateTitleCache((Class) clazz, null, null, null);
			}
			//TaxonName
			else if (TaxonName.class.isAssignableFrom(clazz)){
				getNameService().updateTitleCache((Class) clazz, null, null, null);
			}
			//Classification
			else if (Classification.class.isAssignableFrom(clazz)){
				getClassificationService().updateTitleCache((Class) clazz, null, null, null);
			}
			//unknown class
			else {
				String warning = "Unknown identifable entity subclass + " + clazz == null ? "null" : clazz.getName();
				logger.error(warning);
				return false;
				//getTaxonService().updateTitleCache((Class) clazz);
			}
			return true;
		} catch (Exception e) {
			String warning = "Exception occurred when trying to update class + " + clazz == null ? "null" : clazz.getName();
			warning += " Exception was: " + e.getMessage();
			logger.error(warning);
			e.printStackTrace();
			return false;
		}
	}

	private void createClassListFromBoolean() {
		logger.warn("Create class list from boolean not yet implemented. Can't run cache updater");
	}


// ************* inherited form CdmIoBase but not needed here ********************/

	@Override
	protected boolean doCheck(DefaultImportState<CacheUpdaterConfigurator> state) {
		//not needed here
		return false;
	}

	@Override
	protected boolean isIgnore(DefaultImportState<CacheUpdaterConfigurator> state) {
		//not needed here
		return false;
	}

}
