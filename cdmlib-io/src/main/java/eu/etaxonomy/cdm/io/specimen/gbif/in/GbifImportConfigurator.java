/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.gbif.in;

import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author k.luther
 * @since 15.07.2016
 */
public class GbifImportConfigurator<GbifImportState, InputStream> extends SpecimenImportConfiguratorBase {

    private static final long serialVersionUID = -6574012865739879532L;

    private static IInputTransformer defaultTransformer = null;

    public GbifImportConfigurator(IInputTransformer transformer) {
        super(transformer);
        // TODO Auto-generated constructor stub
    }

    public static GbifImportConfigurator newInstance(OccurenceQuery query){
        GbifImportConfigurator newInstance = new GbifImportConfigurator<>(defaultTransformer);
        newInstance.setOccurenceQuery(query);
        return newInstance;
    }

    @Override
    protected void makeIoClassList() {
        System.out.println("makeIOClassList");
        ioClassList = new Class[]{
               GbifImport.class,
        };
    }

    @Override
    public Reference getSourceReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpecimenImportStateBase getNewState() {
        SpecimenImportStateBase<?,?> state = new SpecimenImportStateBase(this);
        return state;
    }
}