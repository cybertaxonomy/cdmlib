/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.word.out;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author pplitzner
 * @since Oct 18, 2018
 */
public class WordTermExportConfigurator extends XmlExportConfiguratorBase<WordTermExportState> {

    private static final long serialVersionUID = -4360021755279592592L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private TermTree<?> featureTree;


    private WordTermExportConfigurator(ICdmDataSource source, File destinationFolder, TermTree featureTree) {
        super(destinationFolder, source, null);
        this.featureTree = featureTree;
    }

    public static WordTermExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder, TermTree featureTree) {
        return new WordTermExportConfigurator(source, destinationFolder, featureTree);
    }

    @Override
    public WordTermExportState getNewState() {
        return new WordTermExportState(this);
    }

    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                WordTermExport.class
        };
    }

    public TermTree getFeatureTree() {
        return featureTree;
    }

}
