/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;

import java.util.UUID;

/**
 * Enum to indicate how the retrieved specimens are associated to a taxon.
 * <br>
 * <b>Note:</b>This status is <b>ignored</b> if the configurator has either a name
 * or a taxon set via {@link #setAssociatedTaxonNameUuid(UUID)} or
 * {@link #setAssociatedTaxonUuid(UUID)}
 *
 * @author pplitzner
 */
public enum AssignmentStatus{
    INDIVIDUALS_ASSOCIATION,
    TYPE_DESIGNATION,
    DETERMINATION,
    NONE
}
