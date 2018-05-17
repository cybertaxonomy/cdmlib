/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public abstract class DbExportStateBase<CONFIG extends DbExportConfiguratorBase<? extends DbExportStateBase<CONFIG, TRANSFORM>, TRANSFORM, Source>, TRANSFORM extends IExportTransformer>
        extends ExportStateBase<CONFIG, TRANSFORM, Source> {
	
    private static final Logger logger = Logger.getLogger(DbExportStateBase.class);

	protected Map<UUID, Integer> dbIdMap = new HashMap<UUID, Integer>();


	public DbExportStateBase(CONFIG config) {
		super(config);
	}

	public void putDbId(CdmBase cdmBase, int dbId){
		if (cdmBase != null){
			dbIdMap.put(cdmBase.getUuid(), dbId);
		}else{
			logger.warn("CdmBase was (null) and could not be added to dbIdMap");
		}
	}

	public Integer getDbId(CdmBase cdmBase){
		if (cdmBase != null){
			return dbIdMap.get(cdmBase.getUuid());
		}else{
			logger.warn("CdmBase was (null). No entries in dbIdMap available");
			return null;
		}
	}

}
