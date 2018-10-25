// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author pplitzner
 * @since 16.04.2018
 *
 */
public class TaxonRowWrapperDTO extends RowWrapperDTO<TaxonDescription> {

    private static final long serialVersionUID = 5198447592554976471L;


    public TaxonRowWrapperDTO(TaxonDescription description, TaxonNode taxonNode) {
        super(description, taxonNode);
    }

}
