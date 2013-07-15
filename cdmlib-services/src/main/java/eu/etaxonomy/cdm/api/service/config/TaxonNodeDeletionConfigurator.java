// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;

/**
 * This class is used to configure taxon node deletion.
 * 
 * @see ITaxonNodeService#delete(eu.etaxonomy.cdm.model.taxon.TaxonNode)
 * 
 * @author a.mueller
 * @date 09.11.2011
 *
 */
public class TaxonNodeDeletionConfigurator extends DeleteConfiguratorBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNodeDeletionConfigurator.class);


	public enum ChildHandling{
		DELETE,
		MOVE_TO_PARENT
	}
	

	private ChildHandling childHandling = ChildHandling.DELETE;
	private boolean deleteInAllClassifications = true;


	public boolean isDeleteInAllClassifications() {
		return deleteInAllClassifications;
	}


	public void setDeleteInAllClassifications(boolean deleteInAllClassifications) {
		this.deleteInAllClassifications = deleteInAllClassifications;
	}


	public void setChildHandling(ChildHandling childHandling) {
		this.childHandling = childHandling;
	}


	public ChildHandling getChildHandling() {
		return childHandling;
	}
	
	
}
