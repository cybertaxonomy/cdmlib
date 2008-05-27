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
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
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
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTermService()
	 */
	public ITermService getTermService() {
		return this.termService;
	}

}
