/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;


/**
 * @author a.mueller
 * @created 18.04.2011
 */
public class OwlExportConfigurator extends XmlExportConfiguratorBase<OwlExportState> {

    private static final long serialVersionUID = -4360021755279592592L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OwlExportConfigurator.class);

    private FeatureTree featureTree;


    private OwlExportConfigurator(ICdmDataSource source, File destinationFolder, FeatureTree featureTree) {
        super(destinationFolder, source, null);
        this.featureTree = featureTree;
    }

    public static OwlExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder, FeatureTree featureTree) {
        return new OwlExportConfigurator(source, destinationFolder, featureTree);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OwlExportState getNewState() {
        return new OwlExportState(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                OwlExport.class
        };
    }

    public FeatureTree getFeatureTree() {
        return featureTree;
    }

}
