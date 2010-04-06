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

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase.IdType;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public class PesiExportState extends DbExportStateBase<PesiExportConfigurator>{
	private static final Logger logger = Logger.getLogger(PesiExportState.class);

	/**
	 * @param config
	 */
	public PesiExportState(PesiExportConfigurator config) {
		super(config);
	}

	@Override
	public Integer getDbId(CdmBase cdmBase) {
		if (cdmBase != null) {
			IdType type = getConfig().getIdType();
			if (type == IdType.CDM_ID) {
				return cdmBase.getId();
			} else {
				return dbIdMap.get(cdmBase.getUuid());
			}
		} else {
			logger.warn("CdmBase was (null). No entries in dbIdMap available");
			return null;
		}
	}

	/**
	 * Removes a {@link CdmBase CdmBase} entry from this state's {@link java.util.Map Map}.
	 * @param cdmBase The {@link CdmBase CdmBase} to be deleted.
	 * @return Whether deletion was successful or not.
	 */
	public boolean removeDbId(CdmBase cdmBase) {
		if (cdmBase != null) {
			IdType type = getConfig().getIdType();
			if (type != IdType.CDM_ID) {
				dbIdMap.remove(cdmBase.getUuid());
				return true;
			} else {
				return false;
			}
		} else {
			logger.warn("CdmBase was (null). No entries in dbIdMap available");
			return false;
		}
	}

}
