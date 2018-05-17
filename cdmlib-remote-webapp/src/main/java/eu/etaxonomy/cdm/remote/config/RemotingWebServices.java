/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * @author a.kohlbecker
 * @since Feb 25, 2016
 *
 */

@Profile("remoting")
@Configuration
@ImportResource(locations="classpath:/eu/etaxonomy/cdm/remoting-services.xml")
public class RemotingWebServices {

    public static final Logger logger = Logger.getLogger(RemotingWebServices.class);

    public RemotingWebServices() {
        logger.info("========================================");
        logger.info(" Spring Configuration Profile: Remoting");
        logger.info("========================================");
    }
}
