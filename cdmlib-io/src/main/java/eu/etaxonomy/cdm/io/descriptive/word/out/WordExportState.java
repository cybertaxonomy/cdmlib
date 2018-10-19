/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.word.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;

/**
 *
 * @author pplitzner
 * @since Oct 18, 2018
 *
 */
public class WordExportState extends XmlExportState<WordExportConfigurator>{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(WordExportState.class);

    public WordExportState(WordExportConfigurator config) {
        super(config);
    }



}
