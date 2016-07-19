// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.gbif.in;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author k.luther
 * @date 15.07.2016
 *
 */
public class GbifImportConfigurator<GbifImportState> extends SpecimenImportConfiguratorBase{

    /**
     * @param transformer
     */
    public GbifImportConfigurator(IInputTransformer transformer) {
        super(transformer);
        // TODO Auto-generated constructor stub
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
     */
    @Override
    protected void makeIoClassList() {
        System.out.println("makeIOClassList");
        ioClassList = new Class[]{
               GbifImport.class,
        };

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
     */
    @Override
    public Reference getSourceReference() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
     */
    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        // TODO Auto-generated method stub
        return null;
    }

}
