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

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;

/**
 * @author cmathew
 * @date 31 Jul 2015
 *
 */
public interface IIOService {

    public ExportResult export(IExportConfigurator configurator);

}
