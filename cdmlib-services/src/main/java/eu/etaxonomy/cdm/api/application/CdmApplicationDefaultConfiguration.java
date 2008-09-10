/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
@Component
public class CdmApplicationDefaultConfiguration implements ICdmApplicationConfiguration {
	private static Logger logger = Logger.getLogger(CdmApplicationDefaultConfiguration.class);

	@Autowired
	private INameService nameService;
	@Autowired
	private ITaxonService taxonService;
	@Autowired
	private IReferenceService referenceService;
	@Autowired
	private IAgentService agentService;
	@Autowired
	private IDatabaseService databaseService;
	@Autowired
	private ITermService termService;
	@Autowired
	private HibernateTransactionManager transactionManager;
	@Autowired
	private IDescriptionService descriptionService;
	@Autowired
	private IOccurrenceService occurrenceService;
	@Autowired
	private IMediaService mediaService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private ICollectionService collectionService;

	
	/**
	 * 
	 */
	public CdmApplicationDefaultConfiguration() {
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getAgentService()
	 */
	public IAgentService getAgentService() {
		return this.agentService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getDatabaseService()
	 */
	public IDatabaseService getDatabaseService() {
		return this.databaseService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getNameService()
	 */
	public INameService getNameService() {
		return this.nameService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getReferenceService()
	 */
	public IReferenceService getReferenceService() {
		return this.referenceService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTaxonService()
	 */
	public ITaxonService getTaxonService() {
		return this.taxonService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getDescriptionService()
	 */
	public IDescriptionService getDescriptionService(){
		return this.descriptionService;
	}

	public IOccurrenceService getOccurrenceService(){
		return this.occurrenceService;
	}

	public IMediaService getMediaService(){
		return this.mediaService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTermService()
	 */
	public ITermService getTermService() {
		return this.termService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getCommonService()
	 */
	public ICommonService getCommonService(){
		return this.commonService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTransactionManager()
	 */
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getCollectionService()
	 */
	public ICollectionService getCollectionService(){
		return this.collectionService;
	}
	
}
