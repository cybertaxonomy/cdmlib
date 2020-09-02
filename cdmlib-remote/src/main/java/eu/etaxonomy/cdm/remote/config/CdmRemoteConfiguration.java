/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.remote.controller.util.IMediaToolbox;
import eu.etaxonomy.cdm.remote.controller.util.MediaToolbox;

/**
 * This configuration may completely replace the src/main/resources/eu/etaxonomy/cdm/remote.xml in future
 *
 * @author a.kohlbecker
 * @since Jun 3, 2019
 */
@Configuration
public class CdmRemoteConfiguration {

    @Bean
    public IMediaToolbox mediaToolbox() {
        return new MediaToolbox();
    }
}