// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;

/**
 * @author cmathew
 * @date 31 Jul 2015
 *
 */
@Service
@Transactional(readOnly = true)
public class IOServiceImpl implements IIOService {

    @Autowired
    CdmApplicationAwareDefaultExport cdmExport;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.service.IExportService#export(eu.etaxonomy.cdm.io.common.IExportConfigurator)
     */
    @Override
    public ExportResult export(IExportConfigurator config) {
        config.setTarget(TARGET.EXPORT_DATA);
        return cdmExport.execute(config);
    }

}
