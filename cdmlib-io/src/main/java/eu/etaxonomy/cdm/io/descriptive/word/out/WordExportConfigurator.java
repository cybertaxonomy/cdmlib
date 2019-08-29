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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.model.term.TermTree;


/**
 *
 * @author pplitzner
 * @since Oct 18, 2018
 *
 */
public class WordExportConfigurator extends XmlExportConfiguratorBase<WordExportState> {

    private static final long serialVersionUID = -4360021755279592592L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WordExportConfigurator.class);

    private TermTree featureTree;


    private WordExportConfigurator(ICdmDataSource source, File destinationFolder, TermTree featureTree) {
        super(destinationFolder, source, null);
        this.featureTree = featureTree;
    }

    public static WordExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder, TermTree featureTree) {
        return new WordExportConfigurator(source, destinationFolder, featureTree);
    }

    @Override
    public WordExportState getNewState() {
        return new WordExportState(this);
    }

    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                WordExport.class
        };
    }

    public TermTree getFeatureTree() {
        return featureTree;
    }

}
