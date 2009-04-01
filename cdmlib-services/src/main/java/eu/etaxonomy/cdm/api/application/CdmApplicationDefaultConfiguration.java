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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
@Component
public class CdmApplicationDefaultConfiguration implements ICdmApplicationConfiguration {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmApplicationDefaultConfiguration.class);

	@Autowired
	//@Qualifier("nameService")
	private INameService nameService;
	@Autowired
	//@Qualifier("taxonService")
	private ITaxonService taxonService;
	@Autowired
	//@Qualifier("referenceService")
	private IReferenceService referenceService;
	@Autowired
	//@Qualifier("agentService")
	private IAgentService agentService;
	@Autowired
	//@Qualifier("databaseService")
	private IDatabaseService databaseService;
	@Autowired
	//@Qualifier("termService")
	private ITermService termService;
	@Autowired
	private HibernateTransactionManager transactionManager;
	@Autowired
	//@Qualifier("descriptionService")
	private IDescriptionService descriptionService;
	@Autowired
	//@Qualifier("occurrenceService")
	private IOccurrenceService occurrenceService;
	@Autowired
	//@Qualifier("mediaService")
	private IMediaService mediaService;
	@Autowired
	//@Qualifier("commonService")
	private ICommonService commonService;
//	@Autowired
	//@Qualifier("mainService")
	private IService<CdmBase> mainService;
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private DataSource dataSource;
	
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
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getCommonService()
	 */
	public IService<CdmBase> getMainService(){
		return this.mainService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTransactionManager()
	 */
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#NewConversation()
	 */
	public ConversationHolder NewConversation() {
		// TODO make this a prototype
		return new ConversationHolder(dataSource, sessionFactory, transactionManager);
	}

	
}
