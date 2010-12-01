// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.transaction.PlatformTransactionManager;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IIdentificationKeyService;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.IWorkingSetService;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
public interface ICdmApplicationConfiguration {

	/**
	 * @return
	 */
	public INameService getNameService();

	/**
	 * @return
	 */
	public ITaxonService getTaxonService();

	/**
	 * @return
	 */
	public IClassificationService getClassificationService();
	
	/**
	 * 
	 * @return
	 */
	public ITaxonNodeService getTaxonNodeService();
	
	/**
	 * @return
	 */
	public IReferenceService getReferenceService();
	
	/**
	 * @return
	 */
	public IAgentService getAgentService();
	
	/**
	 * @return
	 */
	public IDescriptionService getDescriptionService();
	
	/**
	 * @return
	 */
	public IOccurrenceService getOccurrenceService();
	
	/**
	 * @return
	 */
	public IMediaService getMediaService();
	
	/**
	 * @return
	 */
	public IDatabaseService getDatabaseService();
	
	/**
	 * @return
	 */
	public ITermService getTermService();

	/**
	 * @return
	 */
	public ICommonService getCommonService();
	
	/**
	 * 
	 * @return
	 */
	public ILocationService getLocationService();
	
	/**
	 * 
	 * @return
	 */
	public IUserService getUserService();
	
	
	/**
	 * @return
	 */
	public IService<CdmBase> getMainService();
	
	
	/**
	 * @return
	 */
	public IWorkingSetService getWorkingSetService();
	
	/**
	 * @return
	 */
	public PlatformTransactionManager getTransactionManager();
	
	
	/**
	 * 
	 * @return
	 */
	public ProviderManager getAuthenticationManager();
	
	/**
	 * @return
	 */
	public ConversationHolder NewConversation();

	/**
	 * 
	 * @return
	 */
	public ICollectionService getCollectionService();

	/**
	 * 
	 * @return
	 */
	public IFeatureTreeService getFeatureTreeService();

	/**
	 * 
	 * @return
	 */
	public IFeatureNodeService getFeatureNodeService();
	
	/**
	 * 
	 * @return
	 */
	public IVocabularyService getVocabularyService();
	
	/**
	 * @return
	 */
	public IIdentificationKeyService getIdentificationKeyService();
	
	/**
	 * @return
	 */
	public IPolytomousKeyService getPolytomousKeyService();
}
