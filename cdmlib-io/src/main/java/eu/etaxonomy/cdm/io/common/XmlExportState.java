/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class XmlExportState<CONFIG extends XmlExportConfiguratorBase<?>>
            extends ExportStateBase<CONFIG, IExportTransformer, File> {

    private static final Logger logger = LogManager.getLogger();

	private Map<UUID, String> xmlIdMap = new HashMap<>();


	public XmlExportState(CONFIG config) {
		super(config);
	}

	public void putDbId(CdmBase cdmBase, String xmlId){
		if (cdmBase != null){
			xmlIdMap.put(cdmBase.getUuid(), xmlId);
		}else{
			logger.warn("CdmBase was (null) and could not be added to xmlIdMap");
		}
	}

	public String getDbId(CdmBase cdmBase){
		if (cdmBase != null){
			return xmlIdMap.get(cdmBase.getUuid());
		}else{
			logger.warn("CdmBase was (null). No entries in xmlIdMap available");
			return null;
		}
	}

}
