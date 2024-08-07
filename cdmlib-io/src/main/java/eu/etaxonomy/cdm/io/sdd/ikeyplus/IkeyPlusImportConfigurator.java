/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.ikeyplus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author andreas
 * @since Sep 18, 2012
 */
public class IkeyPlusImportConfigurator extends ImportConfiguratorBase<IkeyPlusImportState, URI> {

    private static final Logger logger = LogManager.getLogger();

    private static IInputTransformer defaultTransformer = null;


    public static IkeyPlusImportConfigurator NewInstance(URI uri,
            ICdmDataSource destination){
        return new IkeyPlusImportConfigurator(uri, destination);
    }

    private IkeyPlusImportConfigurator(URI uri, ICdmDataSource destination) {
        super(defaultTransformer);
        setSource(uri);
        setDestination(destination);
    }

    @Override
    public IkeyPlusImportState getNewState() {
        return new IkeyPlusImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                IkeyPlusImport.class
        };
    }

    @Override
    public Reference getSourceReference() {
        //TODO
        if (this.sourceReference == null){
            logger.warn("getSource Reference not yet fully implemented");
            sourceReference = ReferenceFactory.newDatabase();
            sourceReference.setTitleCache("KeyImport", true);
        }
        return sourceReference;
    }
}