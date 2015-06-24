// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 * @date 24 Jun 2015
 *
 */
public class CdmEntityIdentifier {


    private final int id;
    private final Class<? extends CdmBase> cdmClass;

    public CdmEntityIdentifier(int id, Class cdmClass) {
        this.id = id;
        this.cdmClass = cdmClass;
    }
}
