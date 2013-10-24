// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.biocase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author pplitzner
 * @date 16.09.2013
 *
 */
public class BioCaseResponseSchemaAdapter extends SchemaAdapterBase<SpecimenOrObservationBase> {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.ext.common.SchemaAdapterBase#getIdentifier()
     */
    @Override
    public URI getIdentifier() {
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.ext.common.SchemaAdapterBase#getShortName()
     */
    @Override
    public String getShortName() {
        return "bioCaseAdapter";
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.ext.common.SchemaAdapterBase#getCmdEntities(java.io.InputStream)
     */
    @Override
    public List<SpecimenOrObservationBase> getCmdEntities(InputStream inputStream) throws IOException {
        return null;
    }

}
