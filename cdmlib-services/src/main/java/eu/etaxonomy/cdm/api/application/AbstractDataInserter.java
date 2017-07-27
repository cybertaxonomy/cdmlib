/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author a.kohlbecker
 * @since Jul 27, 2017
 *
 */
public abstract class AbstractDataInserter extends RunAsAuthenticator implements ApplicationListener<ContextRefreshedEvent> {

}
