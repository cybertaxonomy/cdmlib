// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public class PesiExportState extends DbExportStateBase<PesiExportConfigurator>{
	private static final Logger logger = Logger.getLogger(PesiExportState.class);
	private static List<Integer> processedSourceList = new ArrayList<Integer>();

	/**
	 * @param config
	 */
	public PesiExportState(PesiExportConfigurator config) {
		super(config);
	}
	

	/**
	 * Stores the Datawarehouse.id to a specific CDM object originally.
	 * Does nothing now since we do not want to store Cdm.id/Datawarehouse.id pairs. This saves precious memory.
	 * @param cdmBase
	 * @param dbId
	 */
	@Override
	public void putDbId(CdmBase cdmBase, int dbId) {
		// Do nothing
	}

	/**
	 * Gets the Datawarehouse.id to a specific CDM object originally.
	 * Here it just returns the CDM object's id.
	 * @param cdmBase
	 * @return
	 */
	@Override
	public Integer getDbId(CdmBase cdmBase) {
		// We use the Cdm.id for Datawarehouse.id
		if (cdmBase == null) {
			return null;
		} else {
			return cdmBase.getId();
		}
	}
	
	/**
	 * Returns whether the given Source object was processed before or not.
	 * @param
	 * @return
	 */
	public boolean alreadyProcessedSource(Integer sourceId) {
		if (processedSourceList.contains(sourceId)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Adds given Source to the list of processed Sources.
	 */
	public boolean addToProcessedSources(Integer sourceId) {
		if (! processedSourceList.contains(sourceId)) {
			processedSourceList.add(sourceId);
		}
		
		return true;
	}

	/**
	 * Clears the list of already processed Sources.
	 */
	public void clearAlreadyProcessedSources() {
		processedSourceList.clear();
	}

}
