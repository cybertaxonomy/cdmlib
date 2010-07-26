// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.sapindaceae;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class SapindaceaeImportState extends ImportStateBase<SapindaceaeImportConfigurator, SapindaceaeImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SapindaceaeImportState.class);
	
	private UnmatchedLeads openKeys;



	public SapindaceaeImportState(SapindaceaeImportConfigurator config) {
		super(config);
		setTransformer(new SapindaceaTransformer());
	}

	public UnmatchedLeads getOpenKeys() {
		return openKeys;
	}

	public void setOpenKeys(UnmatchedLeads openKeys) {
		this.openKeys = openKeys;
	}


}