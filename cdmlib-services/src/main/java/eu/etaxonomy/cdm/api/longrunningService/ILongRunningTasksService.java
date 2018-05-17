/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.longrunningService;

import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;

/**
 * @author cmathew
 * @since 31 Jul 2015
 *
 */
public interface ILongRunningTasksService {


    /**
     * @param configurator
     * @return
     */
    public UUID monitLongRunningTask(SecundumForSubtreeConfigurator configurator);



}
