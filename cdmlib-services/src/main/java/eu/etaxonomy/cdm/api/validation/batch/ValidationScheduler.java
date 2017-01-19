/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.validation.batch;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * @author a.mueller
 * @date 19.02.2015
 *
 */
@Component
public class ValidationScheduler extends ThreadPoolTaskScheduler {
    private static final long serialVersionUID = 7110200522760862056L;

    public ValidationScheduler(){}
}
