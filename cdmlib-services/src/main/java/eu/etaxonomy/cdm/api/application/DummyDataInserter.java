/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import org.springframework.context.event.ContextRefreshedEvent;

/**
 * This is an empty dummy implementation of the AbstractDataInserter
 * which can be used in situations where inserting project or application specific
 * data is optional. In this case the spring configuration will either include the according
 * AbstractDataInserter implementation or not, depending on environment parameters.
 * AbstractDataInserter implements ApplicationListener and thus must not be null,
 * therefore it is not possible to supply <code>null</code> instead of the specific
 * data inserter. The DummyDataInserter solves this problems and should be returned instead of <code>null</code>.
 *
 * @author a.kohlbecker
 * @since Jul 27, 2017
 *
 */
public class DummyDataInserter extends AbstractDataInserter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // the dummy does nothing
    }

}
