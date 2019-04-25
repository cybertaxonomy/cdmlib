/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import java.net.URI;

import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class OwlImportConfigurator extends ImportConfiguratorBase<OwlImportState, URI> {

    private static final long serialVersionUID = -7981427548996602252L;

    public static OwlImportConfigurator NewInstance(URI source){
        return new OwlImportConfigurator(source);
    }

    protected OwlImportConfigurator(URI source) {
        super(null);
        this.setSource(source);
    }

    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        return null;
    }

    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                OwlImport.class
        };
    }

    @Override
    public Reference getSourceReference() {
        return null;
    }

}
