/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.util;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.longrunningService.CacheUpdater;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseShortSecCacheStrategy;

//@Component
public class CacheUpdaterWithNewCacheStrategy extends CacheUpdater {

    private static final long serialVersionUID = 7667521864001167289L;
    private static final Logger logger = Logger.getLogger(CacheUpdaterWithNewCacheStrategy.class);

	private boolean handleSingleTableClass(Class<? extends IdentifiableEntity> clazz) {
		logger.warn("Updating class " + clazz.getSimpleName() + " ...");
		try {
			//TermBase
			if (DefinedTermBase.class.isAssignableFrom(clazz)){
				termService.updateCaches((Class) clazz, null, null, null);
			}else if (TermTree.class.isAssignableFrom(clazz)){
				termTreeService.updateCaches((Class) clazz, null, null, null);
			}else if (TermVocabulary.class.isAssignableFrom(clazz)){
				vocabularyService.updateCaches((Class) clazz, null, null, null);
			}
			//DescriptionBase
			else if (DescriptionBase.class.isAssignableFrom(clazz)){
				descriptionService.updateCaches((Class) clazz, null, null, null);
			}
			//Media
			else if (Media.class.isAssignableFrom(clazz)){
				mediaService.updateCaches((Class) clazz, null, null, null);
			}//TaxonBase
			else if (TaxonBase.class.isAssignableFrom(clazz)){
				TaxonBaseShortSecCacheStrategy<TaxonBase> cacheStrategy = new TaxonBaseShortSecCacheStrategy<TaxonBase>();
				taxonService.updateCaches((Class) clazz, null,cacheStrategy , null);
			}
			//IdentifiableMediaEntity
			else if (AgentBase.class.isAssignableFrom(clazz)){
				agentService.updateCaches((Class) clazz, null, null, null);
			}else if (Collection.class.isAssignableFrom(clazz)){
				collectionService.updateCaches((Class) clazz, null, null, null);
			}else if (Reference.class.isAssignableFrom(clazz)){
				referenceService.updateCaches((Class) clazz, null, null, null);
			}else if (SpecimenOrObservationBase.class.isAssignableFrom(clazz)){
				occurrenceService.updateCaches((Class) clazz, null, null, null);
			}
			//Sequence
			else if (Sequence.class.isAssignableFrom(clazz)){
				//TODO misuse TaxonServic for sequence update, use sequence service when it exists
				taxonService.updateCaches((Class) clazz, null, null, null);
			}
			//TaxonName
			else if (TaxonName.class.isAssignableFrom(clazz)){
				nameService.updateCaches((Class) clazz, null, null, null);
			}
			//Classification
			else if (Classification.class.isAssignableFrom(clazz)){
				classificationService.updateCaches((Class) clazz, null, null, null);
			}
			//unknown class
			else {
				String warning = "Unknown identifable entity subclass + " + clazz.getName();
				logger.error(warning);
				return false;
				//getTaxonService().updateTitleCache((Class) clazz);
			}
			return true;
		} catch (Exception e) {
			String warning = "Exception occurred when trying to update class + " + clazz.getName();
			warning += " Exception was: " + e.getMessage();
			logger.error(warning);
			e.printStackTrace();
			return false;
		}
	}
}